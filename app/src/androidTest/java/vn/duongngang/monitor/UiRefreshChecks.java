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

  private View findLabel(View view, String text) {
    if (view instanceof TextView label && label.getText().toString().equals(text)) return view;
    if (view instanceof ViewGroup group)
      for (int i = 0; i < group.getChildCount(); i++) {
        View match = findLabel(group.getChildAt(i), text);
        if (match != null) return match;
      }
    return null;
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
              if (interval == 30) {
                var visibleSegments = (java.util.List<?>) field(routeView, "segments");
                check(
                    visibleSegments.equals(List.of(new Rules.TrackSegment(null, points.get(0), 1))),
                    "incoming odd train lights the north edge rather than only the crossing dot");
              }
            });
      }
      ui(
          () -> {
            android.graphics.drawable.ColorDrawable background =
                (android.graphics.drawable.ColorDrawable) root.getBackground();
            check(
                background.getColor() == 0xFF060E1F,
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
            var legs = (java.util.List<?>) field(routeView, "segments");
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
                ((java.util.List<?>) field(routeView, "segments"))
                        .equals(List.of(new Rules.TrackSegment(points.get(0), points.get(1), 1)))
                    && ((int[]) field(routeView, "occupied"))[1] == 1,
                "destination approach keeps the same incoming interval green without disappearing");
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
            var legs = (java.util.List<?>) field(routeView, "segments");
            check(
                legs.size() == 1
                    && ((Rules.TrackSegment) legs.get(0)).from().equals(points.get(1))
                    && ((Rules.TrackSegment) legs.get(0)).to().equals(points.get(2)),
                "only the next bounded interval lights after destination passage");
          });
      long switchedAt = System.currentTimeMillis() + 30;
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-no-approach-pass",
                      switchedAt,
                      points.get(2),
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of())),
              route));
      store.updateActiveFaults(store.account(), 0, switchedAt + 10, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", switchedAt + 10).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                ((List<?>) field(routeView, "segments"))
                        .equals(List.of(new Rules.TrackSegment(points.get(2), points.get(3), 1)))
                    && field(activity, "routeView") == routeView,
                "passage immediately turns off the previous interval even with missing approach");
          });
      long reverseAt = System.currentTimeMillis() + 40;
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-new-reverse", reverseAt, points.get(3), "Tàu chẵn", false, List.of())),
              route));
      store.updateActiveFaults(store.account(), 0, reverseAt + 10, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", reverseAt + 10).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                ((List<?>) field(routeView, "segments"))
                    .equals(List.of(new Rules.TrackSegment(points.get(4), points.get(3), -1))),
                "new even observation removes the old odd interval on the real canvas");
          });
      long delayedAt = System.currentTimeMillis() + 50;
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-stale-pass",
                      delayedAt,
                      points.get(2),
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of())),
              route));
      store.updateActiveFaults(store.account(), 0, delayedAt + 10, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", delayedAt + 10).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                ((List<?>) field(routeView, "segments"))
                    .equals(List.of(new Rules.TrackSegment(points.get(4), points.get(3), -1))),
                "delayed old-direction phase does not relight a retired interval");
            List<Rules.TrackSegment> dense = new ArrayList<>();
            for (int i = 0; i < points.size() - 1; i++)
              dense.add(new Rules.TrackSegment(points.get(i), points.get(i + 1), 1));
            Field segmentField = routeView.getClass().getDeclaredField("segments");
            segmentField.setAccessible(true);
            Object originalSegments = segmentField.get(routeView);
            segmentField.set(routeView, dense);
            var bitmap =
                android.graphics.Bitmap.createBitmap(
                    routeView.getWidth(),
                    routeView.getHeight(),
                    android.graphics.Bitmap.Config.ARGB_8888);
            try {
              var canvas = new android.graphics.Canvas(bitmap);
              Method draw =
                  routeView
                      .getClass()
                      .getDeclaredMethod(
                          "drawDirection",
                          android.graphics.Canvas.class,
                          List.class,
                          float.class,
                          float.class,
                          int.class,
                          int.class);
              draw.setAccessible(true);
              List<float[]> boxes = new ArrayList<>();
              float density = activity.getResources().getDisplayMetrics().density;
              float spacing = (routeView.getWidth() - 60 * density) / (points.size() - 1);
              for (int i = 0; i < dense.size(); i++)
                draw.invoke(
                    routeView,
                    canvas,
                    boxes,
                    25 * density + spacing * (i + 0.5f),
                    50 * density,
                    1,
                    0xFF67D5AD);
              boolean bounded = boxes.size() == dense.size();
              for (int i = 0; i < boxes.size(); i++) {
                float[] box = boxes.get(i);
                bounded &=
                    box[0] - box[1] >= 0
                        && box[0] + box[1] <= routeView.getWidth()
                        && box[3] >= 0
                        && box[4] < 50 * density;
                for (int j = 0; j < i; j++) {
                  float[] previous = boxes.get(j);
                  if (box[2] == previous[2])
                    bounded &= Math.abs(box[0] - previous[0]) > box[1] + previous[1];
                }
              }
              check(
                  bounded,
                  "dense direction labels stay inside the canvas without overlapping or orphan"
                      + " arrows");
            } finally {
              bitmap.recycle();
              segmentField.set(routeView, originalSegments);
            }
          });
      store.prefs.edit().putInt("interval", 30).commit();
      var uiSelf =
          new Rules.Event(
              "self:ui-new-reverse", reverseAt, points.get(3), "Nghi tự kích", true, List.of());
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(uiSelf), route));
      store.updateActiveFaults(store.account(), 0, delayedAt + 20, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", delayedAt + 20).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                labels((View) field(activity, "screen")).contains("Nghi tự kích"),
                "active self-trigger is visible before manual recovery");
          });
      var uiReset =
          new Rules.Event(
              "ui-manual-reset",
              delayedAt + 30,
              points.get(3),
              "Ấn nút Phục hồi",
              false,
              List.of());
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(uiReset), route));
      store.updateActiveFaults(store.account(), 0, delayedAt + 40, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", delayedAt + 40).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                ((List<?>) field(routeView, "segments")).isEmpty()
                    && field(activity, "routeView") == routeView,
                "manual recovery turns off self-trigger on the same canvas");
            check(
                !labels((View) field(activity, "screen")).contains("Nghi tự kích"),
                "overview removes recovered self fault without rebuilding page");
          });
      AlarmSound sixChimes = new AlarmSound();
      try {
        ui(
            () -> {
              check(sixChimes.play(activity) && sixChimes.isPlaying(), "six-chime audio starts");
              var player = (android.media.MediaPlayer) field(sixChimes, "player");
              check(player.getDuration() == 12000, "six chimes have twelve seconds of playback");
            });
        android.os.SystemClock.sleep(6500);
        ui(() -> check(sixChimes.isPlaying(), "alarm continues beyond old three-chime duration"));
        // Software-emulated audio can drain more slowly than the wall clock. Bound the
        // completion wait separately; getDuration above verifies the exact media length.
        long completionDeadline = android.os.SystemClock.uptimeMillis() + 30000;
        while (sixChimes.isPlaying() && android.os.SystemClock.uptimeMillis() < completionDeadline)
          android.os.SystemClock.sleep(250);
        ui(
            () ->
                check(
                    !sixChimes.isPlaying(),
                    "six-chime alarm finishes and releases without looping"));
      } finally {
        ui(sixChimes::stop);
      }
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
            invoke("refreshContent");
            String emptyPage = labels((View) field(activity, "content"));
            check(
                emptyPage.contains("Không có sự cố") && !emptyPage.contains("sự cố phù hợp"),
                "empty incident page uses the owner's exact requested wording");
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
      store.credentials("https://rail.example.test/", "ui-axles", "test-only");
      long axleNow = System.currentTimeMillis();
      long axleStart = Math.max(Rules.faultBaseline(0, axleNow) + 1000, axleNow - 3600000);
      List<Rules.Event> axleRecords = new ArrayList<>();
      for (int i = 0; i < 6; i++)
        axleRecords.add(
            new Rules.Event(
                "ui-axle-" + i,
                axleStart + i * 1000,
                "313+800",
                "Khôi phục hệ thống",
                false,
                List.of("40", "40", "37", "40", "40", "40")));
      for (int i = 0; i < 5; i++)
        axleRecords.add(
            new Rules.Event(
                "ui-axle-below-threshold-" + i,
                axleStart + i * 1000,
                "312+111",
                "Khôi phục hệ thống",
                false,
                List.of("40", "40", "37", "40", "40", "40")));
      axleRecords.add(
          new Rules.Event(
              "ui-axle-large",
              axleStart + 7000,
              "313+800",
              "Khôi phục hệ thống",
              false,
              List.of("40", "40", "40", "52", "40", "40")));
      List<String> exemptPoints =
          List.of("320+445", "307+507", "309+056", "280+500", "343+350", "338+900");
      for (String exemptPoint : exemptPoints) {
        for (int i = 0; i < 7; i++)
          axleRecords.add(
              new Rules.Event(
                  "ui-exempt-" + exemptPoint + "-" + i,
                  axleStart + i * 1000,
                  exemptPoint,
                  "Khôi phục hệ thống",
                  false,
                  List.of("40", "40", i == 6 ? "28" : "37", "40", "40", "40")));
      }
      axleRecords.add(
          new Rules.Event(
              "ui-exempt-hardware",
              axleStart + 40000,
              "320+445",
              "Chuông 2 hỏng",
              true,
              List.of()));
      for (int i = 0; i < 15; i++)
        axleRecords.add(
            new Rules.Event(
                "ui-after-axle-" + i,
                axleStart + 8000 + i * 1000,
                "313+800",
                "Cần chắn 1 đã nâng",
                false,
                List.of()));
      List<String> axlePoints = new ArrayList<>(Rules.DEMO_ROUTE.points());
      for (String point : exemptPoints) if (!axlePoints.contains(point)) axlePoints.add(point);
      Rules.Route axleRoute = new Rules.Route(axlePoints);
      store.saveForAccount(store.account(), new WebSource.DayData(axleRecords, axleRoute));
      store.updateActiveFaults(store.account(), axleStart, axleNow, axleStart + 30000, List.of());
      store
          .prefs
          .edit()
          .putInt("interval", 300)
          .putLong("lastSuccess", axleNow)
          .putString("status", "Đã cập nhật")
          .commit();
      ui(() -> navigate("overview"));
      ui(
          () -> {
            String visible = labels((View) field(activity, "screen"));
            check(
                visible.contains("CB3 đếm sai 6 lần") && visible.contains("CB4 đếm thừa 12 trục"),
                "both axle warning reasons appear on overview at five-minute polling");
          });
      UiPreview.shot(runner, activity, "axle-overview-0.13.png");
      ui(
          () -> {
            Field point = MainActivity.class.getDeclaredField("selectedCrossing");
            point.setAccessible(true);
            point.set(activity, "313+800");
            navigate("detail");
            String visible = labels((View) field(activity, "screen"));
            check(
                visible.contains("Đếm trục lỗi")
                    && visible.contains("CB3 · 6 lần lệch từ 3 trục")
                    && !visible.contains("Đếm trục hôm nay")
                    && !visible.contains("Xem các lần đếm trục"),
                "one axle fault statistics section replaces the two separate entries");
            check(
                visible.contains("thiếu 3 trục") && visible.contains("thừa 12 trục"),
                "daily axle summary preserves amount and sign for each sensor");
          });
      UiPreview.shot(runner, activity, "axle-detail-0.13.png");
      ui(
          () -> {
            Field point = MainActivity.class.getDeclaredField("selectedCrossing");
            point.setAccessible(true);
            point.set(activity, "312+111");
            navigate("detail");
            String visible = labels((View) field(activity, "screen"));
            check(
                !visible.contains("Đếm trục lỗi") && visible.contains("CB3 thiếu 3 trục"),
                "five small errors remain notes without an axle fault statistics section");
          });
      ui(
          () -> {
            Field point = MainActivity.class.getDeclaredField("selectedCrossing");
            point.setAccessible(true);
            point.set(activity, "320+445");
            navigate("detail");
            String visible = labels((View) field(activity, "screen"));
            check(
                visible.contains("Chuông 2 hỏng")
                    && visible.contains("Đang có sự cố")
                    && !visible.contains("Đếm trục lỗi")
                    && !visible.contains("CB3 thiếu")
                    && !visible.contains("CB3 thừa"),
                "320+445 keeps its raw hardware fault but no axle statistics or comparison note");
          });
      UiPreview.shot(runner, activity, "axle-exempt-detail-0.13.png");
      for (String exemptPoint : exemptPoints) {
        if (exemptPoint.equals("320+445")) continue;
        store.saveForAccount(
            store.account(),
            new WebSource.DayData(
                List.of(
                    new Rules.Event(
                        "ui-hardware-" + exemptPoint,
                        axleStart + 40000,
                        exemptPoint,
                        "Chuông 2 hỏng",
                        true,
                        List.of())),
                axleRoute));
        store.updateActiveFaults(store.account(), axleStart, axleNow, axleStart + 30000, List.of());
        ui(
            () -> {
              Field point = MainActivity.class.getDeclaredField("selectedCrossing");
              point.setAccessible(true);
              point.set(activity, exemptPoint);
              navigate("detail");
              String visible = labels((View) field(activity, "screen"));
              check(
                  visible.contains("Km " + exemptPoint)
                      && visible.contains("Chuông 2 hỏng")
                      && visible.contains("Đang có sự cố")
                      && !visible.contains("Đếm trục lỗi")
                      && !visible.contains("CB3 thiếu")
                      && !visible.contains("CB3 thừa"),
                  "new exemption keeps real hardware but hides derived axle details: "
                      + exemptPoint);
            });
      }
      UiPreview.shot(runner, activity, "axle-exempt-detail-0.15.png");
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            navigate("incidents");
            String visible = labels((View) field(activity, "screen"));
            check(
                visible.contains("Cảnh báo đếm trục trong ngày")
                    && visible.contains("CB3 đếm sai 6 lần"),
                "incident tab distinguishes derived axle warning from hardware restoration");
          });
      UiPreview.shot(runner, activity, "axle-incidents-0.13.png");
      store.credentials("https://rail.example.test/", "Chosy", "test-only");
      var stationRoute = new Rules.Route(List.of("300+600", "305+888", "307+507"));
      long stationNow = System.currentTimeMillis();
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-station-arrival",
                      stationNow - 1000,
                      "305+888",
                      "Tàu lẻ",
                      false,
                      List.of())),
              stationRoute));
      store.updateActiveFaults(
          store.account(), stationNow - 2000, stationNow, stationNow - 2000, List.of());
      store
          .prefs
          .edit()
          .putString("rangeFrom", "305+888")
          .putString("rangeTo", "307+507")
          .putLong("lastSuccess", stationNow)
          .commit();
      ui(
          () -> {
            navigate("overview");
            View canvas = (View) field(activity, "routeView");
            check(
                ((List<?>) field(canvas, "points")).equals(store.route().points())
                    && ((List<?>) field(canvas, "points")).contains("303+350 (Nghi Long)"),
                "overview retains full route and Nghi Long while monitoring a smaller scope");
            check(
                ((List<?>) field(canvas, "segments"))
                    .equals(List.of(new Rules.TrackSegment("303+350 (Nghi Long)", "305+888", 1))),
                "diagram shows only station-to-crossing interval for departing train");
          });
      UiPreview.shot(runner, activity, "nghi-long-overview-0.17.png");
      ui(
          () -> {
            navigate("crossings");
            String visible = labels((View) field(activity, "screen"));
            check(
                visible.contains("305+888")
                    && visible.contains("307+507")
                    && !visible.contains("Nghi Long"),
                "station is not listed as a monitored road crossing");
          });
      store.credentials("https://rail.example.test/", "diagram-ui", "test-only");
      var sparseRoute =
          new Rules.Route(List.of("199+000 (A)", "200+000", "201+000", "202+000 (B)"));
      long sparseNow = System.currentTimeMillis();
      var network =
          new Rules.Event(
              "ui-sparse-network",
              sparseNow - 15000,
              "201+000",
              "Mất kết nối giám sát",
              true,
              List.of());
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-sparse-start", sparseNow - 60000, "200+000", "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "ui-sparse-pass",
                      sparseNow - 50000,
                      "200+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-sparse-reset",
                      sparseNow - 40000,
                      "200+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-sparse-motion",
                      sparseNow - 30000,
                      "201+000",
                      "Cần chắn 1 đã hạ",
                      false,
                      List.of()),
                  network),
              sparseRoute));
      store.updateActiveFaults(store.account(), sparseNow - 60000, sparseNow, 0, List.of());
      store
          .prefs
          .edit()
          .putLong("lastSuccess", sparseNow)
          .putString("status", "Đã cập nhật")
          .commit();
      ui(
          () -> {
            navigate("overview");
            check(
                ((List<?>) field(field(activity, "routeView"), "segments"))
                    .equals(List.of(new Rules.TrackSegment("200+000", "201+000", 1))),
                "sparse destination movement retains one incoming interval until restoration");
          });
      Object sparseRoot = field(activity, "screen"), sparseCanvas = field(activity, "routeView");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-sparse-end",
                      sparseNow - 10000,
                      "201+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of())),
              sparseRoute));
      store.updateActiveFaults(store.account(), sparseNow - 10000, sparseNow, 0, List.of());
      store.prefs.edit().putLong("lastSuccess", sparseNow + 1).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                field(activity, "screen") == sparseRoot
                    && field(activity, "routeView") == sparseCanvas
                    && ((List<?>) field(sparseCanvas, "segments")).isEmpty(),
                "sparse restoration clears diagram within existing root and canvas");
            check(
                labels((View) sparseRoot).contains("Mất kết nối giám sát"),
                "clearing old train interval keeps actual network fault visible");
          });
      UiPreview.shot(runner, activity, "diagram-sparse-cleared-0.19.png");
      long nextStart = sparseNow - 9000;
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "ui-lost-start", nextStart, "200+000", "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "ui-lost-pass",
                      nextStart + 1000,
                      "200+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-lost-reset",
                      nextStart + 2000,
                      "200+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-next-start", nextStart + 3000, "200+000", "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "ui-next-destination",
                      nextStart + 4000,
                      "201+000",
                      "Tàu lẻ",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-next-pass",
                      nextStart + 5000,
                      "200+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-next-destination-pass",
                      nextStart + 6000,
                      "201+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-next-reset",
                      nextStart + 7000,
                      "200+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of()),
                  new Rules.Event(
                      "ui-next-destination-reset",
                      nextStart + 8000,
                      "201+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of())),
              sparseRoute));
      store.updateActiveFaults(
          store.account(), nextStart, System.currentTimeMillis(), 0, List.of());
      store.prefs.edit().putLong("lastSuccess", sparseNow + 2).commit();
      ui(
          () -> {
            ((Runnable) field(activity, "refresh")).run();
            check(
                field(activity, "screen") == sparseRoot
                    && ((List<?>) field(sparseCanvas, "segments")).isEmpty()
                    && labels((View) sparseRoot).contains("Mất kết nối giám sát"),
                "next confirmed train heals wholly missing visit without clearing hardware or"
                    + " replacing window");
          });
      store.credentials("https://rail.example.test/", "history-ui", "test-only");
      long historyNow = System.currentTimeMillis();
      var historyRoute = new Rules.Route(List.of("100+000", "101+000"));
      var historyEvents = List.of(
          new Rules.Event("history-first", historyNow - 60000, "Km 100+000", "Tàu lẻ", false, List.of()),
          new Rules.Event("history-bell", historyNow - 59000, "Km 100+000", "Chuông 2 hỏng", true, List.of()),
          new Rules.Event("history-second", historyNow - 30000, "Km 100+000", "Tàu chẵn", false, List.of()),
          new Rules.Event("history-restore", historyNow - 10000, "Km 100+000", "Khôi phục hệ thống", false, List.of()),
          new Rules.Event("history-other", historyNow - 20000, "Km 101+000", "Đèn 2 hỏng", true, List.of()));
      store.saveForAccount(store.account(), new WebSource.DayData(historyEvents, historyRoute));
      store.updateActiveFaults(store.account(), historyNow - 60000, historyNow, 0, List.of());
      ui(() -> {
        Field chosen = MainActivity.class.getDeclaredField("selectedCrossing");
        chosen.setAccessible(true);
        chosen.set(activity, "100+000");
        navigate("detail");
        String visible = labels((View) field(activity, "content"));
        check(visible.contains("Chi tiết") && !visible.contains("Xem trên sơ đồ tuyến"),
            "crossing detail replaces map action with history details");
        View detailsAction = findLabel((View) field(activity, "content"), "Chi tiết");
        check(detailsAction != null && ((View) detailsAction.getParent()).performClick(), "history details action is usable");
        check(field(activity, "page").equals("crossingHistory"), "history details opens a dedicated page");
        String historyVisible = labels((View) field(activity, "content"));
        check(historyVisible.contains("15 ngày") && historyVisible.contains("Tàu lẻ")
                && historyVisible.contains("Tàu chẵn") && !historyVisible.contains("Đèn 2 hỏng"),
            "day history shows selected crossing only and retention period");
        LinearLayout groups = (LinearLayout) field(activity, "content");
        check(labels(groups.getChildAt(1)).contains("Tàu chẵn")
                && labels(groups.getChildAt(1)).contains("Đã ghi nhận kết thúc lượt tàu")
                && !labels(groups.getChildAt(1)).contains("Chuông 2 hỏng"),
            "newest train has a separate completed frame without previous train fault");
        check(labels(groups.getChildAt(2)).contains("Chuông 2 hỏng")
                && labels(groups.getChildAt(2)).contains("Chưa ghi nhận kết thúc")
                && groups.getChildAt(2).getBackground() != null,
            "old incomplete train has its own bordered frame with original fault");
        View oldHistoryRoot = (View) field(activity, "screen");
        invoke("refreshContent");
        check(field(activity, "screen") == oldHistoryRoot,
            "history updates preserve window and header");
        navigate("settings");
        String settingsVisible = labels((View) field(activity, "content"));
        check(!settingsVisible.contains("Thử chuông báo lỗi")
                && settingsVisible.contains("Chuông sự cố"),
            "settings removes trial playback and retains actual incident sound toggle");
      });
      ui(() -> navigate("crossingHistory"));
      UiPreview.shot(runner, activity, "crossing-history-0.21.png");
      return checks;
    } finally {
      ui(() -> activity.finish());
      store.prefs.edit().clear().commit();
      context.deleteDatabase("events.db");
    }
  }
}
