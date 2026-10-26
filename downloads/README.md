# Đường Ngang — bản thử 0.19

**[Tải APK 0.19](https://github.com/cuongthinkpad-ui/app-duong-ngang/blob/main/downloads/DuongNgang-0.19.apk)** — chọn **Download raw file** nếu GitHub mở trang xem tệp.

- Sửa sơ đồ giữ đoạn cũ khi thiếu bản tin tàu: đối chiếu hoạt động cần chắn và khôi phục với tàu đang đi tới. Nếu mất cả một lượt, lượt thật kế tiếp có bằng chứng đi qua giúp đồng bộ lại đúng đoạn. Giữ các tàu khác và lỗi đỏ thiết bị/đường truyền; không tự xóa tàu theo thời gian.
- Cài đè tự dựng lại sơ đồ đã lưu theo quy tắc mới, giữ tài khoản/cài đặt/lịch sử. Không tạo bản tin tàu giả trong dữ liệu gốc.

- Nhãn ga Nghi Long rút gọn thành **Ga NL** để không che mốc cạnh bên; cài đè giữ nguyên dữ liệu và cách hoạt động.

- Bổ sung **ga Nghi Long tại Km 303+350 riêng cho ID Chosy** khi web chưa có mốc này. Tàu vào/ra ga và ranh giới đối chiếu tự kích hoạt động giống các ga hiện có. Cài đè cập nhật tuyến/sơ đồ đã lưu và đánh giá lại nghi tự kích liên quan, giữ tài khoản/cài đặt/lỗi thật. Không thêm ga vào số đường ngang hay phạm vi theo dõi; tài khoản khác giữ tuyến riêng.

- Sửa quy trình tự kích theo xác nhận mới: tại đường ngang phía trước, thêm bản tin cần chắn đã hạ/đã nâng và tàu đã qua cùng hướng làm bằng chứng. Gộp theo lượt, không đếm mỗi cần chắn thành một tàu. Có bằng chứng bổ sung phù hợp thì gỡ riêng nghi tự kích; lỗi đỏ thật vẫn giữ. Cài đè đánh giá lại suy luận cũ từ dữ liệu đã lưu, không báo lại lịch sử.

- Thêm năm điểm **307+507, 309+056, 280+500, 343+350, 338+900** vào ngoại lệ trục cùng **320+445**: không ghi chú/thống kê/cảnh báo trục suy ra; cài đè tự dọn các cảnh báo trục cũ. Lỗi đỏ thật từ web và theo dõi tàu tại các điểm này vẫn giữ nguyên.

- Làm lại bốn tab theo mẫu Stitch: navy/cyan, viền thẻ, ga màu hổ phách, nút chuyển màu cyan–mint. Tổng quan tự chia chiều cao; dữ liệu thực không bị thay bằng các con số mẫu.
- Logo sáng dịu, vòng đếm sự cố pulse và đoạn tàu có viền sáng nhẹ. Hiệu ứng dừng khi app mất focus/chạy nền, tắt theo cài đặt hiệu ứng Android. LIVE phản ánh phiên đang chạy và độ mới của dữ liệu.
- Danh sách đường ngang có bộ lọc theo lỗi thực tế. Dòng đỏ web giữ nguyên nội dung; phần Sự cố vẫn xếp thời điểm phát sinh mới nhất trước, kể cả sự cố đã khôi phục.

- Những điểm ngoại lệ trục vẫn giữ lỗi đỏ thật trên website và theo dõi tàu. Cài đè tự xử lý cảnh báo trục cũ; giữ tối ưu đọc dữ liệu khi chuyển tab.
- Cảnh báo đếm trục: cùng CB lệch từ **3 trục**, tới **lần thứ 6/ngày** thông báo một lần; một lần lệch từ **10 trục** cảnh báo ngay. Giữ đỏ đến hết ngày; chi tiết đường ngang có một mục **Đếm trục lỗi**, chỉ hiện thống kê CB đã đạt ngưỡng.
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

Cần kiểm tra âm thanh, chạy nền và thông báo khi khóa màn hình lâu trên điện thoại thật, nhất là chế độ pin Xiaomi. Kiểm tra phát nhạc trên giả lập không chứng minh loa Xiaomi đã kêu. Bản thử không thay thế quy trình trực hiện có. Không nhúng mật khẩu thử nghiệm vào APK. [Chi tiết kiểm tra](../docs/UI_REVIEW_0.19.md).
