# Kiểm tra bản 0.4

Build `assembleDebug` và Android Lint thành công (0 lỗi, 12 cảnh báo). 28 tình huống quy tắc và 3 tình huống parser chạy qua. Không thay đổi quy tắc nghiệp vụ trong lần sửa giao diện này.

Kiểm tra trực tiếp trên giả lập Android 10 / API 29, density 293:

- 720×1520: Tổng quan vừa màn hình với bốn lượt tàu và ba sự cố minh họa; không có ScrollView ở trang chính. Đầu trang, sơ đồ, hai khối, tên thiết kế và thanh dưới đều hiển thị.
- 720×1280: số dòng thích ứng còn hai lượt tàu và hai sự cố, giữ các khối trong màn hình; mở được danh sách tàu đầy đủ riêng.
- Chọn 320+445 đến 321+780: danh sách chỉ giữ dữ liệu trong phạm vi nhưng sơ đồ đủ 13 mốc, vị trí và kích thước không đổi. Số sự cố còn một, thẻ tự chia lại chiều cao.
- Chỉ một nút tắt âm ở đầu khối Sự cố; thao tác chuyển thành “Bật chuông”, ba lỗi vẫn giữ nguyên.
- Chữ “Tàu lẻ” nằm trên mũi tên, không còn “Lý trình tăng” hay chú giải cố định bên dưới.
- Biểu tượng đầu tàu trong trang thông tin ứng dụng Android khớp biểu tượng ở đầu trang.
- Ảnh thực lưu ở `screenshots/*-0.4.png`.

Giả lập chạy bằng software emulation; hệ điều hành có ANR System UI lúc khởi động, kiểm tra sau khi hệ thống ổn định. Không ghi nhận crash ứng dụng trong các thao tác kiểm tra. Chưa kiểm tra bản này trực tiếp trên Xiaomi 14T Pro, mọi cỡ màn hình hoặc mọi cỡ chữ trợ năng.

Luồng HTTP Visitec, nhận chuông trên thiết bị thật, polling khi khóa màn hình, suy luận tự kích và vị trí tàu thật vẫn chưa được xác minh. Sơ đồ xanh trong ảnh là dữ liệu minh họa, không phải vị trí tàu ngoài hiện trường.
