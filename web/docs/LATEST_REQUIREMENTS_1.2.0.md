# Yêu cầu hiện hành web 1.2.0 — 14/11/2026

Tài liệu này ƯU TIÊN hơn đặc tả 0.27.3, core Android và mọi biên bản 1.0.0/1.1.0. Android chỉ tham chiếu; không áp dụng thay đổi web vào APK.

1. Worker server đọc nguồn mỗi 10 giây từ lúc bắt đầu lượt thành công trước; lượt chậm không bù/chồng. Không cần browser mở.
2. Nghi tự kích: bắt đầu pending khi nhận lượt mới hợp lệ, tối thiểu 10 giây từ firstSeen. Chỉ lần đọc nguồn thành công tiếp theo, sau đối chiếu lại toàn bộ bằng chứng, mới xác nhận. Timer/cache/maintenance offline không được tạo suy luận. Bằng chứng tới muộn gỡ đúng suy luận, không xóa raw/lỗi thiết bị.
3. Lỗi mạng retry 50 giây từ khi lần lỗi kết thúc; retry tay bỏ chờ. Lock/lease giữ request không chồng. Sai xác thực dừng theo dõi.
4. Từng CB có |delta| 1 hoặc 2: loại hoàn toàn khỏi ghi chú, dẫn xuất, số lần, tổng thiếu/thừa, UI thống kê, âm/Push. Giữ bản tin và số CB gốc. CB khác cùng bản tin vẫn xét độc lập. Hòa chuẩn vẫn theo quy tắc cũ, không suy đoán delta.
5. |delta| ≥3 cộng một lần/source/CB/điểm/ngày Việt Nam; lần thứ 6 báo ngưỡng một lần/ngày; lần 7+ lệch 3–9 không nhắc. |delta| ≥10 báo ngay từng source mới. Cùng source/CB đạt cả hai ngưỡng chỉ một cảnh báo. Giữ sáu ngoại lệ, lỗi tới hết ngày, chống trùng. Migration phiên bản webRulesVersion=2 dựng lại derived cache từ raw trước khi phục vụ snapshot cũ; lịch sử cũng tính theo core web mới. Không sửa raw/checkpoint/hệ thống Android.
6. Không MP3 liên tục/âm im lặng để giữ nền. WAV hữu hạn chỉ dùng foreground cho đợt cảnh báo.
7. Chấm đường ngang đỏ nếu còn sự cố chưa khôi phục; thay đổi sự cố phải cập nhật cả khi không đổi tuyến/tàu. Ga vàng như cũ; đoạn tàu/mũi tên xanh cả khi dữ liệu cũ.
8. Danh sách ghi “N sự cố” hiện còn tồn tại, nếu không lỗi chỉ ghi “Tốt” sau đọc đầy đủ ngày được chọn (historyReady); chưa đủ ghi “Chưa có dữ liệu”. Đây không phải chứng nhận an toàn thiết bị.
9. Giữ toàn bộ quy tắc khác: nguyên văn lỗi nguồn, hướng/ga/Ga NL, tàu không timeout, đúng lượt khôi phục, lịch sử 30 ngày/checkpoint trước prune, account/session/scope, confirm/mute/idempotency.

## Nền tảng, thông báo, giọng nói

AI Studio được chọn PWA/ứng dụng, framework, server, database và dịch vụ Push; bộ Python/Java/SQLite local/PostgreSQL mẫu là nền có kiểm thử, không ràng buộc lựa chọn hạ tầng. Nếu port core phải chứng minh tương đương ngoại trừ các thay đổi nêu trên.

Phân biệt SpeechSynthesis foreground, Web Push và media playback. Không khẳng định mọi âm thanh web đều không thể phát khi khóa máy; cũng không hứa PWA/Push tự đọc TTS nền nếu chưa có thiết kế và kiểm chứng. Không tự đưa lại audio liên tục. Nếu PWA không đáp ứng nhu cầu giọng nền, trình bày phương án native iOS (quyền, cơ chế âm thông báo/giọng hữu hạn, giới hạn hệ điều hành, chi phí/tài khoản phát hành) và phần cần chủ sở hữu quyết định trước khi triển khai. Thử Android/iPhone thật, khóa máy, Focus, mạng, restart và ghi thiết bị/thời lượng/kết quả.

## Phạm vi xác nhận

Gói này cung cấp nguồn, test, đặc tả và prompt. Chưa phải website production/URL HTTPS hoặc nghiệm thu iPhone/Apple Push. Yêu cầu APK riêng: hai thay đổi chấm đỏ và Tốt/N sự cố chưa làm; không được đánh dấu hoàn tất hoặc đưa 10/10/50, bỏ lệch 1–2 vào APK.
