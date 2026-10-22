# Kiểm tra bản 0.5

APK `assembleDebug` và Android Lint thành công: 0 lỗi, 14 cảnh báo. Các cảnh báo gồm target Android cũ, cấu hình sao lưu/biểu tượng monochrome, cấp phát khi vẽ, wakelock không có timeout và chuỗi giao diện; không coi Lint thành công là đã kiểm chứng vận hành ngoài hiện trường.

## Các kiểm tra đã chạy

- `scripts/test-rules.sh`: **68** kiểm tra quy tắc, bao gồm danh sách tuyến khác, sắp km theo số, hậu tố ga, ngắt đối chiếu hai phía ga/đầu tuyến, phạm vi theo tuyến động, ghép khôi phục riêng số chuông/đèn và chiếm dụng đồng thời.
- `scripts/test-parser.sh`: **12** kiểm tra parser và **4** kiểm tra đọc web qua máy chủ mô phỏng cục bộ. Kiểm tra giữ nguyên câu lỗi có số thiết bị, lấy đầy đủ danh mục từ select/bảng Km–Status, giữ điểm chưa có sự kiện, chọn Tất cả qua các trang, đăng nhập/cookie/postback, đọc hai trang và từ chối khi tuyến thay đổi hoặc bản ghi nằm ngoài tuyến.
- `scripts/test-android-route.sh`: **12** kiểm tra lưu trữ trên Android API 29. Đổi ID hoặc URL xóa tuyến/dữ liệu/phạm vi cũ, giữ dữ liệu khi cùng tài khoản, từ chối kết quả muộn của tài khoản trước, tách phạm vi minh họa với phạm vi thực. Bộ kiểm tra dùng kho dữ liệu device-protected riêng, không xóa dữ liệu thông thường của app.

## Chiếm dụng và nội dung lỗi

Sơ đồ giữ trạng thái từng đường ngang từ bản tin đến khi có “Khôi phục hệ thống” tại đúng điểm. Cần chắn nâng, tàu qua đường ngang và khôi phục riêng chuông không xóa trạng thái chiếm dụng. Các đường ngang kế tiếp có thể đồng thời còn xanh; không tự xóa điểm trước khi điểm sau báo tàu. Bản tin tại ga không tạo điểm chiếm dụng. Khôi phục hệ thống dùng để giải phóng chiếm dụng; không xóa mọi lỗi thiết bị.

Không suy ra danh tính hoặc số đoàn tàu từ số điểm chiếm dụng. Các điểm liên tiếp cùng hướng được nối xanh và dùng chung nhãn hướng; điểm thuộc hướng khác vẫn được vẽ riêng. Đang gián đoạn/quá hai chu kỳ (tối thiểu 60 giây) thì giữ lại trạng thái cuối bằng màu vàng nâu với giờ cập nhật và nhãn “Dữ liệu cũ”.

Bản tin lỗi giữ nguyên phần nội dung web đọc được, gồm số thiết bị. Thẻ Tổng quan có tối đa hai dòng để giữ bố cục; chạm để đọc trọn câu, hoặc mở chi tiết đường ngang. Chuông 2 khôi phục không xóa lỗi chuông 1; trang chi tiết ưu tiên lỗi còn tồn tại trong nhóm.

## Giao diện đã kiểm tra trên giả lập

Android 10 / API 29, 720×1520, density 293:

- Tổng quan minh họa vừa màn hình, có bốn điểm chiếm dụng thành hai nhóm hướng; giữ đúng câu “Chuông 2 mất trạng thái”, một nút tắt chuông chung.
- Mở hộp thoại lỗi đọc trọn câu, rồi mở chi tiết đường ngang vẫn giữ số chuông.
- Nạp dữ liệu lưu mô phỏng cho tuyến khác (không dùng chế độ samples): hiển thị hai điểm chiếm dụng, nhãn dữ liệu cũ; sau bản tin khôi phục chuông 2 chỉ còn lỗi chuông 1. Ảnh ghi rõ “Dữ liệu kiểm thử tuyến · Không theo dõi thực tế”.
- Danh sách khu vực khác có 12 đường ngang và ba ga CS / MLy / QHa; bộ chọn phạm vi lấy danh sách mới, loại các ga; thu hẹp phạm vi vẫn giữ toàn tuyến. Đã kiểm tra HorizontalScrollView có khả năng cuộn thực tế và chụp hai đầu tuyến sau thao tác vuốt ngang.

Ảnh trong `screenshots/*-0.5.png`: Tổng quan minh họa, lỗi nguyên văn, chi tiết thiết bị, tuyến khác, bộ chọn phạm vi và dữ liệu lưu mô phỏng. Giả lập software emulation có ANR System UI lúc khởi động; các kiểm tra UI tiến hành sau khi đóng hộp thoại hệ điều hành. Có lần công cụ UiAutomator trả null trước khi giao diện ổn định; lấy lại cây giao diện mới khi kiểm tra. Không ghi nhận crash app trong các thao tác này.

## Giới hạn

Chưa xác minh đăng nhập, phân trang đang thay đổi, màu CSS ngoài hoặc sơ đồ bằng web Visitec thật. Việc lấy danh sách và dữ liệu tài khoản mới đã kiểm tra bằng fixture, không phải thử tài khoản thật của khu vực khác. Chưa kiểm tra chuông/polling khi khóa màn hình trên Xiaomi 14T Pro. Cảnh báo tự kích chưa nối vào service. Sơ đồ dùng hôm nay và dữ liệu hôm qua đã lưu; khởi động mới khi chưa có dữ liệu ngày trước chưa bảo đảm nối trạng thái qua nửa đêm.
