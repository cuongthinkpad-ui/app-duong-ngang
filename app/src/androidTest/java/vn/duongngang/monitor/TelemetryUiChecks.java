package vn.duongngang.monitor;

import android.animation.ValueAnimator;
import android.app.Instrumentation;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;

/** Viewport, real-data labels, filters and effect lifecycle on an isolated emulator fixture. */
final class TelemetryUiChecks {
  private final Instrumentation runner;
  private MainActivity activity;
  private int checks;

  TelemetryUiChecks(Instrumentation r) {
    runner = r;
  }

  private void check(boolean ok, String label) {
    checks++;
    if (!ok) throw new AssertionError(label);
  }

  private Object get(String name) throws Exception {
    Field f = MainActivity.class.getDeclaredField(name);
    f.setAccessible(true);
    return f.get(activity);
  }

  private void set(String name, Object value) throws Exception {
    Field f = MainActivity.class.getDeclaredField(name);
    f.setAccessible(true);
    f.set(activity, value);
  }

  private void call(String name) throws Exception {
    Method m = MainActivity.class.getDeclaredMethod(name);
    m.setAccessible(true);
    m.invoke(activity);
  }

  private interface Step {
    void run() throws Exception;
  }

  private void ui(Step step) {
    Throwable[] error = {null};
    runner.runOnMainSync(
        () -> {
          try {
            step.run();
          } catch (Throwable e) {
            error[0] = e;
          }
        });
    runner.waitForIdleSync();
    if (error[0] != null) throw new AssertionError(error[0]);
  }

  private String labels(View v) {
    String result = v instanceof TextView ? ((TextView) v).getText() + "\n" : "";
    if (v instanceof ViewGroup group)
      for (int i = 0; i < group.getChildCount(); i++) result += labels(group.getChildAt(i));
    return result;
  }

  private void page(String page) throws Exception {
    set("page", page);
    call("render");
  }

  private boolean fits(View view) {
    if (view instanceof ViewGroup group) {
      for (int i = 0; i < group.getChildCount(); i++) {
        View child = group.getChildAt(i);
        if (child.getVisibility() == View.GONE) continue;
        if (child.getBottom() > group.getHeight() - group.getPaddingBottom() + 2
            || child.getRight() > group.getWidth() - group.getPaddingRight() + 2
            || !fits(child)) return false;
      }
    }
    return true;
  }

  int run() throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu"))
      throw new IllegalStateException("Emulator only");
    Context isolated = runner.getTargetContext().createDeviceProtectedStorageContext();
    Store store = new Store(isolated);
    store.prefs.edit().clear().commit();
    isolated.deleteDatabase("events.db");
    store.credentials("https://rail.example.test/", "telemetry-checks", "test-only");
    long now = System.currentTimeMillis();
    List<Rules.Event> events = new ArrayList<>();
    int n = 0;
    for (String point : Rules.DEMO_ROUTE.crossings())
      events.add(
          new Rules.Event(
              "baseline-" + n++,
              now - 600000,
              point,
              "Khôi phục hệ thống",
              false,
              List.of("98", "98", "98", "98", "98", "98")));
    events.add(new Rules.Event("even-1", now - 280000, "314+550", "Tàu chẵn", false, List.of()));
    events.add(
        new Rules.Event(
            "even-pass-1",
            now - 270000,
            "314+550",
            "Tàu chẵn đã qua đường ngang",
            false,
            List.of()));
    events.add(new Rules.Event("even-2", now - 180000, "313+800", "Tàu chẵn", false, List.of()));
    events.add(
        new Rules.Event(
            "even-pass-2",
            now - 170000,
            "313+800",
            "Tàu chẵn đã qua đường ngang",
            false,
            List.of()));
    events.add(new Rules.Event("odd", now - 60000, "320+957", "Tàu lẻ", false, List.of()));
    events.add(
        new Rules.Event(
            "odd-pass", now - 50000, "320+957", "Tàu lẻ đã qua đường ngang", false, List.of()));
    events.add(
        new Rules.Event(
            "barrier", now - 400000, "313+800", "Cần chắn 1 mất trạng thái", true, List.of()));
    events.add(new Rules.Event("bell", now - 240000, "321+780", "Chuông 2 hỏng", true, List.of()));
    events.add(
        new Rules.Event(
            "power", now - 300000, "320+445", "Mất nguồn xoay chiều 220VAC", true, List.of()));
    events.add(
        new Rules.Event(
            "recovered-power",
            now - 120000,
            "328+325",
            "Mất nguồn xoay chiều 220VAC",
            true,
            List.of()));
    events.add(
        new Rules.Event(
            "restore-power",
            now - 100000,
            "328+325",
            "Nguồn xoay chiều 220VAC khôi phục tốt",
            false,
            List.of()));
    for (int i = 0; i < 6; i++)
      events.add(
          new Rules.Event(
              "count-" + i,
              now - 370000 + 10000 * i,
              "313+800",
              "Khôi phục hệ thống",
              false,
              List.of("98", "98", "98", i == 5 ? "128" : "101", "98", "98")));
    store.saveForAccount(store.account(), new WebSource.DayData(events, Rules.DEMO_ROUTE));
    store.updateActiveFaults(store.account(), 0, now, 0, List.of());
    store.prefs.edit().putLong("lastSuccess", now).putString("status", "Đã cập nhật").commit();
    activity =
        (MainActivity)
            runner.startActivitySync(
                new Intent(runner.getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    try {
      ui(
          () -> {
            set("store", store);
            page("overview");
          });
      ui(
          () -> {
            check(
                !((TextView) get("liveBadge")).getText().toString().equals("LIVE"),
                "stopped monitor never claims LIVE");
            store.prefs.edit().putBoolean("running", true).commit();
            call("refreshContent");
            check(
                ((TextView) get("liveBadge")).getText().toString().equals("LIVE"),
                "fresh running session shows LIVE");
            LinearLayout dashboard = (LinearLayout) get("content");
            check(
                dashboard.getChildCount() == 2
                    && dashboard.getChildAt(1).getBottom() <= dashboard.getHeight(),
                "dashboard fits available height");
            check(get("pageScroll") == null, "overview has no vertical scroller");
            String shown = labels(dashboard);
            check(
                shown.contains("CB4 đếm thừa 30 trục") && shown.contains("Chuông 2 hỏng"),
                "raw equipment identity and derived axle warning are visible");
            check(
                !labels((View) get("screen")).contains("Designed by"),
                "signature absent from overview");
            activity.onWindowFocusChanged(false);
            check(
                get("lightPulse") == null && ((View) get("headerMark")).getAlpha() == 1f,
                "focus loss stops pulse without dimming the logo");
            activity.onWindowFocusChanged(true);
            check(
                (get("lightPulse") != null) == ValueAnimator.areAnimatorsEnabled(),
                "focus callback resumes pulse only when system animations enabled");
            ValueAnimator pulse = (ValueAnimator) get("lightPulse");
            if (pulse != null) {
              pulse.setCurrentPlayTime(0);
              float low = ((View) get("headerMark")).getAlpha();
              pulse.setCurrentPlayTime(1400);
              check(
                  low < ((View) get("headerMark")).getAlpha()
                      && ((View) get("screen")).getAlpha() == 1f,
                  "pulse changes logo brightness while the full screen stays opaque");
            } else
              check(
                  ((View) get("headerMark")).getAlpha() == 1f,
                  "disabled animations leave static logo visible");
            // Finish the explicit animation check before waiting for UI idle on a software
            // emulator.
            activity.onWindowFocusChanged(false);
          });
      UiPreview.shot(runner, activity, "telemetry-overview-0.14.png");
      ui(
          () -> {
            View dashboard = (View) get("content");
            int width = dashboard.getWidth(), height = dashboard.getHeight();
            int compact = Math.round(300 * activity.getResources().getDisplayMetrics().density);
            dashboard.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(compact, View.MeasureSpec.EXACTLY));
            dashboard.layout(0, 0, width, compact);
            check(
                fits(dashboard),
                "compact dashboard keeps all rows and controls inside their cards");
            dashboard.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
            dashboard.layout(0, 0, width, height);
          });
      ui(
          () -> {
            store.prefs.edit().putLong("lastSuccess", now - 3600000).commit();
            call("refreshContent");
            check(
                !((TextView) get("liveBadge")).getText().toString().equals("LIVE"),
                "stale data loses LIVE badge");
            store
                .prefs
                .edit()
                .putLong("lastSuccess", now)
                .putString("rangeFrom", "313+456")
                .putString("rangeTo", "313+800")
                .commit();
            call("refreshContent");
            check(
                ((View) get("routeView"))
                    .getContentDescription()
                    .toString()
                    .contains("11 đường ngang và 2 ga"),
                "range narrowing keeps complete account route");
            store.prefs.edit().remove("rangeFrom").remove("rangeTo").commit();
            page("crossings");
            String shown = labels((View) get("content"));
            check(
                shown.contains("11 đường ngang") && !shown.contains("14 đường ngang"),
                "crossing totals are real data");
            check(
                !shown.contains("Ắc quy") && !shown.contains("90°") && !shown.contains("86%"),
                "no fabricated readings from visual mockup");
          });
      UiPreview.shot(runner, activity, "telemetry-crossings-0.14.png");
      ui(
          () -> {
            set("filter", "Có lỗi");
            page("crossings");
            String shown = labels((View) get("content"));
            check(
                shown.contains("Km 313+800") && !shown.contains("Km 312+111"),
                "fault filter lists only affected crossings");
            page("incidents");
            shown = labels((View) get("content"));
            check(
                shown.indexOf("Mất nguồn xoay chiều 220VAC") < shown.indexOf("Chuông 2 hỏng"),
                "all incidents ordered by occurrence including newer recovered event");
            check(
                shown.contains("✓ Đã khôi phục") && shown.contains("Chưa khôi phục"),
                "recovery and active states preserved");
          });
      UiPreview.shot(runner, activity, "telemetry-incidents-0.14.png");
      ui(
          () -> {
            page("settings");
            String shown = labels((View) get("screen"));
            check(shown.contains("Designed by Cao Xuân Cường"), "signature in settings only");
            check(
                shown.contains("Thử chuông báo lỗi") && shown.contains("Cài đặt chạy nền"),
                "operational controls retained");
            check(
                shown.contains("30 giây") && shown.contains("2 phút") && shown.contains("5 phút"),
                "all polling intervals retained");
          });
      UiPreview.shot(runner, activity, "telemetry-settings-0.14.png");
      ui(() -> ((ScrollView) get("pageScroll")).fullScroll(View.FOCUS_DOWN));
      UiPreview.shot(runner, activity, "telemetry-settings-bottom-0.14.png");
      return checks;
    } finally {
      store.prefs.edit().putBoolean("running", false).commit();
      ui(activity::finish);
      store.prefs.edit().clear().commit();
      isolated.deleteDatabase("events.db");
    }
  }
}
