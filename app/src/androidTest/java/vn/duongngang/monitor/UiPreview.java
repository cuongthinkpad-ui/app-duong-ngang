package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.*;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ScrollView;
import java.io.FileOutputStream;
import java.lang.reflect.*;
import java.util.*;

/** Synthetic route-stage screenshots, in isolated storage on a dedicated emulator. */
final class UiPreview {
  private static int sequence;
  private static long start;

  static void capture(Instrumentation runner) throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu"))
      throw new IllegalStateException("Emulator only");
    Context context = runner.getTargetContext().createDeviceProtectedStorageContext();
    Store store = new Store(context);
    store.prefs.edit().clear().commit();
    context.deleteDatabase("events.db");
    sequence = 0;
    start = System.currentTimeMillis() - 600000;
    report(store, "312+111", "Tàu lẻ");
    MainActivity activity =
        (MainActivity)
            runner.startActivitySync(
                new Intent(runner.getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    try {
      run(runner, activity, store, "overview");
      shot(runner, "route-approach-0.10.png");
      report(store, "312+111", "Tàu lẻ đã qua đường ngang");
      report(store, "312+111", "Khôi phục hệ thống");
      run(runner, activity, store, "overview");
      shot(runner, "route-passed-0.10.png");
      // Deliberately omit the approach message: passage must still close the previous interval.
      report(store, "313+456", "Tàu lẻ đã qua đường ngang");
      run(runner, activity, store, "overview");
      shot(runner, "route-missing-approach-0.10.png");
      report(store, "313+456", "Khôi phục hệ thống");
      report(store, "313+456", "Tàu lẻ đã qua đường ngang");
      run(runner, activity, store, "overview");
      shot(runner, "route-late-phase-0.10.png");
      report(store, "313+800", "Tàu lẻ");
      report(store, "313+800", "Tàu lẻ đã qua đường ngang");
      report(store, "313+800", "Khôi phục hệ thống");
      report(store, "314+550", "Tàu lẻ");
      report(store, "314+550", "Tàu lẻ đã qua đường ngang");
      run(runner, activity, store, "overview");
      shot(runner, "route-station-0.10.png");
      report(store, "314+550", "Khôi phục hệ thống");
      report(store, "320+445", "Tàu lẻ");
      run(runner, activity, store, "overview");
      shot(runner, "route-from-station-0.10.png");
      report(store, "320+445", "Tàu lẻ đã qua đường ngang");
      report(store, "320+445", "Khôi phục hệ thống");
      report(store, "320+957", "Tàu chẵn");
      report(store, "320+957", "Tàu chẵn đã qua đường ngang");
      report(store, "320+445", "Tàu lẻ đã qua đường ngang");
      run(runner, activity, store, "overview");
      shot(runner, "reverse-cleanup-0.10.png");
      report(store, "320+445", "Tàu chẵn");
      report(store, "320+445", "Tàu chẵn đã qua đường ngang");
      report(store, "320+445", "Khôi phục hệ thống");
      report(store, "320+445", "Tàu lẻ");
      report(store, "320+445", "Tàu lẻ đã qua đường ngang");
      report(store, "320+445", "Khôi phục hệ thống");
      report(store, "334+830", "Tàu chẵn");
      report(store, "334+830", "Tàu chẵn đã qua đường ngang");
      report(store, "334+830", "Khôi phục hệ thống");
      run(runner, activity, store, "incidents");
      shot(runner, "incidents-empty-0.10.png");
      report(store, "328+325", "Chuông 2 hỏng");
      report(store, "313+456", "Cần chắn 1 mất trạng thái");
      run(runner, activity, store, "overview");
      shot(runner, "overview-0.10.png");
      run(runner, activity, store, "incidents");
      shot(runner, "incidents-0.10.png");
      run(runner, activity, store, "settings");
      runner.runOnMainSync(
          () -> {
            try {
              Field f = MainActivity.class.getDeclaredField("pageScroll");
              f.setAccessible(true);
              ((ScrollView) f.get(activity)).fullScroll(View.FOCUS_DOWN);
            } catch (ReflectiveOperationException failure) {
              throw new AssertionError(failure);
            }
          });
      runner.waitForIdleSync();
      shot(runner, "settings-alarm-0.10.png");
    } finally {
      runner.runOnMainSync(activity::finish);
      store.prefs.edit().clear().commit();
      context.deleteDatabase("events.db");
    }
  }

  private static void report(Store store, String point, String text) {
    long now = System.currentTimeMillis();
    int n = ++sequence;
    store.saveForAccount(
        store.account(),
        new WebSource.DayData(
            List.of(
                new Rules.Event(
                    "preview-" + n,
                    start + n * 2000L,
                    point,
                    text,
                    text.contains("hỏng") || text.contains("mất trạng thái"),
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

  static void shot(Instrumentation r, String name) throws Exception {
    Bitmap image = r.getUiAutomation().takeScreenshot();
    if (image == null) throw new IllegalStateException("Screenshot unavailable");
    try (var out =
        new FileOutputStream(new java.io.File(r.getTargetContext().getCacheDir(), name))) {
      image.compress(Bitmap.CompressFormat.PNG, 100, out);
    } finally {
      image.recycle();
    }
  }

  /** Capture the app window itself; unrelated emulator System UI dialogs are not app content. */
  static void shot(Instrumentation r, MainActivity activity, String name) throws Exception {
    Bitmap[] result = {null};
    r.runOnMainSync(
        () -> {
          View root = activity.getWindow().getDecorView();
          result[0] =
              Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
          root.draw(new android.graphics.Canvas(result[0]));
        });
    try (var out =
        new FileOutputStream(new java.io.File(r.getTargetContext().getCacheDir(), name))) {
      result[0].compress(Bitmap.CompressFormat.PNG, 100, out);
    } finally {
      result[0].recycle();
    }
  }
}
