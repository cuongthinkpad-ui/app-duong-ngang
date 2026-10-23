# Ngoại lệ trục Km 320+445 và chuyển tab — 0.13

Ngày 23/10/2026, giờ Việt Nam. Bản đầy đủ 0.13.0, versionCode 13; cài mới hoặc cài đè cùng khóa để giữ cấu hình.

## Hành vi được chủ dự án chốt

Km 320+445 có nhiều hơn sáu CB và cơ chế đếm riêng. Bỏ phép so sáu cảm biến ở điểm này: không hiển thị dữ liệu số trục, không ghi chú thừa/thiếu, không cộng thống kê, không tạo cảnh báo số lần hoặc lệch lớn. Các bản tin vận hành gốc như Khôi phục hệ thống vẫn được giữ. Ngoại lệ theo lý trình, kể cả tên có tiền tố Km/khoảng trắng; áp dụng cả khi web chỉ cung cấp sáu cột đầu.

Giữ nguyên lỗi đỏ thật trên website, chuông/đèn/cần chắn/cảm biến/kết nối/điện áp, tàu và suy luận tự kích tại 320+445. Chỉ loại các bản cảnh báo suy ra có ID `axle:<CB>:...`, không lọc lỗi bằng nội dung chữ hoặc loại cả đường ngang khỏi tuyến/phạm vi.

Đường ngang khác giữ ngưỡng 0.12: lệch từ ba trục mới cộng số lần; báo lần thứ sáu/ngày một lần cho từng CB, hoặc một lượt thừa/thiếu từ mười trục trở lên. Giữ đỏ đến 00:00 Việt Nam và chuông sáu hồi theo cài đặt. Một mục Đếm trục lỗi trong chi tiết vẫn được giữ cho các CB đạt ngưỡng.

## Nâng cấp từ bản cũ

`axleVersion=2` buộc dựng lại trạng thái lỗi khi mở app dù chưa kết nối web. Lớp quy tắc và lớp đọc dữ liệu lọc cảnh báo trục suy ra cũ ngay từ lần hiển thị đầu. Khi chuyển nền, Store xóa riêng các bản cảnh báo dẫn xuất tại 320+445 trong mọi ngày đã lưu và hủy thông báo tương ứng. Không xóa bản tin web gốc, lỗi phần cứng hoặc dấu chống báo lặp của CB ở đường ngang khác. `claimAxleNotice` cũng từ chối cảnh báo trục của điểm ngoại lệ.

## Tối ưu chuyển tab

Trước đây mỗi dòng/sự kiện có thể dựng lại toàn tuyến, đọc lại cả ngày và lọc lại hàng nghìn bản tin. Nay mỗi lượt dựng/cập nhật dùng chung tuyến, phạm vi, dữ liệu ngày, lỗi và một bảng phân nhóm sự kiện theo đường ngang. Mỗi lần đổi tab/nhận cập nhật/đổi phạm vi đều xóa dữ liệu tạm để lần dựng kế tiếp đọc dữ liệu mới. Không thay chu kỳ đọc web hoặc giữ một bản dữ liệu cũ qua các lần cập nhật.

Các biểu thức nhận diện thiết bị được biên dịch một lần với cùng nội dung; lịch sử lỗi chỉ xử lý bản tin đỏ và các dạng khôi phục vốn có. Khôi phục đúng thiết bị, nút phục hồi tự kích, xử lý cùng giây và nội dung web giữ nguyên.

Phép đo dùng 1.000 bản tin tổng hợp trên 11 đường ngang, cùng giả lập Android chạy bằng phần mềm. Đo thời gian thực thi hàm dựng tab, không đo độ trễ chạm/vẽ toàn bộ khung hình hoặc Xiaomi thật. Một lượt đo trước/sau trên cùng dữ liệu:

| Tab | 0.12 | 0.13 |
| --- | ---: | ---: |
| Đường ngang | 7.991 ms | 1.088 ms |
| Sự cố | 4.177 ms | 2.596 ms |
| Cài đặt | 1.320 ms | 1.817 ms |
| Tổng quan | 2.268 ms | 370 ms |

Các tab xử lý nhiều dữ liệu giảm thời gian callback; Cài đặt không cho thấy cải thiện trong lượt đo này. Phép đo một lượt có dao động do giả lập phần mềm và khởi tạo giao diện; không cam kết mọi tab có thời gian cố định hoặc lấy tỷ lệ này áp cho điện thoại thật.

## Kiểm tra

- 391 kiểm tra quy tắc đã đạt, bao gồm ngoại lệ 320+445, dữ liệu sáu cột bị cắt, dấu cảnh báo cũ và lỗi đỏ gốc còn nguyên; các quy tắc tàu/khôi phục/ngưỡng khác giữ kiểm tra cũ.
- **62 kiểm tra lưu trữ và 49 kiểm tra UI** đạt trên APK cuối sau tối ưu: dọn cảnh báo cũ cả ngày trước, hủy cả thông báo chỉ còn trong cache, giữ dữ liệu nguồn/lỗi thật/điểm kế cận; poll vẫn cập nhật trên khung hiện có. UI xác nhận 320+445 còn trạng thái Đang có sự cố do lỗi chuông thật nhưng không hiện mục hoặc ghi chú trục; đường ngang thường vẫn có thống kê.
- **22 kiểm tra service fixture** đạt ở bản sửa ngoại lệ đầu: dữ liệu 320+445 đạt cả hai ngưỡng vẫn không tạo lỗi/âm; lỗi Chuông 2 hỏng thật ở chính điểm này vẫn có âm. Giữ kiểm tra tự kích/phục hồi/tắt âm/khởi động lại/lần thứ sáu không lặp/CB khác/đúng mười trục tại điểm thường. Sau đó bổ sung tối ưu nhóm sự kiện/mẫu nhận diện và trường hợp hủy thông báo chỉ còn trong cache; đã chạy lại 391 quy tắc, 62 lưu trữ và UI trên APK cuối. Không chạy lại toàn bộ service fixture lần thứ hai sau các tối ưu cuối.
- Build APK/gói test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**. APK cùng chứng chỉ với 0.12; checksum ở `downloads/SHA256SUMS`.

Không đăng nhập web công ty trong lượt này; tất cả dữ liệu kiểm tra/tốc độ/ảnh là tổng hợp, trên giả lập riêng. Chưa đo tốc độ đổi tab hoặc âm thanh/chạy nền trên Xiaomi thật. Không suy ra vị trí GPS từ sơ đồ tàu.

Khóa ký vẫn cùng chứng chỉ SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.

Ảnh Android dùng dữ liệu tổng hợp: [320+445 giữ lỗi chuông và ẩn phần trục](screenshots/axle-exempt-detail-0.13.png), [đường ngang thường giữ thống kê đếm trục](screenshots/axle-detail-0.13.png).
