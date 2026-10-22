import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import vn.duongngang.monitor.*;

/**
 * Local ASP.NET-shaped fixture: validates the authenticated multi-page route read as one result.
 */
public final class WebSourceReadTest {
  static String inventory(String code) {
    return "<select name='route'><option value='all'>-- Tất cả --</option>"
        + "<option value='north' selected>Km 275+290</option>"
        + "<option>Km 276+351</option><option>Km 279+000 ("
        + code
        + ")</option>"
        + "<option>Km 280+500</option></select>";
  }

  static String report(int page, String mode) {
    String code = mode.equals("changed") && page == 2 ? "DIFFERENT" : "CS";
    String point = page == 1 ? "275+290" : mode.equals("outside") ? "500+100" : "276+351";
    return "<form action='/report'><input name='date' value='22/10/2026'>"
        + "<input name='__VIEWSTATE' value='test'>"
        + inventory(code)
        + "<table><tr><td>Km "
        + point
        + "</td><td>Tàu lẻ</td><td>22/10</td>"
        + "<td>10:20:0"
        + page
        + "</td></tr></table>"
        + (page == 1 ? "<a href=\"javascript:__doPostBack('grid','Page$2')\">2</a>" : "")
        + "</form>";
  }

  static Map<String, String> form(String text) {
    Map<String, String> out = new HashMap<>();
    for (String pair : text.split("&")) {
      String[] parts = pair.split("=", 2);
      out.put(
          URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
          parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
    }
    return out;
  }

  public static void main(String[] args) throws Exception {
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    AtomicBoolean selectedAll = new AtomicBoolean(false);
    server.createContext(
        "/",
        exchange -> {
          String mode = exchange.getRequestURI().getQuery();
          String cookie = exchange.getRequestHeaders().getFirst("Cookie");
          String body;
          if (exchange.getRequestMethod().equals("GET")) {
            body =
                "<form action='/login?"
                    + mode
                    + "'><input name='user' type='text'><input name='password'"
                    + " type='password'><input type='submit' name='login' value='OK'></form>";
          } else if (exchange.getRequestURI().getPath().equals("/login")) {
            var values =
                form(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (!"fixture-user".equals(values.get("user"))
                || !"fixture-only".equals(values.get("password")))
              throw new AssertionError("login form mapping");
            exchange.getResponseHeaders().add("Set-Cookie", "session=" + mode + "; Path=/");
            body = report(1, mode);
          } else {
            if (cookie == null || !cookie.contains("session="))
              throw new AssertionError("missing login cookie");
            mode = cookie.substring(cookie.indexOf("session=") + 8).split(";")[0];
            var values =
                form(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (!"all".equals(values.get("route")))
              throw new AssertionError("pagination must keep all points");
            if ("date".equals(values.get("__EVENTTARGET"))) {
              if (!"all".equals(values.get("route")))
                throw new AssertionError("must select all points");
              selectedAll.set(true);
            }
            int page = "Page$2".equals(values.get("__EVENTARGUMENT")) ? 2 : 1;
            body = report(page, mode);
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
      var read =
          new WebSource(base + "normal", "fixture-user", "fixture-only")
              .readDay(LocalDate.of(2026, 10, 22));
      if (read.events().size() != 2 || read.route().points().size() != 4 || !selectedAll.get())
        throw new AssertionError("complete route plus both event pages");
      if (!read.route().points().contains("280+500") || read.route().upstream("280+500", 1) != null)
        throw new AssertionError("quiet crossing and station boundary retained");
      for (String mode : List.of("changed", "outside")) {
        boolean rejected = false;
        try {
          new WebSource(base + mode, "fixture-user", "fixture-only")
              .readDay(LocalDate.of(2026, 10, 22));
        } catch (Exception expected) {
          rejected = true;
        }
        if (!rejected) throw new AssertionError("must reject inconsistent snapshot: " + mode);
      }
      System.out.println("4 authenticated read scenarios passed (local web fixture)");
    } finally {
      server.stop(0);
    }
  }
}
