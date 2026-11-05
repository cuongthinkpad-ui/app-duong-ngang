# Đường Ngang 0.25 — lịch sử đầy đủ, tình trạng thiết bị và độ mượt

Chủ dự án xác nhận sửa sáu mục và tối ưu tải/hiển thị trong phiên ngày 05/11/2026. Các ảnh báo lỗi cho thấy cùng đường ngang/ngày có cảnh báo CB3 ở Sự cố nhưng thiếu tại chi tiết Cảm biến, và nút Chi tiết mở nhầm hôm nay.

## Thay đổi

- Đọc ngày cũ vẫn dùng bộ đọc đầy đủ trang, kiểm tra đúng ngày/toàn tuyến và từ chối báo cáo thiếu/lặp trang. Tải ngày liền trước còn trong cửa sổ 30 ngày để có ngữ cảnh hướng tàu. Chỉ đánh dấu ngày hoàn tất sau khi lưu và xử lý xong; marker riêng theo ngày, xóa khi đổi tài khoản.
- Tính lại cảnh báo đếm trục của ngày đã tải và lưu cùng bản tin gốc. Snapshot hiển thị cũng dựng lại cảnh báo từ bản tin đo để máy mới và máy có cache ra cùng kết quả. Ô Cảm biến, thống kê đếm trục, danh sách và Sự cố dùng cùng quy tắc, giữ sáu điểm loại trừ đã chốt.
- Phát lại suy luận nghi tự kích từ bằng chứng lịch sử đầy đủ khi chọn chu kỳ 30 giây; không bật ở 2/5 phút. Không xác nhận sớm lượt còn nằm trong 30 giây cuối ngày. Không phát thông báo/chuông cho dữ liệu lịch sử. Trạng thái lịch sử được tính đến cuối ngày chọn, không dùng trạng thái hoặc mốc reset hôm nay.
- Các ô thiết bị chỉ liệt kê lỗi đang mở. Khi dữ liệu ngày đã tải đủ và không có lỗi thì ghi **Tốt**. Khi chưa đọc đủ hiển thị **Chưa tải đủ dữ liệu**. Bản tin nâng/hạ cần bình thường vẫn nằm trong lịch sử/lượt tàu, không thay ô Cần chắn.
- Điện áp cảm biến thuộc Cảm biến, còn nguồn/điện áp chung thuộc Điện áp; giữ nguyên văn bản tin, số thiết bị và cách ghép khôi phục.
- Giữ ngày xuyên suốt từ danh sách hoặc Sự cố sang chi tiết đường ngang rồi Chi tiết lượt tàu. Tiêu đề lịch sử/ngày được chọn không lấy ngày hiện tại thay thế.
- Chỉ chu kỳ 30 giây hiển thị đoạn tàu/mũi tên. Chu kỳ 2/5 phút giữ khung tuyến, bỏ trạng thái tàu và giải thích yêu cầu 30 giây; đọc báo cáo, lỗi thiết bị và lịch sử vẫn hoạt động.
- SQLite, tạo cảnh báo ngày và phát lại sơ đồ lịch sử chạy trên executor dữ liệu riêng. Cache snapshot tối đa bốn ngày, khóa theo tài khoản/ngày/phiên bản dữ liệu; giữ dữ liệu đúng ngày đã có trong lúc tải mới. Chống tải tay lặp và bỏ yêu cầu đã lỗi thời trước khi bắt đầu đọc. Worker phiên theo dõi giữ nguồn web riêng để đổi ngày không trộn nguồn.
- Danh sách dài dựng 20 nhóm lượt/sự kiện hoặc 40 sự cố trước; nút Hiển thị thêm có số lượng cho phép mở tiếp toàn bộ. Việc chia đợt dựng giao diện không giới hạn số bản tin tải/lưu/xử lý. Giữ vị trí cuộn khi cập nhật.

## Kiểm chứng

- 669 ca quy tắc Java đạt, gồm hồi quy máy mới/cache, ngưỡng trục, sáu điểm loại trừ, nhóm điện áp cảm biến và chu kỳ sơ đồ.
- 12 parser và 39 ca đọc HTTP fixture đạt. Fixture kiểm tra bộ đọc, không xác nhận dữ liệu website thực tế của tài khoản người dùng.
- Build APK ứng dụng/AndroidTest ký gốc và lint mã cuối đạt: **0 lỗi, 15 cảnh báo** (thêm một cảnh báo chuỗi giao diện tiếng Việt trực tiếp). `aapt` xác nhận applicationId/version 0.25.0/code 25; `apksigner` xác nhận chứng chỉ trùng 0.24.
- Đã cài 0.24 rồi nâng lên APK 0.25 ký gốc trên giả lập: tệp private và cài đặt âm/chu kỳ tạo trước nâng cấp được giữ nguyên. Không gỡ app hoặc xóa dữ liệu để vượt qua kiểm tra cài đè.
- Đã bổ sung mười kiểm tra lưu trữ lịch sử và bộ HistoryUiChecks cho ô Cảm biến, Tốt, lịch sử bản tin cần chắn, giữ ngày và bật/tắt sơ đồ. **189 ca lưu trữ Android đã đạt** trên giả lập API 29; **18 ca giao diện lịch sử đã đạt**, gồm giữ ngày qua nút Chi tiết, nhóm Cảm biến/Tốt, ô Cần chắn, bật/tắt sơ đồ và mở đủ 25 nhóm sự kiện qua Hiển thị thêm.

## Mẫu đo chuyển tab

Cùng giả lập phần mềm API 29, cùng 1.000 sự kiện/11 đường ngang, tắt animation hệ thống và chờ đọc đủ dữ liệu trước đo. Đây là **một mẫu thời gian callback dựng giao diện**, không phải thời gian tải web hay FPS trên điện thoại:

| Màn hình | 0.24 | 0.25 |
|---|---:|---:|
| Danh sách đường ngang | 8.331 ms | 3.729 ms |
| Sự cố | 10.627 ms | 3.487 ms |
| Cài đặt | 5.992 ms | 3.983 ms |
| Tổng quan | 1.596 ms | 1.994 ms |

Hai danh sách giảm thời gian trong mẫu này. Tổng quan không cải thiện ở mẫu đơn này; không tuyên bố mọi màn hình nhanh hơn hoặc dùng số đo TCG để suy ra hiệu năng Xiaomi. Bộ đo kiểm tra đủ 1.000 sự kiện trước khi tính thời gian, tránh đo màn hình placeholder.

## Phát hành và giới hạn

- applicationId giữ `vn.duongngang.monitor`, versionName `0.25.0`, versionCode `25`.
- Đã khôi phục khóa từ hai tệp chủ dự án cung cấp, giữ ngoài Git. APK 0.25 ký đúng fingerprint `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.
- Chưa kiểm chứng website thật bằng tài khoản người dùng và chưa kiểm chứng Xiaomi/HyperOS. Không coi build hay fixture là kiểm thử điện thoại thật.
- Website phải còn báo cáo của ngày yêu cầu; không thể phục hồi bản tin website đã xóa. Lỗi tải phải được báo rõ thay vì khẳng định không có sự cố.

## Chạy lại

```sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh --offline assembleDebug assembleDebugAndroidTest lintDebug
# Giả lập riêng đã boot; chọn đúng serial, không dùng dữ liệu người dùng thật:
ANDROID_SERIAL=emulator-5556 ./scripts/test-android-route.sh
ANDROID_SERIAL=emulator-5556 ./scripts/test-android-history.sh
```

Giả lập kiểm tra trong phiên được tạo riêng ngoài Git bằng SDK manager, image Android 10/API 29 x86_64 và chạy `-no-window -no-audio -no-boot-anim -no-snapshot -accel off -gpu swiftshader -memory 1536 -cores 2`. Đặt `ANDROID_AVD_HOME=$ANDROID_USER_HOME/avd` khi khởi động. Không dùng số đo giả lập phần mềm để khẳng định tốc độ hay chạy nền trên Xiaomi thật.


APK phát hành: `downloads/DuongNgang-0.25.apk`. SHA-256: `48d18bc7abf2da9a0f0808290851c05eb155a3bd37fba65395756878b4f53ad4`.
