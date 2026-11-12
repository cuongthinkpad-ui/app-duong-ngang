# Bàn giao phiên 11/11/2026 — tiếp tục bằng Google AI Studio hoặc công cụ khác

> Mới nhất: [0.27.3](UI_REVIEW_0.27.3.md), versionCode 30, thêm thử lại 2 phút khi lỗi mạng. Ô nhỏ giữa **CÓ SỰ CỐ và sơ đồ** hiện trạng thái/tiến trình tải; chỉ mất kết nối mới hiện nút đỏ **Kết nối lại**, cảnh báo **Mất kết nối**. Thu gọn phần trên để dành chỗ, không thêm mục phía dưới. Nhánh `codex/0.27-notification-stop`; giữ toàn bộ chức năng 0.27.2 bên dưới.

> Mới nhất: [0.27.2](UI_REVIEW_0.27.2.md) sửa mở lại app đọc lại lỗi cũ, đổi nút thông báo thành **XÁC NHẬN** và thêm nút cùng chức năng trong ô **CÓ SỰ CỐ** đầu Tổng quan; versionCode 29, cùng nhánh `codex/0.27-notification-stop`. Yêu cầu này thay phần phát lại lỗi hiện có khi bắt đầu phiên trong tài liệu cũ.

> Cập nhật 12/11: tiếp tục trên `codex/0.27-notification-stop`, bản 0.27.1/versionCode 28. Đã thêm nút dừng đợt chuông/giọng đọc/rung từ thông báo, giữ theo dõi/lỗi/chế độ âm; rút gọn ba nhãn lựa chọn theo yêu cầu. Đọc [biên bản và bàn giao 0.27.1](UI_REVIEW_0.27.1.md) trước phần lịch sử bên dưới.

## Lấy đúng mã nguồn

- Repository: https://github.com/cuongthinkpad-ui/app-duong-ngang
- Nhánh tiếp tục: `codex/0.27-voice-alerts`.
- Commit ứng dụng đã kiểm thử: `bb17427611206e969e4a21e34d4a4d1aaca5e583`.
- Main hiện là 0.26, commit `48bdc1d7f8e68c1763e99f3374df1873cf1d69b6`. Không dùng main nếu muốn tiếp tục phần giọng nói 0.27.
- APK thử: [DuongNgang-0.27.apk](../downloads/DuongNgang-0.27.apk); [kiểm chứng, checksum và giới hạn](UI_REVIEW_0.27.md).

Đây là ứng dụng **Android native Java/Gradle**, applicationId `vn.duongngang.monitor`, không phải dự án web. Việc nhập được repository không đồng nghĩa công cụ có thể chạy giả lập hoặc xuất APK. Nếu giao diện AI Studio đang dùng không hỗ trợ nhập repository/nhánh này, tải ZIP của nhánh hoặc dùng Git checkout trong môi trường phát triển có Android SDK. Không chuyển app thành web chỉ để có preview nếu chưa được chủ dự án yêu cầu.

```sh
git clone --branch codex/0.27-voice-alerts --single-branch https://github.com/cuongthinkpad-ui/app-duong-ngang.git
cd app-duong-ngang
```

## Trạng thái đã hoàn thành

0.26 đã sửa đối chiếu tàu cùng hướng qua điểm thiếu bản tin, giữ thứ tự và tàu nối tiếp; kết thúc đúng lượt treo bằng thao tác tại điểm; luôn 30 giây, không có lựa chọn chu kỳ; sáu hồi chuông và rung. Nguyên tắc chủ dự án chốt: **xóa đúng dấu tàu bị treo, không xóa nhầm tàu khác đang chạy**. Không tạo đoạn đi tiếp giả, không xóa lỗi thiết bị gốc hoặc lịch sử.

0.27 thêm ba chế độ:

- Giọng nói: hai hồi chuông rồi đọc từng cảnh báo mới sáu lượt, có rung.
- Chỉ chuông: sáu hồi chuông và rung.
- Im lặng: chỉ hiển thị/thông báo, không âm thanh hoặc rung.

Tắt âm chung dừng cả hàng đọc/chuông/rung; lịch sử không báo; không ép âm lượng hệ thống. Dùng TTS Android tiếng Việt, báo rõ nếu chưa có bộ giọng. Không tích hợp Gemini hoặc khóa API vào APK trong phiên này.

Hai cặp lỗi đã xử lý theo nguyên văn chủ dự án cung cấp:

- `Cầu chì 50A bị đứt` (kể cả không tô đỏ) → `Cầu chì 50A khôi phục tốt`.
- `nguồn DC > 29v` → `Nguồn 24V khôi phục tốt`.

Giữ nguyên văn/ID bản tin; khôi phục đúng điểm/thiết bị, giữ lỗi khác. Migration lỗi nguồn phiên bản 1 sửa cache từ raw; travelVersion=6, selfEvidenceVersion=5, axleVersion=3 và lưu 30 ngày giữ nguyên.

## Phần còn lại

1. Đối chiếu nguyên văn hai cặp lỗi từ website thật `http://visitec.vn:7447/`. Môi trường của phiên này vẫn chặn miền bằng HTTP 403 `Domain forbidden`, chưa đến bước đăng nhập. Không được ghi là đã kiểm tra web thật.
2. Cấu hình mạng đã có bản nháp thêm `visitec.vn`, cần áp dụng/Publish rồi thử kết nối mới. Cấu hình môi trường không tự đi theo GitHub.
3. Chủ dự án đã cung cấp thông tin truy cập và khóa ký riêng. Chúng không nằm trong Git. Môi trường hiện tại có chúng ngoài repository; môi trường AI Studio mới không tự có các tệp này. Chỉ chuyển qua kênh bí mật phù hợp, không đưa vào prompt công khai, mã nguồn, log hoặc commit.
4. Nếu tên thật khác, sửa đối chiếu tối thiểu, thêm kiểm thử tương ứng và cân nhắc nâng marker migration cho máy đã cài bản thử.
5. Kiểm tra trên điện thoại thật, nhất là bộ giọng Việt, âm lượng báo thức, rung và chạy nền. Các kết quả giả lập không xác nhận Xiaomi/HyperOS thật.
6. Sau khi xác minh, build, ký đúng khóa gốc, kiểm tra cài đè giữ dữ liệu, cập nhật APK/checksum/tài liệu rồi hoàn tất phát hành. Chưa đưa bản thử này thay main trong phiên bàn giao.

## Đọc và kiểm thử

Đọc theo thứ tự: `AGENTS.md`, tài liệu này, `docs/UI_REVIEW_0.27.md`, `docs/BAN_GIAO.md`, `docs/LICH_SU_TRAO_DOI.md`. Các tài liệu cũ có thể mô tả chu kỳ 2/5 phút hoặc hoãn giọng nói; yêu cầu 0.26/0.27 mới hơn thay thế chúng.

Các tệp chính: `Rules.java` (quy tắc), `Store.java` (cache/lưu trữ), `MonitorService.java` (đọc dữ liệu/cảnh báo), `VoiceAlarm.java` (chuông/TTS/hàng đợi), `AlertOptions.java` (chế độ/nội dung đọc), `MainActivity.java` (giao diện). Tất cả dưới `app/src/main/java/vn/duongngang/monitor/`.

Kiểm thử đã đạt và phạm vi chính xác được ghi trong UI_REVIEW_0.27.md: 772 quy tắc, 17 lựa chọn/chuỗi đọc, 16 parser, 39 HTTP fixture, 200 lưu trữ, 20 giọng Việt, 4 thiếu giọng, 24 UI/lịch sử và 5 dịch vụ. Build/lint đạt, ký khóa gốc và cài đè 0.26 → 0.27 giữ dữ liệu. Một số bộ chạy trước các chỉnh sửa callback/nhãn cuối; đọc biên bản trước khi khẳng định phạm vi.

Dùng hướng dẫn toolchain trong `scripts/env.sh`, `scripts/build-cloud.sh` và bàn giao. Các đường dẫn `/workspace/toolchains` là của môi trường cũ, không phải dependency đã đóng gói trong Git. Môi trường mới cần JDK 21, Android SDK phù hợp cấu hình Gradle, cùng các dependency của dự án. Không dùng khóa ký mới để phát hành bản cài đè.

## Prompt có thể dán cho AI Studio

> Hãy tiếp tục dự án Android native Java trong repository cuongthinkpad-ui/app-duong-ngang, nhánh codex/0.27-voice-alerts. Đọc AGENTS.md và docs/TIEP_TUC_0.27_AI_STUDIO.md, sau đó các tài liệu được liên kết trước khi sửa. Giữ toàn bộ quy tắc tàu, dữ liệu và giao diện hiện có; không dựng lại thành web. Bản 0.27 đã có ba chế độ cảnh báo và sửa lỗi nguồn, nhưng chưa đối chiếu tên lỗi trên website thật do mạng bị chặn. Trước tiên kiểm tra HEAD, nêu trạng thái hiện tại và khả năng build Android của môi trường; tiếp tục phần xác minh còn thiếu theo bàn giao. Không công bố đã chạy test/đọc web/ký khóa gốc nếu chưa thực hiện. Không đưa thông tin đăng nhập, khóa ký hoặc báo cáo xác thực vào Git.

Tài liệu này tóm tắt yêu cầu, quyết định, kết quả và phần còn lại của phiên; không phải bản xuất nguyên văn toàn bộ chat. Các tệp riêng, cài đặt môi trường và công cụ thử ngoài Git không nằm trong repository.
