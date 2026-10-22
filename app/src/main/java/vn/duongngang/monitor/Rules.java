package vn.duongngang.monitor;

import java.util.*;

/** Domain rules independent of Android. All timestamps are epoch milliseconds. */
public final class Rules {
  private static final java.util.regex.Pattern POINT =
      java.util.regex.Pattern.compile(
          "(?iu)^(?:km\\s*)?(\\d+)\\s*\\+\\s*(\\d{1,3})(?:\\s*\\(\\s*([^()]+?)\\s*\\))?$");

  /** Fixture only: operational routes must come from the authenticated page's full point list. */
  public static final Route DEMO_ROUTE =
      new Route(
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
              "334+830"));

  public static boolean isPoint(String raw) {
    return raw != null && POINT.matcher(raw.trim()).matches();
  }

  public static String stationCode(String raw) {
    if (raw == null) return "";
    var match = POINT.matcher(raw.trim());
    return match.matches() && match.group(3) != null ? match.group(3).trim() : "";
  }

  public static boolean isStation(String raw) {
    return !stationCode(raw).isEmpty();
  }

  private static long chainage(String raw) {
    var match = POINT.matcher(raw);
    if (!match.matches()) throw new IllegalArgumentException("Lý trình không hợp lệ: " + raw);
    return Math.addExact(
        Math.multiplyExact(Long.parseLong(match.group(1)), 1000), Integer.parseInt(match.group(2)));
  }

  /** Immutable account-specific topology. A station breaks adjacency in both directions. */
  public static final class Route {
    private final List<String> points, crossings;

    public Route(Collection<String> raw) {
      Set<String> unique = new LinkedHashSet<>();
      Map<Long, String> locations = new HashMap<>();
      for (String value : raw) {
        if (!isPoint(value)) throw new IllegalArgumentException("Lý trình không hợp lệ: " + value);
        String point = km(value);
        String existing = locations.putIfAbsent(chainage(point), point);
        if (existing != null && !existing.equals(point))
          throw new IllegalArgumentException("Danh sách điểm mâu thuẫn tại Km " + point);
        unique.add(point);
      }
      List<String> ordered = new ArrayList<>(unique);
      ordered.sort(Comparator.comparingLong(Rules::chainage));
      points = Collections.unmodifiableList(ordered);
      List<String> road = new ArrayList<>();
      for (String point : points) if (!isStation(point)) road.add(point);
      crossings = Collections.unmodifiableList(road);
    }

    public List<String> points() {
      return points;
    }

    public List<String> crossings() {
      return crossings;
    }

    public String first() {
      return crossings.isEmpty() ? "" : crossings.get(0);
    }

    public String last() {
      return crossings.isEmpty() ? "" : crossings.get(crossings.size() - 1);
    }

    public boolean validRange(String from, String to) {
      return crossings.contains(km(from))
          && crossings.contains(km(to))
          && crossings.indexOf(km(from)) <= crossings.indexOf(km(to));
    }

    public boolean inRange(String point, String from, String to) {
      if (!validRange(from, to)) return false;
      int i = crossings.indexOf(km(point));
      return i >= crossings.indexOf(km(from)) && i <= crossings.indexOf(km(to));
    }

    public List<String> routeSpan(String from, String to) {
      if (!validRange(from, to)) return List.of();
      return new ArrayList<>(points.subList(points.indexOf(km(from)), points.indexOf(km(to)) + 1));
    }

    public String upstream(String crossing, int direction) {
      if (direction != 1 && direction != -1 || !crossings.contains(km(crossing))) return null;
      int previous = points.indexOf(km(crossing)) - direction;
      if (previous < 0 || previous >= points.size() || isStation(points.get(previous))) return null;
      return points.get(previous);
    }
  }

  public record Event(
      String id, long at, String crossing, String text, boolean red, List<String> counts) {}

  public static String km(String raw) {
    if (raw == null) return "";
    var match = POINT.matcher(raw.trim());
    if (!match.matches()) return raw.replaceFirst("(?i)^Km\\s*", "").trim();
    return match.group(1)
        + "+"
        + match.group(2)
        + (match.group(3) == null ? "" : " (" + match.group(3).trim() + ")");
  }

  public static int direction(String text) {
    String t = text.toLowerCase(Locale.ROOT).trim();
    return t.equals("tàu lẻ") ? 1 : t.equals("tàu chẵn") ? -1 : 0;
  }

  public record Occupancy(String crossing, int direction, long at) {}

  /**
   * Per-crossing occupation from reports, not inferred GPS positions or unique train identities.
   */
  public static List<Occupancy> occupancies(Route route, List<Event> input) {
    List<Event> ordered = new ArrayList<>(input);
    ordered.sort(Comparator.comparingLong(Event::at).thenComparing(Event::id));
    Map<String, Occupancy> occupied = new HashMap<>();
    Set<String> seen = new HashSet<>();
    for (Event e : ordered) {
      String point = km(e.crossing());
      if (!seen.add(e.id()) || !route.crossings().contains(point)) continue;
      int direction = direction(e.text());
      if (direction != 0) occupied.put(point, new Occupancy(point, direction, e.at()));
      else if (e.text().trim().equalsIgnoreCase("Khôi phục hệ thống")) occupied.remove(point);
      // Tail clearing CB3/CB4 and raised barriers do not release the full monitored area.
    }
    List<Occupancy> result = new ArrayList<>();
    for (String point : route.crossings())
      if (occupied.containsKey(point)) result.add(occupied.get(point));
    return result;
  }

  public static int passedDirection(String text) {
    String t = text.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    if (t.equals("tàu lẻ đã qua đường ngang")) return 1;
    if (t.equals("tàu chẵn đã qua đường ngang")) return -1;
    return 0;
  }

  /** One source arrival, kept until both source release and downstream arrival are known. */
  public record Journey(
      String id,
      String from,
      String to,
      int direction,
      long at,
      boolean passed,
      boolean released,
      boolean reached) {}

  public record TravelState(List<Journey> journeys) {
    public TravelState {
      journeys = List.copyOf(journeys);
    }

    public static TravelState empty() {
      return new TravelState(List.of());
    }
  }

  public record Between(String from, String to, int direction, long at) {}

  public static List<Between> betweenCrossings(TravelState state) {
    Map<String, Between> result = new LinkedHashMap<>();
    for (Journey j : state.journeys())
      if (j.passed() && !j.reached() && j.to() != null) {
        String key = j.from() + "|" + j.to() + "|" + j.direction();
        result.put(key, new Between(j.from(), j.to(), j.direction(), j.at()));
      }
    return new ArrayList<>(result.values());
  }

  public static List<Occupancy> approachingCrossings(TravelState state) {
    List<Occupancy> result = new ArrayList<>();
    for (Journey j : state.journeys())
      if (!j.passed() && !j.released()) result.add(new Occupancy(j.from(), j.direction(), j.at()));
    return result;
  }

  private static Journey replaceJourney(
      Journey j, boolean passed, boolean released, boolean reached) {
    return new Journey(j.id(), j.from(), j.to(), j.direction(), j.at(), passed, released, reached);
  }

  /**
   * Carries a train between adjacent crossings after its passage, through source restoration. An
   * early downstream approach consumes the corresponding transfer before source passage, so
   * overlapping detection zones cannot reopen an already completed transfer. A station always
   * breaks the link. This is event-based continuity, not GPS interpolation.
   */
  public static TravelState travelState(Route route, TravelState initial, List<Event> input) {
    List<Event> ordered = new ArrayList<>(input);
    ordered.sort(
        Comparator.comparingLong(Event::at)
            .thenComparingInt(
                e -> direction(e.text()) != 0 ? 0 : passedDirection(e.text()) != 0 ? 1 : 2)
            .thenComparingLong(
                e ->
                    isPoint(e.crossing())
                        ? chainage(km(e.crossing())) * (direction(e.text()) == -1 ? -1 : 1)
                        : 0)
            .thenComparing(Event::id));
    Map<String, Journey> journeys = new LinkedHashMap<>();
    for (Journey j : initial.journeys())
      if (route.crossings().contains(j.from())
          && Objects.equals(j.to(), route.upstream(j.from(), -j.direction())))
        journeys.put(j.id(), j);
    Set<String> seen = new HashSet<>();
    for (Event e : ordered) {
      String point = km(e.crossing());
      if (e.red() || !seen.add(e.id()) || !route.crossings().contains(point)) continue;
      int d = direction(e.text()), passed = passedDirection(e.text());
      if (d != 0) {
        Journey previous = null;
        for (Journey j : journeys.values())
          if (point.equals(j.to())
              && j.direction() == d
              && !j.reached()
              && j.at() <= e.at()
              && (previous == null || j.at() < previous.at())) previous = j;
        if (previous != null) {
          if (previous.released()) journeys.remove(previous.id());
          else
            journeys.put(previous.id(), replaceJourney(previous, previous.passed(), false, true));
        }
        // A new arrival supersedes the current source session, but a pending outgoing leg survives.
        List<Journey> old = new ArrayList<>(journeys.values());
        for (Journey j : old)
          if (j.from().equals(point) && !j.released()) {
            if (j.reached() || !j.passed()) journeys.remove(j.id());
            else journeys.put(j.id(), replaceJourney(j, true, true, false));
          }
        journeys.put(
            e.id(),
            new Journey(e.id(), point, route.upstream(point, -d), d, e.at(), false, false, false));
      } else if (passed != 0 || e.text().trim().equalsIgnoreCase("Khôi phục hệ thống")) {
        Journey current = null;
        for (Journey j : journeys.values())
          if (j.from().equals(point)
              && !j.released()
              && j.at() <= e.at()
              && (passed == 0 || j.direction() == passed)
              && (current == null || j.at() > current.at())) current = j;
        if (current == null && passed != 0)
          current =
              new Journey(
                  e.id(),
                  point,
                  route.upstream(point, -passed),
                  passed,
                  e.at(),
                  true,
                  false,
                  false);
        if (current == null) continue; // A restoration alone cannot establish a direction.
        boolean released = passed == 0;
        Journey next = replaceJourney(current, true, released, current.reached());
        if (released && (next.reached() || next.to() == null)) journeys.remove(next.id());
        else journeys.put(next.id(), next);
      }
    }
    return new TravelState(new ArrayList<>(journeys.values()));
  }

  public static String axleNote(Event event) {
    return event.text().trim().equalsIgnoreCase("Khôi phục hệ thống")
        ? axleNote(event.counts())
        : "";
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
      Route route,
      Event current,
      List<Event> history,
      long coverageStart,
      long travelWindow,
      boolean complete) {
    int d = direction(current.text());
    String previous = route.upstream(current.crossing(), d);
    if (d == 0 || previous == null || !complete || coverageStart > current.at() - travelWindow)
      return false;
    for (Event e : history)
      if (km(e.crossing()).equals(previous)
          && direction(e.text()) == d
          && e.at() <= current.at()
          && e.at() >= current.at() - travelWindow) return false;
    return true;
  }

  public static boolean selfTriggerEnabled(int intervalSeconds) {
    return intervalSeconds == 30;
  }

  public static boolean isSelfTrigger(Event event) {
    return event.id().startsWith("self:");
  }

  private static String eventFaultKey(Event event) {
    return isSelfTrigger(event) ? "@self" : faultKey(event.text());
  }

  /** Each upstream report can justify one downstream arrival of the same direction. */
  public static List<Event> suspectedSelfTriggers(
      Route route, List<Event> input, long coverageStart) {
    List<Event> ordered = new ArrayList<>(input);
    ordered.sort(Comparator.comparingLong(Event::at).thenComparing(Event::id));
    Map<String, ArrayDeque<Event>> evidence = new HashMap<>();
    List<Event> suspects = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (Event event : ordered) {
      int direction = direction(event.text());
      String point = km(event.crossing());
      if (event.at() < coverageStart
          || direction == 0
          || !route.crossings().contains(point)
          || !seen.add(event.id())) continue;
      String upstream = route.upstream(point, direction);
      if (upstream != null) {
        var queue = evidence.get(upstream + "|" + direction);
        if (queue == null || queue.isEmpty()) suspects.add(event);
        else queue.removeFirst();
      }
      evidence.computeIfAbsent(point + "|" + direction, k -> new ArrayDeque<>()).addLast(event);
    }
    return suspects;
  }

  /** Wait for another successful poll so late upstream uploads can disprove an early suspicion. */
  public static List<Event> confirmedSelfTriggers(
      List<Event> candidates, Map<String, Long> firstSeen, long now, long baseline, long delay) {
    Set<String> current = new HashSet<>();
    List<Event> confirmed = new ArrayList<>();
    for (Event event : candidates) {
      if (event.at() < baseline) continue;
      current.add(event.id());
      long first = firstSeen.computeIfAbsent(event.id(), k -> now);
      if (now - first >= delay)
        confirmed.add(
            new Event(
                "self:" + event.id(),
                event.at(),
                event.crossing(),
                "Nghi tự kích",
                true,
                List.of()));
    }
    firstSeen.keySet().retainAll(current);
    return confirmed;
  }

  /** Generic system restoration must never clear unrelated device faults. */
  public static String faultKey(String text) {
    String t = text.toLowerCase(Locale.ROOT);
    if (t.contains("220vac")) return "nguồn 220VAC";
    if (t.contains("kết nối giám sát")) return "kết nối giám sát";
    var voltage = java.util.regex.Pattern.compile("điện áp\\s+cảm biến\\s*(\\d+)").matcher(t);
    if (voltage.find()) return "điện áp cảm biến " + voltage.group(1);
    var m =
        java.util.regex.Pattern.compile(
                "(cảm biến|cần chắn|chuông|đèn)(?:\\s+(?:điện|cảnh báo|tín hiệu|đỏ|số))*\\s*(\\d+)")
            .matcher(t);
    if (m.find()) return m.group(1) + " " + m.group(2);
    for (String family : List.of("chuông", "đèn")) {
      int start = t.indexOf(family);
      if (start >= 0) {
        String device =
            t.substring(start).split("\\s+(?:hỏng|khôi phục|mất|bất thường|bị)\\b", 2)[0].trim();
        return device.isEmpty() ? family : device;
      }
    }
    return t;
  }

  public static long faultBaseline(long previous, long now) {
    long midnight =
        java.time.Instant.ofEpochMilli(now)
            .atZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
            .toLocalDate()
            .atStartOfDay(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
            .toInstant()
            .toEpochMilli();
    return Math.max(previous, midnight);
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
      String key = km(event.crossing()) + "|" + eventFaultKey(event);
      if (event.red()) {
        open.computeIfAbsent(key, k -> new ArrayList<>()).add(result.size());
        result.add(new Incident(event, null));
      } else if (event.text().toLowerCase(Locale.ROOT).contains("khôi phục tốt")
          || event.text().trim().equalsIgnoreCase("Khôi phục hệ thống")) {
        List<String> matches = new ArrayList<>();
        if (event.text().trim().equalsIgnoreCase("Khôi phục hệ thống"))
          matches.add(km(event.crossing()) + "|@self");
        else matches.add(key);
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
      String k = km(e.crossing()) + "|" + eventFaultKey(e);
      if (e.red()) faults.put(k, e);
      else if (e.text().trim().equalsIgnoreCase("Khôi phục hệ thống")) {
        faults.remove(km(e.crossing()) + "|@self");
      } else if (e.text().toLowerCase(Locale.ROOT).contains("khôi phục tốt")) {
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
