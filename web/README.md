# Giám sát Đường ngang — web 1.2.0 bàn giao AI Studio

Đọc **README_START_HERE.md**, **PROMPT_AI_STUDIO.md**, **docs/LATEST_REQUIREMENTS_1.2.0.md** trước. Worker 10 giây, tự kích ≥10 giây chỉ sau đọc nguồn thành công, retry mạng 50 giây từ kết thúc lỗi; bỏ dẫn xuất lệch 1–2 riêng từng CB. Core web tách riêng engine-core; core Android gốc và mã UI/service/store trong reference/android chỉ đối chiếu, không sửa APK.

## Chạy và kiểm thử

Cần Python 3.12+, JDK 21 (`java` và `javac`), Node 20+, curl.

```sh
python3 -m venv .venv
.venv/bin/pip install -r requirements.lock -r requirements-dev.txt
bash tests/test-core.sh
bash tests/test-web-core.sh
.venv/bin/python -m unittest discover -s tests -v
node tests/test-alarm.mjs
npm run build
.venv/bin/python tools/test-browser.py
.venv/bin/python server.py
```

Máy chủ cục bộ cổng 8080, SQLite trong `data/` (không đưa dữ liệu này vào Git/ZIP). Trong ZIP có `engine-core/` và `tests/core/`, không cần Android SDK/APK. Trong repository core gốc lấy từ `../app/` chỉ cho tests tham chiếu; ứng dụng dùng `engine-core/` riêng. Kiểm thử trình duyệt dùng Chromium hệ thống hoặc `CHROMIUM_PATH`; `TEST_WEBKIT=1` để thử WebKit khi đã cài browser. Không có tài khoản/dữ liệu vận hành mặc định. Test dùng website HTTP fixture riêng, không tự nạp demo vào sản phẩm.

`npm run build` xuất shell ở dist; backend/Java/DB vẫn bắt buộc. `deploy/README.md` là cấu hình mẫu để AI Studio hoàn thiện theo hạ tầng của anh. Không đưa riêng dist lên host tĩnh rồi coi là đã theo dõi nền.
