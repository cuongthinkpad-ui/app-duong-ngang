package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.time.*;
import java.util.*;

/** Exercises the rendered list and diagram through fault recovery on an isolated emulator. */
final class FaultDisplayChecks {
  private final Instrumentation runner;
  private MainActivity activity;
  private int checks;
  FaultDisplayChecks(Instrumentation runner) { this.runner = runner; }
  private void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }
  private Object get(String name) throws Exception {
    Field f = MainActivity.class.getDeclaredField(name); f.setAccessible(true); return f.get(activity);
  }
  private void set(String name, Object value) throws Exception {
    Field f = MainActivity.class.getDeclaredField(name); f.setAccessible(true); f.set(activity, value);
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
  private TextView find(View v, String label) {
    if (v instanceof TextView t && t.getText().toString().equals(label)) return t;
    if (v instanceof ViewGroup g) for (int i = 0; i < g.getChildCount(); i++) {
      TextView result = find(g.getChildAt(i), label); if (result != null) return result;
    }
    return null;
  }
  private View entry(String point) throws Exception {
    TextView title = find((View) get("content"), "Km " + point);
    if (title == null) throw new AssertionError("Missing crossing " + point);
    return (View) title.getParent().getParent();
  }
  private void await(String label) throws Exception {
    long until = System.currentTimeMillis() + 30000;
    while (System.currentTimeMillis() < until) {
      boolean[] ready = {false};
      ui(() -> ready[0] = find((View) get("content"), label) != null);
      if (ready[0]) return;
      Thread.sleep(100);
    }
    throw new AssertionError("Timed out: " + label);
  }
  private int color(String field) throws Exception {
    Field f = MainActivity.class.getDeclaredField(field); f.setAccessible(true); return f.getInt(null);
  }
  private int dp(float n) { return Math.round(n * activity.getResources().getDisplayMetrics().density); }
  private Bitmap diagram() throws Exception {
    View view = (View) get("routeView");
    Bitmap result = Bitmap.createBitmap(dp(600), dp(101), Bitmap.Config.ARGB_8888);
    view.layout(0, 0, result.getWidth(), result.getHeight());
    view.draw(new Canvas(result));
    return result;
  }
  private void pixels(boolean fault) throws Exception {
    set("page", "overview"); call("render");
    Bitmap b = diagram();
    try {
      check(b.getPixel(dp(25), dp(50)) == color(fault ? "RED" : "GREEN"),
          "crossing dot reflects unresolved fault, then returns to train green");
      check(b.getPixel(dp(385), dp(50)) == color("WARNING"), "station remains amber even with fault data");
      check(b.getPixel(dp(115), dp(50)) == color("GREEN"), "train segment stays green");
      int green = 0;
      for (int y = dp(3); y < dp(40); y++) for (int x = dp(50); x < dp(180); x++)
        if (b.getPixel(x, y) == color("GREEN")) green++;
      check(green > 0, "direction arrow and train label remain green");
    } finally { b.recycle(); }
  }
  int run() throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu")) throw new IllegalStateException("Emulator only");
    Context isolated = runner.getTargetContext().createDeviceProtectedStorageContext();
    Store fixture = new Store(isolated);
    fixture.prefs.edit().clear().commit(); isolated.deleteDatabase("events.db");
    try {
      fixture.credentials("https://fault-display.example.test/", "fixture", "test-only");
      LocalDate day = LocalDate.now(WebSource.ZONE).minusDays(2);
      long at = day.atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli() + 3600000;
      var route = new Rules.Route(List.of("312+111", "313+800", "319+200 (Vi)", "320+445"));
      fixture.saveForAccount(fixture.account(), new WebSource.DayData(List.of(), route));
      fixture.prefs.edit().putString("monitorDate", day.toString()).commit();
      activity = (MainActivity) runner.startActivitySync(new Intent(runner.getTargetContext(), MainActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      ui(() -> { set("store", fixture); set("page", "crossings"); call("render"); });
      await("Chưa tải đủ dữ liệu");
      ui(() -> check(find((View) get("content"), "Tốt") == null, "incomplete day never reports good"));
      List<Rules.Event> events = new ArrayList<>(List.of(
          new Rules.Event("arrival", at, "312+111", "Tàu lẻ", false, List.of()),
          new Rules.Event("passage", at + 1000, "312+111", "Tàu lẻ đã qua đường ngang", false, List.of()),
          new Rules.Event("bell", at + 2000, "312+111", "Chuông 1 hỏng", true, List.of()),
          new Rules.Event("light", at + 3000, "312+111", "Đèn đỏ 1 hỏng", true, List.of()),
          new Rules.Event("station", at + 4000, "319+200 (Vi)", "Chuông 1 hỏng", true, List.of())));
      fixture.saveHistoricalDay(fixture.account(), day, new WebSource.DayData(events, route));
      ui(() -> call("refreshContent")); await("2 sự cố");
      ui(() -> {
        TextView count = find(entry("312+111"), "2 sự cố");
        check(count != null && count.getCurrentTextColor() == color("RED"), "two unresolved faults count in red");
        check(find(entry("320+445"), "Tốt") != null, "complete day with no events at a crossing is good");
        pixels(true);
      });
      // Exercise refresh while keeping the same diagram View instance.
      Object[] original = {null}; ui(() -> original[0] = get("routeView"));
      events.add(new Rules.Event("bell-recovered", at + 5000, "312+111", "Chuông 1 khôi phục tốt", false, List.of()));
      fixture.saveHistoricalDay(fixture.account(), day, new WebSource.DayData(events, route));
      ui(() -> call("refreshContent"));
      long until = System.currentTimeMillis() + 30000;
      boolean[] one = {false};
      while (System.currentTimeMillis() < until && !one[0]) {
        ui(() -> one[0] = ((List<?>) call("activeFaults")).size() == 2);
        if (!one[0]) Thread.sleep(100);
      }
      ui(() -> {
        check(one[0], "partial recovery removes only one crossing fault");
        check(original[0] == get("routeView"), "poll refresh keeps diagram instance");
        Bitmap b = diagram();
        check(b.getPixel(dp(25), dp(50)) == color("RED"), "dot stays red until final fault recovery");
        b.recycle(); set("page", "crossings"); call("render");
        check(find(entry("312+111"), "1 sự cố") != null, "list count decrements after partial recovery");
      });
      events.add(new Rules.Event("light-recovered", at + 6000, "312+111", "Đèn đỏ 1 khôi phục tốt", false, List.of()));
      fixture.saveHistoricalDay(fixture.account(), day, new WebSource.DayData(events, route));
      ui(() -> call("refreshContent"));
      until = System.currentTimeMillis() + 30000;
      boolean[] good = {false};
      while (System.currentTimeMillis() < until && !good[0]) {
        ui(() -> good[0] = find(entry("312+111"), "Tốt") != null);
        if (!good[0]) Thread.sleep(100);
      }
      ui(() -> {
        check(good[0], "final recovery replaces count with good");
        check(find(entry("312+111"), "Tốt").getCurrentTextColor() == color("GREEN"), "good label is green");
        pixels(false);
        check(fixture.events(at, at + 10000).size() == events.size(), "presentation preserves every raw event");
      });
      return checks;
    } finally {
      if (activity != null) ui(() -> activity.finish());
      fixture.prefs.edit().clear().commit(); isolated.deleteDatabase("events.db");
    }
  }
}
