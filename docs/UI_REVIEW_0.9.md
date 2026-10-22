# Bản 0.9 — 23/10/2026, giờ Việt Nam

## Hành vi sửa theo chủ dự án

Trạng thái trống của tab Sự cố đổi từ “Chưa có sự cố phù hợp” thành **“Không có sự cố”**, bỏ câu gợi ý chọn bộ lọc bên dưới. Khối Sự cố trên Tổng quan khi đã đồng bộ và không có lỗi cũng dùng câu này; lúc chưa có dữ liệu vẫn ghi đang chờ dữ liệu.

Sơ đồ trước đây chỉ tô chấm lúc tiếp cận và đóng đoạn chuyển tiếp khi điểm sau báo tàu. Nay mỗi lượt tàu được nối theo lịch sử có **một đoạn sáng liên tục**:

1. 312+111 báo tàu lẻ: sáng mép Bắc của 312+111.
2. 312+111 báo tàu lẻ đã qua: chuyển sang đoạn 312+111–313+456. Khôi phục 312+111 vẫn giữ đoạn này.
3. 313+456 mới báo tàu lẻ: tiếp tục sáng chính đoạn 312+111–313+456, không mất đoạn và không tô thêm mép ngoài.
4. 313+456 báo tàu lẻ đã qua: chuyển sang đoạn 313+456–313+800; đoạn cũ tắt.
5. Tàu chẵn áp dụng ngược chiều. Có thể hiển thị nhiều lượt độc lập; các lượt trùng một đoạn/hướng dùng chung nét xanh, không suy ra số đoàn tàu từ số nét.

Khi cảm biến điểm sau báo sớm trước lúc điểm trước đã qua/khôi phục, điểm sau nhận quyền biểu diễn lượt tàu đó. Điểm trước không tiếp tục giữ một đoạn sáng phía sau. Đây là cách tránh một tàu sáng nhiều khoảng vì các vùng cảm biến chồng nhau.

Đoạn giáp ga được vẽ nhưng không nối xuyên ga: sau khi qua đường ngang hướng vào ga, sáng đoạn tới ga cho tới khôi phục của chính đường ngang đó. Khi đường ngang phía ra báo tàu từ ga, sáng đoạn ga–đường ngang rồi chuyển tiếp sau bản tin đã qua. Bản tin thiết bị trong ga không tự tạo hoặc giữ tàu cả ngày. Tàu ra ngoài mép tuyến tài khoản cũng chỉ giữ mép ra tới khôi phục của đường ngang cuối.

`Rules.trackSegments` quyết định đoạn đến/đi; hình vẽ dùng đúng các đầu đoạn, chỉ thêm mép ngắn ở đầu/cuối tuyến trong giai đoạn tương ứng. Các mốc dịch vào trong một chút để thấy mép tiếp cận đầu tuyến. Giữ cùng canvas/root/thẻ và vị trí cuộn qua mỗi lượt cập nhật. Không đổi quy tắc chiếm dụng phần cứng, reset lỗi, âm báo hoặc điều kiện tự kích chỉ ở 30 giây.

## Kiểm chứng

- **133 kiểm tra quy tắc** đạt, gồm 20 kiểm tra mới theo chuỗi km thực chủ dự án đưa ra: mép tiếp cận, qua/khôi phục, tiếp cận điểm sau không mất đoạn, chuyển sang đoạn kế, hai hướng, báo điểm sau sớm, hai tàu độc lập, vào/ra ga cả hai phía và bỏ bản tin riêng trong ga.
- **30 kiểm tra lưu trữ Android** đạt, giữ các kiểm tra qua khởi động lại, checkpoint, reset ngày, dữ liệu bổ sung muộn và tách tài khoản.
- **32 kiểm tra UI Android** đạt. Kiểm tra canvas thật nhận đoạn phía trước khi tiếp cận, giữ cùng đoạn khi điểm sau báo tàu và chuyển chính xác sau bản tin đã qua; giữ root/thẻ/cuộn và kiểm tra câu “Không có sự cố”. Các kiểm tra âm báo, thứ tự lỗi, lựa chọn ngày, URL sửa được và chữ ký vẫn đạt.
- Build APK/gói Android test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**. Không nâng target SDK hoặc sửa lịch polling trong thay đổi này.
- Ảnh dưới dùng dữ liệu tổng hợp trong vùng lưu trữ riêng của giả lập. Không truy cập website công ty, không đưa thông tin đăng nhập vào mã/APK/ảnh. Lượt này không chạy lại service fixture hoặc web thật vì không sửa phần đọc web/chuông; kết quả 9 kiểm tra service ở [bản 0.8](UI_REVIEW_0.8.md) là kết quả trước đó.

Lần chụp đầu có hộp thoại “System UI isn't responding” của hệ thống giả lập che màn hình. Đã đóng hộp thoại và chạy lại lượt chụp; không dùng ảnh bị che trong bộ ảnh dưới.

## Ảnh theo từng giai đoạn

- [Tiếp cận 312+111 từ Bắc: một mép sáng](screenshots/route-approach-0.9.png)
- [312+111 đã qua và khôi phục: chỉ sáng tới 313+456](screenshots/route-passed-0.9.png)
- [Qua 313+456: chuyển sang 313+800](screenshots/route-next-0.9.png)
- [Hướng vào ga: giữ đoạn 314+550–Vi](screenshots/route-station-0.9.png)
- [Từ ga ra: đoạn Vi–320+445](screenshots/route-from-station-0.9.png)
- [Tổng quan với hai lượt tàu độc lập và lỗi tổng hợp](screenshots/overview-0.9.png)
- [Sự cố trống: câu chữ đã sửa](screenshots/incidents-empty-0.9.png)
- [Sự cố có dữ liệu](screenshots/incidents-0.9.png)
- [Nút thử chuông trong Cài đặt](screenshots/settings-alarm-0.9.png)

## Giới hạn và cài đặt

Đoạn sáng biểu diễn tiến trình từ bản tin, không phải GPS hoặc định danh tàu. Khi website thiếu/chậm bản tin, trạng thái đoạn có thể chậm hoặc còn chờ; không tự xóa chỉ vì hết một khoảng thời gian. Giữ cơ chế checkpoint/phát lại hai ngày của bản trước. Cần chủ dự án đối chiếu trên Xiaomi và dữ liệu thực; kiểm tra giả lập không thay thế xác nhận hiện trường hoặc kiểm chứng chuông/chạy nền lâu trên điện thoại thật.

APK 0.9 là bộ cài đầy đủ, cài mới trực tiếp hoặc cài đè cùng applicationId/chữ ký cũ để giữ cấu hình. Chứng chỉ SHA-256: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Khóa riêng tiếp tục ở ngoài Git; chưa có xác nhận chủ dự án sao lưu ra ngoài cloud.
