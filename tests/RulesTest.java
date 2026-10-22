import java.util.*;
import vn.duongngang.monitor.Rules;

public class RulesTest {
  static void check(boolean b, String message) {
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
      check(Rules.upstream(first, 1) == null, "north boundary");
    for (String last : List.of("314+550", "334+830"))
      check(Rules.upstream(last, -1) == null, "south boundary");
    check(Rules.upstream("328+325", -1).equals("332+350"), "station skipped");
    var current = event(10000, "313+456", "Tàu lẻ", false);
    check(Rules.suspect(current, List.of(), 0, 5000, true), "missing previous");
    check(
        !Rules.suspect(current, List.of(event(8000, "312+111", "Tàu lẻ", false)), 0, 5000, true),
        "valid train");
    check(!Rules.suspect(current, List.of(), 9000, 5000, true), "startup uncertainty");
    check(!Rules.suspect(current, List.of(), 0, 5000, false), "pagination gap");
    check(
        !Rules.suspect(event(10000, "312+111", "Tàu lẻ", false), List.of(), 0, 5000, true),
        "entry allowed");
    var faults =
        List.of(
            event(1, "328+325", "Mất nguồn xoay chiều 220VAC", true),
            event(2, "328+325", "Cần chắn 1 mất trạng thái", true),
            event(3, "328+325", "Khôi phục hệ thống", false),
            event(4, "328+325", "Nguồn xoay chiều 220VAC khôi phục tốt", false));
    check(Rules.activeFaults(faults).size() == 1, "restore only matching fault");
    System.out.println("12 domain scenarios passed");
  }
}
