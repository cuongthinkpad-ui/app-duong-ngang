import java.util.*;
import vn.duongngang.monitor.Rules;

public class RulesTest {
  static int scenarios = 0;

  static void check(boolean b, String message) {
    scenarios++;
    if (!b) throw new AssertionError(message);
  }

  static Rules.Event event(long t, String km, String text, boolean red) {
    return new Rules.Event(km + t + text, t, km, text, red, List.of());
  }

  static Rules.Event axle(long t, String km, int sensor, int delta) {
    List<String> counts = new ArrayList<>(Collections.nCopies(6, "40"));
    counts.set(sensor - 1, "" + (40 + delta));
    return new Rules.Event(
        "measurement:" + km + ":" + t, t, km, "Khôi phục hệ thống", false, counts);
  }

  static void oneSegment(
      Rules.Route route,
      Rules.TravelState state,
      String from,
      String to,
      int direction,
      String message) {
    check(
        Rules.trackSegments(route, state)
            .equals(List.of(new Rules.TrackSegment(from, to, direction))),
        message);
  }

  public static void main(String[] args) {
    check(
        Rules.axleNote(List.of("46", "46", "48", "44", "46", "46"))
            .equals("CB3 thừa 2 trục · CB4 thiếu 2 trục"),
        "mixed axle deviations");
    check(
        Rules.axleNote(List.of("46", "46", "48", "48", "50", "50"))
            .equals("cảm biến đếm trục không đồng nhất, lỗi"),
        "tie");
    check(
        Rules.axleNote(List.of("4.6V", "4.6V", "4.8V", "4.4V", "4.6V", "4.6V")).isEmpty(),
        "voltage ignored");
    for (String first : List.of("312+111", "320+445"))
      check(Rules.DEMO_ROUTE.upstream(first, 1) == null, "north boundary");
    for (String last : List.of("314+550", "334+830"))
      check(Rules.DEMO_ROUTE.upstream(last, -1) == null, "south boundary");
    check(Rules.DEMO_ROUTE.upstream("328+325", -1) == null, "YX station breaks south adjacency");
    check(Rules.DEMO_ROUTE.upstream("332+350", 1) == null, "YX station breaks north adjacency");
    var current = event(10000, "313+456", "Tàu lẻ", false);
    check(Rules.suspect(Rules.DEMO_ROUTE, current, List.of(), 0, 5000, true), "missing previous");
    check(
        !Rules.suspect(
            Rules.DEMO_ROUTE,
            current,
            List.of(event(8000, "312+111", "Tàu lẻ", false)),
            0,
            5000,
            true),
        "valid train");
    check(
        !Rules.suspect(Rules.DEMO_ROUTE, current, List.of(), 9000, 5000, true),
        "startup uncertainty");
    check(!Rules.suspect(Rules.DEMO_ROUTE, current, List.of(), 0, 5000, false), "pagination gap");
    check(
        !Rules.suspect(
            Rules.DEMO_ROUTE, event(10000, "312+111", "Tàu lẻ", false), List.of(), 0, 5000, true),
        "entry allowed");
    var faults =
        List.of(
            event(1, "328+325", "Mất nguồn xoay chiều 220VAC", true),
            event(2, "328+325", "Cần chắn 1 mất trạng thái", true),
            event(3, "328+325", "Khôi phục hệ thống", false),
            event(4, "328+325", "Nguồn xoay chiều 220VAC khôi phục tốt", false));
    check(Rules.activeFaults(faults).size() == 1, "restore only matching fault");
    var restored =
        List.of(
            event(1, "321+780", "Cần chắn 1 mất trạng thái", true),
            event(2, "321+780", "Cần chắn 2 mất trạng thái", true),
            event(3, "321+780", "Mất nguồn xoay chiều 220VAC", true),
            event(4, "321+780", "Cần chắn khôi phục tốt", false));
    check(
        Rules.activeFaults(restored).size() == 1
            && Rules.activeFaults(restored).get(0).text().contains("220VAC"),
        "collective barrier restore only clears barriers");
    var zone = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
    long before =
        java.time.LocalDateTime.of(2026, 10, 21, 23, 55).atZone(zone).toInstant().toEpochMilli();
    long after = before + 10 * 60 * 1000;
    check(Rules.needsNewBaseline(before, after, true), "restart across date resets");
    check(
        !Rules.needsNewBaseline(before, after, false),
        "continuous midnight monitoring does not reset");
    check(!Rules.needsNewBaseline(0, after, true), "first connection has no stale baseline");
    check(
        !Rules.needsNewBaseline(after, after + 60 * 1000, true),
        "same day short restart preserves state");
    check(Rules.needsNewBaseline(before, before + 25L * 60 * 60 * 1000, false), "long gap resets");
    var incidentList = Rules.incidentHistory(faults);
    check(incidentList.size() == 2, "incident history retains both faults");
    check(
        incidentList.stream().filter(i -> i.recovery() != null).count() == 1,
        "only matching voltage fault restored");
    check(
        incidentList.stream()
            .filter(i -> i.fault().text().contains("Cần chắn"))
            .allMatch(i -> i.recovery() == null),
        "system restoration does not mark barrier green");
    var cycle =
        Rules.incidentHistory(
            List.of(
                event(1, "328+325", "Điện áp cảm biến 6 < 5V", true),
                event(2, "328+325", "Điện áp cảm biến 6 khôi phục tốt", false),
                event(3, "328+325", "Điện áp cảm biến 6 < 5V", true)));
    check(
        cycle.size() == 2 && cycle.get(0).recovery() == null && cycle.get(1).recovery() != null,
        "recurrence starts a separate unresolved incident");
    check(Rules.DEMO_ROUTE.inRange("Km 320+957", "320+445", "321+780"), "middle crossing included");
    check(
        Rules.DEMO_ROUTE.inRange("320+445", "320+445", "321+780")
            && Rules.DEMO_ROUTE.inRange("321+780", "320+445", "321+780"),
        "both endpoints included");
    check(!Rules.DEMO_ROUTE.inRange("328+325", "320+445", "321+780"), "outside crossing excluded");
    check(!Rules.DEMO_ROUTE.validRange("321+780", "320+445"), "reversed range rejected");
    check(
        Rules.DEMO_ROUTE.routeSpan("314+550", "320+445").size() == 3,
        "station included between selected boundaries");
    check(
        Rules.DEMO_ROUTE.routeSpan("321+780", "321+780").equals(List.of("321+780")),
        "single crossing range supported");
    var other =
        new Rules.Route(
            List.of(
                "Km 311+400",
                "Km 279+000 (CS)",
                "Km 276+351",
                "Km 275+290",
                "Km 280+500",
                "Km 291+600 (MLy)",
                "Km 287+348",
                "Km 300+600",
                "Km 308+210 (QHa)",
                "Km 307+507",
                "Km 310+644"));
    check(
        other.first().equals("275+290") && other.last().equals("311+400"),
        "new account numeric ordering");
    check(
        other.points().size() == 11 && other.crossings().size() == 8,
        "stations excluded from road crossings");
    check(
        Rules.stationCode("Km 291+600 (MLy)").equals("MLy"), "preserve station abbreviation case");
    check(Rules.km(" Km 279 + 000  ( CS ) ").equals("279+000 (CS)"), "normalize label whitespace");
    check(
        !Rules.isStation("280+500") && Rules.stationCode("280+500").isEmpty(),
        "crossing has no station code");
    check(
        !Rules.isPoint("Km 279+000 (CS") && !Rules.isPoint("-- Tất cả --"),
        "reject broken station suffix");
    check(other.upstream("276+351", 1).equals("275+290"), "new account upstream le");
    check(other.upstream("275+290", -1).equals("276+351"), "new account upstream chan");
    check(
        other.upstream("276+351", -1) == null && other.upstream("280+500", 1) == null,
        "CS splits both directions");
    check(
        other.upstream("287+348", -1) == null && other.upstream("300+600", 1) == null,
        "MLy splits both directions");
    check(
        other.upstream("307+507", -1) == null && other.upstream("310+644", 1) == null,
        "QHa splits both directions");
    check(
        other.upstream("275+290", 1) == null && other.upstream("311+400", -1) == null,
        "new route outer boundaries");
    check(
        other.upstream("279+000 (CS)", 1) == null && other.upstream("276+351", 0) == null,
        "no station or invalid direction evidence");
    check(
        !other.validRange("279+000 (CS)", "280+500"), "cannot select station as crossing boundary");
    check(other.inRange("280+500", "276+351", "307+507"), "dynamic range crossing included");
    check(!other.inRange("313+456", "276+351", "307+507"), "old account excluded");
    check(
        other.routeSpan("276+351", "280+500").contains("279+000 (CS)"), "range spanning a station");
    check(
        !Rules.suspect(other, event(10000, "280+500", "Tàu lẻ", false), List.of(), 0, 5000, true),
        "station entry not self trigger");
    check(
        Rules.suspect(other, event(10000, "311+400", "Tàu lẻ", false), List.of(), 0, 5000, true),
        "new route missing preceding crossing");
    check(
        !Rules.suspect(
            other,
            event(10000, "311+400", "Tàu lẻ", false),
            List.of(event(8000, "310+644", "Tàu lẻ", false)),
            0,
            5000,
            true),
        "new route valid train");
    check(
        Rules.suspect(
            other,
            event(10000, "311+400", "Tàu lẻ", false),
            List.of(event(8000, "310+644", "Tàu chẵn", false)),
            0,
            5000,
            true),
        "opposite direction is not evidence");
    var singleton = new Rules.Route(List.of("100+010"));
    check(
        singleton.validRange("100+010", "100+010") && singleton.upstream("100+010", 1) == null,
        "single crossing account");
    var numeric = new Rules.Route(List.of("100+001", "9+100", "10+900", "10+010"));
    check(
        numeric.points().equals(List.of("9+100", "10+010", "10+900", "100+001")),
        "sort numerically not alphabetically");
    boolean conflict = false;
    try {
      new Rules.Route(List.of("279+000", "279+000 (CS)"));
    } catch (IllegalArgumentException expected) {
      conflict = true;
    }
    check(conflict, "conflicting station and crossing rejected");
    check(
        !new Rules.Route(List.of()).validRange("312+111", "334+830"),
        "no fallback to demo on empty operational route");
    var bells =
        List.of(
            event(1, "276+351", "Chuông 1 hỏng", true),
            event(2, "276+351", "Chuông 2 hỏng", true),
            event(3, "276+351", "Chuông 2 khôi phục tốt", false));
    check(
        Rules.activeFaults(bells).size() == 1
            && Rules.activeFaults(bells).get(0).text().equals("Chuông 1 hỏng"),
        "bell restoration cannot clear another numbered bell");
    check(
        Rules.incidentHistory(bells).stream().filter(i -> i.recovery() != null).count() == 1,
        "numbered bell incident pairing");
    var lamps =
        List.of(
            event(1, "276+351", "Đèn cảnh báo 1 hỏng", true),
            event(2, "276+351", "Đèn cảnh báo 2 hỏng", true),
            event(3, "276+351", "Đèn cảnh báo 1 khôi phục tốt", false));
    check(
        Rules.activeFaults(lamps).size() == 1
            && Rules.activeFaults(lamps).get(0).text().equals("Đèn cảnh báo 2 hỏng"),
        "numbered lamps remain distinct");
    check(
        Rules.faultKey("Chuông cảnh báo số 2 hỏng").equals("chuông 2"),
        "number after device qualifiers");
    var reports =
        List.of(
            event(1, "320+445", "Tàu lẻ", false),
            event(2, "313+800", "Tàu chẵn", false),
            event(3, "320+957", "Tàu lẻ", false));
    var occupations = Rules.occupancies(Rules.DEMO_ROUTE, reports);
    check(
        occupations.size() == 3 && occupations.stream().anyMatch(t -> t.direction() == -1),
        "simultaneous occupation on several sections and both directions");
    check(
        occupations.stream().anyMatch(t -> t.crossing().equals("320+445")),
        "next crossing report does not clear previous crossing");
    var raised =
        Rules.occupancies(
            Rules.DEMO_ROUTE,
            List.of(
                event(1, "320+445", "Tàu lẻ", false),
                event(2, "320+445", "Tàu lẻ đã qua đường ngang", false),
                event(3, "320+445", "Cần chắn 1 đã nâng", false)));
    check(
        raised.size() == 1, "tail cleared and barrier raised do not restore whole monitored area");
    var restoredArea =
        Rules.occupancies(
            Rules.DEMO_ROUTE,
            List.of(
                event(1, "320+445", "Tàu lẻ", false),
                event(2, "320+957", "Tàu lẻ", false),
                event(3, "320+445", "Khôi phục hệ thống", false)));
    check(
        restoredArea.size() == 1 && restoredArea.get(0).crossing().equals("320+957"),
        "restoration clears only that crossing occupation");
    check(
        Rules.occupancies(other, List.of(event(1, "279+000 (CS)", "Tàu lẻ", false))).isEmpty(),
        "station messages never create or retain train marks");
    var duplicate = event(1, "320+445", "Tàu lẻ", false);
    check(
        Rules.occupancies(Rules.DEMO_ROUTE, List.of(duplicate, duplicate)).size() == 1,
        "overlapping pages cannot duplicate occupation");
    var nextTrain =
        Rules.occupancies(
            Rules.DEMO_ROUTE,
            List.of(
                event(1, "320+445", "Tàu lẻ", false),
                event(2, "320+445", "Khôi phục hệ thống", false),
                event(3, "320+445", "Tàu chẵn", false)));
    check(
        nextTrain.size() == 1 && nextTrain.get(0).direction() == -1,
        "new train replaces restored occupation direction");
    var bellOnly =
        Rules.occupancies(
            Rules.DEMO_ROUTE,
            List.of(
                event(1, "320+445", "Tàu lẻ", false),
                event(2, "320+445", "Chuông 1 khôi phục tốt", false)));
    check(bellOnly.size() == 1, "device recovery does not clear train occupation");
    var separate =
        List.of(
            event(1, "320+445", "Cảm biến 4 hỏng", true),
            event(2, "320+445", "Điện áp cảm biến 4 < 5V", true),
            event(3, "320+445", "Điện áp cảm biến 4 khôi phục tốt", false));
    check(
        Rules.activeFaults(separate).size() == 1
            && Rules.activeFaults(separate).get(0).text().equals("Cảm biến 4 hỏng"),
        "voltage restoration cannot clear failed sensor");
    check(
        Rules.incidentHistory(separate).stream().filter(i -> i.recovery() != null).count() == 1,
        "separate sensor incident groups");
    var measurement =
        new Rules.Event(
            "measurement",
            1,
            "320+445",
            "Đo điện áp cảm biến",
            false,
            List.of("58", "58", "57", "58", "58", "59"));
    check(
        Rules.axleNote(measurement).isEmpty(),
        "measurement text is never interpreted as axle counts");
    var axle =
        new Rules.Event(
            "axle",
            1,
            "320+957",
            "Khôi phục hệ thống",
            false,
            List.of("46", "46", "48", "44", "46", "46"));
    check(
        Rules.axleNote(axle).contains("CB3 thừa 2 trục"),
        "axle notes require system restoration record");
    var normalSequence =
        List.of(
            event(1, "320+445", "Tàu lẻ", false),
            event(2, "320+957", "Tàu lẻ", false),
            event(3, "321+780", "Tàu lẻ", false));
    check(
        Rules.suspectedSelfTriggers(Rules.DEMO_ROUTE, normalSequence, 0).isEmpty(),
        "whole normal chain has upstream evidence");
    var falseArrival = event(10, "321+780", "Tàu lẻ", false);
    check(
        Rules.suspectedSelfTriggers(Rules.DEMO_ROUTE, List.of(falseArrival), 0).size() == 1,
        "isolated internal arrival is suspected");
    check(
        Rules.suspectedSelfTriggers(
                Rules.DEMO_ROUTE, List.of(event(1, "320+445", "Tàu lẻ", false)), 0)
            .isEmpty(),
        "boundary entrance exempt");
    check(
        Rules.suspectedSelfTriggers(
                Rules.DEMO_ROUTE, List.of(event(1, "332+350", "Tàu lẻ", false)), 0)
            .isEmpty(),
        "station-side entrance exempt");
    check(
        Rules.suspectedSelfTriggers(
                Rules.DEMO_ROUTE, List.of(event(1, "319+200 (Vi)", "Tàu lẻ", false)), 0)
            .isEmpty(),
        "station events excluded");
    check(
        Rules.suspectedSelfTriggers(
                    Rules.DEMO_ROUTE,
                    List.of(event(1, "320+957", "Tàu chẵn", false), falseArrival),
                    0)
                .size()
            == 2,
        "opposite direction does not justify arrival");
    var repeated =
        List.of(
            event(1, "320+445", "Tàu lẻ", false),
            event(2, "320+957", "Tàu lẻ", false),
            event(3, "320+957", "Tàu lẻ", false));
    check(
        Rules.suspectedSelfTriggers(Rules.DEMO_ROUTE, repeated, 0).size() == 1,
        "one upstream passage cannot justify two arrivals");
    check(
        Rules.suspectedSelfTriggers(Rules.DEMO_ROUTE, List.of(falseArrival), 11).isEmpty(),
        "events outside known coverage excluded");
    var pending = new HashMap<String, Long>();
    check(
        Rules.confirmedSelfTriggers(List.of(falseArrival), pending, 1000, 0, 30000).isEmpty(),
        "first observation waits for next complete poll");
    check(
        Rules.confirmedSelfTriggers(List.of(falseArrival), pending, 30999, 0, 30000).isEmpty(),
        "confirmation grace respected");
    var confirmed = Rules.confirmedSelfTriggers(List.of(falseArrival), pending, 31000, 0, 30000);
    check(
        confirmed.size() == 1
            && confirmed.get(0).text().equals("Nghi tự kích")
            && confirmed.get(0).red(),
        "confirmed suspicion becomes red inferred event");
    Rules.confirmedSelfTriggers(List.of(), pending, 32000, 0, 30000);
    check(pending.isEmpty(), "late upstream evidence removes pending suspicion");
    check(
        Rules.confirmedSelfTriggers(List.of(falseArrival), new HashMap<>(), 1000, 11, 0).isEmpty(),
        "initial baseline does not replay old self triggers");
    var inferredRecovery =
        List.of(
            confirmed.get(0),
            event(11, "321+780", "Khôi phục hệ thống", false),
            event(1, "321+780", "Chuông 1 hỏng", true));
    check(
        Rules.activeFaults(inferredRecovery).size() == 1
            && !Rules.isSelfTrigger(Rules.activeFaults(inferredRecovery).get(0)),
        "system restores self trigger but keeps actual bell fault");
    check(
        Rules.incidentHistory(inferredRecovery).stream()
            .filter(i -> Rules.isSelfTrigger(i.fault()))
            .allMatch(i -> i.recovery() != null),
        "self trigger history shows real system recovery");
    var namedBells =
        List.of(
            event(1, "320+445", "Chuông phía lẻ hỏng", true),
            event(2, "320+445", "Chuông phía chẵn hỏng", true),
            event(3, "320+445", "Chuông phía lẻ khôi phục tốt", false));
    check(
        Rules.activeFaults(namedBells).size() == 1
            && Rules.activeFaults(namedBells).get(0).text().equals("Chuông phía chẵn hỏng"),
        "named bells remain distinct without assuming numeric identifiers");
    var namedLamps =
        List.of(
            event(1, "320+445", "Đèn A hỏng", true),
            event(2, "320+445", "Đèn B hỏng", true),
            event(3, "320+445", "Đèn B khôi phục tốt", false));
    check(
        Rules.activeFaults(namedLamps).size() == 1
            && Rules.activeFaults(namedLamps).get(0).text().equals("Đèn A hỏng"),
        "named lamp recovery does not clear a different lamp");
    long midnight = java.time.Instant.parse("2026-10-22T17:00:00Z").toEpochMilli();
    check(
        Rules.faultBaseline(0, midnight - 1) == midnight - 86400000L,
        "before Vietnamese midnight retains previous day");
    check(
        Rules.faultBaseline(0, midnight) == midnight,
        "reset at Vietnamese midnight rather than UTC midnight");
    check(
        Rules.faultBaseline(midnight + 5000, midnight + 6000) == midnight + 5000,
        "daily boundary does not rewind a later gap baseline");
    var line = new Rules.Route(List.of("100+000", "101+000", "102+000", "103+000"));
    var firstTrip =
        List.of(
            event(1000, "100+000", "Tàu lẻ", false),
            event(2000, "100+000", "Tàu lẻ đã qua đường ngang", false),
            event(3000, "100+000", "Khôi phục hệ thống", false));
    var trip = Rules.travelState(line, Rules.TravelState.empty(), firstTrip);
    check(
        Rules.occupancies(line, firstTrip).isEmpty() && Rules.betweenCrossings(trip).size() == 1,
        "source restoration leaves the between-crossing leg visible");
    check(
        Rules.betweenCrossings(trip).get(0).from().equals("100+000")
            && Rules.betweenCrossings(trip).get(0).to().equals("101+000"),
        "green leg ends exactly at adjacent markers");
    check(
        Rules.approachingCrossings(trip).isEmpty(),
        "passed source has no outside green approach stub");
    var idle = Rules.travelState(line, trip, List.of());
    check(
        Rules.betweenCrossings(idle).size() == 1,
        "long gap without destination report does not expire the leg");
    var arrived = Rules.travelState(line, trip, List.of(event(10000, "101+000", "Tàu lẻ", false)));
    check(
        Rules.betweenCrossings(arrived).isEmpty()
            && Rules.approachingCrossings(arrived).get(0).crossing().equals("101+000"),
        "destination approach consumes the source transfer and owns the incoming interval");
    var next =
        Rules.travelState(
            line, arrived, List.of(event(11000, "101+000", "Tàu lẻ đã qua đường ngang", false)));
    check(
        Rules.betweenCrossings(next).size() == 1
            && Rules.betweenCrossings(next).get(0).from().equals("101+000")
            && Rules.betweenCrossings(next).get(0).to().equals("102+000"),
        "passing next crossing moves continuity only to its next interval");
    var reverse =
        Rules.travelState(
            line,
            Rules.TravelState.empty(),
            List.of(
                event(1, "103+000", "Tàu chẵn", false),
                event(2, "103+000", "Tàu chẵn đã qua đường ngang", false),
                event(3, "103+000", "Khôi phục hệ thống", false)));
    check(
        Rules.betweenCrossings(reverse).get(0).to().equals("102+000")
            && Rules.betweenCrossings(reverse).get(0).direction() == -1,
        "even train continues towards lower chainage");
    var stationLine = new Rules.Route(List.of("100+000", "101+000 (Ga)", "102+000"));
    check(
        Rules.betweenCrossings(Rules.travelState(stationLine, Rules.TravelState.empty(), firstTrip))
            .isEmpty(),
        "station stops forward continuity");
    check(
        Rules.betweenCrossings(
                Rules.travelState(
                    stationLine,
                    Rules.TravelState.empty(),
                    List.of(
                        event(1, "102+000", "Tàu chẵn", false),
                        event(2, "102+000", "Tàu chẵn đã qua đường ngang", false))))
            .isEmpty(),
        "station stops reverse continuity");
    check(
        Rules.betweenCrossings(
                Rules.travelState(
                    line,
                    Rules.TravelState.empty(),
                    List.of(
                        event(1, "103+000", "Tàu lẻ", false),
                        event(2, "103+000", "Tàu lẻ đã qua đường ngang", false))))
            .isEmpty(),
        "route end has no pending transfer to another crossing");
    var overlapping =
        Rules.travelState(
            line,
            Rules.TravelState.empty(),
            List.of(
                event(1, "100+000", "Tàu lẻ", false), event(2, "101+000", "Tàu lẻ", false),
                event(3, "100+000", "Tàu lẻ đã qua đường ngang", false),
                    event(4, "100+000", "Khôi phục hệ thống", false)));
    check(
        Rules.betweenCrossings(overlapping).isEmpty(),
        "early downstream approach prevents phantom reopening after source passage");
    var both = new ArrayList<>(firstTrip);
    both.add(event(1001, "103+000", "Tàu chẵn", false));
    both.add(event(2001, "103+000", "Tàu chẵn đã qua đường ngang", false));
    check(
        Rules.betweenCrossings(Rules.travelState(line, Rules.TravelState.empty(), both)).size()
            == 2,
        "multiple trains on different intervals remain visible together");
    var following = new ArrayList<>(firstTrip);
    following.add(event(4000, "100+000", "Tàu lẻ", false));
    following.add(event(5000, "100+000", "Tàu lẻ đã qua đường ngang", false));
    following.add(event(6000, "100+000", "Khôi phục hệ thống", false));
    var followingState = Rules.travelState(line, Rules.TravelState.empty(), following);
    var once =
        Rules.travelState(line, followingState, List.of(event(7000, "101+000", "Tàu lẻ", false)));
    check(
        Rules.betweenCrossings(once).size() == 1,
        "one downstream arrival consumes only one of two pending trains");
    check(
        Rules.betweenCrossings(
                Rules.travelState(line, once, List.of(event(8000, "101+000", "Tàu lẻ", false))))
            .isEmpty(),
        "second downstream arrival consumes the remaining pending train");
    var shuffled = new ArrayList<>(both);
    Collections.reverse(shuffled);
    check(
        Rules.travelState(line, Rules.TravelState.empty(), both)
            .equals(Rules.travelState(line, Rules.TravelState.empty(), shuffled)),
        "late and out-of-order records replay chronologically");
    check(
        Rules.betweenCrossings(
                Rules.travelState(
                    line,
                    Rules.TravelState.empty(),
                    List.of(event(1, "100+000", "Khôi phục hệ thống", false))))
            .isEmpty(),
        "restoration without a train direction never invents a leg");
    check(
        Rules.betweenCrossings(
                    Rules.travelState(
                        line,
                        Rules.TravelState.empty(),
                        List.of(
                            event(1, "100+000", "Tàu lẻ", false),
                            event(2, "100+000", "Khôi phục hệ thống", false))))
                .size()
            == 1,
        "known arrival plus restoration maintains continuity if passage message is missing");
    check(
        Rules.betweenCrossings(
                    Rules.travelState(
                        line,
                        Rules.TravelState.empty(),
                        List.of(event(1, "100+000", "Tàu lẻ đã qua đường ngang", false))))
                .size()
            == 1,
        "explicit directional passage can seed continuity without an earlier arrival");
    check(Rules.selfTriggerEnabled(30), "self-trigger inference enabled at 30 seconds");
    check(!Rules.selfTriggerEnabled(120), "self-trigger inference disabled at two minutes");
    check(!Rules.selfTriggerEnabled(300), "self-trigger inference disabled at five minutes");
    var actual = Rules.DEMO_ROUTE;
    var stage =
        Rules.travelState(
            actual, Rules.TravelState.empty(), List.of(event(1, "312+111", "Tàu lẻ", false)));
    oneSegment(
        actual, stage, null, "312+111", 1, "odd approach lights only the north edge of 312+111");
    stage =
        Rules.travelState(
            actual, stage, List.of(event(2, "312+111", "Tàu lẻ đã qua đường ngang", false)));
    oneSegment(
        actual, stage, "312+111", "313+456", 1, "passage moves the one segment south of 312+111");
    stage =
        Rules.travelState(actual, stage, List.of(event(3, "312+111", "Khôi phục hệ thống", false)));
    oneSegment(actual, stage, "312+111", "313+456", 1, "restoration keeps only 312+111 to 313+456");
    stage = Rules.travelState(actual, stage, List.of(event(4, "313+456", "Tàu lẻ", false)));
    oneSegment(
        actual, stage, "312+111", "313+456", 1, "next approach keeps that same incoming segment");
    stage =
        Rules.travelState(
            actual, stage, List.of(event(5, "313+456", "Tàu lẻ đã qua đường ngang", false)));
    oneSegment(
        actual,
        stage,
        "313+456",
        "313+800",
        1,
        "313+456 passage transfers to 313+800 without lighting behind");
    var early =
        Rules.travelState(
            actual,
            Rules.TravelState.empty(),
            List.of(event(1, "312+111", "Tàu lẻ", false), event(2, "313+456", "Tàu lẻ", false)));
    oneSegment(
        actual,
        early,
        "312+111",
        "313+456",
        1,
        "overlapping approach zones still show only one interval");
    check(
        Rules.approachingCrossings(early).size() == 1,
        "upstream session yields to the downstream report");
    early =
        Rules.travelState(
            actual,
            early,
            List.of(
                event(3, "313+456", "Tàu lẻ đã qua đường ngang", false),
                event(4, "312+111", "Tàu lẻ đã qua đường ngang", false),
                event(5, "312+111", "Khôi phục hệ thống", false)));
    oneSegment(
        actual,
        early,
        "313+456",
        "313+800",
        1,
        "late upstream passage cannot draw a second interval behind the train");
    var even =
        Rules.travelState(
            actual, Rules.TravelState.empty(), List.of(event(1, "334+830", "Tàu chẵn", false)));
    oneSegment(actual, even, null, "334+830", -1, "even approach lights only the south edge");
    even =
        Rules.travelState(
            actual,
            even,
            List.of(
                event(2, "334+830", "Tàu chẵn đã qua đường ngang", false),
                event(3, "334+830", "Khôi phục hệ thống", false),
                event(4, "332+350", "Tàu chẵn", false)));
    oneSegment(
        actual,
        even,
        "334+830",
        "332+350",
        -1,
        "even arrival keeps its single northbound interval");
    even =
        Rules.travelState(
            actual, even, List.of(event(5, "332+350", "Tàu chẵn đã qua đường ngang", false)));
    oneSegment(
        actual,
        even,
        "332+350",
        "330+00 (YX)",
        -1,
        "even passage towards station shows the station-side interval");
    even =
        Rules.travelState(actual, even, List.of(event(6, "332+350", "Khôi phục hệ thống", false)));
    check(
        Rules.trackSegments(actual, even).isEmpty(),
        "station approach clears when the adjoining crossing restores");
    var intoVinh =
        Rules.travelState(
            actual,
            Rules.TravelState.empty(),
            List.of(
                event(1, "314+550", "Tàu lẻ", false),
                event(2, "314+550", "Tàu lẻ đã qua đường ngang", false)));
    oneSegment(
        actual,
        intoVinh,
        "314+550",
        "319+200 (Vi)",
        1,
        "odd train into Vinh remains on only the adjoining interval");
    intoVinh =
        Rules.travelState(
            actual, intoVinh, List.of(event(3, "314+550", "Khôi phục hệ thống", false)));
    check(
        Rules.trackSegments(actual, intoVinh).isEmpty(),
        "Vinh interval stops at restoration, without crossing the station");
    var outVinh =
        Rules.travelState(
            actual, Rules.TravelState.empty(), List.of(event(1, "320+445", "Tàu lẻ", false)));
    oneSegment(
        actual,
        outVinh,
        "319+200 (Vi)",
        "320+445",
        1,
        "odd train leaving Vinh lights station to first crossing");
    outVinh =
        Rules.travelState(
            actual, outVinh, List.of(event(2, "320+445", "Tàu lẻ đã qua đường ngang", false)));
    oneSegment(
        actual,
        outVinh,
        "320+445",
        "320+957",
        1,
        "leaving-station interval disappears after the crossing is passed");
    var backIntoVinh =
        Rules.travelState(
            actual,
            Rules.TravelState.empty(),
            List.of(
                event(1, "320+445", "Tàu chẵn", false),
                event(2, "320+445", "Tàu chẵn đã qua đường ngang", false)));
    oneSegment(
        actual,
        backIntoVinh,
        "320+445",
        "319+200 (Vi)",
        -1,
        "even train into Vinh lights only its station-side interval");
    var backOutVinh =
        Rules.travelState(
            actual, Rules.TravelState.empty(), List.of(event(1, "314+550", "Tàu chẵn", false)));
    oneSegment(
        actual,
        backOutVinh,
        "319+200 (Vi)",
        "314+550",
        -1,
        "even train leaving Vinh lights its incoming station-side interval");
    var stationOnly =
        Rules.travelState(
            actual,
            Rules.TravelState.empty(),
            List.of(
                event(1, "319+200 (Vi)", "Tàu lẻ", false),
                event(2, "319+200 (Vi)", "Đóng chắn tín hiệu gửi", false)));
    check(
        Rules.trackSegments(actual, stationOnly).isEmpty(),
        "station reports do not keep a train on the diagram all day");
    var dual =
        Rules.travelState(
            actual,
            Rules.TravelState.empty(),
            List.of(event(1, "312+111", "Tàu lẻ", false), event(2, "334+830", "Tàu chẵn", false)));
    check(
        Rules.trackSegments(actual, dual).size() == 2,
        "independent opposite trains each have one interval");
    var missingArrival =
        Rules.travelState(
            line, trip, List.of(event(10000, "101+000", "Tàu lẻ đã qua đường ngang", false)));
    oneSegment(
        line,
        missingArrival,
        "101+000",
        "102+000",
        1,
        "passage without destination approach still turns the previous interval off immediately");
    var latePass =
        Rules.travelState(
            line,
            Rules.TravelState.empty(),
            List.of(
                event(1, "100+000", "Tàu lẻ", false),
                event(2, "101+000", "Tàu lẻ", false),
                event(3, "100+000", "Khôi phục hệ thống", false),
                event(4, "100+000", "Tàu lẻ đã qua đường ngang", false)));
    oneSegment(
        line,
        latePass,
        "100+000",
        "101+000",
        1,
        "late passage after restoration cannot invent a second train");
    var reverseProof =
        Rules.travelState(line, trip, List.of(event(20000, "101+000", "Tàu chẵn", false)));
    oneSegment(
        line,
        reverseProof,
        "102+000",
        "101+000",
        -1,
        "new opposite evidence retires an old contradictory interval at that crossing");
    var inverted =
        Rules.travelState(
            line,
            Rules.TravelState.empty(),
            List.of(event(1, "101+000", "Tàu lẻ", false), event(2, "100+000", "Tàu lẻ", false)));
    oneSegment(
        line,
        inverted,
        "100+000",
        "101+000",
        1,
        "reverse-ordered neighboring approaches form one train instead of two");
    var protectedFollowing =
        Rules.travelState(
            line,
            Rules.TravelState.empty(),
            List.of(
                event(1, "100+000", "Tàu lẻ", false),
                event(2, "101+000", "Tàu lẻ", false),
                event(3, "100+000", "Tàu lẻ đã qua đường ngang", false),
                event(4, "100+000", "Khôi phục hệ thống", false),
                event(5, "100+000", "Tàu lẻ", false)));
    check(
        Rules.trackSegments(line, protectedFollowing).size() == 2,
        "explicit successor link prevents merging the following train into a claimed downstream"
            + " visit");
    var stalePass =
        Rules.travelState(
            line,
            reverseProof,
            List.of(event(21000, "100+000", "Tàu lẻ đã qua đường ngang", false)));
    oneSegment(
        line,
        stalePass,
        "102+000",
        "101+000",
        -1,
        "delayed old-direction passage cannot resurrect a contradicted visit");
    List<Rules.Event> replay = new ArrayList<>();
    Rules.TravelState successive = Rules.TravelState.empty();
    for (int run = 0; run < 24; run++) {
      int dir = run % 2 == 0 ? 1 : -1;
      String arrivalText = dir == 1 ? "Tàu lẻ" : "Tàu chẵn";
      long clock = 1000000L + run * 3600000L;
      List<String> way = new ArrayList<>(line.crossings());
      if (dir == -1) Collections.reverse(way);
      Rules.Event begin = event(clock, way.get(0), arrivalText, false);
      replay.add(begin);
      successive = Rules.travelState(line, successive, List.of(begin));
      for (int stop = 0; stop < way.size(); stop++) {
        clock += 30000;
        if (stop + 1 < way.size() && run % 3 != 1) {
          Rules.Event approaching = event(clock + 1000, way.get(stop + 1), arrivalText, false);
          replay.add(approaching);
          successive = Rules.travelState(line, successive, List.of(approaching));
        }
        Rules.Event passage =
            event(clock + 5000, way.get(stop), arrivalText + " đã qua đường ngang", false);
        replay.add(passage);
        successive = Rules.travelState(line, successive, List.of(passage));
        String target = stop + 1 < way.size() ? way.get(stop + 1) : null;
        oneSegment(
            line,
            successive,
            way.get(stop),
            target,
            dir,
            "successive trip " + run + " switches only one interval at crossing " + stop);
        List<Rules.Event> closure =
            List.of(
                event(clock + 10000, way.get(stop), "Khôi phục hệ thống", false),
                event(clock + 15000, way.get(stop), arrivalText + " đã qua đường ngang", false));
        replay.addAll(closure);
        successive = Rules.travelState(line, successive, closure);
        if (target == null)
          check(
              Rules.trackSegments(line, successive).isEmpty(),
              "route exit stays cleared even if its passage arrives late, trip " + run);
        else
          oneSegment(
              line,
              successive,
              way.get(stop),
              target,
              dir,
              "closure and late phase cannot add a second interval, trip " + run);
      }
    }
    check(
        Rules.trackSegments(line, Rules.travelState(line, Rules.TravelState.empty(), replay))
            .isEmpty(),
        "two-day archive replay ends without ghost trains after 24 alternating trips");
    Collections.reverse(replay);
    check(
        Rules.trackSegments(line, Rules.travelState(line, Rules.TravelState.empty(), replay))
            .isEmpty(),
        "shuffled archive produces the same cleared diagram");
    check(
        successive.journeys().size() <= line.crossings().size() * 3,
        "completed visit records stay bounded across repeated trips");
    var separatedVisits =
        Rules.travelState(
            line,
            Rules.TravelState.empty(),
            List.of(
                event(1, "101+000", "Tàu lẻ", false), event(21600000, "100+000", "Tàu lẻ", false)));
    check(
        Rules.trackSegments(line, separatedVisits).size() == 2,
        "out-of-order reconciliation does not merge an unrelated hours-old pending visit");
    var selfArrival = event(1000, "101+000", "Tàu lẻ", false);
    var selfFault =
        new Rules.Event(
            "self:" + selfArrival.id(),
            selfArrival.at(),
            selfArrival.crossing(),
            "Nghi tự kích",
            true,
            List.of());
    var bellFault = event(1100, "101+000", "Chuông 2 hỏng", true);
    var manualReset = event(3000, "101+000", "Ấn nút Phục hồi", false);
    var selfReports = new ArrayList<>(List.of(selfArrival, selfFault, bellFault));
    var selfTrip = Rules.travelState(line, Rules.TravelState.empty(), selfReports);
    oneSegment(
        line, selfTrip, "100+000", "101+000", 1, "self-trigger initially lights incoming interval");
    check(
        selfTrip.journeys().get(0).selfTriggered(),
        "self qualification belongs to its exact arrival");
    var selfReset = Rules.travelState(line, selfTrip, List.of(manualReset));
    check(
        Rules.trackSegments(line, selfReset).isEmpty(),
        "manual reset extinguishes self-trigger incoming interval");
    selfReports.add(manualReset);
    check(
        Rules.activeFaults(selfReports).equals(List.of(bellFault)),
        "manual reset clears only self fault, retaining raw device fault");
    check(
        Rules.incidentHistory(selfReports).stream()
            .anyMatch(i -> i.fault().equals(selfFault) && manualReset.equals(i.recovery())),
        "manual reset marks self incident restored in history");
    check(
        Rules.incidentHistory(selfReports).stream()
            .anyMatch(i -> i.fault().equals(bellFault) && i.recovery() == null),
        "manual reset never pretends that bell is repaired");
    var lateSelf = event(4000, "101+000", "Tàu lẻ đã qua đường ngang", false);
    check(
        Rules.trackSegments(line, Rules.travelState(line, selfReset, List.of(lateSelf))).isEmpty(),
        "late self passage cannot relight canceled journey");
    var selfOutgoing =
        Rules.travelState(
            line,
            selfTrip,
            List.of(
                event(2000, "101+000", "Tàu lẻ đã qua đường ngang", false),
                event(2500, "101+000", "Khôi phục hệ thống", false)));
    oneSegment(
        line,
        selfOutgoing,
        "101+000",
        "102+000",
        1,
        "self outgoing interval survives ordinary source restore before manual reset");
    check(
        Rules.trackSegments(line, Rules.travelState(line, selfOutgoing, List.of(manualReset)))
            .isEmpty(),
        "manual reset also clears outgoing self interval after source restore");
    var resetElsewhere = event(3000, "102+000", "Ấn nút Phục hồi", false);
    check(
        Rules.travelState(line, selfTrip, List.of(resetElsewhere)).equals(selfTrip),
        "other crossing reset does not cancel this self-trigger journey");
    check(
        Rules.activeFaults(List.of(selfFault, resetElsewhere)).equals(List.of(selfFault)),
        "other crossing reset does not recover this self fault");
    var realTrip = Rules.travelState(line, Rules.TravelState.empty(), List.of(selfArrival));
    check(
        Rules.travelState(line, realTrip, List.of(manualReset)).equals(realTrip),
        "manual button alone does not erase an unflagged real train");
    var restarted =
        Rules.travelState(line, selfReset, List.of(event(5000, "101+000", "Tàu lẻ", false)));
    oneSegment(
        line,
        restarted,
        "100+000",
        "101+000",
        1,
        "a new later train still displays after manual reset");
    check(
        !restarted.journeys().get(restarted.journeys().size() - 1).selfTriggered(),
        "old self classification is not inherited by a new train");
    var interleaved =
        Rules.travelState(line, selfTrip, List.of(event(2000, "103+000", "Tàu chẵn", false)));
    oneSegment(
        line,
        Rules.travelState(line, interleaved, List.of(manualReset)),
        null,
        "103+000",
        -1,
        "reset leaves independent train elsewhere visible");
    var resetReplay = new ArrayList<>(selfReports);
    resetReplay.add(lateSelf);
    Collections.reverse(resetReplay);
    check(
        Rules.trackSegments(line, Rules.travelState(line, Rules.TravelState.empty(), resetReplay))
            .isEmpty(),
        "shuffled archive replay respects manual reset and late passage");
    check(
        Rules.isManualRecovery("  ấn  nút PHỤC HỒI ") && !Rules.isManualRecovery("Ấn nút Duy tu"),
        "manual recovery is tolerant of case and whitespace but not other buttons");
    var simultaneousReset = event(1000, "101+000", "Ấn nút Phục hồi", false);
    check(
        Rules.trackSegments(
                line,
                Rules.travelState(
                    line,
                    Rules.TravelState.empty(),
                    List.of(simultaneousReset, selfFault, selfArrival)))
            .isEmpty(),
        "second-resolution timestamps apply reset after arrival and inference");
    check(
        Rules.activeFaults(List.of(simultaneousReset, selfFault)).isEmpty(),
        "same-second reset resolves inference regardless of input order");
    var evenArrival = event(1000, "102+000", "Tàu chẵn", false);
    var evenSelf =
        new Rules.Event(
            "self:" + evenArrival.id(), 1000, "102+000", "Nghi tự kích", true, List.of());
    check(
        Rules.trackSegments(
                line,
                Rules.travelState(
                    line,
                    Rules.TravelState.empty(),
                    List.of(
                        evenArrival, evenSelf, event(3000, "102+000", "Ấn nút Phục hồi", false))))
            .isEmpty(),
        "manual self reset also handles even trains");
    long axleDay = java.time.Instant.parse("2026-10-22T17:00:00Z").toEpochMilli();
    List<Rules.Event> five = new ArrayList<>();
    for (int i = 0; i < 5; i++)
      five.add(axle(axleDay + i * 60000, "101+000", 3, i % 2 == 0 ? 3 : -3));
    check(
        Rules.axleAlerts(line, five).isEmpty(),
        "five qualifying errors do not trigger daily warning");
    List<Rules.Event> tiny = new ArrayList<>(five);
    for (int i = 0; i < 20; i++)
      tiny.add(axle(axleDay + (i + 5) * 60000, "101+000", 3, i % 2 == 0 ? 1 : -2));
    check(
        Rules.axleAlerts(line, tiny).isEmpty(),
        "one- and two-axle errors never increment the daily trigger");
    check(
        Rules.axleErrors(line, tiny).get(tiny.size() - 1).dailyCount() == 5,
        "count remains five after one- and two-axle notes");
    var sixth = axle(axleDay + 30 * 60000, "101+000", 3, -3);
    tiny.add(sixth);
    var sixAlerts = Rules.axleAlerts(line, tiny);
    check(
        sixAlerts.size() == 1
            && sixAlerts.get(0).text().contains("CB3 đếm sai 6 lần")
            && sixAlerts.get(0).text().contains("thiếu 3 trục"),
        "sixth three-axle deviation alerts with signed amount");
    tiny.add(axle(axleDay + 31 * 60000, "101+000", 3, 1));
    tiny.add(axle(axleDay + 32 * 60000, "101+000", 3, 3));
    check(
        Rules.axleAlerts(line, tiny).equals(sixAlerts),
        "later small deviations do not become periodic reminders");
    check(
        Rules.axleErrors(line, tiny).get(tiny.size() - 1).dailyCount() == 7,
        "daily qualifying count continues after alert");
    check(
        Rules.axleAlerts(line, List.of(axle(axleDay, "101+000", 4, 10))).size() == 1,
        "exactly ten excess axles trigger a single-event alert");
    check(
        Rules.axleAlerts(line, List.of(axle(axleDay, "101+000", 4, -10))).size() == 1,
        "exactly ten missing axles trigger a single-event alert");
    check(
        Rules.axleAlerts(line, List.of(axle(axleDay, "101+000", 4, 9))).isEmpty(),
        "nine excess axles remain below single-event threshold");
    check(
        Rules.axleAlerts(line, List.of(axle(axleDay, "101+000", 4, -9))).isEmpty(),
        "nine missing axles remain below single-event threshold");
    var largeExtra = Rules.axleAlerts(line, List.of(axle(axleDay, "101+000", 4, 11)));
    check(
        largeExtra.size() == 1
            && largeExtra.get(0).text().contains("thừa 11 trục trong 1 lần")
            && largeExtra.get(0).text().contains("Chuẩn 40, đếm 51"),
        "first event alerts for eleven excess axles with baseline");
    var largeMissing = Rules.axleAlerts(line, List.of(axle(axleDay, "101+000", 4, -11)));
    check(
        largeMissing.size() == 1
            && largeMissing.get(0).text().contains("thiếu 11 trục trong 1 lần")
            && largeMissing.get(0).text().contains("đếm 29"),
        "first event alerts for eleven missing axles");
    List<Rules.Event> combined = new ArrayList<>(five);
    combined.add(axle(axleDay + 31 * 60000, "101+000", 3, -12));
    check(
        Rules.axleAlerts(line, combined).size() == 1
            && Rules.axleAlerts(line, combined).get(0).text().contains("Sai 6 lần"),
        "same event crossing both thresholds creates one warning");
    List<Rules.Event> separateAxles = new ArrayList<>(five);
    for (int i = 0; i < 5; i++) {
      separateAxles.add(axle(axleDay + (i + 6) * 60000, "101+000", 4, 3));
      separateAxles.add(axle(axleDay + (i + 12) * 60000, "102+000", 3, -3));
    }
    check(
        Rules.axleAlerts(line, separateAxles).isEmpty(),
        "do not pool errors across sensors or crossings");
    List<Rules.Event> duplicated = new ArrayList<>(tiny);
    duplicated.addAll(tiny);
    check(
        Rules.axleAlerts(line, duplicated).equals(sixAlerts),
        "duplicate reports do not double-count");
    List<Rules.Event> daySplit = new ArrayList<>(five);
    daySplit.add(axle(axleDay + 86400000, "101+000", 3, -3));
    check(
        Rules.axleAlerts(line, daySplit).isEmpty()
            && Rules.axleErrors(line, daySplit).get(5).dailyCount() == 1,
        "reset counts at Vietnamese midnight");
    List<Rules.Event> nonconsecutive = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      nonconsecutive.add(axle(axleDay + i * 120000, "101+000", 3, 3));
      nonconsecutive.add(axle(axleDay + i * 120000 + 60000, "101+000", 3, 0));
    }
    check(
        Rules.axleAlerts(line, nonconsecutive).size() == 1,
        "correct intervening trains do not reset daily cumulative count");
    check(
        Rules.activeFaults(
                    List.of(
                        sixAlerts.get(0),
                        event(sixth.at() + 1, "101+000", "Ấn nút Phục hồi", false),
                        event(sixth.at() + 2, "101+000", "Cảm biến 3 khôi phục tốt", false)))
                .size()
            == 1,
        "statistical warning is latched for the day, not erased by generic/manual/device recovery");
    List<Rules.Event> shuffledAxles = new ArrayList<>(tiny);
    Collections.reverse(shuffledAxles);
    check(
        Rules.axleAlerts(line, shuffledAxles).equals(sixAlerts),
        "out-of-order pages replay deterministic axle counts");
    var voltageReading =
        new Rules.Event(
            "v",
            axleDay,
            "101+000",
            "Khôi phục hệ thống",
            false,
            List.of("40V", "40V", "0V", "40V", "40V", "40V"));
    check(
        Rules.axleAlerts(line, List.of(voltageReading)).isEmpty(),
        "voltage values never become axle alerts");
    var tiedCounts =
        new Rules.Event(
            "tie",
            axleDay,
            "101+000",
            "Khôi phục hệ thống",
            false,
            List.of("20", "20", "20", "40", "40", "40"));
    check(
        Rules.axleAlerts(line, List.of(tiedCounts)).isEmpty()
            && Rules.axleNote(tiedCounts).equals("cảm biến đếm trục không đồng nhất, lỗi"),
        "tied baseline keeps note without inventing an incorrect sensor");
    var offRoute = axle(axleDay, "900+000", 3, -20);
    check(
        Rules.axleAlerts(line, List.of(offRoute)).isEmpty(), "ignore points outside account route");
    var stationMeasurement = axle(axleDay, "319+200 (Vi)", 3, -20);
    check(
        Rules.axleAlerts(Rules.DEMO_ROUTE, List.of(stationMeasurement)).isEmpty(),
        "station devices are not crossing axle counters");
    var notRestore =
        new Rules.Event(
            "measure",
            axleDay,
            "101+000",
            "Đo điện áp cảm biến",
            false,
            List.of("40", "40", "0", "40", "40", "40"));
    check(
        Rules.axleAlerts(line, List.of(notRestore)).isEmpty(),
        "numeric voltage measurement labels are ignored");
    List<Rules.Event> healthyAfter = new ArrayList<>(sixAlerts);
    healthyAfter.add(axle(axleDay + 40 * 60000, "101+000", 3, 0));
    check(
        Rules.activeFaults(healthyAfter).size() == 1,
        "a later correct count does not erase daily warning");
    check(
        Rules.axleNote(axle(axleDay, "101+000", 3, 1)).equals("CB3 thừa 1 trục"),
        "one-axle deviation remains a silent note");
    check(
        Rules.axleAlerts(line, new ArrayList<>(sixAlerts)).isEmpty(),
        "derived warnings do not count themselves");
    List<Rules.Event> largeThenDaily = new ArrayList<>();
    largeThenDaily.add(axle(axleDay, "101+000", 3, -12));
    for (int i = 1; i < 6; i++) largeThenDaily.add(axle(axleDay + i * 60000, "101+000", 3, -3));
    check(
        Rules.activeFaults(Rules.axleAlerts(line, largeThenDaily)).size() == 2,
        "large deviation and later daily threshold each remain visible for the same sensor");
    largeThenDaily.add(axle(axleDay + 6 * 60000, "101+000", 3, 12));
    check(
        Rules.activeFaults(Rules.axleAlerts(line, largeThenDaily)).size() == 3,
        "each new large measurement remains a distinct alert even on an already warned sensor");
    check(
        Rules.isAxleExempt("Km 320 + 445")
            && Rules.isAxleExempt("0320+445")
            && !Rules.isAxleExempt("320+957"),
        "special counting mechanism is identified by crossing location");
    List<Rules.Event> exemptCounts = new ArrayList<>();
    for (int i = 0; i < 7; i++)
      exemptCounts.add(axle(axleDay + i * 1000, "Km 320+445", 3, i == 6 ? -12 : 3));
    check(
        exemptCounts.stream().allMatch(e -> Rules.axleNote(e).isEmpty()),
        "320+445 never receives the six-sensor comparison note");
    check(
        Rules.axleErrors(Rules.DEMO_ROUTE, exemptCounts).isEmpty(),
        "320+445 has no daily six-sensor statistics");
    check(
        Rules.axleAlerts(Rules.DEMO_ROUTE, exemptCounts).isEmpty(),
        "six errors and large errors at 320+445 never generate alerts");
    var oldExempt =
        new Rules.Event(
            "axle:3:legacy-exempt",
            axleDay,
            "Km 320+445",
            "CB3 đếm thiếu 12 trục trong 1 lần",
            true,
            List.of("40", "40", "28", "40", "40", "40"));
    var actualExemptFault = event(axleDay, "320+445", "Cảm biến 3 hỏng", true);
    check(
        Rules.activeFaults(List.of(oldExempt, actualExemptFault))
            .equals(List.of(actualExemptFault)),
        "cached derived axle warnings are excluded while actual red faults remain");
    check(
        Rules.incidentHistory(List.of(oldExempt, actualExemptFault)).size() == 1
            && Rules.incidentHistory(List.of(oldExempt, actualExemptFault))
                .get(0)
                .fault()
                .equals(actualExemptFault),
        "old derived 320+445 warnings do not reappear in historical incidents");
    check(
        Rules.axleAlerts(Rules.DEMO_ROUTE, List.of(axle(axleDay, "320+957", 3, 10))).size() == 1,
        "normal neighboring crossings still alert at exactly ten axles");
    for (String point : List.of("280+500", "307+507", "309+056", "320+445", "338+900", "343+350")) {
      var exemptRoute = new Rules.Route(List.of("100+000", point, "400+000"));
      String[] parts = point.split("\\+");
      check(
          Rules.isAxleExempt("Km " + parts[0] + " + " + parts[1])
              && Rules.isAxleExempt("0" + point),
          "excluded chainage is recognized with formatting variations: " + point);
      List<Rules.Event> counts = new ArrayList<>();
      for (int i = 0; i < 7; i++) counts.add(axle(axleDay + i * 1000, point, 3, i == 6 ? -10 : 3));
      check(
          counts.stream().allMatch(e -> Rules.axleNote(e).isEmpty()), "no axle notes at " + point);
      check(Rules.axleErrors(exemptRoute, counts).isEmpty(), "no axle statistics at " + point);
      check(
          Rules.axleAlerts(exemptRoute, counts).isEmpty(), "neither threshold alerts at " + point);
      var legacy =
          new Rules.Event(
              "axle:3:old-" + point,
              axleDay,
              point,
              "CB3 đếm thiếu 10 trục trong 1 lần",
              true,
              List.of("40", "40", "30", "40", "40", "40"));
      var raw = event(axleDay + 1, point, "Chuông 2 hỏng", true);
      var rawAxleText = event(axleDay + 2, point, "CB3 đếm thiếu 10 trục trong 1 lần", true);
      check(
          Rules.activeFaults(List.of(legacy, raw, rawAxleText)).size() == 2,
          "only derived alerts are removed, including when raw text mentions axles: " + point);
      check(
          Rules.incidentHistory(List.of(legacy, raw, rawAxleText)).size() == 2,
          "archived derived alert is hidden but raw incidents remain: " + point);
      var train = event(axleDay + 3, point, "Tàu lẻ", false);
      check(
          Rules.occupancies(exemptRoute, List.of(train)).size() == 1,
          "axle exclusion keeps train occupation: " + point);
      var travel = Rules.travelState(exemptRoute, Rules.TravelState.empty(), List.of(train));
      check(
          Rules.trackSegments(exemptRoute, travel).size() == 1,
          "axle exclusion keeps train diagram: " + point);
    }
    for (String normal :
        List.of(
            "280+499", "280+501", "307+506", "307+508", "309+055", "309+057", "338+899", "338+901",
            "343+349", "343+351")) {
      var normalRoute = new Rules.Route(List.of(normal));
      check(
          !Rules.isAxleExempt(normal)
              && Rules.axleAlerts(normalRoute, List.of(axle(axleDay, normal, 3, 10))).size() == 1,
          "neighboring chainage remains subject to axle alerts: " + normal);
    }
    check(
        !Rules.isAxleExempt(null) && !Rules.isAxleExempt("not-a-crossing"),
        "invalid input is not an exemption");
    var activityLine = new Rules.Route(List.of("100+000", "101+000", "102+000"));
    var downstream = event(15000, "101+000", "Tàu lẻ", false);
    for (String motion :
        List.of(
            "Cần chắn 1 đã hạ",
            "Cần chắn 2 đã nâng",
            "Cần chắn 12 đã hạ",
            "Cần chắn đã nâng",
            "CẦN CHẮN 3 ĐÃ NÂNG",
            " Cần  chắn  4  đã  hạ ",
            "Cần chắn số 5 đã nâng"))
      check(
          Rules.suspectedSelfTriggers(
                  activityLine, List.of(event(8000, "100+000", motion, false), downstream), 0)
              .isEmpty(),
          "completed upstream barrier activity justifies the current visit: " + motion);
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        event(8000, "100+000", "Cần chắn 1 đã hạ", false),
                        event(9000, "100+000", "Cần chắn 2 đã hạ", false),
                        event(10000, "100+000", "Cần chắn 1 đã nâng", false),
                        event(11000, "100+000", "Cần chắn 2 đã nâng", false),
                        downstream,
                        event(18000, "101+000", "Tàu lẻ", false)),
                    0)
                .size()
            == 1,
        "all barrier messages in one visit can justify only one downstream arrival");
    for (String passage : List.of("Tàu lẻ đã qua đường ngang", "Tàu chẵn đã qua đường ngang")) {
      int d = passage.contains("lẻ") ? 1 : -1;
      String upstream = d == 1 ? "100+000" : "102+000";
      var arrival = event(15000, "101+000", d == 1 ? "Tàu lẻ" : "Tàu chẵn", false);
      check(
          Rules.suspectedSelfTriggers(
                  activityLine, List.of(event(8000, upstream, passage, false), arrival), 0)
              .isEmpty(),
          "same-direction upstream passage can replace its missing arrival: " + passage);
      check(
          Rules.suspectedSelfTriggers(
                  activityLine, List.of(arrival, event(16000, upstream, passage, false)), 0)
              .isEmpty(),
          "later same-visit passage disproves suspicion without a fixed timestamp allowance");
    }
    for (String wrong :
        List.of(
            "Cần chắn 1 không hạ",
            "Cần chắn 1 hỏng",
            "Cần chắn 1 đã nâng không hết",
            "Cần chắn 1 mất trạng thái"))
      check(
          Rules.suspectedSelfTriggers(
                      activityLine, List.of(event(8000, "100+000", wrong, false), downstream), 0)
                  .size()
              == 1,
          "fault or incomplete barrier phrase is not successful movement evidence: " + wrong);
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(event(8000, "100+000", "Cần chắn 1 đã hạ", true), downstream),
                    0)
                .size()
            == 1,
        "red source message is not positive barrier evidence");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(downstream, event(16000, "101+000", "Cần chắn 1 đã hạ", false)),
                    0)
                .size()
            == 1,
        "a crossing's own barrier dropping must not exonerate its own self-trigger");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        event(8000, "100+000", "Tàu chẵn đã qua đường ngang", false), downstream),
                    0)
                .size()
            == 1,
        "upstream passage in the opposite direction is not evidence");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        event(8000, "100+000", "Tàu chẵn", false),
                        event(9000, "100+000", "Cần chắn 1 đã hạ", false),
                        downstream),
                    0)
                .size()
            == 2,
        "barrier movement within a known opposite-direction visit does not change its direction");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        event(1000, "100+000", "Cần chắn 1 đã hạ", false),
                        event(2000, "100+000", "Khôi phục hệ thống", false),
                        event(3000, "101+000", "Cần chắn 1 đã nâng", false),
                        event(4000, "101+000", "Khôi phục hệ thống", false),
                        downstream),
                    0)
                .size()
            == 1,
        "old activity before the preceding downstream visit ended is not recycled");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        downstream,
                        event(16000, "101+000", "Khôi phục hệ thống", false),
                        event(17000, "100+000", "Tàu lẻ đã qua đường ngang", false)),
                    0)
                .size()
            == 1,
        "evidence from after the target visit ended cannot cancel its earlier suspicion");
    check(
        Rules.suspectedSelfTriggers(
                activityLine,
                List.of(
                    event(1000, "100+000", "Cần chắn 1 đã hạ", false),
                    event(2000, "100+000", "Khôi phục hệ thống", false),
                    event(3000, "101+000", "Tàu lẻ", false),
                    event(4000, "101+000", "Khôi phục hệ thống", false),
                    event(5000, "100+000", "Cần chắn 2 đã nâng", false),
                    downstream),
                0)
            .isEmpty(),
        "separate upstream cycles independently support separate downstream visits");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        event(1000, "100+000", "Tàu lẻ", false),
                        event(2000, "100+000", "Cần chắn 1 đã hạ", false),
                        event(3000, "100+000", "Tàu lẻ đã qua đường ngang", false),
                        event(4000, "100+000", "Cần chắn 1 đã nâng", false),
                        event(5000, "100+000", "Khôi phục hệ thống", false),
                        event(6000, "101+000", "Tàu lẻ", false),
                        downstream),
                    0)
                .size()
            == 1,
        "arrival plus barrier and passage evidence is one train, not independent tokens");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(event(8000, "100+000", "Cần chắn 1 đã hạ", false), downstream),
                    10000)
                .size()
            == 1,
        "activity before known coverage is ignored");
    var delayedCandidates = Rules.suspectedSelfTriggers(activityLine, List.of(downstream), 0);
    check(delayedCandidates.size() == 1, "missing evidence initially remains suspected");
    var correctedCandidates =
        Rules.suspectedSelfTriggers(
            activityLine,
            List.of(downstream, event(16000, "100+000", "Tàu lẻ đã qua đường ngang", false)),
            0);
    var delayedPending = new HashMap<String, Long>();
    delayedPending.put(downstream.id(), 100L);
    check(
        Rules.confirmedSelfTriggers(correctedCandidates, delayedPending, 50000, 0, 30000).isEmpty()
            && delayedPending.isEmpty(),
        "supplemental passage removes pending confirmation instead of alarming later");
    check(
        Rules.suspectedSelfTriggers(
                activityLine,
                List.of(
                    event(1000, "100+000", "Tàu lẻ", false),
                        event(2000, "100+000", "Tàu lẻ", false),
                    event(3000, "101+000", "Tàu lẻ", false),
                        event(4000, "101+000", "Tàu lẻ", false)),
                0)
            .isEmpty(),
        "two queued explicit upstream trains remain distinct when their zones overlap");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        event(1000, "100+000", "Tàu lẻ", false),
                        event(2000, "100+000", "Khôi phục hệ thống", false),
                        event(3000, "101+000", "Cần chắn 1 đã hạ", false),
                        event(4000, "101+000", "Khôi phục hệ thống", false),
                        downstream),
                    0)
                .size()
            == 1,
        "an unmatched old arrival belonging to an earlier unnamed target visit is not recycled");
    for (String motion : List.of("Cần chắn 1 đã hạ", "Cần chắn 2 đã nâng")) {
      check(
          Rules.suspectedSelfTriggers(
                  activityLine, List.of(downstream, event(16000, "100+000", motion, false)), 0)
              .isEmpty(),
          "late same-visit barrier activity also removes suspicion: " + motion);
      check(
          Rules.suspectedSelfTriggers(
                      activityLine,
                      List.of(
                          downstream,
                          event(16000, "101+000", "Khôi phục hệ thống", false),
                          event(17000, "100+000", motion, false)),
                      0)
                  .size()
              == 1,
          "barrier activity from a later visit cannot explain an already ended target: " + motion);
    }
    var duplicates =
        List.of(
            event(1000, "100+000", "Cần chắn 1 đã hạ", false),
            event(1000, "100+000", "Cần chắn 1 đã hạ", false),
            event(1000, "100+000", "Cần chắn 2 đã hạ", false),
            event(2000, "101+000", "Tàu lẻ", false),
            event(3000, "101+000", "Tàu lẻ", false));
    check(
        Rules.suspectedSelfTriggers(activityLine, duplicates, 0).size() == 1,
        "duplicate pages and simultaneous barriers do not multiply the evidence");
    check(
        Rules.suspectedSelfTriggers(
                    activityLine,
                    List.of(
                        event(1000, "100+000", "Tàu lẻ", false),
                        event(3000, "100+000", "Tàu lẻ đã qua đường ngang", false),
                        event(5000, "101+000", "Tàu lẻ", false),
                        event(6000, "100+000", "Cần chắn 1 đã hạ", false),
                        event(7000, "101+000", "Tàu lẻ", false)),
                    0)
                .size()
            == 1,
        "repeated barrier action after passage without reset or new approach remains the same"
            + " visit");
    check(
        Rules.suspectedSelfTriggers(
                activityLine,
                List.of(
                    event(1000, "100+000", "Cần chắn 1 đã nâng", false),
                    event(2000, "101+000", "Cần chắn 1 đã hạ", false),
                    event(3000, "101+000", "Khôi phục hệ thống", false),
                    event(10000, "100+000", "Tàu lẻ", false),
                    downstream),
                0)
            .isEmpty(),
        "a fresh explicit arrival still proves the train when earlier unpaired barrier status is"
            + " present");
    System.out.println(scenarios + " domain scenarios passed");
  }
}
