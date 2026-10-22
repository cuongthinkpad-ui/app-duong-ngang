import java.time.*;
import java.util.*;
import org.jsoup.Jsoup;
import vn.duongngang.monitor.*;

public class ParserTest {
  public static void main(String[] args) throws Exception {
    String html =
        "<table><tr><td>Km 328+325</td><td><font color='red'>Điện áp cảm biến 6 &lt;"
            + " 5V</font></td><td>22/10</td><td>10:26:56</td><td>46</td><td>46</td><td>48</td><td>44</td><td>46</td><td>46</td></tr></table>";
    var parsed = WebSource.parse(Jsoup.parse(html), LocalDate.of(2026, 10, 22));
    if (parsed.size() != 1
        || !parsed.get(0).red()
        || !Rules.axleNote(parsed.get(0).counts()).contains("CB4 thiếu 2"))
      throw new AssertionError("parse red event/axes");
    boolean rejected = false;
    try {
      WebSource.parse(Jsoup.parse(html), LocalDate.of(2026, 10, 21));
    } catch (Exception e) {
      rejected = true;
    }
    if (!rejected) throw new AssertionError("wrong day must reject");
    String css = html.replace("<font color='red'>", "<font style='color: rgb(255, 0, 0);'>");
    if (!WebSource.parse(Jsoup.parse(css), LocalDate.of(2026, 10, 22)).get(0).red())
      throw new AssertionError("CSS red");
    String inventory =
        "<select><option>-- Tất cả --</option><option value='9'>Km 311+400</option>"
            + "<option value='1'>Km 275+290</option><option>Km 279+000 (CS)</option>"
            + "<option>Km 276+351</option><option>Km 291+600 (MLy)</option>"
            + "<option>Km 308+210 (QHa)</option></select>";
    var route = WebSource.parseRoute(Jsoup.parse(inventory + html));
    if (!route.first().equals("275+290")
        || !route.last().equals("311+400")
        || route.points().size() != 6
        || route.crossings().size() != 3)
      throw new AssertionError("read full inventory, not the current event page");
    if (!route.points().contains("291+600 (MLy)") || route.upstream("276+351", -1) != null)
      throw new AssertionError("station abbreviation and boundary");
    var fallback =
        WebSource.parseRoute(
            Jsoup.parse(
                "<table><tr><th>Km</th><th>Status</th></tr><tr><td>Km"
                    + " 276+351</td><td>1</td></tr><tr><td>Km"
                    + " 279+000<br>(CS)</td><td>1</td></tr><tr><td>Km"
                    + " 275+290</td><td>1</td></tr></table>"));
    if (!fallback.points().equals(List.of("275+290", "276+351", "279+000 (CS)")))
      throw new AssertionError("full status inventory fallback");
    rejected = false;
    try {
      WebSource.parseRoute(Jsoup.parse(html));
    } catch (Exception expected) {
      rejected = true;
    }
    if (!rejected) throw new AssertionError("events alone do not establish a complete route");
    rejected = false;
    try {
      WebSource.parseRoute(Jsoup.parse(inventory + "<select><option>Km 500+000</option></select>"));
    } catch (Exception expected) {
      rejected = true;
    }
    if (!rejected) throw new AssertionError("conflicting selectors must reject");
    rejected = false;
    try {
      WebSource.parseRoute(Jsoup.parse("<select><option>Km 279+000 (CS</option></select>"));
    } catch (Exception expected) {
      rejected = true;
    }
    if (!rejected) throw new AssertionError("malformed station must reject");
    rejected = false;
    try {
      new WebSource.DayData(parsed, route);
    } catch (IllegalArgumentException expected) {
      rejected = true;
    }
    if (!rejected) throw new AssertionError("cannot commit events from a different route");
    var eventRoute = new Rules.Route(List.of("328+325", "330+00 (YX)"));
    var snapshot = new WebSource.DayData(parsed, eventRoute);
    if (snapshot.events().size() != 1) throw new AssertionError("valid route snapshot");
    String original = "Chuông cảnh báo số 2 mất trạng thái";
    var bell =
        WebSource.parse(
            Jsoup.parse(html.replace("Điện áp cảm biến 6 &lt; 5V", original)),
            LocalDate.of(2026, 10, 22));
    if (!bell.get(0).text().equals(original) || !bell.get(0).red())
      throw new AssertionError("preserve exact numbered device fault message");
    System.out.println("12 parser scenarios passed");
  }
}
