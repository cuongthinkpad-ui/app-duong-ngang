# Đường Ngang 0.26 — đối chiếu khu đoạn, kết thúc lượt treo, chu kỳ cố định và rung

Yêu cầu chủ dự án đã duyệt, triển khai từ HEAD `111f8db` (0.25). Chủ dự án xác nhận giọng đọc cảnh báo để bản sau.

## Hành vi

- Đối chiếu lượt tàu cùng hướng trong cùng khu đoạn bị chặn bởi ga, cho phép thiếu báo cáo ở một hoặc nhiều đường ngang giữa. Bằng chứng có hướng ở xa phải đi đúng trình tự thời gian; hoạt động cần chắn không có hướng chỉ giữ vai trò dự phòng ở điểm liền kề. Một nguồn chỉ được ghép một lần; khi bắc qua điểm thiếu, ghép nguồn theo thứ tự và giữ nguồn nối tiếp, kể cả nguồn nối tiếp đã xuất hiện trước khi tàu trước tới đích. Lượt mới phía sau không dùng lại nguồn cũ.
- Bản tin tàu ở phía sau theo hướng chạy, kể cả chỉ còn bản tin đã qua, có thể bác bỏ suy luận của đúng lượt phía trước. Khi có đủ chuỗi, xóa riêng bản tin `self:<id>` và thông báo liên quan; giữ bản tin website, lỗi thiết bị, hướng ngược và các khu đoạn khác. Sơ đồ nhận lượt tại điểm đã có báo cáo, không sinh bản tin giả ở điểm thiếu.
- Ấn nút Phục hồi, Ấn nút Duy tu, Ấn nút cảnh báo tại chính đường ngang kết thúc dấu lượt hiện tại ở đó, không kết thúc lượt ở điểm khác hay lượt độc lập đang đi phía trước. Ưu tiên đúng lượt có bằng chứng tự kích/lỗi tàu khi cùng điểm đã có tàu khác. Khôi phục hệ thống kết thúc lượt đang chờ chưa có bản tin đã qua, lượt đã đánh dấu tự kích hoặc có lỗi đỏ liên quan đến tàu; không suy ra đoạn đi tiếp chỉ từ khôi phục. Lượt đã qua bình thường vẫn giữ liên tục tới điểm sau như trước. Bản tin đã qua đến muộn không làm sáng lại lượt đã kết thúc.
- Không xóa lỗi đỏ website bằng thao tác nút/khôi phục hệ thống. Lịch sử giữ bản tin nguyên văn; các lệnh trên chỉ khép lỗi suy luận tương ứng.
- Bỏ tất cả lựa chọn 30 giây/2 phút/5 phút, luôn đọc mỗi 30 giây. Khi mở, cấu hình cũ chuyển về 30 giây; dịch vụ và giao diện dùng hằng số 30, không phụ thuộc giá trị cũ còn trong cache.
- Sáu hồi chuông trong 12 giây, âm có biên độ và thời gian ngân lớn hơn, không clipping; kèm sáu nhịp rung một lần. Dùng âm lượng Báo thức hiện có, không gọi tăng âm lượng hệ thống. Tắt cảnh báo chung, mất audio focus, phát xong hoặc dừng dịch vụ đều dừng player/rung. Lịch sử không phát cảnh báo.
- `travelVersion=6`, `selfEvidenceVersion=5`; phát lại retained raw và giữ checkpoint có nguồn đã hết hạn lưu trữ. Không đổi `axleVersion=3`, cửa sổ 30 ngày hoặc sáu điểm loại trừ trục.

## Kiểm chứng

- 737 ca quy tắc Java đạt: hai hướng, thiếu nhiều điểm giữa, giữ nguồn tàu nối tiếp theo thứ tự, nguồn chỉ dùng một lần, không qua ga, các lệnh tại đúng điểm và không hồi sinh lượt đã kết thúc.
- 12 parser và 39 HTTP fixture đạt.
- 193 ca lưu trữ Android đạt, gồm chuyển cả 120/300 giây về 30, trạng thái tàu bị lỗi qua checkpoint/khởi động lại và khôi phục không xóa bản tin gốc.
- 24 ca giao diện lịch sử/chuông/rung đạt ở màn hình giả lập 1080×1920. Xác nhận bỏ lựa chọn chu kỳ, phát âm 12 giây, quản lý dịch vụ rung, dừng/giải phóng và không đổi âm lượng hệ thống.
- 69 ca UI cập nhật đạt ở màn hình giả lập 1080×2800 trước bổ sung chọn đúng lượt bị lỗi khi có tàu khác; phần bổ sung có hồi quy Java và lưu trữ Android riêng. Bộ test cũ được cập nhật cho snapshot nền từ 0.25, số dòng Tổng quan thích ứng chiều cao và ngày lịch sử chưa tải đủ không được khẳng định không có lỗi. Kiểm tra thứ tự toàn bộ lỗi vẫn nằm ở trang Sự cố.
- Build ứng dụng/AndroidTest và lint đạt: 0 lỗi, 15 cảnh báo. applicationId `vn.duongngang.monitor`, versionName `0.26.0`, versionCode `26`.

- Dịch vụ với HTTP fixture cục bộ `--nontrain-only` đạt trên APK cuối: một chuỗi thao tác tại đích không loại bỏ nguồn tàu hợp lệ; nguồn chỉ dùng một lần; lượt sau thiếu bằng chứng vẫn có suy luận và phát chuông. Chờ dấu phát chuông hoàn tất vì việc lưu sự cố xảy ra trước khi MediaPlayer chuẩn bị xong, đặc biệt trên giả lập phần mềm.
- Đã cài APK 0.25 ký gốc rồi cài đè APK 0.26: giữ tệp private và cài đặt tắt cảnh báo, chuyển 300 giây về 30 giây. Không gỡ app hay xóa dữ liệu trong ca nâng 0.25 → 0.26. Trước khi dựng baseline ban đầu đã gỡ bản test ký khóa mới do onboarding tạo; đó không phải bản 0.25 ký gốc đang dùng.
- `apksigner` xác minh APK cuối và chứng chỉ gốc trùng 0.25. Hash APK trên giả lập sau cài đè trùng tệp phát hành; sau kiểm thử dịch vụ, tệp private/cài đặt vẫn được bảo toàn.
- APK: `downloads/DuongNgang-0.26.apk`. SHA-256: `c13ab4b2cb2c8311fffbfdd0b84a72fc5df8195a7ab877bfcc16ae7b0742cb70`.

Các bộ kiểm tra dùng dữ liệu tổng hợp, không thay thế đối chiếu website và Xiaomi thật. Không tuyên bố âm lượng loa/rung hoặc độ trễ trên Xiaomi đã được xác minh.

## Chạy lại

```sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh --offline assembleDebug assembleDebugAndroidTest lintDebug
# Giả lập riêng đã boot; chọn ANDROID_SERIAL phù hợp:
./scripts/test-android-route.sh
./scripts/test-android-history.sh
# Bộ UI cũ giả định màn hình cao; chỉ thay kích thước trên giả lập riêng:
adb shell wm size 1080x2800
./scripts/test-android-ui.sh
adb shell wm size reset
python3 scripts/test-monitoring-e2e.py --nontrain-only
```

Khóa ký được khôi phục ngoài Git từ hai tệp chủ dự án gửi trong phiên này. Không đưa tệp khóa, thông tin mở khóa hay bản sao mã hóa lên GitHub.
