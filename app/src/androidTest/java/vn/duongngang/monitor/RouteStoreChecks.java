package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import java.util.List;

/** No test dependencies. Uses a separate device-protected sandbox, never the app's normal data. */
public final class RouteStoreChecks extends Instrumentation {
  private int checks;
  private boolean uiMode, previewMode, timingMode, telemetryMode;

  private void check(boolean result, String message) {
    checks++;
    if (!result) throw new AssertionError(message);
  }

  @Override
  public void onCreate(Bundle arguments) {
    super.onCreate(arguments);
    previewMode = arguments != null && "preview".equals(arguments.getString("mode"));
    uiMode = arguments != null && "ui".equals(arguments.getString("mode"));
    timingMode = arguments != null && "timing".equals(arguments.getString("mode"));
    telemetryMode = arguments != null && "telemetry".equals(arguments.getString("mode"));
    start();
  }

  @Override
  public void onStart() {
    if (telemetryMode) {
      Bundle result = new Bundle();
      try {
        result.putString(
            "stream", new TelemetryUiChecks(this).run() + " telemetry UI checks passed\n");
        finish(-1, result);
      } catch (Throwable failure) {
        result.putString("stream", "FAILED: " + failure + "\n");
        finish(0, result);
      }
      return;
    }
    if (timingMode) {
      Bundle result = new Bundle();
      try {
        result.putString("stream", TabSwitchChecks.run(this));
        finish(-1, result);
      } catch (Throwable failure) {
        result.putString("stream", "FAILED: " + failure + "\n");
        finish(0, result);
      }
      return;
    }
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
      check(
          store.travelState().journeys().stream()
              .anyMatch(
                  j ->
                      "bridge-arrive".equals(j.id())
                          && "bridge-late-destination".equals(j.successorId())),
          "successor links persist across JSON and checkpoint replay");
      // Complete the unrelated overnight fixture before the isolated migration scenario.
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "migration-old-exit",
                      midnight - 1000,
                      "502+200",
                      "Khôi phục hệ thống",
                      false,
                      List.of())),
              bridgeRoute));
      int preservedEvents = store.events(0, later + 1).size();
      String preservedFaults = store.prefs.getString("activeFaults", "[]");
      store
          .prefs
          .edit()
          .remove("travelVersion")
          .putString("travelState", "[[\"old-ghost\",1,\"500+000\",\"501+500\",1,true,true,false]]")
          .putString(
              "travelCheckpoint", "[[\"old-ghost\",1,\"500+000\",\"501+500\",1,true,true,false]]")
          .commit();
      check(
          !store.travelReady() && store.travelState().journeys().isEmpty(),
          "obsolete diagram cache is not shown as current while migrating");
      store.prepareTravelState(store.account(), later);
      check(
          store.travelReady()
              && Rules.trackSegments(bridgeRoute, store.travelState())
                  .equals(List.of(new Rules.TrackSegment("500+000", "501+500", 1)))
              && store.events(0, later + 1).size() == preservedEvents
              && store.prefs.getString("activeFaults", "[]").equals(preservedFaults),
          "migration reconstructs from reports, removes old ghosts, and preserves raw/fault data");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "migration-pass",
                      later - 500,
                      "501+500",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of())),
              bridgeRoute));
      store.updateActiveFaults(store.account(), later - 500, later, later - 20000, List.of());
      check(
          Rules.trackSegments(bridgeRoute, store.travelState())
              .equals(List.of(new Rules.TrackSegment("501+500", "502+200", 1))),
          "passage immediately switches a checkpointed interval after migration");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "migration-reverse", later - 400, "502+200", "Tàu chẵn", false, List.of())),
              bridgeRoute));
      store.updateActiveFaults(store.account(), later - 500, later, later - 20000, List.of());
      check(
          Rules.trackSegments(bridgeRoute, new Store(context).travelState())
              .equals(List.of(new Rules.TrackSegment(null, "502+200", -1))),
          "opposite evidence removes stale cached direction across Store restart");
      store.credentials("https://other.example.test/", "account-b", "test-only");
      check(
          store.route().points().isEmpty() && store.events(0, 2000).isEmpty(),
          "new URL isolates same ID");
      check(
          !store.prefs.contains("travelState")
              && !store.prefs.contains("travelVersion")
              && !store.prefs.contains("travelCheckpoint")
              && !store.prefs.contains("faultDay")
              && !store.prefs.contains("activeFaults")
              && !store.prefs.contains("occupiedPoints")
              && !store.prefs.contains("pendingSelf")
              && !store.prefs.contains("lastFull"),
          "new account clears inferred and active state");
      var manualRoute = new Rules.Route(List.of("100+000", "101+000", "102+000", "103+000"));
      long selfAt = midnight - 2 * 86400000L + 1000;
      long resetAt = midnight + 1000;
      var selfStart =
          new Rules.Event("manual-start", selfAt, "101+000", "Tàu lẻ", false, List.of());
      var selfFault =
          new Rules.Event("self:manual-start", selfAt, "101+000", "Nghi tự kích", true, List.of());
      var rawFault =
          new Rules.Event(
              "manual-bell", resetAt - 200, "101+000", "Chuông 2 hỏng", true, List.of());
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(List.of(selfStart, selfFault, rawFault), manualRoute));
      store.updateActiveFaults(store.account(), 0, resetAt - 100, 0, List.of());
      check(
          new Store(context).travelState().journeys().stream().anyMatch(j -> j.selfTriggered()),
          "self-trigger qualification persists across JSON and an old checkpoint");
      var resetEvent =
          new Rules.Event(
              "manual-reset", resetAt, "101+000", "  ấn  nút PHỤC HỒI ", false, List.of());
      store.saveForAccount(
          store.account(), new WebSource.DayData(List.of(resetEvent), manualRoute));
      store.updateActiveFaults(store.account(), resetAt + 1, resetAt + 500, 0, List.of());
      check(
          store.activeFaults().equals(List.of(rawFault))
              && Rules.trackSegments(manualRoute, store.travelState()).isEmpty(),
          "manual reset clears cached self fault and checkpointed diagram even before newest"
              + " delta");
      check(
          Rules.incidentHistory(store.incidentEvents(selfAt, selfAt + 60000)).stream()
              .anyMatch(i -> i.fault().equals(selfFault) && resetEvent.equals(i.recovery())),
          "historical self incident finds later manual recovery with original spelling");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "manual-late",
                      resetAt + 1000,
                      "101+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of())),
              manualRoute));
      store.updateActiveFaults(store.account(), resetAt + 501, resetAt + 1500, 0, List.of());
      check(
          Rules.trackSegments(manualRoute, new Store(context).travelState()).isEmpty(),
          "restart and late passage do not recreate manually reset train");
      store.prefs.edit().putInt("travelVersion", 2).commit();
      store.prepareTravelState(store.account(), resetAt + 1500);
      check(
          store.travelReady()
              && Rules.trackSegments(manualRoute, store.travelState()).isEmpty()
              && store.activeFaults().equals(List.of(rawFault)),
          "v0.10 cache migration replays manual reset without deleting device faults");
      store.credentials("https://rail.example.test/", "axle-account", "test-only");
      long axleDay = Rules.faultBaseline(0, System.currentTimeMillis());
      List<Rules.Event> measurements = new java.util.ArrayList<>();
      for (int i = 0; i < 5; i++)
        measurements.add(
            new Rules.Event(
                "axle-source-" + i,
                axleDay + 1000 + i * 1000,
                "101+000",
                "Khôi phục hệ thống",
                false,
                List.of("40", "40", "37", "40", "40", "40")));
      store.saveForAccount(store.account(), new WebSource.DayData(measurements, manualRoute));
      store.updateActiveFaults(store.account(), axleDay, axleDay + 6000, axleDay, List.of());
      check(
          store.activeFaults().isEmpty(), "five saved axle deviations do not become active alerts");
      var sixthAxle =
          new Rules.Event(
              "axle-source-sixth",
              axleDay + 7000,
              "101+000",
              "Khôi phục hệ thống",
              false,
              List.of("40", "40", "37", "40", "40", "40"));
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(sixthAxle), manualRoute));
      store.updateActiveFaults(
          store.account(), axleDay + 7000, axleDay + 8000, axleDay + 7500, List.of());
      check(
          store.activeFaults().size() == 1
              && Rules.isAxleFault(store.activeFaults().get(0))
              && store.activeFaults().get(0).text().contains("thiếu 3 trục"),
          "sixth deviation raises an alert despite later generic fault baseline");
      check(
          store.events(axleDay, axleDay + 8000).stream().filter(e -> !Rules.isGenerated(e)).count()
              == 6,
          "derived axle alert preserves all original measurements");
      String axleId = store.activeFaults().get(0).id();
      var firstAxleNotice = store.activeFaults().get(0);
      check(
          store.claimAxleNotice(firstAxleNotice, axleDay + 8000)
              && !new Store(context).claimAxleNotice(firstAxleNotice, axleDay + 8001),
          "daily axle warning notifies once even after Store restart");
      store.updateActiveFaults(store.account(), axleDay + 8001, axleDay + 9000, axleDay, List.of());
      check(
          store.activeFaults().size() == 1
              && new Store(context).activeFaults().get(0).id().equals(axleId)
              && store.events(axleDay, axleDay + 9000).size() == 7,
          "poll and Store restart retain one stable alert without counting it again");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "axle-correct",
                      axleDay + 10000,
                      "101+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of("40", "40", "40", "40", "40", "40"))),
              manualRoute));
      store.updateActiveFaults(
          store.account(), axleDay + 10000, axleDay + 11000, axleDay, List.of());
      check(
          store.activeFaults().size() == 1,
          "a later correct count keeps the statistical alert for the day");
      store.resetDailyFaults(axleDay + 86400000 + 1000);
      check(store.activeFaults().isEmpty(), "midnight resets current axle warnings");
      check(
          Rules.incidentHistory(store.incidentEvents(axleDay, axleDay + 86400000)).stream()
              .anyMatch(i -> Rules.isAxleFault(i.fault())),
          "daily reset preserves historical axle warning");
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event(
                      "axle-backfill",
                      axleDay + 500,
                      "101+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of("40", "40", "37", "40", "40", "40"))),
              manualRoute));
      store.syncAxleAlerts(store.account(), axleDay, axleDay + 86400000);
      var backfilled =
          store.events(axleDay, axleDay + 86400000).stream()
              .filter(Rules::isAxleFault)
              .collect(java.util.stream.Collectors.toList());
      check(
          backfilled.size() == 1
              && !backfilled.get(0).id().equals(axleId)
              && backfilled.get(0).at() == axleDay + 5000,
          "late earlier measurement moves sixth threshold and removes obsolete generated alert");
      check(
          !store.claimAxleNotice(backfilled.get(0), axleDay + 30000),
          "moving sixth measurement after backfill never notifies the same sensor again");
      var bigSame =
          new Rules.Event(
              "axle:3:large-test",
              axleDay + 20000,
              "101+000",
              "CB3 đếm thiếu 12 trục trong 1 lần · Sai 7 lần trong ngày",
              true,
              List.of("40", "40", "28", "40", "40", "40"));
      check(
          store.claimAxleNotice(bigSame, axleDay + 30000)
              && !store.claimAxleNotice(bigSame, axleDay + 30001),
          "new single deviation above ten notifies same sensor once per measurement");
      var otherSensor =
          new Rules.Event(
              "axle:4:other-sensor",
              axleDay + 20000,
              "101+000",
              "CB4 đếm sai 6 lần trong ngày",
              true,
              List.of("40", "40", "40", "37", "40", "40"));
      check(
          store.claimAxleNotice(otherSensor, axleDay + 30000),
          "another sensor has its own daily notification");
      check(
          !store.claimAxleNotice(firstAxleNotice, axleDay + 86400000 + 1000),
          "old day warnings do not ring on the next day");
      var newDayNotice =
          new Rules.Event(
              "axle:3:new-day",
              axleDay + 86400000 + 1000,
              "101+000",
              "CB3 đếm sai 6 lần trong ngày",
              true,
              List.of("40", "40", "37", "40", "40", "40"));
      check(
          store.claimAxleNotice(newDayNotice, axleDay + 86400000 + 2000),
          "same sensor can notify again only on a new day or a new large deviation");
      for (String exemptPoint :
          List.of("320+445", "307+507", "309+056", "280+500", "343+350", "338+900")) {
        store.credentials(
            "https://rail.example.test/", "exempt-upgrade-" + exemptPoint, "test-only");
        var exemptSource = new java.util.ArrayList<Rules.Event>();
        for (int i = 0; i < 7; i++)
          exemptSource.add(
              new Rules.Event(
                  "exempt-source-" + i,
                  axleDay + 1000 + i * 1000,
                  "Km " + exemptPoint,
                  "Khôi phục hệ thống",
                  false,
                  List.of("40", "40", i == 6 ? "28" : "37", "40", "40", "40")));
        var legacyExempt =
            new Rules.Event(
                "axle:3:legacy-exempt",
                axleDay + 7000,
                "Km " + exemptPoint,
                "CB3 đếm thiếu 12 trục trong 1 lần",
                true,
                List.of("40", "40", "28", "40", "40", "40"));
        var legacyOldDay =
            new Rules.Event(
                "axle:3:legacy-exempt-yesterday",
                axleDay - 1000,
                exemptPoint,
                "CB3 đếm sai 6 lần trong ngày",
                true,
                List.of("40", "40", "37", "40", "40", "40"));
        var cacheOnlyExempt =
            new Rules.Event(
                "axle:4:legacy-cache-only",
                axleDay + 7000,
                exemptPoint,
                "CB4 đếm thiếu 12 trục trong 1 lần",
                true,
                List.of());
        var actualExempt =
            new Rules.Event(
                "actual-exempt-red",
                axleDay + 9000,
                exemptPoint,
                "Cảm biến 3 hỏng",
                true,
                List.of());
        var neighborSource =
            new Rules.Event(
                "normal-neighbor",
                axleDay + 9000,
                "320+957",
                "Khôi phục hệ thống",
                false,
                List.of("40", "40", "30", "40", "40", "40"));
        exemptSource.addAll(List.of(legacyExempt, legacyOldDay, actualExempt, neighborSource));
        store.saveForAccount(
            store.account(),
            new WebSource.DayData(exemptSource, new Rules.Route(List.of(exemptPoint, "320+957"))));
        var oldCache = new org.json.JSONArray();
        var normalDaily =
            new Rules.Event(
                "axle:3:normal-daily-already-notified",
                axleDay + 9000,
                "320+957",
                "CB3 đếm sai 6 lần trong ngày",
                true,
                List.of("40", "40", "37", "40", "40", "40"));
        check(
            store.claimAxleNotice(normalDaily, axleDay + 20000),
            "normal crossing records its one-time daily notice before migration");
        for (var e : List.of(legacyExempt, cacheOnlyExempt, actualExempt))
          oldCache.put(
              new org.json.JSONArray()
                  .put(e.id())
                  .put(e.at())
                  .put(e.crossing())
                  .put(e.text())
                  .put(new org.json.JSONArray(e.counts())));
        store
            .prefs
            .edit()
            .putString("activeFaults", oldCache.toString())
            .putInt("axleVersion", exemptPoint.equals("320+445") ? 1 : 2)
            .commit();
        check(!store.axleReady(), "previous exclusion set forces migration for " + exemptPoint);
        check(
            store.activeFaults().equals(List.of(actualExempt)),
            "v0.12 cached 320+445 axle alert is hidden immediately without hiding its hardware"
                + " fault");
        check(
            !store.claimAxleNotice(legacyExempt, axleDay + 20000)
                && store.claimAxleNotice(actualExempt, axleDay + 20000),
            "excluded axle alert cannot notify but original red fault can");
        var manager = context.getSystemService(android.app.NotificationManager.class);
        manager.createNotificationChannel(
            new android.app.NotificationChannel(
                "exempt-upgrade-test",
                "Fixture only",
                android.app.NotificationManager.IMPORTANCE_LOW));
        int obsoleteNotification = 100 + Math.floorMod(legacyExempt.id().hashCode(), 1000000);
        manager.notify(
            obsoleteNotification,
            new android.app.Notification.Builder(context, "exempt-upgrade-test")
                .setSmallIcon(vn.duongngang.monitor.R.drawable.ic_train_header)
                .setContentTitle("Synthetic upgrade check")
                .build());
        int cacheOnlyNotification = 100 + Math.floorMod(cacheOnlyExempt.id().hashCode(), 1000000);
        manager.notify(
            cacheOnlyNotification,
            new android.app.Notification.Builder(context, "exempt-upgrade-test")
                .setSmallIcon(vn.duongngang.monitor.R.drawable.ic_train_header)
                .setContentTitle("Synthetic cache-only check")
                .build());
        store.updateActiveFaults(store.account(), axleDay, axleDay + 20000, axleDay, List.of());
        check(
            !store.claimAxleNotice(normalDaily, axleDay + 20000),
            "exemption migration preserves another crossing's daily notification suppression");
        check(
            store.axleReady()
                && store.activeFaults().size() == 2
                && store.activeFaults().stream().anyMatch(e -> e.id().equals(actualExempt.id()))
                && store.activeFaults().stream()
                    .anyMatch(e -> Rules.isAxleFault(e) && e.crossing().equals("320+957")),
            "upgrade keeps hardware and normal-crossing axle warnings while exempting 320+445");
        check(
            java.util.Arrays.stream(manager.getActiveNotifications())
                .noneMatch(
                    n -> n.getId() == obsoleteNotification || n.getId() == cacheOnlyNotification),
            "upgrade cancels obsolete axle notifications including a cache-only alert");
        manager.deleteNotificationChannel("exempt-upgrade-test");
        try (var db = context.openOrCreateDatabase("events.db", 0, null);
            var c =
                db.rawQuery(
                    "SELECT COUNT(*) FROM events WHERE id IN (?,?)",
                    new String[] {legacyExempt.id(), legacyOldDay.id()})) {
          c.moveToFirst();
          check(c.getInt(0) == 0, "upgrade deletes derived exempt alerts from every stored day");
        }
        check(
            store.events(axleDay, axleDay + 20000).stream()
                    .filter(e -> e.id().startsWith("exempt-source-"))
                    .count()
                == 7,
            "upgrade preserves all original 320+445 measurements");
        check(
            Rules.incidentHistory(store.incidentEvents(axleDay - 86400000, axleDay)).isEmpty(),
            "historical incident screen does not recreate exempt axle warnings");
      }
      store.credentials("https://rail.example.test/", "self-evidence-upgrade", "test-only");
      var proofRoute =
          new Rules.Route(
              List.of("100+000", "101+000", "102+000", "103+000", "104+000", "105+000"));
      long proofNow = axleDay + 300000;
      var proof =
          new Rules.Event(
              "barrier-proof", axleDay + 100000, "100+000", "Cần chắn 2 đã nâng", false, List.of());
      var arrival =
          new Rules.Event(
              "proved-arrival", axleDay + 110000, "101+000", "Tàu lẻ", false, List.of());
      var oldSelf =
          new Rules.Event(
              "self:" + arrival.id(),
              arrival.at(),
              arrival.crossing(),
              "Nghi tự kích",
              true,
              List.of());
      var realBell =
          new Rules.Event(
              "real-bell", axleDay + 111000, "101+000", "Chuông 2 hỏng", true, List.of());
      var isolated =
          new Rules.Event(
              "isolated-arrival", axleDay + 120000, "103+000", "Tàu lẻ", false, List.of());
      var genuineSelf =
          new Rules.Event(
              "self:" + isolated.id(),
              isolated.at(),
              isolated.crossing(),
              "Nghi tự kích",
              true,
              List.of());
      var cacheArrival =
          new Rules.Event("cache-arrival", axleDay + 130000, "105+000", "Tàu lẻ", false, List.of());
      var latePassage =
          new Rules.Event(
              "late-passage",
              axleDay + 131000,
              "104+000",
              "Tàu lẻ đã qua đường ngang",
              false,
              List.of());
      var cacheSelf =
          new Rules.Event(
              "self:" + cacheArrival.id(),
              cacheArrival.at(),
              cacheArrival.crossing(),
              "Nghi tự kích",
              true,
              List.of());
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  proof,
                  arrival,
                  oldSelf,
                  realBell,
                  isolated,
                  genuineSelf,
                  cacheArrival,
                  latePassage),
              proofRoute));
      store.updateActiveFaults(store.account(), axleDay, proofNow, axleDay, List.of());
      check(
          store.travelState().journeys().stream()
              .anyMatch(j -> j.id().equals(arrival.id()) && j.selfTriggered()),
          "legacy inference marks the diagram before migration");
      var selfCache = new org.json.JSONArray();
      for (var e : List.of(oldSelf, realBell, genuineSelf, cacheSelf))
        selfCache.put(
            new org.json.JSONArray()
                .put(e.id())
                .put(e.at())
                .put(e.crossing())
                .put(e.text())
                .put(new org.json.JSONArray(e.counts())));
      store
          .prefs
          .edit()
          .putString("activeFaults", selfCache.toString())
          .putString(
              "pendingSelf",
              new org.json.JSONObject()
                  .put(arrival.id(), 1000L)
                  .put(isolated.id(), 2000L)
                  .put(cacheArrival.id(), 3000L)
                  .toString())
          .commit();
      check(!store.selfEvidenceReady(), "install-over has a pending self-evidence migration");
      var selfManager = context.getSystemService(android.app.NotificationManager.class);
      selfManager.createNotificationChannel(
          new android.app.NotificationChannel(
              "self-proof-test", "Fixture only", android.app.NotificationManager.IMPORTANCE_LOW));
      for (var e : List.of(oldSelf, cacheSelf, genuineSelf))
        selfManager.notify(
            100 + Math.floorMod(e.id().hashCode(), 1000000),
            new android.app.Notification.Builder(context, "self-proof-test")
                .setSmallIcon(vn.duongngang.monitor.R.drawable.ic_train_header)
                .setContentTitle("Synthetic self proof")
                .build());
      store.prepareSelfEvidence(store.account(), proofNow);
      check(store.selfEvidenceReady(), "self-evidence migration records its version");
      check(
          store.activeFaults().stream()
              .noneMatch(e -> e.id().equals(oldSelf.id()) || e.id().equals(cacheSelf.id())),
          "successful movement or later passage removes legacy and cache-only false self alerts");
      check(
          store.activeFaults().stream().anyMatch(e -> e.id().equals(realBell.id()))
              && store.activeFaults().stream().anyMatch(e -> e.id().equals(genuineSelf.id())),
          "migration retains original hardware faults and still-unsupported self-trigger");
      check(
          store.events(axleDay, proofNow).stream()
                  .filter(
                      e ->
                          e.id().equals(proof.id())
                              || e.id().equals(arrival.id())
                              || e.id().equals(realBell.id()))
                  .count()
              == 3,
          "migration preserves raw movement, arrival and hardware records");
      check(
          store.events(axleDay, proofNow).stream().noneMatch(e -> e.id().equals(oldSelf.id())),
          "migration removes disproved inference from archive rather than faking hardware"
              + " recovery");
      check(
          store.pendingSelf().equals(java.util.Map.of(isolated.id(), 2000L)),
          "pending candidates are cleared selectively and genuine first-observed time is retained");
      check(
          store.travelState().journeys().stream()
              .noneMatch(j -> j.id().equals(arrival.id()) && j.selfTriggered()),
          "diagram replay drops disproved classification without deleting real train reports");
      check(
          java.util.Arrays.stream(selfManager.getActiveNotifications())
              .noneMatch(
                  n ->
                      n.getId() == 100 + Math.floorMod(oldSelf.id().hashCode(), 1000000)
                          || n.getId() == 100 + Math.floorMod(cacheSelf.id().hashCode(), 1000000)),
          "migration cancels disproved notifications including cache-only one");
      check(
          java.util.Arrays.stream(selfManager.getActiveNotifications())
              .anyMatch(
                  n -> n.getId() == 100 + Math.floorMod(genuineSelf.id().hashCode(), 1000000)),
          "migration does not cancel genuine self-trigger notification");
      int proofRows = store.events(0, proofNow).size();
      store.prepareSelfEvidence(store.account(), proofNow);
      check(
          store.events(0, proofNow).size() == proofRows,
          "migration is idempotent and creates no historic alarms");
      selfManager.deleteNotificationChannel("self-proof-test");
      store.credentials("https://rail.example.test/", "older-self-proof", "test-only");
      long archiveAt = axleDay - 172800000;
      var oldProof =
          new Rules.Event(
              "old-proof", archiveAt + 1000, "100+000", "Cần chắn 1 đã hạ", false, List.of());
      var oldArrival =
          new Rules.Event("old-arrival", archiveAt + 2000, "101+000", "Tàu lẻ", false, List.of());
      var oldInference =
          new Rules.Event(
              "self:" + oldArrival.id(),
              oldArrival.at(),
              oldArrival.crossing(),
              "Nghi tự kích",
              true,
              List.of());
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(List.of(oldProof, oldArrival, oldInference), proofRoute));
      store.updateActiveFaults(store.account(), archiveAt, proofNow, axleDay, List.of());
      check(
          store.travelState().journeys().stream()
              .anyMatch(j -> j.id().equals(oldArrival.id()) && j.selfTriggered()),
          "old checkpoint contains the earlier classification before reconciliation");
      store.prepareSelfEvidence(store.account(), proofNow);
      check(
          store.events(archiveAt, archiveAt + 10000).stream().noneMatch(Rules::isSelfTrigger),
          "migration also removes older disproved history");
      check(
          store.travelState().journeys().stream()
              .noneMatch(j -> j.id().equals(oldArrival.id()) && j.selfTriggered()),
          "disproved record before checkpoint forces correct diagram rebuild");
      store.credentials("https://rail.example.test/", "new-axle-account", "test-only");
      check(
          store.activeFaults().isEmpty()
              && !store.prefs.contains("axleVersion")
              && !store.prefs.contains("axleNotices")
              && !store.prefs.contains("selfEvidenceVersion")
              && store.events(axleDay, axleDay + 86400000).isEmpty(),
          "changing account clears axle counters and generated alerts");
      store.credentials("https://rail.example.test/", "Chosy", "test-only");
      check(
          store.route().points().isEmpty(), "Chosy has no route before successful inventory read");
      var oldChosyRoute = new Rules.Route(List.of("300+600", "305+888", "307+507"));
      String station = "303+350 (Nghi Long)";
      long stationNow = System.currentTimeMillis();
      long stationAt = stationNow - 10000;
      var stationArrival =
          new Rules.Event("station-arrival", stationAt, "305+888", "Tàu lẻ", false, List.of());
      var wrongInference =
          new Rules.Event(
              "self:station-arrival", stationAt, "305+888", "Nghi tự kích", true, List.of());
      var stationBell =
          new Rules.Event("station-bell", stationAt, "305+888", "Chuông 2 hỏng", true, List.of());
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(stationArrival, wrongInference, stationBell), oldChosyRoute));
      check(
          store.route().points().contains(station), "successful save supplements Chosy inventory");
      check(
          store.route().crossings().equals(oldChosyRoute.crossings()),
          "station does not increase crossing count or monitoring choices");
      store.updateActiveFaults(store.account(), stationAt, stationNow, stationAt, List.of());
      // Simulate the previous APK: bare cached web inventory and a ready old-topology diagram.
      store
          .prefs
          .edit()
          .putString("route", new org.json.JSONArray(oldChosyRoute.points()).toString())
          .putString("travelRoute", new org.json.JSONArray(oldChosyRoute.points()).toString())
          .putInt("selfEvidenceVersion", 1)
          .putString(
              "pendingSelf",
              new org.json.JSONObject(java.util.Map.of("station-arrival", stationAt)).toString())
          .commit();
      check(
          store.route().points().contains(station),
          "install-over cached inventory gains station without waiting for network");
      check(!store.travelReady(), "old topology cannot be drawn as if it includes the station");
      check(!store.selfEvidenceReady(), "new station requires reassessing old self-trigger cache");
      store.prepareSelfEvidence(store.account(), stationNow);
      store.prepareTravelState(store.account(), stationNow);
      check(
          store.selfEvidenceReady() && store.travelReady(),
          "station migration completes both inference and diagram caches");
      check(
          store.activeFaults().size() == 1
              && store.activeFaults().get(0).id().equals(stationBell.id()),
          "station removes cross-station inference while preserving real website fault");
      check(store.pendingSelf().isEmpty(), "cross-station pending inference is removed");
      check(
          store.events(stationAt, stationNow + 1).size() == 2,
          "migration preserves original reports and does not create station events");
      check(
          Rules.trackSegments(store.route(), store.travelState())
              .equals(List.of(new Rules.TrackSegment(station, "305+888", 1))),
          "replayed diagram shows train leaving Nghi Long rather than crossing through station");
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(), oldChosyRoute));
      check(
          store.route().points().stream().filter(station::equals).count() == 1,
          "later polls retain one station even while website omits it");
      store.credentials("https://rail.example.test/", "unrelated-account", "test-only");
      store.saveForAccount(store.account(), new WebSource.DayData(List.of(), oldChosyRoute));
      check(
          store.route().points().equals(oldChosyRoute.points()),
          "changing ID removes Chosy-only supplement");
      store.credentials("https://rail.example.test/", "diagram-sparse", "test-only");
      var sparseRoute =
          new Rules.Route(List.of("199+000 (A)", "200+000", "201+000", "202+000 (B)"));
      long replayNow = System.currentTimeMillis();
      long replayBoundary =
          java.time.Instant.ofEpochMilli(replayNow)
              .atZone(WebSource.ZONE)
              .toLocalDate()
              .minusDays(1)
              .atStartOfDay(WebSource.ZONE)
              .toInstant()
              .toEpochMilli();
      long originAt = replayBoundary - 60000;
      var originalArrival =
          new Rules.Event("sparse-origin", originAt, "200+000", "Tàu lẻ", false, List.of());
      var originalConnection =
          new Rules.Event(
              "sparse-network",
              replayNow - 1000,
              "201+000",
              "Mất kết nối giám sát",
              true,
              List.of());
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  originalArrival,
                  new Rules.Event(
                      "sparse-pass",
                      originAt + 10000,
                      "200+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "sparse-source-reset",
                      originAt + 20000,
                      "200+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of()),
                  new Rules.Event(
                      "sparse-motion",
                      replayBoundary - 10000,
                      "201+000",
                      "Cần chắn 1 đã hạ",
                      false,
                      List.of()),
                  new Rules.Event(
                      "sparse-destination-reset",
                      replayBoundary + 10000,
                      "201+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of()),
                  originalConnection),
              sparseRoute));
      store.updateActiveFaults(store.account(), originAt, replayNow, 0, List.of());
      String preservedConnection = store.prefs.getString("activeFaults", "[]");
      int sparseRawCount = store.events(0, replayNow + 1).size();
      var obsolete =
          new org.json.JSONArray()
              .put(
                  new org.json.JSONArray()
                      .put(originalArrival.id())
                      .put("200+000")
                      .put("201+000")
                      .put(1)
                      .put(originAt)
                      .put(true)
                      .put(true)
                      .put(false)
                      .put(org.json.JSONObject.NULL)
                      .put(originAt + 20000)
                      .put(false))
              .toString();
      store
          .prefs
          .edit()
          .putInt("travelVersion", 3)
          .putString("travelCheckpoint", obsolete)
          .putString("travelState", obsolete)
          .putLong("travelCheckpointAt", replayBoundary)
          .commit();
      check(
          !store.travelReady(), "version-three diagram is withheld during sparse-visit migration");
      store.prepareTravelState(store.account(), replayNow);
      check(
          store.travelReady()
              && store.prefs.getInt("travelVersion", 0) == 4
              && Rules.trackSegments(store.route(), store.travelState()).isEmpty(),
          "upgrade replays source and motion before checkpoint and restoration after checkpoint");
      check(
          store.prefs.getString("travelCheckpoint", "").contains("travel:motion:"),
          "motion-derived visit persists at checkpoint when restoration belongs to later replay"
              + " window");
      check(
          Rules.trackSegments(store.route(), new Store(context).travelState()).isEmpty(),
          "restarting after sparse migration cannot resurrect the old interval");
      check(
          store.events(0, replayNow + 1).size() == sparseRawCount
              && store.prefs.getString("activeFaults", "[]").equals(preservedConnection),
          "diagram migration preserves every raw report and original network fault");
      long lostAt = replayNow - 600000, nextAt = replayNow - 300000;
      store.saveForAccount(
          store.account(),
          new WebSource.DayData(
              List.of(
                  new Rules.Event("lost-start", lostAt, "200+000", "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "lost-pass",
                      lostAt + 1000,
                      "200+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "lost-reset",
                      lostAt + 2000,
                      "200+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of()),
                  new Rules.Event("next-start", nextAt, "200+000", "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "next-destination", nextAt + 1000, "201+000", "Tàu lẻ", false, List.of()),
                  new Rules.Event(
                      "next-pass",
                      nextAt + 2000,
                      "200+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "next-destination-pass",
                      nextAt + 3000,
                      "201+000",
                      "Tàu lẻ đã qua đường ngang",
                      false,
                      List.of()),
                  new Rules.Event(
                      "next-reset",
                      nextAt + 4000,
                      "200+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of()),
                  new Rules.Event(
                      "next-destination-reset",
                      nextAt + 5000,
                      "201+000",
                      "Khôi phục hệ thống",
                      false,
                      List.of())),
              sparseRoute));
      store.updateActiveFaults(store.account(), lostAt, replayNow, 0, List.of());
      check(
          Rules.trackSegments(store.route(), new Store(context).travelState()).isEmpty(),
          "next complete train heals a wholly missing destination visit and persists across"
              + " restart");
      check(
          store.activeFaults().size() == 1
              && store.activeFaults().get(0).id().equals(originalConnection.id()),
          "new-train diagram recovery never pretends that network hardware has recovered");
      check(
          store.password().equals("test-only")
              && store.prefs.getString("user", "").equals("diagram-sparse"),
          "cache migration and replay preserve credentials");
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
