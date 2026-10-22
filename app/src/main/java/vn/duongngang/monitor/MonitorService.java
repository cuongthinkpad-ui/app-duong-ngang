package vn.duongngang.monitor;

import android.app.*;
import android.content.*;
import android.os.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.JSONObject;

public class MonitorService extends Service {
  public static final String UPDATE = "vn.duongngang.monitor.UPDATE";
  private ScheduledExecutorService worker;
  private PowerManager.WakeLock wake;
  private Store store;

  @Override
  public void onCreate() {
    super.onCreate();
    store = new Store(this);
    var manager = getSystemService(NotificationManager.class);
    manager.createNotificationChannel(
        new NotificationChannel("monitor", "Theo dõi nền", NotificationManager.IMPORTANCE_LOW));
    manager.createNotificationChannel(
        new NotificationChannel(
            "faults_sound", "Sự cố có chuông", NotificationManager.IMPORTANCE_HIGH));
    var silent =
        new NotificationChannel(
            "faults_silent", "Sự cố im lặng", NotificationManager.IMPORTANCE_HIGH);
    silent.setSound(null, null);
    silent.enableVibration(false);
    manager.createNotificationChannel(silent);
  }

  private PendingIntent open() {
    return PendingIntent.getActivity(
        this,
        0,
        new Intent(this, MainActivity.class),
        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int id) {
    if ((intent == null && !store.prefs.getBoolean("running", false))
        || (intent != null && "STOP".equals(intent.getAction()))) {
      stopSelf();
      return START_NOT_STICKY;
    }
    if (intent != null && "RESTART".equals(intent.getAction()) && worker != null) {
      worker.shutdownNow();
      worker = null;
      source = null;
      firstPoll = true;
    }
    var notification =
        new Notification.Builder(this, "monitor")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Đang theo dõi đường ngang")
            .setContentText("Chạm để xem tình trạng cập nhật")
            .setContentIntent(open())
            .setOngoing(true)
            .build();
    startForeground(1, notification);
    if (worker == null) {
      wake =
          ((PowerManager) getSystemService(POWER_SERVICE))
              .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DuongNgang:Monitoring");
      wake.setReferenceCounted(false);
      wake.acquire(600000L);
      worker = Executors.newSingleThreadScheduledExecutor();
      worker.scheduleAtFixedRate(
          this::poll, 0, store.prefs.getInt("interval", 30), TimeUnit.SECONDS);
    }
    store.prefs.edit().putBoolean("running", true).apply();
    return START_STICKY;
  }

  private WebSource source;
  private String sourceAccount = "";
  private boolean firstPoll = true;

  private void poll() {
    String account = store.account();
    if (wake != null) wake.acquire(600000L);
    try {
      synchronized (Store.class) {
        if (!store.account().equals(account) || Thread.currentThread().isInterrupted()) return;
        if (source == null || !sourceAccount.equals(account)) {
          source =
              new WebSource(
                  store.prefs.getString("url", ""),
                  store.prefs.getString("user", ""),
                  store.password());
          sourceAccount = account;
        }
      }
      LocalDate today = LocalDate.now(WebSource.ZONE);
      long dayStart = today.atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
      long previousStart =
          today.minusDays(1).atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
      long last = store.prefs.getLong("lastSuccess", 0), started = System.currentTimeMillis();
      boolean reset = Rules.needsNewBaseline(last, started, firstPoll);
      boolean newDay =
          last > 0
              && Instant.ofEpochMilli(last).atZone(WebSource.ZONE).toLocalDate().isBefore(today);
      boolean history = last == 0 || reset || newDay || store.prefs.getLong("coverage", 0) == 0;
      boolean full = history || started - store.prefs.getLong("lastFull", 0) >= 15 * 60000L;
      List<Rules.Event> incoming = new ArrayList<>();
      if (history) {
        if (last == 0) {
          store
              .prefs
              .edit()
              .putString("status", "Đang đọc báo cáo ngày trước để nối trạng thái…")
              .apply();
          sendBroadcast(new Intent(UPDATE).setPackage(getPackageName()));
        }
        incoming.addAll(source.readDay(today.minusDays(1)).events());
      }
      Set<String> known = new HashSet<>();
      if (!full)
        for (var e : store.events(dayStart, started + 1))
          if (!Rules.isSelfTrigger(e)) known.add(e.id());
      var data = full ? source.readDay(today) : source.readDelta(today, known);
      incoming.addAll(data.events());
      if (!data.connected()) reset = true;
      incoming.sort(Comparator.comparingLong(Rules.Event::at));
      synchronized (Store.class) {
        if (!store.account().equals(account) || Thread.currentThread().isInterrupted()) return;
        List<Rules.Event> added =
            new ArrayList<>(
                store.saveForAccount(account, new WebSource.DayData(incoming, data.route())));
        long now = System.currentTimeMillis();
        long coverage = history ? previousStart : store.prefs.getLong("coverage", dayStart);
        if (history && !incoming.isEmpty()) coverage = Math.max(coverage, incoming.get(0).at());
        if (reset && !data.connected()) coverage = now;
        long selfFrom = last == 0 || reset ? now : store.prefs.getLong("selfFrom", now);
        if (reset) store.prefs.edit().putLong("baselineFrom", now).apply();
        List<Rules.Event> cached = store.events(Math.max(previousStart, coverage), now + 1);
        List<Rules.Event> original = new ArrayList<>();
        for (var event : cached) if (!Rules.isSelfTrigger(event)) original.add(event);
        var candidates = Rules.suspectedSelfTriggers(data.route(), original, coverage);
        Set<String> candidateIds = new HashSet<>();
        for (var event : candidates) candidateIds.add(event.id());
        List<String> disproved = new ArrayList<>();
        for (var event : cached)
          if (Rules.isSelfTrigger(event) && !candidateIds.contains(event.id().substring(5)))
            disproved.add(event.id());
        store.discardInferences(account, disproved);
        Map<String, Long> pending = last == 0 || reset ? new HashMap<>() : store.pendingSelf();
        Set<String> freshArrivals = new HashSet<>();
        for (var event : added)
          if (Rules.direction(event.text()) != 0) freshArrivals.add(event.id());
        List<Rules.Event> eligible = new ArrayList<>();
        if (last > 0 && !reset)
          for (var candidate : candidates)
            if (freshArrivals.contains(candidate.id()) || pending.containsKey(candidate.id()))
              eligible.add(candidate);
        var confirmed =
            Rules.confirmedSelfTriggers(
                eligible,
                pending,
                now,
                0,
                Math.max(30000L, 1000L * store.prefs.getInt("interval", 30)));
        added.addAll(store.saveForAccount(account, new WebSource.DayData(confirmed, data.route())));
        long baseline = store.prefs.getLong("baselineFrom", 0);
        long replayFrom = incoming.stream().mapToLong(Rules.Event::at).min().orElse(now);
        for (var event : confirmed) replayFrom = Math.min(replayFrom, event.at());
        var active = store.updateActiveFaults(account, replayFrom, now, baseline, disproved);
        Set<String> open = new HashSet<>();
        Set<Integer> notificationIds = new HashSet<>();
        for (var event : active) {
          open.add(event.id());
          if (data.route()
              .inRange(
                  event.crossing(),
                  store.rangeFrom(data.route(), false),
                  store.rangeTo(data.route(), false))) notificationIds.add(notificationId(event));
        }
        var manager = getSystemService(NotificationManager.class);
        for (var notification : manager.getActiveNotifications())
          if (notification.getId() != 1 && !notificationIds.contains(notification.getId()))
            manager.cancel(notification.getId());
        if (last > 0 && !reset)
          for (var event : added)
            if (event.red()
                && open.contains(event.id())
                && notificationIds.contains(notificationId(event))) notifyFault(event);
        firstPoll = false;
        var preferences =
            store
                .prefs
                .edit()
                .putLong("lastSuccess", now)
                .putLong("coverage", coverage)
                .putLong("selfFrom", selfFrom)
                .putString("pendingSelf", new JSONObject(pending).toString())
                .putString(
                    "status",
                    reset ? "Đã đặt lại theo dõi · Chưa ghi nhận lỗi mới" : "Đã cập nhật");
        if (full) preferences.putLong("lastFull", now);
        preferences.apply();
      }
    } catch (Exception error) {
      if (!store.account().equals(account) || Thread.currentThread().isInterrupted()) return;
      store
          .prefs
          .edit()
          .putString(
              "status", error.getMessage() == null ? "Gián đoạn theo dõi" : error.getMessage())
          .apply();
      if (error instanceof WebSource.AuthenticationException) stopSelf();
    }
    sendBroadcast(new Intent(UPDATE).setPackage(getPackageName()));
  }

  private int notificationId(Rules.Event event) {
    return 100 + Math.floorMod(event.id().hashCode(), 1000000);
  }

  private void notifyFault(Rules.Event e) {
    boolean sound = store.prefs.getBoolean("sound", true);
    var n =
        new Notification.Builder(this, sound ? "faults_sound" : "faults_silent")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
                        .withZone(WebSource.ZONE)
                        .format(Instant.ofEpochMilli(e.at()))
                    + " · "
                    + e.crossing())
            .setContentText(e.text())
            .setStyle(new Notification.BigTextStyle().bigText(e.text()))
            .setContentIntent(open())
            .setAutoCancel(true)
            .build();
    getSystemService(NotificationManager.class).notify(notificationId(e), n);
  }

  @Override
  public void onDestroy() {
    if (worker != null) worker.shutdownNow();
    if (wake != null && wake.isHeld()) wake.release();
    store.prefs.edit().putBoolean("running", false).apply();
    sendBroadcast(new Intent(UPDATE).setPackage(getPackageName()));
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
