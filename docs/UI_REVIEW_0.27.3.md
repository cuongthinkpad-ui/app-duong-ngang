# Android 0.27.3 — tự kết nối lại khi mất mạng

Tiếp tục từ `527fc3d`, nhánh `codex/0.27-notification-stop`. VersionName **0.27.3**, versionCode **30**, applicationId `vn.duongngang.monitor` và chứng chỉ ký gốc giữ nguyên.

## Yêu cầu và cách hoạt động

- Khi đọc web gặp lỗi I/O/kết nối hoặc máy chủ không phục vụ được yêu cầu, giữ dữ liệu và mốc cập nhật thành công cũ, ghi nhận mất kết nối. Dịch vụ nền tự thử lại sau **120 giây kể từ khi lượt lỗi kết thúc**; không cần mở app.
- Một ô trạng thái nhỏ nằm **giữa ô CÓ SỰ CỐ và sơ đồ**. Bình thường hiện tình trạng kết nối và thời điểm cập nhật. Khi thực sự đang tải, hiện **Đang tải dữ liệu…** và chỉ báo tiến trình không xác định, không bịa phần trăm.
- Chỉ khi mất kết nối mới hiện nút đỏ **Kết nối lại** và dòng **Mất kết nối** bên dưới nút. Bấm bỏ khoảng chờ và thử ngay. Nút tạm vô hiệu trong lúc đang thử; không tạo các lượt poll song song.
- Chỉ khi tải/lưu dữ liệu thành công mới ẩn nút/cảnh báo, khôi phục trạng thái dữ liệu mới và lịch đọc bình thường 30 giây. Dòng thời gian khi lỗi ghi **Thử lại mỗi 2 phút**.
- Giữ nguyên 25 giây timeout của web, URL/tài khoản, lỗi thiết bị, trạng thái tàu, lịch sử và chế độ âm. XÁC NHẬN vẫn chỉ dừng đợt cảnh báo hiện tại, không can thiệp lịch thử mạng; mở lại app không phát lại lỗi cũ.
- Lỗi xác thực vẫn dừng dịch vụ theo quy tắc hiện có, không tự thử mật khẩu liên tục. Khi đổi tài khoản/URL, xóa trạng thái mất kết nối của tài khoản cũ.
- Thu gọn đệm/vòng đếm của ô CÓ SỰ CỐ và phần sơ đồ để dành chỗ cho ô kết nối. Khi không có đoạn tàu, giảm vùng trống trên sơ đồ; khi có đoạn tàu, giữ nguyên vùng nhãn hướng cũ. Không thêm mục hay nút ở phần dưới.

Lịch poll dùng một executor và một lịch hẹn kế tiếp, không dồn các lượt chạy bù sau một yêu cầu chậm. Dừng/RESTART hủy lịch cũ; callback của executor cũ không được đặt lịch vào phiên mới. Lịch sử đã cache cũng phải thử mạng thật khi đang có cờ mất kết nối, không tự coi cache là kết nối thành công.

## Kiểm chứng

- Build ứng dụng, AndroidTest và lint trên mã cuối đạt: 0 lỗi, 14 cảnh báo.
- APK đã xác minh đúng chứng chỉ gốc SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.
- Cài đè giữ 6 nhóm dữ liệu đạt: mật khẩu mã hóa/Android Keystore, URL/ID, chế độ âm, phạm vi/30 giây, bản tin SQLite và tệp private.
- Ca chạy nền dùng HTTP 503 đạt: lần thử tiếp theo sau **120,8 giây**, dữ liệu/lần cập nhật tốt cũ giữ nguyên; khi máy chủ thử phục hồi, dịch vụ tự tải lại mà không mở Activity. Nhịp đọc bình thường đo được **30,9 giây**. Nút bấm bỏ khoảng chờ, không phải đợi đủ 120 giây.
- Ca hồi quy XÁC NHẬN đạt: nút thông báo và nút Tổng quan dừng đúng đợt âm/rung, mở lại Activity hoặc force-stop tiến trình rồi mở lại không đọc lại lỗi cũ; sự cố mới vẫn phát.
- Sau khi chốt bố cục nhỏ và tiến trình tải, chạy lại ca giao diện trên APK cuối đạt: ô nằm dưới thẻ sự cố và trên sơ đồ, cảnh báo dưới nút; đọc thành công thì ẩn nút/lỗi nhưng **vẫn hiện trạng thái Đã kết nối**. Với phản hồi cố ý chậm 10 giây, có chữ **Đang tải dữ liệu…** và ProgressBar thật; cờ tải kết thúc sau một lượt đọc thành công mới. Đã xem ảnh giao diện mất kết nối và đang tải trên giả lập 540×960.
- Ô kết nối được làm mới riêng theo trạng thái tải, không phụ thuộc việc có bản tin mới hoặc mốc lastSuccess thay đổi; không cần dựng lại toàn màn hình để hiện tiến trình.

APK: [DuongNgang-0.27.3.apk](../downloads/DuongNgang-0.27.3.apk). SHA-256: `3ae382809ab011904c61b5d4e6ea0659d9b2e2f947325dea5c9bf64bf7999288`.

```sh
source scripts/env.sh
./scripts/build-cloud.sh assembleDebug assembleDebugAndroidTest lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
python3 scripts/test-monitoring-e2e.py --reconnect-only
python3 scripts/test-monitoring-e2e.py --reconnect-ui-only
python3 scripts/test-monitoring-e2e.py --notification-stop-only
```

Chỉ dùng giả lập riêng. Ca reconnect dùng web fixture trả HTTP 503, đo khoảng thử lại thật, phục hồi máy chủ thử và bấm nút thật. Đây là xác minh cơ chế tự kết nối lại, không xác định nguyên nhân mất kết nối Visitec trên điện thoại của chủ dự án hoặc khẳng định máy chủ thật đã hết lỗi. Chưa thử Xiaomi/HyperOS thật; Android có thể hạn chế chạy nền theo cấu hình hệ thống. Khóa riêng ở ngoài Git; không cần đổi cấu hình app để cài đè.
