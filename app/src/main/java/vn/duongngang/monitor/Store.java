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
      return new Rules.Route(points);
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
      prefs.edit().putString("route", new JSONArray(data.route().points()).toString()).apply();
      return added;
    }
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
      List<Rules.Event> input = activeFaults();
      input.removeIf(e -> e.at() < baseline || disproved.contains(e.id()));
      List<Rules.Event> recent = events(replayFrom, now + 1);
      for (var e : recent) if (e.at() >= baseline) input.add(e);
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
          .apply();
      return active;
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
            .remove("lastSuccess")
            .remove("coverage")
            .remove("baselineFrom")
            .remove("selfFrom")
            .remove("pendingSelf")
            .remove("activeFaults")
            .remove("occupiedPoints")
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
        for (var e : events) {
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
                "SELECT * FROM events WHERE km=? AND at>=? AND red=0 AND text=? COLLATE NOCASE"
                    + " ORDER BY at LIMIT 1",
                new String[] {point, "" + to, "Khôi phục hệ thống"})) {
          appendRows(cursor, out);
        }
    }
    out.sort(Comparator.comparingLong(Rules.Event::at).reversed());
    return out;
  }
}
