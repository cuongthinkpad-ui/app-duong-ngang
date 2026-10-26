# Sơ đồ khi thiếu bản tin và mất cả lượt — 0.19

Ngày 26/10/2026, giờ Việt Nam. APK 0.19.0, versionCode 19, cùng applicationId và chứng chỉ cũ.

## Phương án được chủ dự án xác nhận

Chủ dự án yêu cầu điều tra trước khi sửa. Đã đọc đủ 3.187 bản tin hai ngày báo cáo thật, phát lại và tái hiện đoạn bị treo. Có một lượt thiếu bản tin tiếp cận/đã qua tại điểm tiếp theo dù có cần chắn và khôi phục. Hàng chờ này sau đó lấy bản tin của lượt sau, để lại dấu vết mới. Chủ dự án bổ sung việc mất cả dữ liệu một lượt do sự cố: lượt thật kế tiếp có bằng chứng đi qua phải giúp đồng bộ lại. Bốn phần đã được nhắc lại, chủ dự án trả lời OK triển khai. Chỉ ghi kết quả tổng hợp trong repo; dữ liệu vận hành, tài khoản/cookie và công cụ phát lại riêng nằm ngoài Git.

## Thực hiện

- Chỉ dùng bản tin gốc không đỏ xác nhận cần chắn đã hạ/nâng. Tìm tàu chưa nối ở điểm liền kề đang đi tới, không qua ga; nếu hướng không xác định duy nhất hoặc không có tàu thì không tự tạo lượt. Ghép các cần trong cùng lượt. ID `travel:motion:…` chỉ lưu trong trạng thái sơ đồ, không thêm vào raw, lịch sử tàu, thống kê trục hoặc thông báo.
- Bản tin tiếp cận rõ ràng bổ sung trong lượt cần chắn sẽ thay ID nội bộ bằng ID thật và giữ liên kết cũ. Nếu bản tin cũ tới muộn thì giữ cả phase đã qua/đã nối; bản tin có thời điểm sau phase cũ được xét thành lượt mới, tránh che mất tàu mới. Khôi phục của lượt được biết hướng chuyển/đóng đoạn như bình thường; khôi phục đơn lẻ không có lượt/bằng chứng không tự phát sinh tàu. Ga/đầu tuyến đóng theo khôi phục đường ngang giáp ranh; đoạn giữa hai đường ngang vẫn giữ liên tục.
- Khi đã có bằng chứng đi qua hoặc khôi phục một lượt, đối chiếu nguồn mới phù hợp trước thời điểm lượt tại điểm tiếp theo bắt đầu để khép các dấu cũ cùng đoạn/hướng. Tiếp cận đơn lẻ vẫn ghép từng lượt; các nguồn mới bắt đầu sau lượt đang xét và tàu ở đoạn khác được giữ. Có hướng ngược vẫn áp dụng quy tắc mâu thuẫn hiện có. Không xóa theo đồng hồ; không xác định danh tính tàu hoặc GPS từ dữ liệu thiếu.
- `travelVersion=4` dựng lại checkpoint và trạng thái từ raw khi cài đè. Lượt cần chắn có thể nằm trước checkpoint, khôi phục nằm trong phần phát lại sau checkpoint; giữ đúng ID/phase qua JSON và khởi động lại. Không đổi schema bản tin, không xóa tài khoản, phạm vi, lịch sử hoặc lỗi thật. Bản tin khôi phục cũ hơn lượt đang xét không giải phóng lượt mới.
- Giữ `selfEvidenceVersion=2`, `axleVersion=3`, chỉ suy luận tự kích ở 30 giây, sáu hồi chuông, các ngưỡng trục và sáu ngoại lệ. Quy tắc sơ đồ dùng chung các chu kỳ đọc. Giữ nhãn Ga NL và bổ sung ga riêng Chosy.

## Kiểm chứng

- Ca hồi quy mới đã thất bại trên 0.18 trước khi sửa. **554 kiểm tra quy tắc** đạt, gồm hai hướng, thiếu tiếp cận/đã qua, mất cả lượt, chỉ còn tiếp cận ở nguồn mới, chỉ còn đã qua ở đích, thiếu khôi phục cũ, tàu nối tiếp, hướng mâu thuẫn, bảo dưỡng riêng lẻ, lỗi/đỏ không phải chuyển động thành công, khôi phục cũ, đọc chồng/phát lại theo từng poll.
- **12 parser và 15 đọc web mô phỏng** đạt trong lượt kiểm tra; không đổi phần đọc web.
- Phát lại riêng báo cáo thật: đoạn cần đối chiếu hết treo ở cả ba mốc ảnh; sau lượt sau kết thúc, sơ đồ phát lại không còn đoạn chờ ở mốc cuối. Không coi kết quả này là xác nhận mọi tình huống hoặc danh tính tàu ngoài hiện trường.
- **151 kiểm tra lưu trữ** đạt trên APK cuối: chuyển cache 3 → 4, checkpoint trước/sau hoạt động cần chắn, khởi động lại, đồng bộ theo lượt sau, giữ raw/tài khoản/lỗi mạng.
- **61 kiểm tra giao diện** đạt trên APK cuối: sơ đồ xóa đoạn cũ trong cùng root/canvas, giữ lỗi mạng đỏ và đồng bộ sau lượt mất dữ liệu. Giữ các ca hồi quy âm sáu hồi, trục và ga NL.
- **25 ca hồi quy dịch vụ và 4 ca sơ đồ mới** đạt trên cùng APK cuối, chạy theo chu kỳ 30 giây với máy chủ mô phỏng. Bốn ca mới được chạy riêng qua `--diagram-only`: khép lượt có cần chắn/khôi phục, giữ sáng khi thiếu toàn bộ điểm đến, đồng bộ theo lượt sau, giữ lỗi mạng và không phát lại chuông. Bài kiểm tra đối chiếu mốc sự cố có tiền tố Km như dữ liệu web; đợi đủ trạng thái cảnh báo và sơ đồ khi kiểm tra gỡ suy luận.
- Build APK/gói test và Lint đạt với **0 lỗi, 15 cảnh báo**. Đã kiểm tra lại chứng chỉ ký và ảnh giao diện bằng dữ liệu tổng hợp.

Kiểm tra Android dùng dữ liệu tổng hợp và giả lập riêng; không phải kiểm chứng loa/pin HyperOS, độ trễ mạng hay định vị hiện trường trên Xiaomi. Chứng chỉ SHA-256 giữ `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. APK `downloads/DuongNgang-0.19.apk`, checksum trong downloads/SHA256SUMS. Gói sao lưu phiên v0.14 vẫn là snapshot lịch sử.
