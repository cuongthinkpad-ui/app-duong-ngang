# Android 0.27.1 — dừng đợt cảnh báo từ thông báo

Tiếp tục từ bàn giao `370bce9335f8983026034ac6f0b6bb3918c4435b`, nhánh `codex/0.27-notification-stop`. Đây là cập nhật của dòng 0.27, versionName **0.27.1**, versionCode **28**, applicationId `vn.duongngang.monitor`.

## Yêu cầu đã duyệt ngày 12/11/2026

- Thêm **Dừng chuông/giọng đọc** trên thông báo theo dõi thường trực và thông báo sự cố Android. Nút gửi PendingIntent trực tiếp tới MonitorService, không cần mở Activity.
- Dừng chuông, giọng đọc, phần còn lại của hàng đọc và rung của đợt đang báo. Không đổi cài đặt âm, không dừng theo dõi, không xóa lỗi hoặc thông báo. Lỗi mới vẫn được phát theo chế độ đang chọn.
- Đồng bộ thao tác dừng với bước cập nhật cảnh báo trong poll; callback TTS đã xếp hàng phải kiểm tra trạng thái sống trước khi phát.
- Theo yêu cầu bổ sung, ba lựa chọn ở **Cách cảnh báo** chỉ ghi **Giọng nói**, **Chỉ chuông**, **Im lặng**. Giữ cách phát của 0.27: giọng nói hai hồi rồi sáu lượt đọc, chỉ chuông sáu hồi, rung và âm lượng hệ thống theo bản cũ.

Không đổi quy tắc tàu, lỗi website, lịch sử, phạm vi hoặc các marker migration của 0.27.

## Kiểm chứng phiên này

- 772 ca quy tắc, 17 ca lựa chọn cảnh báo, 16 parser và 39 ca HTTP fixture đạt.
- Build ứng dụng, AndroidTest và lint đạt; lint 0 lỗi, 15 cảnh báo có sẵn.
- Khôi phục khóa được chủ dự án cung cấp vào thư mục riêng ngoài Git. APK ký đúng chứng chỉ gốc SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.
- Cài APK 0.27 bàn giao, tạo dữ liệu thử bằng instrumentation, rồi `adb install -r` APK 0.27.1, không uninstall/clear data. 6 kiểm tra đạt: giải mã mật khẩu bằng Android Keystore cũ, URL/ID, chế độ âm/tắt âm, phạm vi/30 giây, bản tin raw trong SQLite và tệp private.

- 23 ca âm thanh/giọng Việt đạt trên APK cuối có nhãn rút gọn: chuông phát lại được sau dừng, giải phóng player/rung, TTS thật đọc đủ sáu lượt, xếp hàng, hủy giữa chuông/giọng và không khởi động lại từ callback cũ; các chế độ vẫn chọn được bằng nút giao diện. Chạy lại 6 ca giữ dữ liệu trên APK cuối cũng đạt.

- Ca dịch vụ `--notification-stop-only` đạt với APK cuối: bấm action thật trong khay thông báo sự cố khi Activity ở nền; dừng hàng đọc, player được giải phóng và `mCurrentVibration=null`; giữ nguyên lỗi, running, sound và lựa chọn chế độ; poll tiếp theo tiếp tục cập nhật nhưng không phát lại đợt cũ. Cảnh báo mới sau đó phát chuông/giọng bình thường và nút dừng dùng lại được. Thông báo theo dõi thường trực vẫn tồn tại và dùng cùng PendingIntent/action; chưa có ca bấm trực tiếp action của thông báo thường trực thành công, vì nó nằm dưới các thông báo hệ thống trong lần thử UI trước. Không tính hai lần bộ tự động chưa cuộn tới thông báo thường trực là ca đạt.

APK: [DuongNgang-0.27.1.apk](../downloads/DuongNgang-0.27.1.apk). SHA-256: `3bebfb387adee55576d38b16853891e18f1b2339e7e6c8e1544ca6640d294765`.

## Chạy lại

```sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-alert-options.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh assembleDebug assembleDebugAndroidTest lintDebug
# Giả lập riêng API 29 đã boot và có giọng Việt:
./scripts/test-android-voice.sh
python3 scripts/test-monitoring-e2e.py --notification-stop-only
```

Ca cài đè dùng test APK mới ký cùng khóa: cài `downloads/DuongNgang-0.27.apk`, cài APK AndroidTest mới, chạy mode `upgrade-seed`; sau đó cài `app-debug.apk` bằng `adb install -r` và chạy mode `upgrade-verify` với runner `vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks`. Chỉ chạy trên giả lập riêng; seed từ chối tài khoản đã tồn tại khác tài khoản fixture.

## Giới hạn và môi trường

Các ca Android dùng giả lập API 29 chạy phần mềm, không xác nhận loa/rung, bộ giọng hoặc chạy nền trên Xiaomi/HyperOS thật. Bộ giọng thử eSpeak 1.52.0 chính thức nằm ngoài Git/APK người dùng. Môi trường mới phải mở DownloadVoiceData của eSpeak để giải nén dữ liệu giọng; lần thử đầu thiếu dữ liệu này báo thiếu giọng Việt, không phải kết luận bản cũ bị lỗi.

Phiên này không thay đổi phần đối chiếu website thật đang chờ trong [biên bản 0.27](UI_REVIEW_0.27.md); không tuyên bố đã đăng nhập hoặc đối chiếu báo cáo thật. Main vẫn giữ 0.26; mã/APK cập nhật nằm trên nhánh nêu trên.

`scripts/setup-cloud.sh` đã sửa cách truyền proxy/Java CA cho sdkmanager và được chạy lại thành công; `scripts/env.sh` khai báo ANDROID_AVD_HOME. Khóa riêng, mật khẩu sao lưu và bộ giọng thử không nằm trong Git. Cấu hình install_script/start_skill đã lưu bản nháp; việc lưu không phải Publish hoặc xác nhận khôi phục trên tác vụ mới.
