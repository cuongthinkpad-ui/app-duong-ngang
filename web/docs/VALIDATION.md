# Kiểm chứng web 1.2.0 — 14/11/2026

Kết quả chạy mới trong phiên này, không nhận kết quả 1.1.0 làm bằng chứng thay đổi mới. Nguồn Android/APK trong checkout không sửa. Core web ở engine-core là bản riêng: bỏ notes/errors |delta|<3 và đổi điều kiện historical self từ 30 xuống 10 giây; adapter live xác nhận sau ≥10 giây trên lần đọc nguồn thành công, maintenance không xác nhận.

| Kiểm tra | Kết quả |
|---|---|
| Core Android gốc, compile riêng build/reference | 772 ca đạt |
| Parser / HTTP reader fixture / AlertOptions gốc | 16 / 39 / 17 đạt |
| Core web ghi chú CB | 8 kiểm tra đạt, ±1/±2 và CB lớn cùng bản tin |
| Backend + yêu cầu web mới | 22 bài đạt (16 backend, 6 quy tắc mới riêng) |
| Tự kích | 9.999 giây chưa báo; 10 giây + lần analyze sau đọc thành công đủ điều kiện; offline ở 10/50 giây không báo; bằng chứng muộn bác bỏ đúng self |
| Retry | 50 giây từ sau lỗi; giữ cache; retry tay bỏ chờ; không phát lại lỗi cũ; worker chạy không browser |
| Trục | ±1/±2 không records dẫn xuất/totals/faults; raw/số gốc giữ; nhiều CB độc lập; 3/6 lần/9/10, source mới, duplicate, ngày/điểm, 6 ngoại lệ; nâng cache cũ và history |
| Chromium HTTP/Java thật cục bộ | 320/375/390/430 px không tràn; chấm đỏ, ga vàng, đoạn xanh; cập nhật màu khi không đổi route/segments; N sự cố/Tốt/chưa đủ; retry/confirm/groups/settings/reload không JS exception |
| Âm foreground | Hai test hủy batch/recovery đạt; không phải kiểm chứng đã nghe trên iPhone |
| Build | javac, node syntax và npm run build đạt |

Máy dùng Temurin JDK 21.0.9+10 tải từ Adoptium và xác minh SHA-256, Python 3.12, Node 24, Chromium hệ thống. JAR jsoup/json xác minh SHA-256 theo build script; dependencies Python pin theo lockfile.

## Phần nghiệm thu còn thiếu

Website/tài khoản vận hành thật, Safari/WebKit, Android/iPhone vật lý, Apple Push thực, giọng khi khóa máy, PostgreSQL production, backup/restore production, build Docker image mới và deploy HTTPS chưa thử. Không có URL sản phẩm đã nghiệm thu. Push mock chỉ kiểm outbox/chống trùng, không chứng minh nhà cung cấp giao được tới iPhone. Retry tests dùng clock/delay cục bộ, không phải phép đo mạng vận hành 50 giây trên thiết bị thật.

AI Studio tiếp tục ma trận SRC/TRN/FLT/SELF/AXLE/ALM/DB/UI/BG/IOS của đặc tả, áp dụng yêu cầu mới LATEST_REQUIREMENTS_1.2.0.md. Các ID chưa có kiểm thử cụ thể không được tự đánh dấu đạt chỉ từ tổng số core tests. Hai thay đổi giao diện APK riêng vẫn chưa thực hiện.

## Gói độc lập và môi trường

Giải nén ngoài checkout vào /tmp, tạo venv mới từ lockfile, build Java, chạy lại 772/16/39/17 core gốc + 8 web axle-note + 22 backend/quy tắc + 2 alarm tests + npm build + Chromium bốn chiều rộng: đạt. Không cần Android SDK hoặc nguồn nằm ngoài ZIP. Máy chủ local chạy worker, /health trả 200 ok:true và /api/session tạo phiên riêng được. Script cài môi trường chạy lại thành công, dùng lại dependencies hợp lệ.

`git diff ee1affe5 -- app tests` và diff so với base web xác nhận không sửa Android/test gốc/APK. WebSource.java và AlertOptions.java của engine-core giữ nguyên. ZIP có manifest SHA-256 từng file và revision nguồn; danh sách xuất whitelist loại DB/runtime/key/keystore/capture xác thực. Đây là xác nhận mã nguồn/cục bộ, không phải xác nhận production/iPhone.
