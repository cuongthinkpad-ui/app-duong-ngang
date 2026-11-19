# Android 0.27.6 — tải bản mới từ Google Drive

Android 0.27.6 tiếp tục từ nhánh `codex/android-0.27.5-recovery`, giữ package `vn.duongngang.monitor` và dùng versionCode 33. Nút **Tải bản mới nhất** ở cuối Cài đặt, dưới **Dừng theo dõi**.

Khi bấm nút, app tải từ ID tệp Google Drive đã được chủ dự án cung cấp qua HTTPS. Sau đó app kiểm tra tên package, chứng chỉ ký khớp bản đang cài và versionCode cao hơn. Nếu có bản hợp lệ, app cấp quyền đọc tạm thời cho trình cài đặt Android. Người dùng xác nhận cài đặt trên màn hình của hệ thống. Lần đầu Android có thể yêu cầu bật **Cho phép từ nguồn này**. Nếu Drive trả trang xem trước hoặc yêu cầu tải trong trình duyệt, người dùng có thể chọn **Mở Google Drive**. App từ chối APK sai package hoặc chữ ký.

Bản 0.27.5 cần cài đè thủ công một lần lên 0.27.6. Với các bản sau, chủ dự án tạo APK cùng khóa ký và versionCode cao hơn. Trên Drive, mở **Quản lý phiên bản → Tải phiên bản mới lên** của đúng tệp đã chia sẻ để giữ link. Sau đó thử link cũ trong cửa sổ ẩn danh và thử nút trong bản cũ trên một thiết bị thử.

Phiên phát triển nhận lỗi 403 từ proxy cloud khi truy cập Google Drive. Khả năng tải trực tiếp từ link thật và luồng cài đặt đầy đủ trên điện thoại thật chưa được xác nhận. Bộ nghiệp vụ Java đạt 780 ca, parser đạt 16 ca và fixture HTTP đạt 39 ca. Các bộ này kiểm tra phần giám sát hiện có. Build APK ứng dụng và AndroidTest đạt. Lint báo 0 lỗi và 14 cảnh báo. APK đã được kiểm tra bằng apksigner, SHA-256 chứng chỉ trùng bản 0.27.5. SHA-256 của APK 0.27.6 là `888b5104ba3bb267605e840b5e252184cc005baa67b84db52fec3a4c0f0968b6`.

Đã cài đè 0.27.5 lên 0.27.6 bằng ADB trên AOSP Android 10/API 29 và xác nhận tệp riêng thử nghiệm vẫn còn.

[Tải APK 0.27.6](../downloads/DuongNgang-0.27.6.apk) · [SHA256SUMS](../downloads/SHA256SUMS)
