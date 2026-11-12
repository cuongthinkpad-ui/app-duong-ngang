# Android 0.27.2 — không đọc lại lỗi cũ khi mở app

Tiếp tục từ `247f8d2` (0.27.1), cùng nhánh `codex/0.27-notification-stop`. APK versionName **0.27.2**, versionCode **29**, applicationId giữ `vn.duongngang.monitor`.

## Phản hồi và bản sửa

Chủ dự án phản hồi mở lại app thì đọc lại sự cố cũ. Đã tái hiện trên APK 0.27.1: nhận hai lỗi mới, bấm dừng ở khay thông báo, mở lại Activity đang có dịch vụ thì không lặp; force-stop tiến trình rồi mở lại làm `lastAlarm` tăng và phát lại lỗi cũ. Ca hồi quy thất bại đúng tại `Process restart replayed old faults`.

Nguyên nhân là nhánh `firstPoll` chủ động đưa toàn bộ lỗi còn tồn tại vào hàng phát mỗi khi dịch vụ được tạo lại hoặc RESTART. 0.27.2 bỏ nhánh phát lại này. Lỗi cũ vẫn lưu/hiển thị; chỉ sự cố mới được nhập, còn hiệu lực, đúng phạm vi và đủ điều kiện báo mới được phát theo luồng sẵn có. Giữ cách phát chuông/TTS/rung của 0.27, các quy tắc tàu, lỗi website, dữ liệu và cài đặt.

Theo yêu cầu bổ sung của chủ dự án, nhãn nút trên cả thông báo thường trực và thông báo sự cố đổi thành **XÁC NHẬN**. Chức năng vẫn chỉ dừng đợt chuông/đọc/rung đang phát, giữ theo dõi và lỗi; sự cố mới vẫn được báo. Ba nhãn Cách cảnh báo giữ **Giọng nói**, **Chỉ chuông**, **Im lặng**.

Chủ dự án yêu cầu thêm nút **XÁC NHẬN** ngay trong ô **CÓ SỰ CỐ** ở đầu Tổng quan. Nút nằm bên phải nội dung thẻ, dùng cùng lệnh MUTE_ALARM của nút thông báo, không đổi chế độ âm hoặc trạng thái lỗi. Có phản hồi xác nhận bằng toast; nút ẩn khi không còn lỗi, đã dừng theo dõi hoặc đang xem lịch sử. Cập nhật trạng thái trong thẻ hiện có khi poll, không dựng lại toàn màn hình.

## Kiểm chứng

- Build ứng dụng/AndroidTest và lint trên mã cuối có nút XÁC NHẬN đạt: 0 lỗi, 15 cảnh báo.
- APK ký đúng chứng chỉ gốc SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Khóa và mật khẩu sao lưu vẫn ở ngoài Git.
- Cài đè APK cuối giữ 6 nhóm dữ liệu đạt: mật khẩu mã hóa vẫn giải mã được bằng Android Keystore cũ, URL/ID, chế độ âm, phạm vi/30 giây, bản tin raw SQLite và tệp private. Dữ liệu thử được tạo từ 0.27 và giữ qua 0.27.1 tới 0.27.2, không uninstall/clear data.
- Ca dịch vụ `--notification-stop-only` trên APK cuối đạt: bấm **XÁC NHẬN** thật ở khay thông báo khi app ở nền, giữ lỗi/mode/running; mở lại Activity và force-stop tiến trình rồi mở lại, hoàn tất poll đầu và poll tiếp theo mà `lastAlarm` không tăng, lỗi cũ không bị xóa. Cảnh báo mới sau đó vẫn phát chuông và giọng Việt.
- Đưa app trở lại Tổng quan, tìm nút trong đúng thẻ **CÓ SỰ CỐ**, kiểm tra nút bấm được và nằm trong biên thẻ, rồi bấm thật. Hàng đọc dừng, MediaPlayer được giải phóng và `mCurrentVibration=null`; sự cố, sound và running giữ nguyên. Đã xem ảnh bố cục ở 540×960 trên giả lập API 29; nút hiện rõ bên phải thẻ, không bị cắt.

APK: [DuongNgang-0.27.2.apk](../downloads/DuongNgang-0.27.2.apk). SHA-256: `0a6c9ae4b4b5023452baa5794b086c4aab7a0c9dddc3542e2b16f4f8b62e09ec`.

## Chạy lại

```sh
source scripts/env.sh
./scripts/build-cloud.sh assembleDebug assembleDebugAndroidTest lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
python3 scripts/test-monitoring-e2e.py --notification-stop-only
```

Chỉ chạy ca dịch vụ trên giả lập riêng có giọng Việt đã khởi tạo; fixture tự sao lưu/khôi phục dữ liệu. Ca hiện yêu cầu đúng nhãn **XÁC NHẬN**, kiểm tra mở lại Activity, khởi động lại tiến trình, hoàn tất hai lượt poll rồi kiểm tra không phát lại lỗi cũ, sau đó cảnh báo mới vẫn phát và nút dùng lại được. Chưa thử Xiaomi/HyperOS thật; không đối chiếu website thật trong phiên sửa này. Các bộ quy tắc/parser/giọng độc lập đã đạt ở 0.27.1 không được tính là chạy lại trong phiên này; xem [biên bản trước](UI_REVIEW_0.27.1.md).
