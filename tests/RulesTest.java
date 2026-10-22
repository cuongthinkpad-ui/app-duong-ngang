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
            event(1, "276+351", "Chuông 1 mất trạng thái", true),
            event(2, "276+351", "Chuông 2 mất trạng thái", true),
            event(3, "276+351", "Chuông 2 khôi phục tốt", false));
    check(
        Rules.activeFaults(bells).size() == 1
            && Rules.activeFaults(bells).get(0).text().equals("Chuông 1 mất trạng thái"),
        "bell restoration cannot clear another numbered bell");
    check(
        Rules.incidentHistory(bells).stream().filter(i -> i.recovery() != null).count() == 1,
        "numbered bell incident pairing");
    var lamps =
        List.of(
            event(1, "276+351", "Đèn cảnh báo 1 mất trạng thái", true),
            event(2, "276+351", "Đèn cảnh báo 2 mất trạng thái", true),
            event(3, "276+351", "Đèn cảnh báo 1 khôi phục tốt", false));
    check(
        Rules.activeFaults(lamps).size() == 1
            && Rules.activeFaults(lamps).get(0).text().equals("Đèn cảnh báo 2 mất trạng thái"),
        "numbered lamps remain distinct");
    check(
        Rules.faultKey("Chuông cảnh báo số 2 mất trạng thái").equals("chuông 2"),
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
    System.out.println(scenarios + " domain scenarios passed");
  }
}
