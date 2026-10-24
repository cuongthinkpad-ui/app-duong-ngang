# Bổ sung bằng chứng hoạt động khi xét tự kích — 0.16

Ngày 24/10/2026, giờ Việt Nam. Bản đầy đủ 0.16.0, versionCode 16; cùng applicationId và khóa ký để cài đè giữ cấu hình.

## Quy trình đã được xác nhận

Chủ dự án yêu cầu đọc/đối chiếu trước, chưa sửa app trong lúc trao đổi. Sau phân tích đã thống nhất: bên cạnh Tàu lẻ/Tàu chẵn, tại điểm liền kề phía trước theo hướng chạy, cần chắn đã hạ/đã nâng hoặc tàu đã qua cũng là bằng chứng có tàu trong lượt đang xét. Tàu đã qua phải cùng hướng; không dùng hoạt động của lượt cũ. “Cần chắn hạ/nâng” là cách gọi chung cho các số cần, không phải chuỗi phải khớp nguyên văn. Chủ dự án đã xác nhận và yêu cầu sửa app.

Giữ suy luận chỉ ở chu kỳ 30 giây và ranh giới ga/đầu/cuối tuyến. Nếu bằng chứng phù hợp bổ sung sau thì gỡ riêng nghi tự kích. Giữ lỗi đỏ gốc, chuông sáu hồi, sơ đồ, thiết bị, trục và các ngoại lệ trục ở 280+500/307+507/309+056/320+445/338+900/343+350.

## Cách đối chiếu

Chỉ dùng các bản tin gốc không đỏ. Nhận số cần tùy ý, chữ hoa/thường và khoảng trắng; thông báo xác nhận đã hạ/nâng phải hoàn chỉnh. Các câu lỗi, không hạ, mất trạng thái hoặc chưa hoàn tất không phải bằng chứng thành công.

Gộp bản tin tiếp cận, các cần chắn và bản tin đã qua của một chu kỳ thành một lượt; khôi phục/nút phục hồi khép lượt, lượt tiếp cận mới tách riêng. Nhiều cần hoặc trang tải chồng không tạo thêm bằng chứng. Mỗi lượt phía trước chỉ giải thích một lượt tiếp cận kế tiếp. Bản tin đã qua giữ hướng; khi lượt đã biết hướng ngược, hoạt động cần chắn trong lượt đó không đổi nó thành tàu cùng hướng.

Bằng chứng bổ sung có thể xuất hiện sau mốc tiếp cận đang xét, nhưng phải trong chu kỳ của điểm đó, không lấy một lượt mới sau khi điểm đã khôi phục để xóa nghi vấn trước. Tàu tiếp cận rõ ràng vẫn đối chiếu một-một khi có nhiều tàu nối tiếp/vùng cảm biến chồng nhau; không đặt đồng hồ tự xóa tàu giữa các đường ngang. Dữ liệu hiện có có thể thiếu bản tin nên đây vẫn là suy luận, không xác định danh tính tàu.

## Cài đè và lưu trữ

Thêm `selfEvidenceVersion=1`. Lần mở/poll đầu đánh giá lại các suy luận cũ từ raw đã lưu; chỉ bỏ những suy luận có nguồn gốc xác định và nay được giải thích bằng bằng chứng. Giữ raw, lỗi phần cứng và nghi vấn vẫn thiếu bằng chứng. Lọc pending chọn lọc, hủy riêng thông báo sai và dựng lại phần phân loại sơ đồ; nếu suy luận cũ nằm trước checkpoint thì dựng lại checkpoint từ raw. Không phát cảnh báo lịch sử trong bước chuyển. Đổi URL/ID xóa cờ phiên bản cùng dữ liệu tài khoản như các cache khác.

Khi đang theo dõi, lượt poll sau tính lại ứng viên; bằng chứng mới làm bỏ suy luận sai, giữ thông báo/lỗi thật và không phát chuông báo mới chỉ vì hiệu chỉnh. Thu hẹp phạm vi không loại bằng chứng ở ngoài vùng thông báo.

## Kiểm chứng

Trong phần điều tra trước khi được phép sửa, đã đăng nhập và đọc đủ phân trang hai ngày báo cáo thật bằng tài khoản chủ dự án cung cấp. Phát lại cùng dữ liệu với 0.15 cho một nghi vấn ở lượt cần đối chiếu; quy tắc mới cho không còn nghi vấn đó. Dữ liệu xác thực/vận hành, cookie và bản chụp thô được giữ ngoài Git; chỉ ghi kết quả tổng hợp ở đây. Các ca kiểm tra công khai dùng lý trình/thời gian và tài khoản tổng hợp.

- **486 kiểm tra quy tắc** đạt. Ca mới đã thất bại trên 0.15 trước khi sửa; sau sửa đạt cho hạ/nâng các số cần, câu/đỏ không hợp lệ, gộp bằng chứng, nhiều tàu nối tiếp, hướng ngược, ranh giới lượt, bằng chứng tới sau, giới hạn coverage và gỡ pending.
- **130 kiểm tra lưu trữ** đạt, gồm cài đè, suy luận sai chỉ còn cache, hủy thông báo chọn lọc, giữ lỗi thật/đúng nghi vấn/firstSeen, giữ raw, không báo lại lịch sử, sửa phân loại ở checkpoint cũ và đổi tài khoản.
- **54 kiểm tra UI** đạt: giữ cập nhật tại chỗ/sơ đồ/khôi phục, âm sáu hồi, thống kê trục và đủ sáu ngoại lệ. Giao diện không thay đổi ngoài dữ liệu suy luận được hiệu chỉnh.
- Build APK/gói test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**.
- **25 kiểm tra dịch vụ theo chu kỳ thật đạt trên APK cuối**: bằng chứng đã qua bổ sung gỡ riêng suy luận và không phát lại báo động; thao tác cần lặp trong lượt đã dùng không ngăn một lượt mới thiếu bằng chứng bị nghi tự kích. Giữ nút phục hồi/sơ đồ, đổi 2 phút rồi 30 giây, lỗi chuông thật, tắt âm, cảnh báo lúc bắt đầu theo dõi và các ngưỡng trục/ngăn báo lặp/sáu ngoại lệ. Dữ liệu dịch vụ là tổng hợp, không phải web vận hành. Không coi kiểm tra giả lập hoặc phát lại báo cáo là đã xác nhận loa/pin HyperOS và mọi lượt ngoài hiện trường.

Chứng chỉ SHA-256 giữ nguyên: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Checksum APK trong `downloads/SHA256SUMS`.
