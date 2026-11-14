# Tiêu chí nghiệm thu

## Quy tắc và kết nối

- [ ] Website thật đăng nhập, kiểm tra route đầy đủ và phân trang; lỗi thật/khôi phục đúng thiết bị, nguyên văn.
- [ ] Theo dõi khi đóng trình duyệt vẫn có request backend; không phụ thuộc timer web.
- [ ] Mất TCP → giữ cache và dấu tàu xanh, bỏ LIVE, nút đỏ trong ô giữa summary/sơ đồ; retry nền sau 50 giây kể từ lần lỗi hoàn tất.
- [ ] Nút retry bỏ qua thời gian chờ, không request trùng; thành công xóa cảnh báo mất kết nối và trở lại chu kỳ 10 giây.
- [ ] XÁC NHẬN dừng đúng đợt đang báo, lỗi còn nguyên, đợt mới vẫn báo. Mở lại không đọc lỗi cũ.
- [ ] Chọn lịch sử 30 ngày, đủ pagination và nhóm thiết bị/cảm biến, lịch sử không phát âm.
- [ ] Restart/redeploy giữ đăng nhập, history/checkpoint, Push; không đổi APP_KEY/VAPID.
- [ ] Hai tài khoản khác nhau không xem được dữ liệu/mật khẩu của nhau; API không được SW cache.

- [ ] ±1/±2 không dẫn xuất/tổng/âm/Push, raw nguyên; CB khác cùng bản tin vẫn xét; migration/history.
- [ ] Chấm đỏ cập nhật theo lỗi chưa khôi phục; ga vàng, tàu xanh; Tốt/N sự cố theo ngày đủ dữ liệu.

## iPhone vật lý (bắt buộc, không đánh dấu bằng giả lập)

- [ ] iOS 16.4+, Safari thêm icon, mở standalone; layout 320/375/390/430px không che nút.
- [ ] Cho phép Push bằng thao tác bấm, cảnh báo sự cố thật tới máy khi khóa màn hình/PWA không mở; kiểm tra chế độ Tập trung/Im lặng theo quyền hệ thống.
- [ ] Khi mở: chọn giọng Việt, hai chuông + sáu đọc; không có giọng Việt phải báo rõ, không thay tiếng Anh.

- [ ] Nghi tự kích không xuất hiện trước 10 giây; giữ chờ tối thiểu 10 giây và lần đọc nguồn thành công.
- [ ] Endpoint audio/stream trả 404; không còn media liên tục trong UI/backend/image.

Ma trận đầy đủ SRC/TRN/FLT/SELF/AXLE/ALM/DB/UI/BG/IOS ở đặc tả gốc. Các mục iPhone, Push thật, cấu hình production thuộc AI Studio tích hợp.
