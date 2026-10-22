# Giao diện 0.7 — 23/10/2026

Mỗi lượt đồng bộ trước đây thay toàn bộ content view, sau đó đợi một callback mới dựng khối tàu/sự cố, gây khung hình trống. Nay cập nhật trong cửa sổ hiện tại: giữ root, header, thanh điều hướng, vùng vẽ sơ đồ và hai thẻ Tổng quan. Hàng nội dung được thay đồng bộ trong cùng lượt UI, Dashboard dựng trước khung hình đầu. Danh sách giữ vị trí cuộn khi cập nhật; sơ đồ giữ cuộn ngang, cập nhật lại mảng chiếm dụng trên chính view hiện tại để không giữ nhầm hướng/trạng thái cũ. Không tạo vị trí tàu nội suy hay tự di chuyển theo đồng hồ.

Sự cố xếp theo thời điểm phát sinh giảm dần; kho lỗi đang mở được sắp lại khi đọc, kể cả dữ liệu cache cũ không theo thứ tự. Tab Sự cố vẫn giữ thời điểm lỗi gốc khi ghép khôi phục. Cài đặt bỏ nút minh họa và đoạn giải thích cuối, giữ chữ ký. Khởi động vào theo dõi thực; không tự mở lại chế độ minh họa từ bản cũ. URL mặc định `http://` là nội dung sửa/xóa được, không phải tiền tố cố định. URL đã lưu giữ nguyên. Dòng đơn vị dùng “Công ty CP TTTH Đường sắt Vinh”.

Bổ sung theo yêu cầu: khối Sự cố bắt đầu lại sau 00:00 giờ Việt Nam, không ghi “đã khôi phục” cho lỗi bị reset. Dịch vụ có hẹn reset riêng với chu kỳ đọc web; giao diện nhận đổi ngày ngay khi đang mở. Mỗi lượt đọc vẫn áp mốc đầu ngày để dữ liệu hôm trước không quay lại. Giữ các lỗi mới sau 00:00, kho lịch sử và chiếm dụng tàu; đóng thông báo lỗi cũ. Khi Android hạn chế dịch vụ, mốc lọc vẫn được áp dụng khi app/dịch vụ hoạt động lại.

## Kiểm tra

- Build APK và Android Lint thành công; không có lỗi Lint.
- **92** kiểm tra quy tắc, gồm ranh giới 00:00 Việt Nam và mốc gián đoạn. **26** kiểm tra lưu trữ Android đạt, bổ sung xóa lỗi hôm trước, giữ lỗi mới sau 00:00, giữ chiếm dụng tàu/lịch sử, xóa suy luận cũ và không reset lặp trong ngày.
- **21** kiểm tra UI trên giả lập Android đạt: cache sự cố đảo thứ tự, thứ tự hiển thị cả Tổng quan/tab Sự cố, Dashboard đã có nội dung sau đo layout, giữ root/hai thẻ/vùng sơ đồ/cuộn ngang và đổi hướng/giải phóng chiếm dụng khi gọi đường cập nhật của các chu kỳ 30/120/300 giây; bỏ minh họa/đoạn giải thích, giữ chữ ký, URL có thể xóa và đổi sang HTTPS, ngày mặc định theo ngày mới và giữ ngày lịch sử chọn riêng.
- **7** kiểm tra service trên giả lập với web cục bộ đạt sau bổ sung reset ngày: khởi tạo, lỗi nguyên văn, suy luận tự kích, kênh có chuông, khôi phục/chứng cứ tới muộn, lỗi khi tắt âm và kênh im lặng.
- Bài kiểm tra UI/lưu trữ dùng dữ liệu tổng hợp trong device-protected storage riêng; không truy cập web công ty. Các chu kỳ được kích hoạt qua cùng callback UI, không chờ 30/120/300 giây thực. Lần đầu tuyến thử quá ngắn để cuộn trên độ rộng giả lập; tăng tuyến thử lên 30 điểm rồi kiểm tra lại đạt.
- Kiểm chứng kết nối web/chạy nền của bản 0.6 được ghi tại [biên bản trước](LIVE_REVIEW_0.6.md), không thực hiện lại trong thay đổi giao diện này.

Lệnh tái hiện sau build, trên giả lập riêng:

```sh
./scripts/test-android-route.sh
./scripts/test-android-ui.sh
```

Kiểm tra giữ view và dữ liệu không thay thế đánh giá chuyển động trên Xiaomi thật. Cần đối chiếu cảm nhận mượt, cỡ chữ và cập nhật trong sử dụng thực tế. APK cùng chữ ký các bản trước để cài đè.
