# Nhãn ga Nghi Long gọn thành NL — 0.18

Ngày 25/10/2026, giờ Việt Nam. APK 0.18.0, versionCode 18, cùng applicationId và chữ ký cũ.

Chủ dự án phản hồi nhãn Nghi Long dài, che mốc đường ngang bên cạnh và yêu cầu rút gọn thành NL. Thêm tên hiển thị `Rules.stationLabel`: tại Km 303+350, nhãn Nghi Long được viết thành NL. Sơ đồ và lời nhắc khi chạm ga dùng **Ga NL**; các mã ga khác giữ nguyên.

Giữ nguyên khóa `303+350 (Nghi Long)` trong tuyến, cache và liên kết lượt tàu. Đây chỉ là thay đổi cách hiển thị; không đổi bổ sung ga riêng ID Chosy, bằng chứng tự kích, khôi phục, đoạn sáng, dữ liệu web, cảnh báo hoặc các ngưỡng trục. Không yêu cầu dựng lại cache do đổi tên hiển thị.

Kiểm chứng: **506 kiểm tra quy tắc và 57 kiểm tra UI** đạt trên APK cuối; build APK/gói test và Lint đạt với 0 lỗi, 15 cảnh báo. Giữ chứng chỉ ký SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Không đọc web vận hành hoặc kiểm tra điện thoại Xiaomi trong lượt đổi nhãn; không chạy lại parser/storage/service vì không sửa các phần đó. APK và checksum trong downloads.
