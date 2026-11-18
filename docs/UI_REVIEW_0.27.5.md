# Android 0.27.5 — khôi phục lỗi cần chắn và PLC

Nền Android 0.27.4 tại `1f0ef05d432bdf3e0afdeacea35604ca796e527b`, nhánh `codex/android-0.27.3-ui`. Chấm đường ngang đỏ và số sự cố đã có trên nền này. APK cập nhật dùng `vn.duongngang.monitor`, versionCode **32**.

## Nguyên nhân xác nhận từ mã

- Bộ ghép sự cố cũ chỉ xét bản tin đỏ và các dạng khôi phục đã biết. Bản tin cần đã hạ/nâng không đi vào bộ ghép. Lỗi mất trạng thái và lỗi khác của cùng cần còn dùng chung khóa theo số cần.
- `PLC hỏng` và `PLC khôi phục tốt` trả về chính hai chuỗi khác nhau từ `faultKey`. Quy tắc lịch sử cũ đã có thể khép mọi ghi nhận theo một khóa, nhưng không tìm được các lỗi PLC bằng khóa của bản tin khôi phục. Cache hiện hành chỉ giữ lỗi gần nhất theo khóa, còn lịch sử giữ từng bản tin.
- Nạp ngày lịch sử trước đây chỉ tính lỗi từ bản tin trong ngày đang xem. Bộ đọc bằng chứng nối tiếp cũng chỉ tìm dạng “khôi phục tốt”, bỏ qua hoạt động cần chắn.

## Xử lý và dữ liệu

- Hạ hoặc nâng thành công chỉ khép lỗi mất trạng thái xảy ra trước đó của đúng số cần, đúng đường ngang. Hai sự kiện cùng giây chưa đủ để xác định khôi phục. Ghép theo thời gian của bản tin, xuyên các lượt tàu và các ngày đã lưu.
- Một PLC khôi phục tốt khép mọi ghi nhận PLC hỏng trước đó tại cùng đường ngang. Lịch sử liên kết từng lỗi với bản tin phục hồi gốc. Bản tin hỏng mới hơn vẫn mở.
- Tổng quan, danh sách đường ngang, Sự cố và chi tiết thiết bị dùng cùng quy tắc. PLC có dòng riêng trong chi tiết. Ngày lịch sử giữ các lỗi phát sinh trong ngày được chọn và tra bằng chứng khôi phục cần chắn/PLC đã lưu sau ngày đó. Lịch sử sự kiện vẫn hiển thị nguyên văn từng bản tin.
- `faultRulesVersion=2` dựng lại cache lỗi hiện hành từ các bản tin cục bộ trong mốc hiển thị Android. Bước này không phát âm hoặc đăng cảnh báo lỗi. Dữ liệu raw, tài khoản, lịch sử, các phiên bản cache tàu/trục và nhịp đọc Android 30 giây/120 giây giữ nguyên.

## Kiểm thử

- 780 ca nghiệp vụ đạt, gồm 8 ca mới cho thấy hai cần hoạt động độc lập, khôi phục đúng điểm/số cần/loại lỗi, thời gian bằng hoặc trước lỗi không khôi phục, và lỗi mới xuất hiện sau khôi phục. Hai lỗi PLC cùng liên kết với một bản tin phục hồi.
- 16 ca parser và 39 ca đọc qua HTTP fixture có xác thực đạt.
- Build APK ứng dụng, APK kiểm thử và lint thành công. Lint báo 0 lỗi, 14 cảnh báo.
- Chưa thử trên điện thoại thật/Xiaomi, HyperOS hoặc website Visitec trong phiên này.

## APK

[DuongNgang-0.27.5.apk](../downloads/DuongNgang-0.27.5.apk) · [SHA256SUMS](../downloads/SHA256SUMS)

- SHA-256 của APK là `4650f72d81808a53e092549bc0c085016117add882cd89a8e53b741c5422e432`.
- `apksigner verify` xác nhận SHA-256 chứng chỉ `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`, trùng chứng chỉ APK 0.27.4. Cài đè giữ ứng dụng cũ trên máy.
