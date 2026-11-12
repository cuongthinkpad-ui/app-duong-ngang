# Giám sát Đường ngang — Web iPhone 1.0.0

Bản web độc lập từ Android 0.27.3 (`ee1affe5`). Không sửa Android/APK. Giữ giao diện tối, bốn tab, quy tắc gốc và ô kết nối nhỏ giữa tóm tắt sự cố và sơ đồ.

**Gửi toàn bộ ZIP cho AI Studio, kèm [AI_STUDIO_HANDOFF.md](AI_STUDIO_HANDOFF.md).** Đây là ứng dụng máy chủ + PWA, không phải bản HTML tĩnh. Dùng Dockerfile làm entrypoint Cloud Run. `npm run build` chỉ kiểm tra/đóng gói giao diện; đưa riêng `dist/` lên mạng sẽ không theo dõi được.

## Chạy tại máy phát triển

Cần Python 3.12+, Java/Javac 21, curl. ffmpeg và espeak-ng nếu dùng phiên nghe trực tiếp. Trong ZIP đã có `engine-core/`; trong kho Android, script dùng ba file Java gốc.

```sh
python3 -m venv .venv
.venv/bin/pip install -r requirements.lock
bash build-engine.sh
.venv/bin/python server.py
```

Mở http://127.0.0.1:8080. SQLite chỉ dùng phát triển cục bộ. Nhập tài khoản website hợp lệ trong Cài đặt; không có mật khẩu mặc định hoặc dữ liệu giả trong sản phẩm.

## Triển khai thật

Đọc [deploy/README.md](deploy/README.md). Bắt buộc HTTPS, Cloud SQL PostgreSQL, khóa mã hóa và VAPID ổn định trong Secret Manager, Cloud Run CPU luôn cấp và ít nhất một instance. Máy chủ cần truy cập `visitec.vn:7447`; kiểm tra từ chính Cloud Run trước khi quy lỗi cho tài khoản. Không dùng SQLite trên đĩa tạm Cloud Run.

## iPhone

Safari iOS/iPadOS 16.4 trở lên → Chia sẻ → Thêm vào Màn hình chính → mở từ biểu tượng → lưu tài khoản → Bật thông báo. Việc đọc website chạy ở máy chủ 30 giây/lần; lỗi mạng thử lại sau 120 giây hoặc nút Kết nối lại. Mở lại web không đọc lại lỗi cũ.

- Khi đang mở: giọng Việt thiết bị, hai tiếng chuông rồi sáu lần đọc mỗi cảnh báo; chế độ Chỉ chuông/Im lặng giữ đúng ý nghĩa. Web không điều khiển được rung iPhone qua Vibration API.
- XÁC NHẬN nằm trong giao diện web; không giả lập nút hành động Android trên thông báo iOS.
- Khi khóa máy: Web Push là kênh độc lập, âm/rung do iOS và quyền thông báo quản lý. Push không thể tự chạy TTS tùy ý hoặc tự bật sáng màn hình theo lệnh JavaScript.
- Có phiên **Bắt đầu nghe cảnh báo nền** do người dùng bấm phát: MP3 trực tiếp từ máy chủ, chuông/giọng Việt máy chủ. Đây là tính năng cần nghiệm thu trên iPhone thật. iOS, đổi mạng, cuộc gọi và giới hạn request Cloud Run có thể ngắt; một phiên tối đa 55 phút, mở web và bấm phát lại khi ngắt.

Chỉ “Thêm vào Màn hình chính” không cho JavaScript chạy timer 15/30 giây mãi khi khóa máy. Không được tuyên bố đã thử iPhone thật nếu chỉ chạy trình duyệt giả lập. Xem [docs/ACCEPTANCE.md](docs/ACCEPTANCE.md) và [docs/VALIDATION.md](docs/VALIDATION.md).

## Kiểm thử lại

Với Java 21 trên PATH, Python dùng virtualenv đã cài dependencies:

```sh
bash tests/test-core.sh
.venv/bin/python -m unittest discover -s tests -v
node tests/test-alarm.mjs
npm run build
```

Chạy cùng bộ backend trên PostgreSQL bằng biến DATABASE_URL của database **kiểm thử riêng**, không dùng DB vận hành. Bài kiểm thử tạo dữ liệu fixture, không xóa dữ liệu vận hành nhưng không nên trộn namespace.

Kiểm thử trình duyệt: cài `requirements-dev.txt`, cài Chromium của Playwright (hoặc đặt CHROMIUM_PATH tới Chromium hệ thống), chạy `tools/test-browser.py`. `TEST_WEBKIT=1` bật thêm WebKit khi đã cài được browser. Kiểm tra media thực trong Docker: chuyển nội dung `tests/test_audio_stream.py` qua stdin của `docker run --rm -i IMAGE python -`.

Ảnh trong `docs/screenshots/` dùng dữ liệu fixture, không phải chụp tài khoản vận hành. Chưa có dữ liệu demo/fixture tự nạp trong UI sản phẩm.
