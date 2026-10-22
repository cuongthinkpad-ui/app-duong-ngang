package vn.duongngang.monitor;

import android.app.*;
import android.content.*;
import android.os.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

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
    if (intent != null && "STOP".equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
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
      wake.acquire();
      worker = Executors.newSingleThreadScheduledExecutor();
      worker.scheduleWithFixedDelay(
          this::poll, 0, store.prefs.getInt("interval", 30), TimeUnit.SECONDS);
    }
    store.prefs.edit().putBoolean("running", true).apply();
    return START_NOT_STICKY;
  }

  private boolean firstPoll = true;

  private void poll() {
    String account = store.account();
    try {
      var source =
          new WebSource(
              store.prefs.getString("url", ""),
              store.prefs.getString("user", ""),
              store.password());
      LocalDate today = LocalDate.now(WebSource.ZONE);
      long last = store.prefs.getLong("lastSuccess", 0);
      long pollStart = System.currentTimeMillis();
      boolean reset = Rules.needsNewBaseline(last, pollStart, firstPoll);
      List<Rules.Event> events = new ArrayList<>();
      if (last > 0 && !reset) {
        LocalDate start = Instant.ofEpochMilli(last).atZone(WebSource.ZONE).toLocalDate();
        if (start.isBefore(today.minusDays(7)))
          throw new Exception("Gián đoạn quá 7 ngày; cần tải lại lịch sử trước khi theo dõi");
        for (LocalDate d = start; d.isBefore(today); d = d.plusDays(1))
          events.addAll(source.readDay(d));
      }
      events.addAll(source.readDay(today));
      events.sort(Comparator.comparingLong(Rules.Event::at));
      List<Rules.Event> added = store.saveForAccount(account, events);
      long now = System.currentTimeMillis();
      // First successful sync builds the baseline silently. No speculative self-trigger alerts yet.
      if (last > 0 && !reset)
        for (var e : added)
          if (e.red()
              && e.at() >= store.prefs.getLong("baselineFrom", 0)
              && Rules.inRange(
                  e.crossing(),
                  store.prefs.getString("rangeFrom", "312+111"),
                  store.prefs.getString("rangeTo", "334+830"))) notifyFault(e);
      if (reset) {
        store.prefs.edit().putLong("baselineFrom", now).apply();
        var notifications = getSystemService(NotificationManager.class);
        for (var notification : notifications.getActiveNotifications())
          if (notification.getId() != 1) notifications.cancel(notification.getId());
      }
      firstPoll = false;
      store
          .prefs
          .edit()
          .putLong("lastSuccess", now)
          .putString(
              "status", reset ? "Đã đặt lại theo dõi · Chưa ghi nhận lỗi mới" : "Đã cập nhật")
          .apply();
    } catch (Exception e) {
      if (!store.account().equals(account) || Thread.currentThread().isInterrupted()) return;
      store
          .prefs
          .edit()
          .putString("status", e.getMessage() == null ? "Gián đoạn theo dõi" : e.getMessage())
          .apply();
    }
    sendBroadcast(new Intent(UPDATE).setPackage(getPackageName()));
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
    getSystemService(NotificationManager.class)
        .notify(100 + Math.floorMod(e.id().hashCode(), 1000000), n);
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
