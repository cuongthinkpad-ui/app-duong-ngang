# Thử thiết bị trước lượt tàu và màu sơ đồ — 0.20

Ngày 29/10/2026, giờ Việt Nam. APK 0.20.0, versionCode 20, cùng applicationId và chứng chỉ cũ.

## Yêu cầu và nguyên nhân

Chủ dự án đồng ý sửa sau khi được giải thích nguyên nhân, sau đó xác nhận chuỗi hạ cần/khôi phục/nâng cần trước lượt tàu là người kiểm tra ấn nút thử. Phát lại riêng các bản tin trong ảnh với 0.19 tái hiện báo nhầm: mốc bắt đầu của chuỗi thao tác không có hướng tại điểm hiện tại loại mất một lượt tiếp cận rõ ràng cùng hướng ở phía trước. Không truy cập web trong lượt này, không suy đoán những bản tin ngoài ảnh; chỉ đưa kiểm tra tổng hợp/số liệu tổng hợp vào repo, không đưa ảnh hoặc dữ liệu vận hành thật.

## Thực hiện

- Với chuỗi thao tác phía đích chưa có hướng tàu, giữ bằng chứng tiếp cận rõ ràng ở phía trước nếu lượt nguồn còn tiếp diễn sau khi chuỗi cũ kết thúc. Bằng chứng đã kết thúc trước chuỗi cũ vẫn bị loại. Không tự kết luận có tàu từ riêng thao tác thử tại điểm hiện tại.
- Khi có nhiều tàu nối tiếp, ngoại lệ ghép hàng chờ tham chiếu nguồn đã ghép thành công cho lượt trước; không lấy nguồn cũ hơn để bù một lượt trước chưa có bằng chứng. Mỗi lượt nguồn vẫn chỉ dùng một lần, đúng hướng; giữ ga/đầu tuyến, chỉ suy luận ở 30 giây, bằng chứng cần chắn/đã qua phù hợp và loại suy luận khi có bằng chứng tới muộn.
- `selfEvidenceVersion=3` chạy cơ chế đánh giá lại suy luận đã lưu khi cài đè. Bỏ riêng cảnh báo không còn căn cứ, thông báo liên quan và phân loại sai trên sơ đồ; giữ bản tin web gốc/lỗi thiết bị/tài khoản/cài đặt. Không phát lại cảnh báo lịch sử. Giữ `travelVersion=4`, `axleVersion=3` và chữ ký APK cũ.
- Chủ dự án yêu cầu đoạn tàu và mũi tên luôn xanh, kể cả khi dữ liệu cũ. Giữ nhãn dữ liệu cũ, thời điểm cập nhật và điều kiện LIVE; mốc ga vẫn vàng. Đây là biểu diễn từ bản tin, không bảo đảm vị trí thực khi mất cập nhật.

## Kiểm chứng

- Ca hồi quy mới thất bại với logic 0.19 trước khi sửa. **564 kiểm tra quy tắc** đạt, gồm hai hướng, thao tác thử không có hướng, nguồn kết thúc từ trước không được dùng lại, tàu nối tiếp, thiếu bằng chứng và bản tin đảo thứ tự. Bộ kiểm tra cũ gồm nguồn cần chắn, đúng hướng, ga, reset và trục vẫn đạt.
- Phát lại riêng chuỗi bản tin từ ảnh: logic cũ tạo nghi tự kích, logic mới không tạo; việc bỏ chuỗi thao tác thử không còn làm thay đổi kết luận này.
- Build APK/gói kiểm thử và Lint đạt, **0 lỗi, 15 cảnh báo**. Chứng chỉ SHA-256: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.

**156 kiểm tra lưu trữ Android** đạt trên APK cuối, gồm cài đè từ cache tự kích phiên bản 2, gỡ suy luận sai ở lịch sử/sơ đồ, giữ mọi bản tin gốc và lỗi thiết bị. **61 kiểm tra giao diện cập nhật** đạt trên APK cuối. **21 kiểm tra telemetry UI** đạt, gồm vẽ sơ đồ ra bitmap để xác nhận nét tàu vẫn xanh khi dữ liệu cũ, nhãn cảnh báo vẫn có và LIVE bị bỏ. Không có kiểm chứng mới trên Xiaomi thật; không chạy lại web công ty hoặc toàn bộ dịch vụ mô phỏng trong lượt này. APK `downloads/DuongNgang-0.20.apk`; checksum trong `downloads/SHA256SUMS`. Gói sao lưu phiên v0.14 vẫn là snapshot lịch sử.
