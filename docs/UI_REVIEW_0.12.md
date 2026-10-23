# Cảnh báo đếm trục — 0.12

Ngày 23/10/2026, giờ Việt Nam. Bản đầy đủ 0.12.0, versionCode 12.

## Quy tắc được chủ dự án chốt

- Cùng một CB ở cùng đường ngang, chỉ cộng những lần lệch tuyệt đối **từ 3 trục trở lên**, không cần liên tiếp. Lệch 1–2 trục chỉ ghi chú. Cảnh báo ở **lần thứ 6 trong ngày**, thông báo một lần/ngày cho CB đó. Các tin nhắn từng sửa ngưỡng nhỏ được thay bằng quyết định cuối này; chủ dự án trả lời rõ lần thứ sáu, không phải thứ bảy.
- Một lần thừa/thiếu **từ 10 trục trở lên**: cảnh báo ngay, kể cả CB đã cảnh báo số lần. Mỗi lần đo lớn mới được cảnh báo riêng; tải lại chính bản tin đó không báo lại. Đúng 10 trục đã đủ ngưỡng đơn lẻ.
- CB khác/đường ngang khác có bộ đếm và thông báo riêng. Cùng một lần đạt cả hai điều kiện chỉ sinh một cảnh báo. Lỗi lớn trước hoặc sau mốc số lần vẫn được giữ riêng, không bị thẻ thống kê mới hơn che mất.
- Giữ đỏ tới **00:00 Việt Nam**, giữ archive. Lượt đếm đúng tiếp theo, ấn nút phục hồi hoặc bản tin khôi phục thiết bị không xóa thống kê sai trong ngày. Không tự gắn tick khôi phục thiết bị cho cảnh báo thống kê.
- Chuông sáu hồi theo lựa chọn âm chung; không có nhắc lại định kỳ. Điều kiện đếm trục áp dụng ở cả 30 giây/2 phút/5 phút, khác suy luận tự kích chỉ áp dụng 30 giây.

Chỉ so sáu số nguyên CB1–CB6 của dòng Khôi phục hệ thống, với chuẩn là số xuất hiện nhiều nhất như quy tắc trước. Không dùng dòng đo điện áp/kèm V/thiếu cột/ngoài tuyến hoặc thiết bị ga. Hòa chuẩn thì giữ ghi chú “cảm biến đếm trục không đồng nhất, lỗi”, không tự gán số sai cho CB nào. Việc các CB cùng đếm một số không chứng minh số đó đúng với đoàn tàu thực tế; đây vẫn là đối chiếu nội bộ CB theo yêu cầu.

## Trình bày

Cảnh báo hiện ở Tổng quan và Sự cố, theo giờ – đường ngang – nội dung. Ví dụ: “CB3 đếm sai 6 lần trong ngày · Lần thứ 6 thiếu 3 trục · Chuẩn 40, đếm 37”, hoặc “CB4 đếm thiếu 12 trục trong 1 lần · Chuẩn 40, đếm 28”. Dòng đỏ gốc trên website vẫn giữ nguyên, không đổi sang nội dung suy luận.

Trong chi tiết, nhóm Cảm biến cũng nhận cảnh báo này. Khối **Đếm trục hôm nay** liệt kê số lần lệch từ 3 của mỗi CB, lần lệch gần nhất và tổng thừa/thiếu của mọi ghi chú (tổng bao gồm lệch 1–2). Chạm **Xem các lần đếm trục** để xem toàn ngày, chọn ngày khác hoặc tải lại web. Không giới hạn ở 12 sự kiện gần nhất. Ngày lịch sử được chọn chủ động không tự đổi theo chu kỳ.

Ảnh dùng hoàn toàn dữ liệu tổng hợp và vẽ cửa sổ app Android thật, không chứa bản tin/ảnh vận hành hoặc hộp thoại System UI của giả lập:

- [Tổng quan](screenshots/axle-overview-0.12.png)
- [Chi tiết và bộ đếm theo CB](screenshots/axle-detail-0.12.png)
- [Hai kiểu cảnh báo trong Sự cố](screenshots/axle-incidents-0.12.png)

## Lưu trữ và âm báo

`Rules.axleErrors` cộng theo ngày Việt Nam/đường ngang/CB trên các ID bản tin khác nhau; `axleAlerts` tạo cảnh báo `axle:<CB>:<ID gốc>`. Các dòng web không bị chỉnh sửa. Khi tải bù, Store dựng lại và loại bản cảnh báo dẫn xuất cũ không còn đúng mốc, cập nhật nội dung từ nguồn đầy đủ hơn.

`axleNotices` lưu dấu thông báo ngưỡng số lần theo ngày/điểm/CB, và dấu từng lần đo lớn. Dấu không phụ thuộc vị trí bản tin thứ sáu nên tải bù không báo lại. Giữ qua mở lại app/service; đổi tài khoản xóa cùng dữ liệu cũ. Một lần đồng thời đủ hai ngưỡng đánh dấu cả hai để tránh thông báo trùng.

Đọc lịch sử riêng không phát chuông. Nâng cấp dựng cảnh báo từ dữ liệu gốc ngày hiện tại trong nền; giữ thông tin tài khoản/cài đặt và cơ chế chống chớp ở Tổng quan. Phạm vi theo dõi vẫn không cắt sơ đồ toàn tuyến.

## Kiểm tra

- **384 kiểm tra quy tắc** đạt, gồm biên 1/2/3, 5/6/7 lần, 9/10/11 trục, hai chiều thừa/thiếu, mốc nửa đêm Việt Nam, trùng trang, dữ liệu đảo thứ tự, CB/đường ngang độc lập, dòng điện áp/ga, hòa chuẩn, kết hợp hai lý do.
- **55 kiểm tra lưu trữ Android** đạt, gồm giữ cảnh báo tới hết ngày, tải bù đổi vị trí lượt thứ sáu, nguyên vẹn bản tin gốc, dấu thông báo một lần qua khởi động lại, CB khác và lỗi lớn mới được báo tiếp, ngày mới/tài khoản mới.
- **50 kiểm tra UI Android** đạt, gồm cảnh báo ở chu kỳ 5 phút, đủ hai kiểu cảnh báo trên Tổng quan, ghi chú buổi sáng ngoài 12 dòng vẫn thấy ở khối mới, số có dấu thừa/thiếu, danh sách cả ngày và chọn ngày, giữ các kiểm tra chống chớp/sơ đồ/âm báo cũ.
- **12 parser / 13 đọc web fixture** đạt.
- **21 kiểm tra service fixture** đạt: giữ các trường hợp lỗi gốc/tự kích/phục hồi/âm báo; thêm năm lần chưa cảnh báo, lần thứ sáu có âm, lần thứ bảy không nhắc, khởi động lại không báo lại, CB khác có âm mới, lệch 12 trục rồi đúng 10 trục trên CB đã báo đều có âm mới. Chạy chu kỳ đọc thật 30 giây; dữ liệu/proxy giả lập được khôi phục sau kiểm tra.
- Build APK/gói Android test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**.

## Bản cài và giới hạn

APK `downloads/DuongNgang-0.12.apk` cài mới trực tiếp hoặc cài đè cùng khóa, giữ cấu hình. Chứng chỉ SHA-256: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.

Lượt này không đăng nhập web thật. Các kiểm tra dùng giả lập và máy chủ fixture; chưa xác nhận tiếng loa/tiết kiệm pin Xiaomi thật. Hộp thoại ANR của **System UI** trên giả lập phần mềm từng che ảnh OS; không phải crash tiến trình app. Ảnh bàn giao lấy lại cửa sổ app, không đưa ảnh bị hộp thoại che vào repo.

Thống kê dựa trên bản tin đã đọc được, không thể cộng các bản tin web không cung cấp hoặc bị thiếu. Web không có ID độc lập thì những dòng hoàn toàn giống cùng giây vẫn được gộp như bản cũ. Reset 00:00 không phải khẳng định thiết bị đã được sửa. Chưa thay đổi quy trình trực hay điều khiển thiết bị.
