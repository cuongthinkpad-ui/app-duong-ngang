package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.*;
import android.graphics.Bitmap;
import java.io.FileOutputStream;
import java.lang.reflect.*;
import java.util.List;

/** Only synthetic screenshots, in isolated storage on a dedicated emulator. */
final class UiPreview {
  static void capture(Instrumentation runner) throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu"))
      throw new IllegalStateException("Emulator only");
    Context context = runner.getTargetContext().createDeviceProtectedStorageContext();
    Store store = new Store(context);
    store.prefs.edit().clear().commit();
    context.deleteDatabase("events.db");
    long now = System.currentTimeMillis();
    store.saveForAccount(
        store.account(),
        new WebSource.DayData(
            List.of(
                new Rules.Event(
                    "preview-arrival", now - 300000, "320+957", "Tàu lẻ", false, List.of()),
                new Rules.Event(
                    "preview-passage",
                    now - 180000,
                    "320+957",
                    "Tàu lẻ đã qua đường ngang",
                    false,
                    List.of()),
                new Rules.Event(
                    "preview-restore",
                    now - 120000,
                    "320+957",
                    "Khôi phục hệ thống",
                    false,
                    List.of()),
                new Rules.Event(
                    "preview-reverse", now - 360000, "313+800", "Tàu chẵn", false, List.of()),
                new Rules.Event(
                    "preview-reverse-pass",
                    now - 240000,
                    "313+800",
                    "Tàu chẵn đã qua đường ngang",
                    false,
                    List.of()),
                new Rules.Event(
                    "preview-old-train", now - 500000, "320+445", "Tàu lẻ", false, List.of()),
                new Rules.Event(
                    "preview-old-pass",
                    now - 400000,
                    "320+445",
                    "Tàu lẻ đã qua đường ngang",
                    false,
                    List.of()),
                new Rules.Event(
                    "preview-old-release",
                    now - 350000,
                    "320+445",
                    "Khôi phục hệ thống",
                    false,
                    List.of())),
            Rules.DEMO_ROUTE));
    store.updateActiveFaults(store.account(), 0, now, 0, List.of());
    store
        .prefs
        .edit()
        .putLong("lastSuccess", now)
        .putBoolean("running", true)
        .putString("status", "Đã cập nhật")
        .commit();
    MainActivity activity =
        (MainActivity)
            runner.startActivitySync(
                new Intent(runner.getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    try {
      run(runner, activity, store, "overview");
      shot(runner, "overview-clear-0.8.png");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "preview-bell", now - 60000, "328+325", "Chuông 2 hỏng", true, List.of()),
                  new Rules.Event(
                      "preview-barrier",
                      now - 90000,
                      "313+456",
                      "Cần chắn 1 mất trạng thái",
                      true,
                      List.of())),
              Rules.DEMO_ROUTE));
      store.updateActiveFaults(store.account(), 0, now, 0, List.of());
      run(runner, activity, store, "overview");
      shot(runner, "overview-0.8.png");
      run(runner, activity, store, "incidents");
      shot(runner, "incidents-0.8.png");
      run(runner, activity, store, "crossings");
      shot(runner, "crossings-0.8.png");
      run(runner, activity, store, "settings");
      shot(runner, "settings-0.8.png");
    } finally {
      runner.runOnMainSync(activity::finish);
      store.prefs.edit().clear().commit();
      context.deleteDatabase("events.db");
    }
  }

  private static void run(Instrumentation r, MainActivity a, Store s, String page)
      throws Exception {
    s.prefs.edit().putLong("lastSuccess", System.currentTimeMillis()).commit();
    Throwable[] error = {null};
    r.runOnMainSync(
        () -> {
          try {
            Field store = MainActivity.class.getDeclaredField("store");
            store.setAccessible(true);
            store.set(a, s);
            Field p = MainActivity.class.getDeclaredField("page");
            p.setAccessible(true);
            p.set(a, page);
            Method render = MainActivity.class.getDeclaredMethod("render");
            render.setAccessible(true);
            render.invoke(a);
          } catch (Throwable t) {
            error[0] = t;
          }
        });
    r.waitForIdleSync();
    if (error[0] != null) throw new AssertionError(error[0]);
  }

  private static void shot(Instrumentation r, String name) throws Exception {
    Bitmap image = r.getUiAutomation().takeScreenshot();
    if (image == null) throw new IllegalStateException("Screenshot unavailable");
    try (var out =
        new FileOutputStream(new java.io.File(r.getTargetContext().getCacheDir(), name))) {
      image.compress(Bitmap.CompressFormat.PNG, 100, out);
    } finally {
      image.recycle();
    }
  }
}
