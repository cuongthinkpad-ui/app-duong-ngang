# Bổ sung ga Nghi Long riêng khu vực Chosy — 0.17

Ngày 25/10/2026, giờ Việt Nam. APK đầy đủ 0.17.0, versionCode 17, giữ applicationId và khóa ký cũ để cài đè.

## Yêu cầu được xác nhận

Chủ dự án xác nhận ga mới Nghi Long tại Km 303+350 chưa có trên website và yêu cầu bổ sung vào app, chỉ khi dùng ID Chosy. Xử lý như các ga đang có: nằm đúng thứ tự lý trình; ngắt suy luận tự kích qua ga; đoạn tàu vào ga giữ tới khôi phục của đường ngang giáp ga; tàu từ ga ra xuất hiện khi đường ngang phía ra báo tàu. Không tính ga thành đường ngang hoặc điểm chọn phạm vi/so trục.

## Cách thực hiện

`Rules.accountRoute` bổ sung `303+350 (Nghi Long)` cho ID Chosy, bỏ khoảng trắng ngoài và không phân biệt hoa/thường khi chọn cấu hình. Cần có danh sách tuyến đã tải; không tạo tuyến khi chưa có inventory. Giữ nguyên tuyến của ID khác. Nếu website đã có điểm cùng chainage thì ưu tiên điểm từ website, không chèn trùng hoặc ghi đè tên ga của hệ thống.

`WebSource` vẫn xác nhận danh sách website gốc qua toàn bộ phân trang trước khi bổ sung ga vào kết quả đọc. Đọc tăng thêm giữ cùng bổ sung. Không tạo sự kiện giả tại ga, không đổi ID/nội dung/thời điểm hoặc các cột CB của bản tin web. ID Chosy là khóa cấu hình nghiệp vụ được chủ dự án chỉ định, không phải tài khoản đăng nhập điền sẵn hay mật khẩu đóng gói trong APK.

`Store` áp dụng bổ sung vào cả tuyến cache và các lần lưu mới. `travelReady` so sánh topology để không vẽ cache cũ nối xuyên vị trí ga; dựng lại từ raw và checkpoint theo cơ chế hiện có. Nâng `selfEvidenceVersion` lên 2 để đánh giá lại suy luận đã lưu, bỏ pending/nghi tự kích không còn phù hợp ở ranh giới ga, giữ raw/lỗi đỏ thật và không tạo thông báo lịch sử. Không đổi chu kỳ, giao diện, âm báo sáu hồi, ngưỡng trục hoặc sáu ngoại lệ trục.

## Kiểm chứng

- **506 kiểm tra quy tắc** đạt: vị trí ga, cô lập tài khoản, bổ sung lặp không trùng, ưu tiên mốc website sau này, hai chiều tàu vào/ra/khôi phục và ranh giới tự kích.
- **12 parser và 15 đọc web có xác thực mô phỏng** đạt: đọc đủ trang/tăng thêm, bổ sung sau kiểm tra danh sách gốc, giữ nguyên sự kiện và dấu anchor. Máy chủ kiểm tra chạy cục bộ, không phải website vận hành.
- **143 kiểm tra lưu trữ Android** đạt trên APK cuối: dữ liệu đã lưu khi cài đè, thay topology, đánh giá lại suy luận cũ/pending, giữ lỗi chuông thật và bản tin gốc, không tạo bản tin ga, đổi ID không giữ ga bổ sung.
- Build APK/gói test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**. Chứng chỉ SHA-256 giữ nguyên `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.
- **57 kiểm tra giao diện Android** đạt: giữ các kiểm tra cập nhật tại chỗ, âm báo, khôi phục và trục; bổ sung kiểm tra sơ đồ vẫn đủ tuyến khi thu hẹp phạm vi, đoạn tàu từ ga ra và ga không xuất hiện thành một đường ngang trong danh sách.

Không đăng nhập website thật bằng ID Chosy trong lượt này; vị trí/loại ga do chủ dự án xác nhận. Không chạy lại toàn bộ bài service 25 ca của 0.16 trong lượt bổ sung này; đọc web, lưu trữ và quy tắc được kiểm tra như trên. Giả lập không xác nhận loa/pin Xiaomi hoặc tình trạng tàu ngoài hiện trường. Bản sao lưu phiên v0.14 vẫn là snapshot lịch sử, không tự chứa thay đổi 0.17.

APK `downloads/DuongNgang-0.17.apk`, SHA-256 trong `downloads/SHA256SUMS`.
