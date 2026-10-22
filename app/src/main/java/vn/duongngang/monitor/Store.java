package vn.duongngang.monitor;

import android.content.*;
import android.security.keystore.*;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import org.json.*;

public final class Store {
  private final Context context;
  public final android.content.SharedPreferences prefs;

  public Store(Context c) {
    context = c;
    prefs = c.getSharedPreferences("monitor", 0);
  }

  private javax.crypto.SecretKey key() throws Exception {
    KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
    ks.load(null);
    if (!ks.containsAlias("rail.credentials")) {
      KeyGenerator g = KeyGenerator.getInstance("AES", "AndroidKeyStore");
      g.init(
          new KeyGenParameterSpec.Builder(
                  "rail.credentials", KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
              .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
              .build());
      g.generateKey();
    }
    return (javax.crypto.SecretKey) ks.getKey("rail.credentials", null);
  }

  public String account() {
    return prefs.getString("url", "") + "\n" + prefs.getString("user", "");
  }

  public List<Rules.Event> saveForAccount(String expected, List<Rules.Event> events) {
    synchronized (Store.class) {
      if (!account().equals(expected))
        throw new IllegalStateException("Tài khoản đã thay đổi; bỏ kết quả cũ");
      return save(events);
    }
  }

  public void credentials(String url, String user, String password) throws Exception {
    synchronized (Store.class) {
      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, key());
      String encrypted =
          Base64.encodeToString(c.getIV(), Base64.NO_WRAP)
              + ":"
              + Base64.encodeToString(
                  c.doFinal(password.getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP);
      // Namespace data by endpoint AND account; never show a previous account's records.
      boolean changed =
          !url.equals(prefs.getString("url", "")) || !user.equals(prefs.getString("user", ""));
      if (changed) {
        context.deleteDatabase("events.db");
        prefs.edit().remove("lastSuccess").remove("coverage").apply();
      }
      prefs
          .edit()
          .putString("url", url)
          .putString("user", user)
          .putString("password", encrypted)
          .apply();
    }
  }

  public String password() throws Exception {
    String s = prefs.getString("password", "");
    if (s.isEmpty()) return "";
    String[] parts = s.split(":");
    Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
    c.init(
        Cipher.DECRYPT_MODE,
        key(),
        new GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)));
    return new String(c.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), StandardCharsets.UTF_8);
  }

  private android.database.sqlite.SQLiteDatabase db() {
    var db = context.openOrCreateDatabase("events.db", 0, null);
    db.execSQL(
        "CREATE TABLE IF NOT EXISTS events (id TEXT PRIMARY KEY, at INTEGER NOT NULL, km TEXT NOT"
            + " NULL, text TEXT NOT NULL, red INTEGER NOT NULL, counts TEXT NOT NULL)");
    return db;
  }

  public synchronized List<Rules.Event> save(List<Rules.Event> events) {
    List<Rules.Event> added = new ArrayList<>();
    try (var db = db()) {
      db.beginTransaction();
      try {
        for (var e : events) {
          ContentValues v = new ContentValues();
          v.put("id", e.id());
          v.put("at", e.at());
          v.put("km", e.crossing());
          v.put("text", e.text());
          v.put("red", e.red() ? 1 : 0);
          v.put("counts", new JSONArray(e.counts()).toString());
          if (db.insertWithOnConflict(
                  "events", null, v, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
              != -1) added.add(e);
        }
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }
    return added;
  }

  public synchronized List<Rules.Event> events(long from, long to) {
    var out = new ArrayList<Rules.Event>();
    try (var db = db();
        var c =
            db.rawQuery(
                "SELECT * FROM events WHERE at>=? AND at<? ORDER BY at DESC",
                new String[] {"" + from, "" + to})) {
      while (c.moveToNext()) {
        var counts = new ArrayList<String>();
        try {
          var a = new JSONArray(c.getString(5));
          for (int i = 0; i < a.length(); i++) counts.add(a.getString(i));
        } catch (JSONException ignored) {
        }
        out.add(
            new Rules.Event(
                c.getString(0),
                c.getLong(1),
                c.getString(2),
                c.getString(3),
                c.getInt(4) == 1,
                counts));
      }
    }
    return out;
  }
}
