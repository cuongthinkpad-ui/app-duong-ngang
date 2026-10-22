package vn.duongngang.monitor;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
  private final int blue = Color.rgb(40, 108, 190),
      ink = Color.rgb(35, 56, 82),
      muted = Color.rgb(112, 130, 151),
      red = Color.rgb(193, 54, 62);
  private Store store;
  private LinearLayout screen, body;
  private TextView status;
  private boolean demo = false, expanded = false;
  private LocalDate selected = LocalDate.now(WebSource.ZONE);
  private String filter = "Tất cả";
  private ExecutorService io = Executors.newSingleThreadExecutor();
  private final Handler refreshHandler = new Handler(Looper.getMainLooper());
  private String lastUiState = "";
  private final Runnable refresh =
      new Runnable() {
        public void run() {
          String state =
              store.prefs.getString("status", "") + store.prefs.getLong("lastSuccess", 0);
          if (!state.equals(lastUiState)) {
            lastUiState = state;
            render();
          }
          refreshHandler.postDelayed(this, 2000);
        }
      };

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    store = new Store(this);
    if (Build.VERSION.SDK_INT >= 33)
      requestPermissions(new String[] {"android.permission.POST_NOTIFICATIONS"}, 10);
    render();
  }

  @Override
  protected void onStart() {
    super.onStart();
    refreshHandler.post(refresh);
  }

  @Override
  protected void onStop() {
    refreshHandler.removeCallbacks(refresh);
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    io.shutdownNow();
    super.onDestroy();
  }

  private int dp(float n) {
    return (int) (n * getResources().getDisplayMetrics().density);
  }

  private TextView label(String s, int size, int color) {
    TextView t = new TextView(this);
    t.setText(s);
    t.setTextSize(size);
    t.setTextColor(color);
    t.setPadding(0, dp(4), 0, dp(4));
    return t;
  }

  private LinearLayout column() {
    var v = new LinearLayout(this);
    v.setOrientation(LinearLayout.VERTICAL);
    return v;
  }

  private android.graphics.drawable.GradientDrawable background(int color) {
    var b = new android.graphics.drawable.GradientDrawable();
    b.setColor(color);
    b.setCornerRadius(dp(16));
    return b;
  }

  private LinearLayout card(String title, int color) {
    var c = column();
    c.setPadding(dp(16), dp(12), dp(16), dp(12));
    c.setBackground(background(color));
    var p = new LinearLayout.LayoutParams(-1, -2);
    p.bottomMargin = dp(12);
    c.setLayoutParams(p);
    var t = label(title, 12, muted);
    t.setTypeface(null, Typeface.BOLD);
    c.addView(t);
    return c;
  }

  private Button button(String text, Runnable action) {
    Button b = new Button(this);
    b.setText(text);
    b.setTextSize(12);
    b.setTextColor(blue);
    b.setAllCaps(false);
    b.setOnClickListener(v -> action.run());
    return b;
  }

  private String time(long at) {
    return DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(WebSource.ZONE)
        .format(Instant.ofEpochMilli(at));
  }

  private List<Rules.Event> demoEvents() {
    long n = System.currentTimeMillis();
    return List.of(
        new Rules.Event("demo1", n - 60000, "Km 321+780", "Tàu lẻ", false, List.of()),
        new Rules.Event(
            "demo2", n - 120000, "Km 328+325", "Điện áp cảm biến 6 < 5V", true, List.of()),
        new Rules.Event(
            "demo3",
            n - 180000,
            "Km 313+800",
            "Khôi phục hệ thống",
            false,
            List.of("46", "46", "48", "44", "46", "46")));
  }

  private List<Rules.Event> today() {
    return demo
        ? demoEvents()
        : store.events(
            LocalDate.now(WebSource.ZONE).atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli(),
            System.currentTimeMillis() + 1);
  }

  private void render() {
    screen = column();
    screen.setPadding(dp(16), dp(10), dp(16), dp(4));
    screen.setBackgroundColor(Color.rgb(242, 246, 252));
    setContentView(screen);
    var header = new LinearLayout(this);
    header.setGravity(Gravity.CENTER_VERTICAL);
    var title = label("Đường Ngang", 23, blue);
    title.setTypeface(null, Typeface.BOLD);
    header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
    header.addView(button("Cài đặt", this::settings));
    screen.addView(header);
    status =
        label(
            demo
                ? "DỮ LIỆU MINH HỌA · Không theo dõi thực tế"
                : store.prefs.getString("status", "Chưa kết nối · Nhập tài khoản trong Cài đặt"),
            11,
            muted);
    screen.addView(status);
    long last = store.prefs.getLong("lastSuccess", 0);
    if (!demo && last > 0) screen.addView(label("Lần đọc thành công: " + time(last), 10, muted));
    var map = card("SƠ ĐỒ TUYẾN  ·  BẮC → NAM", Color.WHITE);
    var horizontal = new HorizontalScrollView(this);
    horizontal.setHorizontalScrollBarEnabled(true);
    horizontal.addView(
        new RouteView(today()), new android.widget.FrameLayout.LayoutParams(dp(1040), dp(104)));
    map.addView(horizontal);
    map.addView(label("Xanh lá: đoạn ghi nhận tàu · → Lẻ / ← Chẵn", 10, muted));
    screen.addView(map);
    var scroller = new ScrollView(this);
    body = column();
    scroller.addView(body);
    screen.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1));
    var train = card("TÀU GẦN NHẤT", Color.WHITE);
    var trains = new ArrayList<Rules.Event>();
    for (var e : today()) if (Rules.direction(e.text()) != 0) trains.add(e);
    train.addView(
        label(trains.isEmpty() ? "Chưa có dữ liệu tàu" : line(trains.get(0)) + "  ▾", 14, ink));
    train.setOnClickListener(
        v -> {
          expanded = !expanded;
          render();
        });
    if (expanded) for (var e : trains) train.addView(label(line(e), 12, ink));
    body.addView(train);
    var faults = card("SỰ CỐ", Color.rgb(255, 239, 240));
    var active = Rules.activeFaults(today());
    if (active.isEmpty())
      faults.addView(
          label(
              last == 0 && !demo
                  ? "Chưa đủ dữ liệu để xác định sự cố"
                  : "Không có sự cố trong dữ liệu đã đọc",
              13,
              muted));
    for (var e : active) {
      faults.addView(label(line(e), 14, red));
      faults.addView(
          button(
              "Tắt chuông",
              () -> {
                getSystemService(NotificationManager.class)
                    .cancel(100 + Math.floorMod(e.id().hashCode(), 1000000));
                Toast.makeText(
                        this, "Đã tắt thông báo; trạng thái lỗi được giữ lại", Toast.LENGTH_SHORT)
                    .show();
              }));
    }
    body.addView(faults);
    var history = card("LỊCH SỬ THEO NGÀY", Color.WHITE);
    history.addView(
        button(
            selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "  ·  Chọn ngày",
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
                    .show()));
    var selector = new Spinner(this);
    var names = new ArrayList<String>();
    names.add("Tất cả");
    for (String km : Rules.ROUTE) names.add("Km " + km);
    var adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
    selector.setAdapter(adapter);
    selector.setSelection(Math.max(0, names.indexOf(filter)));
    selector.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          public void onNothingSelected(AdapterView<?> p) {}

          public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
            String next = names.get(pos);
            if (!next.equals(filter)) {
              filter = next;
              render();
            }
          }
        });
    history.addView(selector);
    history.addView(button("Tải ngày này từ web", this::loadHistory));
    long from = selected.atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli(),
        to = selected.plusDays(1).atStartOfDay(WebSource.ZONE).toInstant().toEpochMilli();
    var records = demo ? demoEvents() : store.events(from, to);
    int shown = 0;
    for (var e : records) {
      if (!filter.equals("Tất cả") && !e.crossing().equals(filter)) continue;
      history.addView(label(line(e), 12, e.red() ? red : ink));
      String note = Rules.axleNote(e.counts());
      if (!note.isEmpty()) history.addView(label(note, 11, muted));
      shown++;
    }
    if (shown == 0) history.addView(label("Chưa có dữ liệu đã tải cho ngày này", 12, muted));
    body.addView(history);
    var credit = label("Designed by Cao Xuân Cường", 7, Color.rgb(151, 164, 181));
    credit.setGravity(Gravity.END);
    screen.addView(credit);
  }

  private String line(Rules.Event e) {
    return time(e.at()) + " · " + e.crossing() + " · " + e.text();
  }

  private void loadHistory() {
    if (demo) {
      Toast.makeText(this, "Đang ở chế độ minh họa", Toast.LENGTH_SHORT).show();
      return;
    }
    LocalDate day = selected;
    String account = store.account();
    status.setText("Đang tải đủ các trang…");
    io.execute(
        () -> {
          try {
            var source =
                new WebSource(
                    store.prefs.getString("url", ""),
                    store.prefs.getString("user", ""),
                    store.password());
            var events = source.readDay(day);
            store.saveForAccount(account, events);
            runOnUiThread(
                () -> {
                  render();
                  Toast.makeText(this, "Đã tải " + events.size() + " bản ghi", Toast.LENGTH_SHORT)
                      .show();
                });
          } catch (Exception e) {
            runOnUiThread(
                () ->
                    new AlertDialog.Builder(this)
                        .setTitle("Chưa tải được lịch sử")
                        .setMessage(e.getMessage())
                        .setPositiveButton("Đóng", null)
                        .show());
          }
        });
  }

  private void settings() {
    var content = column();
    content.setPadding(dp(20), dp(8), dp(20), dp(8));
    var url = new EditText(this);
    url.setHint("Địa chỉ web");
    url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    url.setText(store.prefs.getString("url", ""));
    content.addView(url);
    var user = new EditText(this);
    user.setHint("ID đăng nhập");
    user.setText(store.prefs.getString("user", ""));
    content.addView(user);
    var pass = new EditText(this);
    pass.setHint("Mật khẩu (để trống để giữ mật khẩu đã lưu)");
    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    content.addView(pass);
    var show = new CheckBox(this);
    show.setText("Hiện mật khẩu");
    show.setOnCheckedChangeListener(
        (b, on) ->
            pass.setInputType(
                InputType.TYPE_CLASS_TEXT
                    | (on
                        ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : InputType.TYPE_TEXT_VARIATION_PASSWORD)));
    content.addView(show);
    var interval = new Spinner(this);
    interval.setAdapter(
        new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[] {"Quét mỗi 30 giây", "Quét mỗi 2 phút", "Quét mỗi 5 phút"}));
    int old = store.prefs.getInt("interval", 30);
    interval.setSelection(old == 30 ? 0 : old == 120 ? 1 : 2);
    content.addView(interval);
    var sound = new Switch(this);
    sound.setText("Chuông sự cố");
    sound.setChecked(store.prefs.getBoolean("sound", true));
    content.addView(sound);
    var sample = new Switch(this);
    sample.setText("Xem giao diện bằng dữ liệu minh họa");
    sample.setChecked(demo);
    sample.setOnCheckedChangeListener(
        (b, on) -> {
          demo = on;
          if (on) stopService(new Intent(this, MonitorService.class));
          render();
        });
    content.addView(sample);
    content.addView(
        label(
            "Theo dõi nền dùng thông báo thường trực và có thể tăng tiêu thụ pin. Web HTTP truyền"
                + " thông tin không mã hóa. Tự kích và sơ đồ suy luận chưa được xác minh với web"
                + " thật.",
            11,
            muted));
    content.addView(
        button(
            "Dừng theo dõi",
            () -> {
              stopService(new Intent(this, MonitorService.class));
              store.prefs.edit().putString("status", "Đã dừng theo dõi").apply();
              render();
            }));
    var dialog =
        new AlertDialog.Builder(this)
            .setTitle("Kết nối & theo dõi")
            .setView(content)
            .setNegativeButton("Đóng", null)
            .setPositiveButton("Lưu & theo dõi", null)
            .create();
    dialog.setOnShowListener(
        v ->
            dialog
                .getButton(-1)
                .setOnClickListener(
                    w -> {
                      try {
                        String endpoint = url.getText().toString().trim();
                        var uri = java.net.URI.create(endpoint);
                        if (!List.of("http", "https").contains(uri.getScheme())
                            || uri.getHost() == null
                            || uri.getUserInfo() != null)
                          throw new Exception("Nhập địa chỉ http:// hoặc https:// hợp lệ");
                        if (user.getText().toString().isBlank())
                          throw new Exception("Cần nhập ID đăng nhập");
                        String secret = pass.getText().toString();
                        boolean accountChanged =
                            !endpoint.equals(store.prefs.getString("url", ""))
                                || !user.getText()
                                    .toString()
                                    .equals(store.prefs.getString("user", ""));
                        if (secret.isEmpty() && !accountChanged) secret = store.password();
                        if (secret.isEmpty())
                          throw new Exception("Cần nhập mật khẩu cho tài khoản này");
                        stopService(new Intent(this, MonitorService.class));
                        store.credentials(endpoint, user.getText().toString(), secret);
                        store
                            .prefs
                            .edit()
                            .putInt(
                                "interval",
                                new int[] {30, 120, 300}[interval.getSelectedItemPosition()])
                            .putBoolean("sound", sound.isChecked())
                            .apply();
                        demo = false;
                        startForegroundService(new Intent(this, MonitorService.class));
                        dialog.dismiss();
                        render();
                      } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                      }
                    }));
    dialog.show();
  }

  private class RouteView extends View {
    private final java.util.List<Rules.Event> events;

    RouteView(java.util.List<Rules.Event> e) {
      super(MainActivity.this);
      events = e;
    }

    protected void onDraw(Canvas c) {
      super.onDraw(c);
      Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
      p.setStrokeWidth(dp(3));
      p.setColor(Color.rgb(198, 213, 231));
      float y = dp(40), step = dp(76), start = dp(20);
      c.drawLine(start, y, start + step * 12, y, p);
      // Only demonstration movement is drawn until real train correlation has been validated.
      if (demo) {
        p.setColor(Color.rgb(39, 166, 106));
        c.drawLine(start + step * 7, y, start + step * 8, y, p);
        p.setTextSize(dp(24));
        c.drawText("→", start + step * 7 + dp(24), y - dp(8), p);
      }
      for (int i = 0; i < Rules.ROUTE.size(); i++) {
        String km = Rules.ROUTE.get(i);
        boolean station = km.contains("(");
        float x = start + step * i;
        p.setColor(station ? blue : muted);
        c.drawCircle(x, y, dp(station ? 5 : 3), p);
        p.setTextSize(dp(10));
        String name = km.contains("Vi") ? "Ga Vinh" : km.contains("YX") ? "Ga Yên Xuân" : km;
        c.drawText(name, x - dp(16), y + dp(i % 2 == 0 ? 22 : 40), p);
      }
    }
  }
}
