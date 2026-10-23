package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.*;
import android.os.SystemClock;
import java.lang.reflect.*;
import java.util.*;

/**
 * Repeatable synthetic tab timings; compatible with the v0.12 target for before/after comparison.
 */
final class TabSwitchChecks {
  static String run(Instrumentation runner) throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu"))
      throw new IllegalStateException("Dedicated emulator required");
    Context context = runner.getTargetContext().createDeviceProtectedStorageContext();
    Store store = new Store(context);
    store.prefs.edit().clear().commit();
    context.deleteDatabase("events.db");
    store.credentials("https://rail.example.test/", "tab-timing", "test-only");
    long now = System.currentTimeMillis();
    long start = Math.max(Rules.faultBaseline(0, now) + 1000, now - 3600000);
    List<Rules.Event> events = new ArrayList<>();
    for (int i = 0; i < 1000; i++)
      events.add(
          new Rules.Event(
              "timing-" + i,
              start + i * 1000,
              Rules.DEMO_ROUTE.crossings().get(i % Rules.DEMO_ROUTE.crossings().size()),
              i % 100 == 0 ? "Chuông 1 hỏng" : "Cần chắn 1 đã nâng",
              i % 100 == 0,
              List.of()));
    store.saveForAccount(store.account(), new WebSource.DayData(events, Rules.DEMO_ROUTE));
    store.updateActiveFaults(store.account(), start, now, 0, List.of());
    store.prefs.edit().putLong("lastSuccess", now).putString("status", "Đã cập nhật").commit();
    MainActivity activity =
        (MainActivity)
            runner.startActivitySync(
                new Intent(runner.getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    try {
      Field data = MainActivity.class.getDeclaredField("store");
      Field page = MainActivity.class.getDeclaredField("page");
      Method render = MainActivity.class.getDeclaredMethod("render");
      data.setAccessible(true);
      page.setAccessible(true);
      render.setAccessible(true);
      StringBuilder timings = new StringBuilder();
      for (String target : List.of("crossings", "incidents", "settings", "overview")) {
        Throwable[] failure = {null};
        long[] elapsed = {0};
        runner.runOnMainSync(
            () -> {
              try {
                data.set(activity, store);
                page.set(activity, target);
                long begin = SystemClock.elapsedRealtime();
                render.invoke(activity);
                elapsed[0] = SystemClock.elapsedRealtime() - begin;
              } catch (Throwable error) {
                failure[0] = error;
              }
            });
        runner.waitForIdleSync();
        if (failure[0] != null) throw new AssertionError(failure[0]);
        timings.append(target).append('=').append(elapsed[0]).append("ms ");
      }
      return "1000 synthetic events / 11 crossings, render callback: " + timings + "\n";
    } finally {
      runner.runOnMainSync(activity::finish);
      store.prefs.edit().clear().commit();
      context.deleteDatabase("events.db");
    }
  }
}
