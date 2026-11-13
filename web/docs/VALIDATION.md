# Kiểm chứng gói web 1.1.0 — 13/11/2026

Kết quả của phiên hiện tại; không kế thừa tuyên bố hoàn tất từ ZIP 1.0.0. Chỉ web thay đổi, không Android/APK. Rules.java, WebSource.java, AlertOptions.java so nguyên byte với commit Android 0.27.3 `ee1affe5ea024590ce356e0f1e23aafbf17df669`: khớp.

| Kiểm tra thực thi | Kết quả |
|---|---|
| Core Java gốc | 772 ca nghiệp vụ đạt |
| Parser và HTTP reader fixture | 16 + 39 ca đạt |
| AlertOptions gốc | 17 ca đạt |
| Backend SQLite + Java/HTTP thật cục bộ | 14 bài đạt |
| Scheduler/suy luận mới | Nhịp thành công 10 giây; retry mạng 120 giây; pending ở giây 10/20/29, xác nhận giây 30; maintenance offline không xác nhận suy luận |
| Dữ liệu web | AxleError lọc scope qua source; history backfill/checkpoint, không replay lỗi cũ, account isolation, CSRF, chặn static private files; endpoint audio/stream trả 404 |
| Âm foreground | Test hủy generation cũ không dừng batch mới; recovery loại cảnh báo hết hiệu lực |
| Chromium với server HTTP và parser thật | 320/375/390/430 px không tràn trang; thứ tự summary→kết nối→sơ đồ; retry, confirm, sáu nhóm, ba bộ lọc đường ngang, settings/mode/reload không lỗi JS |
| Build | Java compile, npm run build, JS syntax đạt |
| ZIP độc lập | Giải nén ngoài checkout, build core và chạy lại 772/16/39/17 + 14 backend + tests âm + npm build đạt; không cần Android SDK |
| Máy chủ local | /health 200 ok:true; /api/session khởi tạo được; worker chạy không cần browser trong test |

Ảnh bốn tab/chi tiết và mất kết nối trong `screenshots/` được tạo lại từ fixture của phiên này. Không phải dữ liệu vận hành. Luồng MP3 đã xóa cả backend/UI/dependency Docker; hai WAV hữu hạn giữ cho cảnh báo foreground.

## Ranh giới bàn giao

**Chưa thử trong phiên này:** tài khoản/website vận hành thật, Safari/WebKit, iPhone vật lý, Apple Push thật, PostgreSQL production, Docker image mới và Google Cloud triển khai thật. Không có URL HTTPS sản phẩm đã nghiệm thu. Những kết quả Docker/PostgreSQL/media được ghi trong biên bản cũ không được dùng để xác nhận bản 1.1.0.

Theo yêu cầu chủ sở hữu, AI Studio tích hợp thông báo, chạy nền iPhone và cấu hình máy chủ theo `INTEGRATION.md`, rồi thực hiện nghiệm thu thực tế ở đặc tả gốc và `ACCEPTANCE.md`. Gói mã nguồn build được không đồng nghĩa mọi ID nghiệm thu đã đạt. Core tests chứng minh bộ quy tắc gốc; backend/browser tests chứng minh những đường đi được liệt kê, không tự phủ toàn bộ native instrumentation hay mọi tổ hợp vận hành.

Không sửa ngưỡng nghiệp vụ: Nghi tự kích 30 giây, retry mạng 120 giây từ kết thúc lỗi, stale 60 giây, full refresh 15 phút, retention 30 ngày. Chỉ lịch đọc nguồn đổi 30→10 giây. Bản web trước dùng stale 90 giây; bản này sửa về 60 giây theo đặc tả Android. Bổ sung nối UI bị thiếu: Đếm trục lỗi, ba filter, Áp dụng phạm vi, Ngày/phạm vi đứng trước tài khoản; sửa lọc AxleError và không xác nhận self từ maintenance/cache. Không thay core.
