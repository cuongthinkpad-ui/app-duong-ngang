package vn.duongngang.monitor;

import android.app.Instrumentation;
import android.content.*;
import android.media.AudioManager;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.BooleanSupplier;

/** Exercises the installed Vietnamese TTS engine, real utterance callbacks and cancellation. */
final class VoiceChecks {
  private final Instrumentation runner;
  private int checks;
  VoiceChecks(Instrumentation runner) { this.runner = runner; }
  private void check(boolean ok, String text) { checks++; if (!ok) throw new AssertionError(text); }
  private void await(BooleanSupplier condition, String text) throws Exception {
    long until = System.currentTimeMillis() + 240000;
    while (System.currentTimeMillis() < until) {
      if (condition.getAsBoolean()) return;
      Thread.sleep(100);
    }
    throw new AssertionError(text);
  }
  private void ui(Runnable action) {
    Throwable[] failure = {null};
    runner.runOnMainSync(() -> { try { action.run(); } catch (Throwable t) { failure[0] = t; } });
    runner.waitForIdleSync();
    if (failure[0] != null) throw new AssertionError(failure[0]);
  }
  private static Object field(Object o, String name) throws Exception {
    Field f = o.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(o);
  }
  private View tagged(View view, String tag) {
    if (tag.equals(view.getTag())) return view;
    if (view instanceof ViewGroup group) for (int i = 0; i < group.getChildCount(); i++) {
      View found = tagged(group.getChildAt(i), tag); if (found != null) return found;
    }
    return null;
  }
  int runUnavailable() throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu")) throw new IllegalStateException("Dedicated emulator only");
    List<String> statuses = new CopyOnWriteArrayList<>();
    AtomicInteger starts = new AtomicInteger();
    VoiceAlarm voice = new VoiceAlarm(runner.getTargetContext(), statuses::add);
    try {
      voice.enqueue(List.of(new Rules.Event("missing-voice",1,"100+000","Chuông 1 hỏng",true,List.of())),
          () -> true, starts::incrementAndGet);
      await(() -> starts.get()==1 && !voice.isPlaying(), "unavailable Vietnamese voice did not finish safely");
      check(voice.completedUtterances()==0, "unavailable engine cannot claim completed speech");
      check(statuses.stream().anyMatch(s -> s.contains("Chưa có giọng tiếng Việt") || s.contains("Chưa khởi tạo")),
          "missing language is reported clearly");
      check(voice.chimeFinishedAt()>0, "two chimes still complete when speech is unavailable");
      check(field(voice,"vibrator")==null && field(voice,"player")==null,
          "missing speech cannot leave vibration or audio running");
    } finally { voice.close(); }
    return checks;
  }

  int run() throws Exception {
    if (!android.os.Build.HARDWARE.contains("ranchu")) throw new IllegalStateException("Dedicated emulator only");
    Context context = runner.getTargetContext();
    var audio = context.getSystemService(AudioManager.class);
    int volume = audio.getStreamVolume(AudioManager.STREAM_ALARM);
    AlarmSound bell = new AlarmSound();
    try {
      ui(() -> bell.play(context));
      check(bell.isPlaying(), "bell-only warning starts");
      ui(bell::stop);
      check(!bell.isPlaying() && field(bell, "player") == null && field(bell, "vibrator") == null,
          "stop releases bell-only audio and vibration");
      ui(() -> bell.play(context));
      check(bell.isPlaying(), "new bell warning can start after stopping the previous one");
    } finally { ui(bell::stop); }
    List<String> statuses = new CopyOnWriteArrayList<>(), delivered = new CopyOnWriteArrayList<>();
    VoiceAlarm voice = new VoiceAlarm(context, message -> { statuses.add(message); android.util.Log.i("VoiceCheck", message); }, e -> delivered.add(e.id()));
    AtomicInteger starts = new AtomicInteger();
    AtomicBoolean allowed = new AtomicBoolean(true);
    var a = new Rules.Event("voice-a", 1000, "100+000", "Chuông 1 hỏng", true, List.of());
    var b = new Rules.Event("voice-b", 2000, "101+000", "Cầu chì 50A bị đứt", false, List.of());
    try {
      voice.enqueue(List.of(a), allowed::get, starts::incrementAndGet);
      await(() -> voice.speechStartedAt() > 0, "Vietnamese TTS did not start: " + statuses);
      check(voice.chimeFinishedAt() > 0 && voice.speechStartedAt() >= voice.chimeFinishedAt(),
          "Vietnamese speech starts only after the two-chime recording completes");
      check(statuses.stream().anyMatch(s -> s.contains("tiếng Việt sẵn sàng")), "engine accepts Vietnamese");
      var engine = (android.speech.tts.TextToSpeech) field(voice, "speech");
      check(Set.of("vi", "vie").contains(engine.getVoice().getLocale().getLanguage()), "selected real engine voice is Vietnamese");
      voice.enqueue(List.of(a, b), allowed::get, starts::incrementAndGet);
      await(() -> delivered.size() == 12 && !voice.isPlaying(), "six complete readings per queued warning: " + delivered + statuses);
      check(Collections.frequency(delivered, a.id()) == 6 && Collections.frequency(delivered, b.id()) == 6,
          "real TTS completes each warning exactly six times, without duplicating an already queued ID");
      check(delivered.subList(0, 6).stream().allMatch(a.id()::equals)
          && delivered.subList(6, 12).stream().allMatch(b.id()::equals), "new warning waits for current six readings");
      check(starts.get() == 2, "each new batch gets one two-chime introduction");
      check(field(voice, "vibrator") == null && field(voice, "player") == null, "completed reading releases tone and vibration");
      check(audio.getStreamVolume(AudioManager.STREAM_ALARM) == volume, "speech never forces system volume");

      var c = new Rules.Event("mute-intro", 3000, "100+000", "Đèn 1 hỏng", true, List.of());
      voice.enqueue(List.of(c), allowed::get, starts::incrementAndGet);
      await(() -> starts.get() == 3, "intro did not start");
      voice.stop();
      await(() -> !voice.isPlaying(), "stop did not cancel intro");
      Thread.sleep(4500);
      check(!delivered.contains(c.id()) && voice.speechStartedAt() == 0, "muting chimes cannot start delayed speech later");
      check(field(voice, "vibrator") == null && field(voice, "player") == null, "mute cancels audio and vibration");

      var d = new Rules.Event("mute-speech", 4000, "100+000", "Chuông 2 hỏng", true, List.of());
      voice.enqueue(List.of(d), allowed::get, starts::incrementAndGet);
      await(() -> voice.speechStartedAt() > 0, "speech for mute check did not start");
      voice.stop(); await(() -> !voice.isPlaying(), "stop did not cancel speech");
      int stopped = voice.completedUtterances();
      Thread.sleep(1500);
      check(voice.completedUtterances() == stopped && Collections.frequency(delivered, d.id()) < 6,
          "muting an active utterance cancels the remaining repetitions");

      var e = new Rules.Event("resolved", 5000, "100+000", "Đèn 2 hỏng", true, List.of());
      var f = new Rules.Event("still-open", 6000, "101+000", "Chuông 3 hỏng", true, List.of());
      ui(() -> voice.enqueue(List.of(e), allowed::get, starts::incrementAndGet));
      await(() -> voice.speechStartedAt() > 0, "resolved check did not begin");
      voice.enqueue(List.of(f), allowed::get, starts::incrementAndGet);
      voice.retain(Set.of(f.id()));
      await(() -> Collections.frequency(delivered, f.id()) == 6 && !voice.isPlaying(), "remaining independent fault was lost");
      check(Collections.frequency(delivered, e.id()) < 6 && Collections.frequency(delivered, f.id()) == 6,
          "recovery stops the exact warning and preserves another queued warning");
      allowed.set(false);
      int before = starts.get();
      voice.enqueue(List.of(a), allowed::get, starts::incrementAndGet);
      Thread.sleep(500);
      check(starts.get() == before && !voice.isPlaying(), "silent/history guard prevents any introduction or speech");
    } finally { voice.close(); }

    Context isolated = context.createDeviceProtectedStorageContext();
    Store store = new Store(isolated);
    store.prefs.edit().clear().commit();
    MainActivity activity = null;
    try {
      store.prefs.edit().putBoolean("sound", false).commit();
      check(store.alertMode().equals(AlertOptions.SILENT), "upgrade preserves old global mute");
      store.setAlertMode(AlertOptions.BELL);
      store.setAlertMode(AlertOptions.SILENT);
      check(!store.prefs.getBoolean("sound", true) && store.prefs.getString("audibleMode", "").equals(AlertOptions.BELL),
          "mute remembers previous audible mode");
      store.setAlertMode(store.prefs.getString("audibleMode", ""));
      check(store.alertMode().equals(AlertOptions.BELL), "unmute restores bell selection");
      activity = (MainActivity) runner.startActivitySync(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      MainActivity screen = activity;
      ui(() -> {
        try {
          Field sf = MainActivity.class.getDeclaredField("store"); sf.setAccessible(true); sf.set(screen, store);
          Field pf = MainActivity.class.getDeclaredField("page"); pf.setAccessible(true); pf.set(screen, "settings");
          Method render = MainActivity.class.getDeclaredMethod("render"); render.setAccessible(true); render.invoke(screen);
          View content = (View) field(screen, "content");
          for (String mode : List.of(AlertOptions.VOICE, AlertOptions.BELL, AlertOptions.SILENT)) {
            RadioButton choice = (RadioButton) tagged(content, mode);
            if (choice != null) choice.performClick();
            check(choice != null && choice.isChecked() && store.alertMode().equals(mode),
                "real settings control selects " + mode + ": " + store.alertMode());
          }
        } catch (Exception failure) { throw new AssertionError(failure); }
      });
      ui(() -> {
        try {
          View choice = tagged((View) field(screen, "content"), AlertOptions.VOICE);
          choice.requestRectangleOnScreen(new android.graphics.Rect(0, 0, choice.getWidth(), choice.getHeight()), true);
        } catch (Exception failure) { throw new AssertionError(failure); }
      });
      UiPreview.shot(runner, activity, "voice-settings-0.27.png");
      check(!store.prefs.getBoolean("sound", true), "silent settings selection disables all audible alerts");
    } finally {
      if (activity != null) { MainActivity screen = activity; ui(screen::finish); }
      store.prefs.edit().clear().commit(); isolated.deleteDatabase("events.db");
    }
    return checks;
  }
}
