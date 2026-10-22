# Sửa lượt tàu cũ ở bản 0.10 — 23/10/2026, giờ Việt Nam

## Lỗi được tái hiện

Chủ dự án gửi ảnh lúc 04:47:01: nhiều khoảng sáng/hướng cũ còn trên sơ đồ, nhãn hướng bị chồng hoặc cắt. Nhật ký mới nhất là tàu chẵn. Yêu cầu được nhắc rõ: **ngay khi app nhận bản tin tàu đã qua, đoạn trước phải tắt và chuyển sang đoạn sau**, không đợi khôi phục hệ thống.

Đã thêm bài kiểm tra gây thất bại với code 0.9: A đã qua/khôi phục, B chỉ có bản tin đã qua mà thiếu tiếp cận. Code cũ tạo đoạn B–C nhưng không đóng A–B. Các trường hợp khác: bản tin đã qua tới sau khôi phục tạo lại lượt đã xóa; hướng mới không đóng đoạn cũ trái hướng ở điểm liên quan; hai cảm biến kề nhau báo đảo thứ tự bị coi là hai lượt.

## Cách sửa

- Bản tin đã qua cũng nối vào lượt ở điểm phía trước. Mỗi lần chuyển bước đóng đúng lượt trước, kể cả thiếu bản tin tiếp cận riêng.
- Lưu `successorId` giữa các lượt ở hai điểm; giữ dấu hoàn tất để bản tin muộn không tạo lại tàu. Giữ số dấu có giới hạn theo điểm/hướng/lượt đang mở, tránh tăng mãi qua nhiều ngày.
- Bằng chứng hướng ngược mới tại một điểm đóng các đoạn cũ trái hướng chạm điểm đó. Không xóa các tàu độc lập ở nơi khác.
- Đối chiếu báo tiếp cận đảo thứ tự chỉ trong 2 phút và với lượt phía sau chưa được nối. Không ghép nhầm một lượt nhiều giờ trước; đây **không phải hạn hết thời gian của đoạn đang chờ**. Đoạn giữa hai đường ngang vẫn giữ qua khôi phục điểm trước, nửa đêm/khởi động lại và khoảng cách xa.
- `travelVersion=2` dựng lại cache từ raw đã lưu, gồm phần trước checkpoint. Chuyển nền khi mở app/nâng cấp; không bày cache sai như trạng thái hiện tại. Giữ tài khoản, mật khẩu, phạm vi, raw và lỗi. Khi đang chuyển ghi “Đang cập nhật sơ đồ”.
- Tên hướng và mũi tên vẽ liền một nhãn. Khi nhiều đoạn, rút gọn thành “Lẻ →”/“Chẵn ←”, bố trí hàng phía trên đường và giữ khoảng cách tối thiểu; màn hẹp cuộn ngang. Không còn chữ ở một hàng, mũi tên rời ở hàng khác.

## Đối chiếu web thật

Đã đọc chỉ xem bằng tài khoản kiểm thử được chủ dự án cấp, lấy **2.395 bản tin, 13 điểm** cho ngày hiện tại/ngày trước. Phát lại cùng dữ liệu ở mốc ảnh 23/10/2026 04:47:01 giờ Việt Nam:

| Thuật toán | Số đoạn dựng ra | Hướng |
|---|---:|---|
| 0.9 | 8 | 5 lẻ, 3 chẵn |
| 0.10 | 1 | Chẵn |

Phát lại ảnh chụp dữ liệu mới nhất trong lượt đọc cũng còn một đoạn chẵn với thuật toán sửa. Đây là kiểm tra thuật toán trên báo cáo nhận từ web, **không phải xác minh vị trí tàu ngoài hiện trường hoặc kiểm tra bản APK trên Xiaomi thật**. Không đưa bản tin, HTML, cookie, tài khoản hoặc ảnh vận hành vào Git; các tệp audit riêng đã xóa, chỉ giữ số liệu tổng hợp này.

## Kiểm tra đã thực hiện

- **335 kiểm tra quy tắc** đạt, gồm 24 lượt tàu luân phiên hai hướng, có bỏ tiếp cận ở các điểm giữa, bản tin đã qua đến muộn, phát lại kho dữ liệu đảo thứ tự, hai tàu theo sau không bị ghép vào cùng một lượt đã dùng và không ghép lượt cách nhau nhiều giờ. Kiểm tra ngay sau từng bản tin đã qua và sau khôi phục, không chỉ trạng thái cuối.
- **35 kiểm tra lưu trữ Android** đạt: lưu/đọc liên kết kế tiếp, chuyển cache 0.9 sai từ raw, giữ dữ liệu/cấu hình/lỗi, chuyển đoạn sau di trú và bỏ hướng trái cũ sau khởi động lại. Fixture di trú được tách khỏi lượt qua đêm chưa đóng của kiểm tra trước để không giả việc xóa chiếm dụng hợp lệ.
- **36 kiểm tra UI Android** đạt: canvas hiện tại tắt đoạn cũ ngay khi chỉ nhận bản tin đã qua, bỏ hướng cũ bằng bằng chứng mới, không sáng lại từ bản tin muộn; đo bounds của nhãn dày trên canvas thật, không chồng hàng/cắt phía trên. Giữ kiểm tra root/thẻ/cuộn, thứ tự lỗi, câu “Không có sự cố”, ngày/URL/chữ ký và phát/dừng audio.
- **12 kiểm tra parser, 13 kiểm tra đọc web fixture** đạt.
- **9 kiểm tra service fixture** đạt: đồng bộ, lỗi nguyên văn, tự kích có chờ ở 30 giây, chứng cứ muộn/khôi phục, âm báo, tắt âm, lỗi đang mở khi bắt đầu theo dõi ở chu kỳ 2 phút. Giữ dữ liệu/proxy giả lập sau kiểm tra.
- Build APK/gói Android test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**. Không coi build đạt là hết mọi cảnh báo.

Các kiểm tra Android dùng giả lập riêng và dữ liệu tổng hợp. Việc phát nhạc được kiểm tra qua player; chưa chứng minh loa Xiaomi, tiết kiệm pin HyperOS hoặc chạy nền lâu đã được xác nhận.

## Ảnh kiểm tra tổng hợp

- [Đoạn trước sau A đã qua/khôi phục](screenshots/route-passed-0.10.png)
- [B chỉ báo đã qua: A–B tắt, chuyển B–C](screenshots/route-missing-approach-0.10.png)
- [Bản tin muộn không tạo lại đoạn cũ](screenshots/route-late-phase-0.10.png)
- [Hướng chẵn mới thay đoạn lẻ cũ ở điểm liên quan](screenshots/reverse-cleanup-0.10.png)
- [Hai lượt độc lập ở các đoạn khác nhau](screenshots/overview-0.10.png)

## Cài và giới hạn

APK là bản cài đầy đủ, cài đè cùng khóa để giữ cấu hình; không cần gỡ bản cũ để bỏ cache lỗi vì có chuyển cache tự động. Chữ ký SHA-256 vẫn là `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Sao lưu khóa ngoài cloud chưa được chủ dự án xác nhận.

Sơ đồ cập nhật theo dữ liệu app nhận được ở chu kỳ đã chọn, không nhanh hơn nguồn web. Web không cung cấp định danh đoàn tàu; thiếu toàn bộ bản tin/chậm timestamp vẫn có thể làm suy luận chậm hoặc không đủ dữ kiện. Cần đối chiếu bản cài mới với web và sử dụng thật trên Xiaomi; chưa mô tả là đã kiểm định để thay quy trình trực.
