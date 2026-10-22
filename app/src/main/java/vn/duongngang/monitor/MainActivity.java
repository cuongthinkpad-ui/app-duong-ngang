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
  private static final int BLUE = 0xFF246BCE,
      INK = 0xFF1C304C,
      MUTED = 0xFF8290A5,
      BG = 0xFFF3F6FB,
      LINE = 0xFFE6EDF5,
      GREEN = 0xFF219B70,
      RED = 0xFFC64A55;
  private Store store;
  private LinearLayout screen, content;
  private TextView status;
  private String page = "overview",
      filter = "Tất cả",
      selectedCrossing = "312+111",
      incidentFilter = "Tất cả";
  private boolean demo = false, expanded = false;
  private LocalDate selected = LocalDate.now(WebSource.ZONE);
  private final long demoClock = System.currentTimeMillis();
  private final ExecutorService io = Executors.newSingleThreadExecutor();
  private final Handler handler = new Handler(Looper.getMainLooper());
  private String lastUiState = "";
  private final Runnable refresh =
      new Runnable() {
        public void run() {
          String state =
              store.prefs.getString("status", "") + store.prefs.getLong("lastSuccess", 0);
          if (!state.equals(lastUiState)) {
            lastUiState = state;
            if (!page.equals("settings")) render();
          }
          handler.postDelayed(this, 2000);
        }
      };

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    store = new Store(this);
    demo = store.prefs.getBoolean("demoUi", store.prefs.getString("url", "").isEmpty());
    getWindow().setStatusBarColor(0xFF102F52);
    getWindow().setNavigationBarColor(Color.WHITE);
    if (Build.VERSION.SDK_INT >= 26)
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    if (Build.VERSION.SDK_INT >= 33)
      requestPermissions(new String[] {"android.permission.POST_NOTIFICATIONS"}, 10);
    render();
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
    c.setBackground(shape(color, 16, color == Color.WHITE ? LINE : 0));
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
    b.setBackground(shape(Color.WHITE, 14, LINE));
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
    b.setBackground(shape(primary ? BLUE : Color.WHITE, 13, primary ? 0 : LINE));
    if (kind != null) {
      b.addView(icon(kind, primary ? Color.WHITE : BLUE, 17));
      View spacer = new View(this);
      b.addView(spacer, new LinearLayout.LayoutParams(dp(8), 1));
    }
    b.addView(text(title, 13, primary ? Color.WHITE : BLUE, true));
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
                    "d7", demoClock - 405000, "Km 313+800", "Tàu chẵn", false, List.of()),
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
                    "d10",
                    demoClock - 215000,
                    "Km 321+780",
                    "Mất tín hiệu chuông",
                    true,
                    List.of()),
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

  private String rangeFrom() {
    return store.prefs.getString("rangeFrom", "312+111");
  }

  private String rangeTo() {
    return store.prefs.getString("rangeTo", "334+830");
  }

  private boolean inScope(String crossing) {
    return Rules.inRange(crossing, rangeFrom(), rangeTo());
  }

  private List<Rules.Event> today() {
    List<Rules.Event> all =
        demo
            ? samples()
            : store.events(
                LocalDate.now(WebSource.ZONE)
                    .atStartOfDay(WebSource.ZONE)
                    .toInstant()
                    .toEpochMilli(),
                System.currentTimeMillis() + 1);
    List<Rules.Event> selected = new ArrayList<>();
    for (Rules.Event e : all) if (inScope(e.crossing())) selected.add(e);
    return selected;
  }

  private boolean applyScope(String from, String to) {
    if (!Rules.validRange(from, to)) {
      Toast.makeText(
              this,
              "Đường ngang bắt đầu phải ở phía Bắc hoặc trùng điểm kết thúc",
              Toast.LENGTH_LONG)
          .show();
      return false;
    }
    store.prefs.edit().putString("rangeFrom", from).putString("rangeTo", to).apply();
    for (Rules.Event e : store.events(0, System.currentTimeMillis() + 1))
      if (!inScope(e.crossing()))
        getSystemService(NotificationManager.class)
            .cancel(100 + Math.floorMod(e.id().hashCode(), 1000000));
    return true;
  }

  private List<Rules.Event> currentFaults(List<Rules.Event> events) {
    if (demo) return Rules.activeFaults(events);
    long baseline = store.prefs.getLong("baselineFrom", 0);
    List<Rules.Event> current = new ArrayList<>();
    for (Rules.Event e : events) if (e.at() >= baseline) current.add(e);
    return Rules.activeFaults(current);
  }

  private void navigate(String next) {
    page = next;
    render();
  }

  private void render() {
    screen = column();
    screen.setBackgroundColor(BG);
    setContentView(screen);
    LinearLayout header = column();
    header.setPadding(dp(16), dp(8), dp(16), dp(10));
    header.setBackgroundColor(0xFF102F52);
    LinearLayout titleRow = row();
    LinearLayout mark = row();
    mark.setGravity(Gravity.CENTER);
    mark.setBackground(shape(0xFF23496E, 14, 0));
    mark.addView(icon("rail", Color.WHITE, 22));
    titleRow.addView(mark, new LinearLayout.LayoutParams(dp(37), dp(37)));
    LinearLayout titles = column();
    titles.setPadding(dp(12), 0, 0, 0);
    TextView eyebrow =
        text(demo ? "GIÁM SÁT ĐƯỜNG SẮT · MINH HỌA" : "GIÁM SÁT ĐƯỜNG SẮT", 8, 0xFF9CB8D7, true);
    eyebrow.setLetterSpacing(.12f);
    titles.addView(eyebrow);
    gap(titles, 5);
    titles.addView(
        text(
            page.equals("incidents")
                ? "Sự cố đường ngang"
                : page.equals("settings")
                    ? "Cài đặt"
                    : page.equals("detail")
                        ? "Chi tiết đường ngang"
                        : page.equals("crossings") ? "Danh sách đường ngang" : "Đường ngang",
            page.equals("detail") || page.equals("crossings") ? 17 : 20,
            Color.WHITE,
            true));
    titleRow.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
    titleRow.addView(
        iconButton(
            page.equals("detail") ? "back" : page.equals("settings") ? "close" : "settings",
            page.equals("settings") ? "Về tổng quan" : "Cài đặt",
            () ->
                navigate(
                    page.equals("detail")
                        ? "crossings"
                        : page.equals("settings") ? "overview" : "settings")));
    header.addView(titleRow);
    screen.addView(header);
    content = column();
    content.setPadding(dp(14), page.equals("overview") ? 0 : dp(8), dp(14), 0);
    if (page.equals("overview")) {
      LinearLayout map = routeCard();
      LinearLayout mapWrapper = column();
      mapWrapper.setPadding(dp(14), dp(8), dp(14), 0);
      mapWrapper.addView(map);
      screen.addView(mapWrapper);
    }
    ScrollView scroll = new ScrollView(this);
    scroll.setClipToPadding(false);
    scroll.setFillViewport(true);
    scroll.setVerticalScrollBarEnabled(false);
    scroll.addView(content);
    screen.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
    if (page.equals("overview")) overview();
    else if (page.equals("incidents")) incidents();
    else if (page.equals("crossings")) crossingList();
    else if (page.equals("detail")) crossingDetail();
    else settings();
    status = text("", 11, MUTED, false);
    if (!demo && !store.prefs.getString("status", "").isEmpty() && !page.equals("settings")) {
      status.setText(store.prefs.getString("status", ""));
      status.setPadding(dp(2), 0, 0, dp(12));
      content.addView(status);
    }
    TextView credit = text("Designed by Cao Xuân Cường", 6, 0xFFA5B1C2, false);
    credit.setGravity(Gravity.END);
    credit.setPadding(0, dp(3), dp(22), dp(5));
    screen.addView(credit);
    navigation();
  }

  private LinearLayout routeCard() {
    LinearLayout c = card(Color.WHITE);
    LinearLayout compass = row();
    compass.setPadding(0, dp(2), 0, 0);
    compass.addView(text("BẮC", 9, MUTED, true));
    TextView heading = text("Lý trình tăng  →", 9, MUTED, false);
    heading.setGravity(Gravity.CENTER);
    compass.addView(heading, new LinearLayout.LayoutParams(0, -2, 1));
    compass.addView(text("NAM", 9, MUTED, true));
    c.addView(compass);
    RouteView route = new RouteView();
    route.setContentDescription(
        "Sơ đồ từ Km "
            + rangeFrom()
            + " đến Km "
            + rangeTo()
            + (demo ? " · Dữ liệu minh họa" : " · Chưa hiển thị vị trí tàu thật"));
    c.addView(route, new LinearLayout.LayoutParams(-1, dp(76)));
    LinearLayout legend = row();
    legend.addView(pill("→  Tàu lẻ", GREEN, 0xFFEAF7F1));
    TextView other = text("←  Tàu chẵn", 10, MUTED, false);
    other.setPadding(dp(12), 0, 0, 0);
    legend.addView(other, new LinearLayout.LayoutParams(0, -2, 1));

    c.addView(legend);
    return c;
  }

  private void overview() {
    List<Rules.Event> events = today();
    List<Rules.Event> trains = new ArrayList<>();
    for (var e : events) if (Rules.direction(e.text()) != 0) trains.add(e);
    LinearLayout train = card(Color.WHITE);
    LinearLayout title = row();
    title.addView(icon("direction", BLUE, 19));
    TextView caption = text("Tàu gần nhất", 13, INK, true);
    caption.setPadding(dp(8), 0, 0, 0);
    title.addView(caption, new LinearLayout.LayoutParams(0, -2, 1));
    title.addView(icon(expanded ? "up" : "down", MUTED, 17));
    train.addView(title);
    gap(train, 9);
    if (trains.isEmpty())
      empty(train, "Chưa ghi nhận tàu", "Thông tin xuất hiện khi có dữ liệu mới.", "direction");
    else {
      int visible = expanded ? trains.size() : Math.min(3, trains.size());
      for (int i = 0; i < visible; i++) {
        if (i > 0) divider(train);
        eventRow(train, trains.get(i), false);
      }
      if (trains.size() > 3) {
        gap(train, 7);
        train.addView(text(expanded ? "Thu gọn" : "Xem thêm lượt tàu", 9, MUTED, false));
      }
    }
    touch(
        train,
        () -> {
          expanded = !expanded;
          render();
        });
    content.addView(train);
    List<Rules.Event> faults = currentFaults(events);
    LinearLayout faultCard = card(0xFFFFF0F1);
    LinearLayout ft = row();
    ft.addView(icon("alert", RED, 19));
    TextView ftitle = text("Sự cố", 13, RED, true);
    ftitle.setPadding(dp(8), 0, dp(9), 0);
    ft.addView(ftitle);
    ft.addView(pill(String.format(Locale.ROOT, "%02d", faults.size()), RED, 0xFFFFDFE2));
    View stretch = new View(this);
    ft.addView(stretch, new LinearLayout.LayoutParams(0, 1, 1));
    boolean sound = store.prefs.getBoolean("sound", true);
    View bell = icon(sound ? "bell" : "mute", RED, 21);
    LinearLayout hit = row();
    hit.setGravity(Gravity.CENTER);
    hit.addView(bell);
    hit.setContentDescription(sound ? "Tắt chuông sự cố" : "Bật chuông sự cố");
    touch(
        hit,
        () -> {
          store.prefs.edit().putBoolean("sound", !sound).apply();
          render();
        });
    ft.addView(hit, new LinearLayout.LayoutParams(dp(44), dp(36)));
    faultCard.addView(ft);
    if (faults.isEmpty()) {
      gap(faultCard, 16);
      empty(
          faultCard,
          store.prefs.getLong("lastSuccess", 0) == 0 && !demo
              ? "Đang chờ dữ liệu"
              : "✓ Chưa ghi nhận lỗi mới",
          store.prefs.getLong("lastSuccess", 0) == 0 && !demo
              ? "Kết nối tài khoản để bắt đầu theo dõi."
              : "Trong phần dữ liệu đã đọc.",
          "shield");
    }
    for (int i = 0; i < Math.min(3, faults.size()); i++) {
      if (i == 0) gap(faultCard, 9);
      else divider(faultCard);
      var e = faults.get(i);
      eventRow(faultCard, e, true);
      gap(faultCard, 5);
      LinearLayout controls = row();
      TextView indicator = text("Chưa khôi phục", 10, RED, false);
      controls.addView(indicator, new LinearLayout.LayoutParams(0, -2, 1));
      TextView quiet = pill("Tắt chuông", RED, 0xFFFFE1E4);
      quiet.setPadding(dp(10), dp(7), dp(10), dp(7));
      touch(
          quiet,
          () -> {
            getSystemService(NotificationManager.class)
                .cancel(100 + Math.floorMod(e.id().hashCode(), 1000000));
            Toast.makeText(this, "Đã tắt thông báo; vẫn giữ trạng thái lỗi", Toast.LENGTH_SHORT)
                .show();
          });
      controls.addView(quiet);
      faultCard.addView(controls);
    }
    content.addView(faultCard);
    TextView allFaults = text("Xem tất cả sự cố  →", 11, BLUE, true);
    allFaults.setPadding(dp(2), dp(2), 0, dp(10));
    touch(allFaults, () -> navigate("incidents"));
    content.addView(allFaults);
    if (!demo && store.prefs.getString("url", "").isEmpty()) {
      content.addView(action("Kết nối tài khoản", "link", true, () -> navigate("settings")));
      gap(content, 16);
    }
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
    disc.setBackground(shape(0xFFEDF3FA, 13, 0));
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
    words.addView(text(description, 11, MUTED, false));
    r.addView(words, new LinearLayout.LayoutParams(0, -2, 1));
    target.addView(r);
  }

  private void incidents() {
    LinearLayout controls = card(Color.WHITE);
    LinearLayout date = row();
    date.addView(icon("calendar", BLUE, 17));
    TextView day = text(selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 13, INK, true);
    day.setPadding(dp(8), 0, 0, 0);
    date.addView(day, new LinearLayout.LayoutParams(0, -2, 1));
    TextView fetch = pill("Tải dữ liệu", BLUE, 0xFFEDF4FF);
    touch(fetch, this::loadHistory);
    date.addView(fetch);
    touch(
        day,
        () ->
            new DatePickerDialog(
                    this,
                    (v, y, m, d) -> {
                      selected = LocalDate.of(y, m + 1, d);
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
      pick.setBackground(shape(incidentFilter.equals(name) ? 0xFFE8F1FF : Color.TRANSPARENT, 9, 0));
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
            : demo ? List.of() : store.events(from, Math.max(to, System.currentTimeMillis() + 1));
    int shown = 0;
    long baseline = demo ? 0 : store.prefs.getLong("baselineFrom", 0);
    for (Rules.Incident incident : Rules.incidentHistory(records)) {
      if (incident.fault().at() < from
          || incident.fault().at() >= to
          || !inScope(incident.fault().crossing())) continue;
      boolean recovered = incident.recovery() != null,
          reset = !recovered && incident.fault().at() < baseline;
      if (incidentFilter.equals("Đã khôi phục") && !recovered) continue;
      if (incidentFilter.equals("Chưa khôi phục") && (recovered || reset)) continue;
      LinearLayout item = card(recovered ? 0xFFF0FAF5 : reset ? Color.WHITE : 0xFFFFF0F1);
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
      LinearLayout emptyCard = card(Color.WHITE);
      empty(emptyCard, "Chưa có sự cố phù hợp", "Chọn ngày hoặc bộ lọc khác.", "shield");
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
    io.execute(
        () -> {
          try {
            WebSource source =
                new WebSource(
                    store.prefs.getString("url", ""),
                    store.prefs.getString("user", ""),
                    store.password());
            List<Rules.Event> events = source.readDay(day);
            store.saveForAccount(account, events);
            runOnUiThread(
                () -> {
                  if (isFinishing()) return;
                  render();
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
    f.setHintTextColor(0xFFABB7C7);
    f.setInputType(type);
    f.setHint(hint);
    f.setText(value);
    f.setPadding(dp(14), dp(13), dp(14), dp(13));
    f.setBackground(shape(0xFFF7F9FC, 12, LINE));
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
            new int[] {BLUE, 0xFFB5C2D3}));
    sw.setTrackTintList(
        new ColorStateList(
            new int[][] {new int[] {android.R.attr.state_checked}, new int[] {}},
            new int[] {0xFFBCD4F5, 0xFFE1E7EE}));
    sw.setContentDescription(title);
    row.addView(sw);
    parent.addView(row);
    return sw;
  }

  private void settings() {
    String[] range = {rangeFrom(), rangeTo()};
    LinearLayout scope = card(Color.WHITE);
    scope.addView(text("Phạm vi theo dõi", 14, INK, true));
    gap(scope, 10);
    String[] rangeLabels = {"Từ đường ngang", "Đến đường ngang"};
    for (int i = 0; i < 2; i++) {
      final int index = i;
      LinearLayout entry = row();
      entry.addView(
          text(rangeLabels[i], 11, MUTED, false), new LinearLayout.LayoutParams(0, -2, 1));
      TextView choice = pill("Km " + range[i] + "  ▾", BLUE, 0xFFEDF4FF);
      touch(
          choice,
          () -> {
            String[] names = new String[Rules.CROSSINGS.size()];
            for (int j = 0; j < names.length; j++) names[j] = "Km " + Rules.CROSSINGS.get(j);
            new AlertDialog.Builder(this)
                .setTitle(rangeLabels[index])
                .setSingleChoiceItems(
                    names,
                    Rules.CROSSINGS.indexOf(range[index]),
                    (dialog, which) -> {
                      range[index] = Rules.CROSSINGS.get(which);
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
    LinearLayout account = card(Color.WHITE);
    account.addView(text("Kết nối tài khoản", 16, INK, true));
    gap(account, 6);
    account.addView(text("Sử dụng tài khoản website của đơn vị", 11, MUTED, false));
    gap(account, 21);
    EditText url =
        field(
            account,
            "ĐỊA CHỈ WEBSITE",
            "http://…",
            store.prefs.getString("url", ""),
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
    LinearLayout options = card(Color.WHITE);
    options.addView(text("Theo dõi & thông báo", 16, INK, true));
    gap(options, 18);
    options.addView(text("CHU KỲ ĐỌC DỮ LIỆU", 10, MUTED, true));
    gap(options, 10);
    int[] selectedInterval = {store.prefs.getInt("interval", 30)};
    LinearLayout segments = row();
    segments.setPadding(dp(4), dp(4), dp(4), dp(4));
    segments.setBackground(shape(0xFFF0F4FA, 12, 0));
    int[] intervals = {30, 120, 300};
    String[] intervalNames = {"30 giây", "2 phút", "5 phút"};
    for (int i = 0; i < 3; i++) {
      int value = intervals[i];
      TextView option =
          text(intervalNames[i], 12, selectedInterval[0] == value ? BLUE : MUTED, true);
      option.setGravity(Gravity.CENTER);
      option.setPadding(0, dp(11), 0, dp(11));
      if (selectedInterval[0] == value) option.setBackground(shape(Color.WHITE, 9, 0));
      segments.addView(option, new LinearLayout.LayoutParams(0, -2, 1));
      touch(
          option,
          () -> {
            selectedInterval[0] = value;
            for (int j = 0; j < 3; j++) {
              TextView v = (TextView) segments.getChildAt(j);
              v.setTextColor(intervals[j] == value ? BLUE : MUTED);
              v.setBackground(shape(intervals[j] == value ? Color.WHITE : Color.TRANSPARENT, 9, 0));
            }
          });
    }
    options.addView(segments);
    divider(options);
    Switch sound =
        toggle(
            "Chuông sự cố",
            "Tắt để chỉ hiển thị cảnh báo",
            store.prefs.getBoolean("sound", true),
            options);
    sound.setOnCheckedChangeListener((b, on) -> store.prefs.edit().putBoolean("sound", on).apply());
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
                if (!applyScope(range[0], range[1])) return;
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
                startForegroundService(new Intent(this, MonitorService.class));
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
    gap(content, 20);
    LinearLayout preview = card(Color.WHITE);
    Switch sample =
        toggle("Xem giao diện minh họa", "Không sử dụng dữ liệu từ website", demo, preview);
    sample.setOnCheckedChangeListener(
        (b, on) -> {
          demo = on;
          store.prefs.edit().putBoolean("demoUi", on).apply();
          if (on) stopService(new Intent(this, MonitorService.class));
          navigate("overview");
        });
    content.addView(preview);
    TextView note =
        text(
            "Bản thử giao diện 0.2 · Chưa dùng trực vận hành.\n"
                + "Tự kích và vị trí tàu thật chưa bật. Theo dõi nền sử dụng thông báo thường trực."
                + " Website HTTP không mã hóa đường truyền.",
            10,
            MUTED,
            false);
    note.setPadding(dp(3), 0, dp(3), dp(18));
    content.addView(note);
  }

  private void openCrossing(String km) {
    selectedCrossing = Rules.km(km);
    navigate("detail");
  }

  private List<Rules.Event> crossingEvents(String km) {
    List<Rules.Event> list = new ArrayList<>();
    for (Rules.Event e : today()) if (Rules.km(e.crossing()).equals(Rules.km(km))) list.add(e);
    list.sort(Comparator.comparingLong(Rules.Event::at).reversed());
    return list;
  }

  private void crossingList() {
    LinearLayout introduction = row();
    int total = 0;
    for (String point : Rules.CROSSINGS) if (inScope(point)) total++;
    introduction.addView(
        text("KHU VỰC THEO DÕI", 10, MUTED, true), new LinearLayout.LayoutParams(0, -2, 1));
    introduction.addView(pill(total + " đường ngang", BLUE, 0xFFE8F0FC));
    content.addView(introduction);
    gap(content, 14);
    LinearLayout list = card(Color.WHITE);
    int index = 0;
    for (String point : Rules.ROUTE) {
      if (point.contains("(") || !inScope(point)) continue;
      if (index++ > 0) divider(list);
      List<Rules.Event> events = crossingEvents(point);
      boolean fault = !currentFaults(events).isEmpty();
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

  private void deviceRow(
      LinearLayout parent, String title, String value, String kind, boolean error) {
    LinearLayout r = new LinearLayout(this);
    r.setGravity(Gravity.TOP);
    r.addView(icon(kind, error ? RED : BLUE, 17));
    TextView name = text(title, 11, MUTED, false);
    name.setPadding(dp(9), 0, dp(8), 0);
    r.addView(name, new LinearLayout.LayoutParams(dp(108), -2));
    TextView status =
        text(value, 11, error ? RED : value.equals("Chưa có dữ liệu") ? MUTED : INK, true);
    r.addView(status, new LinearLayout.LayoutParams(0, -2, 1));
    parent.addView(r);
  }

  private void crossingDetail() {
    List<Rules.Event> events = crossingEvents(selectedCrossing);
    List<Rules.Event> faults = currentFaults(events);
    LinearLayout summary = card(Color.WHITE);
    LinearLayout top = row();
    top.addView(
        text("Km " + selectedCrossing, 22, INK, true), new LinearLayout.LayoutParams(0, -2, 1));
    top.addView(
        pill(
            !demo && store.prefs.getLong("baselineFrom", 0) > 0 && faults.isEmpty()
                ? "✓ Đã đặt lại"
                : "Nhật ký",
            !demo && store.prefs.getLong("baselineFrom", 0) > 0 && faults.isEmpty() ? GREEN : BLUE,
            0xFFEDF4FF));
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
    String bell = latestDevice(events, "chuông"), light = latestDevice(events, "đèn");
    String barrier1 = latestDevice(events, "cần chắn 1"),
        barrier2 = latestDevice(events, "cần chắn 2");
    String barriers =
        barrier1.equals("Chưa có dữ liệu") && barrier2.equals("Chưa có dữ liệu")
            ? "Chưa có dữ liệu"
            : barrier1 + "\n" + barrier2;
    String sensors = "Chưa có dữ liệu";
    for (Rules.Event e : events) {
      String value = e.text().toLowerCase(Locale.ROOT);
      if (value.contains("cảm biến") && !value.contains("điện áp")) {
        sensors = e.text() + " · " + time(e.at());
        break;
      }
    }
    String connection = latestDevice(events, "kết nối giám sát"),
        voltage = latestDevice(events, "điện áp", "220vac");
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
    LinearLayout history = card(Color.WHITE);
    history.addView(text("Lịch sử sự kiện gần nhất", 15, INK, true));
    gap(history, 18);
    if (events.isEmpty())
      empty(history, "Chưa có sự kiện", "Dữ liệu sẽ xuất hiện sau khi kết nối.", "history");
    for (int i = 0; i < Math.min(12, events.size()); i++) {
      if (i > 0) divider(history);
      Rules.Event e = events.get(i);
      eventRow(history, e, e.red());
      String note = Rules.axleNote(e.counts());
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
    nav.setBackground(shape(Color.WHITE, 0, LINE));
    String[] pages = {"overview", "crossings", "incidents", "settings"},
        names = {"Tổng quan", "Đường ngang", "Sự cố", "Cài đặt"},
        icons = {"overview", "rail", "alert", "settings"};
    for (int i = 0; i < pages.length; i++) {
      String target = pages[i];
      boolean active = page.equals(target) || (page.equals("detail") && target.equals("crossings"));
      LinearLayout tab = column();
      tab.setGravity(Gravity.CENTER);
      tab.setPadding(0, dp(5), 0, dp(4));
      tab.addView(icon(icons[i], active ? BLUE : MUTED, 21));
      gap(tab, 5);
      TextView tabLabel = text(names[i], 10, active ? BLUE : MUTED, active);
      tabLabel.setGravity(Gravity.CENTER);
      tab.addView(tabLabel);
      if (active) tab.setBackground(shape(0xFFEDF4FF, 13, 0));
      nav.addView(tab, new LinearLayout.LayoutParams(0, dp(48), 1));
      touch(tab, () -> navigate(target));
    }
    screen.addView(nav);
  }

  private class RouteView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<String> points = Rules.routeSpan(rangeFrom(), rangeTo());

    RouteView() {
      super(MainActivity.this);
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
      if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
        if (points.isEmpty()) return true;
        float step = points.size() > 1 ? (getWidth() - dp(34)) / (float) (points.size() - 1) : 1;
        int index =
            points.size() == 1
                ? 0
                : Math.max(
                    0, Math.min(points.size() - 1, Math.round((event.getX() - dp(7)) / step)));
        String point = points.get(index);
        if (point.contains("(")) {
          Toast.makeText(
                  MainActivity.this,
                  point.contains("Vi") ? "Ga Vinh" : "Ga Yên Xuân",
                  Toast.LENGTH_SHORT)
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
      if (points.isEmpty()) return;
      float left = points.size() == 1 ? getWidth() / 2f : dp(7),
          right = points.size() == 1 ? left : getWidth() - dp(27),
          step = points.size() > 1 ? (right - left) / (points.size() - 1) : 0,
          y = dp(25);
      int trainIndex = -1, trainDirection = 1;
      if (demo)
        for (Rules.Event e : today())
          if (Rules.direction(e.text()) != 0) {
            trainIndex = points.indexOf(Rules.km(e.crossing()));
            trainDirection = Rules.direction(e.text());
            break;
          }
      int greenStart = Math.max(0, trainIndex - 1),
          greenEnd = Math.min(points.size() - 1, trainIndex + 1);
      p.setStrokeCap(Paint.Cap.ROUND);
      p.setStrokeWidth(dp(3));
      p.setColor(0xFFD7E2EF);
      c.drawLine(left, y, right, y, p);
      if (demo && trainIndex >= 0) {
        p.setStrokeWidth(dp(5));
        p.setColor(GREEN);
        c.drawLine(left + step * greenStart, y, left + step * greenEnd, y, p);
        p.setStrokeWidth(dp(1.7f));
        float ax = left + step * trainIndex;
        c.save();
        c.translate(ax, y - dp(13));
        c.scale(trainDirection, 1);
        c.translate(-ax, -y + dp(13));
        c.drawLine(ax - dp(6), y - dp(13), ax + dp(5), y - dp(13), p);
        c.drawLine(ax + dp(1), y - dp(17), ax + dp(5), y - dp(13), p);
        c.drawLine(ax + dp(1), y - dp(9), ax + dp(5), y - dp(13), p);
        c.restore();
      }
      for (int i = 0; i < points.size(); i++) {
        float x = left + step * i;
        String km = points.get(i);
        boolean station = km.contains("(");
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);
        c.drawCircle(x, y, dp(5.5f), p);
        p.setColor(
            station
                ? BLUE
                : demo && trainIndex >= 0 && i >= greenStart && i <= greenEnd ? GREEN : 0xFF93AAC4);
        if (station) c.drawRoundRect(x - dp(4), y - dp(4), x + dp(4), y + dp(4), dp(2), dp(2), p);
        else c.drawCircle(x, y, dp(3.5f), p);
        p.setTypeface(
            Typeface.create(station ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        p.setTextSize(dp(station ? 9 : 8.5f));
        p.setColor(station ? BLUE : MUTED);
        if (station) {
          String name = km.contains("Vi") ? "Vinh" : "Yên Xuân";
          c.drawText(name, x - p.measureText(name) / 2, y - dp(15), p);
        } else {
          c.save();
          c.translate(x - dp(2), y + dp(14));
          c.rotate(57);
          c.drawText(km, 0, 0, p);
          c.restore();
        }
      }
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
