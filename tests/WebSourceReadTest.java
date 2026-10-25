import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.*;
import vn.duongngang.monitor.*;

/** Local ASP.NET fixture includes the master-page logout button and both report filters. */
public final class WebSourceReadTest {
  static final LocalDate DAY = LocalDate.now(WebSource.ZONE);
  static int checks;

  static void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }

  static String inventory(String code) {
    return "<select name='route'><option value='all'>-- Tất cả --</option>"
        + "<option value='north' selected>Km 275+290</option><option>Km 276+351</option>"
        + "<option>Km 279+000 ("
        + code
        + ")</option><option>Km 280+500</option></select>";
  }

  static String row(String point, String text, String time) {
    return "<tr><td>Km "
        + point
        + "</td><td>"
        + text
        + "</td><td>"
        + DAY.format(DateTimeFormatter.ofPattern("dd/MM"))
        + "</td><td>"
        + time
        + "</td></tr>";
  }

  static String report(int page, String mode, boolean head) {
    String code = mode.equals("changed") && page == 2 ? "DIFFERENT" : "CS";
    String point = page == 1 ? "275+290" : mode.equals("outside") ? "500+100" : "276+351";
    LocalDate selected = mode.equals("wrongday") ? DAY.minusDays(1) : DAY;
    String rows = row(point, "Tàu lẻ", "10:20:0" + (mode.equals("repeat") ? 1 : page));
    if (mode.equals("repeat")) rows = row("275+290", "Tàu lẻ", "10:20:01");
    if (head && mode.equals("moving")) rows = row("280+500", "Tàu chẵn", "11:00:00");
    if (head && mode.equals("newhead")) rows = row("280+500", "Tàu chẵn", "10:20:03") + rows;
    String table =
        mode.equals("empty") ? "<h1>BÁO CÁO ĐƯỜNG NGANG CBTĐ</h1>" : "<table>" + rows + "</table>";
    String pager =
        mode.equals("empty")
            ? ""
            : page == 1
                ? "<a href=\"javascript:__doPostBack('grid','Page$2')\">...</a>"
                : "<a href=\"javascript:__doPostBack('grid','Page$1')\">1</a>";
    return "<form action='/report'><input name='date' value='"
        + selected.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        + "'>"
        + "<input name='__VIEWSTATE' value='test'>"
        + inventory(code)
        + "<input type='radio' name='kind' value='Tất cả'><input type='radio' name='kind'"
        + " value='Trở ngại' checked>"
        + table
        + pager
        + "</form>";
  }

  static Map<String, String> form(String body) {
    Map<String, String> result = new HashMap<>();
    for (String pair : body.split("&")) {
      String[] p = pair.split("=", 2);
      result.put(
          URLDecoder.decode(p[0], StandardCharsets.UTF_8),
          p.length > 1 ? URLDecoder.decode(p[1], StandardCharsets.UTF_8) : "");
    }
    return result;
  }

  public static void main(String[] args) throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    AtomicInteger posts = new AtomicInteger();
    server.createContext(
        "/",
        exchange -> {
          String mode = exchange.getRequestURI().getQuery(),
              cookie = exchange.getRequestHeaders().getFirst("Cookie"),
              body;
          if (exchange.getRequestMethod().equals("GET")) {
            body =
                "<form action='/login?"
                    + mode
                    + "'><input type='submit' name='logout' value='Thoát'>"
                    + "<input type='text' name='user'><input type='password' name='password'>"
                    + "<input type='submit' name='login' value='Đăng nhập'></form>";
          } else if (exchange.getRequestURI().getPath().equals("/login")) {
            var values =
                form(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (values.containsKey("logout")
                || !values.containsKey("login")
                || !("fixture-user".equals(values.get("user"))
                    || "Chosy".equals(values.get("user")))
                || !"fixture-only".equals(values.get("password")))
              throw new AssertionError("login selected wrong button");
            exchange.getResponseHeaders().add("Set-Cookie", "session=" + mode + "; Path=/");
            body =
                mode.equals("authfail")
                    ? "<form><input type='text' name='user'><input type='password'"
                        + " name='password'></form>"
                    : report(1, mode, false);
          } else {
            posts.incrementAndGet();
            if (cookie == null || !cookie.contains("session="))
              throw new AssertionError("no session cookie");
            mode = cookie.substring(cookie.indexOf("session=") + 8).split(";")[0];
            var values =
                form(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (!"all".equals(values.get("route")) || !"Tất cả".equals(values.get("kind")))
              throw new AssertionError("all-points/all-events filter lost");
            String action = values.get("__EVENTARGUMENT");
            body = report("Page$2".equals(action) ? 2 : 1, mode, "Page$1".equals(action));
            if (!mode.equals("filterignored"))
              body =
                  body.replace("value='north' selected", "value='north'")
                      .replace("value='all'", "value='all' selected")
                      .replace("value='Trở ngại' checked", "value='Trở ngại'")
                      .replace("value='Tất cả'", "value='Tất cả' checked");
          }
          byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
          exchange.sendResponseHeaders(200, bytes.length);
          exchange.getResponseBody().write(bytes);
          exchange.close();
        });
    server.start();
    try {
      String base = "http://127.0.0.1:" + server.getAddress().getPort() + "/?";
      var source = new WebSource(base + "normal", "fixture-user", "fixture-only");
      var complete = source.readDay(DAY);
      check(
          complete.events().size() == 2 && complete.route().points().size() == 4,
          "authenticated complete report and ellipsis pager");
      check(
          complete.route().points().contains("280+500")
              && complete.route().upstream("280+500", 1) == null,
          "quiet crossing and station boundary retained");
      int before = posts.get();
      var delta = source.readDelta(DAY, Set.of(complete.events().get(1).id()));
      check(
          delta.connected() && delta.events().size() == 1 && posts.get() - before == 1,
          "delta stops at stored anchor instead of rescanning whole day");
      var gap = source.readDelta(DAY, Set.of("unavailable-old-anchor"));
      check(
          !gap.connected() && gap.events().size() == 2,
          "unmatched old cursor is reported as a coverage gap");
      var added = new WebSource(base + "newhead", "fixture-user", "fixture-only").readDay(DAY);
      check(
          added.events().size() == 3,
          "reread first page captures records inserted during pagination");
      check(
          new WebSource(base + "empty", "fixture-user", "fixture-only")
              .readDay(DAY)
              .events()
              .isEmpty(),
          "validated empty day accepted");
      for (String mode :
          List.of("changed", "outside", "wrongday", "repeat", "moving", "filterignored")) {
        boolean rejected = false;
        try {
          new WebSource(base + mode, "fixture-user", "fixture-only").readDay(DAY);
        } catch (Exception expected) {
          rejected = true;
        }
        check(rejected, "inconsistent/incomplete report rejected: " + mode);
      }
      boolean badLogin = false;
      try {
        new WebSource(base + "authfail", "fixture-user", "fixture-only").readDay(DAY);
      } catch (WebSource.AuthenticationException expected) {
        badLogin = true;
      }
      check(badLogin, "invalid login has a distinct non-retryable error");
      var chosy = new WebSource(base + "normal", "Chosy", "fixture-only");
      var supplemented = chosy.readDay(DAY);
      check(
          supplemented.route().points().contains("303+350 (Nghi Long)")
              && supplemented.route().crossings().equals(complete.route().crossings())
              && supplemented.events().equals(complete.events()),
          "complete authenticated read adds station after validating original paginated inventory");
      var supplementedDelta = chosy.readDelta(DAY, Set.of(supplemented.events().get(1).id()));
      check(
          supplementedDelta.connected()
              && supplementedDelta.route().points().equals(supplemented.route().points())
              && supplementedDelta.events().size() == 1,
          "delta read retains account station without inventing events or breaking anchors");
      System.out.println(checks + " authenticated read scenarios passed (local web fixture)");
    } finally {
      server.stop(0);
    }
  }
}
