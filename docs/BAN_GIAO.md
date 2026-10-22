# Bàn giao để tiếp tục ở tài khoản ChatGPT khác

Cập nhật: 23/10/2026, giờ Việt Nam. Tài liệu này ghi yêu cầu đã chốt và tình trạng thực tế, giúp tiếp tục dự án khi đổi phiên/tài khoản. Không chứa mật khẩu website hoặc khóa ký riêng.

## Bắt đầu ở phiên mới

1. Kết nối cùng repository GitHub `cuongthinkpad-ui/app-duong-ngang`, lấy nhánh `main`; nếu không có công cụ GitHub, tải mã nguồn ZIP từ GitHub và cung cấp cho phiên mới.
2. Đọc tài liệu này, `README.md`, `docs/LICH_SU_TRAO_DOI.md` và `docs/UI_REVIEW_0.7.md` trước khi thay đổi. Tài liệu 0.1–0.6 là lịch sử; các quyết định 0.7 và yêu cầu mới của chủ dự án được ưu tiên.
3. Kiểm tra mã nguồn/commit thực tế. Bản ứng dụng mới nhất khi viết tài liệu là **0.7.0**, versionCode **7**, commit mã ứng dụng `103f51b79aa086bbc503a3dc1ee34d1e7fe713f6`. Các commit tài liệu sau đó không phải bản APK mới.
4. Tài khoản ChatGPT mới cần được cấp quyền truy cập GitHub riêng. Lịch sử chat, bộ nhớ ChatGPT, cache build và cấu hình cloud không tự chuyển theo repository.
5. Tài khoản/mật khẩu website được chủ dự án nhập trực tiếp trong app hoặc cung cấp riêng khi cần kiểm thử. Không yêu cầu gửi lại toàn bộ cuộc trò chuyện và không chép mật khẩu vào mã nguồn.

Tin nhắn chủ dự án có thể sao chép vào cuộc trò chuyện mới:

> Tiếp tục app Android Đường Ngang tại https://github.com/cuongthinkpad-ui/app-duong-ngang, nhánh main. Hãy đọc AGENTS.md, docs/BAN_GIAO.md, docs/LICH_SU_TRAO_DOI.md, README.md và biên bản kiểm tra mới nhất trước. Điểm bàn giao hiện tại là bản 0.7. Giữ các yêu cầu đã chốt, kiểm tra mã nguồn hiện có và tiếp tục từ đó. Phân biệt rõ phần đã thử trên giả lập và phần chưa được xác minh trên Xiaomi thật. Yêu cầu tiếp theo của tôi là: [ghi việc muốn làm].

## Mục tiêu và trạng thái

Chủ dự án: Cao Xuân Cường, công tác theo dõi đường ngang cảnh báo tự động. App Android dùng tài khoản chỉ có quyền xem website để đọc sự kiện và cảnh báo trên điện thoại, cài APK cho nhiều người. Mỗi người có thể dùng URL/ID/mật khẩu riêng; tuyến thay đổi theo tài khoản.

- Website đã thử: `http://visitec.vn:7447/`, trang báo cáo ASP.NET `Default.aspx`, cần đăng nhập. Không chỉ hỗ trợ một danh sách 13 điểm cố định.
- Đây là bản thử có chức năng thật. Đã đăng nhập/đọc web thật, thử service và UI trên giả lập; chưa được chủ dự án xác nhận đủ độ tin cậy để dùng trực chính thức trên Xiaomi 14T Pro.
- APK: `downloads/DuongNgang-0.7.apk`; checksum: `downloads/SHA256SUMS`. Giao file qua GitHub, chọn **Download raw file**. Người dùng từng không tải được artifact trực tiếp từ workspace, kể cả ZIP.
- Play Protect từng chặn bản APK ban đầu; sau đó người dùng cài được. Không coi cảnh báo đã được xử lý cho mọi máy, không hướng dẫn vô hiệu hóa bảo vệ như mặc định.

## Nguyên lý thiết bị đã chốt

Lý trình tăng từ Bắc vào Nam. Bắc là phía lẻ, Nam là phía chẵn.

- CB1/CB2 ở phía Bắc, cách đường ngang hơn 1 km, hai cảm biến cách nhau khoảng 1 m. CB3/CB4 ở đường ngang. CB5/CB6 ở phía Nam, cũng cách hơn 1 km và cách nhau khoảng 1 m.
- Tàu **lẻ**: CB1/2 → CB3/4 → CB5/6, Bắc → Nam, tăng km. Tàu **chẵn** đi ngược lại.
- Đuôi tàu qua CB3/4 thì cần chắn nâng; đường ngang vẫn còn chiếm dụng. Khi tàu qua đủ cảm biến hai đầu, khoảng một phút sau web mới báo **“Khôi phục hệ thống”**. Chỉ bản tin đó của đúng điểm mới bỏ xanh toàn khu vực; không dùng cần chắn nâng hoặc đồng hồ để tự bỏ xanh.
- Sơ đồ hiển thị các điểm/đoạn web báo chiếm dụng, nhiều đoạn và hướng cùng lúc. Không nhận diện danh tính đoàn tàu, không suy ra số tàu từ số điểm xanh, không tự nội suy GPS.
- Mọi tên điểm có hậu tố trong ngoặc là **ga**, nhãn sơ đồ dùng đúng chữ trong ngoặc (Vi, YX, CS, MLy, QHa…). Ga không phải đường ngang và ngắt đối chiếu liền kề ở cả hai phía. Không lấy bản tin ga làm bằng chứng tàu vì tàu có thể nằm trong ga lâu hoặc được tàu khác vượt.

Tuyến tài khoản đầu tiên, từ Bắc xuống Nam: 312+111, 313+456, 313+800, 314+550, 319+200 (Vi), 320+445, 320+957, 321+780, 327+327, 328+325, 330+00 (YX), 332+350, 334+830. **11 đường ngang và 2 điểm ga**. Số km đầu là **312+111**; có tin nhắn cũ gõ nhầm 312+11.

## Lỗi, khôi phục, số trục và tự kích

- Dòng màu đỏ trên web là lỗi: hiển thị **nguyên văn**, giữ rõ chuông/đèn/cần chắn/cảm biến số mấy. Chuông báo “hỏng”, không tự đổi thành “mất trạng thái”; cách nói mất trạng thái thường là cần chắn.
- Ghép khôi phục đúng thiết bị. Chuông 2 khôi phục không xóa chuông 1; điện áp cảm biến phục hồi không xóa lỗi cảm biến hỏng. “Khôi phục hệ thống” không xóa mọi lỗi phần cứng. Bản tin khôi phục cần chắn chung có thể áp dụng các cần chắn tương ứng.
- Sáu nhóm chi tiết: Chuông, Đèn, Cần chắn, Cảm biến, Kết nối, Điện áp. Không thêm camera.
- Chỉ một điều khiển bật/tắt chuông chung, không nút lặp theo từng lỗi, không nhắc lại định kỳ hoặc xác nhận đã xem. Tắt chuông vẫn giữ lỗi đỏ.
- **Yêu cầu mới nhất 0.7: reset khối sự cố sau 00:00 giờ Việt Nam**. Không mang lỗi hôm trước sang Tổng quan; giữ lỗi mới sau 00:00. Không xóa kho lịch sử hoặc chiếm dụng tàu. Reset không được ghi là thiết bị đã khôi phục. Ngày mặc định ở tab Sự cố theo ngày mới; nếu chủ động chọn ngày cũ để tra cứu thì giữ lựa chọn đó.
- Không cần bày các cột Status/số điện áp ở Tổng quan. Các bản tin điện áp đỏ vẫn phải báo lỗi.
- CB1–CB6 ở dòng khôi phục: số không có V là số trục; bản tin đo điện áp không dùng để so trục. Chọn số xuất hiện nhiều nhất làm chuẩn, ghi riêng từng CB thừa/thiếu, có thể đồng thời thừa và thiếu. Khi không có chuẩn duy nhất ghi đúng “cảm biến đếm trục không đồng nhất, lỗi”. Chỉ ghi chú, không phát chuông lỗi từ ghi chú này.
- Tự kích: một đường ngang báo tàu mà chưa có lượt tương ứng tại điểm liền kề phía trước theo hướng chạy thì nghi tự kích; bỏ đối chiếu qua ga và đầu/cuối tuyến. Ví dụ 313+456 báo tàu lẻ, thiếu lượt phía Bắc 312+111; 321+780 báo tàu chẵn, thiếu lượt phía Nam 327+327. Không yêu cầu điểm phía trước còn đang chiếm dụng: cần lịch sử lượt tàu tương ứng.
- Bản hiện tại dùng chữ **“Nghi tự kích”**, chờ ít nhất một chu kỳ/tối thiểu 30 giây để nhận bản tin chậm. Chỉ xét lượt mới, không phát lại suy luận lịch sử. Có bằng chứng đến muộn thì bỏ riêng bản tin suy luận, không xóa bản tin gốc. Chỉ thông báo tên điểm/sự kiện, không trình bày lý do dài. Cần kiểm chứng suy luận ngoài hiện trường.

## Giao diện cần giữ

- Xanh dương/navy, đơn giản và chuyên nghiệp, biểu tượng đầu tàu trắng dùng thống nhất trong app và launcher.
- Đầu trang: “Giám sát Đường ngang”; dòng đơn vị chính xác: **“Công ty CP TTTH Đường sắt Vinh”**.
- Sơ đồ nhỏ, đường thẳng có các mốc đường ngang/ga ở trên cùng. Đoạn có tàu xanh lá, tên hướng tàu nằm trên mũi tên. Không thêm “Lý trình tăng” hoặc chú giải hướng cố định bên dưới. Dữ liệu cũ có dấu hiệu riêng, không giả là dữ liệu mới.
- Bên dưới là khối Tàu gần nhất riêng, rồi Sự cố nền đỏ nhạt. Dòng thông tin theo thứ tự **Giờ – Đường ngang – Sự kiện/lỗi**. Sự cố mới nhất trước theo thời điểm phát sinh, không theo lúc khôi phục.
- Tổng quan vừa màn hình, không cần cuộn dọc, số dòng/đệm thích ứng với chiều cao; không ép cố định ba dòng. Tuyến dài có thể cuộn ngang.
- Cập nhật 30 giây / 2 phút / 5 phút phải giữ root, sơ đồ, khung tàu và khung sự cố; không thay toàn bộ content view rồi chờ dựng lại gây chớp trắng. Cùng view sơ đồ phải cập nhật lại mảng chiếm dụng, tránh hết chớp nhưng tàu bị đứng.
- Phạm vi “từ đường ngang đến đường ngang” chỉ lọc danh sách, sự cố và thông báo. **Không cắt ngắn sơ đồ** và không bỏ dữ liệu phía trước dùng suy luận.
- Thanh dưới: Tổng quan / Đường ngang / Sự cố / Cài đặt. Không khối lịch sử riêng trên Tổng quan. Tab Sự cố có chọn ngày, Tất cả/Chưa khôi phục/Đã khôi phục; tick xanh khi có bản tin phục hồi tương ứng.
- Cài đặt: URL/ID/mật khẩu, phạm vi, chu kỳ, bật/tắt chuông, chạy nền. URL mới điền sẵn `http://`, vẫn sửa/xóa/thay HTTPS được, giữ địa chỉ đã lưu.
- Bỏ nút xem dữ liệu minh họa và đoạn chữ giải thích cuối Cài đặt. Khởi động vào theo dõi thực. Hàm samples còn trong nguồn phục vụ kiểm thử/lịch sử, không khôi phục lại công tắc preview cho người dùng.
- **“Designed by Cao Xuân Cường” chỉ ở Cài đặt**, cỡ nhỏ nhưng đọc được, không ở các màn hình khác.

## Mã nguồn và build

Android native Java, package/applicationId `vn.duongngang.monitor`; minSdk 26, targetSdk 34, compileSdk 35. Android Gradle Plugin 8.7.3, Gradle 8.9, JDK 21 đã dùng; Java source 17 và core library desugaring. Jsoup 1.18.3, SQLite, Android Keystore AES-GCM. Không có Gradle wrapper trong repo hiện tại.

| Tệp | Vai trò |
|---|---|
| `app/src/main/java/vn/duongngang/monitor/MainActivity.java` | Giao diện, cập nhật tại chỗ, sơ đồ, chọn phạm vi/ngày, cài đặt |
| `MonitorService.java` cùng thư mục | Foreground service, đọc định kỳ, thông báo, timer reset ngày |
| `WebSource.java` | Đăng nhập ASP.NET, bộ lọc/ngày, phân trang, đọc chồng |
| `Rules.java` | Tuyến/ga/hướng, chiếm dụng, đúng thiết bị, trục, tự kích, mốc ngày |
| `Store.java` | Dữ liệu theo URL/ID, khóa mật khẩu, cache lỗi/chiếm dụng, reset ngày |
| `tests/`, `app/src/androidTest/` | Kiểm tra quy tắc/parser/web/lưu trữ/UI |
| `downloads/` | APK bàn giao và SHA-256 |

Môi trường cloud cũ đặt repo tại `/workspace/app-duong-ngang`, toolchain tại `/workspace/toolchains`; **các thư mục toolchain/caches không nằm trong Git**. `scripts/setup-cloud.sh` cài JDK/Gradle/SDK và build, nhưng đường dẫn cloud được viết cố định. Khi clone ở nơi khác cần chỉnh đường dẫn hoặc dùng Android Studio/Gradle với SDK và JDK tương ứng. Lượt build mới cần mạng để tải dependencies, chỉ dùng `--offline` khi cache đã đủ.

```sh
# Trong cloud đã chuẩn bị đúng đường dẫn:
source scripts/env.sh
./scripts/build-cloud.sh assembleDebug assembleDebugAndroidTest lintDebug
./scripts/test-rules.sh
./scripts/test-parser.sh
# Giả lập riêng, dữ liệu thử không phải tài khoản vận hành:
./scripts/test-android-route.sh
./scripts/test-android-ui.sh
python3 scripts/test-monitoring-e2e.py
```

Tăng versionCode lớn hơn 7 khi xuất APK cập nhật mới. Giữ applicationId và chữ ký nếu muốn cài đè. Không đưa URL/ID/mật khẩu thử vào nguồn/APK, không đăng HTML đăng nhập hoặc ảnh dữ liệu vận hành vào repo.

## Khóa ký APK cần sao lưu riêng

APK hiện dùng **khóa debug của môi trường cloud này**; khóa riêng không được commit. Mã nguồn và APK trên GitHub **không chứa bản sao khóa riêng để ký lại**. Môi trường/tài khoản mới tạo khóa khác sẽ không cài đè được APK hiện có, dù mã nguồn giống hệt.

- Vị trí khóa ở môi trường hiện tại: `/workspace/toolchains/android-home/debug.keystore` (đã kiểm tra tồn tại khi bàn giao).
- SHA-256 chứng chỉ đang dùng: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.
- Cần sao lưu khóa vào nơi riêng do chủ dự án giữ, kèm thông tin sử dụng khóa; không đưa khóa thô lên repository công khai. Tài liệu này **chưa thực hiện xuất/lưu khóa ra ngoài cloud**.
- Phiên mới cần khóa cũ để ký bản cập nhật cài đè. Nếu không có khóa, vẫn phát triển được mã nguồn, nhưng phải giải quyết phương án cài lại/chuyển sang khóa phát hành và tác động dữ liệu với chủ dự án.

## Kiểm chứng và việc còn lại

Bản 0.7: build/lint thành công, 92 kiểm tra quy tắc, 26 kiểm tra lưu trữ, 21 kiểm tra UI, 7 kiểm tra service cục bộ đạt. Chi tiết ở `docs/UI_REVIEW_0.7.md`. Kiểm tra UI gọi cùng callback của các chu kỳ, không ngồi chờ cả 30/120/300 giây. Kiểm tra reset dùng mốc thời gian tổng hợp; không chứng minh điện thoại thật chạy liên tục qua đêm.

Bản 0.6 đã đăng nhập Visitec thật, đọc đủ ngày hiện tại/ngày trước qua khoảng 20 trang, lấy đủ 13 điểm, thử đọc tăng thêm; giả lập cập nhật được 5 lượt khi tắt màn hình trong 80 giây. Xem `docs/LIVE_REVIEW_0.6.md`. Chặn HTTP 403 ghi trong tài liệu cũ đã hết ở phiên thử 0.6, không áp đặt kết quả đó cho phiên mới; mạng mới vẫn phải kiểm tra lại.

Cần người dùng xác nhận trên **Xiaomi 14T Pro/HyperOS**: còn chớp trắng hay không, bố cục/cỡ chữ, chạy nền lâu khi khóa màn hình, độ trễ chuông, reset 00:00, sự cố/khôi phục và tự kích so với thực tế. Chưa có phản hồi xác nhận đầy đủ bản 0.7. Chưa tự chạy sau khởi động máy. Web không cung cấp ID sự kiện độc lập nên dòng hoàn toàn giống nhau cùng giây được gộp; không cam kết tương thích mọi cấu trúc website.

Công việc cuối phiên là lưu tài liệu bàn giao này để đổi tài khoản ChatGPT vẫn tiếp tục được. Không có yêu cầu sửa app nào khác đang bỏ dở tại thời điểm viết.
