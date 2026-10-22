package vn.duongngang.monitor;

import java.util.*;

/** Domain rules independent of Android. All timestamps are epoch milliseconds. */
public final class Rules {
  public static final List<String> NORTH = List.of("312+111", "313+456", "313+800", "314+550");
  public static final List<String> SOUTH =
      List.of("320+445", "320+957", "321+780", "327+327", "328+325", "332+350", "334+830");
  public static final List<String> ROUTE =
      List.of(
          "312+111",
          "313+456",
          "313+800",
          "314+550",
          "319+200 (Vi)",
          "320+445",
          "320+957",
          "321+780",
          "327+327",
          "328+325",
          "330+00 (YX)",
          "332+350",
          "334+830");

  public static final List<String> CROSSINGS;

  static {
    List<String> all = new ArrayList<>(NORTH);
    all.addAll(SOUTH);
    CROSSINGS = Collections.unmodifiableList(all);
  }

  public static boolean validRange(String from, String to) {
    return CROSSINGS.contains(km(from))
        && CROSSINGS.contains(km(to))
        && CROSSINGS.indexOf(km(from)) <= CROSSINGS.indexOf(km(to));
  }

  public static boolean inRange(String crossing, String from, String to) {
    if (!validRange(from, to)) return false;
    int i = CROSSINGS.indexOf(km(crossing));
    return i >= CROSSINGS.indexOf(km(from)) && i <= CROSSINGS.indexOf(km(to));
  }

  public static List<String> routeSpan(String from, String to) {
    if (!validRange(from, to)) return List.of();
    return new ArrayList<>(ROUTE.subList(ROUTE.indexOf(km(from)), ROUTE.indexOf(km(to)) + 1));
  }

  public record Event(
      String id, long at, String crossing, String text, boolean red, List<String> counts) {}

  public static String km(String raw) {
    return raw.replaceFirst("(?i)^Km\\s*", "").trim();
  }

  public static int direction(String text) {
    String t = text.toLowerCase(Locale.ROOT).trim();
    return t.equals("tàu lẻ") ? 1 : t.equals("tàu chẵn") ? -1 : 0;
  }

  public static String upstream(String crossing, int direction) {
    for (List<String> group : List.of(NORTH, SOUTH)) {
      int i = group.indexOf(km(crossing));
      if (i >= 0) {
        int j = i - direction;
        return j >= 0 && j < group.size() ? group.get(j) : null;
      }
    }
    return null;
  }

  public static String axleNote(List<String> raw) {
    if (raw.size() != 6) return "";
    List<Integer> values = new ArrayList<>();
    Map<Integer, Integer> frequencies = new HashMap<>();
    for (String s : raw) {
      if (!s.trim().matches("\\d+")) return "";
      try {
        int n = Integer.parseInt(s.trim());
        values.add(n);
        frequencies.merge(n, 1, Integer::sum);
      } catch (NumberFormatException e) {
        return "";
      }
    }
    if (frequencies.size() == 1) return "";
    int max = Collections.max(frequencies.values());
    List<Integer> modes = new ArrayList<>();
    frequencies.forEach(
        (n, c) -> {
          if (c == max) modes.add(n);
        });
    if (modes.size() != 1 || max < 2) return "cảm biến đếm trục không đồng nhất, lỗi";
    int standard = modes.get(0);
    List<String> notes = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      int delta = values.get(i) - standard;
      if (delta != 0)
        notes.add(
            "CB" + (i + 1) + " " + (delta > 0 ? "thừa " : "thiếu ") + Math.abs(delta) + " trục");
    }
    return String.join(" · ", notes);
  }

  /** Missing upstream evidence is meaningful only with complete coverage of the whole window. */
  public static boolean suspect(
      Event current, List<Event> history, long coverageStart, long travelWindow, boolean complete) {
    int d = direction(current.text());
    String previous = upstream(current.crossing(), d);
    if (d == 0 || previous == null || !complete || coverageStart > current.at() - travelWindow)
      return false;
    for (Event e : history)
      if (km(e.crossing()).equals(previous)
          && direction(e.text()) == d
          && e.at() <= current.at()
          && e.at() >= current.at() - travelWindow) return false;
    return true;
  }

  /** Generic system restoration must never clear unrelated device faults. */
  public static String faultKey(String text) {
    String t = text.toLowerCase(Locale.ROOT);
    if (t.contains("220vac")) return "nguồn 220VAC";
    if (t.contains("kết nối giám sát")) return "kết nối giám sát";
    var m = java.util.regex.Pattern.compile("(cảm biến|cần chắn)\\s*(\\d+)").matcher(t);
    if (m.find()) return m.group(1) + " " + m.group(2);
    if (t.contains("chuông")) return "chuông";
    if (t.contains("đèn")) return "đèn";
    return t;
  }

  public static boolean needsNewBaseline(long last, long now, boolean restarted) {
    if (last <= 0 || now <= last) return false;
    java.time.ZoneId zone = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
    return now - last >= 24L * 60 * 60 * 1000
        || (restarted
            && !java.time.Instant.ofEpochMilli(last)
                .atZone(zone)
                .toLocalDate()
                .equals(java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate()));
  }

  public record Incident(Event fault, Event recovery) {}

  /** Pair only explicit restoration messages with earlier faults of that device and crossing. */
  public static List<Incident> incidentHistory(List<Event> input) {
    List<Event> ordered = new ArrayList<>(input);
    ordered.sort(Comparator.comparingLong(Event::at));
    List<Incident> result = new ArrayList<>();
    Map<String, List<Integer>> open = new HashMap<>();
    for (Event event : ordered) {
      String key = km(event.crossing()) + "|" + faultKey(event.text());
      if (event.red()) {
        open.computeIfAbsent(key, k -> new ArrayList<>()).add(result.size());
        result.add(new Incident(event, null));
      } else if (event.text().toLowerCase(Locale.ROOT).contains("khôi phục tốt")) {
        List<String> matches = new ArrayList<>();
        matches.add(key);
        String lower = event.text().toLowerCase(Locale.ROOT);
        if (lower.contains("cần chắn") && faultKey(event.text()).equals(lower)) {
          String prefix = km(event.crossing()) + "|cần chắn ";
          for (String candidate : open.keySet())
            if (candidate.startsWith(prefix)) matches.add(candidate);
        }
        for (String match : matches) {
          List<Integer> indices = open.remove(match);
          if (indices != null)
            for (int index : indices)
              result.set(index, new Incident(result.get(index).fault(), event));
        }
      }
    }
    result.sort(Comparator.comparingLong((Incident i) -> i.fault().at()).reversed());
    return result;
  }

  public static List<Event> activeFaults(List<Event> input) {
    List<Event> sorted = new ArrayList<>(input);
    sorted.sort(Comparator.comparingLong(Event::at));
    Map<String, Event> faults = new LinkedHashMap<>();
    for (Event e : sorted) {
      String k = km(e.crossing()) + "|" + faultKey(e.text());
      if (e.red()) faults.put(k, e);
      else if (e.text().toLowerCase(Locale.ROOT).contains("khôi phục tốt")) {
        faults.remove(k);
        // The actual web has a collective "Cần chắn khôi phục tốt" event.
        if (e.text().toLowerCase(Locale.ROOT).contains("cần chắn")
            && faultKey(e.text()).equals(e.text().toLowerCase(Locale.ROOT))) {
          String prefix = km(e.crossing()) + "|cần chắn ";
          faults.keySet().removeIf(key -> key.startsWith(prefix));
        }
      }
    }
    var result = new ArrayList<>(faults.values());
    result.sort(Comparator.comparingLong(Event::at).reversed());
    return result;
  }
}
