# Bắt đầu tại đây — Giám sát Đường ngang Web 1.2.0

Gửi toàn bộ ZIP này cho AI Studio, sao chép `PROMPT_AI_STUDIO.md`. Đây là gói mới thay bản 1.1.0; không phải APK hoặc website đã nghiệm thu.

Đọc theo thứ tự:

1. `docs/LATEST_REQUIREMENTS_1.2.0.md`: quyết định mới nhất, ưu tiên khi mâu thuẫn.
2. `docs/BAN_GIAO_DAY_DU_VIET_LAI_WEB_0.27.3.txt`: đặc tả đầy đủ giao diện/nguồn/nghiệp vụ; các mốc cũ và đề xuất media đã bị mục 1 thay thế.
3. `README.md`, `docs/INTEGRATION.md`, `deploy/README.md`: build, API/account/session/outbox/confirm/mute, secret/database/backup/deploy.
4. `docs/VALIDATION.md`, `docs/ACCEPTANCE.md`: kết quả đã chạy và nghiệm thu còn thiếu.
5. `reference/android/`: mã nguồn/bộ test Android bất biến để đối chiếu; không sửa APK. `engine-core/Rules.java` là biến thể web có chủ ý, không nhận nhầm là core gốc.

Chạy các lệnh README trong thư mục chứa server.py. Cần JDK 21 đầy đủ, Python 3.12+, Node 20+, curl và Chromium cho browser test. Không cần Android SDK/khóa ký để build web.

`SOURCE_REVISION.json` ghi revision xuất phát và SHA-256 nguồn; `SOURCE_SHA256.json` kiểm toàn bộ file trong gói (trừ chính manifest). Checksum ZIP nằm cạnh file phát hành. Gói whitelist không lấy runtime DB, tài khoản, key/keystore hoặc capture đăng nhập.
