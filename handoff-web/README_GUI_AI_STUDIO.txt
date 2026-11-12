GÓI ĐẶC TẢ VIẾT LẠI WEB GIÁM SÁT ĐƯỜNG NGANG — CHUẨN ANDROID 0.27.3

Gửi nguyên ZIP cho AI Studio. Nếu chỉ gửi được một file văn bản, gửi
BAN_GIAO_DAY_DU_VIET_LAI_WEB_0.27.3.txt. File này tự mô tả toàn bộ yêu cầu;
không yêu cầu chatbot mới biết lịch sử chat trước đó.

THỨ TỰ ĐỌC
1. BAN_GIAO_DAY_DU_VIET_LAI_WEB_0.27.3.txt hoặc .md: đặc tả chính.
2. MA_TRAN_NGHIEM_THU.csv: từng ca phải kiểm tra và kết quả mong muốn.
3. reference/android/java/: mã chuẩn của app đang hoạt động.
4. reference/tests/: các kiểm thử nghiệp vụ/parser/âm thanh gốc.
5. reference/history/: lịch sử quyết định, chỉ đọc sau đặc tả chính.
6. reference/screenshots/: ảnh tham khảo phong cách cũ, không phải bố cục cuối.

CẦN LÀM WEB MỚI, KHÔNG SỬA APK
Gói này không phải bản web đã được nghiệm thu, không chứa backend/frontend
web để coi là sản phẩm hoàn chỉnh. Mã Android được gửi để đối chiếu chính xác
quy tắc đường ngang, website chủ, thuật toán tàu, lỗi, trục và giao diện.
Có thể dùng lại Java core hoặc port nguyên thuật toán; phải chứng minh bằng test.
Các ghi chú cũ "không chuyển app thành web" đã được yêu cầu hiện tại làm web
riêng thay thế. Yêu cầu không động vào app Android vẫn giữ nguyên.

Phải đọc toàn bộ đặc tả, gồm 32 ví dụ và ma trận nghiệm thu. Không thay bằng
một app demo đơn giản, không bịa API của Visitec, không tự bỏ tính năng khó.
Các giới hạn Safari/iPhone được ghi riêng; không hứa chạy timer/TTS/rung nền
như native nếu chưa có cơ chế hợp lệ và chưa kiểm chứng trên máy thật.

Nguồn Android cố định: ee1affe5ea024590ce356e0f1e23aafbf17df669.
Không có tài khoản/mật khẩu vận hành, cookie, khóa ký riêng, APK hoặc capture
website có đăng nhập trong gói này. SOURCE_SHA256.json để kiểm tra từng file.
