# Bàn giao để tiếp tục ở tài khoản ChatGPT khác

Cập nhật: 25/10/2026, giờ Việt Nam. Tài liệu này ghi yêu cầu đã chốt và tình trạng thực tế, giúp tiếp tục dự án khi đổi phiên/tài khoản. Không chứa mật khẩu website hoặc khóa ký riêng.

## Bắt đầu ở phiên mới

1. Kết nối cùng repository GitHub `cuongthinkpad-ui/app-duong-ngang`, lấy nhánh `main`; nếu không có công cụ GitHub, tải mã nguồn ZIP từ GitHub và cung cấp cho phiên mới.
2. Đọc tài liệu này, `README.md`, `docs/LICH_SU_TRAO_DOI.md` và `docs/UI_REVIEW_0.17.md` trước khi thay đổi. Tài liệu 0.1–0.16 là lịch sử; yêu cầu mới nhất của chủ dự án được ưu tiên, các quy tắc không được sửa vẫn giữ.
3. Kiểm tra mã nguồn/commit thực tế. Bản ứng dụng hiện tại là **0.17.0**, versionCode **17**, đã đóng gói và kiểm tra. Chủ dự án đã duyệt bổ sung ga Nghi Long riêng ID Chosy; tiếp tục phát hành thông thường trên cùng kho GitHub. Khi tiếp tục, kiểm tra commit/APK thực tế trên main; không nhầm commit chỉ sửa tài liệu với bản APK mới.
4. Tài khoản ChatGPT mới cần được cấp quyền truy cập GitHub riêng. Lịch sử chat, bộ nhớ ChatGPT, cache build và cấu hình cloud không tự chuyển theo repository.
5. Tài khoản/mật khẩu website được chủ dự án nhập trực tiếp trong app hoặc cung cấp riêng khi cần kiểm thử. Không yêu cầu gửi lại toàn bộ cuộc trò chuyện và không chép mật khẩu vào mã nguồn.

Tin nhắn chủ dự án có thể sao chép vào cuộc trò chuyện mới:

> Tiếp tục app Android Đường Ngang tại https://github.com/cuongthinkpad-ui/app-duong-ngang, nhánh main. Hãy đọc AGENTS.md, docs/BAN_GIAO.md, docs/LICH_SU_TRAO_DOI.md, README.md và biên bản kiểm tra mới nhất trước. Điểm bàn giao hiện tại là bản 0.17. Giữ các yêu cầu đã chốt, kiểm tra mã nguồn hiện có và tiếp tục từ đó. Phân biệt rõ phần đã thử trên giả lập và phần chưa được xác minh trên Xiaomi thật. Yêu cầu tiếp theo của tôi là: [ghi việc muốn làm].

## Mục tiêu và trạng thái

Chủ dự án: Cao Xuân Cường, công tác theo dõi đường ngang cảnh báo tự động. App Android dùng tài khoản chỉ có quyền xem website để đọc sự kiện và cảnh báo trên điện thoại, cài APK cho nhiều người. Mỗi người có thể dùng URL/ID/mật khẩu riêng; tuyến thay đổi theo tài khoản.

- Website đã thử: `http://visitec.vn:7447/`, trang báo cáo ASP.NET `Default.aspx`, cần đăng nhập. Không chỉ hỗ trợ một danh sách 13 điểm cố định.
- Chủ dự án phản hồi bản 0.10 cơ bản đã chạy tốt; bản 0.11 bổ sung chuông sáu hồi và phục hồi tự kích theo yêu cầu. Đây là bản thử có chức năng thật. Đã đăng nhập/đọc web thật, thử service và UI trên giả lập; chưa được chủ dự án xác nhận đủ độ tin cậy để dùng trực chính thức trên Xiaomi 14T Pro.
- APK: `downloads/DuongNgang-0.17.apk`; checksum: `downloads/SHA256SUMS`. Đây là bản cài đầy đủ: cài mới trực tiếp hoặc cài đè cùng chữ ký để giữ cấu hình. Giao file qua GitHub, chọn **Download raw file**. Người dùng từng không tải được artifact trực tiếp từ workspace, kể cả ZIP.
- Play Protect từng chặn bản APK ban đầu; sau đó người dùng cài được. Không coi cảnh báo đã được xử lý cho mọi máy, không hướng dẫn vô hiệu hóa bảo vệ như mặc định.

## Nguyên lý thiết bị đã chốt

Lý trình tăng từ Bắc vào Nam. Bắc là phía lẻ, Nam là phía chẵn.

- CB1/CB2 ở phía Bắc, cách đường ngang hơn 1 km, hai cảm biến cách nhau khoảng 1 m. CB3/CB4 ở đường ngang. CB5/CB6 ở phía Nam, cũng cách hơn 1 km và cách nhau khoảng 1 m.
- Tàu **lẻ**: CB1/2 → CB3/4 → CB5/6, Bắc → Nam, tăng km. Tàu **chẵn** đi ngược lại.
- Đuôi tàu qua CB3/4 thì cần chắn nâng; đường ngang vẫn còn chiếm dụng. Khi tàu qua đủ cảm biến hai đầu, khoảng một phút sau web mới báo **“Khôi phục hệ thống”**. Trạng thái chiếm dụng thiết bị (`Rules.occupancies`) vẫn chỉ giải phóng bằng bản tin đó đúng điểm, không bằng cần chắn nâng hay đồng hồ.
- **Yêu cầu sửa rõ ở 0.9:** mỗi lượt tàu chỉ sáng một đoạn. Trước bản tin đã qua, tô đoạn phía tàu đến: tàu lẻ vào 312+111 thì mép Bắc 312+111, tàu chẵn thì phía Nam. Khi có “Tàu lẻ/chẵn đã qua đường ngang”, chuyển sang đoạn phía tàu đi. Tàu lẻ qua 312+111 → sáng 312+111–313+456; qua 313+456 → sáng 313+456–313+800. Khôi phục điểm trước không làm mất đoạn; điểm sau mới báo tiếp cận cũng giữ nguyên đoạn đến, chỉ chuyển tiếp khi báo đã qua. Không tô thêm hai mép quanh điểm, không tắt rồi chỉ tô chấm như 0.8. Nếu có bản tin điểm sau sớm thì chỉ giữ một đoạn ở bước xa nhất đã ghi nhận, không sáng thêm đoạn cũ phía sau.
- **Đoạn ga:** sau khi qua đường ngang hướng vào ga, sáng đoạn đường ngang–ga cho tới khôi phục của chính đường ngang đó rồi tắt. Khi đường ngang phía ra báo tàu từ ga, sáng đoạn ga–đường ngang, sau bản tin đã qua thì chuyển sang đoạn kế tiếp. Không nối xuyên ga; bản tin riêng trong ga không tạo hoặc giữ tàu cả ngày. Hai đầu tuyến có mép vào/ra ngắn chỉ trong giai đoạn tương ứng, không giữ sau khôi phục khi tàu đã ra ngoài vùng tài khoản.
- Nhiều lượt/hai hướng được hiển thị đồng thời, mỗi lượt một đoạn (các lượt cùng đoạn/hướng dùng chung nét xanh). `Rules.trackSegments` là quy tắc hình vẽ; `Rules.occupancies` vẫn là trạng thái phần cứng. Thiếu bản tin đã qua thì khôi phục chỉ nối tiếp khi đã biết hướng từ lượt báo tàu; khôi phục đơn lẻ không tự tạo hướng.
- **Sửa 0.10 sau phản hồi ảnh thực:** “tàu đã qua” cũng nối/đóng lượt phía trước khi thiếu bản tin tiếp cận. Lưu `successorId` và dấu kết thúc, không xóa sạch dấu lượt hoàn tất rồi tạo mới từ bản tin muộn. Hướng ngược mới tại một điểm đóng các đoạn cũ mâu thuẫn chạm điểm đó; không xóa tàu ở các đoạn độc lập. Cảm biến kề nhau báo ngược thứ tự được ghép khi lượt phía sau chưa được dùng và thời gian lệch không quá 2 phút; giới hạn này chỉ để đối chiếu báo đảo thứ tự, **không làm hết hạn đoạn tàu đang chờ**.
- Cache sơ đồ `travelVersion=3`: khi nâng cấp, dựng lại từ toàn bộ bản tin lưu được trước checkpoint và phát lại hai ngày gần nhất. Không dùng cache cũ trong lúc chuyển; chạy chuyển nền, giữ raw/tài khoản/lỗi. Mỗi Journey có `successorId`/`lastAt` và cờ `selfTriggered` đúng lượt; giữ số dấu hoàn tất có giới hạn thay vì tăng theo mọi lượt tàu. Nhãn hướng là một dòng liền chữ/mũi tên, rút gọn khi đông đoạn; giữ khoảng cách tối thiểu và cuộn ngang trên máy hẹp.
- Đây là biểu diễn liên tục từ bản tin, không phải GPS, danh tính hoặc số đoàn tàu. Đoạn chờ không tự mất theo thời gian; giữ qua nửa đêm/khởi động lại. Store có checkpoint và phát lại hai ngày gần nhất để tiếp nhận bản tin tới muộn. Không bảo đảm sửa đúng các bản tin bổ sung cũ hơn mốc checkpoint.
- Mọi tên điểm có hậu tố trong ngoặc là **ga**, nhãn sơ đồ dùng đúng chữ trong ngoặc (Vi, YX, CS, MLy, QHa…). Ga không phải đường ngang và ngắt đối chiếu liền kề ở cả hai phía. Không lấy bản tin ga làm bằng chứng tàu vì tàu có thể nằm trong ga lâu hoặc được tàu khác vượt.

Tuyến tài khoản đầu tiên, từ Bắc xuống Nam: 312+111, 313+456, 313+800, 314+550, 319+200 (Vi), 320+445, 320+957, 321+780, 327+327, 328+325, 330+00 (YX), 332+350, 334+830. **11 đường ngang và 2 điểm ga**. Số km đầu là **312+111**; có tin nhắn cũ gõ nhầm 312+11.

## Lỗi, khôi phục, số trục và tự kích

- Dòng màu đỏ trên web là lỗi: hiển thị **nguyên văn**, giữ rõ chuông/đèn/cần chắn/cảm biến số mấy. Chuông báo “hỏng”, không tự đổi thành “mất trạng thái”; cách nói mất trạng thái thường là cần chắn.
- Ghép khôi phục đúng thiết bị. Chuông 2 khôi phục không xóa chuông 1; điện áp cảm biến phục hồi không xóa lỗi cảm biến hỏng. “Khôi phục hệ thống” không xóa mọi lỗi phần cứng. Bản tin khôi phục cần chắn chung có thể áp dụng các cần chắn tương ứng.
- Sáu nhóm chi tiết: Chuông, Đèn, Cần chắn, Cảm biến, Kết nối, Điện áp. Không thêm camera.
- Chỉ một điều khiển bật/tắt chuông chung, không nút lặp theo từng lỗi, không nhắc lại định kỳ hoặc xác nhận đã xem. Tắt chuông vẫn giữ lỗi đỏ.
- **0.8 sửa phản hồi không nghe chuông:** app có `AlarmSound` dùng MediaPlayer và âm chime riêng (bản 0.11: **6 hồi trong 12 giây**, trước đó 3 hồi/6 giây), phát một lần khi có lỗi mới hoặc bắt đầu phiên theo dõi với lỗi hiện tại chưa khôi phục. Không phát lại chỉ vì quay về Activity khi service vẫn chạy; không nhắc định kỳ. Tắt âm chung dừng player, khôi phục hết lỗi đang báo/reset ngày/dừng service cũng dừng âm. Dùng luồng âm lượng Báo thức, không ép tăng âm lượng hay vượt Không làm phiền. Cài đặt có nút “Thử chuông báo lỗi”. Cần kiểm chứng loa Xiaomi thật.
- **Yêu cầu mới nhất 0.7: reset khối sự cố sau 00:00 giờ Việt Nam**. Không mang lỗi hôm trước sang Tổng quan; giữ lỗi mới sau 00:00. Không xóa kho lịch sử hoặc chiếm dụng tàu. Reset không được ghi là thiết bị đã khôi phục. Ngày mặc định ở tab Sự cố theo ngày mới; nếu chủ động chọn ngày cũ để tra cứu thì giữ lựa chọn đó.
- Không cần bày các cột Status/số điện áp ở Tổng quan. Các bản tin điện áp đỏ vẫn phải báo lỗi.
- CB1–CB6 ở dòng khôi phục: số không có V là số trục; bản tin đo điện áp không dùng để so trục. Chọn số xuất hiện nhiều nhất làm chuẩn, ghi riêng từng CB thừa/thiếu, có thể đồng thời thừa và thiếu. Khi không có chuẩn duy nhất ghi đúng “cảm biến đếm trục không đồng nhất, lỗi”. Ghi chú thường không phát chuông; hai ngưỡng cảnh báo mới của 0.12 áp dụng theo mục bên dưới.
- Tự kích: một đường ngang báo tàu mà chưa có lượt tương ứng tại điểm liền kề phía trước theo hướng chạy thì nghi tự kích; bỏ đối chiếu qua ga và đầu/cuối tuyến. Ví dụ 313+456 báo tàu lẻ, thiếu lượt phía Bắc 312+111; 321+780 báo tàu chẵn, thiếu lượt phía Nam 327+327. Không yêu cầu điểm phía trước còn đang chiếm dụng: cần lịch sử lượt tàu tương ứng.
- Bản hiện tại dùng chữ **“Nghi tự kích”**, **chỉ bật khi chu kỳ đúng 30 giây**, chờ ít nhất một chu kỳ để nhận bản tin chậm. Chủ dự án báo chu kỳ dài gây suy luận sai; 2 phút/5 phút bỏ suy luận hiện tại và pending, không tạo thêm suy luận, vẫn cảnh báo dòng đỏ thật. Lịch sử ngày cũ giữ nguyên. Chỉ xét lượt mới, không phát lại suy luận lịch sử. Có bằng chứng đến muộn thì bỏ riêng bản tin suy luận, không xóa bản tin gốc. Chỉ thông báo tên điểm/sự kiện, không trình bày lý do dài. Cần kiểm chứng suy luận ngoài hiện trường.

- **Bổ sung 0.11:** nhận đúng bản tin “Ấn nút Phục hồi” (không phân biệt hoa/thường, gộp khoảng trắng) tại đường ngang đang nghi tự kích thì khép riêng lỗi suy luận ở đó, liên kết khôi phục trong lịch sử và đóng đoạn sáng của đúng lượt đã bị đánh dấu `selfTriggered`. Không xóa chuông/đèn/cần chắn hay tàu chưa bị đánh dấu tự kích. Giữ dấu hoàn tất qua cache/khởi động lại/đổi chu kỳ; bản tin đã qua đến muộn không làm sáng lại. Chiếm dụng phần cứng thô vẫn theo bản tin khôi phục hệ thống; đoạn sáng là trạng thái hiển thị riêng.

## Đếm trục — quyết định cuối cùng ở 0.15 (kế thừa 0.12–0.14)

- **Ngoại lệ trục: Km 280+500, 307+507, 309+056, 320+445, 338+900 và 343+350.** Chủ dự án yêu cầu thêm năm điểm ở 0.15, giữ 320+445 có cơ chế riêng. Cả sáu điểm không hiển thị dữ liệu trục, không chạy ghi chú/đối chiếu/thống kê/cảnh báo trục suy ra. Lỗi đỏ web thật và mọi chức năng tàu/tự kích giữ nguyên. `axleVersion=3` chạy lại dọn cache khi nâng cấp từ bản 0.13/0.14: lọc tức thì, xóa riêng cảnh báo dẫn xuất cũ mọi ngày, hủy thông báo liên quan; giữ raw và dấu báo một lần của điểm khác. Không loại cả vùng km; danh sách tuyến vẫn lấy từ web theo tài khoản.
- Tính riêng từng CB1–CB6/từng đường ngang/từng ngày Việt Nam, cộng dồn cả các lần không liên tiếp. Chủ dự án đã thay ngưỡng nhỏ nhiều lần trong chat; **chốt cuối: lệch từ 3 trục mới cộng số lần, báo ở lần thứ 6**. Lệch 1 hoặc 2 trục không cộng, vẫn lưu ghi chú.
- Mỗi CB chỉ thông báo ngưỡng số lần **một lần/ngày**, kể cả service khởi động lại hoặc bản tin tải bù làm thay đổi vị trí lượt thứ sáu. `axleNotices` lưu dấu theo ngày/điểm/CB; đổi URL/ID xóa dấu cùng dữ liệu tài khoản.
- Một lần lệch tuyệt đối **từ 10 trục trở lên** cảnh báo mới cho đúng lần đo, gồm CB đã báo ngưỡng số lần. CB khác đạt ngưỡng có thông báo riêng. Một lần đạt cả hai ngưỡng chỉ sinh một cảnh báo. Các lần sai nhẹ thứ bảy trở đi cập nhật thống kê, không thông báo lại.
- Cảnh báo dẫn xuất mang prefix `axle:<CB>:<ID bản tin gốc>`, khác khóa lỗi phần cứng. Hiện rõ thừa/thiếu, chuẩn chung và số đếm. So CB theo số xuất hiện nhiều nhất như trước; không xác định chuẩn duy nhất thì chỉ giữ ghi chú, không kết luận sai CB nào. Chỉ dùng bản tin Khôi phục hệ thống có đủ sáu số nguyên không kèm V, không dùng thiết bị ga.
- Chủ dự án chọn giữ cảnh báo tới **00:00**, lượt đếm đúng/nút phục hồi/khôi phục phần cứng không xóa cảnh báo thống kê trong ngày. Mốc theo dõi chung sau gián đoạn không xóa thống kê đã đủ trong ngày. Reset ngày giữ archive, không giả thiết bị đã khôi phục.
- Cảnh báo xuất hiện ở Tổng quan, Sự cố, nhóm Cảm biến trong chi tiết; chuông sáu hồi theo điều khiển âm chung. Áp dụng cả 30 giây/2 phút/5 phút, khác điều kiện tự kích chỉ 30 giây. Tải lịch sử riêng không phát chuông.
- Chốt giao diện cuối: chi tiết chỉ thêm một mục **Đếm trục lỗi**, chỉ hiện khi có CB đạt một trong hai ngưỡng. Thống kê riêng các CB đã cảnh báo: số lần lệch từ 3, lần lệch gần nhất, tổng trục thiếu/thừa (tổng mọi ghi chú, gồm lệch nhỏ). Bỏ hai mục/trang riêng Đếm trục hôm nay và Xem các lần đếm trục. Mục thống kê không thay cho thông báo: đạt ngưỡng vẫn đỏ và chuông theo cài đặt. Cập nhật Tổng quan trong các khung có sẵn.
- Store dựng lại cảnh báo từ bản tin gốc và loại cảnh báo dẫn xuất cũ khi tải bù, giữ nguyên dữ liệu web. Startup nâng cấp dựng cảnh báo ngày hiện tại nền, giữ tài khoản và khóa ký cũ.

## Giao diện cần giữ

- **Mẫu mới nhất 0.8:** nền navy tối, thẻ xanh xám tối, cyan cho điều khiển, mint cho tàu, đỏ trầm cho sự cố. Biểu tượng đầu tàu trong app dùng cyan trên ô navy, launcher giữ đầu tàu thống nhất. Tất cả màn hình dùng cùng bảng màu, gồm hộp chọn ngày/ô nhập; không thêm lại giao diện minh họa.
- Thẻ tóm tắt trên sơ đồ ghi số điểm theo dõi/lỗi chưa khôi phục/thời gian cập nhật. Không dùng “AN TOÀN” hoặc khẳng định toàn bộ thiết bị bình thường chỉ vì không thấy lỗi đỏ.
- Đầu trang: “Giám sát Đường ngang”; dòng đơn vị chính xác: **“Công ty CP TTTH Đường sắt Vinh”**.
- Sơ đồ nhỏ, đường thẳng có các mốc đường ngang/ga ở trên cùng. Đoạn có tàu xanh lá, tên hướng tàu nằm trên mũi tên. Không thêm “Lý trình tăng” hoặc chú giải hướng cố định bên dưới. Dữ liệu cũ có dấu hiệu riêng, không giả là dữ liệu mới.
- Bên dưới là khối Tàu gần nhất riêng, rồi Sự cố nền đỏ trầm. Dòng thông tin theo thứ tự **Giờ – Đường ngang – Sự kiện/lỗi**. Sự cố mới nhất trước theo thời điểm phát sinh, không theo lúc khôi phục. Không lỗi thì thẻ sự cố thu gọn.
- Tổng quan vừa màn hình, không cần cuộn dọc, số dòng/đệm thích ứng với chiều cao; không ép cố định ba dòng. Tuyến dài có thể cuộn ngang.
- Tối ưu 0.13 dùng bộ dữ liệu tạm trong một lượt dựng/cập nhật màn hình: tuyến, phạm vi, sự kiện ngày, lỗi và phân nhóm theo đường ngang. Xóa bộ tạm trước đổi tab/cập nhật và sau đổi phạm vi; tránh bộ nhớ đệm lâu làm dữ liệu đứng. Các mẫu nhận diện lỗi được biên dịch một lần với nguyên văn biểu thức cũ.
- Cập nhật 30 giây / 2 phút / 5 phút phải giữ root, sơ đồ, khung tàu và khung sự cố; không thay toàn bộ content view rồi chờ dựng lại gây chớp trắng. Cùng view sơ đồ phải cập nhật lại mảng chiếm dụng, tránh hết chớp nhưng tàu bị đứng.
- Phạm vi “từ đường ngang đến đường ngang” chỉ lọc danh sách, sự cố và thông báo. **Không cắt ngắn sơ đồ** và không bỏ dữ liệu phía trước dùng suy luận.
- Thanh dưới: Tổng quan / Đường ngang / Sự cố / Cài đặt. Không khối lịch sử riêng trên Tổng quan. Tab Sự cố có chọn ngày, Tất cả/Chưa khôi phục/Đã khôi phục; tick xanh khi có bản tin phục hồi tương ứng.
- Chủ dự án chốt ở 0.9: trạng thái trống của Sự cố ghi **“Không có sự cố”**, bỏ câu “Chưa có sự cố phù hợp”. Khối Sự cố trên Tổng quan dùng cùng cách ghi khi đã có dữ liệu.
- Cài đặt: URL/ID/mật khẩu, phạm vi, chu kỳ, bật/tắt chuông, chạy nền. URL mới điền sẵn `http://`, vẫn sửa/xóa/thay HTTPS được, giữ địa chỉ đã lưu.
- Bỏ nút xem dữ liệu minh họa và đoạn chữ giải thích cuối Cài đặt. Khởi động vào theo dõi thực. Hàm samples còn trong nguồn phục vụ kiểm thử/lịch sử, không khôi phục lại công tắc preview cho người dùng.
- **“Designed by Cao Xuân Cường” chỉ ở Cài đặt**, cỡ nhỏ nhưng đọc được, không ở các màn hình khác.

## Mã nguồn và build

Android native Java, package/applicationId `vn.duongngang.monitor`; minSdk 26, targetSdk 34, compileSdk 35. Android Gradle Plugin 8.7.3, Gradle 8.9, JDK 21 đã dùng; Java source 17 và core library desugaring. Jsoup 1.18.3, SQLite, Android Keystore AES-GCM. Không có Gradle wrapper trong repo hiện tại.

| Tệp | Vai trò |
|---|---|
| `app/src/main/java/vn/duongngang/monitor/MainActivity.java` | Giao diện, cập nhật tại chỗ, sơ đồ, chọn phạm vi/ngày, cài đặt |
| `MonitorService.java` cùng thư mục | Foreground service, đọc định kỳ, thông báo, timer reset ngày |
| `AlarmSound.java` cùng thư mục | Âm báo lỗi riêng, audio focus, dừng và thử chuông |
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

Tăng versionCode lớn hơn 17 khi xuất APK cập nhật mới. Giữ applicationId và chữ ký nếu muốn cài đè. Không đưa URL/mật khẩu thử vào nguồn/APK, không đăng HTML đăng nhập hoặc ảnh dữ liệu vận hành vào repo. ID Chosy chỉ là khóa cấu hình bổ sung ga do chủ dự án yêu cầu, không được điền sẵn để đăng nhập.

## Khóa ký APK cần sao lưu riêng

APK hiện dùng **khóa debug của môi trường cloud này**; khóa riêng không được commit. Mã nguồn và APK trên GitHub **không chứa bản sao khóa riêng để ký lại**. Môi trường/tài khoản mới tạo khóa khác sẽ không cài đè được APK hiện có, dù mã nguồn giống hệt.

- Vị trí khóa ở môi trường hiện tại: `/workspace/toolchains/android-home/debug.keystore` (đã kiểm tra tồn tại khi bàn giao).
- SHA-256 chứng chỉ đang dùng: `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`.
- Cần sao lưu khóa vào nơi riêng do chủ dự án giữ, kèm thông tin sử dụng khóa; không đưa khóa thô lên repository công khai. Đã tạo bản sao mã hóa và thử khôi phục; xem [hướng dẫn sao lưu khóa](SAO_LUU_KHOA_KY.md). **Chưa có xác nhận chủ dự án tải bản sao ra ngoài cloud**. Tệp mã hóa và mật khẩu nằm ngoài repo và được giao riêng trong chat.
- Phiên mới cần khóa cũ để ký bản cập nhật cài đè. Nếu không có khóa, vẫn phát triển được mã nguồn, nhưng phải giải quyết phương án cài lại/chuyển sang khóa phát hành và tác động dữ liệu với chủ dự án.

## Kiểm chứng và việc còn lại

Bản 0.13: **391** quy tắc, **62** lưu trữ, **49** UI, **22** service fixture trong lượt sửa, build/Lint 0 lỗi, 15 cảnh báo. Đã đo callback đổi tab với 1.000 bản tin/11 đường ngang trên cùng giả lập; tab nhiều dữ liệu nhanh hơn, Cài đặt chưa cho thấy cải thiện trong lượt đo. Thời điểm từng nhóm kiểm tra/số đo/giới hạn xem `docs/UI_REVIEW_0.13.md`. Không đo trên Xiaomi thật. Giữ ngoại lệ và dữ liệu mới sau tối ưu khi tiếp tục.

Bản 0.12: **384** quy tắc, **55** lưu trữ, **48** UI, **12** parser/**13** đọc web fixture và **21** service fixture đạt; build/Lint 0 lỗi, 15 cảnh báo. Cảnh báo ở lần thứ sáu (chỉ cộng lệch từ ba), một lần lệch từ mười; dấu chống báo lặp theo CB/ngày, CB khác và lỗi lớn mới vẫn báo. Không đăng nhập web thật trong lượt này, không xác minh loa Xiaomi thật. Xem `docs/UI_REVIEW_0.12.md`.

Bản 0.11: build/Lint đạt (0 lỗi, 15 cảnh báo), **355** quy tắc, **40** lưu trữ, **43** UI, **12** parser/**13** đọc web fixture và **12** service fixture đạt. Chuông sáu hồi/12 giây, nút phục hồi khép tự kích và tắt đoạn sáng, giữ lỗi thiết bị khác; kiểm tra cả di trú cache/khởi động lại/đổi chu kỳ. Lượt này không đăng nhập lại web thật; kiểm tra trên giả lập, loa Xiaomi vẫn cần chủ dự án thử. Xem `docs/UI_REVIEW_0.11.md`.

Bản 0.10: build/Lint đạt (0 lỗi, 15 cảnh báo), **335** quy tắc, **35** lưu trữ, **36** UI, **12** parser/**13** đọc web fixture và **9** service fixture đạt. Đã đọc web thật 2.395 bản tin/13 điểm, phát lại tại mốc ảnh: 0.9 dựng 8 đoạn (5 lẻ/3 chẵn), 0.10 dựng 1 đoạn chẵn. Chỉ lưu tổng hợp, đã xóa audit riêng. Các kiểm tra này không phải UAT trên Xiaomi/hiện trường; xem `docs/UI_REVIEW_0.10.md`.

Bản 0.9: build/Lint đạt (0 lỗi, 15 cảnh báo), **133** kiểm tra quy tắc, **30** lưu trữ và **32** UI Android đạt. Sửa đoạn đến/đi liên tục, chuyển quyền hiển thị khi vùng cảm biến chồng nhau, đoạn vào/ra ga và câu “Không có sự cố”. Ảnh từng bước ở `docs/UI_REVIEW_0.9.md`. Không chạy lại web thật/service fixture trong thay đổi hình vẽ này; chưa có xác nhận hiện trường/Xiaomi.

Bản 0.8: build/lint đạt (0 lỗi Lint, 15 cảnh báo), **113** kiểm tra quy tắc, **30** lưu trữ, **30** UI và **9** service fixture đạt. Ghi rõ kiểm tra player phát/dừng, lỗi đang mở khi bắt đầu theo dõi, tắt âm, 30 giây mới xét tự kích và đoạn tàu chuyển tiếp. Chi tiết/ảnh tổng hợp ở `docs/UI_REVIEW_0.8.md`; chưa xác nhận loa Xiaomi thật.

Bản 0.7: build/lint thành công, 92 kiểm tra quy tắc, 26 kiểm tra lưu trữ, 21 kiểm tra UI, 7 kiểm tra service cục bộ đạt. Chi tiết ở `docs/UI_REVIEW_0.7.md`. Kiểm tra UI gọi cùng callback của các chu kỳ, không ngồi chờ cả 30/120/300 giây. Kiểm tra reset dùng mốc thời gian tổng hợp; không chứng minh điện thoại thật chạy liên tục qua đêm.

Bản 0.6 đã đăng nhập Visitec thật, đọc đủ ngày hiện tại/ngày trước qua khoảng 20 trang, lấy đủ 13 điểm, thử đọc tăng thêm; giả lập cập nhật được 5 lượt khi tắt màn hình trong 80 giây. Xem `docs/LIVE_REVIEW_0.6.md`. Chặn HTTP 403 ghi trong tài liệu cũ đã hết ở phiên thử 0.6, không áp đặt kết quả đó cho phiên mới; mạng mới vẫn phải kiểm tra lại.

Cần người dùng xác nhận trên **Xiaomi 14T Pro/HyperOS**: còn chớp trắng hay không, bố cục/cỡ chữ, chạy nền lâu khi khóa màn hình, độ trễ chuông, reset 00:00, sự cố/khôi phục và tự kích so với thực tế. Chưa có phản hồi xác nhận đầy đủ bản 0.7. Chưa tự chạy sau khởi động máy. Web không cung cấp ID sự kiện độc lập nên dòng hoàn toàn giống nhau cùng giây được gộp; không cam kết tương thích mọi cấu trúc website.

Các yêu cầu mới nhất và kiểm tra 0.13 xem `docs/UI_REVIEW_0.13.md`. Thuật toán đã được đối chiếu lại với dữ liệu web thật ở mốc ảnh phản hồi, nhưng bố cục/chuông/chạy nền Xiaomi vẫn cần chủ dự án xác nhận trên máy. Tình trạng sao lưu khóa ngoài cloud vẫn **chưa được chủ dự án xác nhận**.

## Giao diện Stitch — 0.14

Chủ dự án gửi bốn ảnh từng tab và bảng màu, yêu cầu làm tương tự nhưng căn linh hoạt. Áp dụng vào app Java hiện có, giữ toàn bộ nghiệp vụ 0.13. Nền navy/cyan, thẻ có viền, ga amber, nút lưu cyan–mint. Logo sáng dịu và vòng đếm lỗi pulse chu kỳ 2,8 giây; dừng khi app mất focus/chạy nền và tôn trọng tắt hiệu ứng Android. Đoạn tàu chỉ có quầng sáng tĩnh, không chạy giả theo hoạt ảnh. LIVE chỉ cho dữ liệu mới của phiên đang theo dõi. Danh sách thêm bộ lọc theo dữ liệu thực. Không dùng pin/góc cần/hướng cố định hoặc các con số giả của JSON mẫu. Xem docs/UI_REVIEW_0.14.md cho ảnh, kiểm tra và giới hạn.

Kiểm tra 0.14: 391 quy tắc, 62 lưu trữ, 49 UI hồi quy và 20 telemetry UI (đạt khi bật và khi tắt hiệu ứng). Build/Lint đạt, 0 lỗi và 15 cảnh báo. Không kiểm tra lại web công ty/service fixture trong lượt đổi giao diện; cần xác nhận độ mượt/âm thanh trên Xiaomi thật.

## Năm ngoại lệ bổ sung — 0.15

Chủ dự án yêu cầu thêm 307+507, 309+056, 280+500, 343+350 và 338+900 vào danh sách không tính lệch trục, cùng ngoại lệ 320+445 cũ. Một tập lý trình số dùng chung; cache trục tăng lên 3 để dọn cả cảnh báo cũ của năm điểm mới. Giữ nguyên giao diện 0.14, lỗi đỏ web thật, tàu/tự kích, âm sáu hồi, tài khoản và các ngưỡng ở điểm khác. Xem docs/UI_REVIEW_0.15.md. Gói ZIP sao lưu đã giao trước đó vẫn là snapshot 0.14; nguồn mới và các quyết định sau nằm trên main.

Kiểm tra 0.15: 450 quy tắc, 115 lưu trữ, 54 UI và 22 dịch vụ với web giả lập đạt trên bản cuối; build/Lint 0 lỗi, 15 cảnh báo. Không đọc web công ty trong lượt này, chưa xác nhận trên Xiaomi thật. Chữ ký APK giữ nguyên.

## Quy trình bằng chứng tự kích — 0.16

Chủ dự án đã xác nhận bổ sung cần chắn đã hạ/đã nâng và tàu đã qua cùng hướng ở đường ngang phía trước làm bằng chứng trong lượt đang xét. Số cần tùy ý; chỉ bản tin gốc xác nhận hoạt động, không dòng đỏ/lỗi. Gộp theo lượt, mỗi lượt dùng một lần, không lấy hoạt động từ lượt cũ hoặc sau khi lượt đang xét đã kết thúc. Bằng chứng bổ sung phù hợp gỡ riêng suy luận, giữ lỗi thật; cập nhật `selfEvidenceVersion=1` để đánh giá lại suy luận cũ khi cài đè, hủy thông báo sai và sửa phân loại sơ đồ từ raw mà không phát lại lịch sử. Giữ 30 giây, ranh giới ga/tuyến và toàn bộ các quy tắc khác. Xem docs/UI_REVIEW_0.16.md.

Từ 24/10, chủ dự án muốn trao đổi bằng cách nhắc lại cách hiểu và đợi xác nhận trước khi sửa. Phương án 0.16 đã được xác nhận và có yêu cầu triển khai rõ; không hỏi lại cùng một sự chấp thuận cho các bước thực hiện/kiểm tra/phát hành thông thường. Dữ liệu web thật chỉ giữ riêng ngoài Git.

Kiểm tra 0.16: 486 quy tắc, 130 lưu trữ, 54 UI và 25 dịch vụ theo chu kỳ thật đạt trên APK cuối; build/Lint 0 lỗi, 15 cảnh báo. Đã phát lại hai ngày báo cáo thật: tình huống cần đối chiếu từ một nghi vấn thành không còn nghi vấn; chỉ ghi kết quả tổng hợp, giữ dữ liệu xác thực/vận hành ngoài Git. Chưa xác nhận trên Xiaomi thật.

## Bổ sung ga Nghi Long — 0.17

Chủ dự án xác nhận ga mới Km 303+350 chưa có trong web và yêu cầu bổ sung riêng cho ID Chosy. `Rules.accountRoute` chèn `303+350 (Nghi Long)` vào đúng thứ tự sau khi đọc đủ danh sách gốc, áp dụng cả tuyến cache cũ; không tạo bản tin vận hành tại ga. Không chèn trùng nếu web có cùng chainage sau này. Ga dùng các quy tắc tàu vào/ra và ranh giới tự kích hiện có; không tính thành đường ngang hoặc điểm chọn phạm vi/đối chiếu trục. ID khác không nhận bổ sung này.

`selfEvidenceVersion=2` đánh giá lại nghi tự kích đã lưu theo ranh giới mới, giữ raw/lỗi thật, không phát lại lịch sử. `travelReady` kiểm tra `travelRoute` khớp topology hiện tại, dựng lại checkpoint/sơ đồ khi cần. Giữ `travelVersion=3`, `axleVersion=3`, các quy tắc và khóa ký trước.

Kiểm tra 0.17: **506 quy tắc, 12 parser, 15 đọc web fixture, 143 lưu trữ và 57 UI** đạt; build/Lint 0 lỗi, 15 cảnh báo. Không đăng nhập ID Chosy trên web thật hoặc chạy lại toàn bộ bộ service 25 ca trong lượt này; không có kiểm chứng Xiaomi mới. Xem docs/UI_REVIEW_0.17.md. Gói sao lưu phiên v0.14 vẫn là snapshot lịch sử; thay đổi mới nằm trên nhánh main, không tự cập nhật vào ZIP cũ.
