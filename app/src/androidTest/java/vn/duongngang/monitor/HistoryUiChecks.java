package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.time.*;
import java.util.*;

/** Historical day regressions, using an isolated synthetic account on an emulator only. */
final class HistoryUiChecks {
  private final Instrumentation runner;
  private MainActivity activity;
  private int checks;
  HistoryUiChecks(Instrumentation runner) { this.runner = runner; }
  private void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }
  private void set(String name, Object value) throws Exception {
    Field f = MainActivity.class.getDeclaredField(name); f.setAccessible(true); f.set(activity, value);
  }
  private Object get(String name) throws Exception {
    Field f = MainActivity.class.getDeclaredField(name); f.setAccessible(true); return f.get(activity);
  }
  private Object call(String name) throws Exception {
    Method m = MainActivity.class.getDeclaredMethod(name); m.setAccessible(true); return m.invoke(activity);
  }
  private interface Step { void run() throws Exception; }
  private void ui(Step step) {
    Throwable[] error = {null};
    runner.runOnMainSync(() -> { try { step.run(); } catch (Throwable e) { error[0] = e; } });
    runner.waitForIdleSync();
    if (error[0] != null) throw new AssertionError(error[0]);
  }
  private String labels(View v) {
    StringBuilder out = new StringBuilder();
    if (v instanceof TextView t) out.append(t.getText()).append('\n');
    if (v instanceof ViewGroup g) for (int i = 0; i < g.getChildCount(); i++) out.append(labels(g.getChildAt(i)));
    return out.toString();
  }
  private TextView find(View v, String label) {
    if (v instanceof TextView t && t.getText().toString().equals(label)) return t;
    if (v instanceof ViewGroup g) for (int i = 0; i < g.getChildCount(); i++) {
      TextView result = find(g.getChildAt(i), label); if (result != null) return result;
    }
    return null;
  }
  private void click(String label) throws Exception {
    View target = find((View) get("content"), label);
    if (target == null) throw new AssertionError("Missing action " + label);
    while (!target.isClickable() && target.getParent() instanceof View parent) target = parent;
    if (!target.isClickable() || !target.performClick()) throw new AssertionError("Action was not clicked: " + label);
  }
  private String row(String label) throws Exception {
    TextView t = find((View) get("content"), label);
    if (t == null) throw new AssertionError("Missing row " + label);
    return labels((View) t.getParent());
  }
  private void await(String text) throws Exception {
    long deadline = System.currentTimeMillis() + 20000;
    while (System.currentTimeMillis() < deadline) {
      boolean[] ready = {false};
      ui(() -> ready[0] = labels((View) get("content")).contains(text));
      if (ready[0]) return;
      Thread.sleep(100);
    }
    throw new AssertionError("Timed out waiting for " + text);
  }
  int run() throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu")) throw new IllegalStateException("Emulator only");
    Context isolated = runner.getTargetContext().createDeviceProtectedStorageContext();
    Store fixture = new Store(isolated);
    fixture.prefs.edit().clear().commit(); isolated.deleteDatabase("events.db");
    try {
      fixture.credentials("https://history.example.test/", "fixture", "test-only");
      LocalDate day = LocalDate.now(WebSource.ZONE).minusDays(2);
      long start = day.atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
      var route = new Rules.Route(List.of("312+111", "313+800", "314+550"));
      List<Rules.Event> events = new ArrayList<>();
      events.add(new Rules.Event("measurement", start + 3600000, "313+800", "Khôi phục hệ thống", false,
          List.of("98", "98", "118", "98", "98", "98")));
      events.add(new Rules.Event("sensor-power", start + 3700000, "313+800", "Điện áp cảm biến 6 < 5V", true, List.of()));
      events.add(new Rules.Event("common-power", start + 3800000, "313+800", "Mất nguồn xoay chiều 220VAC", true, List.of()));
      events.add(new Rules.Event("normal-barrier", start + 3900000, "313+800", "Cần chắn 2 đã nâng", false, List.of()));
      // Keep a real pending journey so the interval gate is tested with nonempty train data.
      events.add(new Rules.Event("upstream-arrival", start + 3990000, "312+111", "Tàu lẻ", false, List.of()));
      events.add(new Rules.Event("arrival", start + 4000000, "313+800", "Tàu lẻ", false, List.of()));
      fixture.saveHistoricalDay(fixture.account(), day, new WebSource.DayData(events, route));
      fixture.prefs.edit().putString("monitorDate", day.toString()).putInt("interval", 30).commit();
      activity = (MainActivity) runner.startActivitySync(new Intent(runner.getTargetContext(), MainActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      ui(() -> {
        set("store", fixture); set("detailDay", day); set("selected", day); set("historyDay", day);
        set("followToday", false); set("historyFollowToday", false);
        set("selectedCrossing", "313+800"); set("page", "detail"); call("render");
      });
      await("CB3 đếm thừa 20 trục");
      ui(() -> {
        check(row("Cảm biến").contains("CB3 đếm thừa 20 trục"), "cold historical detail shows axle fault under sensors");
        check(row("Cảm biến").contains("Điện áp cảm biến 6 < 5V"), "sensor voltage stays in sensor row");
        check(!row("Điện áp").contains("cảm biến") && row("Điện áp").contains("220VAC"), "common voltage row excludes sensor voltage");
        check(row("Cần chắn").contains("Tốt") && !row("Cần chắn").contains("đã nâng"), "normal barrier activity is hidden from health row");
        check(row("Chuông").contains("Tốt") && row("Đèn").contains("Tốt"), "no open faults displays good after full load");
        check(labels((View) get("content")).contains("Đếm trục lỗi"), "historical detail includes axle statistics");
        click("Chi tiết");
        check(get("page").equals("crossingHistory"), "details action actually navigates to crossing history");
        check(get("historyDay").equals(day), "nested details retain chosen historical day");
      });
      await("01:05:00");
      ui(() -> {
        String text = labels((View) get("content"));
        check(text.contains(day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))), "nested history labels requested day");
        check(text.contains("Cần chắn 2 đã nâng"), "normal barrier activity remains in raw history");
        set("page", "overview"); call("render");
      });
      long until = System.currentTimeMillis() + 20000;
      boolean[] lit = {false};
      while (System.currentTimeMillis() < until && !lit[0]) {
        ui(() -> lit[0] = !((Rules.TravelState) call("diagramState")).journeys().isEmpty());
        if (!lit[0]) Thread.sleep(100);
      }
      check(lit[0], "thirty-second diagram has real historical journey");
      for (int interval : new int[] {120, 300}) ui(() -> {
        fixture.prefs.edit().putInt("interval", interval).commit(); call("refreshContent");
        check(((Rules.TravelState) call("diagramState")).journeys().isEmpty(), "slow interval removes train diagram " + interval);
        check(((TextView) get("routeNote")).getText().toString().contains("30 giây"), "disabled diagram explains interval requirement");
      });
      LocalDate pagingDay = day.minusDays(1);
      long pagingStart = pagingDay.atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
      List<Rules.Event> paging = new ArrayList<>();
      for (int i = 0; i < 25; i++) {
        long at = pagingStart + 60000L + i * 1200000L;
        paging.add(new Rules.Event("page-arrival-" + i, at, "313+800", "Tàu lẻ", false, List.of()));
        paging.add(new Rules.Event("page-restore-" + i, at + 60000, "313+800", "Khôi phục hệ thống", false, List.of()));
      }
      fixture.saveHistoricalDay(fixture.account(), pagingDay, new WebSource.DayData(paging, route));
      ui(() -> { set("historyDay", pagingDay); set("page", "crossingHistory"); call("render"); });
      await("Hiển thị thêm · 20/25 nhóm sự kiện");
      ui(() -> {
        check(!labels((View) get("content")).contains("00:01:00"), "long day initially constructs twenty newest groups");
        click("Hiển thị thêm · 20/25 nhóm sự kiện");
        check(labels((View) get("content")).contains("00:01:00"), "load more exposes earliest event without losing records");
        check(find((View) get("content"), "Hiển thị thêm · 20/25 nhóm sự kiện") == null,
            "all twenty-five groups are available after expansion");
      });
      return checks;
    } finally {
      if (activity != null) ui(() -> activity.finish());
      fixture.prefs.edit().clear().commit(); isolated.deleteDatabase("events.db");
    }
  }
}
