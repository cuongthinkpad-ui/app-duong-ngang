# Sửa dấu vết tàu treo — APK 0.23 đã ký

Chủ dự án xác nhận ngày 31/10/2026: áp dụng cho cả tàu chẵn và tàu lẻ. Khi lượt tàu thực tế mới đi vào đoạn liên quan, bỏ dấu vết cũ treo trên đoạn đó và nhận lượt mới ngay, không chờ bản tin đã qua/khôi phục. Nút bấm, nâng/hạ cần và khôi phục không tự trở thành lượt tàu mới. Giữ các đoạn độc lập, lượt theo sau bắt đầu sau lượt đang ghép, lỗi gốc và quy tắc suy luận mất kết nối.

## Logic

- Bản tin tàu rõ hướng ghép với nguồn mới nhất chưa được nhận ở đường ngang liền trước, có thời điểm không sau bản tin hiện tại. Khép các dấu vết cũ chưa ghép trên chính đoạn/hướng đó ngay; không ưu tiên lấy dấu vết cũ nhất như 0.22. Nguồn mới hơn thời điểm đến của lượt đang ghép vẫn được giữ riêng. Đây là quy tắc đồng bộ biểu diễn theo bản tin, không xác định danh tính/số hiệu tàu vật lý.
- Thao tác cần chắn chỉ ghép một nguồn có sẵn như trước; không được chạy nhánh dọn dấu vết dành cho bản tin tàu thật. Khi bản tin thật đến, đối chiếu lại nguồn mới nhất kể cả trước đó thao tác đã tạm ghép vào dấu vết cũ. Chu kỳ thao tác/khôi phục trước bản tin thật không để lại thêm đoạn tàu giả.
- Bản tin đã qua đến sau lúc điểm tiếp theo nhận tàu vẫn đối chiếu phần phía sau; không bỏ qua chỉ vì lượt hiện tại đã chuyển quyền hiển thị cho điểm sau.
- `travelVersion=5` dựng lại cache từ bản tin còn lưu. Giữ seed checkpoint có nguồn đã quá hạn 15 ngày, không dựng lại toàn bộ từ kho raw đã bị dọn. Giữ `selfEvidenceVersion=4`, `axleVersion=3`, lỗi thiết bị, tài khoản, lưu trữ và giao diện.

## Kiểm chứng

- Đã đọc đầy đủ báo cáo thực tế ngày 31/10 bằng WebSource, 1.998 bản tin. Mã 0.22 tái hiện hai đoạn tàu lẻ trong khoảng được phản ánh. Dữ liệu xác thực và công cụ phát lại đặt ngoài Git; không đưa tài khoản/mật khẩu hoặc bản chụp báo cáo vào repository.
- 629 ca quy tắc đạt, gồm hai hướng, thay dấu vết ngay khi tiếp cận, thao tác ghép nhầm nguồn cũ, chu kỳ thao tác/khôi phục rồi báo tàu thật, lượt nối tiếp, phát lại từng poll và đảo thứ tự. Ca mới thay dấu vết ngay thất bại trên mã HEAD 0.22 trước sửa.
- Phát lại dữ liệu thực tế kiểm tra mỗi giây 16:40:00–16:44:59, cả chế độ raw và có suy luận tự kích, cùng chia checkpoint trước 16:00: tổng 602 kiểm tra. Một đoạn tàu lẻ được giữ đến bản tin khôi phục kết thúc đoạn giáp ga; tàu chẵn phía khác vẫn độc lập. Không thêm ngoại lệ mất kết nối vào bộ suy luận.
- Bổ sung ba ca Android kiểm tra nâng cache 4 → 5 giữ checkpoint có raw hết hạn, số raw và mở lại Store. Runner chờ 174 kiểm tra thay vì 171. Chưa chạy các ca Android này vì máy không có /dev/kvm và không có thiết bị/giả lập đang chạy; không coi biên dịch APK test là đã chạy instrumentation.
- Chưa kiểm chứng trên Xiaomi. Đã dùng khóa khôi phục để ký APK 0.23.0/versionCode 23. `apksigner verify` xác nhận chứng chỉ khớp khóa cũ; applicationId vẫn là `vn.duongngang.monitor`. APK tại `downloads/DuongNgang-0.23.apk`, checksum trong `downloads/SHA256SUMS`. Mã và APK được bàn giao cùng commit; dùng Download raw file trên GitHub. Build dùng đường dẫn metadata riêng qua `DUONG_NGANG_SIGNING_INFO`; script cloud tự nhận metadata đã khôi phục ở ngoài repository. Không ghi mật khẩu/khóa vào Git.

Build cuối: `assembleDebug assembleDebugAndroidTest lintDebug --offline` đạt; lint 0 lỗi, 15 cảnh báo. Parser 12 ca và đọc xác thực bằng web fixture 15 ca đạt. Các kết quả Android runtime/UI/service nêu ở bản 0.22 trước đây không được coi là đã chạy lại cho bản sửa này.

SHA-256 APK 0.23: `a70702fa555c62e0fa9d5c7584b6277d89b6ef6a3bdfdcda1d9c5bf1d1e07373`.
