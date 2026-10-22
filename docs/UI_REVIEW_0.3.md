# Kiểm tra bản 0.3

Build `assembleDebug` và Android Lint thành công; 28 tình huống quy tắc và 3 tình huống parser đã chạy qua. Các ca mới kiểm tra ghép lỗi/khôi phục, lỗi tái xuất hiện, phạm vi hai đầu, loại điểm ngoài phạm vi, từ–đến ngược, một điểm riêng lẻ và mốc ga nằm giữa phạm vi.

Đã kiểm tra trực tiếp trên giả lập Android 10 / API 29, kích thước 720×1520, density 293 (khoảng 393 dp ngang):

- Trang chính có sẵn 3 lượt tàu và 3 sự cố, không còn “Sơ đồ tuyến”, “11 ĐN · 2 GA”, dải trạng thái phụ hoặc mục Lịch sử; chữ trạng thái đúng “Chưa khôi phục”.
- Thanh dưới có Sự cố. Trong mục này có bản ghi “✓ Đã khôi phục” sau đúng bản tin khôi phục nguồn 220VAC, bộ lọc Đã khôi phục hoạt động.
- Chọn từ 320+445 đến 321+780 bằng giao diện Cài đặt. Trang chính chỉ giữ tàu và lỗi trong khoảng; các lỗi tại 313+456 và 328+325 không còn xuất hiện.
- Quay lại Cài đặt vẫn giữ hai giá trị đã chọn. Phạm vi được lưu bằng SharedPreferences.
- Xem ảnh chụp thực trong `screenshots/*-0.3.png`.

Tương tự bản 0.2, giả lập chạy software emulation có lỗi System UI/ANR lúc khởi động hệ điều hành; kiểm tra giao diện sau khi hệ thống ổn định. Không ghi nhận crash ứng dụng trong các thao tác trên. Không coi đây là kiểm thử Xiaomi 14T Pro hay toàn bộ hệ điều hành Android.

Luồng HTTP Visitec, nhận chuông trên thiết bị thật, polling khi khóa màn hình, suy luận tự kích và vị trí tàu thật vẫn chưa được xác minh. Phần lọc chuông theo phạm vi đã nối vào service và kiểm tra quy tắc; chưa có thử nghiệm chuông với dữ liệu thật.
