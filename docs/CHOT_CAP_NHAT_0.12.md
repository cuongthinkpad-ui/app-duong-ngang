# Chốt thay đổi bản 0.12

Cập nhật 23/10/2026. APK đã đóng gói, kiểm tra và lưu trong workspace; **chưa đăng lên GitHub**, đang để chủ dự án xem lại trước khi đăng.

## Hai ngưỡng cảnh báo

| Điều kiện | Cách xử lý |
| --- | --- |
| Lệch 1–2 trục | Chỉ ghi chú, không cộng số lần sai. |
| Lệch từ 3 trục trở lên | Cộng một lần cho đúng CB, đúng đường ngang trong ngày. |
| Đến lần sai thứ 6 trong ngày | Cảnh báo đỏ, thông báo/chuông một lần cho CB đó trong ngày. Lần thứ 7, 8… không nhắc lại chỉ vì tăng số lần. |
| Một lần thừa hoặc thiếu từ 10 trục trở lên | Cảnh báo ngay, kể cả đúng 10 và kể cả CB đã cảnh báo số lần. Mỗi lần đo lớn mới được báo riêng; tải lại cùng bản tin không báo lại. |

CB khác/đường ngang khác có bộ đếm riêng. Cùng một bản tin đạt cả hai ngưỡng thì gộp một cảnh báo. Mở lại app hoặc tải bù không phát lại thông báo ngưỡng số lần đã báo.

Chuẩn so sánh vẫn là số trục xuất hiện nhiều nhất ở CB1–CB6 trong bản tin Khôi phục hệ thống. Bỏ bản tin đo điện áp/kèm V. Nếu không có chuẩn chung duy nhất thì giữ ghi chú không đồng nhất, không tự chỉ định CB nào sai bao nhiêu trục.

## Trình bày và thời hạn

- Cảnh báo ở Tổng quan và Sự cố, theo thời gian – đường ngang – nội dung; ghi rõ CB nào, thừa/thiếu bao nhiêu, chuẩn và số đếm.
- Ví dụ: “CB3 đếm sai 6 lần trong ngày · Lần thứ 6 thiếu 3 trục · Chuẩn 40, đếm 37”.
- Ví dụ: “CB4 đếm thừa 10 trục trong 1 lần · Chuẩn 40, đếm 50”.
- Giữ cảnh báo thống kê đến 00:00 giờ Việt Nam rồi đặt lại phần trong ngày; lịch sử vẫn lưu. Lượt đếm đúng hoặc nút phục hồi không xóa cảnh báo thống kê trước giờ này.
- Dùng chuông sáu hồi nếu bật âm; tắt chuông chung vẫn giữ lỗi đỏ. Không có nhắc lại định kỳ.
- Chi tiết đường ngang chỉ thêm một mục **Đếm trục lỗi**, chỉ hiện khi có CB đạt ngưỡng. Thống kê các CB đó: số lần, lần lệch gần nhất và tổng thiếu/thừa. Không thêm màn danh sách đếm trục riêng. Khi đạt ngưỡng, thông báo đỏ/chuông vẫn phát như trên; mục thống kê không thay thế cảnh báo.

Giữ các chức năng đã chốt trước: sơ đồ chuyển đúng một đoạn theo lượt tàu, ngắt tại ga; đủ tuyến khi lọc phạm vi; tự kích chỉ ở chu kỳ 30 giây; “Ấn nút Phục hồi” khép đúng lượt tự kích; nguyên văn lỗi đỏ của web; cập nhật trong các khung hiện tại; chữ ký chỉ ở Cài đặt. Cài mới được, cài đè cùng khóa giữ cài đặt.

## Kiểm tra và ngữ cảnh làm việc

Đã đạt 384 kiểm tra quy tắc, 55 lưu trữ Android, 48 giao diện, 12 parser, 13 đọc web fixture và 21 service fixture; build/Lint không có lỗi. Lượt này dùng dữ liệu tổng hợp và giả lập, chưa kiểm tra loa/pin Xiaomi thật. Chi tiết và ảnh ở [biên bản 0.12](UI_REVIEW_0.12.md).

Yêu cầu/nguyên lý và quyết định cũ nằm trong [bản bàn giao](BAN_GIAO.md), [nhật ký trao đổi](LICH_SU_TRAO_DOI.md) và README. Đây là bản tóm tắt để tiếp tục công việc, **không phải bản sao nguyên văn toàn bộ cuộc trò chuyện**; tài liệu trong repository không tự sao lưu lịch sử hiển thị của ChatGPT.
