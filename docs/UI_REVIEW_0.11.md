# Chuông sáu hồi và phục hồi tự kích — 0.11

Ngày 23/10/2026, giờ Việt Nam. Chủ dự án phản hồi app cơ bản đã chạy tốt và yêu cầu hai bổ sung dưới đây.

## Hành vi

- Âm báo cũ ba hồi/6 giây được phát thành **sáu hồi/12 giây**, giữ nguyên âm và nhịp. Phát một lượt, không nhắc định kỳ. Nút tắt chuông chung vẫn dừng ngay, giữ lỗi đỏ. Cài đặt → Thử chuông báo lỗi dùng cùng tệp âm.
- Khi nhận **“Ấn nút Phục hồi”** từ web tại đường ngang đang **Nghi tự kích**, app khép riêng cảnh báo tự kích tại điểm đó, đánh dấu đã khôi phục trong tab Sự cố và tắt đoạn xanh của đúng lượt tự kích. Không đợi “Khôi phục hệ thống”.
- Giữ nguyên lỗi chuông/đèn/cần chắn/cảm biến/kết nối/điện áp chưa có bản tin phục hồi tương ứng và các lượt tàu độc lập. Nút phục hồi đơn lẻ không xóa lượt tàu chưa bị đánh dấu tự kích.
- Bản tin nút phục hồi được nhận dạng không phân biệt hoa/thường, gộp khoảng trắng; giữ nguyên chữ gốc để hiển thị. Bản tin cùng giây xử lý theo thứ tự tiếp cận → suy luận → đã qua → phục hồi.
- Giữ dấu kết thúc: bản tin tàu đã qua đến muộn không làm đoạn tự kích sáng lại. Lượt tàu mới sau đó vẫn hiện bình thường.

## Lưu trữ và cập nhật

`Journey.selfTriggered` gắn suy luận vào đúng ID bản tin tiếp cận. Cờ được giữ qua JSON/checkpoint; `travelVersion=3` tự dựng lại trạng thái từ raw khi cài đè bản cũ. Không xóa tài khoản/cài đặt/lịch sử.

Nếu cache lỗi còn tự kích từ bản trước và bản tin phục hồi nằm trước trang delta mới nhất, Store phát lại từ mốc lỗi đó để không bỏ sót phục hồi đã lưu. Truy vấn lịch sử liên kết được bản tin nút phục hồi ở ngày sau, kể cả cách viết hoa/khoảng trắng khác nhau. Chiếm dụng phần cứng thô vẫn chỉ kết thúc theo khôi phục hệ thống; đoạn xanh hiển thị có quy tắc kết thúc riêng cho tự kích.

Khi đổi sang 2 phút/5 phút, chỉ bỏ suy luận đang mở; giữ bằng chứng của lượt đã được phục hồi bằng nút để không dựng lại nó thành tàu thật.

Cập nhật lỗi/sơ đồ trên cùng các view hiện tại, giữ cơ chế chống chớp và toàn tuyến khi lọc phạm vi. Không sửa điều kiện chỉ suy luận tự kích ở chu kỳ 30 giây.

## Kiểm tra

- **355 kiểm tra quy tắc** đạt, gồm phục hồi tự kích ở đoạn đến/đi, hai hướng, bản tin muộn/cùng giây, lỗi thiết bị khác, tàu độc lập và lượt tàu mới sau reset.
- **40 kiểm tra lưu trữ Android** đạt: cờ tự kích qua checkpoint/khởi động lại, nút phục hồi trước trang delta mới nhất, liên kết khôi phục khác ngày/cách viết, di trú cache bản 0.10 và giữ lỗi chuông.
- **43 kiểm tra UI Android** đạt: hiện rồi bỏ cảnh báo tự kích, tắt đoạn xanh trên cùng canvas, giữ lỗi thiết bị; player đọc đúng 12 giây, còn chạy sau thời lượng ba hồi cũ, tự hoàn tất/giải phóng không lặp, dừng ngay khi tắt. Âm thanh giả lập có thể tiêu thụ chậm hơn đồng hồ nên bài kiểm tra đợi hoàn tất với giới hạn riêng, vẫn kiểm tra độ dài media đúng 12 giây.
- **12 parser / 13 đọc web fixture** đạt.
- **12 kiểm tra service fixture** đạt trên APK bàn giao: chu kỳ thực 30 giây, lỗi chuông nguyên văn/âm báo, xác nhận tự kích, nút phục hồi khép lỗi và đoạn sáng nhưng giữ lỗi chuông, cờ hoàn tất lưu được, đổi sang 2 phút không làm sáng lại (cả khi tải bù khôi phục hệ thống trước nút phục hồi), chứng cứ muộn/khôi phục đúng thiết bị, tắt âm chung và phát lỗi đang mở khi bắt đầu phiên. Khôi phục dữ liệu/proxy giả lập sau kiểm tra.
- Build APK/gói test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**.
- Đã kiểm tra APK đúng versionCode/versionName, chữ ký cũ và checksum của toàn bộ APK bàn giao.

## Bản cài và giới hạn

`downloads/DuongNgang-0.11.apk`, versionName 0.11.0, versionCode 11. Bộ cài đầy đủ cho người cài mới hoặc cài đè bản cũ cùng chữ ký, giữ cấu hình. Chứng chỉ SHA-256: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.

Lượt này dùng dữ liệu tổng hợp và giả lập Android; không đăng nhập lại website thật, không có bản tin/ảnh vận hành được đưa vào Git. Kiểm tra player trên giả lập không chứng minh âm lượng/loa/chế độ pin Xiaomi thật. Sơ đồ và phục hồi cập nhật khi app nhận được bản tin ở lần đọc web, theo chu kỳ người dùng đã chọn.
