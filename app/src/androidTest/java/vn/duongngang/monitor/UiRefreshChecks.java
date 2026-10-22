package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.*;
import android.os.Build;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.JSONArray;

/** Actual window/view checks on a dedicated emulator, with isolated device-protected test data. */
final class UiRefreshChecks {
  private final Instrumentation runner;
  private int checks;
  private MainActivity activity;
  private Store store;

  UiRefreshChecks(Instrumentation runner) {
    this.runner = runner;
  }

  private void check(boolean valid, String message) {
    checks++;
    if (!valid) throw new AssertionError(message);
  }

  private Object field(Object target, String name) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.get(target);
  }

  private void invoke(String name) throws Exception {
    Method method = MainActivity.class.getDeclaredMethod(name);
    method.setAccessible(true);
    method.invoke(activity);
  }

  private void navigate(String page) throws Exception {
    Field f = MainActivity.class.getDeclaredField("page");
    f.setAccessible(true);
    f.set(activity, page);
    invoke("render");
  }

  private void ui(RunnableWithException action) {
    Throwable[] error = {null};
    runner.runOnMainSync(
        () -> {
          try {
            action.run();
          } catch (Throwable t) {
            error[0] = t;
          }
        });
    if (error[0] != null) throw new AssertionError(error[0]);
    runner.waitForIdleSync();
  }

  private interface RunnableWithException {
    void run() throws Exception;
  }

  private String labels(View view) {
    String result = view instanceof TextView ? ((TextView) view).getText().toString() + "\n" : "";
    if (view instanceof ViewGroup)
      for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
        result += labels(((ViewGroup) view).getChildAt(i));
    return result;
  }

  private EditText firstInput(View view) {
    if (view instanceof EditText) return (EditText) view;
    if (view instanceof ViewGroup)
      for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
        EditText found = firstInput(((ViewGroup) view).getChildAt(i));
        if (found != null) return found;
      }
    return null;
  }

  int run() throws Exception {
    if (!Build.HARDWARE.contains("ranchu") && !Build.HARDWARE.contains("goldfish"))
      throw new IllegalStateException("Dedicated emulator required");
    Context context = runner.getTargetContext().createDeviceProtectedStorageContext();
    store = new Store(context);
    store.prefs.edit().clear().commit();
    context.deleteDatabase("events.db");
    long now = System.currentTimeMillis();
    List<String> points = new ArrayList<>();
    for (int i = 0; i < 30; i++) points.add((100 + i) + "+000");
    Rules.Route route = new Rules.Route(points);
    List<Rules.Event> faults =
        List.of(
            new Rules.Event("old", now - 60000, points.get(0), "Chuông 1 hỏng", true, List.of()),
            new Rules.Event("new", now - 10000, points.get(0), "Chuông 3 hỏng", true, List.of()),
            new Rules.Event(
                "middle", now - 30000, points.get(0), "Chuông 2 hỏng", true, List.of()));
    store.saveForAccount(store.account(), new WebSource.DayData(faults, route));
    JSONArray cached = new JSONArray();
    for (Rules.Event event : faults)
      cached.put(
          new JSONArray()
              .put(event.id())
              .put(event.at())
              .put(event.crossing())
              .put(event.text())
              .put(new JSONArray()));
    store
        .prefs
        .edit()
        .putString("activeFaults", cached.toString())
        .putLong("lastSuccess", now)
        .putString("status", "Đã cập nhật")
        .commit();
    check(
        store.activeFaults().get(0).id().equals("new"),
        "unsorted cache returns latest fault first");
    activity =
        (MainActivity)
            runner.startActivitySync(
                new Intent(runner.getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    try {
      ui(
          () -> {
            Field f = MainActivity.class.getDeclaredField("store");
            f.setAccessible(true);
            f.set(activity, store);
            invoke("render");
          });
      View root = (View) field(activity, "screen"), routeView = (View) field(activity, "routeView");
      LinearLayout dashboard = (LinearLayout) field(activity, "content");
      ui(
          () -> {
            check(
                dashboard.getChildCount() == 2 && dashboard.getHeight() > 0,
                "dashboard filled before drawing");
            String visible = labels(dashboard);
            check(
                visible.indexOf("Chuông 3 hỏng") < visible.indexOf("Chuông 2 hỏng")
                    && visible.indexOf("Chuông 2 hỏng") < visible.indexOf("Chuông 1 hỏng"),
                "overview uses original fault times descending");
            ((HorizontalScrollView) routeView.getParent()).scrollTo(200, 0);
          });
      View trainCard = dashboard.getChildAt(0), faultCard = dashboard.getChildAt(1);
      int scrollX = ((View) routeView.getParent()).getScrollX();
      check(scrollX > 0, "long route is horizontally scrolled for retention check");
      for (int interval : new int[] {30, 120, 300}) {
        int direction = interval == 30 ? 1 : interval == 120 ? -1 : 0;
        long at = System.currentTimeMillis();
        store.saveForAccount(
            store.account(),
            new WebSource.DayData(
                List.of(
                    new Rules.Event(
                        "update-" + interval,
                        at,
                        points.get(0),
                        direction == 1
                            ? "Tàu lẻ"
                            : direction == -1 ? "Tàu chẵn" : "Khôi phục hệ thống",
                        false,
                        List.of())),
                route));
        store.updateActiveFaults(store.account(), 0, at, 0, List.of());
        store
            .prefs
            .edit()
            .putInt("interval", interval)
            .putLong("lastSuccess", at + interval)
            .commit();
        ui(
            () -> {
              ((Runnable) field(activity, "refresh")).run();
              check(
                  field(activity, "screen") == root
                      && field(activity, "content") == dashboard
                      && dashboard.getChildAt(0) == trainCard
                      && dashboard.getChildAt(1) == faultCard,
                  "poll retains root/dashboard at interval " + interval);
              check(
                  field(activity, "routeView") == routeView
                      && ((View) routeView.getParent()).getScrollX() == scrollX,
                  "poll retains route and scroll at interval " + interval);
              check(
                  ((int[]) field(routeView, "occupied"))[0] == direction,
                  "same route view shows updated train direction/release at interval " + interval);
            });
      }
      ui(
          () -> {
            android.graphics.drawable.ColorDrawable background =
                (android.graphics.drawable.ColorDrawable) root.getBackground();
            check(
                background.getColor() == 0xFF0C1220,
                "overview uses requested dark navy background");
            check(
                field(activity, "summaryCard") != null && !labels(root).contains("AN TOÀN"),
                "summary presents monitored data without asserting physical safety");
          });
      long transitAt = System.currentTimeMillis();
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-leg-arrival", transitAt, points.get(0), "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "ui-leg-passed",
                      transitAt + 1,
                      points.get(0),
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-leg-restored",
                      transitAt + 2,
                      points.get(0),
                      "Khôi phục hệ thống",
                      false,
                      List.of())),
              route));
      store.updateActiveFaults(store.account(), 0, transitAt + 10, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", transitAt + 10).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            var legs = (java.util.List<?>) field(routeView, "between");
            check(
                legs.size() == 1 && ((int[]) field(routeView, "occupied"))[0] == 0,
                "restored source leaves interval green without outside source occupation");
            check(
                field(activity, "routeView") == routeView && field(activity, "screen") == root,
                "between-crossing update retains the same canvas and window");
          });
      long arrivedAt = System.currentTimeMillis() + 10;
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-leg-destination", arrivedAt, points.get(1), "Tàu lẻ", false, List.of())),
              route));
      store.updateActiveFaults(store.account(), 0, arrivedAt + 10, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", arrivedAt + 10).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                ((java.util.List<?>) field(routeView, "between")).isEmpty()
                    && ((int[]) field(routeView, "occupied"))[1] == 1,
                "destination arrival replaces the interval on the existing diagram");
          });
      long passedAt = System.currentTimeMillis() + 20;
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-next-leg",
                      passedAt,
                      points.get(1),
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of())),
              route));
      store.updateActiveFaults(store.account(), 0, passedAt + 10, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", passedAt + 10).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            var legs = (java.util.List<?>) field(routeView, "between");
            check(
                legs.size() == 1
                    && ((Rules.Between) legs.get(0)).from().equals(points.get(1))
                    && ((Rules.Between) legs.get(0)).to().equals(points.get(2)),
                "only the next bounded interval lights after destination passage");
          });
      ui(
          () -> {
            AlarmSound sound = new AlarmSound();
            try {
              check(
                  sound.play(activity) && sound.isPlaying(),
                  "app alarm starts an actual audio player");
            } finally {
              sound.stop();
            }
            check(!sound.isPlaying(), "alarm can be stopped and released");
          });
      store.prefs.edit().putInt("interval", 120).commit();
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "self:ui-disabled",
                      System.currentTimeMillis(),
                      points.get(0),
                      "Nghi tự kích",
                      true,
                      List.of())),
              route));
      store.updateActiveFaults(store.account(), 0, System.currentTimeMillis() + 10, 0, List.of());
      ui(
          () -> {
            Method active = MainActivity.class.getDeclaredMethod("activeFaults");
            active.setAccessible(true);
            var faultsShown = (java.util.List<?>) active.invoke(activity);
            check(
                faultsShown.stream().noneMatch(e -> Rules.isSelfTrigger((Rules.Event) e)),
                "slower polling hides self-trigger inference while retaining actual website"
                    + " faults");
          });
      store.discardInferences(store.account(), List.of("self:ui-disabled"));
      store.prefs.edit().putInt("interval", 30).commit();
      ui(() -> navigate("incidents"));
      ui(
          () -> {
            String visible = labels((View) field(activity, "content"));
            check(
                visible.indexOf("Chuông 3 hỏng") < visible.indexOf("Chuông 2 hỏng")
                    && visible.indexOf("Chuông 2 hỏng") < visible.indexOf("Chuông 1 hỏng"),
                "incident tab uses descending original fault times");
          });
      ui(
          () -> {
            java.time.LocalDate today = java.time.LocalDate.now(WebSource.ZONE);
            Field selected = MainActivity.class.getDeclaredField("selected");
            selected.setAccessible(true);
            selected.set(activity, today.minusDays(1));
            ((Runnable) field(activity, "refresh")).run();
            check(
                selected.get(activity).equals(today),
                "default incident date follows today after rollover");
            Field follow = MainActivity.class.getDeclaredField("followToday");
            follow.setAccessible(true);
            follow.set(activity, false);
            selected.set(activity, today.minusDays(1));
            ((Runnable) field(activity, "refresh")).run();
            check(
                selected.get(activity).equals(today.minusDays(1)),
                "explicit historical date is not reset by periodic refresh");
            follow.set(activity, true);
          });
      ui(() -> navigate("settings"));
      ui(
          () -> {
            View settings = (View) field(activity, "screen");
            String visible = labels(settings);
            check(
                !visible.contains("Xem giao diện minh họa")
                    && !visible.contains("Bản 0.6")
                    && !visible.contains("Nghi tự kích được đối chiếu"),
                "preview and footer explanation removed");
            check(visible.contains("Designed by Cao Xuân Cường"), "signature retained in Settings");
            EditText url = firstInput(settings);
            check(
                url != null && url.getText().toString().equals("http://"),
                "URL defaults to editable http prefix");
            url.setText("");
            check(url.getText().length() == 0, "prefix can be deleted");
            url.setText("https://rail.example.test/");
            check(url.getText().toString().startsWith("https://"), "prefix can be replaced");
          });
      return checks;
    } finally {
      ui(() -> activity.finish());
      store.prefs.edit().clear().commit();
      context.deleteDatabase("events.db");
    }
  }
}
