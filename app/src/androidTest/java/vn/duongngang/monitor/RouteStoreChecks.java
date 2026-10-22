package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import java.util.List;

/** No test dependencies. Uses a separate device-protected sandbox, never the app's normal data. */
public final class RouteStoreChecks extends Instrumentation {
  private int checks;

  private void check(boolean result, String message) {
    checks++;
    if (!result) throw new AssertionError(message);
  }

  @Override
  public void onCreate(Bundle arguments) {
    super.onCreate(arguments);
    start();
  }

  @Override
  public void onStart() {
    Context context = getTargetContext().createDeviceProtectedStorageContext();
    Store store = new Store(context);
    Bundle result = new Bundle();
    int code = -1;
    store.prefs.edit().clear().commit();
    context.deleteDatabase("events.db");
    try {
      store.credentials("https://rail.example.test/", "account-a", "test-only");
      String a = store.account();
      var routeA = new Rules.Route(List.of("275+290", "276+351", "279+000 (CS)"));
      var eventA = new Rules.Event("a", 1000, "Km 275+290", "Tàu lẻ", false, List.of());
      store.saveForAccount(a, new WebSource.DayData(List.of(eventA), routeA));
      check(store.route().points().equals(routeA.points()), "persist account inventory");
      check(store.events(0, 2000).size() == 1, "persist account events");
      store.prefs.edit().putString("rangeFrom", "276+351").putString("rangeTo", "276+351").commit();
      check(store.rangeFrom(routeA, false).equals("276+351"), "saved account range");
      store
          .prefs
          .edit()
          .putString(Store.rangeKey(true, true), "312+111")
          .putString(Store.rangeKey(true, false), "313+800")
          .commit();
      check(
          store.rangeFrom(routeA, false).equals("276+351"), "demo scope cannot change live scope");
      store.credentials("https://rail.example.test/", "account-b", "test-only");
      check(store.route().points().isEmpty(), "new ID starts without old route");
      check(store.events(0, 2000).isEmpty(), "new ID starts without old events");
      check(
          !store.prefs.contains("rangeFrom") && !store.prefs.contains("rangeTo"),
          "clear old account scope");
      boolean rejected = false;
      try {
        store.saveForAccount(a, new WebSource.DayData(List.of(eventA), routeA));
      } catch (IllegalStateException expected) {
        rejected = true;
      }
      check(
          rejected && store.route().points().isEmpty(),
          "late old-account request cannot publish route");
      var routeB = new Rules.Route(List.of("500+000", "501+100 (AB)", "502+200"));
      var eventB = new Rules.Event("b", 1000, "Km 502+200", "Tàu chẵn", false, List.of());
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(eventB), routeB));
      check(
          store.rangeFrom(routeB, false).equals("500+000")
              && store.rangeTo(routeB, false).equals("502+200"),
          "new account defaults to its full route");
      check(store.events(0, 2000).get(0).id().equals("b"), "only current account records remain");
      store.credentials("https://rail.example.test/", "account-b", "test-only");
      check(store.route().points().equals(routeB.points()), "same ID retains route");
      var inferred = new Rules.Event("self:b", 1000, "Km 502+200", "Nghi tự kích", true, List.of());
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(inferred), routeB));
      boolean originalProtected = false;
      try {
        store.discardInferences(store.account(), List.of("b"));
      } catch (IllegalArgumentException expected) {
        originalProtected = true;
      }
      check(
          originalProtected && store.events(0, 2000).size() == 2,
          "cannot delete a web record when removing a disproved inference");
      store.discardInferences(store.account(), List.of("self:b"));
      check(
          store.events(0, 2000).size() == 1,
          "disproved inference removed without altering original report");
      var oldBell =
          new Rules.Event("old-bell", 100, "Km 500+000", "Chuông 1 hỏng", true, List.of());
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(oldBell), routeB));
      store.updateActiveFaults(store.account(), 0, 1000, 0, List.of());
      check(store.activeFaults().size() == 1, "active fault cache built");
      store.updateActiveFaults(store.account(), 1001, 2000, 0, List.of());
      check(store.activeFaults().size() == 1, "old unresolved fault survives later empty windows");
      var restoredBell =
          new Rules.Event(
              "restore-bell", 2500, "Km 500+000", "Chuông 1 khôi phục tốt", false, List.of());
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(restoredBell), routeB));
      store.updateActiveFaults(store.account(), 2000, 3000, 0, List.of());
      check(store.activeFaults().isEmpty(), "later matching recovery clears carried fault");
      check(store.occupationEvents().size() == 1, "occupation survives empty later windows");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "release", 3500, "Km 502+200", "Khôi phục hệ thống", false, List.of())),
              routeB));
      store.updateActiveFaults(store.account(), 3000, 4000, 0, List.of());
      check(store.occupationEvents().isEmpty(), "system recovery releases carried occupation");
      check(
          Rules.incidentHistory(store.incidentEvents(0, 1000)).stream()
              .anyMatch(i -> i.fault().id().equals("old-bell") && i.recovery() != null),
          "historical day links later recovery without reading all later train logs");
      store
          .prefs
          .edit()
          .putLong("lastFull", 3000)
          .putString("pendingSelf", "{\"case\":500}")
          .commit();
      check(
          store.pendingSelf().get("case") == 500L,
          "pending confirmation timestamps survive restart");
      store.credentials("https://other.example.test/", "account-b", "test-only");
      check(
          store.route().points().isEmpty() && store.events(0, 2000).isEmpty(),
          "new URL isolates same ID");
      check(
          !store.prefs.contains("activeFaults")
              && !store.prefs.contains("occupiedPoints")
              && !store.prefs.contains("pendingSelf")
              && !store.prefs.contains("lastFull"),
          "new account clears inferred and active state");
      result.putString("stream", checks + " account storage checks passed\n");

    } catch (Throwable failure) {
      result.putString("stream", "FAILED: " + failure + "\n");
      code = 0;
    } finally {
      store.prefs.edit().clear().commit();
      context.deleteDatabase("events.db");
    }
    finish(code, result);
  }
}
