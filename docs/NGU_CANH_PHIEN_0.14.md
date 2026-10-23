# Ngữ cảnh để tiếp tục sau khi mất phiên chat

Chủ dự án Cao Xuân Cường. Đơn vị: **Công ty CP TTTH Đường sắt Vinh**. Đây là bản chốt quyết định, không phải bản xuất nguyên văn chat. Đọc kèm nhật ký và bản bàn giao đầy đủ.

## Trạng thái hiện tại

App Java Android hiện có, tên Giám sát Đường ngang, bản **0.14.0 / code 14**; không chuyển framework. Repository `cuongthinkpad-ui/app-duong-ngang`, main. Commit app/APK `7218529`. Người dùng đã cho phép phát hành cập nhật lên cùng GitHub. Điện thoại Xiaomi 14T Pro; link workspace từng tải thất bại, giao APK/ZIP qua GitHub với **Download raw file**.

Bản 0.14 làm lại giao diện từ bốn ảnh Stitch do chủ dự án gửi, được phép linh hoạt khi căn chỉnh. Giữ màu navy/cyan/mint, ga amber, sự cố đỏ trầm, viền thẻ rõ, nút lưu cyan–mint. Chữ và nút đọc được, không sao chép khoảng trống lớn hoặc dữ liệu giả trong mẫu. Logo sáng dịu/vòng lỗi pulse 2,8 giây, dừng khi mất focus/chạy nền; theo tùy chọn tắt hoạt ảnh Android. Đoạn tàu chỉ có quầng sáng tĩnh. Badge LIVE chỉ cho phiên đang chạy có dữ liệu mới.

Đã đạt: 391 quy tắc, 62 lưu trữ, 49 UI hồi quy, 20 telemetry UI với hiệu ứng bật và tắt; build/lint 0 lỗi, 15 cảnh báo. Không thử lại web công ty/service fixture trong lượt đổi UI, chưa đo khung hình hoặc âm thanh trên Xiaomi thật. APK vẫn cùng khóa cũ, cài mới trực tiếp hoặc cài đè giữ cấu hình.

## Những điều phải giữ

- Đọc web ASP.NET cần đăng nhập bằng tài khoản chỉ có quyền xem; URL/ID/mật khẩu nhập ở Cài đặt. Tuyến lấy động theo tài khoản, không chỉ hỗ trợ 13 điểm ban đầu. Chạy nền có foreground service, chu kỳ 30 giây/2 phút/5 phút, đọc đủ phân trang, đăng nhập lại khi hết phiên. Không thêm điều khiển thiết bị.
- Km tăng Bắc→Nam. Bắc là lẻ, Nam là chẵn. Lẻ: CB1/2 phía Bắc → CB3/4 tại đường ngang → CB5/6 phía Nam; chẵn ngược lại. Tàu qua CB3/4 thì cần nâng; web chỉ khôi phục khi ra toàn vùng cảm biến, khoảng một phút sau.
- Sơ đồ mỗi lượt tàu có **một đoạn**: đến thì sáng phía tiếp cận, báo đã qua thì tắt ngay đoạn trước và chuyển đoạn sau, không chờ khôi phục. Giữ đoạn giữa hai đường ngang khi điểm trước đã khôi phục và điểm sau chưa đến/đang tiếp cận. Nhiều tàu/hướng đồng thời; tránh giữ tàu cũ do bản tin muộn. Không tự xóa đoạn theo thời gian, không di chuyển bằng hoạt ảnh. Đây là suy từ bản tin, không phải GPS.
- Điểm có hậu tố ngoặc là ga; hiển thị chữ trong ngoặc. Không nối xuyên ga hoặc dùng bản tin ga giữ tàu cả ngày. Đoạn vào ga tắt khi đường ngang giáp ga khôi phục; tàu từ ga ra xuất hiện khi đường ngang kế cận báo tàu.
- Tuyến ban đầu: 312+111, 313+456, 313+800, 314+550, 319+200 (Vi), 320+445, 320+957, 321+780, 327+327, 328+325, 330+00 (YX), 332+350, 334+830. **11 đường ngang + 2 ga**. Chọn phạm vi chỉ lọc danh sách/lỗi/thông báo, không cắt sơ đồ hoặc dữ liệu đối chiếu.
- Dòng đỏ web là lỗi, giữ **đúng nguyên văn và số thiết bị**. Không đổi Chuông 2 hỏng thành mất tín hiệu hoặc mất trạng thái. Khôi phục chỉ đúng thiết bị; Khôi phục hệ thống không xóa lỗi phần cứng. Sáu nhóm: Chuông, Đèn, Cần chắn, Cảm biến, Kết nối, Điện áp; không camera.
- Chuông **6 hồi/12 giây**, một lần theo cảnh báo mới hoặc khi bắt đầu theo dõi với lỗi đang mở. Một điều khiển tắt/bật chuông chung, giữ trạng thái đỏ khi tắt; có thử chuông trong Cài đặt. Không nhắc lại định kỳ, không nút xác nhận từng lỗi. Lỗi hiện tại reset **00:00 Việt Nam**, giữ lịch sử và lượt tàu; reset không có nghĩa thiết bị đã phục hồi.
- Tự kích chỉ khi chu kỳ **đúng 30 giây**: thiếu lượt tương ứng từ đường ngang trước theo hướng, chờ một chu kỳ và đủ dữ liệu, không qua ga/đầu tuyến. Dùng lịch sử lượt, không đòi điểm trước vẫn đang chiếm dụng. Chu kỳ 2/5 phút tắt suy luận, vẫn giữ dòng đỏ thật. Đúng điểm đang nghi tự kích nhận **Ấn nút Phục hồi** thì khép suy luận và tắt đoạn tàu tự kích đó, giữ các lỗi/tàu khác.
- Đối chiếu **CB1–CB6** chỉ từ Khôi phục hệ thống có sáu số nguyên trục, không có V. Chuẩn là số xuất hiện nhiều nhất duy nhất; hòa chuẩn chỉ ghi “cảm biến đếm trục không đồng nhất, lỗi”, không đoán CB sai. Ghi rõ thừa/thiếu.
- Mỗi CB/đường ngang/ngày: lệch **từ 3 trục** mới cộng số lần; **lần thứ 6** báo lỗi một lần/ngày cho CB đó. Một lượt lệch **từ 10 trục**, gồm đúng 10, báo ngay; CB khác tính riêng, một lượt chạm cả hai ngưỡng gộp một báo. Giữ cảnh báo đến hết ngày, không xóa khi đếm đúng hoặc có phục hồi thiết bị; không lặp ngưỡng số lần sau restart/tải bù. Lệch 1–2 chỉ ghi chú.
- **Km 320+445 ngoại lệ hoàn toàn phần trục suy ra** vì hơn sáu cảm biến/cơ chế riêng: không so, không ghi chú/thống kê/hiển thị trục/cảnh báo dẫn xuất. Dọn cả cảnh báo trục cũ mọi ngày, giữ nguyên bản tin web gốc và lỗi/tàu/tự kích. Điểm khác vẫn áp dụng đầy đủ ngưỡng.
- Chỉ một mục **Đếm trục lỗi** trong chi tiết, chỉ hiện CB đạt ngưỡng; có số lần, lệch gần nhất, tổng thiếu/thừa. Thống kê không thay cho thông báo/chuông. Ghi chú thường nằm dưới sự kiện khôi phục trong chi tiết.
- 4 tab Tổng quan/Đường ngang/Sự cố/Cài đặt. Chi tiết là trang con. Tổng quan vừa màn hình, số dòng thích ứng, không cố định ba dòng, không cần vuốt dọc; giữ root/sơ đồ/thẻ khi poll, không chớp trắng. Không giả pin/góc cần/hướng cố định từ mẫu Stitch.
- Sự kiện theo thứ tự **Giờ – Đường ngang – Nội dung**; Sự cố xếp giờ phát sinh mới nhất trước, cả khi đã khôi phục, không nhóm màu trước/sau. Đã khôi phục tick xanh theo bản tin thật. Trống ghi **Không có sự cố**. Giữ chọn ngày trong Sự cố; không thêm tab lịch sử riêng.
- Chữ ký **Designed by Cao Xuân Cường** chỉ trong Cài đặt. Giữ đơn vị chính xác nêu đầu tài liệu. Không thêm chế độ minh họa trong giao diện vận hành.

## Tiếp tục phát triển

Đọc AGENTS.md và docs/BAN_GIAO.md. Các quyết định mới của chủ dự án ưu tiên tài liệu cũ. Giữ tối ưu bộ dữ liệu tạm trong từng lần render; đừng đổi quy tắc để tăng tốc. Thử những phần bị ảnh hưởng, ghi lại kiểm chứng và giới hạn, không gọi giả lập là đã xác nhận trên Xiaomi.

Khóa ký và mật khẩu sao lưu nằm ngoài Git; chủ dự án cần giữ hai tệp riêng theo docs/SAO_LUU_KHOA_KY.md. Không đưa chúng, tài khoản website, cookie hoặc dữ liệu vận hành lên repo. Tăng versionCode trên 14, giữ applicationId/chữ ký để cài đè. SDK/cache/cloud và lịch sử nguyên văn ChatGPT không tự đi cùng mã nguồn.
