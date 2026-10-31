# Không dùng thao tác thử làm mốc lượt tàu — 0.22

Ngày 31/10/2026, giờ Việt Nam. APK 0.22.0, versionCode 22; giữ applicationId/chữ ký cũ.

## Yêu cầu được xác nhận

Chủ dự án gửi ảnh báo nhầm và yêu cầu phân tích trước. Phát lại riêng các mốc trong ảnh tái hiện lỗi: nguồn phía trước đã báo tàu và khôi phục, trong khi điểm tiếp theo có thao tác nút bấm/hạ-nâng cần/khôi phục trước khi tàu thật tới. Logic 0.21 dùng một chu kỳ hoạt động không có hướng làm mốc trước đó, loại mất nguồn tàu chưa được dùng.

Sau trao đổi, chủ dự án chốt: một thao tác hay cả loạt đều không tự được xem là một đoàn tàu, không chia lượt thực hoặc loại bằng chứng tàu đang đi tới. Chỉ sửa vấn đề này; **giữ nguyên cách xử lý trường hợp mất kết nối** theo yêu cầu riêng của chủ dự án. Chủ dự án đã yêu cầu sửa và test. Không truy cập lại web trong lượt sửa; ảnh và công cụ phát lại vận hành thật ở ngoài Git.

## Thực hiện

- Tách lịch sử lượt có bản tin tàu rõ hướng khỏi lịch sử hoạt động thiết bị. Mốc trước của một lượt được lấy từ bản tin tiếp cận/đã qua có hướng, không từ lúc nút bấm/cần chắn tự hoạt động. Chỉ thao tác riêng lẻ hoặc cả loạt không tiến mốc lượt thật.
- Bằng chứng tiếp cận/đã qua ở phía trước chưa dùng được giữ qua các thao tác ở điểm đang xét, kể cả nguồn đã khôi phục. Giữ ghép một-một, đúng hướng, ranh giới ga/tuyến, nguồn từ trước lượt thật đã đi qua không được dùng lại, và chờ một chu kỳ ở chế độ 30 giây. Không xóa suy luận chỉ vì chính điểm đang xét hạ/nâng cần.
- Giữ quy tắc bằng chứng bổ sung từ cần chắn ở phía trước khi thiếu bản tin tàu, theo yêu cầu trước đó. Đây là dữ liệu đối chiếu có điều kiện, không được nâng thành một đoàn tàu có hướng hoặc tự sinh thêm bản tin tàu. Giữ ràng buộc cũ thận trọng cho nguồn chỉ có cần chắn; chỉ lượt có bản tin tàu rõ ràng mới được bảo toàn như lượt thực.
- `selfEvidenceVersion=4` đánh giá lại suy luận đã lưu khi nâng cấp. Bỏ suy luận hết căn cứ, hủy thông báo tương ứng và cập nhật phân loại sơ đồ; giữ lỗi thiết bị thật, raw và các suy luận vẫn thiếu bằng chứng. Không phát lại chuông lịch sử.
- Khi cần phát lại phần trước checkpoint, giữ riêng các lượt có nguồn đã hết hạn 15 ngày rồi phát lại phần raw còn lưu. Không xóa seed và dựng lại từ một kho raw đã bị dọn mất phần cũ. Giữ `travelVersion=4`, `axleVersion=3`, nguyên tắc lưu 15 ngày, khung lịch sử, chuông sáu hồi và giao diện.
- Không thêm ngoại lệ suy luận khi mất kết nối; ca đối chiếu riêng về mất kết nối cho kết quả không đổi. Không điều khiển thiết bị hay suy đoán danh tính đoàn tàu.

## Kiểm chứng

- **611 kiểm tra quy tắc** đạt. Ca mới thất bại trên 0.21 trước sửa: nguồn đã khôi phục, từng thao tác riêng và cả loạt, hai hướng, đọc đảo thứ tự, mỗi nguồn chỉ hỗ trợ một lượt thật, không có nguồn vẫn cảnh báo. Điều chỉnh các kỳ vọng cũ từng coi hoạt động không có hướng là lượt trước, theo yêu cầu mới; giữ các ca không dùng lại bằng chứng của lượt có báo tàu thật.
- Phát lại riêng các mốc từ ảnh: cảnh báo cần sửa được tái hiện ở logic cũ và hết ở logic mới; ca mất kết nối ngoài phạm vi sửa không đổi. Không coi tập bản tin trích từ ảnh là toàn bộ dữ liệu web hay cache điện thoại.
- **171 kiểm tra lưu trữ Android** đạt trên APK cuối, gồm nguồn đã khôi phục trước thao tác thử, nâng cache 3 → 4, gỡ suy luận ở lịch sử/sơ đồ và giữ lỗi thật. Ca mới có checkpoint chứa một lượt với raw đã quá hạn, cùng suy luận sai trong phần raw còn lưu: đánh giá lại suy luận vẫn giữ được lượt quá hạn qua mở lại Store.

**69 kiểm tra giao diện cập nhật** đạt trên APK cuối, giữ các khung lịch sử, nút Chi tiết, âm báo và nguyên tắc cập nhật tại chỗ. Build/Lint đạt **0 lỗi, 15 cảnh báo**. **Hai ca dịch vụ mới** đạt trên cùng APK cuối, cấu hình 30 giây và chạy qua thời gian chờ xác nhận: nguồn đã khôi phục vẫn đối chiếu được sau cả loạt thao tác, không có suy luận/pending/chuông giả; lượt mới tiếp theo thiếu nguồn mới vẫn được cảnh báo và ghi nhận phát âm, không dùng lại nguồn cũ. Bài test chờ `lastSuccess` của lượt xác nhận trước khi kiểm tra âm báo, tránh đọc snapshot activeFaults giữa lúc service chưa hoàn tất ghi lastAlarm (lượt chạy đầu bắt gặp tình trạng này). Chỉ chạy bộ `--nontrain-only`, không chạy lại toàn bộ service suite; dữ liệu/proxy giả lập được sao lưu/khôi phục. Không có kiểm chứng mới trên Xiaomi thật. Khóa riêng không nằm trong repo; giữ chứng chỉ SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.

APK `downloads/DuongNgang-0.22.apk`, checksum trong downloads/SHA256SUMS. Gói ZIP v0.14 vẫn là snapshot lịch sử.
