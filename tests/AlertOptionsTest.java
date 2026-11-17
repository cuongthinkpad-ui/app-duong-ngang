import java.util.*;
import vn.duongngang.monitor.*;

public class AlertOptionsTest {
  static int checks;
  static void check(boolean ok, String message) { checks++; if (!ok) throw new AssertionError(message); }
  public static void main(String[] args) {
    check(AlertOptions.mode("", true).equals(AlertOptions.VOICE), "fresh audible mode enables requested voice variant");
    check(AlertOptions.mode("voice", false).equals(AlertOptions.SILENT), "legacy mute always wins");
    check(AlertOptions.mode("bell", true).equals(AlertOptions.BELL), "bell-only selection is preserved");
    check(AlertOptions.mode("silent", true).equals(AlertOptions.SILENT), "explicit silent mode cannot produce sound");
    check(AlertOptions.mode("invalid", true).equals(AlertOptions.VOICE), "invalid old preference has a defined default");
    check(AlertOptions.REPEATS == 6, "exactly six readings requested");
    for (String message : List.of("Cầu chì 50A bị đứt", "nguồn DC > 29v", "Chuông 2 hỏng")) {
      var event = new Rules.Event("speech", 1, "Km 313+800", message, true, List.of());
      String speech = AlertOptions.announcement(event);
      check(speech.startsWith("Đường ngang ki lô mét 313 cộng 800."), "spoken alert includes exact point");
      String spoken = message.equals("Cầu chì 50A bị đứt") ? "Cầu chì 50 am pe bị đứt"
          : message.equals("nguồn DC > 29v") ? "nguồn DC lớn hơn 29 vôn" : message;
      check(speech.contains(spoken) && event.text().equals(message), "speech preserves fault meaning and original record stays unchanged");
    }
    var cb = new Rules.Event("cb", 1, "100+000", "CB3 đếm thừa 12 trục", true, List.of());
    check(AlertOptions.announcement(cb).contains("cảm biến 3 đếm thừa 12 trục"), "speech expands CB identity without losing its number");
    String longText = "Cầu chì 50A bị đứt. ".repeat(500);
    var chunks = AlertOptions.chunks(longText, 4000);
    check(chunks.size() > 1 && chunks.stream().allMatch(c -> c.length() <= 4000), "long speech respects engine input limit");
    check(String.join(" ", chunks).equals(longText.trim()), "long alert remainder is not silently dropped");
    check(AlertOptions.chunks("", 4000).isEmpty(), "empty speech produces no utterance");
    String emoji = "🚂".repeat(30);
    var unicode = AlertOptions.chunks(emoji, 9);
    check(unicode.stream().allMatch(c -> !Character.isHighSurrogate(c.charAt(c.length()-1)))
        && String.join("", unicode).equals(emoji), "chunking preserves Unicode characters");
    System.out.println(checks + " alert option checks passed");
  }
}
