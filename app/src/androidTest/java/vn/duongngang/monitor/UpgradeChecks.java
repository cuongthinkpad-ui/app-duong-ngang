package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.Context;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Synthetic data on a dedicated emulator; run seed on 0.27, then verify after install -r. */
final class UpgradeChecks {
  static String run(Instrumentation runner, boolean seed) throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu"))
      throw new IllegalStateException("Dedicated emulator only");
    Context context = runner.getTargetContext();
    Store store = new Store(context);
    long version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).getLongVersionCode();
    if (seed) {
      if (version != 27) throw new AssertionError("Seed must run against original 0.27 APK");
      String user = store.prefs.getString("user", "");
      if (!user.isEmpty() && !user.equals("upgrade-fixture"))
        throw new IllegalStateException("Refusing to replace existing account");
      store.credentials("http://127.0.0.1:9/", "upgrade-fixture", "synthetic-upgrade-password");
      long now = System.currentTimeMillis();
      store.prefs.edit().putBoolean("sound", false).putString("alertMode", "silent")
          .putString("audibleMode", "voice").putString("rangeFrom", "100+000")
          .putString("rangeTo", "101+000").putInt("interval", 30)
          .putLong("upgradeFixtureAt", now).commit();
      store.save(List.of(new Rules.Event("upgrade-fixture-event", now, "100+000", "Chuông 1 hỏng", true, List.of())));
      try (var output = context.openFileOutput("upgrade-fixture-marker", Context.MODE_PRIVATE)) {
        output.write("preserve-private-data".getBytes(StandardCharsets.UTF_8));
      }
      return "Upgrade fixture seeded on original 0.27\n";
    }
    if (version != 29) throw new AssertionError("Verify must run against 0.27.2 APK");
    if (!store.password().equals("synthetic-upgrade-password")) throw new AssertionError("Encrypted password or Android Keystore lost");
    if (!store.prefs.getString("user", "").equals("upgrade-fixture")
        || !store.prefs.getString("url", "").equals("http://127.0.0.1:9/")) throw new AssertionError("Account lost");
    if (store.prefs.getBoolean("sound", true) || !store.alertMode().equals("silent")
        || !store.prefs.getString("audibleMode", "").equals("voice")) throw new AssertionError("Audio settings lost");
    if (!store.prefs.getString("rangeFrom", "").equals("100+000")
        || !store.prefs.getString("rangeTo", "").equals("101+000")
        || store.prefs.getInt("interval", 0) != 30) throw new AssertionError("Scope or polling lost");
    long at = store.prefs.getLong("upgradeFixtureAt", 0);
    if (at == 0 || store.events(0, System.currentTimeMillis() + 1).stream().noneMatch(e -> e.id().equals("upgrade-fixture-event")))
      throw new AssertionError("Raw database event lost");
    try (var input = context.openFileInput("upgrade-fixture-marker")) {
      byte[] bytes = new byte[64];
      int size = input.read(bytes);
      if (size < 0 || !new String(bytes, 0, size, StandardCharsets.UTF_8).equals("preserve-private-data"))
        throw new AssertionError("Private file lost");
    }
    return "6 install-over data checks passed (0.27 -> 0.27.1 -> 0.27.2)\n";
  }
}
