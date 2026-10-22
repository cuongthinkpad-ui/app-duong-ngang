package vn.duongngang.monitor;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;

/** Native Android presentation. Demo events never enter the operational store. */
public class MainActivity extends Activity {
  private static final int BLUE = 0xFF43BFF3,
      INK = 0xFFEAF0FA,
      MUTED = 0xFF99A5BB,
      BG = 0xFF0C1220,
      LINE = 0xFF29344C,
      GREEN = 0xFF67D5AD,
      RED = 0xFFF2A3AC,
      SURFACE = 0xFF151E2F,
      TINT = 0xFF1D2942,
      FAULT = 0xFF311C22,
      FAULT_TINT = 0xFF48232E,
      GOOD = 0xFF162E2A,
      BUTTON_TEXT = 0xFF071724,
      WARNING = 0xFFF0C66F;
  private Store store;
  private final AlarmSound testAlarm = new AlarmSound();
  private LinearLayout screen, content;
  private TextView status, routeNote, summaryTitle, summaryDetail, summaryTime;
  private LinearLayout summaryCard;
  private SummaryRing summaryRing;
  private LinearLayout routeWrapper;
  private RouteView routeView;
  private ScrollView pageScroll;
  private List<String> renderedRoute = List.of();
  private String page = "overview",
      filter = "Tất cả",
      selectedCrossing = "312+111",
      incidentFilter = "Tất cả";
  private boolean demo = false, expanded = false, followToday = true;
  private LocalDate selected = LocalDate.now(WebSource.ZONE);
  private final long demoClock = System.currentTimeMillis();
  private final ExecutorService io = Executors.newSingleThreadExecutor();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private String lastUiState = "";
  private final Runnable refresh =
      new Runnable() {
        public void run() {
          if (followToday) selected = LocalDate.now(WebSource.ZONE);
          String state =
              store.prefs.getString("status", "")
                  + store.prefs.getLong("lastSuccess", 0)
                  + staleData()
                  + LocalDate.now(WebSource.ZONE);
          if (!state.equals(lastUiState)) {
            lastUiState = state;
            if (!page.equals("settings")) refreshContent();
          }
          handler.postDelayed(this, 2000);
        }
      };

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    store = new Store(this);
    // Older builds could reopen in preview mode. The app now always opens live monitoring.
    store.prefs.edit().remove("demoUi").apply();
    demo = false;
    getWindow().setStatusBarColor(BG);
    getWindow().setNavigationBarColor(BG);
    if (Build.VERSION.SDK_INT >= 26) getWindow().getDecorView().setSystemUiVisibility(0);
    if (Build.VERSION.SDK_INT >= 33)
      requestPermissions(new String[] {"android.permission.POST_NOTIFICATIONS"}, 10);
    if (!demo
        && store.prefs.getBoolean("running", false)
        && !store.prefs.getString("url", "").isEmpty())
      startForegroundService(new Intent(this, MonitorService.class));
    render();
    Store capturedStore = store;
    String capturedAccount = capturedStore.account();
    if (!capturedStore.travelReady())
      io.execute(
          () -> {
            capturedStore.prepareTravelState(capturedAccount, System.currentTimeMillis());
            runOnUiThread(
                () -> {
                  if (!isFinishing()
                      && store == capturedStore
                      && store.account().equals(capturedAccount)) refreshContent();
                });
          });
  }

  @Override
  protected void onStart() {
    super.onStart();
    handler.post(refresh);
  }

  @Override
  protected void onStop() {
    handler.removeCallbacks(refresh);
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    testAlarm.stop();
    io.shutdownNow();
    super.onDestroy();
  }

  private int dp(float value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }

  private LinearLayout column() {
    LinearLayout v = new LinearLayout(this);
    v.setOrientation(LinearLayout.VERTICAL);
    return v;
  }

  private LinearLayout row() {
    LinearLayout v = new LinearLayout(this);
    v.setGravity(Gravity.CENTER_VERTICAL);
    return v;
  }

  private TextView text(String value, int size, int color, boolean bold) {
    TextView v = new TextView(this);
    v.setText(value);
    v.setTextSize(size);
    v.setTextColor(color);
    v.setTypeface(Typeface.create(bold ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
    v.setIncludeFontPadding(false);
    v.setLineSpacing(dp(2), 1);
    return v;
  }

  private GradientDrawable shape(int color, int radius, int stroke) {
    GradientDrawable d = new GradientDrawable();
    d.setColor(color);
    d.setCornerRadius(dp(radius));
    if (stroke != 0) d.setStroke(dp(1), stroke);
    return d;
  }

  private void touch(View v, Runnable action) {
    Drawable base = v.getBackground();
    v.setBackground(
        new RippleDrawable(ColorStateList.valueOf(0x17246BCE), base, shape(Color.WHITE, 18, 0)));
    v.setOnClickListener(w -> action.run());
    v.setClickable(true);
    v.setFocusable(true);
  }

  private void gap(LinearLayout parent, int height) {
    parent.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height)));
  }

  private void divider(LinearLayout parent) {
    View v = new View(this);
    v.setBackgroundColor(LINE);
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(1));
    p.topMargin = dp(7);
    p.bottomMargin = dp(7);
    parent.addView(v, p);
  }

  private TextView pill(String value, int color, int background) {
    TextView t = text(value, 10, color, true);
    t.setPadding(dp(9), dp(5), dp(9), dp(5));
    t.setBackground(shape(background, 8, 0));
    return t;
  }

  private LinearLayout card(int color) {
    LinearLayout c = column();
    c.setPadding(dp(12), dp(11), dp(12), dp(11));
    c.setBackground(shape(color, 16, color == SURFACE ? LINE : color == FAULT ? FAULT_TINT : 0));
    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
    p.bottomMargin = dp(8);
    c.setLayoutParams(p);
    return c;
  }

  private View icon(String kind, int color, int size) {
    Icon v = new Icon(kind, color);
    v.setLayoutParams(new LinearLayout.LayoutParams(dp(size), dp(size)));
    return v;
  }

  private View iconButton(String kind, String description, Runnable action) {
    LinearLayout b = row();
    b.setGravity(Gravity.CENTER);
    b.setBackground(shape(SURFACE, 14, LINE));
    b.setLayoutParams(new LinearLayout.LayoutParams(dp(44), dp(44)));
    b.addView(icon(kind, BLUE, 21));
    b.setContentDescription(description);
    touch(b, action);
    return b;
  }

  private View action(String title, String kind, boolean primary, Runnable run) {
    LinearLayout b = row();
    b.setGravity(Gravity.CENTER);
    b.setPadding(dp(14), dp(12), dp(14), dp(12));
    b.setMinimumHeight(dp(46));
    b.setBackground(shape(primary ? BLUE : SURFACE, 13, primary ? 0 : LINE));
    if (kind != null) {
      b.addView(icon(kind, primary ? BUTTON_TEXT : BLUE, 17));
      View spacer = new View(this);
      b.addView(spacer, new LinearLayout.LayoutParams(dp(8), 1));
    }
    b.addView(text(title, 13, primary ? BUTTON_TEXT : BLUE, true));
    touch(b, run);
    return b;
  }

  private String time(long at) {
    return DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(WebSource.ZONE)
        .format(Instant.ofEpochMilli(at));
  }

  private List<Rules.Event> samples() {
    List<Rules.Event> values =
        new ArrayList<>(
            List.of(
                new Rules.Event("d6", demoClock - 365000, "Km 320+445", "Tàu lẻ", false, List.of()),
                new Rules.Event(
                    "d7", demoClock - 285000, "Km 313+800", "Tàu chẵn", false, List.of()),
                new Rules.Event(
                    "d8",
                    demoClock - 420000,
                    "Km 334+830",
                    "Mất nguồn xoay chiều 220VAC",
                    true,
                    List.of()),
                new Rules.Event(
                    "d9",
                    demoClock - 390000,
                    "Km 334+830",
                    "Nguồn xoay chiều 220VAC khôi phục tốt",
                    false,
                    List.of()),
                new Rules.Event(
                    "d10", demoClock - 215000, "Km 321+780", "Chuông 2 hỏng", true, List.of()),
                new Rules.Event("d1", demoClock - 45000, "Km 321+780", "Tàu lẻ", false, List.of()),
                new Rules.Event(
                    "d2",
                    demoClock - 125000,
                    "Km 328+325",
                    "Điện áp cảm biến 6 < 5V",
                    true,
                    List.of()),
                new Rules.Event(
                    "d3",
                    demoClock - 185000,
                    "Km 313+456",
                    "Cần chắn 1 mất trạng thái",
                    true,
                    List.of()),
                new Rules.Event("d4", demoClock - 245000, "Km 320+957", "Tàu lẻ", false, List.of()),
                new Rules.Event(
                    "d5",
                    demoClock - 310000,
                    "Km 313+800",
                    "Khôi phục hệ thống",
                    false,
                    List.of("46", "46", "48", "44", "46", "46"))));
    values.sort(Comparator.comparingLong(Rules.Event::at).reversed());
    return values;
  }

  private Rules.Route route() {
    return demo ? Rules.DEMO_ROUTE : store.route();
  }

  private String rangeFrom() {
    return store.rangeFrom(route(), demo);
  }

  private String rangeTo() {
    return store.rangeTo(route(), demo);
  }

  private boolean inScope(String crossing) {
    return route().inRange(crossing, rangeFrom(), rangeTo());
  }

  private List<Rules.Event> allToday() {
    return demo
        ? samples()
        : store.events(
            LocalDate.now(WebSource.ZONE).atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli(),
            System.currentTimeMillis() + 1);
  }

  private List<Rules.Event> occupancyEvents() {
    if (!demo && store.prefs.contains("occupiedPoints")) return store.occupationEvents();
    return demo
        ? samples()
        : store.events(
            LocalDate.now(WebSource.ZONE)
                .minusDays(1)
                .atStartOfDay(WebSource.ZONE)
                .toInstant()
                .toEpochMilli(),
            System.currentTimeMillis() + 1);
  }

  private boolean staleData() {
    if (demo) return false;
    long last = store.prefs.getLong("lastSuccess", 0);
    String message = store.prefs.getString("status", "");
    return last == 0
        || System.currentTimeMillis() - last
            > Math.max(60000L, 2000L * store.prefs.getInt("interval", 30))
        || !(message.equals("Đã cập nhật") || message.startsWith("Đã đặt lại theo dõi"));
  }

  private List<Rules.Event> today() {
    List<Rules.Event> selected = new ArrayList<>();
    for (Rules.Event e : allToday()) if (inScope(e.crossing())) selected.add(e);
    return selected;
  }

  private boolean applyScope(String from, String to) {
    if (!route().validRange(from, to)) {
      Toast.makeText(
              this,
              "Đường ngang bắt đầu phải ở phía Bắc hoặc trùng điểm kết thúc",
              Toast.LENGTH_LONG)
          .show();
      return false;
    }
    store
        .prefs
        .edit()
        .putString(Store.rangeKey(demo, true), from)
        .putString(Store.rangeKey(demo, false), to)
        .apply();
    if (!demo)
      for (Rules.Event e : activeFaults())
        if (!inScope(e.crossing()))
          getSystemService(NotificationManager.class)
              .cancel(100 + Math.floorMod(e.id().hashCode(), 1000000));
    return true;
  }

  private List<Rules.Event> activeFaults() {
    if (demo) return Rules.activeFaults(samples());
    long baseline =
        Rules.faultBaseline(store.prefs.getLong("baselineFrom", 0), System.currentTimeMillis());
    List<Rules.Event> current = new ArrayList<>();
    List<Rules.Event> records =
        store.prefs.contains("activeFaults")
            ? store.activeFaults()
            : store.events(baseline, System.currentTimeMillis() + 1);
    boolean selfEnabled = Rules.selfTriggerEnabled(store.prefs.getInt("interval", 30));
    for (var event : records)
      if (event.at() >= baseline && (selfEnabled || !Rules.isSelfTrigger(event)))
        current.add(event);
    return Rules.activeFaults(current);
  }

  private List<Rules.Event> scopedFaults() {
    List<Rules.Event> result = new ArrayList<>();
    for (var event : activeFaults()) if (inScope(event.crossing())) result.add(event);
    return result;
  }

  private List<Rules.Event> faultsAt(String point) {
    List<Rules.Event> result = new ArrayList<>();
    for (var event : activeFaults())
      if (Rules.km(event.crossing()).equals(Rules.km(point))) result.add(event);
    return result;
  }

  private void navigate(String next) {
    page = next;
    render();
  }

  private void render() {
    summaryCard = null;
    routeWrapper = null;
    routeView = null;
    pageScroll = null;
    screen = column();
    screen.setBackgroundColor(BG);
    setContentView(screen);
    LinearLayout header = column();
    header.setPadding(dp(16), dp(8), dp(16), dp(10));
    header.setBackgroundColor(BG);
    LinearLayout titleRow = row();
    ImageView mark = new ImageView(this);
    mark.setImageResource(R.drawable.ic_train_header);
    mark.setPadding(dp(6), dp(6), dp(6), dp(6));
    mark.setBackground(shape(TINT, 13, 0));
    mark.setScaleType(ImageView.ScaleType.FIT_CENTER);
    mark.setContentDescription("Biểu tượng Giám sát Đường ngang");
    titleRow.addView(mark, new LinearLayout.LayoutParams(dp(38), dp(42)));
    LinearLayout titles = column();
    titles.setPadding(dp(10), 0, dp(4), 0);
    String heading =
        page.equals("overview")
            ? "Giám sát Đường ngang"
            : page.equals("incidents")
                ? "Sự cố đường ngang"
                : page.equals("settings")
                    ? "Cài đặt"
                    : page.equals("detail")
                        ? "Chi tiết đường ngang"
                        : page.equals("trains") ? "Các lượt tàu" : "Danh sách đường ngang";
    titles.addView(text(heading, page.equals("overview") ? 18 : 17, INK, true));
    gap(titles, 5);
    titles.addView(text("Công ty CP TTTH Đường sắt Vinh", 11, MUTED, false));
    titleRow.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
    titleRow.addView(
        iconButton(
            page.equals("detail") || page.equals("trains")
                ? "back"
                : page.equals("settings") ? "close" : "settings",
            page.equals("settings") ? "Về tổng quan" : "Cài đặt",
            () ->
                navigate(
                    page.equals("trains")
                        ? "overview"
                        : page.equals("detail")
                            ? "crossings"
                            : page.equals("settings") ? "overview" : "settings")));
    header.addView(titleRow);
    screen.addView(header);
    content = column();
    content.setPadding(dp(14), page.equals("overview") ? 0 : dp(8), dp(14), 0);
    if (page.equals("overview")) {
      LinearLayout summaryWrapper = column();
      summaryWrapper.setPadding(dp(14), dp(6), dp(14), 0);
      summaryWrapper.addView(overviewSummary());
      screen.addView(summaryWrapper);
      LinearLayout map = routeCard();
      routeWrapper = column();
      routeWrapper.setPadding(dp(14), dp(8), dp(14), 0);
      routeWrapper.addView(map);
      screen.addView(routeWrapper);
    }
    if (page.equals("overview")) {
      content = new DashboardView();
      screen.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
    } else {
      pageScroll = new ScrollView(this);
      pageScroll.setClipToPadding(false);
      pageScroll.setFillViewport(true);
      pageScroll.setVerticalScrollBarEnabled(false);
      pageScroll.addView(content);
      screen.addView(pageScroll, new LinearLayout.LayoutParams(-1, 0, 1));
      if (page.equals("incidents")) incidents();
      else if (page.equals("crossings")) crossingList();
      else if (page.equals("detail")) crossingDetail();
      else if (page.equals("trains")) trainList();
      else settings();
    }
    status = text("", 11, MUTED, false);
    status.setMaxLines(2);
    status.setEllipsize(android.text.TextUtils.TruncateAt.END);
    if (!page.equals("settings")) {
      if (page.equals("overview")) screen.addView(status);
      else content.addView(status);
      updateStatusLine();
    }
    if (page.equals("settings")) {
      TextView credit = text("Designed by Cao Xuân Cường", 8, MUTED, true);
      credit.setGravity(Gravity.END);
      credit.setPadding(dp(16), dp(3), dp(16), dp(5));
      screen.addView(credit);
    }
    navigation();
  }

  private void updateStatusLine() {
    String message = store.prefs.getString("status", "");
    status.setText(message);
    status.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
    status.setPadding(dp(2), 0, 0, dp(12));
  }

  /** A poll changes data inside the existing window, never replaces its root/header/navigation. */
  private void refreshContent() {
    if (page.equals("settings")) return;
    if (page.equals("overview")) {
      updateSummary();
      if (!renderedRoute.equals(route().points())) {
        routeWrapper.removeAllViews();
        routeWrapper.addView(routeCard());
      } else updateRoutePresentation();
      updateStatusLine();
      if (content.getHeight() > 0) fillDashboard(content, content.getHeight());
    } else {
      ScrollView currentScroll = pageScroll;
      int x = currentScroll.getScrollX(), y = currentScroll.getScrollY();
      content.removeAllViews();
      if (page.equals("incidents")) incidents();
      else if (page.equals("crossings")) crossingList();
      else if (page.equals("detail")) crossingDetail();
      else if (page.equals("trains")) trainList();
      content.addView(status);
      updateStatusLine();
      // Restore after measuring the new rows, before the next frame is drawn.
      currentScroll
          .getViewTreeObserver()
          .addOnPreDrawListener(
              new android.view.ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                  currentScroll.getViewTreeObserver().removeOnPreDrawListener(this);
                  currentScroll.scrollTo(x, y);
                  return true;
                }
              });
    }
  }

  private LinearLayout overviewSummary() {
    summaryCard = card(GOOD);
    summaryCard.setOrientation(LinearLayout.HORIZONTAL);
    summaryCard.setGravity(Gravity.CENTER_VERTICAL);
    summaryRing = new SummaryRing();
    summaryCard.addView(summaryRing, new LinearLayout.LayoutParams(dp(52), dp(52)));
    LinearLayout words = column();
    words.setPadding(dp(12), 0, 0, 0);
    summaryTitle = text("", 10, GREEN, true);
    summaryDetail = text("", 15, INK, true);
    summaryTime = text("", 9, MUTED, false);
    words.addView(summaryTitle);
    gap(words, 4);
    words.addView(summaryDetail);
    gap(words, 4);
    words.addView(summaryTime);
    summaryCard.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
    updateSummary();
    return summaryCard;
  }

  private void updateSummary() {
    if (summaryCard == null) return;
    int total = 0;
    for (String point : route().crossings()) if (inScope(point)) total++;
    int faults = scopedFaults().size();
    boolean known = store.prefs.getLong("lastSuccess", 0) > 0;
    int color = !known || staleData() ? WARNING : faults > 0 ? RED : GREEN;
    summaryCard.setBackground(
        shape(faults > 0 ? FAULT : !known || staleData() ? TINT : GOOD, 18, 0));
    summaryTitle.setText(
        !known
            ? "CHỜ KẾT NỐI"
            : staleData() ? "CẦN KIỂM TRA CẬP NHẬT" : faults > 0 ? "CÓ SỰ CỐ" : "ĐANG THEO DÕI");
    summaryTitle.setTextColor(color);
    summaryDetail.setText(
        !known
            ? "Kết nối tài khoản để theo dõi"
            : faults > 0
                ? faults + " sự cố chưa khôi phục"
                : total + " đường ngang · Chưa ghi nhận lỗi mới");
    long last = store.prefs.getLong("lastSuccess", 0);
    int interval = store.prefs.getInt("interval", 30);
    summaryTime.setText(
        (last == 0 ? "Chưa có dữ liệu từ website" : "Cập nhật " + time(last))
            + " · "
            + (interval < 60 ? interval + " giây" : interval / 60 + " phút"));
    summaryRing.number = faults > 0 ? faults : total;
    summaryRing.color = color;
    summaryRing.invalidate();
  }

  private class SummaryRing extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    int number, color;

    SummaryRing() {
      super(MainActivity.this);
    }

    protected void onDraw(Canvas canvas) {
      float cx = getWidth() / 2f, cy = getHeight() / 2f, radius = Math.min(cx, cy) - dp(3);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(dp(2.5f));
      paint.setColor(color);
      canvas.drawCircle(cx, cy, radius, paint);
      paint.setStyle(Paint.Style.FILL);
      paint.setTextSize(dp(18));
      paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
      String text = "" + number;
      canvas.drawText(
          text,
          cx - paint.measureText(text) / 2,
          cy - (paint.ascent() + paint.descent()) / 2,
          paint);
    }
  }

  private Rules.TravelState diagramState() {
    if (!demo) return store.travelState();
    return Rules.travelState(route(), Rules.TravelState.empty(), samples());
  }

  private void updateRoutePresentation() {
    List<String> points = route().points();
    routeView.setContentDescription(
        "Toàn tuyến, gồm "
            + route().crossings().size()
            + " đường ngang và "
            + (points.size() - route().crossings().size())
            + " ga, "
            + Rules.approachingCrossings(diagramState()).size()
            + " điểm tiếp cận, "
            + Rules.trackSegments(route(), diagramState()).size()
            + " đoạn tàu"
            + (demo
                ? " · Dữ liệu minh họa"
                : staleData() ? " · Dữ liệu cũ hoặc chưa cập nhật" : " · Theo bản tin web"));
    routeView.updateOccupancy();
    long last = store.prefs.getLong("lastSuccess", 0);
    routeNote.setText(
        demo
            ? "Minh họa"
            : !store.travelReady()
                ? "Đang cập nhật sơ đồ"
                : last == 0
                    ? "Chưa cập nhật từ web"
                    : (staleData() ? "Dữ liệu cũ · " : "Theo bản tin web · ") + time(last));
    routeNote.setTextColor(!demo && staleData() ? WARNING : MUTED);
  }

  private LinearLayout routeCard() {
    LinearLayout c = card(SURFACE);
    LinearLayout compass = row();
    compass.setPadding(0, dp(2), 0, 0);
    compass.addView(text("BẮC", 9, MUTED, true));
    compass.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
    compass.addView(text("NAM", 9, MUTED, true));
    c.addView(compass);
    routeView = new RouteView();
    List<String> points = route().points();
    renderedRoute = List.copyOf(points);
    {
      int routeWidth = dp(60 + (points.size() > 13 ? 26 : 22) * Math.max(0, points.size() - 1));
      routeView.setMinimumWidth(routeWidth);
      HorizontalScrollView scroll = new HorizontalScrollView(this);
      scroll.setFillViewport(true);
      scroll.addView(routeView, new HorizontalScrollView.LayoutParams(routeWidth, dp(101)));
      c.addView(scroll, new LinearLayout.LayoutParams(-1, dp(105)));
    }
    routeNote = text("", 8, MUTED, false);
    routeNote.setGravity(Gravity.END);
    c.addView(routeNote);
    updateRoutePresentation();
    return c;
  }

  private void setAlarmSound(boolean enabled) {
    store.prefs.edit().putBoolean("sound", enabled).apply();
    if (!enabled) {
      NotificationManager manager = getSystemService(NotificationManager.class);
      testAlarm.stop();
      if (store.prefs.getBoolean("running", false))
        startService(new Intent(this, MonitorService.class).setAction("MUTE_ALARM"));
      for (var notification : manager.getActiveNotifications())
        if (notification.getId() != 1) manager.cancel(notification.getId());
    }
  }

  /** The main dashboard owns the available viewport; it never adds a vertical scroller. */
  private class DashboardView extends LinearLayout {
    private int lastWidth = -1, lastHeight = -1;

    DashboardView() {
      super(MainActivity.this);
      setOrientation(VERTICAL);
      setPadding(dp(14), 0, dp(14), 0);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
      int width = MeasureSpec.getSize(widthSpec), height = MeasureSpec.getSize(heightSpec);
      // Build before drawing the first frame, instead of posting an empty dashboard frame.
      if (height > 0 && (width != lastWidth || height != lastHeight || getChildCount() == 0)) {
        lastWidth = width;
        lastHeight = height;
        fillDashboard(this, height);
      }
      super.onMeasure(widthSpec, heightSpec);
    }
  }

  private void fillDashboard(LinearLayout panel, int height) {
    boolean reuseCards = panel.getChildCount() == 2;
    LinearLayout previousTrain = reuseCards ? (LinearLayout) panel.getChildAt(0) : null;
    LinearLayout previousFault = reuseCards ? (LinearLayout) panel.getChildAt(1) : null;
    if (!reuseCards) panel.removeAllViews();
    List<Rules.Event> trains = new ArrayList<>();
    for (Rules.Event e : today()) if (Rules.direction(e.text()) != 0) trains.add(e);
    List<Rules.Event> faults = scopedFaults();
    float density = getResources().getDisplayMetrics().density,
        scale = getResources().getConfiguration().fontScale;
    float available = height / density;
    int tn = Math.min(trains.size(), expanded ? 10 : 6), fn = Math.min(faults.size(), 6);
    float trainRow = 36 * Math.max(1, scale), faultRow = 56 * Math.max(1, scale);
    while (128 + Math.max(1, tn) * trainRow + Math.max(1, fn) * faultRow > available
        && (tn > 1 || fn > 1)) {
      if (expanded && fn > 2) fn--;
      else if (tn > 1 && (fn <= 1 || tn >= fn)) tn--;
      else if (fn > 1) fn--;
      else tn--;
    }
    float trainNeed = 48 + Math.max(1, tn) * trainRow,
        faultNeed = fn == 0 ? 78 : 56 + fn * faultRow;
    float trainActual = (available - 8) * trainNeed / (trainNeed + faultNeed),
        faultActual = available - 8 - trainActual;
    boolean roomyTrain = (trainActual - 48) / Math.max(1, tn) > 46,
        roomyFault = (faultActual - 56) / Math.max(1, fn) > 68;
    LinearLayout train = reuseCards ? previousTrain : card(SURFACE);
    train.removeAllViews();
    LinearLayout.LayoutParams trainParams = new LinearLayout.LayoutParams(-1, 0, trainNeed);
    trainParams.bottomMargin = dp(8);
    if (reuseCards) train.setLayoutParams(trainParams);
    else panel.addView(train, trainParams);
    LinearLayout th = row();
    th.addView(icon("direction", BLUE, 19));
    TextView tt = text("Tàu gần nhất", 14, INK, true);
    tt.setPadding(dp(8), 0, 0, 0);
    th.addView(tt, new LinearLayout.LayoutParams(0, -2, 1));
    if (trains.size() > tn || expanded) {
      LinearLayout more = row();
      more.addView(icon(expanded ? "up" : "down", MUTED, 17));
      more.setPadding(dp(8), dp(4), dp(2), dp(4));
      more.setContentDescription(expanded ? "Thu gọn lượt tàu" : "Hiển thị thêm lượt tàu");
      touch(
          more,
          () -> {
            expanded = !expanded;
            fillDashboard(panel, panel.getHeight());
          });
      th.addView(more);
    } else th.addView(pill("" + trains.size(), BLUE, TINT));
    train.addView(th);
    gap(train, 6);
    if (tn == 0) {
      LinearLayout placeholder = column();
      placeholder.setGravity(Gravity.CENTER_VERTICAL);
      empty(
          placeholder, "Chưa ghi nhận tàu", "Thông tin xuất hiện khi có dữ liệu mới.", "direction");
      train.addView(placeholder, new LinearLayout.LayoutParams(-1, 0, 1));
    }
    for (int i = 0; i < tn; i++) {
      if (i > 0) dashboardDivider(train);
      LinearLayout entry = compactTrainRow(trains.get(i), i == 0);
      train.addView(entry, new LinearLayout.LayoutParams(-1, 0, 1));
    }
    if (trains.size() > tn) {
      TextView more = text("Xem tất cả lượt tàu  →", 10, BLUE, true);
      more.setPadding(0, dp(5), 0, dp(3));
      touch(more, () -> navigate("trains"));
      train.addView(more);
    }
    LinearLayout fault = reuseCards ? previousFault : card(FAULT);
    fault.removeAllViews();
    LinearLayout.LayoutParams faultParams =
        fn == 0
            ? new LinearLayout.LayoutParams(-1, dp(78))
            : new LinearLayout.LayoutParams(-1, 0, faultNeed);
    if (reuseCards) fault.setLayoutParams(faultParams);
    else panel.addView(fault, faultParams);
    LinearLayout fh = row();
    fh.addView(icon("alert", RED, 19));
    TextView ft = text("Sự cố", 14, RED, true);
    ft.setPadding(dp(8), 0, dp(8), 0);
    fh.addView(ft);
    fh.addView(pill(String.format(Locale.ROOT, "%02d", faults.size()), RED, FAULT_TINT));
    fh.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1));
    boolean sound = store.prefs.getBoolean("sound", true);
    LinearLayout mute = row();
    mute.setPadding(dp(8), dp(6), dp(8), dp(6));
    mute.setGravity(Gravity.CENTER);
    mute.setBackground(shape(FAULT_TINT, 10, 0));
    mute.addView(icon(sound ? "bell" : "mute", RED, 17));
    TextView muteText = text(sound ? "Tắt chuông" : "Bật chuông", 10, RED, true);
    muteText.setPadding(dp(5), 0, 0, 0);
    mute.addView(muteText);
    mute.setContentDescription(sound ? "Tắt chuông tất cả sự cố" : "Bật chuông sự cố");
    touch(
        mute,
        () -> {
          setAlarmSound(!sound);
          fillDashboard(panel, panel.getHeight());
        });
    fh.addView(mute);
    fault.addView(fh);
    gap(fault, 5);
    if (fn == 0) {
      TextView empty =
          text(
              store.prefs.getLong("lastSuccess", 0) > 0 ? "Không có sự cố" : "Đang chờ dữ liệu",
              11,
              MUTED,
              false);
      fault.addView(empty);
    }
    for (int i = 0; i < fn; i++) {
      if (i > 0) dashboardDivider(fault);
      LinearLayout entry = dashboardEntry(faults.get(i), true, roomyFault);
      fault.addView(entry, new LinearLayout.LayoutParams(-1, 0, 1));
    }
    TextView moreFaults = text("Xem tất cả sự cố  →", 10, BLUE, true);
    moreFaults.setPadding(0, dp(5), 0, dp(3));
    touch(moreFaults, () -> navigate("incidents"));
    if (fn > 0) fault.addView(moreFaults);
  }

  private LinearLayout compactTrainRow(Rules.Event event, boolean latest) {
    LinearLayout line = row();
    View dot = new View(this);
    dot.setBackground(shape(latest ? GREEN : MUTED, 8, 0));
    LinearLayout.LayoutParams point = new LinearLayout.LayoutParams(dp(6), dp(6));
    point.rightMargin = dp(7);
    line.addView(dot, point);
    line.addView(
        text(time(event.at()), 10, MUTED, false), new LinearLayout.LayoutParams(dp(59), -2));
    TextView km = text("Km " + Rules.km(event.crossing()), 13, INK, true);
    line.addView(km, new LinearLayout.LayoutParams(0, -2, 1));
    line.addView(
        text(event.text() + (Rules.direction(event.text()) > 0 ? " →" : " ←"), 10, GREEN, true));
    touch(line, () -> openCrossing(event.crossing()));
    return line;
  }

  private void dashboardDivider(LinearLayout parent) {
    View line = new View(this);
    line.setBackgroundColor(LINE);
    parent.addView(line, new LinearLayout.LayoutParams(-1, dp(1)));
  }

  private LinearLayout dashboardEntry(Rules.Event e, boolean fault, boolean roomy) {
    LinearLayout box = column();
    box.setGravity(Gravity.CENTER_VERTICAL);
    LinearLayout line = new LinearLayout(this);
    line.setGravity(Gravity.TOP);
    TextView clock = text(time(e.at()), roomy ? 12 : 11, fault ? RED : MUTED, true);
    clock.setSingleLine(true);
    line.addView(clock, new LinearLayout.LayoutParams(dp(roomy ? 71 : 67), -2));
    LinearLayout words = column();
    TextView name = text(e.crossing(), roomy ? 15 : 13, INK, true);
    name.setSingleLine(true);
    words.addView(name);
    gap(words, 3);
    int direction = Rules.direction(e.text());
    TextView message =
        text(
            e.text() + (direction > 0 ? "  →" : direction < 0 ? "  ←" : ""),
            roomy ? 12 : 11,
            fault ? RED : direction != 0 ? GREEN : MUTED,
            false);
    message.setMaxLines(2);
    message.setEllipsize(android.text.TextUtils.TruncateAt.END);
    words.addView(message);
    line.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
    box.addView(line);
    if (fault) {
      gap(box, 4);
      box.addView(text("Chưa khôi phục", 10, RED, false));
      touch(
          box,
          () ->
              new AlertDialog.Builder(this)
                  .setTitle(time(e.at()) + " · " + e.crossing())
                  .setMessage(e.text() + "\n\nChưa khôi phục")
                  .setPositiveButton("Đóng", null)
                  .setNeutralButton(
                      "Chi tiết đường ngang", (dialog, which) -> openCrossing(e.crossing()))
                  .show());
    }
    return box;
  }

  private void trainList() {
    LinearLayout card = card(SURFACE);
    card.addView(text("Các lượt tàu trong phạm vi theo dõi", 13, INK, true));
    gap(card, 13);
    int count = 0;
    for (Rules.Event e : today())
      if (Rules.direction(e.text()) != 0) {
        if (count++ > 0) divider(card);
        eventRow(card, e, false);
      }
    if (count == 0)
      empty(card, "Chưa có lượt tàu", "Thông tin xuất hiện khi có dữ liệu mới.", "direction");
    content.addView(card);
  }

  private void eventRow(LinearLayout parent, Rules.Event e, boolean fault) {
    LinearLayout r = new LinearLayout(this);
    r.setGravity(Gravity.TOP);
    LinearLayout clock = column();
    TextView value = text(time(e.at()), 11, fault ? RED : MUTED, true);
    value.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
    clock.addView(value);
    r.addView(clock, new LinearLayout.LayoutParams(dp(67), -2));
    LinearLayout details = column();
    details.addView(text(e.crossing(), 13, INK, true));
    gap(details, 3);
    int direction = Rules.direction(e.text());
    String message = direction == 0 ? e.text() : e.text() + (direction > 0 ? "  →" : "  ←");
    details.addView(text(message, 11, fault ? RED : direction != 0 ? GREEN : MUTED, false));
    r.addView(details, new LinearLayout.LayoutParams(0, -2, 1));
    parent.addView(r);
  }

  private void empty(LinearLayout target, String title, String description, String kind) {
    LinearLayout r = row();
    LinearLayout disc = row();
    disc.setGravity(Gravity.CENTER);
    disc.setBackground(shape(TINT, 13, 0));
    disc.addView(
        icon(
            kind,
            kind.equals("shield") && (demo || store.prefs.getLong("lastSuccess", 0) > 0)
                ? GREEN
                : MUTED,
            22));
    r.addView(disc, new LinearLayout.LayoutParams(dp(44), dp(44)));
    LinearLayout words = column();
    words.setPadding(dp(12), 0, 0, 0);
    words.addView(text(title, 13, INK, true));
    gap(words, 5);
    if (!description.isEmpty()) words.addView(text(description, 11, MUTED, false));
    r.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
    target.addView(r);
  }

  private void incidents() {
    LinearLayout controls = card(SURFACE);
    LinearLayout date = row();
    date.addView(icon("calendar", BLUE, 17));
    TextView day = text(selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 13, INK, true);
    day.setPadding(dp(8), 0, 0, 0);
    date.addView(day, new LinearLayout.LayoutParams(0, -2, 1));
    TextView fetch = pill("Tải dữ liệu", BLUE, TINT);
    touch(fetch, this::loadHistory);
    date.addView(fetch);
    touch(
        day,
        () ->
            new DatePickerDialog(
                    this,
                    (v, y, m, d) -> {
                      selected = LocalDate.of(y, m + 1, d);
                      followToday = selected.equals(LocalDate.now(WebSource.ZONE));
                      render();
                    },
                    selected.getYear(),
                    selected.getMonthValue() - 1,
                    selected.getDayOfMonth())
                .show());
    controls.addView(date);
    gap(controls, 10);
    LinearLayout filters = row();
    for (String name : List.of("Tất cả", "Chưa khôi phục", "Đã khôi phục")) {
      TextView pick = text(name, 10, incidentFilter.equals(name) ? BLUE : MUTED, true);
      pick.setPadding(dp(5), dp(9), dp(5), dp(9));
      pick.setGravity(Gravity.CENTER);
      pick.setBackground(shape(incidentFilter.equals(name) ? TINT : Color.TRANSPARENT, 9, 0));
      filters.addView(pick, new LinearLayout.LayoutParams(0, -2, 1));
      touch(
          pick,
          () -> {
            incidentFilter = name;
            render();
          });
    }
    controls.addView(filters);
    content.addView(controls);
    long from = selected.atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli(),
        to = selected.plusDays(1).atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
    List<Rules.Event> records =
        demo && selected.equals(LocalDate.now(WebSource.ZONE))
            ? samples()
            : demo ? List.of() : store.incidentEvents(from, to);
    Set<String> openIds = new HashSet<>();
    if (selected.equals(LocalDate.now(WebSource.ZONE))) {
      records = new ArrayList<>(records);
      Set<String> present = new HashSet<>();
      for (var event : records) present.add(event.id());
      for (var event : activeFaults()) {
        openIds.add(event.id());
        if (!present.contains(event.id())) records.add(event);
      }
    }
    if (selected.equals(LocalDate.now(WebSource.ZONE))
        && !Rules.selfTriggerEnabled(store.prefs.getInt("interval", 30))) {
      records = new ArrayList<>(records);
      records.removeIf(Rules::isSelfTrigger);
    }
    int shown = 0;
    long baseline = demo ? 0 : store.prefs.getLong("baselineFrom", 0);
    for (Rules.Incident incident : Rules.incidentHistory(records)) {
      if ((!openIds.contains(incident.fault().id())
              && (incident.fault().at() < from || incident.fault().at() >= to))
          || !inScope(incident.fault().crossing())) continue;
      boolean recovered = incident.recovery() != null,
          reset = !recovered && incident.fault().at() < baseline;
      if (incidentFilter.equals("Đã khôi phục") && !recovered) continue;
      if (incidentFilter.equals("Chưa khôi phục") && (recovered || reset)) continue;
      LinearLayout item = card(recovered ? GOOD : reset ? SURFACE : FAULT);
      eventRow(item, incident.fault(), !recovered && !reset);
      gap(item, 8);
      LinearLayout state = row();
      TextView badge =
          text(
              recovered ? "✓ Đã khôi phục" : reset ? "Đã đặt lại theo dõi" : "Chưa khôi phục",
              11,
              recovered ? GREEN : reset ? MUTED : RED,
              true);
      state.addView(badge, new LinearLayout.LayoutParams(0, -2, 1));
      if (recovered) state.addView(text(time(incident.recovery().at()), 10, MUTED, false));
      item.addView(state);
      touch(item, () -> openCrossing(incident.fault().crossing()));
      content.addView(item);
      shown++;
    }
    if (shown == 0) {
      LinearLayout emptyCard = card(SURFACE);
      empty(emptyCard, "Không có sự cố", "", "shield");
      content.addView(emptyCard);
    }
  }

  private void loadHistory() {
    if (demo) {
      Toast.makeText(this, "Dữ liệu minh họa không kết nối website", Toast.LENGTH_SHORT).show();
      return;
    }
    if (store.prefs.getString("url", "").isEmpty()) {
      navigate("settings");
      return;
    }
    LocalDate day = selected;
    String account = store.account();
    status.setText("Đang tải dữ liệu sự cố…");
    status.setVisibility(View.VISIBLE);
    io.execute(
        () -> {
          try {
            WebSource source =
                new WebSource(
                    store.prefs.getString("url", ""),
                    store.prefs.getString("user", ""),
                    store.password());
            var data = source.readDay(day);
            store.saveForAccount(account, data);
            List<Rules.Event> events = data.events();
            runOnUiThread(
                () -> {
                  if (isFinishing()) return;
                  refreshContent();
                  Toast.makeText(this, "Đã tải " + events.size() + " bản ghi", Toast.LENGTH_SHORT)
                      .show();
                });
          } catch (Exception e) {
            runOnUiThread(
                () -> {
                  if (!isFinishing())
                    new AlertDialog.Builder(this)
                        .setTitle("Chưa tải được dữ liệu sự cố")
                        .setMessage(e.getMessage())
                        .setPositiveButton("Đóng", null)
                        .show();
                });
          }
        });
  }

  private EditText field(LinearLayout parent, String title, String hint, String value, int type) {
    parent.addView(text(title, 11, MUTED, true));
    gap(parent, 8);
    EditText f = new EditText(this);
    f.setSingleLine(true);
    f.setTextSize(14);
    f.setTextColor(INK);
    f.setHintTextColor(0xFF75849B);
    f.setInputType(type);
    f.setHint(hint);
    f.setText(value);
    f.setPadding(dp(14), dp(13), dp(14), dp(13));
    f.setBackground(shape(BG, 12, LINE));
    parent.addView(f, new LinearLayout.LayoutParams(-1, dp(49)));
    gap(parent, 17);
    return f;
  }

  private Switch toggle(String title, String description, boolean value, LinearLayout parent) {
    LinearLayout row = row();
    LinearLayout words = column();
    words.addView(text(title, 13, INK, true));
    if (!description.isEmpty()) {
      gap(words, 5);
      words.addView(text(description, 10, MUTED, false));
    }
    row.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
    Switch sw = new Switch(this);
    sw.setChecked(value);
    sw.setThumbTintList(
        new ColorStateList(
            new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}},
            new int[] {BLUE, MUTED}));
    sw.setTrackTintList(
        new ColorStateList(
            new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}},
            new int[] {0xFF216386, LINE}));
    sw.setContentDescription(title);
    row.addView(sw);
    parent.addView(row);
    return sw;
  }

  private void settings() {
    String[] range = {rangeFrom(), rangeTo()};
    LinearLayout scope = card(SURFACE);
    scope.addView(text("Phạm vi theo dõi", 14, INK, true));
    gap(scope, 10);
    String[] rangeLabels = {"Từ đường ngang", "Đến đường ngang"};
    for (int i = 0; i < 2; i++) {
      final int index = i;
      LinearLayout entry = row();
      entry.addView(
          text(rangeLabels[i], 11, MUTED, false), new LinearLayout.LayoutParams(0, -2, 1));
      TextView choice =
          pill(range[i].isEmpty() ? "Chưa có danh sách" : "Km " + range[i] + "  ▾", BLUE, TINT);
      touch(
          choice,
          () -> {
            if (route().crossings().isEmpty()) {
              Toast.makeText(
                      this, "Kết nối tài khoản để đọc danh sách đường ngang", Toast.LENGTH_LONG)
                  .show();
              return;
            }
            String[] names = new String[route().crossings().size()];
            for (int j = 0; j < names.length; j++) names[j] = "Km " + route().crossings().get(j);
            new AlertDialog.Builder(this)
                .setTitle(rangeLabels[index])
                .setSingleChoiceItems(
                    names,
                    route().crossings().indexOf(range[index]),
                    (dialog, which) -> {
                      range[index] = route().crossings().get(which);
                      choice.setText("Km " + range[index] + "  ▾");
                      dialog.dismiss();
                    })
                .setNegativeButton("Đóng", null)
                .show();
          });
      entry.addView(choice);
      scope.addView(entry);
      gap(scope, 9);
    }
    scope.addView(
        action(
            "Áp dụng phạm vi",
            null,
            false,
            () -> {
              if (applyScope(range[0], range[1])) navigate("overview");
            }));
    content.addView(scope);
    LinearLayout account = card(SURFACE);
    account.addView(text("Kết nối tài khoản", 16, INK, true));
    gap(account, 6);
    account.addView(text("Sử dụng tài khoản website của đơn vị", 11, MUTED, false));
    gap(account, 21);
    EditText url =
        field(
            account,
            "ĐỊA CHỈ WEBSITE",
            "http://…",
            store.prefs.getString("url", "http://"),
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    EditText user =
        field(
            account,
            "ID ĐĂNG NHẬP",
            "Nhập tài khoản",
            store.prefs.getString("user", ""),
            InputType.TYPE_CLASS_TEXT);
    EditText pass =
        field(
            account,
            "MẬT KHẨU",
            store.prefs.contains("password") ? "Để trống để giữ mật khẩu đã lưu" : "Nhập mật khẩu",
            "",
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    TextView reveal = text("Hiện mật khẩu", 11, BLUE, true);
    reveal.setGravity(Gravity.END);
    touch(
        reveal,
        () -> {
          boolean visible =
              pass.getInputType()
                  == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
          pass.setInputType(
              InputType.TYPE_CLASS_TEXT
                  | (visible
                      ? InputType.TYPE_TEXT_VARIATION_PASSWORD
                      : InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
          pass.setSelection(pass.length());
          reveal.setText(visible ? "Hiện mật khẩu" : "Ẩn mật khẩu");
        });
    account.addView(reveal);
    content.addView(account);
    LinearLayout options = card(SURFACE);
    options.addView(text("Theo dõi & thông báo", 16, INK, true));
    gap(options, 18);
    options.addView(text("CHU KỲ ĐỌC DỮ LIỆU", 10, MUTED, true));
    gap(options, 10);
    int[] selectedInterval = {store.prefs.getInt("interval", 30)};
    LinearLayout segments = row();
    segments.setPadding(dp(4), dp(4), dp(4), dp(4));
    segments.setBackground(shape(BG, 12, 0));
    int[] intervals = {30, 120, 300};
    String[] intervalNames = {"30 giây", "2 phút", "5 phút"};
    for (int i = 0; i < 3; i++) {
      int value = intervals[i];
      TextView option =
          text(intervalNames[i], 12, selectedInterval[0] == value ? BLUE : MUTED, true);
      option.setGravity(Gravity.CENTER);
      option.setPadding(0, dp(11), 0, dp(11));
      if (selectedInterval[0] == value) option.setBackground(shape(TINT, 9, 0));
      segments.addView(option, new LinearLayout.LayoutParams(0, -2, 1));
      touch(
          option,
          () -> {
            selectedInterval[0] = value;
            for (int j = 0; j < 3; j++) {
              TextView v = (TextView) segments.getChildAt(j);
              v.setTextColor(intervals[j] == value ? BLUE : MUTED);
              v.setBackground(shape(intervals[j] == value ? TINT : Color.TRANSPARENT, 9, 0));
            }
          });
    }
    options.addView(segments);
    gap(options, 7);
    options.addView(text("Tự kích chỉ theo dõi ở chu kỳ 30 giây", 10, MUTED, false));
    divider(options);
    Switch sound =
        toggle(
            "Chuông sự cố",
            "Tắt để chỉ hiển thị cảnh báo",
            store.prefs.getBoolean("sound", true),
            options);
    sound.setOnCheckedChangeListener((b, on) -> setAlarmSound(on));
    gap(options, 10);
    options.addView(
        action(
            "Thử chuông báo lỗi",
            "bell",
            false,
            () -> {
              setVolumeControlStream(android.media.AudioManager.STREAM_ALARM);
              boolean played = testAlarm.play(this);
              Toast.makeText(
                      this,
                      played
                          ? "Đang thử chuông · Dùng phím âm lượng để chỉnh Báo thức"
                          : "Chưa phát được chuông trên thiết bị",
                      Toast.LENGTH_LONG)
                  .show();
            }));
    gap(options, 7);
    options.addView(text("Âm lượng chuông dùng mức Báo thức của điện thoại", 10, MUTED, false));
    gap(options, 10);
    options.addView(
        action(
            "Cài đặt chạy nền",
            "link",
            false,
            () ->
                startActivity(
                    new Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:" + getPackageName())))));
    gap(options, 7);
    options.addView(
        text(
            "Cho phép thông báo và chọn pin Không giới hạn để theo dõi khi khóa màn hình.",
            10,
            MUTED,
            false));
    content.addView(options);
    content.addView(
        action(
            "Lưu & bắt đầu theo dõi",
            "link",
            true,
            () -> {
              try {
                String endpoint = url.getText().toString().trim();
                var uri = java.net.URI.create(endpoint);
                if (!List.of("http", "https").contains(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getUserInfo() != null)
                  throw new Exception("Nhập địa chỉ http:// hoặc https:// hợp lệ");
                String login = user.getText().toString().trim();
                if (login.isEmpty()) throw new Exception("Cần nhập ID đăng nhập");
                String secret = pass.getText().toString();
                boolean changed =
                    !endpoint.equals(store.prefs.getString("url", ""))
                        || !login.equals(store.prefs.getString("user", ""));
                if (secret.isEmpty() && !changed) secret = store.password();
                if (secret.isEmpty()) throw new Exception("Cần nhập mật khẩu cho tài khoản này");
                if (!changed
                    && !demo
                    && !route().crossings().isEmpty()
                    && !applyScope(range[0], range[1])) return;
                stopService(new Intent(this, MonitorService.class));
                store.credentials(endpoint, login, secret);
                store
                    .prefs
                    .edit()
                    .putInt("interval", selectedInterval[0])
                    .putBoolean("sound", sound.isChecked())
                    .putBoolean("demoUi", false)
                    .apply();
                demo = false;
                startForegroundService(new Intent(this, MonitorService.class).setAction("RESTART"));
                navigate("overview");
              } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
              }
            }));
    gap(content, 10);
    content.addView(
        action(
            "Dừng theo dõi",
            null,
            false,
            () -> {
              stopService(new Intent(this, MonitorService.class));
              store.prefs.edit().putString("status", "Đã dừng theo dõi").apply();
              Toast.makeText(this, "Đã dừng theo dõi", Toast.LENGTH_SHORT).show();
            }));
  }

  private void openCrossing(String km) {
    selectedCrossing = Rules.km(km);
    navigate("detail");
  }

  private List<Rules.Event> crossingEvents(String km) {
    List<Rules.Event> list = new ArrayList<>();
    for (Rules.Event e : allToday()) if (Rules.km(e.crossing()).equals(Rules.km(km))) list.add(e);
    list.sort(Comparator.comparingLong(Rules.Event::at).reversed());
    return list;
  }

  private void crossingList() {
    LinearLayout introduction = row();
    int total = 0;
    for (String point : route().crossings()) if (inScope(point)) total++;
    introduction.addView(
        text("KHU VỰC THEO DÕI", 10, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1));
    introduction.addView(pill(total + " đường ngang", BLUE, TINT));
    content.addView(introduction);
    gap(content, 14);
    LinearLayout list = card(SURFACE);
    int index = 0;
    for (String point : route().points()) {
      if (Rules.isStation(point) || !inScope(point)) continue;
      if (index++ > 0) divider(list);
      List<Rules.Event> events = crossingEvents(point);
      boolean fault = !faultsAt(point).isEmpty();
      LinearLayout entry = row();
      LinearLayout words = column();
      words.addView(text("Km " + point, 15, INK, true));
      gap(words, 7);
      words.addView(
          text(
              events.isEmpty()
                  ? "Chưa có dữ liệu"
                  : fault ? "●  Có ghi nhận lỗi" : "●  Chưa ghi nhận lỗi",
              10,
              events.isEmpty() ? MUTED : fault ? RED : GREEN,
              false));
      entry.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
      entry.addView(text(events.isEmpty() ? "—" : time(events.get(0).at()), 11, MUTED, false));
      TextView more = text("  ›", 24, MUTED, false);
      entry.addView(more);
      touch(entry, () -> openCrossing(point));
      list.addView(entry);
    }
    content.addView(list);
  }

  private String latestDevice(List<Rules.Event> events, String... keywords) {
    for (Rules.Event e : events) {
      String t = e.text().toLowerCase(Locale.ROOT);
      for (String keyword : keywords)
        if (t.contains(keyword)) return e.text() + " · " + time(e.at());
    }
    return "Chưa có dữ liệu";
  }

  private String deviceReading(
      List<Rules.Event> events, List<Rules.Event> faults, String... keywords) {
    List<String> active = new ArrayList<>();
    for (Rules.Event fault : faults) {
      String lower = fault.text().toLowerCase(Locale.ROOT);
      for (String keyword : keywords)
        if (lower.contains(keyword)) {
          active.add(fault.text() + " · " + time(fault.at()));
          break;
        }
    }
    return active.isEmpty() ? latestDevice(events, keywords) : String.join("\n", active);
  }

  private void deviceRow(
      LinearLayout parent, String title, String value, String kind, boolean error) {
    LinearLayout r = new LinearLayout(this);
    r.setGravity(Gravity.TOP);
    boolean recovered = value.toLowerCase(Locale.ROOT).contains("khôi phục tốt");
    r.addView(icon(kind, error ? RED : recovered ? GREEN : BLUE, 17));
    TextView name = text(title, 11, MUTED, false);
    name.setPadding(dp(9), 0, dp(8), 0);
    r.addView(name, new LinearLayout.LayoutParams(dp(108), -2));
    TextView status =
        text(
            value,
            11,
            error ? RED : recovered ? GREEN : value.equals("Chưa có dữ liệu") ? MUTED : INK,
            true);
    r.addView(status, new LinearLayout.LayoutParams(0, -2, 1));
    parent.addView(r);
  }

  private void crossingDetail() {
    List<Rules.Event> events = crossingEvents(selectedCrossing);
    List<Rules.Event> faults = faultsAt(selectedCrossing);
    LinearLayout summary = card(SURFACE);
    LinearLayout top = row();
    top.addView(
        text("Km " + selectedCrossing, 22, INK, true), new LinearLayout.LayoutParams(0, -2, 1));
    boolean resetBaseline = !demo && store.prefs.getLong("baselineFrom", 0) > 0;
    String badge =
        !faults.isEmpty()
            ? "Đang có sự cố"
            : resetBaseline
                ? "✓ Đã đặt lại"
                : events.isEmpty() ? "Chờ dữ liệu" : "✓ Chưa ghi nhận lỗi";
    int badgeColor = !faults.isEmpty() ? RED : events.isEmpty() && !resetBaseline ? MUTED : GREEN;
    top.addView(pill(badge, badgeColor, !faults.isEmpty() ? FAULT_TINT : GOOD));
    summary.addView(top);
    gap(summary, 9);
    summary.addView(
        text(
            events.isEmpty() ? "Chờ dữ liệu từ website" : "Cập nhật: " + time(events.get(0).at()),
            11,
            MUTED,
            false));
    gap(summary, 7);
    summary.addView(text("Thông tin ghi nhận gần nhất", 10, MUTED, false));
    divider(summary);
    String bell = deviceReading(events, faults, "chuông"),
        light = deviceReading(events, faults, "đèn");
    String barriers = deviceReading(events, faults, "cần chắn");
    List<Rules.Event> sensorEvents = new ArrayList<>(), sensorFaults = new ArrayList<>();
    for (var event : events)
      if (!event.text().toLowerCase(Locale.ROOT).contains("điện áp")) sensorEvents.add(event);
    for (var event : faults)
      if (!event.text().toLowerCase(Locale.ROOT).contains("điện áp")) sensorFaults.add(event);
    String sensors = deviceReading(sensorEvents, sensorFaults, "cảm biến");
    String connection = deviceReading(events, faults, "kết nối giám sát"),
        voltage = deviceReading(events, faults, "điện áp", "220vac");
    String[] labels = {"Chuông", "Đèn", "Cần chắn", "Cảm biến", "Kết nối", "Điện áp"};
    String[] values = {bell, light, barriers, sensors, connection, voltage};
    String[] icons = {"bell", "alert", "rail", "overview", "link", "voltage"};
    for (int i = 0; i < labels.length; i++) {
      final String value = values[i];
      boolean redReading = faults.stream().anyMatch(e -> value.contains(e.text()));
      deviceRow(summary, labels[i], value, icons[i], redReading);
      if (i < labels.length - 1) divider(summary);
    }
    content.addView(summary);
    LinearLayout history = card(SURFACE);
    history.addView(text("Lịch sử sự kiện gần nhất", 15, INK, true));
    gap(history, 18);
    if (events.isEmpty())
      empty(history, "Chưa có sự kiện", "Dữ liệu sẽ xuất hiện sau khi kết nối.", "history");
    for (int i = 0; i < Math.min(12, events.size()); i++) {
      if (i > 0) divider(history);
      Rules.Event e = events.get(i);
      eventRow(history, e, e.red());
      String note = Rules.axleNote(e);
      if (!note.isEmpty()) {
        gap(history, 10);
        history.addView(text(note, 11, MUTED, false));
      }
    }
    content.addView(history);
    content.addView(action("Xem trên sơ đồ tuyến", "direction", true, () -> navigate("overview")));
    gap(content, 17);
  }

  private void navigation() {
    LinearLayout nav = row();
    nav.setPadding(dp(12), dp(5), dp(12), dp(5));
    nav.setBackground(shape(BG, 0, LINE));
    String[] pages = {"overview", "crossings", "incidents", "settings"},
        names = {"Tổng quan", "Đường ngang", "Sự cố", "Cài đặt"},
        icons = {"overview", "rail", "alert", "settings"};
    for (int i = 0; i < pages.length; i++) {
      String target = pages[i];
      boolean active =
          page.equals(target)
              || (page.equals("detail") && target.equals("crossings"))
              || (page.equals("trains") && target.equals("overview"));
      LinearLayout tab = column();
      tab.setGravity(Gravity.CENTER);
      tab.setPadding(0, dp(5), 0, dp(4));
      tab.addView(icon(icons[i], active ? BLUE : MUTED, 21));
      gap(tab, 5);
      TextView tabLabel = text(names[i], 10, active ? BLUE : MUTED, active);
      tabLabel.setGravity(Gravity.CENTER);
      tab.addView(tabLabel);
      if (active) tab.setBackground(shape(TINT, 13, 0));
      nav.addView(tab, new LinearLayout.LayoutParams(0, dp(48), 1));
      touch(tab, () -> navigate(target));
    }
    screen.addView(nav);
  }

  private class RouteView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<String> points = route().points();
    private final int[] occupied = new int[points.size()];
    private List<Rules.TrackSegment> segments = List.of();

    RouteView() {
      super(MainActivity.this);
      updateOccupancy();
    }

    void updateOccupancy() {
      Arrays.fill(occupied, 0);
      Rules.TravelState state = diagramState();
      segments = Rules.trackSegments(route(), state);
      for (Rules.Occupancy report : Rules.approachingCrossings(state)) {
        int index = points.indexOf(report.crossing());
        if (index >= 0) occupied[index] = report.direction();
      }
      invalidate();
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
      if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
        if (points.isEmpty()) return true;
        float step = points.size() > 1 ? (getWidth() - dp(60)) / (float) (points.size() - 1) : 1;
        int index =
            points.size() == 1
                ? 0
                : Math.max(
                    0, Math.min(points.size() - 1, Math.round((event.getX() - dp(25)) / step)));
        String point = points.get(index);
        if (Rules.isStation(point)) {
          Toast.makeText(MainActivity.this, "Ga " + Rules.stationCode(point), Toast.LENGTH_SHORT)
              .show();
        } else openCrossing(point);
        performClick();
        return true;
      }
      return true;
    }

    @Override
    public boolean performClick() {
      super.performClick();
      return true;
    }

    @Override
    protected void onDraw(Canvas c) {
      super.onDraw(c);
      if (points.isEmpty()) {
        p.setColor(MUTED);
        p.setTextSize(dp(11));
        c.drawText("Đang chờ danh sách tuyến từ web", dp(7), dp(38), p);
        return;
      }
      float left = points.size() == 1 ? getWidth() / 2f : dp(25),
          right = points.size() == 1 ? left : getWidth() - dp(35),
          step = points.size() > 1 ? (right - left) / (points.size() - 1) : 0,
          y = dp(50);
      p.setStrokeCap(Paint.Cap.ROUND);
      p.setStrokeWidth(dp(3));
      p.setColor(LINE);
      c.drawLine(left - dp(20), y, right + dp(20), y, p);
      int reportColor = staleData() ? WARNING : GREEN;
      List<float[]> labels = new ArrayList<>();
      p.setColor(reportColor);
      p.setStrokeWidth(dp(5));
      p.setStrokeCap(Paint.Cap.BUTT);
      for (Rules.TrackSegment leg : segments) {
        float[] ends = segmentEnds(leg, left, right, step);
        if (ends == null) continue;
        float x1 = ends[0], x2 = ends[1];
        p.setColor(reportColor);
        p.setStrokeWidth(dp(5));
        p.setStrokeCap(Paint.Cap.BUTT);
        c.drawLine(Math.min(x1, x2), y, Math.max(x1, x2), y, p);
      }
      for (Rules.TrackSegment leg : segments) {
        float[] ends = segmentEnds(leg, left, right, step);
        if (ends != null)
          drawDirection(c, labels, (ends[0] + ends[1]) / 2f, y, leg.direction(), reportColor);
      }
      for (int i = 0; i < points.size(); i++) {
        float x = left + step * i;
        String km = points.get(i);
        boolean station = Rules.isStation(km);
        p.setStyle(Paint.Style.FILL);
        p.setColor(SURFACE);
        c.drawCircle(x, y, dp(5.5f), p);
        p.setColor(station ? BLUE : occupied[i] != 0 ? reportColor : 0xFF63728F);
        if (station) c.drawRoundRect(x - dp(4), y - dp(4), x + dp(4), y + dp(4), dp(2), dp(2), p);
        else c.drawCircle(x, y, dp(3.5f), p);
        p.setTypeface(
            Typeface.create(station ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        p.setTextSize(dp(station ? 9 : 8.5f));
        p.setColor(station ? BLUE : MUTED);
        if (station) {
          String name = Rules.stationCode(km);
          c.drawText(name, x - p.measureText(name) / 2, y + dp(17), p);
        } else {
          c.save();
          c.translate(x - dp(2), y + dp(14));
          c.rotate(57);
          c.drawText(km, 0, 0, p);
          c.restore();
        }
      }
    }

    private float[] segmentEnds(Rules.TrackSegment leg, float left, float right, float step) {
      int from = points.indexOf(leg.from()), to = points.indexOf(leg.to());
      if (leg.from() == null) {
        if (to != 0 && to != points.size() - 1) return null;
        float end = left + step * to;
        return new float[] {end - leg.direction() * dp(20), end};
      }
      if (leg.to() == null) {
        if (from != 0 && from != points.size() - 1) return null;
        float start = left + step * from;
        return new float[] {start, start + leg.direction() * dp(20)};
      }
      if (from < 0 || to < 0 || Math.abs(from - to) != 1) return null;
      return new float[] {left + step * from, left + step * to};
    }

    private void drawDirection(
        Canvas c, List<float[]> labels, float ax, float y, int direction, int color) {
      p.setStyle(Paint.Style.FILL);
      p.setColor(color);
      p.setStrokeWidth(dp(1.7f));
      p.setStrokeCap(Paint.Cap.ROUND);
      p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
      boolean compact = segments.size() > 4;
      p.setTextSize(dp(compact ? 8 : 9));
      String name =
          (direction > 0 ? (compact ? "Lẻ →" : "Tàu lẻ →") : (compact ? "Chẵn ←" : "Tàu chẵn ←"));
      float half = p.measureText(name) / 2;
      float center = Math.max(half + dp(1), Math.min(getWidth() - half - dp(1), ax));
      int lane = 0;
      boolean collision;
      do {
        collision = false;
        for (float[] label : labels)
          if ((int) label[2] == lane && Math.abs(label[0] - center) < label[1] + half + dp(3)) {
            lane++;
            collision = true;
            break;
          }
      } while (collision);
      float baseline = y - dp(8 + lane * 11);
      labels.add(new float[] {center, half, lane, baseline + p.ascent(), baseline + p.descent()});
      c.drawText(name, center - half, baseline, p);
    }
  }

  /** Small consistent line icons, drawn locally without image/network dependencies. */
  private class Icon extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String kind;
    private final int color;

    Icon(String k, int c) {
      super(MainActivity.this);
      kind = k;
      color = c;
      setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);
      canvas.save();
      canvas.scale(getWidth() / 24f, getHeight() / 24f);
      p.setColor(color);
      p.setStyle(Paint.Style.STROKE);
      p.setStrokeWidth(1.65f);
      p.setStrokeCap(Paint.Cap.ROUND);
      p.setStrokeJoin(Paint.Join.ROUND);
      switch (kind) {
        case "rail":
          canvas.drawLine(8, 3, 5, 21, p);
          canvas.drawLine(16, 3, 19, 21, p);
          for (int y = 5; y < 22; y += 5) canvas.drawLine(7, y, 17, y, p);
          break;
        case "overview":
          for (int x = 4; x <= 14; x += 10)
            for (int y = 4; y <= 14; y += 10)
              canvas.drawRoundRect(x, y, x + 6, y + 6, 1.5f, 1.5f, p);
          break;
        case "settings":
          canvas.drawCircle(12, 12, 6.5f, p);
          canvas.drawCircle(12, 12, 2.4f, p);
          for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            canvas.drawLine(
                12 + (float) Math.cos(a) * 7,
                12 + (float) Math.sin(a) * 7,
                12 + (float) Math.cos(a) * 9.5f,
                12 + (float) Math.sin(a) * 9.5f,
                p);
          }
          break;
        case "history":
          canvas.drawArc(4, 4, 21, 21, -140, 290, false, p);
          canvas.drawLine(4, 3, 4, 9, p);
          canvas.drawLine(4, 9, 10, 9, p);
          canvas.drawLine(12, 8, 12, 13, p);
          canvas.drawLine(12, 13, 16, 15, p);
          break;
        case "direction":
          canvas.drawLine(4, 9, 20, 9, p);
          canvas.drawLine(16, 5, 20, 9, p);
          canvas.drawLine(16, 13, 20, 9, p);
          canvas.drawLine(20, 17, 4, 17, p);
          canvas.drawLine(8, 13, 4, 17, p);
          canvas.drawLine(8, 21, 4, 17, p);
          break;
        case "alert":
          Path triangle = new Path();
          triangle.moveTo(12, 3);
          triangle.lineTo(22, 20);
          triangle.lineTo(2, 20);
          triangle.close();
          canvas.drawPath(triangle, p);
          canvas.drawLine(12, 9, 12, 13, p);
          canvas.drawPoint(12, 17, p);
          break;
        case "bell":
        case "mute":
          canvas.drawArc(6, 4, 18, 19, 180, 180, false, p);
          canvas.drawLine(6, 11, 6, 16, p);
          canvas.drawLine(18, 11, 18, 16, p);
          canvas.drawLine(4, 18, 20, 18, p);
          canvas.drawArc(9, 18, 15, 23, 15, 150, false, p);
          if (kind.equals("mute")) canvas.drawLine(3, 3, 21, 21, p);
          break;
        case "down":
          canvas.drawLine(6, 9, 12, 15, p);
          canvas.drawLine(12, 15, 18, 9, p);
          break;
        case "up":
          canvas.drawLine(6, 15, 12, 9, p);
          canvas.drawLine(12, 9, 18, 15, p);
          break;
        case "back":
          canvas.drawLine(5, 12, 20, 12, p);
          canvas.drawLine(5, 12, 11, 6, p);
          canvas.drawLine(5, 12, 11, 18, p);
          break;
        case "close":
          canvas.drawLine(6, 6, 18, 18, p);
          canvas.drawLine(18, 6, 6, 18, p);
          break;
        case "voltage":
          Path bolt = new Path();
          bolt.moveTo(14, 2);
          bolt.lineTo(5, 14);
          bolt.lineTo(11, 14);
          bolt.lineTo(10, 22);
          bolt.lineTo(20, 9);
          bolt.lineTo(13, 9);
          bolt.close();
          canvas.drawPath(bolt, p);
          break;
        case "calendar":
          canvas.drawRoundRect(4, 5, 20, 21, 3, 3, p);
          canvas.drawLine(4, 10, 20, 10, p);
          canvas.drawLine(8, 3, 8, 7, p);
          canvas.drawLine(16, 3, 16, 7, p);
          canvas.drawPoint(9, 15, p);
          canvas.drawPoint(15, 15, p);
          break;
        case "download":
          canvas.drawLine(12, 3, 12, 15, p);
          canvas.drawLine(7, 10, 12, 15, p);
          canvas.drawLine(12, 15, 17, 10, p);
          canvas.drawLine(4, 16, 4, 21, p);
          canvas.drawLine(4, 21, 20, 21, p);
          canvas.drawLine(20, 21, 20, 16, p);
          break;
        case "shield":
          Path shield = new Path();
          shield.moveTo(12, 3);
          shield.lineTo(20, 6);
          shield.lineTo(19, 15);
          shield.quadTo(16, 20, 12, 22);
          shield.quadTo(8, 20, 5, 15);
          shield.lineTo(4, 6);
          shield.close();
          canvas.drawPath(shield, p);
          canvas.drawLine(8, 12, 11, 15, p);
          canvas.drawLine(11, 15, 16, 9, p);
          break;
        default:
          canvas.drawArc(3, 7, 14, 17, 90, 240, false, p);
          canvas.drawArc(10, 7, 21, 17, -90, 240, false, p);
          canvas.drawLine(8, 12, 16, 12, p);
      }
      canvas.restore();
    }
  }
}
