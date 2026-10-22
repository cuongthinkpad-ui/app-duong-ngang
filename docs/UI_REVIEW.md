# Kiểm tra bản giao diện 0.2

Đã build `assembleDebug`, chạy `lintDebug` (0 lỗi), xác minh APK ký cùng chứng chỉ thử nghiệm của 0.1. Đã chạy 18 tình huống quy tắc, 3 tình huống parser.

Đã cài và mở trên giả lập Android 10 / API 29 bằng software emulation, viewport 720×1520, density 293 (khoảng 393 dp ngang):

- Màn hình tổng quan: đủ các mốc 11 ĐN và 2 ga, có đoạn xanh và mũi tên dữ liệu minh họa; không còn sơ đồ trống.
- Chạm mốc 328+325 mở đúng trang chi tiết. UI hierarchy xác nhận đủ Chuông, Đèn, Cần chắn, Cảm biến, Kết nối, Điện áp; không có camera.
- Chuyển tab Lịch sử và Cài đặt; UI hierarchy xác nhận các bộ điều khiển tương ứng có mặt. Chụp trực tiếp màn hình trong thư mục screenshots, không dùng ảnh thiết kế thay cho ảnh app.
- Dữ liệu minh họa được tách khỏi kho vận hành, hiển thị nhãn rõ ràng.
- Lượt tàu đầu không bị lặp khi mở rộng danh sách.

Giả lập có lỗi System UI lúc khởi động hệ điều hành (ConcurrentModificationException của TunerServiceImpl), sau đó khởi động thành công. Không có crash ứng dụng trong các màn hình được kiểm tra. Đây không phải kết quả kiểm thử Xiaomi 14T Pro hay đảm bảo mọi phiên bản Android.

Chưa xác minh các luồng mạng Visitec, thông báo/chuông, polling khi khóa màn hình, tự kích và vị trí tàu thật. Reset qua ngày có kiểm thử logic; chưa có phiên tích hợp web thật chứng minh toàn bộ luồng reset.
