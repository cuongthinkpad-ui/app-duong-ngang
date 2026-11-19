package vn.duongngang.monitor;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

public final class UpdateFileProvider extends ContentProvider {
  @Override public boolean onCreate() { return true; }

  private File apk(Uri uri) {
    String name = uri.getLastPathSegment();
    if (uri.getPathSegments().size() != 1 || name == null
        || !name.matches("[0-9a-f-]{36}\\.apk")) throw new IllegalArgumentException("Invalid APK URI");
    return new File(AppUpdate.directory(getContext()), name);
  }

  @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    if (!"r".equals(mode)) throw new FileNotFoundException("Read only");
    return ParcelFileDescriptor.open(apk(uri), ParcelFileDescriptor.MODE_READ_ONLY);
  }

  @Override public String getType(Uri uri) { return AppUpdate.APK_TYPE; }

  @Override public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    File file = apk(uri);
    String[] columns = projection == null
        ? new String[] {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE} : projection;
    var cursor = new MatrixCursor(columns, 1);
    if (file.isFile()) {
      var row = cursor.newRow();
      for (String column : columns) {
        row.add(switch (column) {
          case OpenableColumns.DISPLAY_NAME -> "DuongNgang-Latest.apk";
          case OpenableColumns.SIZE -> file.length();
          default -> null;
        });
      }
    }
    return cursor;
  }

  @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
  @Override public int update(Uri uri, ContentValues values, String selection, String[] args) {
    throw new UnsupportedOperationException();
  }
  @Override public int delete(Uri uri, String selection, String[] args) {
    throw new UnsupportedOperationException();
  }
}
