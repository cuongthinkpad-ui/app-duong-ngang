package vn.duongngang.monitor;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;

final class AppUpdate {
  private static final String DRIVE_ID = "1aj1XYLOag1NW385YBNutBSavYZDyC0CA";
  static final String SHARE_URL = "https://drive.google.com/file/d/" + DRIVE_ID + "/view";
  static final String APK_TYPE = "application/vnd.android.package-archive";
  private static final URI DOWNLOAD_URL =
      URI.create("https://drive.usercontent.google.com/download?id=" + DRIVE_ID + "&export=download");
  private static final long MAX_BYTES = 64L * 1024 * 1024;

  static final class InvalidApk extends IOException {
    InvalidApk(String message) { super(message); }
  }

  private AppUpdate() {}

  static File directory(Context context) {
    return new File(context.getCacheDir(), "app-updates");
  }

  static Uri download(Context context) throws IOException, PackageManager.NameNotFoundException {
    File directory = directory(context);
    Files.createDirectories(directory.toPath());
    File[] oldFiles = directory.listFiles();
    if (oldFiles != null) {
      long cutoff = System.currentTimeMillis() - 24L * 60 * 60 * 1000;
      for (File old : oldFiles) if (old.lastModified() < cutoff) old.delete();
    }
    String name = UUID.randomUUID() + ".apk";
    File apk = new File(directory, name);
    boolean ready = false;
    try {
      fetch(apk);
      if (!isNewerUpdate(context, apk)) return null;
      ready = true;
      return Uri.parse("content://" + context.getPackageName() + ".updates/" + name);
    } finally {
      if (!ready) apk.delete();
    }
  }

  private static void fetch(File apk) throws IOException {
    URI url = DOWNLOAD_URL;
    for (int redirect = 0; redirect <= 5; redirect++) {
      String host = url.getHost();
      if (!"https".equalsIgnoreCase(url.getScheme()) || url.getUserInfo() != null
          || (url.getPort() != -1 && url.getPort() != 443)
          || host == null || !(host.equals("drive.google.com")
              || host.equals("drive.usercontent.google.com")
              || host.endsWith(".googleusercontent.com")))
        throw new IOException("Địa chỉ tải xuống của Drive không hợp lệ.");
      HttpsURLConnection connection = (HttpsURLConnection) url.toURL().openConnection();
      try {
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(20000);
        connection.setUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-cache");
        int status = connection.getResponseCode();
        if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
          String location = connection.getHeaderField("Location");
          if (location == null) throw new IOException("Drive không cung cấp địa chỉ tải xuống.");
          url = url.resolve(location);
          continue;
        }
        if (status != 200)
          throw new IOException("Không tải được tệp từ Drive (HTTP " + status + ").");
        String type = connection.getContentType();
        if (type != null && type.toLowerCase(java.util.Locale.ROOT).startsWith("text/"))
          throw new IOException("Drive yêu cầu mở trang tải xuống trong trình duyệt.");
        long expected = connection.getContentLengthLong();
        if (expected > MAX_BYTES) throw new IOException("Tệp APK vượt giới hạn tải xuống.");
        try (var input = connection.getInputStream();
            var output = Files.newOutputStream(apk.toPath())) {
          byte[] buffer = new byte[16384];
          long total = 0;
          int count;
          while ((count = input.read(buffer)) != -1) {
            if (Thread.currentThread().isInterrupted()) throw new IOException("Đã dừng tải APK.");
            total += count;
            if (total > MAX_BYTES) throw new IOException("Tệp APK vượt giới hạn tải xuống.");
            output.write(buffer, 0, count);
          }
          if (total == 0 || (expected >= 0 && expected != total))
            throw new IOException("Tệp tải xuống chưa đầy đủ.");
        }
        return;
      } finally {
        connection.disconnect();
      }
    }
    throw new IOException("Drive chuyển hướng quá nhiều lần.");
  }

  @SuppressWarnings("deprecation")
  static boolean isNewerUpdate(Context context, File apk)
      throws IOException, PackageManager.NameNotFoundException {
    PackageManager packages = context.getPackageManager();
    int flags = Build.VERSION.SDK_INT >= 28
        ? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES;
    PackageInfo installed = packages.getPackageInfo(context.getPackageName(), flags);
    PackageInfo candidate = packages.getPackageArchiveInfo(apk.getAbsolutePath(), flags);
    if (candidate == null || !installed.packageName.equals(candidate.packageName))
      throw new InvalidApk("Tệp tải xuống không phải APK Giám sát Đường ngang.");
    Signature[] original = signers(installed), downloaded = signers(candidate);
    if (original == null || original.length == 0 || downloaded == null
        || !new HashSet<>(List.of(original)).equals(new HashSet<>(List.of(downloaded))))
      throw new InvalidApk("Chữ ký APK không trùng với bản đang cài.");
    return Build.VERSION.SDK_INT >= 28
        ? candidate.getLongVersionCode() > installed.getLongVersionCode()
        : candidate.versionCode > installed.versionCode;
  }

  @SuppressWarnings("deprecation")
  private static Signature[] signers(PackageInfo info) {
    if (Build.VERSION.SDK_INT < 28) return info.signatures;
    return info.signingInfo == null ? null : info.signingInfo.getApkContentsSigners();
  }
}
