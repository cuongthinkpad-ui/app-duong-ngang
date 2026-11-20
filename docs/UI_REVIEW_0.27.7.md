# Android 0.27.7 — cảnh báo cần chắn hoạt động sai trình tự

Bản Android 0.27.7 tiếp tục từ `a33cc2a` trên nhánh `codex/android-0.27.5-recovery`, giữ package `vn.duongngang.monitor` và dùng versionCode 34.

App nhận diện **Cần chắn hoạt động sai trình tự** theo nội dung bản tin, kể cả khi nguồn hiển thị màu xanh. Bản tin giữ nguyên văn, vào nhóm **Cần chắn** và đi qua luồng Sự cố, trạng thái điểm và thông báo hiện có. Bản tin mới đủ điều kiện báo theo phạm vi theo dõi và chế độ cảnh báo đang chọn. Bản tin hạ/nâng thành công của một cần và khôi phục hệ thống giữ nguyên sự cố sai trình tự. Quy tắc hiện có cho bản tin chung **Cần chắn khôi phục tốt** tiếp tục áp dụng tại đúng điểm.

`faultRulesVersion=3` dựng lại cache sự cố từ các bản tin đã lưu trong mốc hiện hành. Việc dựng cache giữ raw và tài khoản, không phát lại cảnh báo cũ. Lịch sử đọc lại bản tin theo quy tắc phân loại mới.

Fixture parser kiểm tra HTML màu xanh và nguyên văn bản tin. Bộ nghiệp vụ kiểm tra nhóm thiết bị, lỗi hiện hành, lịch sử, thao tác bình thường và khôi phục. Đã đạt 785 ca nghiệp vụ, 17 ca parser và 39 ca fixture HTTP. Build APK và lint thành công với 0 lỗi, 14 cảnh báo. APK 0.27.7 đã được xác nhận có cùng chứng chỉ ký với APK 0.27.6 bằng apksigner. SHA-256 của APK là `0e04bd513a6cb08603730382255a342059e1db9c0c7fce8f885897ba8c9d2b55`. Chưa kiểm thử thông báo từ website thật hay trên điện thoại thật cho bản này.

[Tải APK 0.27.7](../downloads/DuongNgang-0.27.7.apk) · [SHA256SUMS](../downloads/SHA256SUMS)
