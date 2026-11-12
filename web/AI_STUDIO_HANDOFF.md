# Lệnh bàn giao cho Google AI Studio — đọc trước khi sửa

Bạn nhận gói **DuongNgang-Web-1.0.0-source.zip**. Chủ sở hữu đã duyệt triển khai bản web riêng. Hãy mở toàn bộ dự án, chạy backend hiện có và triển khai Dockerfile lên Google Cloud Run; sau đó trả URL HTTPS hoạt động. **Không yêu cầu gửi lại app Android để suy đoán giao diện. Không thiết kế lại, không tạo demo thay thế.**

## Phải giữ nguyên

1. Giao diện `public/index.html`, `app.css`, `app.js`: tông tối, Tổng quan/Đường ngang/Sự cố/Cài đặt. Tóm tắt Có sự cố nhỏ gọn có **XÁC NHẬN**. Ô kết nối nhỏ nằm **giữa tóm tắt và sơ đồ**; bình thường hiện trạng thái/tải thực tế, mất kết nối mới có nút đỏ **Kết nối lại**, dưới nút ghi **Mất kết nối**. Không thêm ô phía dưới.
2. XÁC NHẬN dừng âm của lượt hiện tại, giữ lỗi, chế độ và theo dõi; lượt mới vẫn báo. Không phát lại lỗi cũ khi mở lại. Ba nhãn đúng: **Giọng nói**, **Chỉ chuông**, **Im lặng**.
3. Giữ Python server và Java Engine. `engine-core/Rules.java`, `WebSource.java`, `AlertOptions.java` là bản nguyên gốc lấy từ Android 0.27.3, có checksum. Không viết lại quy tắc bằng suy đoán/AI: mất lỗi gốc, gộp nhầm thiết bị hoặc xóa nhầm tàu đang chạy đều không đạt.
4. Toàn tuyến tài khoản, Ga NL bổ sung chỉ Chosy; tàu xanh/ga vàng; 30 ngày Việt Nam; checkpoint trước xóa raw; sáu điểm loại trừ đếm trục. Backend 30 giây thành công, lỗi mạng chờ 120 giây kể từ kết thúc lần lỗi; retry tay không tạo hai lần đọc song song.
5. Không nhúng tài khoản/mật khẩu/khóa riêng vào frontend, Git, log hay ZIP. Không sửa Android hoặc ký lại APK. Không dùng localStorage làm kho mật khẩu. Không dùng cache Service Worker chứa `/api/`.

## Triển khai cụ thể

- Dùng [deploy/README.md](deploy/README.md), dựng Docker từ thư mục giải nén chứa Dockerfile. Không chỉ chạy `npm run build` rồi host `dist/`.
- Tạo Cloud SQL PostgreSQL riêng, Secret Manager cho `APP_KEY`, `VAPID_PRIVATE_KEY`, `DATABASE_URL`, service account chỉ đọc các secret cần thiết và Cloud SQL Client.
- Cloud Run: CPU always allocated (`--no-cpu-throttling`), min/max instance 1, timeout 3600, bộ nhớ 2 GiB, concurrency 40, HTTPS đúng `PUBLIC_ORIGIN`. Không tạo loop trong trình duyệt để giả lập worker.
- Xác minh truy cập website từ backend. Nếu lỗi DNS/TCP/7447, kiểm tra nguồn mạng/log trạng thái đã làm sạch; **không bảo chủ sở hữu cấu hình lại tài khoản khi chưa xác định nguyên nhân**.
- Không tự thêm `/reset` xóa cookie hoặc thay khóa VAPID khi cập nhật. Giữ hostname, DB và secret qua các revision để không mất tài khoản/đăng ký Push.

## Nghiệm thu trước khi nói “xong”

Chạy [docs/ACCEPTANCE.md](docs/ACCEPTANCE.md). Cung cấp URL HTTPS, ảnh giao diện, kết quả kiểm thử, revision triển khai, kết quả kiểm tra dữ liệu thật và các mục iPhone chưa thử. Gửi thông báo thử bằng cơ chế Web Push thật; kiểm tra khi PWA bị đóng và khóa màn hình.

Đối với iPhone, không hứa “100% chạy ngầm đọc giọng/rung như Android”. Web Push cần iOS 16.4+, cài PWA và quyền. Giọng đọc khóa máy là **phiên phát MP3 người dùng chủ động bật**, không phải TTS trong Service Worker; request tối đa 55 phút, có thể bị iOS/ngắt mạng dừng. Phải thử thật trên iPhone trước khi xác nhận. Không dùng audio im lặng làm bằng chứng rằng timer JavaScript chạy nền.

Nếu cần sửa để phù hợp môi trường triển khai, chỉ sửa cấu hình/adapter có lỗi đã chứng minh, ghi diff cụ thể và chạy lại kiểm thử; giữ nguyên giao diện và hành vi đã bàn giao.
