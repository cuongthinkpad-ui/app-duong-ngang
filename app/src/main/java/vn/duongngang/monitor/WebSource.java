package vn.duongngang.monitor;

import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

/** Authenticated ASP.NET reader. Report reads are complete before the caller can save them. */
public final class WebSource {
  private final String base, user, password;
  private final Map<String, String> cookies = new HashMap<>();
  public static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  public WebSource(String base, String user, String password) {
    this.base = base;
    this.user = user;
    this.password = password;
  }

  private Connection connection(String url) throws Exception {
    URI b = URI.create(base), u = URI.create(url);
    if (!Objects.equals(b.getHost(), u.getHost())
        || b.getPort() != u.getPort()
        || !b.getScheme().equals(u.getScheme()))
      throw new Exception("Trang chuyển sang địa chỉ khác; cần kiểm tra cấu hình.");
    return Jsoup.connect(url)
        .timeout(25000)
        .maxBodySize(8 * 1024 * 1024)
        .followRedirects(false)
        .cookies(cookies)
        .userAgent("DuongNgang/0.6 Android");
  }

  private Document execute(Connection c) throws Exception {
    if (Thread.currentThread().isInterrupted())
      throw new java.io.InterruptedIOException("Đã dừng đọc web");
    var r = c.execute();
    cookies.putAll(r.cookies());
    for (int i = 0; r.statusCode() >= 300 && r.statusCode() < 400 && i < 5; i++) {
      String location = r.header("Location");
      if (location == null) throw new Exception("Chuyển trang không hợp lệ");
      r = connection(URI.create(r.url().toString()).resolve(location).toString()).execute();
      cookies.putAll(r.cookies());
    }
    if (r.statusCode() != 200)
      throw new Exception("Không đọc được web (HTTP " + r.statusCode() + ")");
    return r.parse();
  }

  private static String optionValue(Element option) {
    return option.hasAttr("value") ? option.attr("value") : option.text();
  }

  private static String selectedValue(Element select) {
    Element option = select.selectFirst("option[selected]");
    if (option == null) option = select.selectFirst("option");
    return option == null ? "" : optionValue(option);
  }

  private Map<String, String> fields(Element form) throws Exception {
    if (form == null) throw new Exception("Không tìm thấy biểu mẫu web");
    Map<String, String> data = new LinkedHashMap<>();
    for (Element e : form.select("input[name], select[name], textarea[name]")) {
      String type = e.attr("type");
      if (List.of("submit", "button", "image").contains(type)) continue;
      if (List.of("checkbox", "radio").contains(type) && !e.hasAttr("checked")) continue;
      data.put(e.attr("name"), e.tagName().equals("select") ? selectedValue(e) : e.val());
    }
    for (Element radio : form.select("input[type=radio][name]"))
      if (radio.val().trim().equalsIgnoreCase("Tất cả")) data.put(radio.attr("name"), radio.val());
    // Fetch all managed points; the app's range filter must not hide upstream evidence.
    for (Element select : form.select("select[name]")) {
      boolean pointList = false;
      Element all = null;
      for (Element option : select.select("option")) {
        if (Rules.isPoint(option.text())) pointList = true;
        if (option.text().toLowerCase(Locale.ROOT).contains("tất cả")) all = option;
      }
      if (pointList) {
        if (all == null) throw new Exception("Chưa chọn được toàn bộ tuyến trên web");
        data.put(select.attr("name"), optionValue(all));
      }
    }
    return data;
  }

  private Document submit(Document d, Map<String, String> data) throws Exception {
    Element form = d.selectFirst("form");
    if (form == null) throw new Exception("Không tìm thấy biểu mẫu web");
    String action = form.absUrl("action");
    if (action.isBlank()) action = d.location();
    return execute(connection(action).method(Connection.Method.POST).data(data));
  }

  public record DayData(List<Rules.Event> events, Rules.Route route, boolean connected) {
    public DayData(List<Rules.Event> events, Rules.Route route) {
      this(events, route, true);
    }

    public DayData {
      events = List.copyOf(events);
      if (route.crossings().isEmpty())
        throw new IllegalArgumentException("Danh sách tuyến chưa có đường ngang");
      for (Rules.Event e : events)
        if (!route.points().contains(Rules.km(e.crossing())))
          throw new IllegalArgumentException("Bản ghi nằm ngoài danh sách tuyến; cần kiểm tra web");
    }
  }

  private static Element loginButton(Element form, Element pass) throws Exception {
    var buttons = form.select("input[type=submit][name],button[type=submit][name]");
    for (Element button : buttons) {
      String label = (button.val() + " " + button.text()).toLowerCase(Locale.ROOT);
      if (label.contains("đăng nhập") || label.contains("log in") || label.contains("login"))
        return button;
    }
    String name = pass.attr("name");
    int separator = name.lastIndexOf('$');
    if (separator >= 0) {
      String prefix = name.substring(0, separator + 1);
      List<Element> nearby = new ArrayList<>();
      for (Element button : buttons) if (button.attr("name").startsWith(prefix)) nearby.add(button);
      if (nearby.size() == 1) return nearby.get(0);
    }
    if (buttons.size() == 1) return buttons.get(0);
    throw new Exception("Chưa nhận diện đúng nút đăng nhập; không gửi nhầm nút khác");
  }

  public static final class AuthenticationException extends Exception {
    AuthenticationException(String message) {
      super(message);
    }
  }

  private Document login() throws Exception {
    Document d = execute(connection(base));
    Element pass = d.selectFirst("input[type=password]");
    if (pass == null) return d;
    Element form = pass.closest("form");
    var data = fields(form);
    Element login =
        form.selectFirst("input[type=text][name],input[type=email][name],input:not([type])[name]");
    if (login == null) throw new Exception("Chưa nhận diện được ô tài khoản");
    Element button = loginButton(form, pass);
    data.put(login.attr("name"), user);
    data.put(pass.attr("name"), password);
    data.put(button.attr("name"), button.val());
    d = submit(d, data);
    if (d.selectFirst("input[type=password]") != null)
      throw new AuthenticationException(
          "Chưa đăng nhập được; kiểm tra tài khoản và mật khẩu rồi bật theo dõi lại");
    return d;
  }

  private static Element dateInput(Document d) {
    for (Element e : d.select("input[name]"))
      if (e.val().matches("\\d{2}/\\d{2}/\\d{4}") || e.attr("type").equals("date")) return e;
    return null;
  }

  private static String dayValue(Element date, LocalDate day) {
    return day.format(
        DateTimeFormatter.ofPattern(
            date.attr("type").equals("date") ? "yyyy-MM-dd" : "dd/MM/yyyy"));
  }

  private static boolean allFiltersSelected(Document d) {
    for (Element select : d.select("select[name]")) {
      boolean points = false;
      String all = null;
      for (Element option : select.select("option")) {
        if (Rules.isPoint(option.text())) points = true;
        if (option.text().toLowerCase(Locale.ROOT).contains("tất cả")) all = optionValue(option);
      }
      if (points && (all == null || !selectedValue(select).equals(all))) return false;
    }
    Set<String> groups = new HashSet<>();
    Map<String, String> checked = new HashMap<>();
    for (Element radio : d.select("input[type=radio][name]")) {
      if (radio.val().trim().equalsIgnoreCase("Tất cả")) groups.add(radio.attr("name"));
      if (radio.hasAttr("checked")) checked.put(radio.attr("name"), radio.val());
    }
    for (String group : groups)
      if (!"Tất cả".equalsIgnoreCase(checked.getOrDefault(group, "").trim())) return false;
    return true;
  }

  private Document selectDay(Document d, LocalDate day) throws Exception {
    Element date = dateInput(d);
    if (date == null) throw new Exception("Chưa nhận diện bộ chọn ngày");
    var data = fields(date.closest("form"));
    if (date.val().equals(dayValue(date, day)) && allFiltersSelected(d)) return d;
    data.put(date.attr("name"), dayValue(date, day));
    data.put("__EVENTTARGET", date.attr("name"));
    data.put("__EVENTARGUMENT", "");
    return submit(d, data);
  }

  private static final Pattern POSTBACK =
      Pattern.compile("__doPostBack\\(['\"]([^'\"]*)['\"],\\s*['\"]([^'\"]*)['\"]\\)");

  private static Element pageLink(Document d, int number) {
    for (Element a : d.select("a[href]")) {
      var post = POSTBACK.matcher(a.attr("href"));
      if (post.find() && post.group(2).equalsIgnoreCase("Page$" + number)) return a;
      if (!a.attr("href").startsWith("javascript:") && a.text().trim().equals("" + number))
        return a;
    }
    return null;
  }

  private Document followPage(Document d, Element a) throws Exception {
    var post = POSTBACK.matcher(a.attr("href"));
    if (!post.find()) return execute(connection(a.absUrl("href")));
    var data = fields(d.selectFirst("form"));
    data.put("__EVENTTARGET", post.group(1));
    data.put("__EVENTARGUMENT", post.group(2));
    return submit(d, data);
  }

  private static void validateReport(Document d, LocalDate day, Rules.Route expected)
      throws Exception {
    Element date = dateInput(d);
    if (date == null || !date.val().equals(dayValue(date, day)))
      throw new Exception("Ngày trên web không khớp ngày yêu cầu; bỏ lượt đọc");
    if (!allFiltersSelected(d))
      throw new Exception("Web chưa trả về toàn bộ tuyến và bản tin; bỏ lượt đọc");
    if (!expected.points().equals(parseRoute(d).points()))
      throw new Exception("Danh sách tuyến thay đổi khi đọc trang; cần đọc lại đầy đủ");
  }

  private static class MovingReport extends Exception {}

  public DayData readDay(LocalDate day) throws Exception {
    return readDelta(day, Set.of());
  }

  /** Known IDs are supplied only from a previously complete read of this account and day. */
  public synchronized DayData readDelta(LocalDate day, Set<String> known) throws Exception {
    for (int attempt = 0; attempt < 2; attempt++) {
      try {
        return readSnapshot(day, known);
      } catch (MovingReport changed) {
        /* Retry from page one, never publish a partial day. */
      }
    }
    throw new Exception("Báo cáo đang thay đổi quá nhanh; chưa lưu dữ liệu thiếu, sẽ đọc lại");
  }

  private DayData readSnapshot(LocalDate day, Set<String> known) throws Exception {
    Document d = selectDay(login(), day);
    Rules.Route route = parseRoute(d);
    List<Rules.Event> first = null;
    Map<String, Rules.Event> out = new LinkedHashMap<>();
    Set<String> seenPages = new HashSet<>();
    for (int page = 1; page <= 200; page++) {
      validateReport(d, day, route);
      var events = parse(d, day);
      if (first == null) first = events;
      if (events.isEmpty()) {
        boolean knownGrid =
            d.selectFirst("table[id$=Gdata]") != null
                || d.text().toLowerCase(Locale.ROOT).contains("báo cáo đường ngang cbtđ");
        if (!knownGrid) throw new Exception("Chưa xác minh được bảng sự kiện; bỏ lượt đọc");
      }
      if (!seenPages.add(events.toString()))
        throw new Exception("Phân trang lặp; dữ liệu chưa đầy đủ");
      for (var event : events) out.put(event.id(), event);
      boolean connected = events.stream().anyMatch(e -> known.contains(e.id()));
      Element next = pageLink(d, page + 1);
      if (next == null || connected) {
        // Revisit the newest page to include records inserted while the remaining pages were read.
        if (page > 1 && day.equals(LocalDate.now(ZONE))) {
          Element start = pageLink(d, 1);
          if (start == null) throw new Exception("Chưa trở lại được trang đầu để đối chiếu");
          Document head = followPage(d, start);
          validateReport(head, day, route);
          var latest = parse(head, day);
          Set<String> anchors = new HashSet<>();
          for (var event : first) anchors.add(event.id());
          if (latest.stream().noneMatch(e -> anchors.contains(e.id()))) throw new MovingReport();
          for (var event : latest) out.put(event.id(), event);
        }
        List<Rules.Event> result = new ArrayList<>(out.values());
        result.sort(Comparator.comparingLong(Rules.Event::at).reversed());
        return new DayData(result, route, known.isEmpty() || connected);
      }
      d = followPage(d, next);
    }
    throw new Exception("Vượt giới hạn đọc trang; chưa đủ dữ liệu");
  }

  /** Read the complete account inventory, never infer missing neighbours from event rows. */
  public static Rules.Route parseRoute(Document d) throws Exception {
    Rules.Route selected = null;
    for (Element select : d.select("select")) {
      List<String> labels = new ArrayList<>();
      for (Element option : select.select("option")) {
        String label = option.text().trim();
        if (Rules.isPoint(label)) labels.add(label);
        else if (label.matches("(?iu)^km\\s*\\d.*"))
          throw new Exception("Chưa nhận diện được điểm trong danh sách tuyến");
      }
      if (labels.isEmpty()) continue;
      Rules.Route candidate = new Rules.Route(labels);
      if (selected != null && !selected.points().equals(candidate.points()))
        throw new Exception("Có nhiều danh sách tuyến khác nhau; cần kiểm tra biểu mẫu web");
      selected = candidate;
    }
    if (selected != null) return selected;
    // The separate status table is also a full inventory on this web layout.
    for (Element table : d.select("table")) {
      boolean statusHeader = false;
      List<String> labels = new ArrayList<>();
      for (Element row : table.select("tr")) {
        List<Element> cells = new ArrayList<>();
        for (Element child : row.children())
          if (child.tagName().equals("td") || child.tagName().equals("th")) cells.add(child);
        if (cells.size() != 2) continue;
        if (cells.get(0).text().equalsIgnoreCase("Km")
            && cells.get(1).text().equalsIgnoreCase("Status")) statusHeader = true;
        if (Rules.isPoint(cells.get(0).text())) labels.add(cells.get(0).text());
      }
      if (statusHeader && !labels.isEmpty()) {
        Rules.Route candidate = new Rules.Route(labels);
        if (selected != null && !selected.points().equals(candidate.points()))
          throw new Exception("Danh sách trạng thái tuyến không đồng nhất");
        selected = candidate;
      }
    }
    if (selected == null) throw new Exception("Chưa đọc được danh sách đầy đủ đường ngang và ga");
    return selected;
  }

  public static List<Rules.Event> parse(Document d, LocalDate day) throws Exception {
    List<Rules.Event> out = new ArrayList<>();
    for (Element row : d.select("tr")) {
      var cells = new java.util.ArrayList<Element>();
      for (Element child : row.children()) if (child.tagName().equals("td")) cells.add(child);
      if (cells.size() < 4) continue;
      String crossing = cells.get(0).text().trim();
      if (!crossing.matches("(?i)Km\\s*\\d+\\+.*")) continue;
      int timeIndex = -1;
      for (int i = 2; i < cells.size(); i++)
        if (cells.get(i).text().matches("\\d{2}:\\d{2}:\\d{2}")) {
          timeIndex = i;
          break;
        }
      if (timeIndex < 0) continue;
      String date = cells.get(timeIndex - 1).text();
      if (!date.equals(day.format(DateTimeFormatter.ofPattern("dd/MM")))
          && !date.equals(day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
        throw new Exception("Ngày trả về không khớp ngày yêu cầu");
      long at =
          LocalDateTime.of(day, LocalTime.parse(cells.get(timeIndex).text()))
              .atZone(ZONE)
              .toInstant()
              .toEpochMilli();
      String text = cells.get(1).text();
      boolean red = false;
      for (Element e : row.getAllElements()) {
        String color =
            (e.attr("color") + " " + e.attr("style")).toLowerCase(Locale.ROOT).replace(" ", "");
        if (color.matches(".*(?:^|[:;])(?:red|#ff0000|#f00|rgb\\(255,0,0\\))(?:;|$).*")) red = true;
      }
      List<String> counts = new ArrayList<>();
      for (int i = timeIndex + 1; i < Math.min(cells.size(), timeIndex + 7); i++)
        counts.add(cells.get(i).text());
      String sourceId = row.attr("data-event-id");
      String id = sourceId.isBlank() ? at + "|" + crossing + "|" + text + "|" + counts : sourceId;
      out.add(new Rules.Event(id, at, crossing, text, red, counts));
    }
    return out;
  }
}
