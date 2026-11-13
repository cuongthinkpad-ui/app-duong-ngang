# Giám sát Đường ngang — gói web 1.1.0 bàn giao AI Studio

Gói mới ngày 13/11/2026 thay gói tham khảo 1.0.0. Chuẩn Android 0.27.3 (`ee1affe5ea024590ce356e0f1e23aafbf17df669`); không sửa Android/APK. Dùng nguyên byte Rules.java, WebSource.java, AlertOptions.java. Chỉ đổi lịch lấy nguồn thành công thành **10 giây**; Nghi tự kích vẫn chờ **30 giây**, retry mạng **120 giây từ khi lỗi kết thúc**, stale **60 giây**, full refresh **15 phút**, lịch sử **30 ngày**. Không có luồng âm thanh liên tục.

Gửi `releases/DuongNgang-Web-1.1.0-source.zip` cùng SHA-256 cho AI Studio. Đọc **AI_STUDIO_HANDOFF.md**, **docs/INTEGRATION.md**, **docs/VALIDATION.md** và đặc tả đầy đủ trong ZIP trước. Thông báo, chạy nền iPhone và cấu hình máy chủ do AI Studio tích hợp/nghiệm thu. Gói có backend và PWA chạy cục bộ; build mã nguồn không đồng nghĩa đã triển khai HTTPS hay đã thử iPhone/website thật.

## Chạy và kiểm thử

Cần Python 3.12+, JDK 21 (`java` và `javac`), Node 20+, curl.

```sh
python3 -m venv .venv
.venv/bin/pip install -r requirements.lock -r requirements-dev.txt
bash tests/test-core.sh
.venv/bin/python -m unittest discover -s tests -v
node tests/test-alarm.mjs
npm run build
.venv/bin/python tools/test-browser.py
.venv/bin/python server.py
```

Máy chủ cục bộ cổng 8080, SQLite trong `data/` (không đưa dữ liệu này vào Git/ZIP). Trong ZIP có `engine-core/` và `tests/core/`, không cần Android SDK/APK. Trong repository build đọc core gốc từ `../app/`. Kiểm thử trình duyệt dùng Chromium hệ thống hoặc `CHROMIUM_PATH`; `TEST_WEBKIT=1` để thử WebKit khi đã cài browser. Không có tài khoản/dữ liệu vận hành mặc định. Test dùng website HTTP fixture riêng, không tự nạp demo vào sản phẩm.

`npm run build` xuất shell ở dist; backend/Java/DB vẫn bắt buộc. `deploy/README.md` là cấu hình mẫu để AI Studio hoàn thiện theo hạ tầng của anh. Không đưa riêng dist lên host tĩnh rồi coi là đã theo dõi nền.
