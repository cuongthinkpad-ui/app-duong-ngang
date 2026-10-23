package vn.duongngang.monitor;

import java.util.*;

/** Domain rules independent of Android. All timestamps are epoch milliseconds. */
public final class Rules {
  private static final Set<Long> AXLE_EXEMPT_CHAINAGES =
      Set.of(280500L, 307507L, 309056L, 320445L, 338900L, 343350L);
  private static final java.util.regex.Pattern POINT =
      java.util.regex.Pattern.compile(
          "(?iu)^(?:km\\s*)?(\\d+)\\s*\\+\\s*(\\d{1,3})(?:\\s*\\(\\s*([^()]+?)\\s*\\))?$");
  private static final java.util.regex.Pattern WHITESPACE = java.util.regex.Pattern.compile("\\s+");
  private static final java.util.regex.Pattern VOLTAGE_DEVICE =
      java.util.regex.Pattern.compile("điện áp\\s+cảm biến\\s*(\\d+)");
  private static final java.util.regex.Pattern NUMBERED_DEVICE =
      java.util.regex.Pattern.compile(
          "(cảm biến|cần chắn|chuông|đèn)(?:\\s+(?:điện|cảnh báo|tín hiệu|đỏ|số))*\\s*(\\d+)");
  private static final java.util.regex.Pattern DEVICE_STATE =
      java.util.regex.Pattern.compile("\\s+(?:hỏng|khôi phục|mất|bất thường|bị)\\b");

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

    /** Neighbor for drawing, including stations; null represents the edge of the account route. */
    public String adjacent(String crossing, int step) {
      if (step != 1 && step != -1 || !crossings.contains(km(crossing))) return null;
      int neighbor = points.indexOf(km(crossing)) + step;
      return neighbor < 0 || neighbor >= points.size() ? null : points.get(neighbor);
    }

    public String upstream(String crossing, int direction) {
      String previous = adjacent(crossing, -direction);
      return previous == null || isStation(previous) ? null : previous;
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

  /** The operator's reset ends a self-trigger, not unrelated hardware faults. */
  public static boolean isManualRecovery(String text) {
    return WHITESPACE.matcher(text.trim()).replaceAll(" ").equalsIgnoreCase("Ấn nút Phục hồi");
  }

  private static int reportPhase(Event event) {
    if (direction(event.text()) != 0) return 0;
    if (event.red()) return 1;
    return passedDirection(event.text()) != 0 ? 2 : 3;
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
      boolean reached,
      String successorId,
      long lastAt,
      boolean selfTriggered) {
    public Journey(
        String id,
        String from,
        String to,
        int direction,
        long at,
        boolean passed,
        boolean released,
        boolean reached,
        String successorId,
        long lastAt) {
      this(id, from, to, direction, at, passed, released, reached, successorId, lastAt, false);
    }

    public Journey(
        String id,
        String from,
        String to,
        int direction,
        long at,
        boolean passed,
        boolean released,
        boolean reached) {
      this(id, from, to, direction, at, passed, released, reached, null, at);
    }
  }

  public record TravelState(List<Journey> journeys) {
    public TravelState {
      journeys = List.copyOf(journeys);
    }

    public static TravelState empty() {
      return new TravelState(List.of());
    }
  }

  public record Between(String from, String to, int direction, long at) {}

  /** Exactly one incoming or outgoing interval per unmatched journey, with nullable route edges. */
  public record TrackSegment(String from, String to, int direction) {}

  public static List<TrackSegment> trackSegments(Route route, TravelState state) {
    Set<TrackSegment> segments = new LinkedHashSet<>();
    for (Journey j : state.journeys()) {
      // The downstream report has taken ownership of this train's one visible interval.
      if (j.reached() || !route.crossings().contains(j.from())) continue;
      if (!j.passed() && !j.released())
        segments.add(
            new TrackSegment(route.adjacent(j.from(), -j.direction()), j.from(), j.direction()));
      // Crossing-to-crossing continuity survives source restoration. Station/route-exit
      // intervals last only until the adjoining crossing restores; station logs are ignored.
      else if (j.passed() && (!j.released() || j.to() != null))
        segments.add(
            new TrackSegment(j.from(), route.adjacent(j.from(), j.direction()), j.direction()));
    }
    return new ArrayList<>(segments);
  }

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
      if (!j.reached() && !j.passed() && !j.released())
        result.add(new Occupancy(j.from(), j.direction(), j.at()));
    return result;
  }

  private static Journey progress(
      Journey j, boolean passed, boolean released, boolean reached, String successor, long at) {
    return new Journey(
        j.id(),
        j.from(),
        j.to(),
        j.direction(),
        j.at(),
        passed,
        released,
        reached,
        successor,
        Math.max(j.lastAt(), at),
        j.selfTriggered());
  }

  private static Journey latestJourney(
      Map<String, Journey> journeys, String point, int direction, boolean unreleasedOnly) {
    Journey result = null;
    for (Journey j : journeys.values())
      if (j.from().equals(point)
          && (direction == 0 || j.direction() == direction)
          && (!unreleasedOnly || !j.released())
          && (result == null || j.at() >= result.at())) result = j;
    return result;
  }

  private static boolean claimed(Map<String, Journey> journeys, String id) {
    for (Journey j : journeys.values()) if (id.equals(j.successorId())) return true;
    return false;
  }

  private static boolean freshUpstream(
      Map<String, Journey> journeys, String point, int direction, long after, long before) {
    for (Journey j : journeys.values())
      if (!j.reached()
          && point.equals(j.to())
          && j.direction() == direction
          && j.at() > after
          && j.at() <= before) return true;
    return false;
  }

  /** A passage is also evidence of arrival; do not require its separate approach message. */
  private static void linkPredecessor(Map<String, Journey> journeys, Journey current) {
    if (claimed(journeys, current.id())) return;
    Journey previous = null;
    for (Journey j : journeys.values())
      if (current.from().equals(j.to())
          && current.direction() == j.direction()
          && !j.reached()
          && j.at() <= current.at()
          && (previous == null || j.at() < previous.at())) previous = j;
    if (previous != null)
      journeys.put(
          previous.id(),
          progress(
              previous, previous.passed(), previous.released(), true, current.id(), current.at()));
  }

  /** Neighboring approach sensors can report in the reverse order while both zones overlap. */
  private static void linkEarlierDownstream(Map<String, Journey> journeys, Journey current) {
    if (current.to() == null || current.reached()) return;
    Journey downstream = null;
    for (Journey j : journeys.values())
      if (current.to().equals(j.from())
          && current.direction() == j.direction()
          && !j.passed()
          && !j.released()
          && !j.reached()
          && j.at() <= current.at()
          // Only reconcile nearby detection timestamps; do not merge a later train into
          // an old unfinished visit. This does not expire an existing pending interval.
          && current.at() - j.at() <= 120000L
          && !claimed(journeys, j.id())
          && (downstream == null || j.at() > downstream.at())) downstream = j;
    if (downstream != null)
      journeys.put(
          current.id(),
          progress(
              current, current.passed(), current.released(), true, downstream.id(), current.at()));
  }

  /** New directional evidence at a crossing supersedes older opposite intervals touching it. */
  private static void retireContradictions(
      Route route, Map<String, Journey> journeys, String point, int direction, long at) {
    for (Journey j : new ArrayList<>(journeys.values())) {
      if (j.reached() || j.direction() == direction || j.at() > at) continue;
      String neighbor = route.adjacent(j.from(), j.passed() ? j.direction() : -j.direction());
      if (point.equals(j.from()) || point.equals(neighbor))
        journeys.put(j.id(), progress(j, j.passed(), true, true, null, at));
    }
  }

  /** Keep pending trains and bounded completion records, so late passages cannot resurrect them. */
  private static TravelState compactJourneys(Map<String, Journey> journeys) {
    Map<String, Journey> latestClosed = new HashMap<>();
    Set<String> live = new HashSet<>();
    for (Journey j : journeys.values()) {
      if (!j.reached()) live.add(j.id());
      else {
        String key = j.from() + "|" + j.direction();
        Journey previous = latestClosed.get(key);
        if (previous == null || j.at() >= previous.at()) latestClosed.put(key, j);
      }
    }
    List<Journey> result = new ArrayList<>();
    for (Journey j : journeys.values())
      if (!j.reached()
          || latestClosed.get(j.from() + "|" + j.direction()) == j
          || (j.successorId() != null && live.contains(j.successorId()))) result.add(j);
    return new TravelState(result);
  }

  /** Event-based continuity with explicit successor links and completion records, not GPS. */
  public static TravelState travelState(Route route, TravelState initial, List<Event> input) {
    List<Event> ordered = new ArrayList<>(input);
    ordered.sort(
        Comparator.comparingLong(Event::at)
            .thenComparingInt(Rules::reportPhase)
            .thenComparingLong(
                e ->
                    isPoint(e.crossing())
                        ? chainage(km(e.crossing())) * (direction(e.text()) == -1 ? -1 : 1)
                        : 0)
            .thenComparing(Event::id));
    Map<String, Journey> journeys = new LinkedHashMap<>();
    for (Journey j : initial.journeys())
      if ((j.direction() == 1 || j.direction() == -1)
          && route.crossings().contains(j.from())
          && Objects.equals(j.to(), route.upstream(j.from(), -j.direction())))
        journeys.put(j.id(), j);
    Set<String> seen = new HashSet<>(journeys.keySet());
    for (Event e : ordered) {
      String point = km(e.crossing());
      if (!seen.add(e.id()) || !route.crossings().contains(point)) continue;
      if (isSelfTrigger(e)) {
        Journey j = journeys.get(e.id().substring(5));
        if (j != null && j.from().equals(point))
          journeys.put(
              j.id(),
              new Journey(
                  j.id(),
                  j.from(),
                  j.to(),
                  j.direction(),
                  j.at(),
                  j.passed(),
                  j.released(),
                  j.reached(),
                  j.successorId(),
                  j.lastAt(),
                  true));
        continue;
      }
      if (e.red()) continue;
      int d = direction(e.text()), passed = passedDirection(e.text());
      if (isManualRecovery(e.text())) {
        for (Journey j : new ArrayList<>(journeys.values()))
          if (j.from().equals(point) && j.selfTriggered() && !j.reached() && j.at() <= e.at())
            journeys.put(j.id(), progress(j, j.passed(), true, true, j.successorId(), e.at()));
      } else if (d != 0) {
        retireContradictions(route, journeys, point, d, e.at());
        for (Journey j : new ArrayList<>(journeys.values()))
          if (j.from().equals(point) && !j.released()) {
            if (j.reached() || !j.passed() || j.direction() != d)
              journeys.put(j.id(), progress(j, j.passed(), true, true, j.successorId(), e.at()));
            else journeys.put(j.id(), progress(j, true, true, false, null, e.at()));
          }
        Journey current =
            new Journey(e.id(), point, route.upstream(point, -d), d, e.at(), false, false, false);
        journeys.put(current.id(), current);
        linkPredecessor(journeys, current);
        linkEarlierDownstream(journeys, current);
      } else if (passed != 0 || e.text().trim().equalsIgnoreCase("Khôi phục hệ thống")) {
        Journey current = latestJourney(journeys, point, passed, passed == 0);
        if (passed != 0
            && current != null
            && current.released()
            && freshUpstream(journeys, point, passed, current.at(), e.at())) current = null;
        if (current == null && passed != 0) {
          Journey latest = latestJourney(journeys, point, 0, false);
          if (latest != null
              && latest.direction() != passed
              && !freshUpstream(journeys, point, passed, latest.at(), e.at())) continue;
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
          journeys.put(current.id(), current);
        }
        if (current == null) continue;
        if (passed != 0 && !current.released() && !current.reached()) {
          retireContradictions(route, journeys, point, passed, e.at());
          linkPredecessor(journeys, current);
          linkEarlierDownstream(journeys, current);
          current = journeys.get(current.id());
        }
        boolean released = current.released() || passed == 0;
        boolean closed = current.reached() || (released && current.to() == null);
        journeys.put(
            current.id(), progress(current, true, released, closed, current.successorId(), e.at()));
      }
      if (journeys.size() > route.crossings().size() * 4 + 16) {
        List<Journey> retained = compactJourneys(journeys).journeys();
        journeys.clear();
        for (Journey j : retained) journeys.put(j.id(), j);
      }
    }
    return compactJourneys(journeys);
  }

  /** Owner-specified exclusions from the app's six-sensor axle comparison, across accounts. */
  public static boolean isAxleExempt(String crossing) {
    return isPoint(crossing) && AXLE_EXEMPT_CHAINAGES.contains(chainage(km(crossing)));
  }

  public static String axleNote(Event event) {
    return !isAxleExempt(event.crossing())
            && event.text().trim().equalsIgnoreCase("Khôi phục hệ thống")
        ? axleNote(event.counts())
        : "";
  }

  public static String axleNote(List<String> raw) {
    AxleComparison comparison = compareAxles(raw);
    if (comparison == null) return "";
    if (comparison.standard() == null) return "cảm biến đếm trục không đồng nhất, lỗi";
    List<String> notes = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      int delta = comparison.counts().get(i) - comparison.standard();
      if (delta != 0) notes.add("CB" + (i + 1) + " " + axleDifference(delta));
    }
    return String.join(" · ", notes);
  }

  public record AxleComparison(List<Integer> counts, Integer standard) {}

  /** null means invalid/voltage data; a null standard means a tied comparison. */
  public static AxleComparison compareAxles(List<String> raw) {
    if (raw.size() != 6) return null;
    List<Integer> values = new ArrayList<>();
    Map<Integer, Integer> frequencies = new HashMap<>();
    for (String s : raw) {
      if (!s.trim().matches("\\d+")) return null;
      try {
        int n = Integer.parseInt(s.trim());
        values.add(n);
        frequencies.merge(n, 1, Integer::sum);
      } catch (NumberFormatException e) {
        return null;
      }
    }
    int max = Collections.max(frequencies.values());
    List<Integer> modes = new ArrayList<>();
    frequencies.forEach(
        (n, c) -> {
          if (c == max) modes.add(n);
        });
    return new AxleComparison(
        List.copyOf(values), modes.size() == 1 && max >= 2 ? modes.get(0) : null);
  }

  public static String axleDifference(int delta) {
    return (delta > 0 ? "thừa " : "thiếu ") + Math.abs(delta) + " trục";
  }

  public record AxleError(
      Event source,
      int sensor,
      int standard,
      int observed,
      int delta,
      int dailyCount,
      long missing,
      long extra) {}

  /** Count distinct restore measurements per crossing, sensor and Vietnamese calendar day. */
  public static List<AxleError> axleErrors(Route route, List<Event> input) {
    List<Event> ordered = new ArrayList<>(input);
    ordered.sort(Comparator.comparingLong(Event::at).thenComparing(Event::id));
    Set<String> seen = new HashSet<>();
    Map<String, long[]> totals = new HashMap<>();
    List<AxleError> out = new ArrayList<>();
    for (Event e : ordered) {
      if (e.red()
          || isAxleExempt(e.crossing())
          || !e.text().trim().equalsIgnoreCase("Khôi phục hệ thống")
          || !route.crossings().contains(km(e.crossing()))
          || !seen.add(e.id())) continue;
      AxleComparison c = compareAxles(e.counts());
      if (c == null || c.standard() == null) continue;
      long day = faultBaseline(0, e.at());
      for (int i = 0; i < 6; i++) {
        int delta = c.counts().get(i) - c.standard();
        if (delta == 0) continue;
        String key = day + "|" + km(e.crossing()) + "|" + i;
        long[] count = totals.computeIfAbsent(key, k -> new long[3]);
        if (Math.abs(delta) >= 3) count[0]++;
        if (delta < 0) count[1] -= delta;
        else count[2] += delta;
        out.add(
            new AxleError(
                e,
                i + 1,
                c.standard(),
                c.counts().get(i),
                delta,
                (int) count[0],
                count[1],
                count[2]));
      }
    }
    return out;
  }

  public static boolean isAxleFault(Event event) {
    String id = event.id();
    return id.length() >= 7
        && id.startsWith("axle:")
        && id.charAt(5) >= '1'
        && id.charAt(5) <= '6'
        && id.charAt(6) == ':';
  }

  public static boolean isGenerated(Event event) {
    return isSelfTrigger(event) || isAxleFault(event);
  }

  public static boolean isExcludedAxleFault(Event event) {
    return isAxleFault(event) && isAxleExempt(event.crossing());
  }

  public static boolean isLargeAxleFault(Event event) {
    if (!isAxleFault(event)) return false;
    var comparison = compareAxles(event.counts());
    return comparison != null
        && comparison.standard() != null
        && Math.abs(comparison.counts().get(event.id().charAt(5) - '1') - comparison.standard())
            >= 10;
  }

  /** Sixth error and each single deviation of at least ten axles are new alerts, not reminders. */
  public static List<Event> axleAlerts(Route route, List<Event> input) {
    List<Event> alerts = new ArrayList<>();
    for (AxleError e : axleErrors(route, input)) {
      boolean large = Math.abs(e.delta()) >= 10;
      if (!large && (Math.abs(e.delta()) < 3 || e.dailyCount() != 6)) continue;
      String text =
          "CB"
              + e.sensor()
              + (large
                  ? " đếm " + axleDifference(e.delta()) + " trong 1 lần"
                  : " đếm sai 6 lần trong ngày · Lần thứ 6 " + axleDifference(e.delta()))
              + " · Chuẩn "
              + e.standard()
              + ", đếm "
              + e.observed();
      if (large && e.dailyCount() >= 6) text += " · Sai " + e.dailyCount() + " lần trong ngày";
      alerts.add(
          new Event(
              "axle:" + e.sensor() + ":" + e.source().id(),
              e.source().at(),
              km(e.source().crossing()),
              text,
              true,
              e.source().counts()));
    }
    return alerts;
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
    return isSelfTrigger(event)
        ? "@self"
        : isAxleFault(event)
            ? (isLargeAxleFault(event)
                ? "@axleSingle:" + event.id()
                : "@axleDaily:" + event.id().charAt(5))
            : faultKey(event.text());
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
    var voltage = VOLTAGE_DEVICE.matcher(t);
    if (voltage.find()) return "điện áp cảm biến " + voltage.group(1);
    var m = NUMBERED_DEVICE.matcher(t);
    if (m.find()) return m.group(1) + " " + m.group(2);
    for (String family : List.of("chuông", "đèn")) {
      int start = t.indexOf(family);
      if (start >= 0) {
        String device = DEVICE_STATE.split(t.substring(start), 2)[0].trim();
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

  private static boolean isFaultRelevant(Event event) {
    return event.red()
        || event.text().toLowerCase(Locale.ROOT).contains("khôi phục tốt")
        || event.text().trim().equalsIgnoreCase("Khôi phục hệ thống")
        || isManualRecovery(event.text());
  }

  /** Pair only explicit restoration messages with earlier faults of that device and crossing. */
  public static List<Incident> incidentHistory(List<Event> input) {
    List<Event> ordered = new ArrayList<>();
    for (var event : input) if (isFaultRelevant(event)) ordered.add(event);
    ordered.sort(Comparator.comparingLong(Event::at).thenComparingInt(Rules::reportPhase));
    List<Incident> result = new ArrayList<>();
    Map<String, List<Integer>> open = new HashMap<>();
    for (Event event : ordered) {
      if (isExcludedAxleFault(event)) continue;
      String key = km(event.crossing()) + "|" + eventFaultKey(event);
      if (event.red()) {
        open.computeIfAbsent(key, k -> new ArrayList<>()).add(result.size());
        result.add(new Incident(event, null));
      } else if (event.text().toLowerCase(Locale.ROOT).contains("khôi phục tốt")
          || event.text().trim().equalsIgnoreCase("Khôi phục hệ thống")
          || isManualRecovery(event.text())) {
        List<String> matches = new ArrayList<>();
        if (event.text().trim().equalsIgnoreCase("Khôi phục hệ thống")
            || isManualRecovery(event.text())) matches.add(km(event.crossing()) + "|@self");
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
    List<Event> sorted = new ArrayList<>();
    for (var event : input) if (isFaultRelevant(event)) sorted.add(event);
    sorted.sort(Comparator.comparingLong(Event::at).thenComparingInt(Rules::reportPhase));
    Map<String, Event> faults = new LinkedHashMap<>();
    for (Event e : sorted) {
      if (isExcludedAxleFault(e)) continue;
      String k = km(e.crossing()) + "|" + eventFaultKey(e);
      if (e.red()) faults.put(k, e);
      else if (e.text().trim().equalsIgnoreCase("Khôi phục hệ thống")
          || isManualRecovery(e.text())) {
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
