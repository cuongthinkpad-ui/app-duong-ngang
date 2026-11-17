package vn.duongngang.monitor;

import android.content.Context;
import android.media.*;
import android.os.*;
import android.speech.tts.*;
import java.util.*;
import java.util.function.*;

/** Two chimes, then six complete readings. Main-thread ownership serializes engine callbacks. */
final class VoiceAlarm {
  private record Batch(List<Rules.Event> events, BooleanSupplier allowed, Runnable started) {}
  private final Context context;
  private final Consumer<String> status;
  private final Consumer<Rules.Event> utteranceCompleted;
  private final Handler main = new Handler(Looper.getMainLooper());
  private final ArrayDeque<Batch> queue = new ArrayDeque<>();
  private final Set<String> open = new HashSet<>();
  private final AudioAttributes attributes = new AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_ALARM)
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
  private Batch current;
  private MediaPlayer player;
  private Vibrator vibrator;
  private AudioManager manager;
  private AudioFocusRequest focus;
  private TextToSpeech speech;
  private boolean initializing, ready, closed, chimeDone;
  private int generation, cycle, item, chunk;
  private String expected, speakingEvent;
  private Runnable timeout;
  private volatile boolean active;
  private volatile int completed;
  private volatile long chimeFinishedAt, speechStartedAt;

  VoiceAlarm(Context context, Consumer<String> status) { this(context, status, event -> {}); }

  VoiceAlarm(Context context, Consumer<String> status, Consumer<Rules.Event> utteranceCompleted) {
    this.context = context.getApplicationContext();
    this.status = status;
    this.utteranceCompleted = utteranceCompleted;
  }

  private void dispatch(Runnable action) {
    if (Looper.myLooper() == Looper.getMainLooper()) action.run(); else main.post(action);
  }

  void enqueue(List<Rules.Event> alerts, BooleanSupplier allowed, Runnable started) {
    List<Rules.Event> copy = List.copyOf(alerts);
    dispatch(() -> {
      if (closed || copy.isEmpty() || !allowed.getAsBoolean()) return;
      Set<String> queued = new HashSet<>();
      if (current != null) for (var e : current.events()) queued.add(e.id());
      for (var batch : queue) for (var e : batch.events()) queued.add(e.id());
      List<Rules.Event> fresh = new ArrayList<>();
      for (var e : copy) if (queued.add(e.id())) { fresh.add(e); open.add(e.id()); }
      if (fresh.isEmpty()) return;
      queue.add(new Batch(List.copyOf(fresh), allowed, started));
      if (current == null) nextBatch();
    });
  }

  void retain(Set<String> ids) {
    Set<String> copy = Set.copyOf(ids);
    dispatch(() -> {
      open.retainAll(copy);
      queue.removeIf(b -> b.events().stream().noneMatch(e -> open.contains(e.id())));
      if (current == null) return;
      if (current.events().stream().noneMatch(e -> open.contains(e.id()))) cancelBatch();
      else if (expected != null && !open.contains(speakingEvent)) {
        clearTimeout();
        expected = null;
        if (speech != null) speech.stop();
        item++; chunk = 0;
        readNext();
      }
    });
  }

  boolean isPlaying() { return active; }
  int completedUtterances() { return completed; }
  long chimeFinishedAt() { return chimeFinishedAt; }
  long speechStartedAt() { return speechStartedAt; }

  private void nextBatch() {
    if (closed) return;
    while (!queue.isEmpty()) {
      Batch b = queue.removeFirst();
      if (b.allowed().getAsBoolean() && b.events().stream().anyMatch(e -> open.contains(e.id()))) {
        current = b; break;
      }
    }
    if (current == null) { active = false; return; }
    active = true;
    cycle = item = chunk = 0;
    chimeDone = false;
    chimeFinishedAt = speechStartedAt = 0;
    int token = ++generation;
    manager = context.getSystemService(AudioManager.class);
    focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener(change -> {
          if (token == generation && (change == AudioManager.AUDIOFOCUS_LOSS
              || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)) stop();
        }).build();
    if (manager == null || manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      stopNow(); return;
    }
    ensureSpeech();
    try {
      player = new MediaPlayer();
      player.setAudioAttributes(attributes);
      try (var fd = context.getResources().openRawResourceFd(R.raw.rail_intro)) {
        player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
      }
      player.setVolume(1f, 1f);
      player.setOnPreparedListener(p -> {
        if (token != generation) return;
        if (current == null || !current.allowed().getAsBoolean()) { cancelBatch(); return; }
        p.start();
        vibrator = context.getSystemService(Vibrator.class);
        if (vibrator != null && vibrator.hasVibrator())
          vibrator.vibrate(VibrationEffect.createWaveform(new long[] {0, 600, 1400}, 0), attributes);
        current.started().run();
      });
      player.setOnCompletionListener(p -> {
        if (token != generation) return;
        releasePlayer();
        chimeDone = true;
        chimeFinishedAt = SystemClock.elapsedRealtime();
        if (ready) readNext();
        else if (!initializing) unavailable("Chưa có giọng tiếng Việt. Mở Cài đặt giọng đọc để bổ sung.");
        else armTimeout(15000, () -> unavailable("Chưa khởi tạo được giọng tiếng Việt. Mở Cài đặt giọng đọc."));
      });
      player.setOnErrorListener((p, what, extra) -> {
        if (token == generation) finishBatch();
        return true;
      });
      player.prepareAsync();
    } catch (Exception failure) { finishBatch(); }
  }

  private void ensureSpeech() {
    if (speech != null || initializing) return;
    initializing = true;
    speech = new TextToSpeech(context, result -> main.post(() -> {
      if (closed) return;
      initializing = false;
      int language = result == TextToSpeech.SUCCESS ? speech.setLanguage(Locale.forLanguageTag("vi-VN"))
          : TextToSpeech.LANG_NOT_SUPPORTED;
      ready = language >= TextToSpeech.LANG_AVAILABLE;
      if (ready) {
        // Prefer an installed offline Vietnamese voice; never silently fall back to another language.
        Set<Voice> voices = speech.getVoices();
        if (voices != null) for (Voice voice : voices)
          if (Set.of("vi", "vie").contains(voice.getLocale().getLanguage()) && !voice.isNetworkConnectionRequired()
              && (voice.getFeatures() == null || !voice.getFeatures().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))) {
            speech.setVoice(voice); break;
          }
        speech.setAudioAttributes(attributes);
        speech.setSpeechRate(1f);
        status.accept("Giọng tiếng Việt sẵn sàng");
        if (current != null && chimeDone) { clearTimeout(); readNext(); }
      } else {
        status.accept("Chưa có giọng tiếng Việt. Mở Cài đặt giọng đọc để bổ sung.");
        speech.shutdown(); speech = null;
        if (current != null && chimeDone) unavailable("Chưa có giọng tiếng Việt. Mở Cài đặt giọng đọc để bổ sung.");
      }
    }));
    speech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
      @Override public void onStart(String id) {
        main.post(() -> { if (id.equals(expected) && speechStartedAt == 0) speechStartedAt = SystemClock.elapsedRealtime(); });
      }
      @Override public void onDone(String id) { main.post(() -> speechDone(id)); }
      @Override public void onError(String id) { main.post(() -> speechError(id)); }
      @Override public void onError(String id, int code) { onError(id); }
    });
  }

  private void readNext() {
    if (current == null || !chimeDone || !ready || expected != null) return;
    if (!current.allowed().getAsBoolean()) { cancelBatch(); return; }
    while (cycle < AlertOptions.REPEATS) {
      if (item >= current.events().size()) { cycle++; item = chunk = 0; continue; }
      Rules.Event event = current.events().get(item);
      if (!open.contains(event.id())) { item++; chunk = 0; continue; }
      List<String> words = AlertOptions.chunks(AlertOptions.announcement(event), TextToSpeech.getMaxSpeechInputLength());
      if (chunk >= words.size()) { item++; chunk = 0; continue; }
      String text = words.get(chunk);
      speakingEvent = event.id();
      expected = generation + ":" + cycle + ":" + item + ":" + chunk;
      Bundle params = new Bundle();
      params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f);
      status.accept("Đang đọc cảnh báo · lần " + (cycle + 1) + "/6");
      if (speech.speak(text, TextToSpeech.QUEUE_FLUSH, params, expected) == TextToSpeech.ERROR) {
        speechError(expected); return;
      }
      armTimeout(Math.max(30000, Math.min(180000, text.length() * 150L)),
          () -> speechError(expected));
      return;
    }
    status.accept("Giọng tiếng Việt sẵn sàng");
    finishBatch();
  }

  private void speechDone(String id) {
    if (!id.equals(expected)) return;
    clearTimeout(); expected = null; completed++;
    utteranceCompleted.accept(current.events().get(item));
    chunk++;
    readNext();
  }

  private void speechError(String id) {
    if (id == null || !id.equals(expected)) return;
    unavailable("Không đọc được cảnh báo. Kiểm tra giọng tiếng Việt trong Cài đặt giọng đọc.");
  }

  private void unavailable(String message) {
    status.accept(message);
    // Chimes have already sounded; never leave vibration running when speech is unavailable.
    finishBatch();
  }

  private void armTimeout(long delay, Runnable action) {
    clearTimeout(); timeout = action; main.postDelayed(timeout, delay);
  }
  private void clearTimeout() {
    if (timeout != null) main.removeCallbacks(timeout);
    timeout = null;
  }
  private void releasePlayer() {
    if (player != null) { player.release(); player = null; }
  }
  private void releaseCurrent() {
    generation++; clearTimeout(); expected = speakingEvent = null;
    if (speech != null) speech.stop();
    releasePlayer();
    if (vibrator != null) { vibrator.cancel(); vibrator = null; }
    if (manager != null && focus != null) manager.abandonAudioFocusRequest(focus);
    manager = null; focus = null; current = null; active = false;
  }
  private void cancelBatch() {
    status.accept(ready ? "Giọng tiếng Việt sẵn sàng" : "Đã dừng giọng đọc");
    finishBatch();
  }
  private void finishBatch() { releaseCurrent(); nextBatch(); }
  private void stopNow() {
    boolean wasActive = active;
    queue.clear(); releaseCurrent();
    if (wasActive) status.accept("Đã dừng giọng đọc");
  }
  void stop() { dispatch(this::stopNow); }
  void close() {
    dispatch(() -> {
      closed = true; stopNow();
      if (speech != null) { speech.shutdown(); speech = null; }
    });
  }
}
