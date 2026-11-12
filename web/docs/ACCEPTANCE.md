# Tiêu chí nghiệm thu

## Quy tắc và kết nối

- [ ] Website thật đăng nhập, kiểm tra route đầy đủ và phân trang; lỗi thật/khôi phục đúng thiết bị, nguyên văn.
- [ ] Theo dõi khi đóng trình duyệt vẫn có request backend; không phụ thuộc timer web.
- [ ] Mất TCP → giữ cache và dấu tàu xanh, bỏ LIVE, nút đỏ trong ô giữa summary/sơ đồ; retry nền sau 120 giây kể từ lần lỗi hoàn tất.
- [ ] Nút retry bỏ qua thời gian chờ, không request trùng; thành công xóa cảnh báo mất kết nối và trở lại chu kỳ 30 giây.
- [ ] XÁC NHẬN dừng đúng đợt đang báo, lỗi còn nguyên, đợt mới vẫn báo. Mở lại không đọc lỗi cũ.
- [ ] Chọn lịch sử 30 ngày, đủ pagination và nhóm thiết bị/cảm biến, lịch sử không phát âm.
- [ ] Restart/redeploy giữ đăng nhập, history/checkpoint, Push; không đổi APP_KEY/VAPID.
- [ ] Hai tài khoản khác nhau không xem được dữ liệu/mật khẩu của nhau; API không được SW cache.

## iPhone vật lý (bắt buộc, không đánh dấu bằng giả lập)

- [ ] iOS 16.4+, Safari thêm icon, mở standalone; layout 320/375/390/430px không che nút.
- [ ] Cho phép Push bằng thao tác bấm, cảnh báo sự cố thật tới máy khi khóa màn hình/PWA không mở; kiểm tra chế độ Tập trung/Im lặng theo quyền hệ thống.
- [ ] Khi mở: chọn giọng Việt, hai chuông + sáu đọc; không có giọng Việt phải báo rõ, không thay tiếng Anh.
- [ ] Phiên nghe MP3: bấm phát, khóa máy ít nhất 10 phút, tạo sự cố mới, đo chuông/giọng; thử XÁC NHẬN, lỗi mới, đổi mạng Wi-Fi/4G/cuộc gọi và phiên 55 phút. Ghi rõ khi cần bấm phát lại.
- [ ] Không tuyên bố web điều khiển rung iPhone hoặc đọc TTS qua Push. Nếu phiên nghe bị iOS ngắt thì Push vẫn là kênh riêng.
