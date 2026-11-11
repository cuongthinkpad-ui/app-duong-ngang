# Đường Ngang 0.27 — lựa chọn giọng đọc, chuông và im lặng

Bản kiểm thử được chuẩn bị từ `48bdc1d` (0.26), trên nhánh `codex/0.27-voice-alerts`. Chưa thay bản 0.26 chính thức trên main: yêu cầu đối chiếu tên lỗi trên website thật còn bị chặn bởi môi trường.

## Chế độ cảnh báo

- **Giọng nói:** hai hồi chuông trong 4 giây, sau đó đọc từng cảnh báo mới đủ sáu lượt bằng tiếng Việt; rung trong lúc báo. Nhiều cảnh báo đọc theo thứ tự, mỗi nội dung đủ sáu lượt; cảnh báo đến sau được xếp hàng, không cắt mất cảnh báo đang đọc. Không lặp lại chỉ vì mở Activity hoặc có poll rỗng.
- **Chỉ chuông:** sáu hồi/12 giây và rung như 0.26, không khởi tạo giọng đọc.
- **Im lặng:** chỉ hiển thị lỗi/thông báo; không phát chuông, giọng đọc hay rung. Tắt âm thanh chung dừng ngay chuỗi đang phát và nhớ chế độ có âm trước đó để bật lại. Cấu hình tắt âm cũ vẫn được giữ khi nâng cấp.
- Lịch sử không phát cảnh báo. Khôi phục một lỗi chỉ loại lỗi đó khỏi hàng đọc, giữ cảnh báo khác; dừng dịch vụ, đổi tài khoản/ngày, mất audio focus và reset ngày đều hủy phần không còn hợp lệ. Kiểm tra lại trạng thái sống trước khi bắt đầu đọc để callback đến muộn không phát cảnh báo cũ.
- Dùng âm lượng Báo thức, không ép âm lượng hệ thống. Kênh thông báo mới không tự phát âm/rung để tránh cộng thêm tiếng thông báo vào hai hồi chuông.
- Dùng Text-to-Speech của Android, ưu tiên giọng Việt offline đã cài. Nếu chưa có giọng Việt, vẫn phát phần chuông rồi dừng rung, báo rõ trong Cài đặt và có nút Cài đặt giọng đọc; không lặng lẽ dùng tiếng Anh hoặc tuyên bố đã đọc đủ. Không gói bộ giọng thử của giả lập vào APK người dùng.
- Chữ sự kiện hiển thị/lưu giữ nguyên. Khi đọc, CB3 được nói rõ là cảm biến 3, `>`/`<` thành lớn hơn/nhỏ hơn và V/A thành vôn/am pe; số thiết bị, điện áp và lý trình được giữ.

## Hai cặp lỗi nguồn theo tên chủ dự án cung cấp

- `Cầu chì 50A bị đứt` được nhận là lỗi dù website không tô đỏ. `Cầu chì 50A khôi phục tốt` khôi phục đúng cầu chì 50A tại cùng điểm.
- `nguồn DC > 29v` được ghép với `Nguồn 24V khôi phục tốt` tại cùng điểm. Không xóa nguồn 220VAC, điện áp cảm biến, cầu chì hay lỗi ở điểm khác.
- Chuẩn hóa chữ hoa/thường và khoảng trắng để đối chiếu nội bộ; không đổi nguyên văn báo cáo. Hai nhóm thuộc Điện áp trong chi tiết.
- `faultRulesVersion=1` dựng lại cache lỗi hiện tại từ raw đã lưu, kể cả khi mất mạng. Dữ liệu cũ của cầu chì vốn lưu không đỏ được phân loại đúng khi đọc. Truy vấn khôi phục sau khoảng lịch sử phân loại các bản tin khôi phục đã xác nhận trước khi lọc màu cũ. Giữ raw, ID và lịch sử; không đổi travelVersion=6, selfEvidenceVersion=5, axleVersion=3 hay retention 30 ngày.

## Kiểm chứng

- 772 ca quy tắc, 17 ca lựa chọn/chuỗi đọc, 16 parser và 39 HTTP fixture đạt.
- 200 ca lưu trữ Android đạt: gồm cầu chì cũ không đỏ, cache DC còn treo dù đã có khôi phục, khôi phục sau khoảng lịch sử dù màu cũ sai và migration lặp không đổi dữ liệu.
- 20 ca giọng Việt đạt với bộ giọng eSpeak NG 1.52.0 thật trên giả lập API 29: đọc sau hai hồi, đúng sáu lượt/cảnh báo, xếp hàng, không đọc trùng ID đang xếp, tắt giữa chuông/giọng không tự phát lại, khôi phục đúng cảnh báo, giữ cảnh báo còn lại và chọn được ba chế độ bằng nút giao diện thật. Bộ 20 ca được chạy trước các chỉnh sửa bảo vệ callback theo trạng thái sống và nhãn trạng thái dịch vụ; các chỉnh sửa sau được kiểm tra qua ca dịch vụ và thiếu bộ giọng.
- 4 ca thiếu bộ giọng đạt khi tạm vô hiệu hóa bộ giọng thử rồi khôi phục: không giả báo đã đọc, thông báo rõ, chuông hoàn tất, player/rung được giải phóng.
- 24 ca giao diện/lịch sử đạt, cùng 5 ca dịch vụ `--audio-modes-only`: giọng đọc từ cảnh báo thật của service, nhận cầu chì không đỏ, khôi phục cả hai nhóm nguồn, chỉ chuông, im lặng vẫn có thông báo và lịch sử không phát âm.
- Build ứng dụng/AndroidTest và lint đạt: 0 lỗi, 15 cảnh báo. Ký chứng chỉ gốc `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.
- Cài đè 0.26 → 0.27 trên giả lập giữ tệp private, cài đặt tắt âm và chu kỳ 30 giây; không gỡ app/xóa dữ liệu trong ca nâng cấp.
- APK `downloads/DuongNgang-0.27.apk`, applicationId `vn.duongngang.monitor`, versionName `0.27.0`, versionCode 27.
- SHA-256: `27a59300ea6737e8cf128f24096514961149aff07ec601c623095c603a91c7c5`.

Bộ giọng thử là APK ký từ bản phát hành chính thức https://github.com/espeak-ng/espeak-ng/releases/tag/1.52.0, lưu ngoài repo tại `/workspace/voice-test-tools`; đã kiểm tra APK signature và chọn đúng locale vi/vie. Đây là kiểm thử giao tiếp với TTS thật, không xác nhận chất giọng/loa/rung hoặc HyperOS trên Xiaomi thật.

## Đối chiếu web thật còn chờ

Nguồn tên lỗi hiện tại là chuỗi do chủ dự án cung cấp, kiểm thử bằng fixture; chưa xác nhận nguyên văn từ báo cáo thật. Truy cập `http://visitec.vn:7447/` nhận HTTP 403 `Domain forbidden` từ proxy. Runtime vẫn dùng chính sách chưa có miền này mặc dù bản nháp đã thêm `visitec.vn`. Cần lưu/Publish cấu hình để áp dụng quyền mạng rồi thử lại bằng kết nối mới.

Thông tin truy cập đã được cung cấp và lưu riêng ngoài Git, không yêu cầu gửi lại: `/workspace/private-audit/website-access.json`. Công cụ đọc kiểm tra và tệp truy cập được bảo vệ trong cùng thư mục; không in/copy thông tin truy cập hoặc báo cáo xác thực vào Git. Khi mạng cho phép, đọc báo cáo thật bằng WebSource và đối chiếu hai cặp tên trước khi đưa bản này thành bản chính thức trên main. Website chưa tới bước đăng nhập, nên chưa có kết luận tài khoản đúng/sai.

## Chạy lại

```sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-alert-options.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh --offline assembleDebug assembleDebugAndroidTest lintDebug
# Giả lập riêng đã boot, có giọng Việt mặc định:
./scripts/test-android-voice.sh
./scripts/test-android-voice-missing.sh
./scripts/test-android-route.sh
./scripts/test-android-history.sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
python3 scripts/test-monitoring-e2e.py --audio-modes-only
```
