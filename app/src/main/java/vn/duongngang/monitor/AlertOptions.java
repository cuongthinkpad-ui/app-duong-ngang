package vn.duongngang.monitor;

import java.util.*;

/** Alert preferences and speech wording; displayed website reports stay unchanged. */
public final class AlertOptions {
  public static final String VOICE = "voice", BELL = "bell", SILENT = "silent";
  public static final int REPEATS = 6;

  public static String mode(String saved, boolean sound) {
    if (!sound || SILENT.equals(saved)) return SILENT;
    return BELL.equals(saved) ? BELL : VOICE;
  }

  public static String announcement(Rules.Event event) {
    String point = Rules.km(event.crossing()).replace("+", " cộng ");
    String fault = event.text().trim().replaceAll("\\s+", " ")
        .replaceAll("(?i)\\bCB([1-6])\\b", "cảm biến $1")
        .replace(">", " lớn hơn ").replace("<", " nhỏ hơn ")
        .replaceAll("(?i)\\b(\\d+)\\s*vac\\b", "$1 vôn xoay chiều")
        .replaceAll("(?i)\\b(\\d+(?:[.,]\\d+)?)\\s*v\\b", "$1 vôn")
        .replaceAll("(?i)\\b(\\d+)\\s*a\\b", "$1 am pe")
        .replaceAll("\\s+", " ");
    return "Đường ngang ki lô mét " + point + ". " + fault
        + (fault.matches(".*[.!?]$") ? "" : ".");
  }

  /** Engines limit each utterance; split long reports without dropping their remainder. */
  public static List<String> chunks(String text, int maximum) {
    if (maximum < 2) throw new IllegalArgumentException("Speech chunk too short");
    List<String> result = new ArrayList<>();
    String rest = text.trim();
    while (rest.length() > maximum) {
      int end = rest.lastIndexOf(' ', maximum);
      if (end <= 0) end = maximum;
      if (Character.isHighSurrogate(rest.charAt(end - 1))) end--;
      result.add(rest.substring(0, end).trim());
      rest = rest.substring(end).trim();
    }
    if (!rest.isEmpty()) result.add(rest);
    return result;
  }
}
