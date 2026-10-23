# Đường Ngang — bản thử 0.12

**[Tải APK 0.12](https://github.com/cuongthinkpad-ui/app-duong-ngang/blob/main/downloads/DuongNgang-0.12.apk)** — chọn **Download raw file** nếu GitHub mở trang xem tệp.

- Cảnh báo đếm trục: cùng CB lệch từ **3 trục**, tới **lần thứ 6/ngày** thông báo một lần; một lần lệch từ **10 trục** cảnh báo ngay. Giữ đỏ đến hết ngày, có thống kê và danh sách ghi chú cả ngày trong chi tiết đường ngang.
- Chuông lỗi **6 hồi** (12 giây). **“Ấn nút Phục hồi”** ở điểm nghi tự kích sẽ đánh dấu sự cố đã khôi phục và tắt đoạn sáng tự kích tại đó, giữ nguyên các lỗi thiết bị khác.
- Giao diện tối navy/cyan theo mẫu mới, Tổng quan vừa màn hình và cập nhật trong khung hiện tại.
- Sơ đồ mỗi lượt tàu chỉ sáng một đoạn: đoạn tiếp cận → đoạn sau đường ngang khi báo tàu đã qua. Giữ liên tục giữa hai đường ngang, cả khi điểm trước khôi phục và điểm sau mới tiếp cận. Có mép tiếp cận ở đầu tuyến, đoạn vào/ra ga; không nối xuyên qua ga.
- Khi không có sự cố, ghi đúng **“Không có sự cố”**.
- Sửa các khoảng/hướng tàu cũ còn sáng: “tàu đã qua” chuyển đoạn ngay, gồm khi thiếu bản tin tiếp cận; bản tin muộn không tạo lại lượt đã kết thúc. Cache sai từ bản cũ được tự dựng lại khi nâng cấp.
- Tên hướng/mũi tên nằm chung một nhãn để tránh chồng/cắt chữ. Đã đối chiếu thuật toán với dữ liệu web thật ở mốc ảnh phản hồi.
- Tự kích chỉ xét ở chu kỳ 30 giây; 2 phút/5 phút vẫn báo lỗi đỏ thật trên web.
- Có âm báo riêng khi có lỗi mới hoặc bắt đầu theo dõi với lỗi chưa khôi phục. Vào **Cài đặt → Thử chuông báo lỗi**, chỉnh âm lượng **Báo thức** nếu chưa nghe. Một nút tắt chuông chung, giữ trạng thái lỗi.
- Giữ reset sự cố 00:00 giờ Việt Nam, thứ tự mới nhất trước, phạm vi theo dõi không cắt sơ đồ, và chữ ký chỉ trong Cài đặt.

Có thể cài mới trực tiếp, không cần bản gốc. Nếu đã có app, cài đè bằng cùng chữ ký thử nghiệm để giữ cấu hình. Mở Cài đặt, nhập web/ID/mật khẩu rồi Lưu & theo dõi; chu kỳ 30 giây / 2 phút / 5 phút. Lượt đồng bộ đầu cần đọc hai ngày nên lâu hơn những lượt sau.

Cần kiểm tra âm thanh, chạy nền và thông báo khi khóa màn hình lâu trên điện thoại thật, nhất là chế độ pin Xiaomi. Kiểm tra phát nhạc trên giả lập không chứng minh loa Xiaomi đã kêu. Bản thử không thay thế quy trình trực hiện có. Không nhúng tài khoản thử nghiệm vào APK. [Chi tiết kiểm tra](../docs/UI_REVIEW_0.12.md).
