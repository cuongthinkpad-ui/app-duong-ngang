package vn.duongngang.monitor;

import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

/** Conservative ASP.NET adapter. Must be verified against the actual deployment. */
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
        .userAgent("DuongNgang/0.1 Android");
  }

  private Document execute(Connection c) throws Exception {
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

  private Map<String, String> fields(Element form) {
    Map<String, String> data = new LinkedHashMap<>();
    for (Element e : form.select("input[name], select[name], textarea[name]")) {
      String type = e.attr("type");
      if (List.of("submit", "button", "image", "checkbox", "radio").contains(type)) continue;
      data.put(e.attr("name"), e.val());
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

  public List<Rules.Event> readDay(LocalDate day) throws Exception {
    Document d = execute(connection(base));
    Element pass = d.selectFirst("input[type=password]");
    if (pass != null) {
      Element form = pass.closest("form");
      if (form == null) throw new Exception("Chưa nhận diện được biểu mẫu đăng nhập");
      var data = fields(form);
      Element login =
          form.selectFirst(
              "input[type=text][name],input[type=email][name],input:not([type])[name]");
      Element button = form.selectFirst("input[type=submit][name],button[type=submit][name]");
      if (login == null) throw new Exception("Chưa nhận diện được ô tài khoản");
      data.put(login.attr("name"), user);
      data.put(pass.attr("name"), password);
      if (button != null) data.put(button.attr("name"), button.val());
      d = submit(d, data);
      if (d.selectFirst("input[type=password]") != null)
        throw new Exception("Chưa đăng nhập được; cần kiểm tra tài khoản hoặc biểu mẫu");
    }
    Element date = null;
    for (Element e : d.select("input[name]")) {
      if (e.val().matches("\\d{2}/\\d{2}/\\d{4}") || e.attr("type").equals("date")) {
        date = e;
        break;
      }
    }
    if (date == null)
      throw new Exception("Chưa nhận diện bộ chọn ngày; không dùng dữ liệu chưa xác minh");
    var data = fields(date.closest("form"));
    data.put(
        date.attr("name"),
        day.format(
            DateTimeFormatter.ofPattern(
                date.attr("type").equals("date") ? "yyyy-MM-dd" : "dd/MM/yyyy")));
    data.put("__EVENTTARGET", date.attr("name"));
    data.put("__EVENTARGUMENT", "");
    d = submit(d, data);
    List<Rules.Event> out = new ArrayList<>();
    Set<String> pages = new HashSet<>();
    for (int page = 1; page <= 200; page++) {
      var events = parse(d, day);
      if (events.isEmpty() && page == 1)
        throw new Exception("Chưa xác minh được bảng sự kiện hoặc ngày không có dữ liệu");
      String fingerprint = events.toString();
      if (!pages.add(fingerprint)) throw new Exception("Phân trang lặp; dữ liệu chưa đầy đủ");
      out.addAll(events);
      Element next = null;
      for (Element a : d.select("a[href]")) {
        if (a.text().trim().equals("" + (page + 1))) {
          next = a;
          break;
        }
      }
      if (next == null) return out;
      String href = next.attr("href");
      var matcher = Pattern.compile("__doPostBack\\('([^']*)','([^']*)'\\)").matcher(href);
      if (matcher.find()) {
        data = fields(d.selectFirst("form"));
        data.put("__EVENTTARGET", matcher.group(1));
        data.put("__EVENTARGUMENT", matcher.group(2));
        d = submit(d, data);
      } else if (!href.startsWith("javascript:")) {
        d = execute(connection(next.absUrl("href")));
      } else throw new Exception("Chưa hỗ trợ kiểu phân trang của web");
    }
    throw new Exception("Vượt giới hạn đọc trang; chưa đủ dữ liệu");
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
