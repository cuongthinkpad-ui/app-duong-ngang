package vn.duongngang.monitor;

import android.content.Context;
import android.media.*;

/** One audible alert for a new fault; no periodic replay of old faults and no forced volume. */
final class AlarmSound {
  private MediaPlayer player;
  private AudioManager manager;
  private AudioFocusRequest focus;

  synchronized boolean play(Context context) {
    stop();
    manager = context.getSystemService(AudioManager.class);
    AudioAttributes attributes =
        new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
    focus =
        new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener(
                change -> {
                  if (change == AudioManager.AUDIOFOCUS_LOSS
                      || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) stop();
                })
            .build();
    if (manager.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      stop();
      return false;
    }
    try {
      player = new MediaPlayer();
      player.setAudioAttributes(attributes);
      try (var descriptor = context.getResources().openRawResourceFd(R.raw.rail_alarm)) {
        player.setDataSource(
            descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
      }
      player.setLooping(false);
      player.setVolume(0.85f, 0.85f);
      player.setOnCompletionListener(p -> stop());
      player.setOnErrorListener(
          (p, what, extra) -> {
            stop();
            return true;
          });
      player.prepare();
      player.start();
      return true;
    } catch (Exception failure) {
      stop();
      return false;
    }
  }

  synchronized boolean isPlaying() {
    try {
      return player != null && player.isPlaying();
    } catch (IllegalStateException ignored) {
      return false;
    }
  }

  synchronized void stop() {
    if (player != null) {
      player.release();
      player = null;
    }
    if (manager != null && focus != null) manager.abandonAudioFocusRequest(focus);
    focus = null;
    manager = null;
  }
}
