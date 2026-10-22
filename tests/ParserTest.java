import java.time.*;
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
    System.out.println("3 parser scenarios passed");
  }
}
