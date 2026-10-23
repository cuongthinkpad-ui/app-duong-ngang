# Thêm năm ngoại lệ đếm trục — 0.15

Ngày 23/10/2026, giờ Việt Nam. App 0.15.0, versionCode 15; bản cài đầy đủ, cài đè giữ dữ liệu/cấu hình và cùng khóa ký cũ.

## Yêu cầu và phạm vi

Chủ dự án yêu cầu thêm 307+507, 309+056, 280+500, 343+350 và 338+900 vào danh sách không tính lệch trục. Giữ 320+445 đã loại trước đó. Danh sách đầy đủ theo km tăng:

**280+500 · 307+507 · 309+056 · 320+445 · 338+900 · 343+350.**

Áp dụng theo lý trình đã chuẩn hóa trên mọi tài khoản có điểm tương ứng. Nhận cả tiền tố Km, khoảng trắng quanh dấu cộng và số 0 đầu. Chỉ đúng sáu vị trí này, không loại cả dải km hoặc các đường ngang lân cận.

Không đối chiếu sáu CB, không ghi chú, không thống kê/hiển thị dữ liệu trục hoặc tạo cảnh báo trục do app suy ra tại sáu điểm. Giữ bản tin nguồn, mọi lỗi đỏ thật từ web (kể cả bản tin nguồn có chữ trục), chuông/đèn/cần chắn/cảm biến/kết nối/điện áp, chiếm dụng tàu/sơ đồ và tự kích. Không sửa ngưỡng hay hành vi của đường ngang khác, không thay giao diện 0.14.

## Cài đè và dọn dữ liệu cũ

`Rules.isAxleExempt` dùng một tập lý trình số dùng chung cho mọi luồng đối chiếu, hiển thị, lịch sử, cảnh báo và dọn dữ liệu. `Store.AXLE_VERSION` tăng 2→3 để chạy lại migration từ bản 0.13/0.14 dù cache cũ đã sẵn sàng. MainActivity vẫn dùng luồng chuyển nền đã có, không chờ poll mới lọc lỗi sai.

Lọc ngay cảnh báo có ID `axle:<CB>:<nguồn>` thuộc điểm ngoại lệ, rồi xóa riêng dữ liệu dẫn xuất cũ trong mọi ngày và hủy thông báo tương ứng, gồm thông báo chỉ còn ở cache. Giữ raw, lỗi thiết bị, tài khoản/phạm vi và dấu đã thông báo ngưỡng số lần của các điểm khác. Không đổi version sơ đồ hoặc khóa ký.

## Kiểm tra

- **450 kiểm tra quy tắc** đạt: cả sáu ngoại lệ, chuẩn hóa tên, cả hai ngưỡng, lịch sử lỗi dẫn xuất, giữ dòng đỏ gốc và tàu; các lý trình sát cạnh từng điểm mới vẫn báo trục đúng ngưỡng.
- **115 kiểm tra lưu trữ** đạt: nâng từ cache 1/2 lên 3 ở đủ sáu điểm, lọc tức thì/dọn ngày cũ, hủy thông báo chỉ còn cache, giữ raw và lỗi thật; dấu thông báo một lần/ngày của điểm bình thường không bị đặt lại.
- **54 kiểm tra UI** đạt: cả năm điểm bổ sung có lỗi Chuông 2 hỏng vẫn hiện đỏ nhưng không có thống kê/ghi chú trục; điểm thường vẫn giữ mục và thông báo trục. Giữ các kiểm tra sơ đồ, cập nhật tại chỗ, âm sáu hồi và giao diện 0.14. [Ảnh điểm 338+900 dùng dữ liệu tổng hợp](screenshots/axle-exempt-detail-0.15.png).
- Build APK/gói test và Lint đạt: **0 lỗi, 15 cảnh báo Lint**.
- **22 kiểm tra dịch vụ** đạt trên APK cuối với website giả lập và chu kỳ thật: cả sáu điểm có sáu lần sai và một lần lệch lớn vẫn không tạo lỗi/âm; dòng đỏ Chuông 2 hỏng tại **309+056** vẫn có báo động và khôi phục đúng. Giữ kiểm tra tự kích/nút phục hồi/đổi chu kỳ/tắt âm/lần thứ sáu không lặp/restart/CB khác/lệch lớn và đúng mười trục tại điểm bình thường. Các fixture dùng tài khoản/website giả và giả lập riêng, không dùng dữ liệu vận hành công ty; không coi kiểm tra giả lập là xác nhận loa/pin HyperOS ngoài hiện trường.

Giữ chứng chỉ SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Checksum APK trong `downloads/SHA256SUMS`.
