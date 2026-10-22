# Kiểm tra bản 0.6 — 22/10/2026

## Dữ liệu thật

Đã dùng tài khoản được chủ tài khoản cho phép để đăng nhập Visitec trong phiên kiểm tra, không lưu tài khoản/mật khẩu vào repo hay APK. Môi trường lần này truy cập được website; giới hạn HTTP 403 ghi ở biên bản 0.5 không còn áp dụng cho phiên này.

- Xác minh trang ASP.NET có cả nút Thoát và Đăng nhập; bộ đọc chọn đúng nút Đăng nhập, giữ cookie và hidden postback.
- Xác minh bộ chọn ngày, lý trình và Tất cả/Trở ngại. Khắc phục việc Jsoup không tự trả selected option qua `select.val()`.
- Đọc ngày hiện tại và ngày trước qua toàn bộ phân trang, khoảng 20 trang/ngày, xấp xỉ 1.995 bản ghi sau gộp trùng/ngày. Lấy đủ 13 điểm tuyến. Đọc lại đầu trang sau lượt phân trang; lượt tăng thêm dừng khi gặp mốc đã lưu, thử được 99 bản ghi trong khoảng 2,6 giây. Số lượng/thời gian này chỉ là quan sát tại thời điểm thử.
- App trên giả lập Android đã đăng nhập qua giao diện và đồng bộ. Khi tắt màn hình 80 giây, ghi nhận thêm 5 mốc cập nhật thành công. Không coi thử nghiệm này là bằng chứng về HyperOS/Doze trên điện thoại thật.
- Không đưa HTML, dữ liệu vận hành hay ảnh màn hình tài khoản thật vào repo. Ảnh giao diện đính kèm dùng dữ liệu minh họa.

## Kiểm tra tự động

- **89** tình huống quy tắc: tuyến động, ga, hướng, nhiều điểm chiếm dụng, đúng thiết bị, điện áp khác lỗi cảm biến, trục, tự kích và bản tin tới muộn.
- **12** tình huống parser; **13** tình huống đọc web cục bộ: đăng nhập đúng nút, sai mật khẩu, ngày/bộ lọc, phân trang dấu ba chấm, đọc tăng thêm, mất mốc, đầu trang dịch chuyển, ngày rỗng hợp lệ, trả nhầm ngày/tuyến/bộ lọc bị từ chối.
- **22** kiểm tra lưu trữ Android: cách ly URL/ID, dữ liệu muộn, mốc/phạm vi, không xóa bản tin gốc khi bỏ suy luận, lưu lỗi chưa khôi phục, lưu chiếm dụng qua cửa sổ thời gian, đúng bản tin giải phóng.
- **7** kiểm tra service trên giả lập với web cục bộ: khởi tạo im lặng, giữ nguyên lỗi chuông, chờ tự kích, kênh có chuông, chứng cứ tới muộn/khôi phục, lỗi mới vẫn đỏ khi tắt âm thanh và kênh im lặng. Script được sửa để xử lý lệnh dừng service không được shell cho phép và lượt đọc ADB rỗng tạm thời; lượt cuối đạt cả 7 kiểm tra.
- Build APK, APK kiểm tra và Android Lint thành công. Lint không có lỗi, còn 14 cảnh báo, gồm target SDK, cấu hình biểu tượng/sao lưu, vẽ và chuỗi giao diện.

## Cơ chế mới

Lượt đầu tải hai ngày để nối trạng thái. Lượt sau đọc chồng, làm mới đầy đủ 15 phút/lần để bổ sung bản tin đến muộn. Lỗi chưa khôi phục và chiếm dụng được lưu riêng; UI không phải đọc toàn bộ kho sự kiện để dựng trạng thái. Mật khẩu thay đổi mở phiên đọc mới; sai xác thực dừng dịch vụ.

Suy luận tự kích chỉ xét lượt tàu mới, chờ ít nhất một chu kỳ/tối thiểu 30 giây, mỗi lượt phía trước dùng một lần. Ga/đầu tuyến bỏ đối chiếu phía không có điểm liền kề. Khi có chứng cứ tới muộn, xóa riêng bản tin suy luận; khôi phục hệ thống của điểm kết thúc suy luận còn treo. Thông báo ghi “Nghi tự kích” và tên điểm, không đưa diễn giải dài lên màn hình.

Màu lỗi và câu lỗi giữ theo bản tin web. Mẫu minh họa dùng “Chuông 2 hỏng”. Tắt chuông chung không xóa đỏ; lỗi đã khôi phục không phát thông báo mới. “Designed by Cao Xuân Cường” chỉ ở Cài đặt.

## Giao diện APK cuối

Đã mở Tổng quan, Đường ngang, Sự cố và Cài đặt trên giả lập: dòng tên thiết kế không xuất hiện ở ba trang đầu, xuất hiện đúng một lần trong Cài đặt. Tổng quan minh họa giữ đủ 11 đường ngang và 2 ga, nhiều hướng chiếm dụng, lỗi chuông đúng câu “Chuông 2 hỏng”. Ảnh: [Tổng quan](screenshots/overview-0.6.png), [Cài đặt](screenshots/settings-0.6.png).

## Cần thử tại hiện trường

Xiaomi 14T Pro: cấp thông báo, cho chạy nền và thử khóa màn hình lâu với các chu kỳ 30/120/300 giây; đối chiếu sự cố/khôi phục và tự kích với người trực. Suy luận không chứng minh được nguyên nhân vật lý hoặc nhận diện từng đoàn tàu khi bản tin thiếu/trễ. Không tự chạy lại sau khởi động máy. Bản ghi cùng giây/nội dung hoàn toàn giống nhau không có ID gốc để phân biệt, nên được gộp. Không cam kết tương thích mọi website hay vùng có cấu trúc khác.
