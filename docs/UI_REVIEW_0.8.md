# Bản 0.8 — 23/10/2026, giờ Việt Nam

## Kết quả thay đổi

Tất cả màn hình dùng nền navy tối, thẻ xanh xám có viền, cyan cho điều khiển, mint cho tàu và đỏ trầm cho sự cố, theo ảnh mới của chủ dự án. Tổng quan vẫn tự chia chiều cao, không thêm cuộn dọc; giữ root, sơ đồ, hai khối Tàu gần nhất/Sự cố khi đồng bộ. Thẻ tóm tắt ghi số điểm theo dõi hoặc lỗi đang mở và thời điểm cập nhật. Không kết luận “AN TOÀN” từ việc chưa thấy lỗi đỏ. Tên đơn vị đúng, chữ ký chỉ trong Cài đặt, biểu tượng đầu tàu giữ rõ cửa sổ/đèn trên nền tối.

Sơ đồ bổ sung lượt chuyển tiếp: khi có bản tin tàu đã qua điểm A, chỉ tô đoạn giữa A và đường ngang B liền kề theo hướng chạy. A khôi phục vẫn giữ đoạn này tới khi B báo tàu cùng hướng. Không vẽ phần rìa ngoài hai mốc, không nối qua ga hoặc ra ngoài tuyến. Nhiều đoạn/hai hướng được giữ độc lập. Nếu B đã báo trước lúc A đi qua/khôi phục thì lượt chuyển tiếp được ghi nhận hoàn tất, không sáng lại đoạn cũ. Trạng thái chiếm dụng thiết bị vẫn chỉ giải phóng theo bản tin khôi phục; cách vẽ chuyển tiếp không đổi quy tắc phần cứng này.

Store giữ checkpoint và phát lại hai ngày gần nhất để sửa khi bản tin tới muộn; đoạn chờ tồn tại qua nửa đêm/khởi động lại, không tự hết theo đồng hồ. Khôi phục chỉ tạo đoạn chuyển tiếp dự phòng khi đã biết hướng từ lượt báo tàu. Không dùng bản tin ga hoặc khôi phục không biết hướng để tạo đoàn tàu. Đây là suy luận từ sự kiện, không phải GPS; dữ liệu thiếu có thể giữ đoạn chờ, và bản tin bổ sung cũ hơn checkpoint không được bảo đảm phát lại.

**Tự kích chỉ xét khi chu kỳ đúng 30 giây.** Chọn 2 phút/5 phút không tạo suy luận; bỏ pending và suy luận hiện tại trong ngày, giữ lịch sử ngày cũ và mọi dòng đỏ gốc. Lỗi thật vẫn hiện nguyên văn và báo theo cấu hình âm thanh.

Chuông lỗi có bộ phát riêng `AlarmSound`, dùng MediaPlayer/audio focus và âm chime tự tạo 6 giây. Phát một lần khi có lỗi mới hoặc bắt đầu phiên theo dõi với lỗi chưa khôi phục trong ngày/phạm vi đã chọn. Không phát lại chỉ vì mở Activity trong khi service vẫn chạy, không nhắc định kỳ. Tắt chuông chung dừng âm đang phát và giữ lỗi đỏ; khôi phục hết lỗi đang báo, reset ngày hoặc dừng dịch vụ cũng dừng âm. Cài đặt có **Thử chuông báo lỗi**, dùng mức âm lượng **Báo thức**; không tự tăng âm lượng hoặc vượt chế độ Không làm phiền.

## Kiểm tra đã thực hiện

- Build APK chính/gói Android test thành công; Lint **0 lỗi, 15 cảnh báo**. Cảnh báo gồm target SDK, lịch polling và một số phân bổ khi vẽ; không mô tả build đạt là không còn cảnh báo.
- **113 kiểm tra quy tắc** đạt. Bổ sung chuyển tiếp sau khôi phục, hai hướng/nhiều lượt, ga/đầu cuối tuyến, bản tin đến không theo thứ tự, điểm sau kích trước lúc điểm trước đã qua, cùng timestamp, dự phòng thiếu bản tin và điều kiện tự kích đúng 30 giây.
- **30 kiểm tra lưu trữ Android** đạt: giữ đoạn sau khôi phục, qua checkpoint nhiều ngày, khởi động lại/reset ngày, phát lại bản tin điểm sau tới muộn, cùng các kiểm tra tách tài khoản/đúng thiết bị đã có.
- **30 kiểm tra UI Android** đạt: giữ root/thẻ/canvas/vị trí cuộn khi cập nhật; hướng và đoạn chuyển tiếp đổi thật; không thêm xanh ở điểm nguồn đã qua; đóng đoạn khi điểm sau báo tàu; bảng màu/tóm tắt; phát và dừng MediaPlayer; ẩn suy luận hiện tại khi chọn chu kỳ dài. Giữ các kiểm tra thứ tự lỗi, reset ngày, URL sửa được, bỏ minh họa và chữ ký chỉ trong Cài đặt.
- **9 kiểm tra service** với web fixture cục bộ trên giả lập đạt: ảnh chụp đầu rỗng không phát âm; lỗi chuông nguyên văn kích hoạt player; suy luận có thời gian chờ; kênh thông báo có chuông; bằng chứng muộn/khôi phục đúng thiết bị; lỗi mới khi tắt âm; kênh im lặng; tắt âm không kích hoạt player; bắt đầu lại với lỗi đang mở phát âm, kể cả chu kỳ 2 phút. Kiểm tra sử dụng chu kỳ 30 giây thật cho suy luận và sao lưu/khôi phục dữ liệu/proxy giả lập.
- Player được kiểm tra bằng `isPlaying()` trên Android và dấu mốc khởi chạy trong service. Giả lập không có loa vật lý; kết quả này **không chứng minh tiếng chuông đã nghe được trên Xiaomi**.

Các bài kiểm tra dùng dữ liệu tổng hợp; không đăng nhập lại web công ty trong thay đổi này. Kiểm chứng kết nối thật ở bản 0.6 vẫn là kết quả lịch sử, xem [biên bản 0.6](LIVE_REVIEW_0.6.md). Kiểm tra UI gọi cùng đường cập nhật của các chu kỳ; không ngồi chờ 120/300 giây để kiểm tra từng khung hình.

Lần chụp đầu sau đổi độ phân giải giả lập có hộp thoại **System UI isn't responding** của hệ thống, không phải lỗi app. Đã đóng hộp thoại và chụp lại trước khi lưu ảnh bàn giao. Không dùng ảnh bị che làm bằng chứng giao diện.

## Ảnh dữ liệu tổng hợp

Các ảnh sau chỉ minh họa bản build bằng fixture, không phải dữ liệu tàu/thiết bị thực:

- [Tổng quan có sự cố và hai đoạn tàu](screenshots/overview-0.8.png)
- [Tổng quan chưa ghi nhận lỗi](screenshots/overview-clear-0.8.png)
- [Sự cố](screenshots/incidents-0.8.png)
- [Danh sách đường ngang](screenshots/crossings-0.8.png)
- [Cài đặt](screenshots/settings-0.8.png)

## Cài và thử trên điện thoại

APK là bộ cài đầy đủ, cài mới trực tiếp hoặc cập nhật cùng applicationId/chứng chỉ các bản trước. Chữ ký SHA-256 phải là `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Bản sao khóa riêng không nằm trong Git, chủ dự án chưa xác nhận sao lưu ra ngoài cloud.

Sau cài, vào **Cài đặt → Thử chuông báo lỗi**; nếu chưa nghe, kiểm tra mức âm lượng Báo thức và chế độ Không làm phiền. Sau đó bật Chuông sự cố, lưu/bắt đầu theo dõi để kiểm tra lỗi thực và khóa màn hình. Cần chủ dự án kiểm chứng Xiaomi 14T Pro/HyperOS: âm thanh loa, chạy nền lâu, độ trễ, độ mượt, sơ đồ chuyển tiếp, tự kích và reset 00:00. Chưa coi là bản đã kiểm định để thay quy trình trực hiện có.
