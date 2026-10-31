package vn.duongngang.monitor;

import android.content.*;
import android.security.keystore.*;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import org.json.*;

public final class Store {
  private static final int TRAVEL_VERSION = 4;
  private static final int AXLE_VERSION = 3;
  private static final int SELF_EVIDENCE_VERSION = 3;
  private final Context context;
  public final android.content.SharedPreferences prefs;

  public Store(Context c) {
    context = c;
    prefs = c.getSharedPreferences("monitor", 0);
  }

  private javax.crypto.SecretKey key() throws Exception {
    KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
    ks.load(null);
    if (!ks.containsAlias("rail.credentials")) {
      KeyGenerator g = KeyGenerator.getInstance("AES", "AndroidKeyStore");
      g.init(
          new KeyGenParameterSpec.Builder(
                  "rail.credentials", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
              .build());
      g.generateKey();
    }
    return (javax.crypto.SecretKey) ks.getKey("rail.credentials", null);
  }

  public String account() {
    return prefs.getString("url", "") + "\n" + prefs.getString("user", "");
  }

  public Rules.Route route() {
    try {
      var array = new JSONArray(prefs.getString("route", "[]"));
      List<String> points = new ArrayList<>();
      for (int i = 0; i < array.length(); i++) points.add(array.getString(i));
      return Rules.accountRoute(prefs.getString("user", ""), new Rules.Route(points));
    } catch (Exception invalid) {
      return new Rules.Route(List.of());
    }
  }

  public static String rangeKey(boolean demo, boolean from) {
    return (demo ? "demoRange" : "range") + (from ? "From" : "To");
  }

  public String rangeFrom(Rules.Route route, boolean demo) {
    return selectedRange(route, demo)[0];
  }

  public String rangeTo(Rules.Route route, boolean demo) {
    return selectedRange(route, demo)[1];
  }

  private String[] selectedRange(Rules.Route route, boolean demo) {
    String from = prefs.getString(rangeKey(demo, true), route.first());
    String to = prefs.getString(rangeKey(demo, false), route.last());
    return route.validRange(from, to)
        ? new String[] {from, to}
        : new String[] {route.first(), route.last()};
  }

  public List<Rules.Event> saveForAccount(String expected, WebSource.DayData data) {
    synchronized (Store.class) {
      if (!account().equals(expected))
        throw new IllegalStateException("Tài khoản đã thay đổi; bỏ kết quả cũ");
      List<Rules.Event> added = save(data.events());
      Rules.Route effective = Rules.accountRoute(prefs.getString("user", ""), data.route());
      prefs.edit().putString("route", new JSONArray(effective.points()).toString()).apply();
      return added;
    }
  }

  public boolean historyCleanupDue(long now) {
    return prefs.getLong("retentionCutoff", 0) < Rules.historyCutoff(now);
  }

  /** Checkpoint first, delete second: long-running trains survive removal of old raw rows. */
  public int maintainHistory(String expected, long now) {
    synchronized (Store.class) {
      if (!account().equals(expected)) throw new IllegalStateException("Tài khoản đã thay đổi");
      long cutoff = Rules.historyCutoff(now);
      if (!historyCleanupDue(now)) return 0;
      prepareSelfEvidence(expected, now);
      updateTravelState(now);
      if (!prefs.contains("occupiedPoints")) {
        JSONArray occupied = new JSONArray();
        for (var point : Rules.occupancies(route(), events(0, now + 1)))
          occupied.put(new JSONArray().put(point.crossing()).put(point.direction()).put(point.at()));
        prefs.edit().putString("occupiedPoints", occupied.toString()).apply();
      }
      // Flush the checkpoint synchronously before the destructive database commit. If the
      // process dies during cleanup, a restart must still know about trains older than cutoff.
      if (!prefs.edit().putLong("retentionPreparedAt", cutoff).commit())
        throw new IllegalStateException("Chưa lưu được trạng thái trước khi dọn lịch sử");
      int removed;
      try (var database = db()) {
        removed = database.delete("events", "at<?", new String[] {"" + cutoff});
        // Persist the import floor as well: old pages must not grow the archive again.
        prefs.edit().putLong("retentionCutoff", cutoff).apply();
        if (removed > 0) database.execSQL("VACUUM");
      }
      Map<String, Long> pending = pendingSelf();
      pending.values().removeIf(at -> at < cutoff);
      prefs.edit().putString("pendingSelf", new JSONObject(pending).toString()).apply();
      return removed;
    }
  }

  public synchronized List<Rules.Event> crossingEvents(String crossing, long from, long to) {
    List<Rules.Event> out = new ArrayList<>();
    try (var database = db(); var cursor = database.rawQuery(
        "SELECT * FROM events WHERE replace(lower(km),' ','') IN (?,?) AND at>=? AND at<? ORDER BY at DESC",
        new String[] {Rules.km(crossing).toLowerCase(Locale.ROOT), "km" + Rules.km(crossing).toLowerCase(Locale.ROOT), "" + from, "" + to})) {
      appendRows(cursor, out);
    }
    out.removeIf(Rules::isExcludedAxleFault);
    out.sort(Comparator.comparingLong(Rules.Event::at).reversed().thenComparing(Rules.Event::id));
    return out;
  }

  public List<Rules.Event> activeFaults() {
    List<Rules.Event> result = new ArrayList<>();
    try {
      JSONArray list = new JSONArray(prefs.getString("activeFaults", "[]"));
      for (int i = 0; i < list.length(); i++) {
        JSONArray row = list.getJSONArray(i);
        List<String> counts = new ArrayList<>();
        for (int j = 0; j < row.getJSONArray(4).length(); j++)
          counts.add(row.getJSONArray(4).getString(j));
        result.add(
            new Rules.Event(
                row.getString(0),
                row.getLong(1),
                row.getString(2),
                row.getString(3),
                true,
                counts));
      }
    } catch (JSONException ignored) {
    }
    result.removeIf(Rules::isExcludedAxleFault);
    result.sort(
        java.util.Comparator.comparingLong(Rules.Event::at)
            .reversed()
            .thenComparing(Rules.Event::id));
    return result;
  }

  public List<Rules.Event> occupationEvents() {
    List<Rules.Event> result = new ArrayList<>();
    try {
      JSONArray list = new JSONArray(prefs.getString("occupiedPoints", "[]"));
      for (int i = 0; i < list.length(); i++) {
        JSONArray row = list.getJSONArray(i);
        result.add(
            new Rules.Event(
                "occupied:" + row.getString(0),
                row.getLong(2),
                row.getString(0),
                row.getInt(1) > 0 ? "Tàu lẻ" : "Tàu chẵn",
                false,
                List.of()));
      }
    } catch (JSONException ignored) {
    }
    return result;
  }

  /**
   * Carry unresolved faults across midnight without scanning the whole archive on the UI thread.
   */
  public List<Rules.Event> updateActiveFaults(
      String expected, long replayFrom, long now, long baseline, List<String> disproved) {
    synchronized (Store.class) {
      if (!account().equals(expected)) throw new IllegalStateException("Tài khoản đã thay đổi");
      if (!axleReady()) discardExcludedAxleAlerts();
      long day = Rules.faultBaseline(0, now);
      List<Rules.Event> axleAlerts = syncAxleAlerts(expected, day, now + 1);
      List<Rules.Event> input = activeFaults();
      input.removeIf(e -> Rules.isAxleFault(e) || e.at() < baseline || disproved.contains(e.id()));
      input.addAll(axleAlerts);
      // A cached self-trigger may predate the newest delta page. Include its reset even
      // after upgrading from a version that did not yet recognize the manual button.
      long from = replayFrom;
      for (var fault : input) if (Rules.isSelfTrigger(fault)) from = Math.min(from, fault.at());
      List<Rules.Event> recent = events(from, now + 1);
      for (var e : recent) if (!Rules.isAxleFault(e) && e.at() >= baseline) input.add(e);
      List<Rules.Event> active = Rules.activeFaults(input);
      JSONArray list = new JSONArray();
      for (var e : active)
        list.put(
            new JSONArray()
                .put(e.id())
                .put(e.at())
                .put(e.crossing())
                .put(e.text())
                .put(new JSONArray(e.counts())));
      List<Rules.Event> occupied = occupationEvents();
      occupied.addAll(recent);
      JSONArray points = new JSONArray();
      for (var point : Rules.occupancies(route(), occupied))
        points.put(new JSONArray().put(point.crossing()).put(point.direction()).put(point.at()));
      prefs
          .edit()
          .putString("activeFaults", list.toString())
          .putString("occupiedPoints", points.toString())
          .putInt("axleVersion", AXLE_VERSION)
          .apply();
      updateTravelState(now);
      return active;
    }
  }

  public boolean axleReady() {
    return prefs.getInt("axleVersion", 0) == AXLE_VERSION;
  }

  public boolean selfEvidenceReady() {
    return prefs.getInt("selfEvidenceVersion", 0) == SELF_EVIDENCE_VERSION;
  }

  /** Reassess old inferred records once; never turn historical arrivals into new alarms. */
  public void prepareSelfEvidence(String expected, long now) {
    synchronized (Store.class) {
      if (!account().equals(expected) || selfEvidenceReady()) return;
      List<Rules.Event> saved = events(0, now + 1);
      List<Rules.Event> original = new ArrayList<>();
      Map<String, Rules.Event> rawById = new HashMap<>(), inferred = new HashMap<>();
      for (var e : saved) {
        if (!Rules.isGenerated(e)) {
          original.add(e);
          rawById.put(e.id(), e);
        } else if (Rules.isSelfTrigger(e)) inferred.put(e.id(), e);
      }
      for (var e : activeFaults()) if (Rules.isSelfTrigger(e)) inferred.put(e.id(), e);
      Set<String> candidates = new HashSet<>();
      for (var e : Rules.suspectedSelfTriggers(route(), original, 0)) candidates.add(e.id());
      List<String> disproved = new ArrayList<>();
      boolean rebuildCheckpoint = false;
      for (var e : inferred.values()) {
        var raw = rawById.get(e.id().substring(5));
        if (raw != null
            && route().crossings().contains(Rules.km(raw.crossing()))
            && !candidates.contains(raw.id())) {
          disproved.add(e.id());
          rebuildCheckpoint |= e.at() < prefs.getLong("travelCheckpointAt", 0);
        }
      }
      Map<String, Long> pending = pendingSelf();
      pending.keySet().retainAll(candidates);
      if (!disproved.isEmpty()) {
        discardInferences(expected, disproved);
        var manager = context.getSystemService(android.app.NotificationManager.class);
        if (manager != null)
          for (String id : disproved) manager.cancel(100 + Math.floorMod(id.hashCode(), 1000000));
        if (rebuildCheckpoint) prefs.edit().remove("travelVersion").apply();
        long baseline = Rules.faultBaseline(prefs.getLong("baselineFrom", 0), now);
        updateActiveFaults(expected, baseline, now, baseline, disproved);
      }
      prefs
          .edit()
          .putString("pendingSelf", new JSONObject(pending).toString())
          .putInt("selfEvidenceVersion", SELF_EVIDENCE_VERSION)
          .apply();
    }
  }

  /** Remove only obsolete derived alerts, across all days; preserve original website records. */
  private void discardExcludedAxleAlerts() {
    Set<String> obsolete = new HashSet<>();
    try {
      var cached = new JSONArray(prefs.getString("activeFaults", "[]"));
      for (int i = 0; i < cached.length(); i++) {
        var row = cached.getJSONArray(i);
        var event =
            new Rules.Event(
                row.getString(0),
                row.getLong(1),
                row.getString(2),
                row.getString(3),
                true,
                List.of());
        if (Rules.isExcludedAxleFault(event)) obsolete.add(event.id());
      }
    } catch (JSONException ignored) {
    }
    try (var db = db()) {
      try (var cursor =
          db.rawQuery("SELECT id,km FROM events WHERE id GLOB ?", new String[] {"axle:[1-6]:*"})) {
        while (cursor.moveToNext())
          if (Rules.isAxleExempt(cursor.getString(1))) obsolete.add(cursor.getString(0));
      }
      for (String id : obsolete) db.delete("events", "id=?", new String[] {id});
    }
    var manager = context.getSystemService(android.app.NotificationManager.class);
    if (manager != null)
      for (String id : obsolete) manager.cancel(100 + Math.floorMod(id.hashCode(), 1000000));
  }

  /** Recompute derived warnings after late uploads; never edit the original website records. */
  public List<Rules.Event> syncAxleAlerts(String expected, long from, long to) {
    synchronized (Store.class) {
      if (!account().equals(expected)) throw new IllegalStateException("Tài khoản đã thay đổi");
      List<Rules.Event> records = events(from, to);
      List<Rules.Event> alerts = Rules.axleAlerts(route(), records);
      Map<String, Rules.Event> wanted = new HashMap<>();
      for (var e : alerts) wanted.put(e.id(), e);
      try (var db = db()) {
        for (var e : records)
          if (Rules.isAxleFault(e) && !e.equals(wanted.get(e.id())))
            db.delete("events", "id=?", new String[] {e.id()});
      }
      save(alerts);
      return alerts;
    }
  }

  /**
   * Persist per-sensor daily notice suppression, including across restarts and backfilled pages.
   */
  public boolean claimAxleNotice(Rules.Event event, long now) {
    synchronized (Store.class) {
      if (!Rules.isAxleFault(event)) return true;
      if (Rules.isAxleExempt(event.crossing())) return false;
      long day = Rules.faultBaseline(0, now);
      if (Rules.faultBaseline(0, event.at()) != day) return false;
      String prefix = day + "|";
      Set<String> notices = new HashSet<>();
      try {
        JSONArray stored = new JSONArray(prefs.getString("axleNotices", "[]"));
        for (int i = 0; i < stored.length(); i++)
          if (stored.getString(i).startsWith(prefix)) notices.add(stored.getString(i));
      } catch (JSONException ignored) {
      }
      String sensor = prefix + Rules.km(event.crossing()) + "|" + event.id().charAt(5);
      boolean large = Rules.isLargeAxleFault(event);
      String key = large ? sensor + "|large|" + event.id() : sensor + "|daily";
      boolean first = notices.add(key);
      if (event.text().contains("lần trong ngày")) notices.add(sensor + "|daily");
      prefs.edit().putString("axleNotices", new JSONArray(notices).toString()).apply();
      return first;
    }
  }

  private Rules.TravelState readTravelState(String key) {
    List<Rules.Journey> result = new ArrayList<>();
    try {
      JSONArray list = new JSONArray(prefs.getString(key, "[]"));
      for (int i = 0; i < list.length(); i++) {
        JSONArray j = list.getJSONArray(i);
        result.add(
            new Rules.Journey(
                j.getString(0),
                j.getString(1),
                j.isNull(2) ? null : j.getString(2),
                j.getInt(3),
                j.getLong(4),
                j.getBoolean(5),
                j.getBoolean(6),
                j.getBoolean(7),
                j.isNull(8) ? null : j.getString(8),
                j.optLong(9, j.getLong(4)),
                j.optBoolean(10, false)));
      }
    } catch (JSONException ignored) {
    }
    return new Rules.TravelState(result);
  }

  private String travelJson(Rules.TravelState state) {
    JSONArray list = new JSONArray();
    for (Rules.Journey j : state.journeys())
      list.put(
          new JSONArray()
              .put(j.id())
              .put(j.from())
              .put(j.to() == null ? JSONObject.NULL : j.to())
              .put(j.direction())
              .put(j.at())
              .put(j.passed())
              .put(j.released())
              .put(j.reached())
              .put(j.successorId() == null ? JSONObject.NULL : j.successorId())
              .put(j.lastAt())
              .put(j.selfTriggered()));
    return list.toString();
  }

  public Rules.TravelState travelState() {
    return travelReady() ? readTravelState("travelState") : Rules.TravelState.empty();
  }

  public boolean travelReady() {
    return prefs.getInt("travelVersion", 0) == TRAVEL_VERSION
        && prefs.contains("travelState")
        && new JSONArray(route().points()).toString().equals(prefs.getString("travelRoute", ""));
  }

  /** Rebuild obsolete diagram caches from raw reports, without touching credentials or faults. */
  public void prepareTravelState(String expected, long now) {
    synchronized (Store.class) {
      if (account().equals(expected) && !travelReady()) updateTravelState(now);
    }
  }

  /**
   * A rolling checkpoint preserves long intervals and permits backfills within the last two days.
   */
  private void updateTravelState(long now) {
    long boundary =
        java.time.Instant.ofEpochMilli(now)
            .atZone(WebSource.ZONE)
            .toLocalDate()
            .minusDays(1)
            .atStartOfDay(WebSource.ZONE)
            .toInstant()
            .toEpochMilli();
    String topology = new JSONArray(route().points()).toString();
    long checkpointAt = prefs.getLong("travelCheckpointAt", boundary);
    Rules.TravelState checkpoint = readTravelState("travelCheckpoint");
    if (prefs.getInt("travelVersion", 0) != TRAVEL_VERSION
        || !topology.equals(prefs.getString("travelRoute", ""))
        || checkpointAt > boundary) {
      checkpointAt = boundary;
      checkpoint = Rules.travelState(route(), Rules.TravelState.empty(), events(0, boundary));
    }
    if (checkpointAt < boundary)
      checkpoint = Rules.travelState(route(), checkpoint, events(checkpointAt, boundary));
    Rules.TravelState current = Rules.travelState(route(), checkpoint, events(boundary, now + 1));
    prefs
        .edit()
        .putString("travelRoute", topology)
        .putInt("travelVersion", TRAVEL_VERSION)
        .putLong("travelCheckpointAt", boundary)
        .putString("travelCheckpoint", travelJson(checkpoint))
        .putString("travelState", travelJson(current))
        .apply();
  }

  /** Clear the current fault panel at Vietnamese midnight; retain archive and train occupation. */
  public void resetDailyFaults(long now) {
    synchronized (Store.class) {
      long day = Rules.faultBaseline(0, now);
      if (prefs.getLong("faultDay", 0) == day) return;
      long baseline = Rules.faultBaseline(prefs.getLong("baselineFrom", 0), now);
      updateActiveFaults(account(), baseline, now, baseline, List.of());
      prefs
          .edit()
          .putLong("baselineFrom", baseline)
          .putLong("faultDay", day)
          .putString("pendingSelf", "{}")
          .apply();
    }
  }

  public Map<String, Long> pendingSelf() {
    Map<String, Long> result = new HashMap<>();
    try {
      JSONObject data = new JSONObject(prefs.getString("pendingSelf", "{}"));
      for (Iterator<String> keys = data.keys(); keys.hasNext(); ) {
        String key = keys.next();
        result.put(key, data.getLong(key));
      }
    } catch (JSONException ignored) {
    }
    return result;
  }

  public void discardInferences(String expected, List<String> ids) {
    synchronized (Store.class) {
      if (!account().equals(expected)) throw new IllegalStateException("Tài khoản đã thay đổi");
      for (String id : ids)
        if (!id.startsWith("self:"))
          throw new IllegalArgumentException("Không được xóa bản tin gốc của web");
      try (var db = db()) {
        for (String id : ids) db.delete("events", "id=?", new String[] {id});
      }
    }
  }

  public void credentials(String url, String user, String password) throws Exception {
    synchronized (Store.class) {
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, key());
      String encrypted =
          Base64.encodeToString(c.getIV(), Base64.NO_WRAP)
              + ":"
              + Base64.encodeToString(
                  c.doFinal(password.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP);
      // Namespace data by endpoint AND account; never show a previous account's records.
      boolean changed =
          !url.equals(prefs.getString("url", "")) || !user.equals(prefs.getString("user", ""));
      if (changed) {
        context.deleteDatabase("events.db");
        var notifications = context.getSystemService(android.app.NotificationManager.class);
        for (var n : notifications.getActiveNotifications())
          if (n.getId() != 1) notifications.cancel(n.getId());
        prefs
            .edit()
            .remove("retentionPreparedAt")
            .remove("retentionCutoff")
            .remove("lastSuccess")
            .remove("coverage")
            .remove("baselineFrom")
            .remove("faultDay")
            .remove("selfFrom")
            .remove("pendingSelf")
            .remove("selfEvidenceVersion")
            .remove("activeFaults")
            .remove("occupiedPoints")
            .remove("travelState")
            .remove("travelCheckpoint")
            .remove("travelCheckpointAt")
            .remove("travelRoute")
            .remove("travelVersion")
            .remove("axleVersion")
            .remove("axleNotices")
            .remove("lastFull")
            .remove("route")
            .remove("rangeFrom")
            .remove("rangeTo")
            .putString("status", "Đang chờ danh sách tuyến từ web")
            .apply();
      }
      prefs
          .edit()
          .putString("url", url)
          .putString("user", user)
          .putString("password", encrypted)
          .apply();
    }
  }

  public String password() throws Exception {
    String s = prefs.getString("password", "");
    if (s.isEmpty()) return "";
    String[] parts = s.split(":");
    Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
    c.init(
        Cipher.DECRYPT_MODE,
        key(),
        new GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)));
    return new String(c.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), StandardCharsets.UTF_8);
  }

  private android.database.sqlite.SQLiteDatabase db() {
    var db = context.openOrCreateDatabase("events.db", 0, null);
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS events (id TEXT PRIMARY KEY, at INTEGER NOT NULL, km TEXT NOT"
            + " NULL, text TEXT NOT NULL, red INTEGER NOT NULL, counts TEXT NOT NULL)");
    db.execSQL("CREATE INDEX IF NOT EXISTS events_at ON events(at)");
    db.execSQL("CREATE INDEX IF NOT EXISTS events_km_at ON events(km,at)");
    return db;
  }

  public synchronized List<Rules.Event> save(List<Rules.Event> events) {
    List<Rules.Event> added = new ArrayList<>();
    try (var db = db()) {
      db.beginTransaction();
      try {
        long cutoff = prefs.getLong("retentionCutoff", 0);
        for (var e : events) {
          if (e.at() < cutoff) continue;
          ContentValues v = new ContentValues();
          v.put("id", e.id());
          v.put("at", e.at());
          v.put("km", e.crossing());
          v.put("text", e.text());
          v.put("red", e.red() ? 1 : 0);
          v.put("counts", new JSONArray(e.counts()).toString());
          if (db.insertWithOnConflict(
                  "events", null, v, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
              != -1) added.add(e);
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }
    return added;
  }

  public synchronized List<Rules.Event> events(long from, long to) {
    var out = new ArrayList<Rules.Event>();
    try (var db = db();
        var c =
            db.rawQuery(
                "SELECT * FROM events WHERE at>=? AND at<? ORDER BY at DESC",
                new String[] {"" + from, "" + to})) {
      appendRows(c, out);
    }
    out.removeIf(Rules::isExcludedAxleFault);
    return out;
  }

  private void appendRows(android.database.Cursor c, List<Rules.Event> out) {
    while (c.moveToNext()) {
      var counts = new ArrayList<String>();
      try {
        var a = new JSONArray(c.getString(5));
        for (int i = 0; i < a.length(); i++) counts.add(a.getString(i));
      } catch (JSONException ignored) {
      }
      out.add(
          new Rules.Event(
              c.getString(0),
              c.getLong(1),
              c.getString(2),
              c.getString(3),
              c.getInt(4) == 1,
              counts));
    }
  }

  /**
   * Read one day's incident starts, later device restorations and the next release of each self
   * trigger.
   */
  public synchronized List<Rules.Event> incidentEvents(long from, long to) {
    List<Rules.Event> out = events(from, to);
    out.removeIf(Rules::isAxleFault);
    out.addAll(Rules.axleAlerts(route(), out));
    Set<String> inferredPoints = new HashSet<>();
    for (var event : out) if (Rules.isSelfTrigger(event)) inferredPoints.add(event.crossing());
    try (var db = db()) {
      try (var cursor =
          db.rawQuery(
              "SELECT * FROM events WHERE at>=? AND red=0 AND text LIKE ? ORDER BY at DESC",
              new String[] {"" + to, "%khôi phục tốt%"})) {
        appendRows(cursor, out);
      }
      for (String point : inferredPoints)
        try (var cursor =
            db.rawQuery(
                "SELECT * FROM events WHERE km=? AND at>=? AND red=0 AND"
                    + " (text=? COLLATE NOCASE OR text LIKE ?) ORDER BY at",
                new String[] {point, "" + to, "Khôi phục hệ thống", "%n_t%"})) {
          var recoveries = new ArrayList<Rules.Event>();
          appendRows(cursor, recoveries);
          for (var recovery : recoveries)
            if (Rules.isManualRecovery(recovery.text())
                || recovery.text().trim().equalsIgnoreCase("Khôi phục hệ thống")) {
              out.add(recovery);
              break;
            }
        }
    }
    out.sort(Comparator.comparingLong(Rules.Event::at).reversed());
    return out;
  }
}
