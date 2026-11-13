# Bàn giao AI Studio — web 1.1.0, 13/11/2026

## Yêu cầu mới nhất của chủ sở hữu

Giữ giao diện và toàn bộ nghiệp vụ Android 0.27.3. Không sửa Android/APK. Chỉ đổi chu kỳ lấy nguồn của web từ 30 xuống **10 giây**. Không đổi thuật toán/ngưỡng/mốc chờ: đặc biệt Nghi tự kích vẫn ≥30 giây và có lần đọc thành công tiếp theo, retry lỗi mạng 120 giây từ kết thúc lỗi. Bỏ hoàn toàn luồng âm thanh liên tục; không đưa lại MP3, audio im lặng hoặc keep-alive media.

Đặc tả đầy đủ: `docs/BAN_GIAO_DAY_DU_VIET_LAI_WEB_0.27.3.txt`. Các câu 30 giây về nhịp lấy nguồn trong tài liệu này được yêu cầu mới thay bằng 10 giây; 30 giây nghiệp vụ suy luận giữ nguyên. Phần đề xuất media liên tục đã hủy. Không coi ZIP cũ 1.0.0 là bản đáp ứng yêu cầu hiện tại.

## Phần đã có trong gói

Backend Python bền vững, adapter Java gọi nguyên core chuẩn, ASP.NET parser/đọc nguồn, scheduler 10/120, giao diện bốn tab, sáu nhóm thiết bị, Đếm trục lỗi, lịch sử/ga/phạm vi, queue cảnh báo và adapter Web Push tham khảo. Quy tắc không viết lại bằng logic rút gọn. Foreground có chuông/TTS tiếng Việt và XÁC NHẬN. Đọc kết quả thực tế của phiên tại `docs/VALIDATION.md`; không dùng biên bản cũ để nhận đã thử bản này.

## AI Studio phải tích hợp

1. Thông báo: nối queue/seq với dịch vụ Push, lưu subscription, xác nhận/mute và chống trùng; đọc hợp đồng `docs/INTEGRATION.md`.
2. Chạy nền iPhone: worker máy chủ luôn chạy, PWA/permission/Web Push theo iOS thật. Không chạy timer 10 giây trong Service Worker. Không cam kết TTS/rung/action khóa màn hình khi chưa kiểm chứng. Không dùng luồng âm thanh liên tục.
3. Máy chủ: HTTPS/origin, PostgreSQL, APP_KEY/VAPID ổn định, backup, always-on CPU/worker, allowlist nguồn/Push, giám sát và tải thực. Có Dockerfile và mẫu triển khai; AI Studio lựa chọn/cấu hình tài nguyên thật.
4. Kiểm thử website có tài khoản hợp lệ trên hạ tầng thật; kiểm tra iPhone vật lý và các ID còn chưa thử trong ma trận gốc. Không xin mật khẩu trong source/chat; nhập qua UI/secret settings.

## Cách tiếp tục

Giải nén ZIP, chạy đầy đủ các lệnh README, đọc hợp đồng API trước khi nối frontend/backend khác. Giữ ba core Java nguyên bản; nếu port ngôn ngữ phải đối chiếu toàn bộ fixture gốc. Không sửa gói APK. Giao URL HTTPS, revision triển khai, ảnh và kết quả thật sau khi tích hợp. Gói mã build/test cục bộ chưa phải nghiệm thu vận hành thực địa.
