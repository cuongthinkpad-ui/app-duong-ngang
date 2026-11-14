# Android 0.27.4 — hiển thị sự cố tại đường ngang

Nền chính xác: Android **0.27.3**, commit `ee1affe5ea024590ce356e0f1e23aafbf17df669` trên `codex/0.27-notification-stop`. Nhánh làm việc: `codex/android-0.27.3-ui`. Main còn 0.26; không dùng mã nhánh iPhone/web. Bản mới **0.27.4**, versionCode **31**, applicationId giữ `vn.duongngang.monitor`.

## Hai thay đổi đã duyệt

- Chấm đường ngang đỏ khi chính điểm đó còn sự cố chưa khôi phục từ danh sách lỗi đang hiển thị. Khôi phục một phần vẫn đỏ; hết lỗi trở về màu sơ đồ trước đó (xanh khi có đoạn tàu, xám khi không). Ga giữ vàng; đoạn tàu, nhãn hướng và mũi tên giữ xanh, kể cả dữ liệu cũ.
- Danh sách đường ngang thay giờ bên phải bằng **N sự cố** màu đỏ theo số lỗi chưa khôi phục tại điểm. Không lỗi thì hiện **Tốt** màu xanh sau khi tải đủ ngày đang xem; trước đó hiện **Chưa tải đủ dữ liệu**. Dùng cùng điều kiện hoàn tất đang có ở chi tiết thiết bị. Mô tả bên trái và bộ lọc giữ nguyên.

Chỉ đọc trạng thái sự cố có sẵn để hiển thị; không thay thuật toán nghiệp vụ, dữ liệu raw, cache/migration, âm thanh, XÁC NHẬN hoặc lịch đọc. Giữ 30 giây bình thường và thử lại sau 120 giây khi lỗi theo 0.27.3. Cập nhật màu trên RouteView đang có khi dữ liệu đổi.

## Kiểm chứng

- 772 ca nghiệp vụ, 16 ca parser, 39 ca đọc có xác thực qua local fixture đạt.
- Build APK ứng dụng/AndroidTest và lint đạt: 0 lỗi, 14 cảnh báo có sẵn.
- 18 ca giao diện mới đạt trên giả lập Android 10/API 29: ngày chưa đầy đủ không ghi Tốt; hai lỗi ghi 2 sự cố màu đỏ; khôi phục một lỗi còn 1 sự cố và chấm đỏ; khôi phục lỗi cuối hiện Tốt và chấm về xanh của sơ đồ. Kiểm tra bitmap trực tiếp cho chấm, ga và đoạn tàu/nhãn hướng. Refresh giữ cùng RouteView; raw vẫn đủ sau thao tác.
- 200 ca lưu trữ tài khoản đạt trên APK mới.
- 69 ca cập nhật giao diện và 24 ca lịch sử/âm/rung sẵn có đạt trên APK mới.
- Cài đè APK gốc 0.27.3 → 0.27.4 bằng `adb install -r` đạt cả 6 nhóm: mật khẩu mã hóa/Android Keystore, URL/ID, chế độ âm, phạm vi/chu kỳ, bản tin SQLite và tệp private. Không gỡ app giữa hai bản.

Các kiểm tra dùng dữ liệu tổng hợp trên giả lập riêng; chưa kiểm chứng Xiaomi/HyperOS hoặc website thật trong phiên này. Không gán kết quả lịch đọc/âm thanh từ phiên 0.27.3 cho phiên này; mã xử lý đó không đổi.

## APK và khóa ký

[DuongNgang-0.27.4.apk](../downloads/DuongNgang-0.27.4.apk), cài đè trực tiếp, **không gỡ app cũ**.

SHA-256 APK: `3dbef3609207a1aefaf13e07563a1d3f4dfdfd7ca88433fe4d69502c571dc4d4`.

Chứng chỉ APK đã xác minh bằng apksigner, SHA-256: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`, khớp khóa gốc. Hai tệp chủ dự án cung cấp được khôi phục ngoài repository tại `/workspace/private-backups/duong-ngang-signing/restored/`, không ghi đè khóa debug toolchain, không đưa khóa/mật khẩu vào Git.

```sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh --offline assembleDebug assembleDebugAndroidTest lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w -e mode fault-display vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks
```

Runner tùy chỉnh yêu cầu chuỗi kết quả `18 fault display checks passed`; trạng thái tiến trình 0 không đủ chứng minh assertion đạt. Dùng giả lập riêng, không chạy fixture lên điện thoại đang dùng.
