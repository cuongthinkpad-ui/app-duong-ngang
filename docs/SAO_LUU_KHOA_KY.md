# Sao lưu và khôi phục khóa ký APK

Đã chuẩn bị một bản sao khóa hiện tại, mã hóa bằng mật khẩu ngẫu nhiên riêng. Đã thử khôi phục qua công cụ độc lập, so sánh đúng toàn bộ bytes của khóa và chứng chỉ APK. Sai mật khẩu hoặc sửa nội dung bản sao đều bị từ chối.

## Chủ dự án cần giữ gì?

Hai tệp được giao riêng trong cuộc trò chuyện:

1. `DuongNgang-signing-backup.enc.txt`: bản sao khóa đã mã hóa; có thể giữ nguyên đuôi `.txt`.
2. `backup-password.txt`: mật khẩu mở bản sao. Cất mật khẩu ở nơi riêng, ví dụ trình quản lý mật khẩu/ghi chú riêng; không đăng lên GitHub hoặc chia sẻ công khai.

Chủ dự án cần tải và giữ ở máy cá nhân/ổ lưu trữ riêng ngoài cloud, rồi kiểm tra mở được hai tệp. Nếu thao tác tải tệp trong giao diện thất bại, các tệp đều là văn bản: có thể mở, sao chép TOÀN BỘ nội dung và lưu bằng trình soạn thảo văn bản thuần như Notepad, không thêm lời giải thích vào nội dung. Khi khôi phục, tên/đuôi tệp không quan trọng bằng nội dung nguyên vẹn.

**Trạng thái:** đã tạo và kiểm tra trong cloud; chưa có xác nhận chủ dự án đã tải ra ngoài. Không coi bản sao vẫn nằm trong cloud là đã được lưu bền vững ở nơi khác.

Trong môi trường hiện tại, các tệp nằm ở `/workspace/private-backups/duong-ngang-signing/`, ngoài repository, quyền thư mục 700/tệp 600. Không commit khóa, mật khẩu hoặc bản sao mã hóa vào Git. Repository chỉ lưu công cụ khôi phục và hướng dẫn này.

## Dùng ở phiên/môi trường mới

Chủ dự án cung cấp hai tệp qua kênh riêng cho người tiếp tục dự án. Không cần chủ dự án chạy lệnh này trên điện thoại; đây là hướng dẫn dành cho người build app.

Cần Python 3 và thư viện `cryptography` (cài trong môi trường build nếu chưa có). Khôi phục vào thư mục mới, không ghi đè khóa khác:

```sh
python3 scripts/restore-signing-key.py /duong-dan/DuongNgang-signing-backup.enc.txt --output-dir /thu-muc-cha-da-ton-tai/khoa-da-khoi-phuc
# Nhập mật khẩu khi được hỏi; terminal không hiện mật khẩu.
```

Có thể dùng `--password-file /duong-dan-rieng/backup-password.txt` để không đặt mật khẩu trực tiếp trong câu lệnh. Thư mục kết quả phải chưa tồn tại. Kết quả gồm `debug.keystore` và `signing-info.json`; cả hai cần được giữ kín. Tệp thông tin chứa alias/thông tin mở kho khóa cần để cấu hình ký.

Kiểm tra SHA-256 chứng chỉ phải là:

```
281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44
```

Sau đó cấu hình build dùng khóa đã khôi phục. Cloud cũ dùng `ANDROID_USER_HOME=/workspace/toolchains/android-home` và file `debug.keystore` trong đó. Môi trường mới phải kiểm tra đường dẫn/cấu hình ký thực tế, không tự ghi đè một khóa đã có. Giữ applicationId `vn.duongngang.monitor`, tăng versionCode và kiểm tra chữ ký APK kết quả trước khi phát hành cài đè.

## Định dạng và kiểm chứng

Định dạng `duong-ngang-signing-backup-v1`, AES-256-GCM, PBKDF2-HMAC-SHA256 600.000 vòng, salt 16 bytes, nonce 12 bytes; mật khẩu ngẫu nhiên 192 bit, khác mật khẩu tiêu chuẩn của kho debug Android. Thông tin khóa cũng nằm trong phần mã hóa. Công cụ không in mật khẩu hay khóa thô, kiểm tra xác thực trước khi ghi tệp và không ghi đè thư mục đã tồn tại.

SHA-256 tệp mã hóa đã chuẩn bị (hai tên `.enc` và `.enc.txt` có cùng bytes):

```
b5a46a086a71f161a554daa47acadd8741018a3648d0506cbe156b06f4768108
```

Các kiểm tra đã đạt: giải mã qua CLI, khớp bytes khóa gốc, khớp chứng chỉ, từ chối mật khẩu sai, từ chối ciphertext bị sửa. Không thay đổi mã app hoặc APK hiện tại.
