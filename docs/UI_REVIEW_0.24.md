# Đường Ngang 0.24 — chọn ngày, đọc ngày linh hoạt và lưu 30 ngày

Ngày 01/11/2026, giờ Việt Nam. Chủ dự án yêu cầu sửa lỗi đầu tháng, thêm mục nhỏ Ngày theo dõi trên Phạm vi theo dõi trong Cài đặt; sau đó chốt giới hạn **30 ngày tính cả hôm nay** cho cả chọn ngày và dữ liệu trên máy. Việc sửa, ký bằng khóa cũ và bàn giao GitHub được phép tiếp tục.

## Nguyên nhân và sửa bộ đọc

Ô ngày thực tế của website trả `1/11/2026`; 0.23 nhận ô qua biểu thức chỉ chấp nhận `dd/MM/yyyy` đủ chữ số, dẫn tới báo chưa nhận diện bộ chọn ngày. Đây là lỗi nhận dạng của app, không phải bằng chứng website không có báo cáo.

0.24 nhận ô theo tên `txtngay` hoặc giá trị ngày hợp lệ/type=date, chuyển ngày về LocalDate và so sánh giá trị. Nhận cả d/M/yyyy, dd/MM/yyyy và dạng trộn (01/6, 1/06); hỗ trợ type=date dạng ISO. Ngày trong bảng sự kiện cũng so sánh theo ngày/tháng/năm, chấp nhận d/M hoặc dạng đầy đủ năm; vẫn từ chối ngày sai, không bỏ kiểm tra báo cáo/phân trang/toàn tuyến. Không tắt TLS hoặc kiểm tra tính đầy đủ.

## Cài đặt và lịch sử

- Mục Ngày theo dõi đặt trên Phạm vi theo dõi. Mặc định hôm nay theo Asia/Ho_Chi_Minh, tự đổi qua nửa đêm/tháng/năm, không lưu cứng ngày hiện tại. Có bộ chọn ngày/tháng/năm và nút Về hôm nay khi đang xem ngày cũ.
- Giới hạn từ hôm nay về 29 ngày trước, tổng 30 ngày. Các bộ chọn ngày ở Sự cố/Chi tiết cũng dùng cửa sổ này. Giới hạn DatePicker quy đổi theo múi giờ thiết bị để nhãn lịch không lệch ngày Việt Nam.
- Chọn ngày cũ lưu lựa chọn, khởi động lại phiên đọc; tải ngày được chọn và ngày liền trước còn trong cửa sổ để nối ngữ cảnh. Sơ đồ, tàu gần nhất, chiếm dụng và lỗi hiển thị theo dữ liệu tới cuối ngày được chọn, không lấy trạng thái sơ đồ hiện tại thay cho ngày cũ. Có nhãn LỊCH SỬ/ngày rõ ràng, không LIVE.
- Nhánh tải ngày cũ không chạy tạo thông báo/chuông lỗi từ dữ liệu cũ, không ghi lastSuccess/coverage của phiên trực tiếp. Ngừng chuông đang phát khi chuyển sang lịch sử. Tải lại báo cáo cố định tối đa mỗi 15 phút hoặc khi người dùng chọn lại ngày, tránh tải đầy đủ hai ngày trên mỗi tick 30 giây.
- Khi quay về hôm nay, chạy lại theo dõi thực, giữ cài đặt âm đã chọn. Đổi tài khoản xóa lựa chọn ngày và metadata tải của tài khoản trước. Lựa chọn hết cửa sổ tự về hôm nay.

## Giữ dữ liệu 30 ngày

Rules.historyCutoff là 00:00 Việt Nam ngày hôm nay trừ 29 ngày. Giữ quy trình checkpoint/ghi bền trước xóa raw/VACUUM; giữ chiếm dụng và lỗi gốc. Metadata retentionDays=30 bắt buộc xử lý nâng cấp từ mốc 15 ngày, hạ sàn nhập cũ để có thể tải lại các ngày 16–30 nếu website còn báo cáo. Không khẳng định phục hồi tự động các bản tin đã bị xóa; người dùng chọn/tải lại ngày cần xem.

Giữ logic sơ đồ 0.23 (`travelVersion=5`), tự kích 4 và trục 3; không thêm ngoại lệ mất kết nối. Không thay khóa ký, applicationId hoặc dữ liệu tài khoản ngoài các khóa ngày/metadata được mô tả.

## Kiểm chứng đã thực hiện

- Xác nhận ô ngày thật ngày 01/11 là 1/11/2026. Đã đọc đầy đủ, kiểm tra ngày từng sự kiện qua WebSource mới: **01/11/2026: 1.989 bản tin, 13 điểm tuyến; 31/10/2026: 1.998 bản tin, 13 điểm tuyến**. Không đưa thông tin đăng nhập hoặc báo cáo xác thực vào Git.
- **644 ca quy tắc** đạt, thêm kiểm tra hôm nay tự động, biên ngày thứ 30/ngày hết hạn, ngày tương lai, chuyển tháng/năm/ngày nhuận và mốc dọn 30 ngày.
- **12 parser + 39 kiểm tra đọc xác thực với HTTP fixture** đạt. Ma trận 6 ngày × 4 kiểu zero-padding kiểm tra cả input và hàng sự kiện, gồm 01/06, đầu tháng 11, đầu năm, 29/02 năm nhuận và đầu tháng 3. Các ca kiểm tra báo cáo sai ngày/tuyến, thiếu/lặp trang, đăng nhập sai vẫn đạt.
- Build APK debug và AndroidTest, lint trên mã cuối đạt; APK 0.24.0/versionCode 24 đã xác minh chữ ký gốc bằng apksigner và applicationId/version bằng aapt; lint 0 lỗi/14 cảnh báo.
- Android storage instrumentation bổ sung 5 ca (runner dự kiến 179) cho nâng cửa sổ 15 → 30, nhập lại ngày cũ và chọn ngày/auto today. Các ca và UI test đã biên dịch, **chưa chạy Android runtime/UI/service hoặc Xiaomi** ở môi trường không có thiết bị/giả lập. Không trình bày kết quả đọc thật/fixture như kiểm chứng giao diện, âm hay nâng cấp trên máy thật.

APK: `downloads/DuongNgang-0.24.apk`. SHA-256 `8370d9e3383137416a91f574fd0223f19d4a5cebe591470d3d75cfe39cd8ada6`. Chứng chỉ SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Mã, tài liệu và APK đi kèm commit bàn giao; không có khóa riêng trong Git.

Ca hồi quy input ngày không zero-padding đã chạy riêng với WebSource/Rules nguyên bản HEAD 0.23 và thất bại đúng lỗi “Chưa nhận diện bộ chọn ngày”; cùng fixture với mã 0.24 đạt.
