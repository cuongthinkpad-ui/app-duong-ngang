# Giao diện telemetry theo mẫu Stitch — 0.14

Ngày 23/10/2026. APK đầy đủ 0.14.0, versionCode 14; cài mới hoặc cài đè cùng khóa ký hiện có.

## Phạm vi

Chủ dự án gửi ảnh riêng bốn tab và bảng màu/JSON mẫu từ Stitch, yêu cầu áp dụng linh hoạt vào app hiện có. Tiếp tục dùng Android Java; không chuyển nền tảng và không coi JSON mẫu là API vận hành.

- Nền `#060E1F`, thẻ `#101C36`, cyan `#00D9EF`, mint `#35D6A0`; ga hổ phách. Viền thẻ và gradient tối nhẹ; nút lưu cyan–mint. Căn cỡ chữ/nút cho điện thoại, bỏ những khoảng trống lớn và thông số giả trong mẫu.
- Tổng quan tự chia chiều cao, giữ root/sơ đồ/hai thẻ khi đọc lại. Dòng sự cố gọn gồm giờ, Km, trạng thái và chữ lỗi; mở danh sách để đọc đầy đủ. Một điều khiển chuông chung; chữ ký chỉ ở Cài đặt.
- Danh sách thêm bộ lọc Tất cả/Chưa ghi nhận lỗi/Có lỗi, số lượng từ tài khoản. Không bịa pin, góc cần, hướng cố định, mức độ nghiêm trọng hoặc chép số 14/10 của mẫu. Dòng điện áp đỏ vẫn là lỗi đỏ.
- Sự cố mở màu đỏ trầm, đã khôi phục màu xanh ngọc/tick. Tất cả vẫn xếp theo thời điểm phát sinh giảm dần, không nhóm đỏ rồi xanh. Hiển thị nguyên văn web và khôi phục đúng thiết bị.
- Cài đặt nhóm phạm vi, máy chủ/tài khoản và theo dõi/báo động; giữ URL sửa được, hiện/ẩn mật khẩu, ba chu kỳ, thử chuông, cài đặt chạy nền, lưu/dừng và chữ ký. Trang này cuộn để giữ chữ dễ đọc.

## Hiệu ứng và độ mới

Một ValueAnimator cho logo sáng dịu và quầng vòng đếm lỗi, chu kỳ 2,8 giây; giới hạn mức thay đổi độ sáng để giảm lần vẽ. Vòng chỉ pulse khi có lỗi. Dừng khi mất focus/onStop/onDestroy, bỏ tham chiếu vòng ở tab cũ. Khi Android tắt hiệu ứng, biểu tượng/vòng vẫn hiện tĩnh. Chuyển tab dịch nhẹ 4dp/140ms, không fade toàn màn hình; nhấn lại tab hiện tại không dựng lại.

Đoạn tàu có quầng sáng tĩnh, chỉ đổi theo dữ liệu web, không chạy bằng đồng hồ hoạt ảnh. LIVE chỉ hiện khi phiên đang chạy và dữ liệu còn mới; dừng/cũ/chưa có dữ liệu có nhãn riêng. Không khẳng định an toàn vật lý từ việc chưa thấy lỗi mới.

## Giữ nguyên nghiệp vụ

Không sửa Rules, Store, WebSource, MonitorService hay AlarmSound. Giữ phân trang/đăng nhập lại, chu kỳ, khôi phục đúng thiết bị, reset sự cố 00:00 Việt Nam, nhiều tàu/hướng, chuyển đoạn khi báo đã qua, giữ đoạn giữa các đường ngang và ranh giới ga. Thu hẹp phạm vi không cắt sơ đồ.

Giữ chuông sáu hồi; tự kích chỉ 30 giây và xử lý Ấn nút Phục hồi. Lệch từ ba trục mới cộng, báo lần thứ sáu một lần/ngày/CB hoặc một lượt lệch từ mười trục. Giữ mục Đếm trục lỗi có điều kiện và ngoại lệ toàn bộ đối chiếu trục tại Km 320+445, vẫn báo dòng đỏ thật ở đó.

## Kiểm tra

Build APK và gói kiểm tra thành công; Lint **0 lỗi, 15 cảnh báo**. **391 kiểm tra quy tắc** đạt. **62 kiểm tra lưu trữ** đạt. **20 kiểm tra telemetry** đạt với hiệu ứng bật và đạt lại với hiệu ứng tắt: LIVE/freshness, dữ liệu thật, phạm vi không cắt tuyến, bộ lọc, thứ tự cả sự cố đã khôi phục, nguyên văn thiết bị, chữ ký và các điều khiển vận hành. Có kiểm tra vùng Dashboard chỉ cao 300dp để phát hiện tràn hàng/nút; kiểm tra pulse thay đổi độ sáng logo trong khi root luôn đục và dừng theo callback mất focus. **49 kiểm tra UI hồi quy đạt trên APK cuối**, gồm giữ cùng root/card/canvas khi poll, chuyển đoạn/hướng, phục hồi, âm sáu hồi, thứ tự sự cố, URL sửa được và ngoại lệ trục 320+445. Các kiểm tra callback hiệu ứng dùng mốc animator xác định, không phải phép đo FPS hoặc điện năng trên điện thoại. Toàn bộ ảnh/kiểm tra lượt này dùng dữ liệu tổng hợp và `rail.example.test` trên giả lập riêng; không đăng nhập web công ty. Chưa xác minh khung hình, âm thanh hoặc HyperOS trên Xiaomi thật. Không chạy lại toàn bộ service fixture vì lớp đọc web, lưu trạng thái và báo động không thay đổi.

Ảnh Android: [Tổng quan](screenshots/telemetry-overview-0.14.png), [Đường ngang](screenshots/telemetry-crossings-0.14.png), [Sự cố](screenshots/telemetry-incidents-0.14.png), [Cài đặt](screenshots/telemetry-settings-0.14.png), [cuối Cài đặt](screenshots/telemetry-settings-bottom-0.14.png).

Chứng chỉ SHA-256 tiếp tục là `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Checksum APK tại `downloads/SHA256SUMS`.
