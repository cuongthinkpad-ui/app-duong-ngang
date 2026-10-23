# Lưu và tiếp tục phiên làm việc Đường Ngang

Gói sao lưu ngày 23/10/2026, ứng dụng 0.14.0 (versionCode 14).

## Cách lưu

Tải `DuongNgang-phien-lam-viec-2026-10-23-v0.14.zip` trong thư mục `backups` của GitHub dự án, chọn **Download raw file**. Giữ một bản trên máy cá nhân và một bản ở nơi lưu trữ riêng của bạn. Giải nén thử và mở `BAT_DAU_TAI_DAY.md` để xác nhận đã tải được.

Gói gồm mã nguồn, APK, tài liệu yêu cầu/nhật ký đã chốt, ảnh kiểm tra, công cụ kiểm tra/build và lịch sử Git. Phần trao đổi là nhật ký có cấu trúc và bản chốt ngữ cảnh; gói không có bản xuất nguyên văn mọi tin nhắn hoặc ảnh gốc trong ChatGPT. Nếu cần nguyên văn, xuất thêm dữ liệu từ mục quản lý dữ liệu của tài khoản ChatGPT.

Gói không chứa khóa ký/mật khẩu mở bản sao khóa, tài khoản website đang lưu trên điện thoại, dữ liệu vận hành của điện thoại, SDK hoặc cache build. Hai tệp khóa cần giữ riêng: `DuongNgang-signing-backup.enc.txt` và `backup-password.txt`. Xem `docs/SAO_LUU_KHOA_KY.md`; không đưa chúng lên GitHub.

## Tiếp tục ở ChatGPT/Codex khác

Gửi gói ZIP, hoặc kết nối repository https://github.com/cuongthinkpad-ui/app-duong-ngang, rồi gửi đoạn sau:

> Tôi là Cao Xuân Cường. Hãy tiếp tục app Android Giám sát Đường ngang của Công ty CP TTTH Đường sắt Vinh từ gói sao lưu 23/10/2026, bản 0.14. Trước khi sửa, đọc AGENTS.md, docs/NGU_CANH_PHIEN_0.14.md, docs/BAN_GIAO.md, docs/LICH_SU_TRAO_DOI.md, README.md và docs/UI_REVIEW_0.14.md. Kiểm tra phiên bản/commit thực tế trước vì repository có thể đã có cập nhật mới hơn gói này. Giữ nguyên các nguyên tắc hoạt động, ngoại lệ trục 320+445 và khóa ký APK. Không nhúng tài khoản website vào mã nguồn. Đừng dựng lại dự án từ đầu. Yêu cầu tiếp theo của tôi là: [ghi yêu cầu mới].

Nếu GitHub không còn truy cập được, mã nguồn nằm trong `app-duong-ngang/` của ZIP vẫn dùng được. Để khôi phục cả lịch sử Git, người phát triển chạy:

```sh
git clone git-history.bundle duong-ngang-restored
```

Tệp `MANIFEST.json` ghi commit nguồn trong gói; `SHA256SUMS` ghi mã kiểm tra các tệp bên trong. Khôi phục khóa ký theo hướng dẫn riêng rồi xác nhận chứng chỉ trước khi ký bản cài đè. Khóa mới sẽ không cập nhật đè được app đã cài bằng khóa cũ.

## Mốc bàn giao

App 0.14.0, applicationId `vn.duongngang.monitor`, mã giao diện/APK tại commit `7218529092a87ca2e4766e94eaa11e45d1f57c40`.

APK SHA-256: `7f36960af2674f34279df30ccb78ab18592db5dd6a9e5edcfaae13455cb5d974`.

Chứng chỉ ký SHA-256: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.

Đã phát hành GitHub và xác nhận file tải công khai trùng APK đã kiểm tra. Giữ các giới hạn xác minh trên Xiaomi thật trong biên bản 0.14.
