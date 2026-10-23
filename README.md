# Đường Ngang — Android 0.14

**Đổi phiên/tài khoản ChatGPT:** đọc [Bản bàn giao và cách tiếp tục](docs/BAN_GIAO.md) và [Nhật ký trao đổi, yêu cầu đã chốt](docs/LICH_SU_TRAO_DOI.md). [Sao lưu khóa ký APK](docs/SAO_LUU_KHOA_KY.md).

Ứng dụng theo dõi đường ngang CBTĐ, giao diện tiếng Việt navy, xanh cyan và xanh lá. [Tải APK](downloads/README.md). Không nhúng tài khoản hay mật khẩu thử nghiệm vào mã nguồn hoặc APK.

## Thay đổi 0.14

- Áp dụng mẫu Stitch vào app Android hiện có: nền navy tối, cyan, thẻ có viền, nút chính chuyển màu cyan–mint; căn lại cả bốn tab và màn chi tiết. Giữ Tổng quan vừa chiều cao màn hình, không chép những khoảng trống lớn hoặc thông số giả từ bản thiết kế.
- Logo sáng dịu theo chu kỳ 2,8 giây; vòng đếm lỗi có hiệu ứng pulse; đoạn tàu có quầng sáng tĩnh và nhãn hướng liền mũi tên. Dừng pulse khi mất focus/chạy nền và tôn trọng tùy chọn tắt hiệu ứng của Android. Không dùng hiệu ứng để tự di chuyển tàu hoặc trì hoãn cảnh báo.
- Badge LIVE chỉ hiện khi phiên đang theo dõi, dữ liệu còn mới; dừng/cũ/chưa có dữ liệu có nhãn riêng. Danh sách đường ngang thêm bộ lọc và số lượng theo dữ liệu thật; lỗi nguồn màu đỏ nếu web báo đỏ. Không bịa góc cần chắn, pin, hướng cố định hoặc mức độ nghiêm trọng.
- Giữ nguyên đọc web, quy tắc tàu, phân trang, khôi phục, chuông sáu hồi, hai ngưỡng đếm trục và ngoại lệ Km 320+445; không đổi nền tảng, tài khoản, kho dữ liệu hoặc khóa ký.

[Kiểm tra và ảnh 0.14](docs/UI_REVIEW_0.14.md).

## Thay đổi 0.13 (lịch sử)

- **Loại Km 320+445 khỏi toàn bộ đối chiếu sáu CB, ghi chú, thống kê và cảnh báo trục** vì có nhiều hơn sáu cảm biến và cơ chế đếm riêng. Dòng đỏ thật trên website, sơ đồ tàu và suy luận tự kích tại điểm này vẫn theo quy tắc cũ.
- Khi cài đè, lọc ngay cảnh báo trục suy ra cũ của điểm này và dọn bản lưu dẫn xuất trên mọi ngày, hủy thông báo liên quan. Giữ bản tin web gốc, tài khoản, cài đặt, lịch sử thật và dấu chống báo lặp của các CB khác.
- Tối ưu chuyển tab: đọc dữ liệu ngày/tuyến/lỗi một lần cho mỗi lần dựng hoặc cập nhật giao diện; phân nhóm sự kiện theo đường ngang một lần; tái sử dụng mẫu nhận diện thiết bị. Bộ dữ liệu giao diện được làm mới khi đổi tab, nhận cập nhật, đổi ngày/tài khoản/phạm vi. Giữ chu kỳ đọc web, khôi phục, sơ đồ và chuông.

[Kiểm tra bản 0.13](docs/UI_REVIEW_0.13.md).

## Thay đổi 0.12 (lịch sử)

- Cùng một CB tại cùng đường ngang: chỉ cộng lần lệch **từ 3 trục trở lên**; đến **lần thứ 6 trong ngày** cảnh báo đỏ và thông báo một lần/ngày cho CB đó. Mở lại app, tải lại hoặc tải bù dữ liệu không phát lại cảnh báo này.
- Một lần thừa/thiếu **từ 10 trục** cảnh báo ngay, kể cả CB đã báo ngưỡng số lần. CB khác có bộ đếm và thông báo riêng. Cùng lần chạm cả hai ngưỡng thì gộp một cảnh báo.
- Cảnh báo có đúng CB, số trục thừa/thiếu, chuẩn chung và số đếm, dùng chuông sáu hồi theo cài đặt. Giữ cảnh báo tới 00:00 giờ Việt Nam; lịch sử vẫn lưu. Lượt đếm đúng sau đó không xóa thống kê lỗi trong ngày.
- Chi tiết đường ngang chỉ thêm một mục **Đếm trục lỗi**, thống kê các CB đã đạt ngưỡng cảnh báo trong ngày; chưa đủ ngưỡng thì không hiện mục này. Không thêm trang/bộ lọc đếm trục riêng. Lệch 1–2 trục vẫn là ghi chú, không cộng bộ đếm. Bản tin điện áp hoặc không xác định được chuẩn chung không tạo cảnh báo suy ra CB.

[Kiểm tra bản 0.12](docs/UI_REVIEW_0.12.md).

## Thay đổi 0.11 (lịch sử)

- Chuông lỗi phát **6 hồi** (12 giây), giữ âm cũ và nút tắt chuông chung.
- Đường ngang đang **Nghi tự kích** nhận bản tin **“Ấn nút Phục hồi”**: khép cảnh báo, đánh dấu đã khôi phục trong Sự cố và tắt đúng đoạn sáng của lượt tự kích. Giữ lỗi thiết bị khác và các tàu độc lập.
- Giữ dấu lượt đã kết thúc để bản tin tàu tới muộn không làm sáng lại. Tự chuyển cache khi cập nhật, giữ tài khoản/cài đặt/lịch sử.

[Kiểm tra bản 0.11](docs/UI_REVIEW_0.11.md).

## Thay đổi 0.10 (lịch sử)

- Sửa lỗi giữ các đoạn/hướng tàu cũ. Bản tin **tàu đã qua** đóng lượt ở đoạn trước và chuyển sang đoạn sau ngay khi app nhận được, kể cả khi thiếu bản tin tiếp cận ở điểm đó; không đợi khôi phục.
- Lưu liên kết lượt giữa hai điểm và dấu kết thúc để bản tin muộn không tạo lại tàu cũ. Bằng chứng hướng ngược tại một điểm loại bỏ các đoạn cũ mâu thuẫn chạm điểm đó, giữ các tàu độc lập ở nơi khác.
- Khi nâng cấp, tự dựng lại cache sơ đồ từ bản tin đã lưu, giữ tài khoản/cài đặt/lịch sử. Trong lúc chuyển cache chỉ ghi “Đang cập nhật sơ đồ”.
- Gộp tên hướng và mũi tên trong một nhãn; nhiều đoạn thì rút gọn nhãn và bố trí hàng để không tách chữ/mũi tên hoặc bị cắt phía trên.
- Đã đọc web thật và phát lại ở mốc ảnh chủ dự án gửi: thuật toán 0.9 cho 8 đoạn (5 lẻ/3 chẵn), bản sửa cho 1 đoạn chẵn. Chỉ lưu kết quả tổng hợp, không đăng dữ liệu vận hành hoặc thông tin đăng nhập.

[Kiểm tra bản 0.10](docs/UI_REVIEW_0.10.md).

## Thay đổi 0.9 (lịch sử)

- Đổi dòng trống ở Sự cố thành **“Không có sự cố”**.
- Mỗi lượt tàu có một đoạn sáng: tiếp cận thì sáng đoạn phía tàu đến; báo tàu đã qua thì chuyển sang đoạn phía tàu đi. Điểm trước khôi phục và điểm sau bắt đầu báo tàu đều không làm đoạn giữa hai điểm mất đi; chỉ chuyển tiếp khi tàu qua điểm sau. Ví dụ tàu lẻ: mép Bắc 312+111 → 312+111–313+456 → 313+456–313+800.
- Đoạn đường ngang–ga vẫn sáng lúc tàu vào ga tới khi đường ngang giáp ga khôi phục. Khi đường ngang báo tàu từ ga ra, sáng đoạn ga–đường ngang đó. Không nối xuyên qua ga hoặc giữ tàu theo bản tin ga cả ngày.
- Giữ màu tối, cập nhật trong khung hiện có, chuông thực và điều kiện tự kích chỉ ở chu kỳ 30 giây của bản trước.

[Kiểm tra bản 0.9 và ảnh từng giai đoạn tàu](docs/UI_REVIEW_0.9.md).

## Thay đổi 0.8 (lịch sử)

- Đổi Tổng quan, danh sách/chi tiết đường ngang, Sự cố và Cài đặt theo mẫu giao diện tối. Tổng quan tự chia chiều cao, cập nhật trong các khung hiện có. Thẻ tóm tắt ghi số điểm theo dõi hoặc lỗi chưa khôi phục; không kết luận thiết bị an toàn chỉ vì chưa thấy bản tin đỏ.
- Giữ xanh đoạn **giữa hai đường ngang liền kề** khi tàu đã qua điểm trước, kể cả điểm đó đã khôi phục, cho tới khi điểm sau báo tàu cùng hướng. Không tô phần rìa ngoài hai mốc, không nối qua ga hoặc ngoài tuyến. Nhiều đoạn và hai hướng được hiển thị đồng thời; bản tin điểm sau đến trước lúc điểm trước khôi phục không làm đoạn cũ sáng trở lại.
- **Nghi tự kích chỉ áp dụng chu kỳ 30 giây**. Chọn 2 phút/5 phút thì bỏ suy luận hiện tại và ngừng tạo suy luận mới; lỗi đỏ thật từ website vẫn được cảnh báo.
- Thêm âm báo riêng của app cho lỗi mới và lỗi đang mở khi bắt đầu phiên theo dõi. Có nút **Thử chuông báo lỗi** trong Cài đặt; dùng âm lượng **Báo thức** của điện thoại. Tắt chuông chung giữ nguyên lỗi đỏ và dừng âm đang phát. Không có nhắc lại định kỳ.

[Kiểm tra bản 0.8 và ảnh giao diện](docs/UI_REVIEW_0.8.md).

## Thay đổi 0.7

- Cập nhật định kỳ trong cửa sổ hiện tại: giữ khung sơ đồ, tàu gần nhất và sự cố; không thay toàn màn hình. Sơ đồ đổi trạng thái trên cùng vùng vẽ và giữ vị trí cuộn ngang. Dashboard có dữ liệu ngay khung hình đầu, không chờ callback để dựng lại.
- Reset khối sự cố sau 00:00 giờ Việt Nam; không mang lỗi hôm trước sang Tổng quan. Lưu lịch sử ngày cũ và giữ chiếm dụng tàu qua nửa đêm.
- Sự cố xếp theo thời điểm phát sinh, mới nhất trước, cả Tổng quan và tab Sự cố; dữ liệu lưu cũ cũng được sắp lại khi đọc.
- Bỏ nút xem minh họa và đoạn giải thích cuối Cài đặt, mở theo dõi thực khi khởi động. Chữ ký vẫn chỉ ở Cài đặt.
- Đơn vị dưới tiêu đề: “Công ty CP TTTH Đường sắt Vinh”.
- Ô web điền sẵn `http://`, sửa/xóa hoặc thay bằng `https://` bình thường; địa chỉ đã lưu được giữ nguyên.

## Theo dõi

- Nhập website, ID, mật khẩu trong Cài đặt. Tuyến được lấy từ danh sách điểm của tài khoản, sắp xếp km từ Bắc xuống Nam. Điểm có ngoặc là ga, nhãn dùng đúng chữ trong ngoặc; ga ngắt đối chiếu đường ngang liền kề ở hai phía.
- Chọn theo dõi từ đường ngang đến đường ngang. Phạm vi lọc danh sách, sự cố và thông báo; sơ đồ vẫn giữ đủ tuyến. Đổi URL/ID xóa trạng thái của tài khoản cũ.
- Sơ đồ luôn giữ một đoạn sáng cho mỗi lượt tàu đang theo dõi: đoạn đến trước khi qua đường ngang, đoạn đi sau bản tin đã qua; nhiều đoạn/hai hướng đồng thời. Tàu lẻ đi tăng km, tàu chẵn đi giảm km. Đoạn giữa hai đường ngang không tắt khi điểm trước khôi phục hoặc điểm sau mới tiếp cận. Đoạn giáp ga tắt theo khôi phục của đường ngang đó. Trạng thái chiếm dụng thiết bị vẫn chỉ kết thúc theo “Khôi phục hệ thống”. Đây là biểu diễn từ bản tin, không phải vị trí GPS, danh tính hoặc số đoàn tàu.
- Mọi lỗi đỏ giữ nguyên chữ trên web, gồm tên/số thiết bị. Khôi phục chỉ xóa lỗi tương ứng; khôi phục hệ thống không xóa lỗi phần cứng. Sáu nhóm chi tiết: Chuông, Đèn, Cần chắn, Cảm biến, Kết nối, Điện áp.
- Lưu lỗi chưa khôi phục trong ngày; khối hiện tại reset ở 00:00 giờ Việt Nam, lịch sử ngày cũ vẫn tra cứu được. Chiếm dụng tàu giữ qua nửa đêm tới bản tin khôi phục đúng điểm. Sau gián đoạn qua ngày hoặc trên 24 giờ, mốc theo dõi lỗi có thể được đặt lại với “Chưa ghi nhận lỗi mới”; đây không phải bản tin thiết bị phục hồi.
- Ở chu kỳ **30 giây**, suy luận “Nghi tự kích” từ lượt tàu mới thiếu lượt tương ứng ở đường ngang phía trước theo hướng chạy, có chờ ít nhất một chu kỳ. Không áp dụng qua ga hay đầu/cuối tuyến, không áp dụng chu kỳ 2 phút/5 phút. Bản tin phía trước tới muộn sẽ bỏ suy luận; bản tin gốc được giữ. Đây là suy luận theo dữ liệu web, cần đối chiếu hiện trường.
- CB1–CB6 ở bản tin khôi phục hệ thống được đối chiếu số trục theo số xuất hiện nhiều nhất. Ghi chú thừa/thiếu riêng từng CB; hòa chuẩn ghi “cảm biến đếm trục không đồng nhất, lỗi”. Ghi chú dưới ngưỡng không phát chuông; hai ngưỡng bản 0.12 được cảnh báo như lỗi. Riêng Km 320+445 không áp dụng phần đối chiếu trục. Không coi bản tin đo điện áp là số trục.

## Giao diện và thông báo

Tổng quan gồm sơ đồ, tàu gần nhất và sự cố, tự chia chiều cao để vừa màn hình. Chạm để xem danh sách/chi tiết đầy đủ. Thanh dưới: Tổng quan / Đường ngang / Sự cố / Cài đặt. Mục Sự cố chọn ngày, lọc chưa/đã khôi phục, tick xanh khi có bản tin khôi phục tương ứng. Chỉ một nút tắt chuông chung; tắt chuông vẫn giữ lỗi đỏ. **“Designed by Cao Xuân Cường” chỉ hiển thị trong Cài đặt.**

Foreground service đọc theo chu kỳ 30 giây / 2 phút / 5 phút, có thông báo thường trực. Khi mới kết nối, tải ngày trước và ngày hiện tại, đọc hết phân trang rồi kiểm tra lại đầu trang; sau đó đọc chồng tới bản tin đã biết và làm mới toàn bộ định kỳ. Phiên hết hạn được đăng nhập lại; sai tài khoản/mật khẩu dừng dịch vụ để tránh thử liên tục. Tải lịch sử riêng không phát cảnh báo.

Khi dữ liệu gián đoạn/cũ, sơ đồ giữ trạng thái cuối và đổi màu cảnh báo kèm thời điểm cập nhật. Chu kỳ đọc web không phải dữ liệu liên tục tại hiện trường. Các bản ghi hoàn toàn giống nhau cùng giây được gộp do web không cung cấp ID sự kiện độc lập.

## Kiểm chứng và giới hạn

Đã đăng nhập Visitec thật, đọc đủ ngày hiện tại/ngày trước và kiểm tra lượt đọc tăng thêm. App Android trên giả lập đã đồng bộ dữ liệu thật và có 5 lượt cập nhật khi tắt màn hình trong 80 giây. Kiểm tra quy tắc, phân trang, lưu trữ và thông báo dùng thêm dữ liệu mô phỏng; xem [biên bản 0.6](docs/LIVE_REVIEW_0.6.md).

Cần thử khóa màn hình lâu, chế độ pin HyperOS và độ trễ thông báo trên Xiaomi 14T Pro trước khi dùng trực. Cài đặt có lối mở thông tin ứng dụng để chỉnh pin/thông báo. Chưa tự bật sau khởi động máy. Không bảo đảm chu kỳ tuyệt đối khi Android hạn chế chạy nền, không bảo đảm mọi website có cấu trúc khác đều tương thích. App chỉ giám sát, không điều khiển thiết bị hay thay thế quy trình trực.

Mật khẩu mã hóa bằng Android Keystore, tắt sao lưu ứng dụng; website HTTP vẫn truyền dữ liệu không mã hóa trên mạng. APK dùng chữ ký phát triển để thử và cập nhật cùng các bản trước.

## Build

JDK 21, Gradle 8.9, Android SDK 35; Android tối thiểu 8.0 (API 26).

```sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh --offline assembleDebug assembleDebugAndroidTest lintDebug
# Với giả lập riêng đang chạy:
./scripts/test-android-route.sh
./scripts/test-android-ui.sh
./scripts/test-telemetry-ui.sh
python3 scripts/test-monitoring-e2e.py
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. Script kiểm tra service chỉ chấp nhận giả lập, sao lưu/khôi phục dữ liệu ứng dụng và proxy.

[Kiểm tra giao diện 0.7](docs/UI_REVIEW_0.7.md) lưu các kiểm tra chống chớp và reset ngày trước khi đổi giao diện.
