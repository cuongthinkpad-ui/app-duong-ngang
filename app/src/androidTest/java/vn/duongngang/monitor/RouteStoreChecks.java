package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import java.util.List;

/** No test dependencies. Uses a separate device-protected sandbox, never the app's normal data. */
public final class RouteStoreChecks extends Instrumentation {
  private int checks;
  private boolean uiMode, previewMode;

  private void check(boolean result, String message) {
    checks++;
    if (!result) throw new AssertionError(message);
  }

  @Override
  public void onCreate(Bundle arguments) {
    super.onCreate(arguments);
    previewMode = arguments != null && "preview".equals(arguments.getString("mode"));
    uiMode = arguments != null && "ui".equals(arguments.getString("mode"));
    start();
  }

  @Override
  public void onStart() {
    if (previewMode) {
      Bundle result = new Bundle();
      try {
        UiPreview.capture(this);
        result.putString("stream", "Synthetic UI previews captured\n");
        finish(-1, result);
      } catch (Throwable failure) {
        result.putString("stream", "FAILED: " + failure + "\n");
        finish(0, result);
      }
      return;
    }
    if (uiMode) {
      Bundle result = new Bundle();
      try {
        result.putString("stream", new UiRefreshChecks(this).run() + " UI refresh checks passed\n");
        finish(-1, result);
      } catch (Throwable error) {
        result.putString("stream", "FAILED: " + error + "\n");
        finish(0, result);
      }
      return;
    }
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
      long midnight = java.time.Instant.parse("2026-10-22T17:00:00Z").toEpochMilli();
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "yesterday-bell",
                      midnight - 1000,
                      "Km 500+000",
                      "Chuông 2 hỏng",
                      true,
                      List.of()),
                  new Rules.Event(
                      "new-day-lamp", midnight + 1000, "Km 500+000", "Đèn 1 hỏng", true, List.of()),
                  new Rules.Event(
                      "overnight-train",
                      midnight - 2000,
                      "Km 502+200",
                      "Tàu lẻ",
                      false,
                      List.of())),
              routeB));
      store.updateActiveFaults(store.account(), midnight - 3000, midnight - 1, 0, List.of());
      store.resetDailyFaults(midnight + 2000);
      check(
          store.activeFaults().size() == 1
              && store.activeFaults().get(0).id().equals("new-day-lamp"),
          "daily reset removes yesterday fault and retains new-day fault");
      check(
          store.occupationEvents().size() == 1
              && store.events(midnight - 3000, midnight).size() == 2,
          "daily reset retains overnight train and yesterday archive");
      check(
          store.pendingSelf().isEmpty() && store.prefs.getLong("baselineFrom", 0) == midnight,
          "daily reset clears pending inference and sets midnight boundary");
      store.prefs.edit().putString("pendingSelf", "{\"new-case\":500}").commit();
      store.resetDailyFaults(midnight + 3000);
      check(
          store.pendingSelf().containsKey("new-case"),
          "same-day reset is idempotent and retains new pending inference");
      var bridgeRoute = new Rules.Route(List.of("500+000", "501+500", "502+200"));
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "bridge-arrive", midnight + 10000, "500+000", "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "bridge-pass",
                      midnight + 11000,
                      "500+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "bridge-restore",
                      midnight + 12000,
                      "500+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of())),
              bridgeRoute));
      store.updateActiveFaults(store.account(), midnight, midnight + 20000, midnight, List.of());
      check(
          Rules.betweenCrossings(store.travelState()).size() == 1,
          "travel cache survives source restoration");
      long later = midnight + 3 * 86400000L + 20000;
      store.updateActiveFaults(store.account(), later, later, later - 20000, List.of());
      check(
          Rules.betweenCrossings(new Store(context).travelState()).size() == 1,
          "checkpoint preserves travel beyond two-day replay and across Store restart");
      store.resetDailyFaults(later);
      check(
          Rules.betweenCrossings(store.travelState()).size() == 1,
          "fault midnight reset does not erase between-crossing travel");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "bridge-late-destination",
                      midnight + 2 * 86400000L + 5000,
                      "501+500",
                      "Tàu lẻ",
                      false,
                      List.of())),
              bridgeRoute));
      store.updateActiveFaults(store.account(), later, later, later - 20000, List.of());
      check(
          Rules.betweenCrossings(store.travelState()).isEmpty(),
          "late downstream backfill consumes the checkpointed leg");
      store.credentials("https://other.example.test/", "account-b", "test-only");
      check(
          store.route().points().isEmpty() && store.events(0, 2000).isEmpty(),
          "new URL isolates same ID");
      check(
          !store.prefs.contains("travelState")
              && !store.prefs.contains("travelCheckpoint")
              && !store.prefs.contains("faultDay")
              && !store.prefs.contains("activeFaults")
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
