# Nhật ký trao đổi và các quyết định của chủ dự án

Tổng hợp đến 23/10/2026, giờ Việt Nam. Mục đích là giữ ngữ cảnh và lý do để người tiếp tục dự án hiểu yêu cầu mà không phải hỏi lại từ đầu. Đây là **bản tổng hợp các trao đổi chính**, không phải tệp xuất nguyên văn của ChatGPT. Không chứa mật khẩu, cookie hoặc các ảnh gốc đã đính kèm trong chat. Thông số và hành vi hiện tại xem thêm [bàn giao](BAN_GIAO.md); yêu cầu mới của chủ dự án được ưu tiên.

## 1. Bắt đầu bằng trao đổi nghiệp vụ

Chủ dự án yêu cầu thiết lập môi trường cloud cho repo `cuongthinkpad-ui/app-duong-ngang`, sau đó muốn trao đổi kế hoạch trước khi code. Mục tiêu là làm app Android phục vụ công tác theo dõi đường ngang CBTĐ của đơn vị, có chạy nền và cảnh báo điện thoại. Website công ty cần đăng nhập, tài khoản chỉ có quyền xem; thiết bị hiện trường không do app này điều khiển.

Chủ dự án gửi nhiều ảnh trang báo cáo Visitec: danh sách điểm bên trái, bộ lọc ngày/lý trình/Tất cả–Trở ngại, các dòng sự kiện có màu, cột CB và nhiều trang kết quả. Mong muốn app nhận URL/ID/mật khẩu và đọc các thông báo đó. Ban đầu làm một tài khoản/khu vực, sau này tài khoản khác hiển thị các điểm quản lý khác; nhiều người có thể cài cùng APK. Không thiết kế cố định chỉ 13 đường ngang.

Chu kỳ đã chốt: **30 giây, 2 phút, 5 phút**; phải theo dõi khi app không mở trên màn hình. Tài khoản thử được cung cấp riêng trong chat để kiểm tra, không dùng làm giá trị mặc định hoặc nhúng vào APK.

## 2. Chủ dự án giải thích cảm biến và hướng tàu

Lý trình đường sắt đánh từ Bắc xuống Nam: phía Bắc gọi là phía lẻ, phía Nam gọi là phía chẵn. CB1/CB2 ở phía Bắc cách đường ngang hơn 1 km, hai CB cách nhau khoảng 1 m; CB3/CB4 ở đường ngang; CB5/CB6 ở phía Nam với cách bố trí tương tự.

Tàu từ Bắc vào gặp CB1/2 trước, qua CB3/4 rồi CB5/6: **tàu lẻ**. Ngược lại là **tàu chẵn**. Chủ dự án sửa rõ một hiểu nhầm quan trọng: tàu qua CB3/4 thì cần chắn nâng, nhưng chưa khôi phục toàn hệ thống. Sau khi ra khỏi toàn khu cảm biến, khoảng một phút sau mới có khôi phục. Vì vậy không tự bỏ xanh khi chỉ thấy cần chắn nâng.

Chuông, đèn và cần chắn đều có thông báo lỗi; cứ dòng đỏ thì cần cảnh báo, không tự suy ra một lỗi khác. Các cột Status và điện áp số không cần bày nhiều ngoài màn chính.

## 3. Hai điểm ga và số trục

Tài khoản đầu tiên có 13 điểm: **11 đường ngang + 2 thiết bị báo tàu trong ga**. `319+200 (Vi)` là ga Vinh, `330+00 (YX)` là ga Yên Xuân. Chủ dự án nhấn mạnh đây không phải đường ngang; tàu có thể nằm lại trong ga cả ngày hoặc có tàu khác vượt, nên bản tin ga không dùng làm chứng cứ liền kề để xét tự kích.

Các giá trị CB1–CB6 khi khôi phục là số trục đếm được; dòng đo điện áp có thể có V phía sau, không mang số đó đi so trục. Chủ dự án muốn ghi chú CB đếm thừa/thiếu, có thể đồng thời một con thừa và con khác thiếu, dùng mặt bằng chung làm chuẩn. Đây là ghi chú theo dõi, không phải cảnh báo có chuông. Câu được yêu cầu giữ nguyên khi không xác định được chuẩn chung: “cảm biến đếm trục không đồng nhất, lỗi”. Bản hiện tại chọn số xuất hiện nhiều nhất làm chuẩn; hòa chuẩn dùng câu đó.

## 4. Phát hiện tự kích sớm

Chủ dự án giải thích cảm biến nhạy với sấm sét/thời tiết, có thể tự kích mà không có tàu. Web thường mất khoảng 10 phút mới báo lỗi đỏ; mong muốn app nhận biết sớm bằng diễn biến tàu trên tuyến.

Quy tắc trao đổi: một điểm phía Nam báo tàu lẻ nhưng chưa có lượt tương ứng ở điểm phía Bắc, hoặc một điểm báo tàu chẵn mà chưa có lượt từ phía Nam, thì nghi tự kích. Điểm ở giữa có tàu nhưng thiếu lượt tương ứng hai bên cũng cần chú ý. Không áp dụng hướng đối chiếu không có đường ngang liền kề, gồm các mép tuyến/đoạn bị ga ngắt. Ví dụ gốc: phía Bắc 312+111, phía Nam 314+550, phía Bắc 320+445, phía Nam 334+830.

Không yêu cầu đường ngang trước phải còn xanh ở cùng thời điểm; cần xét lịch sử lượt tàu cùng hướng. Thông báo chỉ cần tên đường ngang tự kích, không đưa giải thích dài. Bản hiện tại ghi “Nghi tự kích”, chờ một chu kỳ/tối thiểu 30 giây để tránh kết luận từ bản tin chậm; chứng cứ đến muộn loại bỏ riêng suy luận. Đây là cách triển khai hiện tại, chưa được đối chiếu đủ với hiện trường.

## 5. Bố cục và giao diện được chỉnh dần

Chủ dự án muốn màu xanh dương nhẹ nhàng, ít thông số ngoài màn hình, chuyên nghiệp, dễ nhìn khi đi kiểm tra. Các ảnh tham khảo gồm Tổng quan, danh sách, chi tiết đường ngang, cảnh báo và cài đặt; đặc biệt thích màn **Chi tiết đường ngang** trong ảnh thứ ba. Chốt chỉ sáu nhóm: chuông, đèn, cần chắn, cảm biến, kết nối, điện áp; bỏ camera.

Các quyết định được sửa qua nhiều lượt:

- Sơ đồ tàu ở trên cùng, chỉ cần đường thẳng nhỏ có các mốc đường ngang và ga, đoạn có tàu xanh lá và mũi tên hướng; không cần ảnh vệ tinh hoặc hình đoàn tàu. Sau đó bỏ “Lý trình tăng” và chú giải tàu lẻ/chẵn cố định, đưa tên hướng lên chính mũi tên.
- Tàu gần nhất là một khối riêng ngay dưới sơ đồ; có thể mở danh sách đầy đủ. Sự cố nền đỏ nhạt nằm dưới khối tàu.
- Dòng thông tin theo **Thời gian – Tên đường ngang – Sự kiện/lỗi**. Chọn ngày riêng để xem lại, không lặp ngày trên từng dòng lịch sử.
- Có thể giảm cỡ chữ/đệm để hiện vừa đủ dữ liệu. Chủ dự án sửa lại ý “giữ ba dòng mặc định”: không ép cố định số dòng; Tổng quan phải vừa màn hình, không quá trống và không kéo dài khiến phải cuộn dọc.
- Bỏ khối Lịch sử ở Tổng quan và bỏ tab Lịch sử; thay bằng tab Sự cố, vẫn có chọn ngày và lọc trạng thái trong đó.
- Đổi câu “Chưa thấy bản tin khôi phục” thành **“Chưa khôi phục”**. Sự cố khôi phục có tick xanh đúng bản tin tương ứng; không để lỗi cũ treo khi đã phục hồi.
- Không dùng tính năng xác nhận đã xem hoặc nhắc lại lỗi định kỳ. Nút tắt chuông từng lỗi ban đầu cũng được bỏ: chỉ một điều khiển chung cạnh biểu tượng chuông, tắt âm không xóa đỏ.
- Chọn phạm vi từ đường ngang này đến đường ngang khác, nhưng phải **giữ nguyên sơ đồ toàn tuyến** để xem ngữ cảnh tàu.
- Đầu trang/biểu tượng app theo đầu tàu trắng trên nền navy trong ảnh tham khảo. Dòng tên thiết kế lúc đầu đặt nhỏ ở góc ngoài, sau đó chủ dự án chuyển hẳn sang **Cài đặt**, không xuất hiện ở các màn hình khác.

Khi app gián đoạn qua ngày không đủ dữ liệu, chủ dự án chấp nhận đặt lại theo dõi, nhưng không được giả bản tin thiết bị khôi phục. Bản hiện tại phân biệt “Chưa ghi nhận lỗi mới” với “Đã khôi phục”.

## 6. Đưa bản cài và các vướng mắc đã gặp

Chủ dự án nhiều lần không tải được APK/artifact trong giao diện cloud; các cách mở bản xem trước, “Lưu dưới dạng…” hoặc gói ZIP cũng thất bại. Sau đó chuyển sang bàn giao APK qua GitHub; hướng dẫn tải **Download raw file**. Phiên sau nên dùng cách đã hoạt động này.

Trên Xiaomi 14T Pro, Play Protect từng hiện “Đã chặn ứng dụng có hại” và cảnh báo rủi ro. Chủ dự án nói các APK khác vẫn cài được, sau đó báo đã cài được app này. Không có bằng chứng vấn đề bảo vệ đã hết với mọi bản/mọi điện thoại.

Chủ dự án phản hồi bản đầu nhìn thô sơ, sơ đồ không hiện tàu. Các bản sau cải thiện giao diện theo ảnh tham khảo, bố cục linh hoạt, biểu tượng đồng nhất và chiếm dụng theo bản tin. Không coi dữ liệu minh họa là kết nối web thật.

Thông báo “Selected model is at capacity” từng xuất hiện trong giao diện ChatGPT. Đây là vấn đề của phiên/model, không phải lỗi nghiệp vụ đường ngang hoặc bằng chứng APK hỏng.

## 7. Mở rộng theo tài khoản và ghi đúng lỗi

Chủ dự án gửi ảnh tuyến khác và chốt nhận dạng điểm có ngoặc là ga, dùng chữ bên trong làm nhãn; nguyên lý tàu như khu vực đầu, app cần linh hoạt theo danh sách mới. Sau đó hỏi tuyến dài có hai tàu thì sơ đồ có hiện đồng thời không; cần biểu diễn nhiều điểm/đoạn có bản tin chiếm dụng, không giả chỉ một đoàn tàu.

Chủ dự án sửa cụ thể mẫu “Mất tín hiệu chuông”: website ghi rõ **chuông nào hỏng**, phải chép đúng nguyên văn dòng đỏ khi đọc dữ liệu thật. Không tự đổi thành “chuông mất trạng thái”; mất trạng thái là cách thông báo của cần chắn. Bản minh họa cũ dùng câu sai đã được sửa thành “Chuông 2 hỏng”.

## 8. Bản 0.6: kết nối thực và giới hạn được nói rõ

Đã sửa đăng nhập ASP.NET để không chọn nhầm nút Thoát, xử lý select/ngày/bộ lọc/phân trang và đọc tăng thêm. Thử được web thật, tuyến 13 điểm, ngày hiện tại/ngày trước; giả lập còn cập nhật khi tắt màn hình. Phát suy luận tự kích được nối vào service, vẫn giữ bản tin gốc khi loại bỏ suy luận.

Chủ dự án hỏi “đây là bản hoàn thiện dùng được rồi hay đang thử nghiệm”. Đã trả lời rõ: **bản thử có chức năng thật**, chưa được kiểm chứng đủ trên Xiaomi để thay theo dõi web trong ca trực. Cần đối chiếu app với web/hiện trường và thử chạy nền lâu. Phiên tiếp theo không được đổi lời mô tả thành đã vận hành chính thức chỉ vì build/test đạt.

## 9. Các yêu cầu mới nhất đã đưa vào bản 0.7

1. Mỗi chu kỳ 30 giây/2 phút/5 phút bị chớp trắng khi tải lại. Chủ dự án nhấn mạnh cả sơ đồ tàu, Tàu gần nhất và Sự cố đều phải mượt. Đã đổi cách cập nhật: giữ root, vùng sơ đồ và hai thẻ, cập nhật nội dung tại chỗ; dựng Dashboard trước khung hình đầu. Sơ đồ vẫn phải đổi hướng/khôi phục thật, không chỉ giữ ảnh cũ để tránh nháy.
2. Sự cố xếp từ **thời điểm phát sinh gần nhất đến lâu nhất**, cả Tổng quan và tab Sự cố.
3. Bỏ mục xem giao diện minh họa và các dòng giải thích cuối Cài đặt; **giữ chữ ký**.
4. Ô website điền sẵn **`http://`**, người dùng có thể sửa/xóa/thay HTTPS. Giữ URL đã lưu.
5. Sửa dòng đơn vị dưới tiêu đề từ tên cũ thành đúng **“Công ty CP TTTH Đường sắt Vinh”**.
6. Chủ dự án thấy sự cố qua ngày vẫn giữ, yêu cầu **reset sau 00:00**. Đã áp giờ Việt Nam; loại lỗi hôm trước khỏi khối hiện tại, giữ lỗi mới sau 00:00, giữ lịch sử và chiếm dụng tàu. Tab Sự cố đang xem hôm nay tự sang ngày mới; lựa chọn ngày cũ chủ động được giữ.

Bản 0.7 đã được đưa lên nhánh main, có APK trong `downloads/`, đã gửi link cho chủ dự án. Build/lint và các kiểm tra giả lập đạt; **chưa có phản hồi xác nhận toàn bộ bản 0.7 trên điện thoại thật** tại thời điểm lưu nhật ký. Kết quả kiểm tra cụ thể nằm ở `docs/UI_REVIEW_0.7.md`.

## 10. Lưu ngữ cảnh khi đổi tài khoản ChatGPT

Chủ dự án yêu cầu giữ cả phần trao đổi để phiên sau hiểu ngữ cảnh. Đáp ứng bằng ba lớp tài liệu trong cùng GitHub: `README.md` để bắt đầu, `docs/BAN_GIAO.md` để tiếp tục kỹ thuật/yêu cầu hiện hành, và nhật ký này để hiểu diễn biến/lý do. `AGENTS.md` chỉ đường cho trợ lý đọc các tệp đó.

Các tài liệu này không tự chuyển lịch sử chat của tài khoản ChatGPT. Nếu cần nguyên văn kèm ảnh gốc, chủ dự án dùng chức năng **Xuất dữ liệu** trong Cài đặt → Kiểm soát dữ liệu của ChatGPT, giữ tệp xuất riêng và cung cấp phần cần thiết cho phiên sau. Cuộc chat từng chứa thông tin đăng nhập thử; không đưa toàn bộ tệp xuất đó lên GitHub công khai.

Khóa riêng dùng ký APK cũng cần sao lưu riêng ngoài cloud để tài khoản/môi trường mới ký được bản cài đè; xem mục khóa ký trong bản bàn giao. GitHub hiện giữ mã nguồn/APK/tài liệu, chưa có bản sao khóa ký riêng.

## 11. Chuẩn bị sao lưu khóa ký

Chủ dự án hỏi ý nghĩa khóa ký và cần làm gì để giữ khả năng cập nhật khi đổi tài khoản/môi trường. Đã giải thích: app đã cài vẫn dùng được; khóa cũ cần cho bản APK mới cài đè. Sau đó đã chuẩn bị bản sao mã hóa, mật khẩu ngẫu nhiên riêng, công cụ/hướng dẫn khôi phục và kiểm chứng khôi phục đúng khóa/chứng chỉ, từ chối mật khẩu sai/nội dung bị sửa. Tệp khóa, bản mã và mật khẩu ở ngoài Git, giao riêng cho chủ dự án để tải về; chỉ công cụ và hướng dẫn vào repo. Chưa xác nhận chủ dự án đã lưu ra máy/ổ riêng. Xem `docs/SAO_LUU_KHOA_KY.md`.

## 12. Các yêu cầu đưa vào 0.8

Chủ dự án gửi năm ảnh giao diện phòng điều độ tối, muốn đổi màu và bố trí tương tự nhưng giữ bố cục gọn đã chốt. Dùng nền navy, thẻ xanh xám, điều khiển cyan, tàu xanh mint, lỗi đỏ trầm ở Tổng quan, danh sách/chi tiết, Sự cố, Cài đặt. Thẻ tổng hợp không được suy ra “an toàn” chỉ từ việc chưa ghi nhận lỗi.

Về sơ đồ, chủ dự án phản hồi hai đường ngang cách xa: điểm trước đã khôi phục mà điểm sau chưa có tàu làm tàu biến mất. Yêu cầu mới là giữ sáng **khoảng giữa hai đường ngang liền kề**, không tính qua ga. Khi có bản tin tàu đã qua điểm trước, chỉ sáng khoảng giữa hai điểm, bỏ phần rìa ngoài hai phía. Bản 0.8 lưu lượt chuyển tiếp, chỉ đóng khi điểm sau báo tàu cùng hướng; giữ sau khôi phục điểm trước và qua nửa đêm/khởi động lại. Nếu điểm sau đã báo trước lúc điểm trước khôi phục thì không làm đoạn cũ sáng lại. Đây là suy luận theo sự kiện web, không phải định vị GPS. Trạng thái chiếm dụng thiết bị vẫn là quy tắc cũ, cách biểu diễn trên sơ đồ là yêu cầu mới.

Chủ dự án hỏi cài bản mới có cần app gốc: mỗi APK là bộ cài đầy đủ, cài mới trực tiếp được; cùng chữ ký và applicationId thì cài đè giữ cấu hình. Tiếp tục dùng khóa cũ cho 0.8.

Chủ dự án yêu cầu **chỉ cảnh báo tự kích khi chọn chu kỳ 30 giây** vì các chu kỳ dài khiến suy luận báo sai nhiều. Chọn 2 phút/5 phút thì ngừng suy luận, bỏ pending/suy luận hiện tại trong ngày, không xóa dòng đỏ gốc hoặc lịch sử ngày cũ. Những lỗi đỏ từ web vẫn phát cảnh báo theo lựa chọn âm thanh.

Chủ dự án nói app có biểu tượng chuông, có thông báo nhưng **mở app, có lỗi vẫn không nghe tiếng chuông**. Bản 0.8 thêm player phát âm báo riêng thay vì chỉ phụ thuộc tiếng thông báo hệ thống: một chime 6 giây cho lỗi mới, và lỗi đang mở khi bắt đầu phiên theo dõi; không nhắc lại định kỳ. Tắt chuông chung dừng âm đang phát, giữ đỏ. Có “Thử chuông báo lỗi” trong Cài đặt, dùng mức âm lượng Báo thức. Kiểm tra player trên giả lập không chứng minh loa Xiaomi đã kêu; cần chủ dự án thử trên máy thật.

Biên bản kiểm tra và ảnh chỉ dùng dữ liệu tổng hợp ở `docs/UI_REVIEW_0.8.md`. Không đăng ảnh vận hành hoặc tài khoản thử.

## 13. Sửa sơ đồ và câu trống ở 0.9

Chủ dự án yêu cầu đổi “Chưa có sự cố phù hợp” thành **“Không có sự cố”**, nhắc lại riêng để xác nhận câu chữ. Áp dụng tab Sự cố và khối trống đã có dữ liệu trên Tổng quan.

Chủ dự án nói sơ đồ 0.8 chưa đúng, đưa chuỗi cụ thể: khi 312+111 báo tàu lẻ tiếp cận, sáng mép phía lẻ/Bắc của 312+111. Sau khi tàu qua, chỉ sáng khoảng từ phía Nam 312+111 tới phía Bắc 313+456, giữ dù 312+111 đã khôi phục. Khi 313+456 mới tiếp cận vẫn là đoạn đến đó; khi báo tàu đã qua thì chuyển sang 313+456–313+800. Các đường ngang/hướng chẵn làm tương tự. Chủ dự án chốt: **tàu còn đang chạy trên tuyến thì luôn có một khoảng sáng cho mỗi tàu, trừ khi trên tuyến không có tàu**; không mất giữa hai điểm và không sáng thêm đoạn phía sau cùng một lượt tàu.

Riêng đoạn giáp ga, vẫn sáng từ đường ngang hướng vào ga tới khi đường ngang khôi phục, rồi tắt; khi đường ngang nhận tàu từ ga ra thì sáng đoạn ga–đường ngang, sau đó chuyển tiếp như bình thường. Không lấy tín hiệu ga để giữ tàu trên tuyến cả ngày hoặc nối xuyên qua ga.

Nguyên nhân cách vẽ trước: chỉ tô chấm khi tiếp cận và đóng đoạn chuyển tiếp ngay khi điểm sau tiếp cận. Sửa 0.9 vẽ đoạn đến/đi theo từng giai đoạn, chuyển quyền hiển thị cho lượt ở điểm sau, bỏ đoạn tiếp cận cũ đã được nối sang điểm sau. Không thay nguyên lý khôi phục phần cứng hoặc cảnh báo lỗi. Chủ dự án đã xác nhận cách hiểu và yêu cầu sửa; không cần hỏi lại trước khi xuất APK.

## 14. Phản hồi ảnh thực và sửa giữ tàu cũ ở 0.10

Chủ dự án gửi ảnh app lúc 04:47:01 có nhiều khoảng sáng/mũi tên hướng chồng nhau; nhật ký bên dưới là tàu chẵn nhưng sơ đồ vẫn giữ các hướng lẻ cũ. Đã thừa nhận lỗi ghép/giữ lượt tàu trong code. Chủ dự án nhấn mạnh: **khi nhận bản tin tàu đã qua đường ngang thì đoạn trước phải tắt liền và chuyển sang đoạn kế**, không chờ khôi phục.

Đã tái hiện bằng kiểm tra lỗi: nếu thiếu bản tin tiếp cận ở điểm sau, code 0.9 có thể tạo đoạn mới từ bản tin đã qua mà không đóng đoạn phía trước. Ngoài ra, code xóa dấu lượt hoàn tất, nên bản tin đã qua đến muộn sau khôi phục có thể tạo lại tàu; lượt cũ trái hướng không được đóng bằng bằng chứng mới; các cảm biến kề nhau báo ngược thứ tự có thể bị coi thành hai lượt. Nhãn chữ/mũi tên vẽ riêng theo hàng không giới hạn bị chồng/cắt.

Sửa 0.10 nối cả bản tin đã qua vào lượt phía trước, giữ liên kết kế tiếp/dấu hoàn tất, loại đoạn cũ mâu thuẫn khi có bằng chứng hướng ngược tại điểm liên quan và ghép báo đảo thứ tự gần nhau. Không áp dụng xóa tàu theo một đồng hồ hết hạn. Cache sơ đồ cũ được chuyển nền từ raw đã lưu, giữ tài khoản/cài đặt/lỗi. Tên hướng và mũi tên gộp chung một nhãn, rút gọn khi đông.

Đã dùng tài khoản kiểm thử được cho phép để đọc web thật và so sánh phát lại cùng dữ liệu ở mốc ảnh: thuật toán 0.9 có 8 đoạn (5 lẻ/3 chẵn), thuật toán sửa còn 1 đoạn chẵn. Chỉ lưu kết quả tổng hợp vào repo; không lưu tài khoản, cookie, ảnh hoặc bản tin vận hành vào Git. Các ảnh kiểm tra 0.10 vẫn là dữ liệu tổng hợp. Xem `docs/UI_REVIEW_0.10.md` để phân biệt kiểm tra thật, mô phỏng và phần còn cần xác nhận trên Xiaomi.


## 15. Chuông sáu hồi và phục hồi tự kích ở 0.11

Chủ dự án phản hồi “cơ bản là app đã chạy ok”, yêu cầu hai bổ sung: chuông đang ba hồi tăng thành **sáu hồi**; khi đường ngang đang tự kích nhận **“Ấn nút Phục hồi”**, phải hết cảnh báo tự kích và tắt đoạn xanh tương ứng trên sơ đồ.

Bản 0.11 giữ âm sắc/nhịp âm cũ, tăng thời lượng từ 6 lên 12 giây (sáu hồi), vẫn một lần báo và một nút tắt chung. Quy tắc khôi phục chỉ khép lỗi suy luận của đúng điểm, giữ nguyên lỗi phần cứng khác. Sơ đồ lưu cờ tự kích gắn đúng lượt và đóng lượt đó khi có bản tin nút phục hồi; giữ dấu kết thúc để bản tin tàu tới muộn không tạo lại. Lượt tàu mới vẫn hiển thị bình thường. Cache tự chuyển từ raw, không yêu cầu gỡ bản cũ. Kết quả kiểm tra và giới hạn ở `docs/UI_REVIEW_0.11.md`.


## 16. Thống kê và hai ngưỡng cảnh báo trục ở 0.12

Chủ dự án không tìm thấy ghi chú thừa trục ở Km 313+800 buổi sáng. Kiểm tra code 0.11 cho thấy ghi chú chỉ nằm dưới dòng khôi phục trong 12 sự kiện gần nhất của chi tiết, không có bảng riêng. Chưa đối chiếu bản tin cụ thể sáng đó; không khẳng định app đã nhận được lượt đó.

Chủ dự án yêu cầu thêm cảnh báo: cùng CB sai hơn năm lần/ngày hoặc một lần lệch hơn mười trục. Ngưỡng lệch nhỏ để cộng số lần được chỉnh qua vài tin nhắn; **chốt cuối lệch từ ba trục mới cộng**, lệch một/hai vẫn ghi chú nhưng không cộng. Đã hỏi rõ mốc “quá sáu lần” ở tin nhắn sau: chủ dự án chọn **lần thứ sáu như đã chốt**, không phải thứ bảy.

Chủ dự án chọn giữ cảnh báo tới hết ngày/00:00 và yêu cầu ngưỡng số lần chỉ thông báo một lần/ngày cho CB đó. CB khác đạt ngưỡng hoặc một lần lệch từ mười trục thì thông báo mới; cùng lần đạt cả hai thì gộp. Lưu dấu chống báo lại qua đóng/mở app và tải bù. Cảnh báo đỏ trong Sự cố, chuông sáu hồi theo cài đặt; ghi đúng số thừa/thiếu, chuẩn chung, số đếm. Chi tiết đường ngang có thống kê theo quyết định giao diện cuối bên dưới. Xem `docs/UI_REVIEW_0.12.md` cho kết quả kiểm tra, ảnh tổng hợp và giới hạn.

Chủ dự án sửa lại ngưỡng đơn lẻ ngay trước khi đóng gói: **lệch 10 trục trở lên**, bao gồm đúng 10, thay cho cách diễn đạt “quá 10” trước đó. Code, dấu thông báo và bài kiểm tra biên đều theo ngưỡng chốt này.

Chốt giao diện cuối sau khi xem lại danh sách thay đổi: chủ dự án yêu cầu chỉ một mục **Đếm trục lỗi**, thống kê các CB đã đạt ngưỡng trong ngày, đặt trong chi tiết đường ngang. Bỏ hai mục/trang Đếm trục hôm nay và Xem các lần đếm trục. Chủ dự án xác nhận mục này là thống kê; khi đạt ngưỡng vẫn phải thông báo/đỏ/chuông như quy tắc đã chốt, không chỉ thêm vào bảng. Tổng quan không thêm bảng thống kê riêng.

Chủ dự án đã duyệt phương án cuối và yêu cầu đăng bản 0.12 lên kho GitHub đang dùng để tải APK.


## 17. Ngoại lệ 320+445 và tốc độ đổi tab ở 0.13

Chủ dự án nhắc lại Km 320+445 có nhiều hơn sáu cảm biến và cơ chế đếm riêng: bỏ toàn bộ đối chiếu/ghi chú/thống kê/cảnh báo trục suy ra ở điểm này. Giữ dòng đỏ gốc website và các tính năng tàu, khôi phục, tự kích. Phải xử lý cả cảnh báo trục đã lưu ở bản cũ khi cài đè, không chỉ chặn bản tin mới.

Chủ dự án phản hồi đổi tab mất khoảng 1–2 giây và yêu cầu tăng tốc, đồng thời xác nhận chỉ tối ưu, không giảm tính năng. Kiểm tra thấy giao diện đọc cả ngày nhiều lần, dựng lại tuyến cho từng sự kiện và quét lại toàn bộ sự kiện cho mỗi đường ngang. Sửa bằng bộ dữ liệu dùng chung trong một lượt dựng/cập nhật, nhóm sự kiện một lần, tái sử dụng mẫu nhận diện thiết bị; bộ dữ liệu tạm được xóa ở lượt cập nhật tiếp theo để dữ liệu vẫn mới. Giữ chu kỳ web, quy tắc cảnh báo, chuông và sơ đồ.

Chủ dự án đã tìm thấy mục Đếm trục lỗi trong chi tiết và không yêu cầu đổi bố cục của mục này. Mục chỉ hiện khi có CB đạt ngưỡng; ngoại lệ 320+445 không hiện mục thống kê này. Kết quả kiểm tra và đo tốc độ ở `docs/UI_REVIEW_0.13.md`.

## 18. Thiết kế Stitch và hiệu ứng — 0.14

Chủ dự án hỏi về Google Stitch, muốn mang đặc tả sang thiết kế giao diện rồi áp dụng trở lại app hiện có. Chủ dự án muốn viền sáng nhẹ và logo nhấp nháy; đã thống nhất dùng nhịp sáng dịu, không làm nháy trắng hoặc chuyển động tàu giả. Chủ dự án gửi bảng màu dark telemetry, JSON mẫu và ảnh riêng của bốn tab, sau đó yêu cầu thực hiện linh hoạt, căn theo app đã code, không cần sao chép chính xác từng chi tiết.

Đã chỉ rõ số liệu mẫu không nhất quán (11 đường ngang/2 ga so với số 14), pin/góc cần/hướng cố định không có nguồn web, lỗi điện áp phải giữ đỏ, nguyên văn thiết bị và ngoại lệ 320+445 phải giữ. JSON chỉ là minh họa, không phải API Visitec. Sự cố Tất cả vẫn xếp theo giờ phát sinh, không nhóm đỏ trước xanh. Bản 0.14 chỉ đổi lớp giao diện Android Java, giữ toàn bộ quy tắc nghiệp vụ và khóa ký. Chi tiết kiểm tra/ảnh tổng hợp tại docs/UI_REVIEW_0.14.md.

## 19. Mở rộng ngoại lệ trục — 0.15

Chủ dự án yêu cầu thêm 307+507, 309+056, 280+500, 343+350 và 338+900 vào danh sách không tính lệch trục. Đã áp dụng như ngoại lệ 320+445: không đối chiếu/ghi chú/thống kê/hiển thị dữ liệu trục hoặc cảnh báo trục suy ra; giữ toàn bộ dòng đỏ gốc, tàu và các tính năng khác. Nâng cache trục từ 2 lên 3 để dọn các cảnh báo trục cũ tại cả sáu điểm kể cả trước khi đọc web mới. Không xóa raw, cấu hình hoặc dấu chống báo lặp của điểm khác. Xem docs/UI_REVIEW_0.15.md cho kiểm chứng và APK.

## 20. Đối chiếu báo tự kích và xác nhận quy trình mới — 0.16

Chủ dự án báo một lượt nghi tự kích và yêu cầu chỉ xem web/phân tích trước. Đã đăng nhập bằng tài khoản được cung cấp, đọc đủ phân trang hai ngày và phát lại code cũ: thiếu dòng tiếp cận tại điểm phía trước làm sinh nghi vấn dù có hoạt động cần chắn và bản tin đã qua. Không lưu dữ liệu vận hành hoặc thông tin đăng nhập vào Git.

Chủ dự án yêu cầu tính thêm cần chắn đã hạ/đã nâng hoặc tàu đã qua ở điểm phía trước làm bằng chứng. Đã xác nhận chỉ xét trong lượt đang xét, bản tin đã qua vẫn cùng hướng, bằng chứng bổ sung làm gỡ riêng nghi vấn. Chủ dự án giải thích cách gọi “cần chắn hạ/nâng” bao gồm các số cần khác nhau trên web và sau đó yêu cầu sửa app. Đồng thời chốt cách làm việc: trình bày lại cách hiểu, chờ xác nhận trước khi chỉnh app trong các cuộc trao đổi yêu cầu. Chi tiết triển khai/kiểm tra tại docs/UI_REVIEW_0.16.md.

## 21. Ga Nghi Long cho khu vực Chosy — 0.17

Ngày 25/10/2026, chủ dự án xác nhận Km 303+350 là ga Nghi Long mới, chưa có trong danh sách website. Đã nhắc lại cách hiểu, hỏi rõ thiếu mốc hay chỉ thiếu nhãn ga; chủ dự án xác nhận web chưa cập nhật và yêu cầu thêm vào app, riêng ID Chosy. Xử lý như các ga hiện có: chèn đúng lý trình, đoạn tàu giáp ga tắt theo khôi phục đường ngang, xuất hiện khi tàu từ ga ra được đường ngang báo, ngắt đối chiếu tự kích qua ga. Không coi là đường ngang, không tạo dữ liệu vận hành ga giả hoặc thay đổi tài khoản khác.

Bản 0.17 áp dụng bổ sung vào tuyến từ web sau khi hoàn thành kiểm tra phân trang và vào tuyến cache khi cài đè. Dựng lại sơ đồ theo topology mới, đánh giá lại nghi tự kích đã lưu, giữ dòng đỏ thật và các quy tắc khác. Nếu web có mốc cùng chainage thì ưu tiên website, tránh chèn trùng. ID Chosy trong cấu hình là khóa chọn ngoại lệ nghiệp vụ do chủ dự án yêu cầu, không phải tài khoản/mật khẩu đăng nhập được điền sẵn. Chi tiết kiểm tra và giới hạn tại docs/UI_REVIEW_0.17.md.

## 22. Rút gọn nhãn Nghi Long thành NL — 0.18

Chủ dự án xác nhận vị trí ga đúng nhưng tên dài che mốc bên cạnh, yêu cầu ghi tắt NL. Bản 0.18 chỉ đổi nhãn hiển thị thành Ga NL trên sơ đồ và khi chạm vào ga; giữ khóa tuyến/cache Nghi Long và toàn bộ quy tắc hiện có. Yêu cầu này đã có chỉ thị sửa rõ, không cần hỏi lại. Kiểm tra tại docs/UI_REVIEW_0.18.md.

## 23. Thiếu lượt tàu gây treo sơ đồ và đồng bộ lại theo lượt sau — 0.19

Ngày 26/10/2026, chủ dự án gửi ảnh đoạn 320+957–320+445 giữ sáng và yêu cầu xem web, tìm nguyên nhân, trao đổi phương án trước khi sửa. Đã đọc đủ hai ngày báo cáo thật và tái hiện: một lượt thiếu tiếp cận/đã qua tại điểm sau dù có cần chắn và khôi phục; hàng chờ cũ sau đó lấy mất bản tin của lượt mới. Dữ liệu vận hành chỉ giữ riêng ngoài Git, không đưa vào ví dụ công khai.

Chủ dự án bổ sung trường hợp lỗi thiết bị/đường truyền làm mất cả lượt: lượt thật kế tiếp có bằng chứng đi qua phải giúp sơ đồ tự đồng bộ lại, không treo cả buổi. Đã diễn đạt lại và nhận xác nhận, sau đó tổng hợp bốn phần và được trả lời OK triển khai: dùng hoạt động/khôi phục phù hợp khi thiếu bản tin tàu; ghép đúng lượt cũ/mới; hồi phục sau một lượt thiếu hoàn toàn nhờ lượt kế tiếp; dựng lại cache cũ khi cài đè. Giữ tàu đang giữa hai điểm không hết hạn theo giờ, giữ tàu khác và các lỗi đỏ thật tới khôi phục đúng thiết bị.

Bản 0.19 dùng lượt nội bộ từ cần chắn khi có tàu đi tới từ đúng điểm kế cận, không tạo raw giả hoặc tự tạo tàu từ thao tác bảo dưỡng. Khi có bản tin tiếp cận rõ ràng trong lượt đó thì chuyển sang ID bản tin gốc, không tạo tàu thứ hai. Bằng chứng đã qua/khôi phục xác nhận mới đồng bộ các dấu cũ cùng đoạn/hướng, giữ các lượt phía sau. Nâng `travelVersion=4`; giữ cache tự kích/trục, giao diện và chữ ký. Kiểm chứng tại docs/UI_REVIEW_0.19.md.

## 29/10/2026 — Thử thiết bị trước lượt tàu và màu sơ đồ

Chủ dự án gửi ảnh báo nhầm tự kích. Phát lại cục bộ đúng chuỗi bản tin trong ảnh tái hiện lỗi: thao tác hạ/khôi phục/nâng không có hướng ở điểm hiện tại loại mất bằng chứng tiếp cận thật phía trước. Chủ dự án OK sửa, sau đó xác nhận thao tác đó do người kiểm tra ấn nút thử. Bản 0.20 giữ bằng chứng tiếp cận rõ ràng còn tiếp diễn qua chuỗi thử, giữ chặn bằng chứng cũ đã kết thúc và dùng một lần. Đánh giá lại cache suy luận phiên bản 3, giữ lỗi gốc/lịch sử thật và khóa ký.

Chủ dự án yêu cầu tàu trên sơ đồ luôn xanh: chỉ đổi màu đoạn tàu/mũi tên khi dữ liệu cũ thành xanh; mốc ga vẫn vàng. Cảnh báo dữ liệu cũ/thời điểm cập nhật và trạng thái LIVE vẫn phản ánh độ mới. Không đăng nhập web trong lượt này; ảnh và công cụ phát lại riêng không nằm trong Git. Xem docs/UI_REVIEW_0.20.md.

Kiểm tra 0.20: 564 quy tắc, 156 lưu trữ, 61 UI cập nhật, 21 telemetry UI đạt; build/Lint 0 lỗi, 15 cảnh báo. Cài đè từ cache suy luận phiên bản 2 gỡ đúng cảnh báo sai và giữ raw/lỗi thiết bị. Kiểm tra bitmap xác nhận tàu xanh khi dữ liệu cũ, vẫn có nhãn cảnh báo. Giữ khóa ký cũ; chưa có kiểm chứng Xiaomi mới.

## 31/10/2026 — Lưu 15 ngày, khung lịch sử từng lượt và bỏ thử chuông

Chủ dự án hỏi thời hạn lưu; bản 0.20 chưa tự dọn. Chủ dự án chọn 15 ngày, yêu cầu thay Xem trên sơ đồ tuyến bằng Chi tiết để đọc toàn bộ sự kiện của đường ngang trong ngày; mỗi lượt khung có viền nhẹ, lượt mới tách riêng dù lượt trước lỗi/thiếu kết thúc. Tách khung không xóa lỗi thiết bị thật. Bỏ nút Thử chuông báo lỗi trong Cài đặt. Đã nhắc lại và nhận OK triển khai/phát hành thông thường.

Trong lúc thực hiện, chủ dự án yêu cầu gom những bản tin thiếu kết thúc gần nhau. Đã hỏi rõ và nhận xác nhận: chỉ gom bản tin rời chưa rõ lượt; vẫn tách theo bản tin Tàu lẻ/Tàu chẵn mới. Cụm rời tối đa cách nhau 5 phút, tổng dài tối đa 15 phút. Giữ 15 ngày theo ngày Việt Nam tính cả hôm nay, checkpoint tàu trước khi xóa và thu hồi dung lượng tệp; tải bù không nhập lại bản tin cũ hơn mốc. Chi tiết triển khai/kiểm tra tại docs/UI_REVIEW_0.21.md.

Kiểm tra 0.21: 579 quy tắc; 168 lưu trữ và 21 telemetry UI trên APK phát hành; 69 UI cập nhật trên cùng mã giao diện trước bổ sung đồng bộ checkpoint trong Store. Smoke service đồng bộ đầu xác nhận mốc 15 ngày và không phát lỗi giả; không chạy lại toàn bộ 25/29 ca. Build/Lint 0 lỗi, 15 cảnh báo; chữ ký giữ nguyên. Đã xem ảnh khung lịch sử bằng dữ liệu tổng hợp; chưa có kiểm chứng Xiaomi mới.

## 31/10/2026 — Loại mốc thao tác khỏi lượt tàu thật, chỉ sửa trường hợp thử thiết bị

Chủ dự án gửi ảnh báo nhầm. Môi trường đọc Visitec bị chặn 403 Domain forbidden; không vượt chặn, dùng ảnh và phát lại cục bộ. Tái hiện hai tình huống khác nhau, nhưng chủ dự án yêu cầu để nguyên trường hợp phía trước mất kết nối, chỉ sửa việc thử thiết bị làm loại mất bằng chứng nguồn đã khôi phục. Sau nhiều lần làm rõ, chốt một thao tác hay cả loạt nút bấm/hạ-nâng cần/khôi phục không tự là một đoàn tàu hoặc mốc chia lượt thực. Chủ dự án yêu cầu sửa và test.

Bản 0.22 giữ mốc lượt có hướng riêng khỏi hoạt động thiết bị, giữ nguồn tàu rõ hướng chưa sử dụng dù đã khôi phục; đúng hướng và một nguồn chỉ dùng một lần. Không thay xử lý mất kết nối hay tự sinh tàu từ thao tác. Nâng cache suy luận lên 4, giữ lỗi gốc và checkpoint của lượt có raw đã quá 15 ngày khi phát lại suy luận. Công cụ phát lại thật ngoài Git, các test công khai dùng số liệu tổng hợp. Xem docs/UI_REVIEW_0.22.md.

Kiểm tra 0.22: 611 quy tắc, 171 lưu trữ, 69 UI cập nhật đạt; 2 ca dịch vụ mới trên chu kỳ 30 giây xác nhận không báo/chuông giả khi chuỗi thử xen giữa, nguồn chỉ dùng một lần và cảnh báo mới vẫn phát âm. Đợi lastSuccess trước kiểm tra lastAlarm để tránh đọc giữa lượt cập nhật; giữ cùng APK. Build/Lint 0 lỗi, 15 cảnh báo; cùng chữ ký, chưa có kiểm chứng Xiaomi mới. Chủ dự án yêu cầu giữ nguyên phần mất kết nối, ca canary vẫn cho kết quả cũ.
