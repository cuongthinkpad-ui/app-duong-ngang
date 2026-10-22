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
      store.credentials("https://other.example.test/", "account-b", "test-only");
      check(
          store.route().points().isEmpty() && store.events(0, 2000).isEmpty(),
          "new URL isolates same ID");
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
