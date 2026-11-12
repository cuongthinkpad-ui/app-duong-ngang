# ĐẶC TẢ ĐẦY ĐỦ ĐỂ VIẾT LẠI GIÁM SÁT ĐƯỜNG NGANG THÀNH WEB

**Ngày chốt: 12/11/2026, giờ Việt Nam.**

**Chuẩn nghiệp vụ: app Android 0.27.3, versionCode 30.**

**Mã chuẩn Android: `ee1affe5ea024590ce356e0f1e23aafbf17df669`.**

**Repository: https://github.com/cuongthinkpad-ui/app-duong-ngang**

**Người sở hữu/yêu cầu: Cao Xuân Cường.**

Tài liệu này dùng để giao cho Google AI Studio hoặc nhóm lập trình **viết một ứng dụng web mới**, giữ giao diện, dữ liệu và quy tắc đang có của app Android. Không phải yêu cầu sửa APK. Không phải yêu cầu dựng màn hình minh họa. Không coi bản web đã gửi trước là chuẩn nghiệp vụ đã được chủ sở hữu nghiệm thu.

Tài liệu được tổng hợp từ mã Android, tài liệu các phiên trước và yêu cầu trong cuộc trao đổi hiện tại. Đây không phải bản xuất nguyên văn toàn bộ chat. Không có mã của sản phẩm AI Studio vừa làm sai để kết luận nó sai ở đâu; yêu cầu dưới đây xác định sản phẩm mới phải làm gì.

## Cách dùng tài liệu và thứ tự ưu tiên

1. Đọc phần 0 và 1 trước khi chọn kiến trúc.
2. Đọc hết phần liên kết website và dữ liệu trước khi dựng cảnh báo.
3. Đọc cả ba phần riêng: trạng thái thiết bị, lượt tàu/sơ đồ, suy luận tự kích. Không gộp chúng thành một biến `status`.
4. Đọc quy tắc đếm trục, âm thanh và lưu trữ. Chúng là chức năng bắt buộc, không phải tính năng phụ có thể bỏ cho nhanh.
5. Làm từng màn hình theo phần giao diện và chạy ma trận nghiệm thu cuối tài liệu.
6. Đối chiếu mã nguồn tham chiếu và bộ kiểm thử đi kèm khi chuyển thuật toán.

Thứ tự giải quyết mâu thuẫn:

- Yêu cầu mới nhất của chủ sở hữu trong phiên hiện tại.
- Các yêu cầu hiện hành được chốt trong tài liệu này.
- Hành vi mã Android 0.27.3 và bộ kiểm thử, đối với chi tiết chưa diễn đạt hết bằng văn bản.
- Biên bản 0.27.3, 0.27.2, 0.27 rồi các biên bản cũ.
- Tài liệu lịch sử chỉ giải thích lý do, không được khôi phục yêu cầu đã bị thay thế.

Trong tài liệu cũ có câu “không chuyển app thành web nếu chưa được yêu cầu”. **Chủ sở hữu hiện đã yêu cầu làm web riêng**, vì vậy được làm web. Vẫn không được sửa app Android. Các nguồn Java trong gói tham chiếu để hiểu/kiểm chứng hành vi, không phải lệnh bắt phải build APK.

---

## Mục lục

0. Đầu việc phải bàn giao và những thứ không được tự đổi
1. Bối cảnh công việc và mô hình thiết bị đường ngang
2. Thuật ngữ, giờ Việt Nam và các lớp trạng thái
3. Liên kết website chủ: URL, đăng nhập, ASP.NET, cookie và form
4. Đọc toàn tuyến, chọn ngày và phân trang an toàn
5. Chuyển HTML thành bản tin, nhận màu và nhận dạng ID
6. Quy trình đồng bộ, chu kỳ và kết nối lại
7. Tuyến tài khoản, phạm vi, ga và ngoại lệ Ga NL
8. Lỗi thiết bị, ghép khôi phục và reset ngày
9. Nguyên lý lượt tàu, hướng chạy và đoạn sáng
10. Ghép lượt, thiếu bản tin, lượt treo và nhiều tàu
11. Suy luận Nghi tự kích và bằng chứng liên kết giữa các điểm
12. Đếm trục: dữ liệu, chuẩn, ngưỡng, ngoại lệ và thống kê
13. Âm thanh, giọng Việt, im lặng và XÁC NHẬN
14. Lịch sử 30 ngày, checkpoint, sửa dữ liệu cũ và đổi tài khoản
15. Giao diện chung và bố cục Tổng quan
16. Danh sách đường ngang, chi tiết thiết bị và lịch sử từng lượt
17. Tab Sự cố, Các lượt tàu và Cài đặt
18. Kiến trúc web/iPhone và phân biệt với Android
19. Hợp đồng dữ liệu/API đề xuất cho bản viết mới
20. Triển khai, bảo mật và khả năng chẩn đoán
21. Những yêu cầu cũ đã bị thay thế và lỗi tuyệt đối không được tái tạo
22. Các ví dụ nghiệp vụ có đầu vào và đầu ra cụ thể
23. Ma trận nghiệm thu và bằng chứng phải nộp
24. Bản đồ mã nguồn, tài liệu và cách chuyển thuật toán
25. Prompt giao việc nguyên khối cho AI Studio

---

## 0. Đầu việc phải bàn giao và những thứ không được tự đổi

### 0.1. Kết quả cần có

Một web thật dùng được trên điện thoại, ưu tiên iPhone, gồm:

- Giao diện tiếng Việt theo app hiện có, bốn tab ở dưới: **Tổng quan / Đường ngang / Sự cố / Cài đặt**.
- Đăng nhập website báo cáo bằng URL, ID, mật khẩu của từng người.
- Backend thật đọc báo cáo từ website chủ, lưu dữ liệu và chạy quy tắc ngay cả khi người dùng không mở trang.
- Toàn bộ quy tắc tàu, sự cố, khôi phục, tự kích, đếm trục và lịch sử được giữ.
- Cảnh báo có chống trùng; XÁC NHẬN chỉ dừng đợt đang báo.
- Kết nối lại tự động khi mất mạng, dữ liệu cũ được giữ và được ghi rõ là cũ.
- PWA thêm ra màn hình chính iPhone; Web Push nếu thiết bị đủ điều kiện và người dùng cấp quyền.
- Lưu trữ bền vững, không mất tài khoản/lịch sử/đăng ký Push khi redeploy.
- URL HTTPS chạy thật, mã nguồn đầy đủ và báo cáo nghiệm thu từng nhóm chức năng.

### 0.2. Chỉ tiêu “giống app”

“Giống app” có nghĩa đồng thời đúng **bố cục + nhãn + thao tác + dữ liệu + quy tắc + hành vi sau khi đóng/mở lại**. Chỉ vẽ bốn màn hình có màu tương tự thì chưa đạt. Chỉ lấy được vài dòng website nhưng bỏ quy tắc liên kết tàu cũng chưa đạt.

Không dùng dữ liệu demo để giả kết nối; không sinh ngẫu nhiên số tàu/số lỗi; không thay lỗi gốc bằng mô tả do AI viết. Không bỏ các ca khó với lý do “MVP”, “bản nhẹ” hoặc “sẽ bổ sung sau” rồi báo hoàn thành.

### 0.3. Phạm vi được phép

Được chọn framework/ngôn ngữ cho bản web mới nếu bảo toàn hành vi và chứng minh bằng kiểm thử. Có thể giữ Java core trong backend hoặc chuyển nguyên thuật toán sang ngôn ngữ khác. Không cần và không được yêu cầu khóa ký APK để triển khai web.

Giữ nguyên mã, APK, chữ ký, dữ liệu và cách hoạt động của app Android. Tách sản phẩm web bằng thư mục/dự án/service riêng. Không đưa bản web vào thay APK. Không sửa `main` Android để có preview web.

### 0.4. Chưa được hứa trước

Không hứa Safari chạy timer JavaScript liên tục khi khóa màn hình. Không hứa Push có thể tự chạy giọng đọc tùy ý. Việc Android phát chuông/TTS/rung nền là hành vi chuẩn Android; khả năng thực hiện tương ứng trên iPhone phải được thiết kế và nghiệm thu riêng ở phần 18.

---

## 1. Bối cảnh công việc và mô hình thiết bị đường ngang

### 1.1. Mục đích ứng dụng

Ứng dụng **Giám sát Đường ngang** phục vụ người theo dõi hệ thống đường ngang cảnh báo tự động. Dòng đơn vị dưới tiêu đề đúng là:

**Công ty CP TTTH Đường sắt Vinh**

Website công ty cho tài khoản quyền xem báo cáo. App chỉ đọc và phân tích báo cáo, không gửi lệnh điều khiển thiết bị hiện trường. Không có yêu cầu mở/đóng cần chắn, tác động chuông hoặc phục hồi thiết bị qua web.

Nhiều người có thể dùng sản phẩm với tài khoản và khu vực khác nhau. Tài khoản đầu tiên chỉ là ví dụ, không phải danh sách điểm cố định cho mọi người.

### 1.2. Lý trình và hướng chạy

Lý trình tăng từ **Bắc vào Nam**:

| Khái niệm | Hướng địa lý | Lý trình | Mũi tên trên sơ đồ từ trái sang phải tăng km |
|---|---|---|---|
| Tàu lẻ | Bắc → Nam | Tăng | `Tàu lẻ →` |
| Tàu chẵn | Nam → Bắc | Giảm | `Tàu chẵn ←` |

Đầu Bắc đặt bên trái, đầu Nam bên phải trên sơ đồ ngang. Không đảo hướng chỉ vì nhìn từ điện thoại theo tư thế khác. Không suy ra hướng từ chữ “cần chắn”, từ màu đỏ hoặc từ vị trí bản tin trong bảng.

### 1.3. Vị trí các cảm biến theo giải thích của chủ sở hữu

```text
BẮC / phía lẻ                                           NAM / phía chẵn
        CB1–CB2  ---------  CB3–CB4  ---------  CB5–CB6
      hơn 1 km về Bắc      tại đường ngang      hơn 1 km về Nam
Tàu lẻ:      ───────────────────────────────────────────────→
Tàu chẵn:    ←───────────────────────────────────────────────
```

CB1 và CB2 gần nhau khoảng 1 m; CB5 và CB6 cũng tương tự. Tàu lẻ gặp CB1/2 trước, qua CB3/4 rồi CB5/6; tàu chẵn đi ngược lại. Đây là mô hình nghiệp vụ, không phải dữ liệu tọa độ GPS hay tốc độ tàu để tính vị trí nội suy.

### 1.4. Điểm rất quan trọng: cần chắn nâng không có nghĩa toàn hệ thống đã khôi phục

Khi đuôi tàu qua CB3/CB4, cần chắn có thể nâng. Tuy nhiên vùng cảm biến hai đầu vẫn đang có quá trình chiếm dụng. Sau khi tàu ra đủ vùng cảm biến, website mới ghi **Khôi phục hệ thống**, có thể chậm khoảng một phút theo giải thích thực tế.

Vì vậy:

- Không xóa chiếm dụng phần cứng khi thấy “Cần chắn … đã nâng”.
- Không xóa lỗi chuông/đèn/cảm biến khi thấy cần chắn nâng.
- Không dùng một timer một phút để tự sinh “Khôi phục hệ thống”.
- Không coi khoảng một phút là hằng số thuật toán hoặc cam kết độ trễ.

### 1.5. Ba thứ khác nhau phải tồn tại song song

1. **Chiếm dụng phần cứng tại điểm**: điểm có báo tàu, chỉ giải phóng theo Khôi phục hệ thống tại đúng điểm.
2. **Lượt tàu và đoạn sáng trên sơ đồ**: liên kết các sự kiện để biểu diễn hành trình liên tục qua nhiều điểm.
3. **Lỗi thiết bị/suy luận cảnh báo**: có vòng đời khôi phục riêng, không phải cờ cho biết có tàu hay không.

Một đường ngang đã khôi phục phần cứng vẫn có thể có tàu đang đi ở đoạn giữa nó và đường ngang kế tiếp. Một lượt tàu kết thúc không có nghĩa Chuông 2 đã hết hỏng. Một cảnh báo được xác nhận không có nghĩa thiết bị đã phục hồi.

---

## 2. Thuật ngữ, giờ Việt Nam và các lớp trạng thái

### 2.1. Đồng hồ và ngày

- Múi giờ nghiệp vụ cố định: **Asia/Ho_Chi_Minh, UTC+7**.
- Ngày nghiệp vụ không phụ thuộc timezone hệ điều hành server hoặc điện thoại.
- Dữ liệu thời gian nội bộ dùng epoch **mili giây**, kiểu đủ 64 bit.
- Ngày gửi/nhận API nội bộ có thể dùng ISO `YYYY-MM-DD`; giao diện hiển thị `dd/MM/yyyy`.
- Giờ bản tin hiển thị `HH:mm:ss` theo Việt Nam.
- Reset ngày lúc 00:00 Việt Nam. Không reset theo 00:00 UTC.
- Khoảng một ngày truy vấn dùng `[00:00 ngày D, 00:00 ngày D+1)`, tránh mất bản tin đúng ranh giới.

Ví dụ ngày hiện tại 12/11/2026 thì 30 ngày tính cả hôm nay là **14/10/2026 đến 12/11/2026**, gồm hai đầu. Không hiểu thành “hôm nay trừ 30” tạo 31 ngày.

### 2.2. Các đối tượng tối thiểu

| Đối tượng | Nghĩa | Không được dùng thay cho |
|---|---|---|
| Account namespace | URL website + ID tài khoản, hoặc khóa nội bộ tách biệt tương đương | Một biến tài khoản toàn cục cho tất cả người dùng |
| Route/topology | Danh sách đầy đủ các đường ngang và ga của tài khoản | Danh sách điểm vừa có sự kiện hôm nay |
| Scope | Phạm vi người dùng chọn từ đường ngang đến đường ngang | Tuyến nguồn bị cắt mất hàng xóm |
| Raw event | Bản tin nguyên gốc lấy từ website | Câu tóm tắt do AI tạo |
| Derived event | Nghi tự kích hoặc cảnh báo đếm trục do app suy ra | Bản tin thật của website |
| Active fault | Lỗi đang mở, ghép theo điểm và thiết bị/loại lỗi | Mọi dòng đỏ từng xuất hiện |
| Incident | Một lần phát sinh lỗi kèm bản tin khôi phục nếu có | Chỉ bản tin cuối cùng của thiết bị |
| Evidence visit | Một lượt bằng chứng để đối chiếu tàu | Một dòng hạ cần riêng lẻ |
| Journey | Trạng thái một lượt đi qua một điểm/đoạn có hướng | Số hiệu hoặc danh tính đoàn tàu thực |
| Track segment | Đoạn cần tô xanh, có hướng | GPS, số đoàn tàu hoặc mọi điểm đang chiếm dụng |
| Checkpoint | Trạng thái đã lưu để tiếp tục khi raw cũ bị dọn | Cache có thể xóa tùy ý |
| Alert batch | Đợt âm thanh/giọng đọc đang xử lý | Mọi lỗi đang mở trong DB |
| Day complete | Đã đọc đủ và xác thực ngày đó, đã tính dẫn xuất | HTTP 200, trang đầu, hay mảng rỗng bất kỳ |
| Last success | Lần hoàn tất đọc, kiểm tra và lưu dữ liệu thành công | Lần mở app, lần gửi request hoặc lần đọc cache |

### 2.3. Độ mới dữ liệu

Chuẩn Android xem dữ liệu là cũ nếu chưa có thành công, hoặc quá **60 giây** kể từ lần thành công ở chu kỳ 30 giây, hoặc trạng thái cập nhật không còn thành công hợp lệ. Cờ mất kết nối cũng loại LIVE. Đừng lấy một mốc 90 giây trong bản web tham khảo làm yêu cầu chuẩn nếu đang cần khớp app.

Phải thể hiện được tối thiểu: CHỜ, LIVE, CŨ, DỪNG, LỊCH SỬ. Có dữ liệu trong bộ nhớ không đồng nghĩa LIVE.

---

## 3. Liên kết website chủ: URL, đăng nhập, ASP.NET, cookie và form

### 3.1. Website nguồn đã biết

**Địa chỉ đã dùng trong dự án: `http://visitec.vn:7447/`**.

Website báo cáo dùng HTML/ASP.NET; trong tài liệu các phiên trước có trang báo cáo `Default.aspx`. Không có bằng chứng website cung cấp API JSON công khai. Không được tự viết frontend gọi `/api/trains`, `/api/incidents` trên Visitec rồi cho rằng đó là API thật.

Mã chuẩn vào từ URL cấu hình, đọc HTML và theo **form action/đường dẫn thực tế nhận được**. Không ép mọi website đều có `/Default.aspx` nếu HTML trả về không như vậy. Nếu base URL có thư mục con, phải resolve URL tương đối theo vị trí trang/form.

URL/ID/mật khẩu do người dùng nhập. Không nhúng tài khoản thử đã từng trao đổi vào source, giá trị mặc định hoặc ví dụ công khai.

### 3.2. Sơ đồ liên kết thật cần có ở bản web

```text
Safari/PWA iPhone ──HTTPS──> Backend của web mới
                                  │
                                  ├─ HTTP/HTTPS đến website nguồn do người dùng cấu hình
                                  │    ├─ GET trang đầu
                                  │    ├─ POST đăng nhập nếu có form password
                                  │    ├─ Cookie phiên ASP.NET
                                  │    ├─ POST chọn ngày + Tất cả điểm + Tất cả bản tin
                                  │    └─ GET/POST phân trang và kiểm tra lại đầu bảng
                                  │
                                  ├─ DB bền vững: raw + route + lỗi + checkpoint + chống báo trùng
                                  ├─ Bộ quy tắc đường ngang/tàu/ga/cảm biến
                                  └─ Push provider ──> thông báo iPhone khi được cấp quyền
```

Frontend không trực tiếp cào website HTTP từ trang HTTPS: có mixed content, CORS, cookie cross-origin và giới hạn chạy nền. Giải pháp là backend, không yêu cầu người dùng tắt bảo mật Safari.

### 3.3. Quy trình đăng nhập phải theo mã chuẩn

1. GET URL cấu hình.
2. Cập nhật cookie nhận được, kể cả cookie từ redirect.
3. Nếu trang không có `input[type=password]`, tiếp tục kiểm tra xem có phải báo cáo hợp lệ; không tự nhận login thành công chỉ vì không có password.
4. Nếu có password, lấy form chứa chính ô đó.
5. Thu tất cả trường form cần thiết, gồm hidden ASP.NET như `__VIEWSTATE`, `__VIEWSTATEGENERATOR`, `__EVENTVALIDATION` nếu có. Không tự bịa giá trị.
6. Tìm ô tài khoản có `name`: text/email/input không type theo form hiện tại.
7. Chọn đúng nút đăng nhập. Ưu tiên nhãn chứa “đăng nhập”, “log in”, “login”. Nếu không có nhãn, chỉ dùng quy tắc chắc chắn tương đương mã chuẩn: cùng prefix ASP.NET với password và duy nhất, hoặc chỉ có một nút submit.
8. Không gửi nhầm nút Thoát/Đăng xuất/reset có cùng form. Lỗi này đã gặp trong lịch sử dự án.
9. Điền ID/mật khẩu vào đúng tên trường vừa đọc. POST tới action thực của form, hoặc URL trang nếu action trống.
10. Nếu vẫn còn form password sau submit, xem là lỗi xác thực; không tiếp tục parse nó thành ngày không có dữ liệu.
11. Khi phiên hết hạn, thực hiện lại đăng nhập trong lần đọc. Nếu sai tài khoản/mật khẩu thật, dừng theo dõi của tài khoản đó và báo rõ; không tự thử mật khẩu liên tục.

### 3.4. Quy tắc lấy form fields

- Giữ trường hidden và giá trị hiện tại của input/select/textarea.
- Bỏ các nút submit/button/image khi gom mặc định; chỉ thêm nút login đúng lúc đăng nhập.
- Với checkbox/radio, chỉ giữ lựa chọn checked, sau đó chủ động chọn radio **Tất cả** của bộ lọc bản tin khi đọc báo cáo.
- Với select, lấy option có `selected`; nếu không có, lấy option đầu theo hành vi form.
- Phân biệt label của option và `value` gửi đi: dùng value nếu có, không lấy nhãn hiển thị thay value.
- Sau mỗi trang/postback phải dùng hidden state của **trang vừa nhận**, không dùng VIEWSTATE cũ cho cả ngày.

### 3.5. Redirect, timeout và an toàn kết nối

Mã chuẩn:

- Timeout mỗi request: **25.000 ms**.
- Giới hạn body: **8 MiB mỗi phản hồi**.
- Không tự theo redirect mù; tối đa 5 bước redirect.
- Kiểm tra cùng scheme, hostname và port với website đã cấu hình trước khi gửi request/credential/cookie tiếp.
- Không gửi thông tin đăng nhập tới host khác chỉ vì HTML/redirect yêu cầu.
- Dừng request khi tác vụ bị hủy hoặc tài khoản đã đổi.

Backend công khai cần thêm kiểm tra URL để không thành proxy truy cập localhost, metadata hoặc mạng nội bộ tùy ý. Nếu cần website mới, thêm allowlist bằng cấu hình máy chủ; không nới quyền cho mọi URL chỉ để vượt một ca thử.

### 3.6. Những điều phải phân biệt khi báo lỗi

| Hiện tượng | Nghĩa có thể xác định | Cách xử lý |
|---|---|---|
| DNS/TCP timeout, refused, I/O, HTTP server không đáp ứng | Chưa kết nối/đọc được website nguồn | Giữ dữ liệu, báo mất kết nối, tự retry 120 giây |
| Vẫn form login sau khi gửi đúng cấu trúc | Đăng nhập chưa thành công | Báo lỗi xác thực, dừng thử liên tục |
| HTTP 200 nhưng không nhận ra bảng/ngày/tuyến | Không có snapshot báo cáo đáng tin cậy | Giữ dữ liệu, báo lỗi đọc/định dạng; không ghi rỗng |
| Trình duyệt không gọi được backend web mới | Mất đường từ điện thoại tới máy chủ web | Giữ dữ liệu đang hiện, báo riêng; không kết luận Visitec hỏng |
| Môi trường phát triển trả 403 Domain forbidden | Chính sách egress của môi trường | Chưa chứng minh Visitec từ chối tài khoản |

Ảnh chủ sở hữu đã gửi có lỗi `failed to connect to visitec.vn/203.210.197.165 (port 7447) ... after 25000ms`. Điều xác định được là request TCP không kết nối xong trong 25 giây. Ảnh đó **không chứng minh mật khẩu sai**. Địa chỉ IP trong ảnh chỉ là kết quả phân giải ở lần đó; không hard-code IP vào sản phẩm.

Chủ sở hữu yêu cầu: **Không yêu cầu cấu hình lại khi chưa xác định nguyên nhân lỗi.** Không mặc định bảo họ xóa app, đổi URL, nhập lại mật khẩu hoặc `/reset` để chữa timeout.

---

## 4. Đọc toàn tuyến, chọn ngày và phân trang an toàn

### 4.1. Luôn đọc toàn tuyến và tất cả loại bản tin

Phải chọn **Tất cả** ở:

- Danh sách lý trình/điểm của tài khoản.
- Bộ lọc loại bản tin, không chỉ “Trở ngại”.

Nếu chỉ tải dòng lỗi, sẽ mất Tàu lẻ/Tàu chẵn, cần chắn, đã qua, Khôi phục hệ thống, số trục và bản tin khôi phục thiết bị. Khi đó không thể dựng đúng sơ đồ, tự kích hoặc trạng thái đã khôi phục.

Không đặt phạm vi theo dõi của app trực tiếp lên website nguồn. Phải giữ bằng chứng phía ngoài phạm vi để đối chiếu và giữ sơ đồ toàn tuyến.

### 4.2. Nhận dạng ô ngày

- Ưu tiên tên input kết thúc bằng `txtNgay` không phân biệt hoa/thường.
- Nếu không có, tìm input có giá trị ngày hợp lệ hoặc type=date.
- Chấp nhận ISO `yyyy-MM-dd` và ngày Việt Nam có/không có số 0 đầu.
- `01/06/2026`, `1/6/2026`, `01/6/2026`, `1/06/2026` phải hiểu là cùng ngày.
- Bảng có ngày/tháng không năm thì dùng năm của ngày được yêu cầu, không tự dùng năm khác.
- Phải kiểm tra ngày thực, không cho 31/02 qua chỉ vì regex khớp.

Nếu ngày/filters đã đúng thì có thể đọc trang hiện tại. Nếu chưa đúng, lấy form fields và POST ngày với `__EVENTTARGET` là name của input ngày, `__EVENTARGUMENT` trống theo cấu trúc hiện tại. Input HTML date gửi ISO; input text gửi `dd/MM/yyyy`.

### 4.3. Đọc hết phân trang

Chuẩn hỗ trợ:

- Liên kết ASP.NET dạng `__doPostBack('controlName','Page$2')`.
- Liên kết href thường có số trang.
- Mỗi trang tiếp theo phải tiếp tục giữ đúng ngày, bộ lọc Tất cả và tuyến.
- Giới hạn tối đa 200 trang một lượt. Chạm giới hạn mà chưa đủ thì lỗi, không ghi “đã tải đủ”.

Không cào cố định 10 dòng/trang đầu. Không bỏ các dòng phía dưới hoặc các nhóm phía sau chỉ vì giao diện chỉ hiện vài thẻ.

### 4.4. Kiểm tra từng trang trước khi nhập

Mỗi trang phải thỏa:

1. Ngày của form khớp ngày yêu cầu.
2. Bộ lọc toàn tuyến và tất cả bản tin vẫn đúng.
3. Danh sách tuyến không thay đổi giữa các trang.
4. Dòng sự kiện có ngày khớp ngày yêu cầu.
5. Các điểm trong sự kiện thuộc danh sách tuyến đã xác thực.
6. Không lặp lại nguyên tập sự kiện của một trang trước.

Trang rỗng chỉ có thể coi là ngày không có sự kiện nếu nhận diện được bảng báo cáo chuẩn, ví dụ table có id kết thúc `Gdata`, hoặc dấu hiệu trang báo cáo đường ngang CBTĐ tương đương. Một trang HTML rỗng/trang lỗi/trang đăng nhập không phải “không có sự cố”.

### 4.5. Báo cáo đang tăng khi đọc nhiều trang

Trong lúc đọc, website có thể thêm bản tin đầu bảng, làm dồn dữ liệu sang trang khác. Chuẩn xử lý:

- Ghi nhớ các ID ở trang đầu đã đọc.
- Sau khi đọc từ trang 2 trở đi của ngày hôm nay, quay lại trang 1.
- Xác thực lại ngày/filters/tuyến và nhập thêm bản tin mới ở đầu.
- Phải còn ít nhất một ID chồng giữa trang đầu cũ và trang đầu vừa đọc; nếu không còn anchor thì snapshot có thể đã dịch quá xa.
- Khi mất anchor, đọc lại từ trang 1; tối đa hai lượt thử trong bộ đọc chuẩn.
- Nếu vẫn thay đổi quá nhanh, báo chưa hoàn tất và giữ snapshot cũ. Không công bố một ngày bị thiếu.

### 4.6. Đọc tăng thêm và làm mới đầy đủ

Sau snapshot đầy đủ, có thể đọc từ trang mới nhất đến khi gặp ID đã biết của **đúng tài khoản và ngày**. Có gặp overlap thì dừng sớm được, nhưng vẫn kiểm tra đầu bảng như trên nếu đã qua nhiều trang.

Khoảng **15 phút** làm mới đầy đủ ngày hiện tại để nhận bản tin tải bù/sửa muộn. Khi mới bắt đầu hoặc cần nối trạng thái, đọc ngày trước và ngày hiện tại. Qua ngày hoặc gián đoạn dài phải kiểm tra lại coverage và checkpoint.

**Tên trường `DayData.connected` trong mã Java có nghĩa đã nối được overlap với lịch sử `known` của lần đọc tăng thêm, không phải cờ TCP đang online.** `known` rỗng của lần full read thì trường này true. Nếu chạy hết ngày mà không gặp known, nó false và có thể cần đặt lại mốc theo dõi/coverage. Không dùng trường này làm spinner hoặc cờ mất mạng trên UI.

### 4.7. Công bố dữ liệu theo snapshot hoàn chỉnh

Tác vụ mạng có thể thu nhiều trang trong bộ nhớ/transaction tạm. Chỉ đưa vào trạng thái “Đã cập nhật” sau khi đủ xác thực, nhập DB, tính quy tắc và lưu trạng thái thành công. Nếu một trang ở giữa lỗi, giữ dữ liệu và lastSuccess cũ; không thay trạng thái bằng nửa ngày mới.

---

## 5. Chuyển HTML thành bản tin, nhận màu và nhận dạng ID

### 5.1. Trường bản tin

Một bản tin tối thiểu có:

```text
id         : khóa sự kiện trong namespace tài khoản
at         : thời điểm sự kiện, epoch milliseconds UTC từ ngày/giờ Việt Nam
crossing   : tên/lý trình điểm từ website
text       : nguyên văn nội dung website sau khi đọc text HTML
red        : nhận diện lỗi hiệu lực, gồm màu nguồn và hai nhóm lỗi nguồn đặc biệt
counts     : các giá trị CB theo thứ tự trong bảng, giữ dạng chuỗi khi chưa phân loại
```

Có thể lưu thêm sourceRed/raw metadata cho audit nội bộ, nhưng không thay text hiển thị. Không gửi raw HTML chứa thông tin đăng nhập cho frontend/log công khai.

### 5.2. Cách đọc dòng bảng theo adapter chuẩn

- Xét hàng `tr` và các ô `td` con trực tiếp, tránh lấy nhầm ô trong bảng lồng.
- Tối thiểu bốn ô.
- Ô đầu là lý trình dạng Km số+số; ô thứ hai là nội dung.
- Từ ô thứ ba trở đi tìm thời gian `HH:mm:ss`; ô ngay trước thời gian là ngày.
- Tối đa sáu ô ngay sau giờ là các số CB1–CB6.
- Kiểm tra ngày đúng với ngày được yêu cầu, rồi dựng timestamp theo giờ Việt Nam.
- Không đoán trạng thái từ tên file ảnh hoặc cột Status bên cạnh nếu nguồn sự kiện không nói vậy.

### 5.3. Nhận màu đỏ

Mã chuẩn xét thuộc tính `color` và `style` trên các phần tử trong dòng, nhận các cách biểu diễn:

- `red`
- `#ff0000`
- `#f00`
- `rgb(255,0,0)`

Không chỉ đọc màu của ô thứ nhất. Không dùng màu nền giao diện mới để đoán lỗi nguồn. Nếu website thật đổi cách tô bằng class/CSS khác, phải kiểm tra dữ liệu thật và sửa parser có test; không âm thầm coi toàn bộ bản tin là bình thường.

### 5.4. Nhận ID và chống trùng

Nếu hàng có `data-event-id` thì ưu tiên ID nguồn. Nếu không có, chuẩn dựng khóa từ:

```text
at + "|" + crossing + "|" + text + "|" + counts
```

Bản ghi hoàn toàn giống nhau cùng giây có thể bị gộp vì website không có ID độc lập. Không được hứa phân biệt hai bản tin mà nguồn không phân biệt.

Khóa DB phải gồm account namespace + event ID. Không dùng vị trí hàng, số trang, thời gian fetch hoặc UUID mới mỗi lần tải làm ID: các cách này làm báo lại chuông mỗi chu kỳ.

Các prefix dành riêng:

- `self:<ID bản tin nguồn>`: Nghi tự kích.
- `axle:<số CB>:<ID bản tin đo nguồn>`: cảnh báo đếm trục.
- `travel:motion:...`: lượt nội bộ phục vụ sơ đồ khi đủ bằng chứng chuyển động, **không phải raw event website**.

### 5.5. Thứ tự xử lý cùng thời điểm

Giữ xử lý thời gian tăng dần để dựng trạng thái. Với sự kiện cùng timestamp, mã có thứ tự pha nghiệp vụ: báo tàu → lỗi đỏ → hoạt động cần chắn → đã qua → kết thúc/khôi phục → bản tin khác. Ghép lịch sử và sơ đồ có tie-breaker riêng trong `Rules.java`. Không dùng thứ tự DOM ngẫu nhiên hoặc đảo mảng mà bỏ mất quy tắc cùng giây.

Danh sách hiển thị thì mới nhất trước. Phân biệt thứ tự **tính trạng thái** với thứ tự **đọc trên màn hình**.

---

## 6. Quy trình đồng bộ, chu kỳ và kết nối lại

### 6.1. Chu kỳ bình thường

Hiện hành chỉ **30 giây**, không có ba nút chọn 30 giây / 2 phút / 5 phút. Cấu hình cũ phải chuyển về 30 giây.

Mã Android tính thời gian đã dùng trong lượt để giữ nhịp xấp xỉ 30 giây từ lần bắt đầu trước, nhưng không chạy song song và không dồn các lượt bù. Ví dụ một lượt mất 8 giây thì khoảng chờ thường còn 22 giây. Một lượt lâu quá 30 giây không được sinh nhiều request cùng lúc để “đuổi kịp”.

Backend web phải giữ tinh thần này: một worker theo tài khoản, một lượt đọc có khóa/lease, không nhân số lượt theo số tab đang mở.

### 6.2. Khi mất kết nối

- Giữ route, raw, lỗi, đoạn tàu, lastSuccess và lựa chọn âm hiện tại.
- Đặt mất kết nối, bỏ LIVE, hiển thị dữ liệu cũ/thời điểm cuối.
- **120 giây tính từ khi lượt lỗi kết thúc**, không tính từ lúc bắt đầu request.
- Tự thử ở backend ngay cả khi không có trình duyệt mở.
- Mỗi lần vẫn thất bại thì chờ tiếp 120 giây từ lần thất bại vừa kết thúc.
- Không tự xóa dữ liệu và không tự đổi URL/ID/mật khẩu.
- Không xóa suy luận chỉ vì mạng lỗi; đề xuất miễn tự kích do mất kết nối đã bị chủ sở hữu từ chối trước đây.

Ví dụ: request bắt đầu 14:00:00, timeout kết thúc 14:00:25 → lần tự thử kế tiếp khoảng **14:02:25**, không phải 14:02:00.

### 6.3. Retry bằng tay

Nút **Kết nối lại** bỏ thời gian chờ và thử ngay. Trong lúc đang có lượt đọc thì không mở worker mới; có thể ghi nhận một yêu cầu retry tiếp theo theo quy tắc lịch duy nhất. Nút tạm vô hiệu khi đang thử. Callback của tác vụ cũ không được đặt lịch mới sau khi đổi tài khoản/restart/dừng.

Chỉ xóa trạng thái mất kết nối khi **đọc và lưu thành công**. Đọc cache thành công hoặc bấm nút không phải bằng chứng đã nối lại website.

### 6.4. Lỗi đọc cấu trúc và lỗi xác thực

Lỗi không nhận diện được trang, ngày không khớp, thiếu phân trang: giữ snapshot cũ và báo chưa đọc đủ. Không gọi nó là ngày không có sự cố. Phân loại lỗi này khác với sai mật khẩu và timeout.

Lỗi xác thực thật dừng theo dõi tài khoản đó để tránh thử liên tục. Dữ liệu đã lưu không bị xóa. Người dùng vẫn có thể vào Cài đặt sửa đúng thông tin.

### 6.5. Đồng bộ lịch sử

Đọc lịch sử là tác vụ riêng, có thể cần thêm ngày trước D để nối bằng chứng. Không chặn UI. Chọn Ngày theo dõi quá khứ thì chạy chế độ lịch sử, không cảnh báo lịch sử. Ngày quá khứ đã đọc đủ có thể cache và làm mới khoảng 15 phút thay vì tải toàn bộ hai ngày mỗi tick 30 giây.

Phân biệt:

- **Chọn Ngày theo dõi trong Cài đặt**: đổi chế độ theo dõi của phiên sang ngày đó; ngày cũ không phát cảnh báo.
- **Vào tab Sự cố/Chi tiết để xem lại một ngày**: là lựa chọn xem; không được vô tình làm biến mất dữ liệu của ngày đang theo dõi hoặc phát âm bản tin cũ. Nếu bản mới chủ động duy trì worker live riêng ở server khi người dùng tra cứu, phải giữ tách bạch phiên xem lịch sử và luồng live; không giả UI lịch sử là hiện tại.

---

## 7. Tuyến tài khoản, phạm vi, ga và ngoại lệ Ga NL

### 7.1. Lấy topology từ nguồn đầy đủ

Ưu tiên danh sách option lý trình trên website. Nếu có nhiều select lý trình thì chúng phải nhất quán. Fallback được mã chuẩn hỗ trợ là bảng trạng thái đầy đủ có header hai cột **Km / Status**. Không suy ra tuyến bằng các dòng sự kiện vì điểm chưa có sự kiện vẫn thuộc tài khoản.

Tuyến phải có ít nhất một đường ngang. Điểm sai định dạng hoặc cùng lý trình số mà tên/kiểu mâu thuẫn phải báo lỗi đọc, không tự bỏ để có “tuyến hợp lệ”. Sắp xếp theo lý trình số, không sắp chuỗi chữ: 99+900 và 100+000 phải có thứ tự đúng.

### 7.2. Nhận diện ga

Điểm có hậu tố trong ngoặc là ga:

- `319+200 (Vi)` → **Ga Vi**.
- `330+00 (YX)` → **Ga YX**.
- Những tên khác như CS, MLy, QHa cũng lấy nhãn từ ngoặc.

Ga không nằm trong danh sách chọn **Từ đường ngang / Đến đường ngang**, không cộng vào số lượng đường ngang, không chạy đối chiếu sáu cảm biến. Không biến bản tin của ga thành một lượt tàu giữ cả ngày trên sơ đồ.

Ga ngắt quan hệ đối chiếu giữa đường ngang hai bên. Tàu có thể dừng lâu hoặc vượt nhau trong ga, nên không lấy “tàu vào ga lúc nào đó” làm nguồn ghép cho tất cả tàu phía ra.

### 7.3. Ngoại lệ Nghi Long theo yêu cầu chủ sở hữu

Chỉ ID **Chosy** (so sánh không phân biệt hoa/thường sau trim) được bổ sung:

```text
Khóa topology giữ nguyên: 303+350 (Nghi Long)
Nhãn hiển thị gọn:        Ga NL
```

- Bổ sung sau khi đã xác thực tuyến nguồn đầy đủ.
- Áp dụng cả tuyến cache khi nâng cấp.
- Chèn theo đúng thứ tự số km.
- Nếu website sau này đã có điểm cùng chainage 303350 thì ưu tiên điểm website, không chèn trùng.
- Không tạo raw event giả ở ga này.
- ID khác không tự nhận ga này nếu website của họ không cung cấp.
- Đổi nhãn NL chỉ là hiển thị, không đổi khóa dữ liệu thành một tên khác.

### 7.4. Phạm vi theo dõi

Phạm vi gồm hai đường ngang hợp lệ trên chính tuyến tài khoản, đầu không đứng sau cuối. Mặc định toàn bộ đường ngang từ đầu tới cuối.

Lọc theo phạm vi ở:

- Danh sách đường ngang và số đếm số đường ngang đang theo dõi.
- Tàu gần nhất/Các lượt tàu thuộc phạm vi.
- Sự cố, thông báo và âm thanh của người dùng.

**Không cắt topology nguồn và không cắt sơ đồ toàn tuyến.** Vẫn cần dữ liệu ngoài phạm vi để ghép lượt/suy luận và nhìn bối cảnh. Không lấy danh sách đã lọc để xác định “hàng xóm” mới cách xa hơn một ga.

### 7.5. Tuyến ví dụ đầu tiên, không hard-code cho mọi người

```text
312+111
313+456
313+800
314+550
319+200 (Vi)
320+445
320+957
321+780
327+327
328+325
330+00 (YX)
332+350
334+830
```

Tuyến này có **11 đường ngang + 2 ga**, không phải 13 đường ngang. Mốc đầu đúng `312+111`; có tin cũ gõ nhầm `312+11`. Giữ biểu diễn `330+00` từ nguồn nếu đó là khóa hợp lệ; đừng tự sửa chuỗi nguồn cho đẹp rồi làm trùng/mất ID.

---

## 8. Lỗi thiết bị, ghép khôi phục và reset ngày

### 8.1. Lỗi gốc và câu chữ

Dòng đỏ website là lỗi, kể cả loại chưa có trong bộ từ khóa UI. Hiển thị đầy đủ nguyên văn, giữ điểm, số thiết bị, thời điểm và ID. Ví dụ website ghi `Chuông 2 hỏng` thì hiển thị đúng vậy; không sửa thành “Mất tín hiệu chuông” hoặc “Mất trạng thái chuông”.

Giao diện Tổng quan gọn có thể cắt hai dòng, nhưng chạm xem đầy đủ/chi tiết phải đọc hết câu. Không cắt nội dung đã lưu hay thông báo dài vì UI card ngắn.

### 8.2. Khóa ghép lỗi

Tối thiểu gồm **đúng đường ngang + đúng thiết bị + đúng loại trạng thái**. Cùng số 6 nhưng `Cảm biến 6 hỏng` và `Điện áp cảm biến 6 < 5V` là hai loại khác nhau.

Ví dụ:

| Lỗi đang mở | Bản tin tới sau | Kết quả |
|---|---|---|
| Chuông 1 hỏng + Chuông 2 hỏng | Chuông 2 khôi phục tốt | Chỉ bỏ lỗi Chuông 2 |
| Đèn 1 hỏng ở A | Đèn 1 khôi phục tốt ở B | Lỗi tại A vẫn mở |
| Cảm biến 6 hỏng + Điện áp cảm biến 6 thấp | Điện áp cảm biến 6 khôi phục tốt | Chỉ bỏ lỗi điện áp cảm biến 6 |
| Chuông 1 hỏng | Khôi phục hệ thống | Chuông 1 vẫn hỏng |
| Nghi tự kích + Chuông 1 hỏng | Ấn nút Phục hồi | Khép suy luận/đúng lượt, giữ Chuông 1 |
| Cần chắn 1/2 mất trạng thái | Cần chắn khôi phục tốt không có số | Áp dụng khôi phục tập thể cần chắn tại đúng điểm theo mã chuẩn |

Không dùng phép “hễ có chữ khôi phục thì xóa tất cả lỗi của điểm”. Không xóa lỗi do bấm XÁC NHẬN hoặc vì không thấy lại dòng đó trong trang đầu.

### 8.3. Hai cặp lỗi nguồn đặc biệt của bản 0.27

| Lỗi phải nhận | Khôi phục tương ứng | Ghi chú |
|---|---|---|
| `Cầu chì 50A bị đứt` | `Cầu chì 50A khôi phục tốt` | Lỗi kể cả nguồn không tô đỏ |
| `nguồn DC > 29v` | `Nguồn 24V khôi phục tốt` | Ghép đúng cặp dù hai tên không giống hệt |

Nhận dạng có chuẩn hóa khoảng trắng/hoa thường/ký hiệu đơn vị theo mã gốc, nhưng text hiển thị vẫn nguyên văn. Bản tin khôi phục đã nhận đúng không được giữ đỏ chỉ vì CSS nguồn cũ tô đỏ. Không thay cặp DC/24V thành quy tắc mọi nguồn đều khôi phục lẫn nhau.

Tên hai cặp trên do chủ sở hữu cung cấp và đã có kiểm thử cục bộ; các phiên gần đây chưa truy cập được website thật để đối chiếu lại nguyên văn vì chặn domain. Không viết trong báo cáo triển khai rằng đã xác minh web thật nếu chưa làm.

### 8.4. Sáu nhóm ở màn chi tiết

Thứ tự cố định:

1. Chuông
2. Đèn
3. Cần chắn
4. Cảm biến
5. Kết nối
6. Điện áp

Ưu tiên danh tính thiết bị:

- Điện áp cảm biến thuộc **Cảm biến**, không thuộc nguồn chung.
- Lỗi đếm trục dẫn xuất thuộc **Cảm biến**.
- Nguồn 220VAC, nguồn DC, cầu chì và điện áp chung thuộc **Điện áp**.
- Mất kết nối giám sát của một đường ngang thuộc **Kết nối**, khác lỗi mạng giữa app/backend với Visitec.
- Lỗi lạ chưa biết nhóm vẫn phải xuất hiện trong Sự cố; không được làm rơi lỗi chỉ vì không có icon tương ứng.

Chỉ hiển thị lỗi đang mở trong hàng tình trạng. Không lấy “Cần chắn 2 đã nâng” làm giá trị lỗi của hàng Cần chắn. Hoạt động bình thường vẫn nằm trong lịch sử sự kiện.

Nếu không có lỗi và đã đọc đủ ngày, ghi **Tốt**. Nếu chưa tải đủ, ghi **Chưa tải đủ dữ liệu**, không vẽ xanh “Tốt” từ mảng rỗng chưa xác thực.

### 8.5. Active faults và incident history khác nhau

Active faults có thể chỉ giữ bản tin mới nhất cho cùng khóa lỗi đang mở. Incident history phải giữ các lần lỗi trong ngày, mỗi lần được ghép bản tin khôi phục phù hợp tới sau. Ví dụ lỗi → khôi phục → lỗi lần mới phải có hai lần phát sinh, không ghi đè thành một sự kiện duy nhất.

Sắp danh sách Sự cố theo **thời điểm phát sinh giảm dần**, không đẩy toàn bộ đỏ lên trên rồi mới tới xanh. Câu “Đã khôi phục” chỉ xuất hiện khi có bản tin khôi phục thật tương ứng.

### 8.6. Reset ngày và đặt lại theo dõi

Sau 00:00 Việt Nam:

- Khối lỗi hiện tại bỏ lỗi ngày trước; ngày mới bắt đầu theo dữ liệu ngày mới.
- Giữ archive và trạng thái tàu/checkpoint qua đêm.
- Dừng âm/rung thuộc lỗi đã ra khỏi khối hiện tại.
- Không chèn một bản tin khôi phục giả lúc 00:00.
- Đang theo hôm nay thì tự đổi ngày; chủ động tra ngày cũ thì giữ ngày họ chọn.

Sau gián đoạn qua ngày hoặc từ 24 giờ, mốc theo dõi lỗi có thể đặt lại theo quy tắc baseline mã gốc. Nhãn phù hợp là **Đã đặt lại theo dõi / Chưa ghi nhận lỗi mới**, không phải “Đã khôi phục”. Lỗi trục thống kê trong ngày có quy tắc giữ riêng, không bị baseline chung vô tình xóa.

---

## 9. Nguyên lý lượt tàu, hướng chạy và đoạn sáng

### 9.1. Các bản tin hướng chuẩn

| Nội dung | Ý nghĩa thuật toán |
|---|---|
| `Tàu lẻ` | Một lượt tiếp cận có hướng tăng km |
| `Tàu chẵn` | Một lượt tiếp cận có hướng giảm km |
| `Tàu lẻ đã qua đường ngang` | Xác nhận qua điểm theo hướng tăng km |
| `Tàu chẵn đã qua đường ngang` | Xác nhận qua điểm theo hướng giảm km |
| `Cần chắn [số] đã hạ` / `đã nâng` | Hoạt động thiết bị, có thể hỗ trợ một lượt đã có bằng chứng |
| `Khôi phục hệ thống` | Giải phóng phần cứng tại điểm; xử lý lượt theo trạng thái cụ thể |
| `Ấn nút Phục hồi`, `Ấn nút Duy tu`, `Ấn nút Cảnh báo` | Thao tác tại điểm; kết thúc đúng lượt liên quan, không tạo tàu mới |

Nhận dạng hướng tiếp cận trong mã gốc là khớp câu Tàu lẻ/Tàu chẵn sau trim/hoa thường; không dùng điều kiện `text.includes('tàu')` để tạo một đoàn tàu từ lỗi đỏ “tàu …”. Lỗi liên quan tàu chỉ đánh dấu lượt bị gián đoạn thích hợp.

### 9.2. Một lượt tương ứng một đoạn đang hiển thị

Trước đã qua: tô đoạn phía tàu đến. Sau đã qua: chuyển ngay sang đoạn phía tàu đi. Không tô cả hai phía quanh một điểm chỉ vì nó đang chiếm dụng.

Với ba đường ngang A–B–C tăng km, chưa có ga ở giữa:

| Sự kiện theo trình tự | Đoạn phải sáng của lượt đó |
|---|---|
| Tàu lẻ tại A | Đoạn phía Bắc → A, có thể là mép tuyến ngắn |
| Tàu lẻ đã qua A | A → B |
| Khôi phục hệ thống tại A | Vẫn A → B nếu chưa có đủ bằng chứng nối tiếp |
| Tàu lẻ tại B | Vẫn đoạn A → B, quyền hiển thị chuyển sang lượt B, không có đoạn thứ hai của cùng tàu |
| Tàu lẻ đã qua B | Tắt A → B ngay, sáng B → C |
| Khôi phục tại B, C chưa báo | Vẫn B → C |

Chiều chẵn đối xứng: C → B → A, mũi tên quay trái. Không viết thuật toán chỉ đúng chiều lẻ.

### 9.3. Dữ liệu Journey cần giữ

Mã chuẩn lưu:

```text
id             ID lượt gắn bản tin nguồn hoặc ID nội bộ chuyển động
from           đường ngang đang đại diện lượt
to             đường ngang tiếp theo cùng khu đoạn; null khi gặp ga/mép tuyến
 direction     +1 lẻ, -1 chẵn
at             thời điểm nguồn bắt đầu lượt
passed         đã có xác nhận qua/tiến trình tương đương
released       nguồn đã giải phóng
reached        lượt đã được điểm sau nhận hoặc đã kết thúc, không còn tự vẽ
successorId    liên kết lượt ở điểm kế tiếp
lastAt         thời điểm tiến trình cuối
selfTriggered  đúng lượt bị đánh dấu tự kích
stalled        đúng lượt có bản tin tàu bị lỗi/gián đoạn
```

Tên biến có thể khác trong bản port, nhưng phải giữ ý nghĩa, liên kết và khả năng phục hồi sau restart. Chỉ lưu `currentKm`, `direction`, `isActive` không đủ cho nhiều lượt, dữ liệu trễ và hủy đúng lượt.

### 9.4. Quy tắc vẽ tương đương mã chuẩn

Với từng Journey còn hợp lệ:

```text
Nếu reached = true: không vẽ đoạn riêng của Journey này.
Nếu chưa passed và chưa released:
    vẽ adjacent(from, -direction) → from.
Nếu passed và (chưa released hoặc vẫn còn to là đường ngang):
    vẽ from → adjacent(from, direction).
```

`adjacent` dùng cả ga trong topology; `to` chỉ là đường ngang liền kề có thể nối qua, không nhảy qua ga. Mép tuyến null được vẽ một đoạn ngắn, không kéo ra một km giả. Hai lượt trùng cùng đoạn/hướng dùng chung nét xanh; không coi số nét là số đoàn tàu.

### 9.5. Đoạn giữa hai đường ngang không tự hết hạn

Không tắt tàu sau 2/5/10/15 phút. Giữ qua nửa đêm, mất mạng, đóng/mở app và dọn raw cũ nếu chưa có bằng chứng kết thúc/nhận lượt thích hợp.

Có giới hạn 120 giây trong **một nhánh ghép báo đảo thứ tự giữa cảm biến kề nhau**; đó không phải timeout cho tàu. Không thấy số `120000` trong code rồi biến thành TTL của mọi đoạn sáng.

### 9.6. Ga và hai mép tài khoản

- Tàu đi từ đường ngang vào ga: đoạn đường ngang–ga sáng sau đã qua, tắt theo khôi phục của đường ngang giáp ga đó.
- Tàu từ ga ra: khi đường ngang phía ra báo tiếp cận thì sáng ga–đường ngang; sau đã qua mới chuyển tiếp.
- Không nối xuyên ga từ đường ngang trước tới đường ngang sau.
- Bản tin riêng của ga không tạo lượt chiếm giữ cả ngày.
- Mép vào/ra ngoài tuyến chỉ là đoạn ngắn trong giai đoạn tương ứng; tàu đã ra và điểm giáp mép khôi phục thì không giữ mép mãi.

### 9.7. Màu và nhãn

Mọi đoạn tàu và mũi tên **luôn xanh lá/mint**, kể cả lúc dữ liệu cũ. Ga vàng/amber. Dữ liệu cũ thể hiện bằng nhãn và timestamp, không đổi tàu thành xám hoặc đỏ. Có nhiều đoạn/hướng phải vẽ đồng thời, tránh chồng chữ/cắt nhãn.

Không có hoạt ảnh giả tàu chạy theo quãng đường/thời gian. Không vẽ vị trí GPS, tốc độ hoặc số hiệu tàu khi website không cung cấp.

---

## 10. Ghép lượt, thiếu bản tin, lượt treo và nhiều tàu

### 10.1. Nguyên tắc quan trọng nhất do chủ sở hữu nhắc lại

**“Khi xóa dấu tàu bị treo thì không xóa nhầm dấu của tàu khác đang chạy trên sơ đồ.”**

Đây là yêu cầu cho thuật toán ghép/đóng lượt, không phải tùy chọn người dùng hoặc lời mô tả chung. Mỗi thao tác phải tìm đúng Journey, đúng điểm, đúng hướng, đúng đoạn và thời gian đủ điều kiện.

### 10.2. Không tạo thêm tàu khi nhận bản tin đã qua

Một bản tin tiếp cận rồi đã qua của cùng lượt chỉ cập nhật Journey hiện tại. Nếu thiếu tiếp cận ở điểm sau, bản tin đã qua phải có khả năng nối và đóng lượt phía trước, không tự tạo một tàu thứ hai rồi bỏ tàu cũ sáng.

Lưu successor/completion record để bản tin muộn không “hồi sinh” lượt đã kết thúc. Không xóa sạch tất cả dấu kết thúc ngay khi không còn đoạn sáng.

### 10.3. Tàu mới nhận đoạn có dấu vết cũ treo

Khi có **bản tin tàu thật có hướng** nhận đúng đoạn:

- Chọn nguồn phù hợp theo mã gốc, tại đoạn liền kề ưu tiên nguồn mới nhất hợp lệ.
- Đóng dấu cũ chưa khớp trên đúng đoạn/hướng được thay thế, không đợi tàu mới đã qua hoặc khôi phục.
- Giữ nguồn/lượt theo sau có thời điểm mới hơn lượt đang nhận.
- Giữ các tàu ở đoạn độc lập và không làm sạch cả tuyến.
- Hoạt động cần chắn/nút bấm không được tự thực hiện quyền “tàu mới nhận đoạn” này.

Không dùng quy tắc “lấy Journey đầu tiên trong mảng”, “xóa mọi tàu ở crossing”, hoặc “mỗi lần có tàu chỉ giữ một tàu toàn tuyến”.

### 10.4. Thiếu bản tin tại điểm tiếp theo nhưng có hoạt động cần chắn

Nếu tại B không có tiếp cận rõ ràng nhưng có hạ/nâng cần, chỉ tạo Journey chuyển động nội bộ khi:

- Có một nguồn tàu đủ điều kiện đang đi tới B từ hàng xóm đúng hướng.
- Không tự dùng ga làm nguồn.
- Không có mâu thuẫn hai hướng không phân giải được.
- Không lấy nguồn tương lai hoặc nguồn cũ không còn phù hợp với lượt địa phương.
- Không tạo lượt từ hoạt động bảo dưỡng đơn lẻ khi không có tàu đi tới.

Journey này có ID nội bộ `travel:motion:...`, không thêm một raw “Tàu lẻ” giả vào lịch sử. Sau đó nếu có bản tin tiếp cận thật cùng lượt thì chuyển/ghép về ID thật, không nhân đôi tàu.

### 10.5. Thiếu hẳn điểm giữa trong cùng khu đoạn

Bản 0.26 cho phép đối chiếu chuỗi cùng hướng trong khu đoạn bị giới hạn bởi ga:

```text
A ─ B ─ C ─ D ─ Ga
A có tàu lẻ, B thiếu dữ liệu, C/D có bằng chứng lẻ theo đúng thứ tự.
```

Có thể dùng chuỗi đủ điều kiện để nối hành trình và gỡ đúng suy luận thiếu nguồn. Không kết luận mọi điểm giữa có lỗi đã phục hồi. Không bắc cầu qua Ga. Không lấy bằng chứng hướng ngược hoặc sau thời điểm mục tiêu một cách tùy ý.

Khi chuyển thuật toán, phải giữ các nhánh chọn nguồn và tiêu thụ nguồn trong `upstreamSection`, `linkPredecessor`, `suspectedSelfTriggers`; một điều kiện “có bất kỳ tàu cùng hướng ở đâu đó” sẽ sai.

### 10.6. Đối chiếu lại dấu cũ từ đã qua/khôi phục mới

Bằng chứng xác nhận phù hợp có thể đồng bộ các dấu treo cũ cùng đoạn/hướng. Tuy nhiên nguồn mới hơn thời điểm lượt đang xét là tàu theo sau và phải được giữ. Không dùng sự kiện lúc 10:05 để xóa cả các lượt mới lúc 10:06.

### 10.7. Hướng ngược và báo đảo thứ tự

Bằng chứng hướng ngược mới tại một điểm có thể kết thúc các đoạn cũ trái hướng **chạm chính điểm/đoạn liên quan**, không xóa tàu ở nơi khác.

Hai vùng cảm biến kề nhau có thể gửi tiếp cận đảo thứ tự. Chuẩn có nhánh ghép nếu timestamp gần nhau tối đa 120 giây, hướng phù hợp, lượt phía sau chưa bị nhận bởi nguồn khác và vẫn đang ở pha phù hợp. Giữ giới hạn này chỉ cho ghép đảo thứ tự, không làm mọi pending train hết hạn.

### 10.8. Kết thúc lượt treo theo bốn câu tại chính điểm

Các câu được nhận sau chuẩn hóa khoảng trắng và hoa thường:

- `Ấn nút Phục hồi`
- `Ấn nút Duy tu`
- `Ấn nút Cảnh báo`
- `Khôi phục hệ thống`

Nút thao tác kết thúc lượt liên quan tại chính điểm, không tạo đoạn đi tiếp giả. Khôi phục hệ thống phải phân biệt:

- Lượt đang chờ/chưa qua, tự kích hoặc có lỗi tàu: kết thúc đúng lượt bị gián đoạn.
- Lượt tàu bình thường đã qua: vẫn giữ liên tục tới đường ngang tiếp theo theo quy tắc giữa hai điểm; không xóa tàu đang chạy chỉ vì nguồn đã khôi phục.

Nếu tại cùng một điểm có lượt tự kích/lỗi và một lượt bình thường khác, ưu tiên chọn **đúng lượt được đánh dấu interrupted/selfTriggered/stalled** để kết thúc, không lấy nhầm lượt mới nhất bình thường.

### 10.9. Bản tin lỗi tàu và lỗi phần cứng

Bản tin đỏ liên quan tàu gắn cờ gián đoạn vào Journey phù hợp ở điểm đó, không đánh dấu mọi Journey. Đóng Journey không xóa raw lỗi đỏ. Khôi phục lỗi thiết bị vẫn phải theo phần 8, không phụ thuộc nét xanh đã tắt hay chưa.

### 10.10. Thu gọn cache nhưng không làm mất pending

Có thể giới hạn các dấu hoàn tất cũ theo điểm/hướng, giữ dấu mới nhất và dấu còn liên kết với lượt sống. Không giới hạn tùy ý toàn bộ Journey bằng “lấy 10 cái cuối” khiến mất tàu chờ lâu.

---

## 11. Suy luận Nghi tự kích và bằng chứng liên kết giữa các điểm

### 11.1. Mục đích và nhãn

Thiết bị nhạy với thời tiết/sấm sét có thể tự báo không có tàu. Website có thể chậm báo lỗi đỏ. App dùng trình tự giữa các đường ngang để nhận diện sớm **Nghi tự kích**.

Đây là suy luận từ dữ liệu web, không biến thành bản tin “chắc chắn tự kích” hoặc lệnh điều khiển. Câu cảnh báo ngắn đúng **Nghi tự kích**, kèm đường ngang/thời điểm. Không thêm đoạn giải thích dài vào thông báo người dùng.

### 11.2. Điều kiện cơ bản

Một tiếp cận mới ở B theo hướng d phải có bằng chứng cùng lượt ở đường ngang phía trước theo hướng đó, hoặc chuỗi cùng hướng đủ điều kiện trong cùng khu đoạn. Nếu không có và đủ coverage/chờ xác nhận thì nghi.

- Lẻ tại B: đối chiếu phía Bắc, lý trình nhỏ hơn.
- Chẵn tại B: đối chiếu phía Nam, lý trình lớn hơn.
- Không đối chiếu qua ga.
- Không có hàng xóm phía cần đối chiếu ở mép tuyến thì không suy luận theo thiếu hàng xóm đó.
- Không yêu cầu nguồn còn đang chiếm dụng/xanh ở thời điểm xét. Nguồn đã Khôi phục hệ thống vẫn có thể là bằng chứng tàu đang đi tới nếu chưa dùng và đúng lượt.

### 11.3. Bằng chứng được chấp nhận

Trong một lượt phù hợp tại nguồn:

- Tiếp cận Tàu lẻ/Tàu chẵn rõ hướng.
- Tàu lẻ/chẵn đã qua đường ngang cùng hướng.
- Cần chắn có số bất kỳ đã hạ/nâng theo nhánh bổ sung thận trọng của mã gốc.

Nhiều cần chắn hạ/nâng trong cùng lượt chỉ là một nguồn bằng chứng. Không coi Cần chắn 1 và Cần chắn 2 của cùng một tàu là hai tàu để hỗ trợ hai mục tiêu khác nhau.

Bằng chứng xa hơn hàng xóm trực tiếp khi điểm giữa bị thiếu phải có hướng rõ và đúng thứ tự trong cùng khu đoạn. Hoạt động cần chắn đơn thuần ở xa không được tự có quyền bắc cầu cả khu đoạn.

### 11.4. Ghép theo lượt, dùng một lần

- Mỗi lượt nguồn chỉ dùng một lần cho lượt mục tiêu tương ứng.
- Không tái dùng nguồn của tàu trước cho tất cả tàu sau.
- Phân biệt `previousTrain` có hướng với `previousActivity` chỉ thao tác.
- Mốc chia lượt thực phải theo báo tàu có hướng, không theo nút Duy tu/hạ cần/khôi phục đơn lẻ.
- Hàng chờ đối chiếu dựa vào lượt trước đã được ghép thật; không chỉ so một timestamp trước/sau cố định.
- Bằng chứng sau khi lượt mục tiêu đã kết thúc không được đem về hỗ trợ nhầm lượt đó.

### 11.5. Chờ tối thiểu một chu kỳ thành công

Hiện hành chu kỳ luôn 30 giây. Ở lần đầu thấy một tiếp cận thiếu nguồn, tạo pending theo ID nguồn và mốc `firstSeen`. Chỉ xác nhận khi vẫn còn nghi ở lần đọc thành công sau và đã qua ít nhất 30 giây.

Không kết luận ngay trong cùng một mảng response vừa nhận. Không dùng một timer client chạy trong nền rồi tự xác nhận dù chưa có lần đọc nguồn mới. Lượt mới phải đủ điều kiện, không lấy toàn bộ dữ liệu cũ khi mở app để phát lại.

Nguồn đến muộn, bản tin cần chắn/đã qua bổ sung đúng lượt hoặc chuỗi phù hợp có thể bác bỏ pending/suy luận. Khi bác bỏ, xóa **riêng dẫn xuất self**, hủy âm/thông báo liên quan và dựng lại phân loại Journey cần thiết; giữ raw và mọi lỗi thật.

### 11.6. Thử thiết bị không được làm mất bằng chứng tàu thật

Ví dụ nguồn A đã có tàu lẻ rõ hướng và chưa dùng. Sau đó B có một hoặc một loạt thao tác hạ cần → Phục hồi → nâng cần. Đến lúc B thật sự báo tàu lẻ, các thao tác vừa rồi không được tạo “mốc đoàn tàu thực trước đó” khiến nguồn A bị coi là quá cũ.

Ngược lại, không nâng chính chuỗi thử tại B thành tàu thật để che việc lượt tiếp theo thiếu nguồn. Sau khi đã dùng A cho một lượt B, lượt B tiếp theo phải có nguồn khác mới đủ bằng chứng.

### 11.7. Không thêm ngoại lệ mất kết nối ngoài yêu cầu

Trong lịch sử trao đổi đã có đề xuất bỏ suy luận khi điểm phía trước mất kết nối; chủ sở hữu yêu cầu **không triển khai thay đổi đó**, chỉ sửa thao tác thử làm mất bằng chứng. Bản viết mới không được âm thầm thêm quy tắc “cứ có lỗi kết nối là bỏ hết Nghi tự kích”.

Vẫn phải phân biệt coverage không đủ trong lần đọc với một raw lỗi “Mất kết nối giám sát” của thiết bị. Không biến điều kiện coverage thành xóa hàng loạt lỗi suy luận đã có.

### 11.8. Ngày lịch sử

Ngày cũ phải dựng lại suy luận từ dữ liệu đầy đủ, gồm bằng chứng ngày trước khi còn nằm trong retention. Những tiếp cận sát cuối ngày chưa đủ 30 giây trước mốc kết thúc thì không tự kết luận sớm.

Không phát chuông/Push cho việc tải lịch sử. Lưu các suy luận lịch sử cần thiết trước khi rebuild checkpoint live để không dựng một lượt từng bị tự kích thành tàu thật đang chạy. Không xóa toàn bộ suy luận các ngày xa chỉ vì lượt xét live hiện chỉ đọc cửa sổ hai ngày.

---

## 12. Đếm trục: dữ liệu, chuẩn, ngưỡng, ngoại lệ và thống kê

### 12.1. Dữ liệu nào được đưa vào tính

Chỉ bản tin **Khôi phục hệ thống**, không đỏ, tại đường ngang hợp lệ, có **đúng sáu số nguyên không âm** CB1–CB6. Giữ string gốc để nhận diện số có đơn vị.

Không tính từ:

- Dòng đo điện áp hoặc giá trị có V/VAC.
- Số thập phân như 4.6V hay 4.6.
- Mảng thiếu/thừa số so với sáu CB.
- Giá trị không đọc được hoặc tràn số nguyên.
- Điểm ga.
- Sáu điểm ngoại lệ ở mục 12.7.

Không lấy số Status, thời gian hay điện áp đi ghép đủ sáu số cho có kết quả.

### 12.2. Chọn chuẩn theo tần suất, không chọn trung bình

Đếm tần suất sáu giá trị. Chỉ có chuẩn nếu có **một số xuất hiện nhiều nhất duy nhất** và tần suất ít nhất hai. Không lấy giá trị lớn nhất, nhỏ nhất, trung bình, trung vị hay mặc định 46.

Ví dụ:

| CB1…CB6 | Chuẩn/kết quả |
|---|---|
| 46,46,48,44,46,46 | Chuẩn 46, CB3 thừa 2, CB4 thiếu 2 |
| 40,40,43,40,40,40 | Chuẩn 40, CB3 thừa 3 |
| 40,40,50,40,40,40 | Chuẩn 40, CB3 thừa 10, cảnh báo đơn lẻ |
| 46,46,48,48,50,50 | Hòa chuẩn, không kết luận CB nào sai |
| 40,41,42,43,44,45 | Không có chuẩn đủ tin cậy |
| 4.6V,4.6V,4.8V,4.4V,4.6V,4.6V | Không phải dữ liệu trục |

Khi hòa/không xác định được chuẩn, ghi đúng:

**cảm biến đếm trục không đồng nhất, lỗi**

Đó là ghi chú, không tự dựng sáu lỗi lớn và sáu tiếng báo.

### 12.3. Ghi chú và thống kê mỗi cảm biến

`delta = observed - standard`:

- delta > 0: thừa delta trục.
- delta < 0: thiếu abs(delta) trục.
- delta = 0: không có sai lệch.

Ghi chú thừa/thiếu có thể cùng xuất hiện ở nhiều CB trong một bản tin. Tính riêng theo **ngày Việt Nam + đường ngang + số CB**. Không cộng lẫn CB3 với CB4 hoặc đường ngang khác.

Tổng thiếu/thừa tích lũy mọi sai lệch đã xác định chuẩn, kể cả lệch 1/2. Số lần để đạt ngưỡng thì chỉ cộng lần có |delta| ≥ 3. Không nhầm hai tổng này.

### 12.4. Hai ngưỡng cảnh báo đã chốt cuối

1. **Lần lệch từ 3 trục trở lên thứ 6 trong ngày** của đúng CB: tạo cảnh báo ngày. Không cần liên tiếp.
2. **Một lần lệch từ 10 trục trở lên**, gồm đúng 10: cảnh báo cho chính lần đo đó.

Biên cụ thể:

- Lệch 1 hoặc 2: ghi chú, không cộng số lần ngưỡng 6.
- Lệch 3: cộng một lần.
- Lần đủ điều kiện thứ 5: chưa cảnh báo theo số lần.
- Lần thứ 6: cảnh báo, không đợi lần 7.
- Lệch 9 đơn lẻ khi chưa đủ sáu lần: chưa cảnh báo theo ngưỡng đơn lẻ.
- Lệch 10 hoặc -10: cảnh báo ngay.
- Một lần vừa là lần thứ 6 vừa lệch ≥10: chỉ một cảnh báo cho CB và bản tin đó.

### 12.5. Chống nhắc lại

Ngưỡng sáu lần chỉ thông báo **một lần/ngày/đường ngang/CB**, kể cả service restart hoặc tải bù làm thay đổi ID bản tin đứng ở vị trí thứ sáu. Cần khóa chống lặp theo ngày/điểm/CB, không chỉ theo source ID.

Lần thứ 7,8… sai nhẹ chỉ cập nhật thống kê. CB khác vừa đủ ngưỡng vẫn có cảnh báo riêng. Một lần sai lớn mới ≥10 vẫn có cảnh báo mới dù CB đó đã báo ngưỡng ngày. Không biến chống nhắc lại thành chặn hết mọi lỗi sau lần đầu.

### 12.6. Text, vòng đời và hiển thị

Mẫu câu từ mã hiện hành:

```text
CB3 đếm sai 6 lần trong ngày · Lần thứ 6 thừa 3 trục · Chuẩn 40, đếm 43
CB3 đếm thừa 10 trục trong 1 lần · Chuẩn 40, đếm 50
```

Nếu đủ số lần, câu lỗi lớn có thể thêm “Sai N lần trong ngày” theo mã gốc. Giữ đúng thiếu/thừa, chuẩn và observed. ID `axle:3:<source-id>`; nó không phải khóa lỗi phần cứng Cảm biến 3 hỏng.

Giữ cảnh báo trục tới hết ngày/00:00. Lần đếm đúng sau đó, nút Phục hồi, khôi phục phần cứng hoặc reset baseline chung không được tự xóa cảnh báo thống kê của ngày đó.

Phải có ở:

- Tổng quan/Sự cố như cảnh báo đỏ đủ điều kiện.
- Nhóm **Cảm biến** trong chi tiết đường ngang, cả xem trực tiếp và lịch sử.
- Mục thống kê duy nhất **Đếm trục lỗi** trong chi tiết, chỉ hiện khi có CB đã cảnh báo.

Trong Đếm trục lỗi, chỉ liệt kê các CB đạt ngưỡng, gồm số lần lệch từ 3, thời điểm/sai lệch gần nhất, tổng thiếu và tổng thừa. Không thêm hai trang “Đếm trục hôm nay” và “Xem các lần đếm trục” đã bị bỏ. Không đưa bảng này lên Tổng quan làm thêm khối mới.

### 12.7. Sáu điểm không áp dụng đối chiếu trục suy ra

```text
Km 280+500
Km 307+507
Km 309+056
Km 320+445
Km 338+900
Km 343+350
```

Tại cả sáu điểm: không hiển thị dữ liệu/ghi chú/thống kê/cảnh báo trục do app suy ra. 320+445 có nhiều hơn sáu cảm biến và cơ chế riêng theo giải thích của chủ sở hữu.

**Vẫn giữ mọi lỗi đỏ website thật, cảm biến hỏng, điện áp, tàu, khôi phục và tự kích.** Không bỏ cả đường ngang khỏi route và không xóa raw để làm ngoại lệ. Nhận ngoại lệ theo lý trình số, không theo một phép so prefix rộng cả khu vực.

Dữ liệu/cache cũ đã có cảnh báo trục tại sáu điểm phải được dọn riêng dẫn xuất, gồm lịch sử. Không cần đợi website có bản tin mới mới lọc được. Không xóa dấu báo một lần của những điểm khác.

### 12.8. Máy mới và lịch sử phải ra cùng kết quả

Dùng cùng raw, route và ngày, tải lạnh từ đầu hoặc đọc cache phải có cùng cảnh báo trục và cùng nhóm Cảm biến. Không chỉ tính trục khi worker live nhận “event mới” rồi để màn hình lịch sử trống trên máy mới.

---

## 13. Âm thanh, giọng Việt, im lặng và XÁC NHẬN

### 13.1. Ba chế độ, nhãn phải đúng

| Nhãn trong Cài đặt | Android chuẩn khi có đợt lỗi mới đủ điều kiện |
|---|---|
| **Giọng nói** | Hai hồi chuông rồi đọc các cảnh báo sáu lượt bằng giọng Việt, có rung |
| **Chỉ chuông** | Sáu hồi chuông khoảng 12 giây, có rung |
| **Im lặng** | Hiển thị/thông báo, không chuông, không đọc, không rung |

Chỉ ghi ba nhãn trên ở lựa chọn. Không nối thêm “(chuông + giọng đọc…)” vào chính tên lựa chọn. Nếu cần giải thích cơ chế web, đặt dòng trợ giúp riêng, không đổi nhãn.

### 13.2. Khi nào một cảnh báo được phát

Chỉ lỗi mới đủ điều kiện của tài khoản/phạm vi đang theo dõi, còn mở và thuộc chế độ live. Lỗi mới phải dựa trên nhập/dẫn xuất thật, không chỉ “không nằm trong state React vừa khởi tạo”.

- Đọc lại cùng trang hoặc cùng ID không phát lại.
- Chỉ mở màn hình/quay lại app không phát lại.
- Tạo lại service/restart worker không phát lại mọi lỗi cache.
- Bắt đầu với lỗi cũ đã tải chỉ hiển thị; không đọc lại cả danh sách đó.
- Tải ngày cũ không phát chuông/Push.
- Một lỗi mới khác sau XÁC NHẬN vẫn phát bình thường.

Nếu một loại lỗi đã thật sự khôi phục rồi phát sinh lần mới có ID mới thì là lần lỗi mới, không bị chặn vĩnh viễn chỉ vì cùng câu chữ/thiết bị.

### 13.3. Đọc giọng Việt

Dùng giọng Việt thật. Android ưu tiên giọng Việt offline đã cài; không tự rơi về tiếng Anh. Nếu chưa có giọng Việt, báo rõ trạng thái để bổ sung; không làm mất lỗi trên UI và không để rung lặp vô hạn.

Thứ tự Android trong một batch: chuông mở đầu hai hồi → đọc danh sách cảnh báo lượt 1 → lượt 2 … lượt 6. Nếu A và B cùng batch thì A,B,A,B… sáu vòng. Lỗi đã khôi phục/bị bác bỏ không còn nằm trong tập được phép đọc.

Nội dung đọc được chuẩn hóa để dễ nghe, nhưng **text hiển thị không đổi**:

- `313+800` đọc “313 cộng 800”.
- `CB3` đọc “cảm biến 3”.
- `>` đọc “lớn hơn”, `<` đọc “nhỏ hơn”.
- `V` đọc “vôn”, `VAC` đọc “vôn xoay chiều”, `A` đọc “am pe”.
- Mẫu đầu: “Đường ngang ki lô mét …”.

Bản tin dài phải chia khúc theo giới hạn engine, không cắt mất phần sau/số thiết bị, không chẻ đôi ký tự Unicode. Callback giọng đọc phải kiểm tra token đợt; callback cũ không được khởi động lại đợt đã hủy hoặc dừng nhầm đợt mới.

### 13.4. Điều khiển âm chung

Nút **Tắt âm thanh / Bật âm thanh** ở khối Sự cố là lựa chọn lâu dài:

- Tắt chuyển sang Im lặng, dừng cả đang phát và hàng đợi.
- Giữ lỗi, theo dõi và cập nhật dữ liệu.
- Bật lại khôi phục chế độ có âm trước đó (Giọng nói hoặc Chỉ chuông), không mặc định làm mất lựa chọn cũ.
- Đổi chế độ cũng hủy đợt cũ để không tiếp tục phát bằng chế độ trước.
- Không tăng ép âm lượng hệ thống hoặc tự vượt Không làm phiền.

Android dùng mức âm lượng Báo thức. Web không được giả có quyền chỉnh âm lượng Báo thức iOS; giải thích đúng cơ chế âm lượng hệ thống của web/media.

### 13.5. XÁC NHẬN là dừng đợt hiện tại, khác tắt âm chung

Nhãn cuối cùng đúng **XÁC NHẬN**. Nhãn cũ “Dừng chuông/giọng đọc” đã bị thay.

Sau bấm:

- Dừng ngay player, TTS, rung và toàn bộ hàng đợi thuộc đợt đang báo.
- Giữ chế độ Giọng nói/Chỉ chuông hiện tại.
- Giữ mọi lỗi đang mở và màu đỏ.
- Giữ worker theo dõi/lịch reconnect.
- Không ghi “Đã khôi phục”, không gọi lệnh Phục hồi tới website.
- Không reset thống kê trục/route/checkpoint.
- Đợt mới phát sinh sau đó vẫn có âm/rung theo chế độ đã chọn.

Nút trên Android có hai chỗ: thông báo điện thoại (dùng được không mở app) và trong thẻ **CÓ SỰ CỐ** đầu Tổng quan. Hai chỗ dùng chung ý nghĩa. Nút Tổng quan ẩn khi không có lỗi, đang dừng theo dõi hoặc đang xem chế độ lịch sử.

Đừng hiểu “xác nhận lỗi” là tạo trạng thái nghiệp vụ “đã xử lý xong”, xóa lỗi hoặc ngừng theo dõi. Trong Android không có yêu cầu lưu tên người đã nhận lỗi/biên bản xử lý; không tự thêm quy trình đó.

### 13.6. Khi lỗi được khôi phục, reset ngày hoặc dừng theo dõi

Giữ tập ID lỗi hiện còn mở cho batch. Loại khỏi queue/đọc đang diễn ra khi lỗi đó khôi phục hoặc suy luận bị bác bỏ. Hết lỗi thuộc batch thì dừng âm/rung. Dừng theo dõi thì hủy toàn bộ. Không để player/TTS callback từ phiên tài khoản cũ phát sang phiên mới.

### 13.7. Giới hạn iPhone phải được ghi thành phần riêng

Web cần thao tác người dùng để mở khóa âm thanh và cần quyền thông báo. Có thể có nút xin phép âm trong web, nhưng đó là nhu cầu nền tảng, không phải yêu cầu khôi phục nút Thử chuông của app Android đã bị bỏ.

Hành vi XÁC NHẬN trong thông báo iOS, giọng đọc khi khóa máy và rung không được cam kết bằng cách sao chép nhãn Android. Xem phần 18 và bắt buộc thử trên iPhone thật.

---

## 14. Lịch sử 30 ngày, checkpoint, sửa dữ liệu cũ và đổi tài khoản

### 14.1. Retention hiện hành

Giữ 30 ngày Việt Nam gồm hôm nay, từ 00:00 của hôm nay trừ 29 ngày. Các picker, API tải ngày, truy vấn DB và import floor dùng cùng quy tắc. Không còn 15 ngày.

Raw cũ hơn sàn được dọn; đọc website về trang cũ cũng không được nhập lại chúng làm DB phình lên. Tài khoản mới có namespace/sàn riêng; không mang sàn cũ của người A sang người B.

Chạy bảo trì/dọn ở nền khi khởi động, khi sang ngày và trong vòng theo dõi, **kể cả khi lượt mạng thất bại**; không chỉ dọn sau fetch thành công. Android bị tắt hoàn toàn thì lần chạy tiếp mới thực hiện được. Backend luôn chạy phải có công việc bảo trì riêng phù hợp, không chặn thao tác giao diện.

### 14.2. Checkpoint trước, xóa raw sau

Thứ tự bắt buộc:

1. Dựng/cập nhật travel state với raw còn có.
2. Giữ pending Journey, dấu liên kết, trạng thái chiếm dụng cần thiết.
3. Lưu checkpoint bền vững và xác nhận commit thành công.
4. Sau đó mới xóa raw quá sàn.
5. Lưu import floor và thu hồi dung lượng theo loại DB.

Nếu commit checkpoint thất bại thì không xóa raw. Nếu process chết giữa hai bước, restart vẫn phải giữ được tàu chờ có nguồn đã quá 30 ngày.

Không áp dụng “xóa DB/cache rồi tải lại” vì nguồn raw đã quá retention không còn để dựng lại mọi tàu đang chờ.

### 14.3. Cửa sổ phát lại và dữ liệu bổ sung muộn

Mã Android giữ checkpoint trước cửa sổ hai ngày gần nhất; replay phần còn lại để nhận bản tin trễ. Nếu nhập thêm lịch sử cũ trước checkpoint hoặc đổi phân loại suy luận thì rebuild phần retained raw, đồng thời mang theo các Journey có nguồn đã hết retention.

Không giả toàn bộ lịch sử từ đầu còn có trong DB. Không đưa kết quả từ request cũ của tài khoản/route/ngày khác vào cache đang dùng.

### 14.4. Tải ngày D đầy đủ

- Xác thực snapshot cả ngày D.
- Khi cần bằng chứng liên ngày, đọc D-1 nếu nó chưa ra ngoài 30 ngày.
- Tính lại đếm trục, incident pairs, thiết bị và suy luận lịch sử bằng cùng quy tắc.
- Lưu lại dẫn xuất cần thiết và đánh dấu dayComplete sau khi tính/lưu xong.
- Rebuild checkpoint live nếu dữ liệu mới ảnh hưởng quá khứ đang dùng.
- Không phát cảnh báo lịch sử.
- Trên máy mới và máy có cache phải ra cùng kết quả.

### 14.5. Tách cache theo tài khoản/ngày

Cache tối thiểu gồm namespace + ngày + revision dữ liệu/topology. Thay ngày phải hiện loading/cache đúng ngày, không chớp dữ liệu hôm nay vào ngày hôm qua. Từ Sự cố ngày D mở Chi tiết rồi Chi tiết lịch sử vẫn là ngày D; không tự nhảy về hôm nay.

Đang tải D nhưng người dùng chuyển D2/tài khoản khác, response D không được ghi đè màn hình mới. Có thể hủy hoặc giữ vào cache đúng namespace; UI phải kiểm tra identity/revision trước khi áp dụng.

### 14.6. Đổi URL/ID/tài khoản và đổi mật khẩu

Android coi URL + ID là account namespace. Đổi URL hoặc ID phải xóa/chuyển khỏi trạng thái người cũ: route, lỗi hiện tại, lịch sử đang xem, coverage, pending, checkpoint, dedup và mất kết nối. Backend nhiều người dùng có thể giữ dữ liệu từng account riêng thay vì xóa vật lý, nhưng tuyệt đối không trộn hoặc lộ dữ liệu.

Để trống mật khẩu khi URL/ID không đổi có thể giữ mật khẩu đã lưu. Nếu URL/ID đổi thì phải có mật khẩu cho account mới. Không tái dùng mật khẩu A cho B chỉ vì ô password trống.

Đổi mật khẩu không phải lý do mất history của cùng account; nếu hệ thống dùng key nội bộ khác khi credential đổi, cần cơ chế chuyển namespace hợp lệ sau xác thực hoặc giữ lịch sử chính xác. Đừng mặc định lấy hash toàn bộ password làm identity nghiệp vụ rồi làm người dùng “mất tuyến” mỗi lần đổi mật khẩu.

### 14.7. Nâng phiên bản quy tắc

Marker của Android chuẩn:

| Cache/quy tắc | Phiên bản |
|---|---|
| travelVersion | 6 |
| selfEvidenceVersion | 5 |
| axleVersion | 3 |
| faultRulesVersion | 1 |
| retentionDays | 30 |

Bản port có thể có migration key riêng. Khi thay quy tắc, tính lại đúng dẫn xuất từ raw, bảo toàn lỗi gốc và expired-source checkpoint, hủy thông báo suy luận sai; không phát lại toàn bộ history. Không chỉ đổi một số version rồi xóa tất cả cache.

---

## 15. Giao diện chung và bố cục Tổng quan

### 15.1. Bộ màu từ app Android chuẩn

| Vai trò | Mã màu CSS tương đương |
|---|---|
| Nền chính | `#060E1F` |
| Thẻ tối | `#101C36` |
| Viền | `#25334D` |
| Chữ chính | `#F1F6FF` |
| Chữ phụ | `#94A3B8` |
| Cyan điều khiển | `#00D9EF` |
| Tàu xanh mint | `#35D6A0` |
| Chữ lỗi đỏ nhạt | `#FFA3AD` |
| Viền/nhấn lỗi | `#EF4444` |
| Nền sự cố | `#26131F` |
| Nền nút/tint lỗi | `#55212F` |
| Nền điều khiển cyan tối | `#122D42` |
| Nền trạng thái tốt | `#0C292C` |
| Ga/cảnh báo dữ liệu cũ | `#FBBF24` |

Dùng bo góc, viền và sáng nhẹ theo ảnh app. Không nền trắng, không chuyển phong cách sang dashboard bảng tính hoặc bản đồ vệ tinh. Dialog, ngày, select, input cũng phải theo cùng nền tối.

### 15.2. Logo, header, footer và thanh dưới

- Logo đầu tàu cyan/navy thống nhất với icon PWA, không dùng emoji thay logo chính nếu đã có asset.
- Header Tổng quan: **Giám sát Đường ngang**, dòng đơn vị, trạng thái LIVE/CŨ…, nút bánh răng mở Cài đặt.
- Các trang con có tiêu đề đúng chức năng, back về ngữ cảnh trước.
- Thanh dưới bốn tab, icon và chữ; tab active cyan. Chi tiết thuộc tab Đường ngang, Các lượt tàu thuộc Tổng quan.
- Tab Sự cố có thể có chấm báo khi trong scope còn lỗi.
- **Designed by Cao Xuân Cường** chỉ ở Cài đặt, không chèn vào Tổng quan hoặc từng thẻ.
- Tôn trọng safe area/notch/home indicator iPhone và thanh công cụ Safari. Không để nav che hàng cuối hoặc nút chính.

### 15.3. Thứ tự Tổng quan bắt buộc

```text
HEADER: logo | Giám sát Đường ngang / đơn vị | LIVE/CŨ | bánh răng

[ VÒNG SỐ | CÓ SỰ CỐ / ĐANG THEO DÕI ...        | XÁC NHẬN ]
[ Ô KẾT NỐI NHỎ: trạng thái + loading thật      | retry khi lỗi ]
[ SƠ ĐỒ TOÀN TUYẾN: BẮC ─ đường ngang / ga ─ NAM; giờ dữ liệu ]
[ TÀU GẦN NHẤT: giờ – đường ngang – hướng, xem tất cả ]
[ SỰ CỐ: số đếm, Tắt/Bật âm thanh, lỗi, xem tất cả ]

NAV: Tổng quan | Đường ngang | Sự cố | Cài đặt
```

Chủ sở hữu đã chỉnh nhiều lần để chốt **ô kết nối ở giữa ô Có sự cố và sơ đồ**. Không chuyển lên header, không thêm một ô ở cuối trang, không đặt dưới Sự cố. Không thêm đồng thời nút retry ở nhiều nơi để thay cho đúng vị trí.

### 15.4. Thẻ tóm tắt gọn

- Có lỗi: vòng số là **số lỗi đang mở trong scope**, không phải số đường ngang bị lỗi.
- Không lỗi: vòng số là số đường ngang trong scope, không cộng ga.
- Chưa có dữ liệu: CHỜ KẾT NỐI / Kết nối tài khoản để theo dõi.
- Có dữ liệu, đang theo dõi, mới và có lỗi: CÓ SỰ CỐ / N sự cố chưa khôi phục.
- Không lỗi: ĐANG THEO DÕI / N đường ngang · Chưa ghi nhận lỗi mới.
- Dừng: ĐÃ DỪNG THEO DÕI.
- Dữ liệu cũ: CẦN KIỂM TRA CẬP NHẬT; không che số lỗi bằng thông báo “0”.
- Lịch sử: ĐANG XEM LỊCH SỬ, ghi ngày đang chọn.
- Dòng nhỏ ghi thời điểm thành công và `30 giây`, hoặc `Thử lại mỗi 2 phút` khi mất kết nối.
- Nút XÁC NHẬN gọn, chạm được, không bị cắt chữ. Không ẩn chỉ vì TTS vừa đọc xong nếu vẫn thỏa điều kiện hiển thị của app.

Chủ sở hữu yêu cầu thu gọn vòng số/đệm/phần sơ đồ hơi dư để dành chỗ ô kết nối; không tăng chiều dài Tổng quan.

### 15.5. Ô kết nối nhỏ, luôn có khi phù hợp màn Tổng quan

| Tình trạng thật | Nội dung/điều khiển |
|---|---|
| Chưa theo dõi | Chờ cấu hình/Đã dừng theo dõi |
| Đang đọc bình thường | Spinner indeterminate + Đang tải dữ liệu… |
| Vừa cập nhật thành công | Đã kết nối · HH:mm:ss, không nút retry |
| Mất kết nối, đang chờ | Tự thử lại mỗi 2 phút; nút đỏ Kết nối lại; dưới nút Mất kết nối |
| Mất kết nối, đang thử | Đang kết nối lại…, spinner; retry vô hiệu tạm |
| Đã đọc lịch sử | Đã tải lịch sử, không giả LIVE |

Spinner thể hiện có tác vụ thật, không bịa 10%→100% bằng setInterval. Cập nhật trạng thái tải phải xảy ra ngay khi bắt đầu/kết thúc tác vụ, kể cả chưa có event mới hoặc lastSuccess chưa đổi.

Thành công ẩn nút đỏ và cảnh báo nhưng **không bỏ hẳn ô trạng thái kết nối**.

### 15.6. Sơ đồ

- Một đường thẳng ngang với mốc lý trình, BẮC trái/NAM phải.
- Đường ngang chấm/mốc nhỏ; ga ô vàng và nhãn Ga Vi/Ga YX/Ga NL…
- Đoạn tàu xanh có mũi tên và chữ hướng cùng một nhãn, không tách rời khiến hiểu sai.
- Tuyến dài cuộn ngang trong khung; giữ vị trí cuộn sau mỗi poll.
- Không có tàu thì khung thấp hơn, tránh vùng trống dư.
- Có nhiều hướng/đoạn thì đủ hàng nhãn; không thu khung đến mức cắt tên hướng.
- Timestamp/nhãn Dữ liệu cũ thể hiện freshness. Nét tàu vẫn xanh.
- Chạm mốc đường ngang mở Chi tiết đúng điểm/ngày. Chạm ga chỉ hiển thị nhãn, không mở sáu nhóm thiết bị như đường ngang.

### 15.7. Hai thẻ phía dưới phải dùng chiều cao có sẵn

Tổng quan native không là một trang cuộn dọc rất dài. Tự phân bổ số hàng tàu và lỗi theo chiều cao, cỡ chữ và dữ liệu thực. Không ép ba hàng cố định; không dựng 30 card rồi giấu nav dưới đáy.

Bản web có thể cuộn **bên trong** thẻ/danh sách hoặc giới hạn hàng kèm xem tất cả, nhưng khung chính phải vừa màn điện thoại hợp lý. Nội dung chi tiết và lịch sử có thể cuộn dọc bình thường.

Thẻ Tàu gần nhất:

- Bản tin tiếp cận Tàu lẻ/Tàu chẵn của đúng ngày/scope, mới nhất trước.
- Giờ – Km – pill hướng xanh, chấm mới nhất nổi hơn.
- Có mở rộng/thu gọn hoặc xem tất cả, không làm mất phần Sự cố.
- Không có dữ liệu: Chưa ghi nhận tàu / Thông tin xuất hiện khi có dữ liệu mới.

Thẻ Sự cố:

- Nền đỏ trầm; badge số lỗi, nút Tắt/Bật âm chung.
- Giờ – Km – Chưa khôi phục; câu lỗi nguyên văn, có đường xem hết.
- Không có lỗi sau dữ liệu thành công: **Không có sự cố**.
- Chưa có dữ liệu: Đang chờ dữ liệu; không kết luận an toàn.
- Không thêm thống kê trục hoặc một khối Lịch sử mới bên dưới.

### 15.8. Độ mượt và hiệu ứng

Giữ header/nav/root, cập nhật nội dung trong khung hiện có. Không reload trang, không chớp trắng mỗi 30 giây, không đẩy scroll về đầu sau mỗi poll. Form Cài đặt đang nhập không bị refresh tự ghi đè.

Hiệu ứng logo/vòng lỗi nếu giữ thì nhẹ, chu kỳ khoảng 2,8 giây theo thiết kế trước; ngừng khi không visible và tôn trọng Reduce Motion. Đoạn tàu chỉ có glow tĩnh, không chạy giả. Không dùng hiệu ứng để che việc dữ liệu không cập nhật.

---

## 16. Danh sách đường ngang, chi tiết thiết bị và lịch sử từng lượt

### 16.1. Tab Đường ngang

- Tiêu đề khu vực theo dõi + N đường ngang, không gồm ga.
- Bộ lọc có số lượng thực: **Tất cả / Chưa ghi nhận lỗi / Có lỗi**.
- Số lượng theo scope; số điểm có lỗi khác số lượng fault.
- Danh sách theo thứ tự tuyến, không đảo theo lỗi ngẫu nhiên.
- Mỗi dòng có Km, mô tả lỗi mới nhất nếu có, hoặc Chưa ghi nhận lỗi/Chưa có dữ liệu theo dữ liệu thật; thời gian bản tin gần nhất và dấu mở chi tiết.
- Điểm lỗi có viền/stripe đỏ, điểm không lỗi có accent cyan/mint phù hợp.
- Chạm mở đúng đường ngang, giữ ngày nguồn và scope.

Có thể thêm tìm lý trình nếu không làm mất bộ lọc gốc, nhưng không lấy ô tìm kiếm để thay ba bộ lọc đang có.

### 16.2. Màn Chi tiết đường ngang

Đầu màn:

- Km rõ ràng, badge Đang có sự cố / Chưa tải đủ dữ liệu / Chưa ghi nhận lỗi / Đã đặt lại theo dõi đúng ngữ nghĩa.
- Thời điểm bản tin gần nhất, không giả giờ hiện tại thành giờ cập nhật nguồn.
- Sáu nhóm theo thứ tự phần 8.4, chỉ lỗi còn mở và thời điểm lỗi; đủ dữ liệu không lỗi thì Tốt.

Phía dưới:

- **Đếm trục lỗi** khi có CB đạt ngưỡng, gồm thống kê phần 12.
- **Lịch sử sự kiện gần nhất**: native hiện tối đa 12 dòng tại phần tóm tắt này, có ghi chú trục hợp lệ dưới dòng khôi phục.
- Nút **Chi tiết** mở màn toàn bộ lịch sử sự kiện theo ngày, không phải nút bản đồ đã bị bỏ.

Không bỏ mục Đếm trục lỗi chỉ vì đã hiển thị cảnh báo trong tab Sự cố. Hai chỗ có mục đích khác nhau và đều được chủ sở hữu yêu cầu.

### 16.3. Lịch sử sự kiện đầy đủ của một đường ngang

- Có Km đang xem, ngày được chọn và nút Tải dữ liệu.
- Ghi Lưu lịch sử 30 ngày, tính cả hôm nay.
- Từng lượt tàu là một khung viền nhẹ có Km, hướng, khoảng giờ và trạng thái kết thúc.
- Bên trong là mọi bản tin của nhóm: giờ + nguyên văn + màu lỗi nếu có + ghi chú trục hợp lệ.
- Có phân đợt hiển thị/thêm dòng nhưng phải xem được hết, không cắt cả ngày ở 12/20/100 bản tin.

Chuẩn native ban đầu render 20 nhóm, sau đó **Hiển thị thêm** mỗi 20 nhóm; bản web có thể dùng phân trang/virtual list tương đương, phải thể hiện có thêm dữ liệu và giữ đúng thứ tự.

### 16.4. Quy tắc tách khung sự kiện

1. Mỗi bản tin tiếp cận rõ ràng Tàu lẻ/Tàu chẵn luôn bắt đầu một khung mới, kể cả lượt trước chưa có kết thúc.
2. Bản tin đã qua thiếu tiếp cận có thể mở một lượt thiếu đầu; không tự gộp vào lượt đã xong/hướng ngược.
3. Khôi phục/thao tác kết thúc ghi nhận trạng thái khung theo mã gốc.
4. Các bản tin không gắn được lượt rõ ràng chỉ gom cụm nếu khoảng cách giữa hai bản tin liên tiếp không quá 5 phút và tổng cụm không quá 15 phút.
5. Khung các bản tin rời ghi **Sự kiện ngoài lượt tàu**, không tự đặt “Thử thiết bị” nếu không có căn cứ.
6. Tách/gộp khung chỉ phục vụ đọc lịch sử, không xóa/khôi phục lỗi thiết bị hoặc thay đổi sơ đồ.
7. Nhóm mới nhất hiển thị trước, các dòng trong nhóm giữ thứ tự diễn biến.

### 16.5. Giữ ngày xuyên điều hướng

Sự cố ngày 01/11 → chạm lỗi Km 313+800 → Chi tiết → Chi tiết toàn bộ: cả chuỗi vẫn xem **01/11**, không nhảy 12/11. Quay lại giữ bộ lọc/ngày/vị trí cuộn hợp lý. Chọn ngày khác trong lịch sử không đổi sang account khác hoặc xóa state live.

---

## 17. Tab Sự cố, Các lượt tàu và Cài đặt

### 17.1. Tab Sự cố

Có ngày, Tải dữ liệu và ba bộ lọc:

- Tất cả.
- Chưa khôi phục.
- Đã khôi phục.

Mỗi incident có giờ phát sinh, Km, **toàn bộ** text, nhãn trạng thái, giờ khôi phục nếu có. Màu tốt/tick xanh chỉ cho cặp đã ghép thật. Tất cả xếp theo giờ phát sinh mới nhất, không phân khu đỏ trước xanh.

Các nhãn khác cần giữ:

- **Đã đặt lại theo dõi**: lỗi trước baseline nhưng không có khôi phục thật; không nằm trong bộ lọc Chưa khôi phục hiện tại và không giả thành Đã khôi phục.
- **Cảnh báo đếm trục trong ngày**: dẫn xuất thống kê có vòng đời tới hết ngày, không đòi bản tin khôi phục giả.
- **Chưa tải đủ dữ liệu**: chưa hoàn tất snapshot; khác Không có sự cố.

Native render từng đợt 40 incidents rồi Hiển thị thêm; bản web được tối ưu tương đương nhưng phải xem được hết. Chạm incident mở Chi tiết đúng điểm/ngày.

### 17.2. Các lượt tàu

Mở từ Tàu gần nhất. Liệt kê các tiếp cận trong phạm vi theo dõi của **ngày đang xem**, không đưa bản tin hôm qua lên làm tàu hôm nay. Không mặc định cắt còn 100 rồi không có cách xem tiếp.

Giờ – đường ngang – Tàu lẻ/chẵn kèm mũi tên. Đây là danh sách báo tàu, không tính số đoàn tàu thực bằng số bản tin ở nhiều điểm. Không có lượt thì ghi Chưa có lượt tàu.

### 17.3. Cài đặt: thứ tự và trường bắt buộc

Bố cục chuẩn hiện tại:

1. **Ngày theo dõi**: ngày hiện tại tự động hoặc ngày cũ có nhãn Lịch sử.
2. **Phạm vi theo dõi**: Từ đường ngang / Đến đường ngang, nút Áp dụng phạm vi.
3. **Máy chủ & Tài khoản**: Website, ID, mật khẩu, Hiện/Ẩn mật khẩu.
4. **Theo dõi & Báo động**: Cố định 30 giây; ba chế độ đúng nhãn; trạng thái giọng đọc; điều khiển nền tương đương nền tảng.
5. **Lưu & bắt đầu theo dõi**.
6. **Dừng theo dõi**.
7. Chữ ký thiết kế chỉ ở màn này.

Không đổi thứ tự Ngày theo dõi xuống sau Phạm vi hoặc bỏ Áp dụng phạm vi nếu muốn giao diện/chức năng như app. Bản web trước chỉ là tham khảo, không phải căn cứ để bỏ nút/bộ lọc của native.

### 17.4. Ngày theo dõi

Mặc định **Hôm nay (tự động)**, tự chuyển theo ngày Việt Nam. Chọn ngày cũ chỉ trong 30 ngày, có dòng Đang xem ngày cũ · Không phát chuông lịch sử và nút **Về hôm nay · Tự đổi ngày**.

Chọn ngày cũ không phải lệnh tải bù rồi phát lại toàn bộ chuông của ngày đó. Quay hôm nay không phát lại lỗi cache.

### 17.5. Website và tài khoản

Ô Website có thể gợi ý `http://`, sửa/xóa/thay `https://` bình thường, giữ URL đã lưu. Nhận scheme http/https và host hợp lệ, không URL có thông tin đăng nhập nhúng trong userinfo.

Mật khẩu che mặc định. Lưu credentials an toàn, không trả lại mật khẩu đã lưu qua API. Nếu không đổi URL/ID thì để trống có thể giữ mật khẩu cũ. Không điền sẵn tài khoản vận hành hoặc mật khẩu từ hội thoại.

Lưu & bắt đầu: kiểm tra trường, kiểm tra phạm vi nếu đã có tuyến, hủy request/âm của phiên cũ, lưu account/options, bắt đầu đúng chế độ và về Tổng quan. Mọi kết quả của phiên cũ phải bị loại.

Dừng theo dõi: hủy worker tương ứng/đăng ký theo dõi phiên, lịch hẹn và âm/rung; giữ dữ liệu để tra cứu. Không âm thầm chạy mãi cho chính người đã dừng. Nếu backend chia sẻ polling giữa nhiều phiên cùng account thì người khác vẫn có thể theo dõi, nhưng phiên đã dừng không nhận cảnh báo.

### 17.6. Các điều khiển riêng nền tảng

Android có Cài đặt giọng đọc và Cài đặt chạy nền dẫn tới cài đặt hệ thống. Web không có quyền mở cùng trang Android trên iPhone; thay bằng hướng dẫn/thao tác hợp lệ cho iOS:

- Thêm vào Màn hình chính.
- Xin quyền thông báo từ thao tác bấm.
- Mở khóa âm thanh bằng thao tác người dùng nếu cần.
- Trạng thái hỗ trợ thực và hướng dẫn khi bị từ chối quyền.

Đây là phần thích nghi nền tảng, không được dùng làm lý do đổi bố cục Tổng quan hoặc bỏ nghiệp vụ.

---

## 18. Kiến trúc web/iPhone và phân biệt với Android

### 18.1. Yêu cầu người dùng về iPhone

Chủ sở hữu muốn web có giao diện và tính năng như app, cài icon lên iPhone và nhận cảnh báo khi không mở màn hình ứng dụng. Họ đã dùng một web khác và thấy nó có hoạt động nền, nhưng không hài lòng giao diện/độ ổn định. Không có mã web đó trong dữ liệu bàn giao này để xác minh cơ chế.

Một hướng dẫn bên ngoài từng hứa “thêm icon là đọc website 15 giây/lần khi khóa máy, tự sáng màn hình, rung, chuông và đọc giọng”. **Không lấy các lời hứa đó làm đặc tính được đảm bảo của Safari.** Chu kỳ nghiệp vụ hiện tại là 30 giây, không tự đổi về 15 giây theo tài liệu bên ngoài.

### 18.2. Những phần làm được bằng kiến trúc chuẩn

| Khả năng | Cách làm đúng/điều kiện |
|---|---|
| Giao diện giống app | Responsive web/PWA, giữ đúng bố cục và state |
| Đọc website khi không mở web | Worker backend luôn chạy, DB bền vững |
| Theo dõi 30 giây/retry 120 giây | Scheduler backend, không timer iPhone |
| Lịch sử/sơ đồ/quy tắc đầy đủ | Backend/Java core hoặc bản port có kiểm chứng |
| Cài icon iPhone | Manifest + HTTPS + Add to Home Screen |
| Web Push iPhone | iOS/iPadOS 16.4+ phù hợp, PWA được thêm Màn hình chính, user cấp quyền |
| Chuông/giọng khi trang đang hoạt động | User gesture mở âm, media/TTS hỗ trợ giọng Việt |
| Hình thức thông báo khi khóa máy | Web Push do iOS hiển thị/quản lý theo quyền và chế độ máy |

### 18.3. Những phần không được giả định tương đương Android

- JavaScript timer không chạy liên tục vô thời hạn khi Safari/PWA bị treo nền/khóa màn hình.
- Service Worker không phải foreground service Android, không thể chạy vòng lặp cào web 30 giây mãi.
- Push event không cho tự chạy SpeechSynthesis đọc mọi câu cảnh báo khi màn hình khóa.
- Web Vibration API không cung cấp rung tùy ý tương đương Android trên iPhone.
- Web không ép âm lượng tối đa/vượt Focus hoặc Silent của iOS.
- Web không đảm bảo tự đánh thức/sáng màn hình khóa theo lệnh JS.
- Nút action trong notification iOS không được coi hỗ trợ giống Android chỉ vì đã khai báo `actions`.

### 18.4. Luồng âm thanh nền nếu bên triển khai chọn thử

Có thể thiết kế phiên nghe media trực tiếp người dùng chủ động bấm phát: backend đưa chuông/giọng thành luồng MP3, iPhone dùng native media playback. Đây là một phương án web **cần thử thực tế**, không phải yêu cầu thuật toán đường ngang và không chứng minh timer JS chạy nền.

Phải nói rõ:

- Dùng mạng liên tục, có thể có khoảng im giữa cảnh báo.
- iOS, cuộc gọi, chuyển mạng hoặc giới hạn request của hạ tầng có thể ngắt.
- Cần xử lý XÁC NHẬN bằng dừng/flush buffer hiện tại; không chỉ dừng một oscillator trong JS trong khi audio element còn buffer cảnh báo.
- Không phát chồng với TTS foreground cho cùng batch.
- Mở lại không đọc lại cảnh báo cũ đang nằm queue trên server.
- Có cách nhận biết phiên đã ngắt và khởi động lại hợp lệ theo user gesture.
- Web Push vẫn là kênh riêng, không phụ thuộc một phiên MP3 sống mãi.

Gói web tham khảo trước dùng mốc 55 phút cho một request để nằm dưới timeout 3600 giây Cloud Run. **55 phút là lựa chọn triển khai của gói đó, không phải quy tắc app Android, không phải giới hạn chung của mọi web và không phải bảo đảm chạy ổn định 55 phút trên iPhone.** Người viết mới phải công bố giới hạn của kiến trúc mình chọn.

Nếu mục tiêu bắt buộc là toàn bộ chuông/giọng/rung và action khóa màn hình giống native Android liên tục trong mọi điều kiện, không được báo web đã đạt khi nền tảng chưa cho phép/kiểm chứng. Cần trình bày ranh giới đó cụ thể; không âm thầm làm một giao diện có nút nhưng không hoạt động.

### 18.5. Cấu hình quyền và onboarding iPhone

1. Mở URL HTTPS bằng Safari.
2. Chia sẻ → Thêm vào Màn hình chính → Thêm.
3. Mở từ icon.
4. Nhập/lưu thông tin website đúng account.
5. Bấm Bật thông báo, để iOS hiện prompt thật; chấp nhận nếu muốn.
6. Nếu chọn chế độ âm foreground/phiên media, bấm kích hoạt âm thật theo khả năng hỗ trợ.
7. Có thông báo thử thực qua cùng hệ thống Push; không chỉ gọi alert() hoặc Notification ở trang visible rồi nhận là nền đã đạt.
8. Thử khóa màn hình, mất mạng, đổi Wi-Fi/4G và mở lại.

Không tự redirect `/reset` để xóa cookie/cache/permission khi cập nhật. Nếu user từ chối quyền thì báo cách vào cài đặt hệ thống, không lặp prompt vô hạn.

---

## 19. Hợp đồng dữ liệu/API đề xuất cho bản viết mới

**Mọi URL `/api/...` trong phần này là API NỘI BỘ của sản phẩm web mới, không phải API đã được xác minh tồn tại trên visitec.vn.** Tên route có thể đổi, ý nghĩa và dữ liệu cần có không được bỏ.

### 19.1. Đề xuất dữ liệu chuẩn hóa

```json
{
  "id": "source-event-id",
  "at": 0,
  "crossing": "101+000",
  "text": "Chuông 2 hỏng",
  "red": true,
  "counts": [],
  "kind": "source"
}
```

`at: 0` ở đây chỉ minh họa kiểu trường, không phải dữ liệu demo để hiển thị. Dữ liệu vận hành phải có timestamp thật từ website. `kind` là metadata đề xuất; native phân loại theo prefix và nội dung. Không tự đổi IDs gốc khi thêm trường này.

Các cấu trúc nên có:

- RoutePoint: key canonical, chainage số, station boolean, displayLabel.
- Incident: fault event + recovery event nullable + trạng thái baseline nếu có.
- DeviceState: sáu nhóm, tập lỗi mở theo nhóm, trạng thái completeness.
- Journey/TrackSegment: đầy đủ phần 9.
- AxleError: source, sensor, standard, observed, delta, dailyCount, missing, extra.
- Checkpoint: version, route hash/topology, checkpointAt, Journey còn cần giữ, occupancy, data revision.
- MonitorState: running, mode, audibleMode, monitorDate, scope, loading, connectionLost, lastAttempt, lastSuccess, lastFull, nextRetryAt, coverage, fault baseline.
- AlertNotice: seq/batchId, account, session/device, fault IDs, mode lúc gửi/hiện tại theo chính sách, created, delivered/acknowledged, expiry.

Không suy ngược text từ một enum rồi bỏ nguyên văn website. Không dùng một boolean `healthy=true` thay completeness/faults/freshness.

### 19.2. API phía trình duyệt cần có

| Nhóm | Chức năng | Dữ liệu quan trọng |
|---|---|---|
| Session | Tạo/khôi phục phiên an toàn | CSRF, trạng thái cấu hình, khả năng Push |
| Settings | Lưu website/ID/password/options | Không trả lại password đã lưu |
| Scope | Áp dụng phạm vi | Chỉ đường ngang hợp lệ; không thay full route |
| Monitor | Bắt đầu/dừng/đổi chế độ ngày | Running, generation, lịch đúng account |
| State | Snapshot giao diện | Ngày, scope, route, lỗi, tàu, devices, freshness, completeness |
| History | Tải đầy đủ ngày D | Job ID/trạng thái thật nếu tác vụ dài |
| Crossing | Chi tiết điểm/ngày | Thiết bị, Đếm trục lỗi, raw/nhóm, cursor |
| Incidents | Danh sách lỗi/ngày/bộ lọc | Incident pairs, paging đầy đủ |
| Alerts | Nhận đợt mới sau cursor | Không trả backlog thành âm khi reconnect/resume |
| Confirm | Xác nhận batch đang báo | Hủy đúng batch/ID/cutoff, giữ mode/running/faults |
| Retry | Bỏ lịch chờ lỗi mạng | Không tạo request song song |
| Push | Đăng ký/hủy subscription thiết bị | VAPID public key, endpoint/keys, permission state |
| Health | Kiểm tra backend/worker/DB | Không chứa credential hoặc báo cáo riêng |

Có thể dùng SSE/WebSocket/poll ngắn để cập nhật trạng thái và loading khi trang visible. Khi trang hidden, backend vẫn tự làm việc. Frontend polling không phải nguồn duy nhất duy trì monitor.

### 19.3. Ví dụ snapshot giao diện, cấu trúc chứ không phải dữ liệu thật

```text
configured
selectedDay / historical / followsToday
settings: url, user                         # không password
options: running, mode, audibleMode, scope
route / points                             # toàn tuyến
faults                                     # active faults trong scope/ngày
incidents                                  # đủ cặp theo ngày, phân trang nếu cần
trains                                     # tiếp cận đúng ngày, không mất phần ngoài page đầu
segments / occupancy                       # tách biệt ý nghĩa
crossingDevices / axleSummary / eventGroups # theo điểm/ngày yêu cầu
historyReady / incompleteReason
loading / connectionLost / status
lastSuccess / lastAttempt / nextRetryAt
dataRevision / routeRevision
alertCursor / newAlerts
platformCapabilities / pushRegistered
```

Nếu snapshot là lịch sử, dùng mốc kết thúc ngày để dựng trạng thái ngày đó; không áp baseline hôm nay lên ngày cũ. Không trả checkpoint/private credentials trong API công khai chỉ để tiện debug.

### 19.4. Bảo toàn điều kiện dừng đợt âm

Confirm nên xác định batch hoặc cutoff đang được người dùng xác nhận tại thời điểm bấm. Sau xử lý:

```text
faults_after = faults_before
running_after = running_before
mode_after = mode_before
current_audio = stopped
future_batch_allowed = true
```

Không đặt `mode=silent` để giả lập confirm. Không đặt một cờ `acknowledged=true` toàn account khiến mọi lỗi tương lai bị câm. Các callback queue phải có generation/token để không phát lại đợt cũ sau khi hủy.

### 19.5. Snapshot và công việc bất đồng bộ

Mỗi request/job gắn account identity và generation. Mỗi response gắn ngày/scope/revision. Kiểm tra trước khi commit và trước khi render. Thay account khi đang tải hoặc dừng service không để job cũ tạo lịch retry, thông báo hoặc ghi route sang account mới.

Không giữ khóa DB toàn thời gian tải mạng nhiều trang. Có thể tải vào staging, rồi khóa account ngắn để merge/derive/commit. Poll cho một account phải tuần tự; lịch sử cần cơ chế không làm hỏng snapshot live.

---

## 20. Triển khai, bảo mật và khả năng chẩn đoán

### 20.1. Backend phải thực sự chạy khi không có request người dùng

Nếu dùng Google Cloud Run với worker ở trong process service:

- Cấu hình CPU luôn cấp cho worker, ít nhất một instance luôn hoạt động.
- Không để chỉ chạy CPU khi có HTTP request rồi tuyên bố theo dõi nền.
- Có khóa/lease trong DB chống hai revision cùng poll khi deploy.
- Giới hạn instance/concurrency theo kiến trúc thực, không nhân loop theo mỗi request.
- Có health/giám sát worker sống và mốc cập nhật từng account.

Nếu dùng worker/service/scheduler riêng thì có thể chọn thiết kế khác, nhưng vẫn phải chứng minh nhịp 30 giây và retry 120 giây không phụ thuộc người mở trang. Cron một phút đơn giản không thỏa chu kỳ 30 giây đã chốt.

### 20.2. Lưu bền vững

Không dùng một file SQLite trên đĩa tạm Cloud Run làm DB duy nhất cho sản phẩm công khai. Dùng Cloud SQL/PostgreSQL hoặc kho bền vững có transaction/lease phù hợp.

Timestamp epoch mili giây phải là **BIGINT/64 bit**, không dùng INTEGER 32 bit của PostgreSQL. Có unique account+event ID, index theo account/time, chống insert trùng khi retry. Có backup DB và khóa mã hóa cần để đọc dữ liệu.

Nếu chỉ prototype cục bộ thì SQLite được, phải ghi rõ đó là môi trường phát triển; không dùng kết quả cục bộ để nói redeploy cloud giữ dữ liệu.

### 20.3. Secrets và bảo vệ phiên

- Credential website mã hóa phía server, tách theo account; key trong Secret Manager hoặc cơ chế bí mật tương đương.
- Session cookie Secure, HttpOnly, SameSite phù hợp; CSRF cho thao tác thay đổi.
- HTTPS cho web sản phẩm; không nội suy mật khẩu vào URL/log/command line.
- Không nhúng secret vào JavaScript, file `.env` public, Git, ZIP, ảnh chụp hoặc prompt.
- Không lưu mật khẩu website trong localStorage/IndexedDB ở phía client như giải pháp bảo mật.
- Thay account phải xác thực thật trước khi cấp quyền đọc cache của account đó.
- Cache của Service Worker chỉ giữ shell/asset, **không cache `/api/` chứa dữ liệu riêng** như tài nguyên công cộng.
- Endpoint Push và URL nguồn phải được kiểm tra, không làm SSRF/open proxy.
- Giữ khóa VAPID và domain sản phẩm ổn định qua deploy, nếu không subscription cũ có thể mất hiệu lực.
- Giữ key mã hóa ổn định qua deploy; không tạo key mới mỗi lần container khởi động rồi mất khả năng đọc mật khẩu trong DB.
- Khóa ký Android không dùng cho web, không yêu cầu chuyển vào môi trường AI Studio.

### 20.4. Chẩn đoán mà không làm lộ bí mật

Nên ghi thông tin sạch:

```text
account internal ID (không password)
request/job ID và generation
giai đoạn: DNS/TCP/login/selectDay/page N/validate/derive/commit
thời điểm bắt đầu/kết thúc và thời gian xử lý
ngày đang đọc, số trang/số bản tin, full hay delta, overlap có hay không
lastSuccess, lịch retry dự kiến và lỗi phân loại
worker lease / tuổi checkpoint / data revision
```

Không ghi raw HTML trang đăng nhập, cookie, token Push hoặc URL có secret. UI người dùng báo gọn và đúng lớp lỗi; chi tiết vận hành để log sạch.

Khi mất dữ liệu từ 14:00, kiểm tra lần thành công cuối và từng lớp từ điện thoại → backend → website nguồn → parser → DB. Không chỉ hiện spinner liên tục rồi ghi “Đang kết nối” mãi.

### 20.5. Bằng chứng kết nối website thật

Sau triển khai từ đúng backend Cloud Run:

- Kiểm tra DNS/TCP port 7447 và phản hồi website.
- Kiểm tra login, tuyến và một ngày có nhiều trang bằng tài khoản được phép.
- Đối chiếu tổng bản tin/điểm/ngày, một số lỗi và cặp khôi phục với nguồn thật.
- Thử ít nhất một lần delta có overlap và một lần full.
- Không công bố tài khoản/báo cáo vận hành lên Git.

Môi trường viết tài liệu hiện không được phép truy cập Visitec do policy domain. Tài liệu mô tả adapter đã có, không phải kết quả audit website thật trong ngày lập tài liệu này.

---

## 21. Những yêu cầu cũ đã bị thay thế và lỗi tuyệt đối không được tái tạo

### 21.1. Bảng quyết định mới nhất

| Nội dung cũ có thể thấy trong source/doc/ảnh | Quyết định hiện hành |
|---|---|
| Chọn 30 giây, 2 phút, 5 phút | Chỉ 30 giây; 120 giây là retry lỗi, không là lựa chọn polling |
| Hoãn giọng nói sau 0.26 | 0.27 đã có Giọng nói/Chỉ chuông/Im lặng |
| Phát lại lỗi đang mở khi bắt đầu phiên | 0.27.2 bỏ phát lại lỗi cũ; chỉ lỗi mới đủ điều kiện |
| Nhãn Dừng chuông/giọng đọc | Nhãn đúng XÁC NHẬN |
| Không có xác nhận/không cần nút tắt ở summary | Có XÁC NHẬN tại summary và Android notification; chỉ dừng batch |
| Lưu 15 ngày | 30 ngày gồm hôm nay |
| Nút Thử chuông báo lỗi trong Android | Đã bỏ từ 0.21; web chỉ thêm bước xin quyền âm nếu nền tảng cần |
| Xem trên sơ đồ tuyến trong chi tiết | Đổi thành Chi tiết mở lịch sử đầy đủ |
| Tàu chấm xanh tắt khi nguồn khôi phục | Sơ đồ giữ đoạn liên tục tới nhận lượt/đúng kết thúc |
| Tàu đang chờ tự tắt theo phút | Không timeout tự hết |
| Ga Nghi Long ghi đầy đủ trên nhãn | Nhãn Ga NL; khóa vẫn Nghi Long |
| Ngoại lệ trục chỉ 320+445 | Sáu điểm ở mục 12.7 |
| Ngưỡng lệch >10 hoặc hơn sáu lần | abs(delta) ≥10 đơn lẻ; đủ lần thứ 6 với abs(delta) ≥3 |
| Đặt kết nối ở cuối Tổng quan | Ô nhỏ giữa summary và sơ đồ |
| Giữ LIVE chỉ vì vẫn có cache | Chỉ LIVE khi theo dõi và dữ liệu mới hợp lệ |
| Khôi phục hệ thống xóa hết lỗi | Chỉ vòng đời phù hợp; giữ phần cứng chưa khôi phục đúng loại |
| Web hướng dẫn ngoài nói polling 15 giây | Giữ nghiệp vụ 30 giây; không hứa timer iOS chạy nền |

### 21.2. Các kiểu triển khai sai cần chủ động kiểm tra

- Chỉ dựng frontend và dùng mảng dữ liệu mẫu.
- Chỉ tải trang đầu, chỉ lọc Trở ngại hoặc chỉ lấy điểm trong scope từ nguồn.
- Hard-code tuyến mẫu 13 điểm, cộng ga vào số đường ngang.
- Tìm chữ “tàu” là tạo Journey, kể cả lỗi đỏ.
- Một boolean xanh/đỏ cho tất cả phần cứng, tàu và lỗi.
- Nguồn đã khôi phục thì coi chưa từng có tàu, làm mất bằng chứng phía trước.
- Một nguồn tàu được dùng lại cho nhiều lượt sau.
- Thử thiết bị ở điểm sau làm vô hiệu nguồn rõ hướng chưa dùng.
- Tàu đã qua tạo thêm tàu mới thay vì chuyển đoạn/ghép lượt.
- Có tàu mới thì xóa toàn tuyến hoặc mọi Journey tại điểm.
- Dùng timeout 2 phút ghép đảo thứ tự để xóa tàu đang chạy.
- Thấy Khôi phục hệ thống thì xóa Chuông/Đèn/Cảm biến hỏng.
- XÁC NHẬN = im lặng vĩnh viễn hoặc = lỗi đã sửa.
- Mở lại app đọc lại toàn bộ lỗi cache.
- Chỉ tính đếm trục ở live, lịch sử/máy mới thiếu lỗi Cảm biến.
- Biến sáu điểm ngoại lệ thành bỏ qua mọi lỗi thiết bị.
- Tải ngày cũ nhưng render data hôm nay hoặc phát chuông lịch sử.
- Dọn raw rồi mới lưu checkpoint; redeploy xóa DB/khóa.
- Tô tàu xám/đỏ khi stale thay vì giữ xanh và gắn nhãn dữ liệu cũ.
- Loading do setTimeout giả hoặc lastSuccess cập nhật khi chỉ gửi request.
- Dùng một trang Chrome giả lập iPhone để kết luận đã thử Safari/khóa iPhone thật.

---

## 22. Các ví dụ nghiệp vụ có đầu vào và đầu ra cụ thể

Mọi ví dụ dưới đây là **dữ liệu tổng hợp**. A/B/C là các đường ngang khác nhau tăng km, không có ga ở giữa trừ khi ghi rõ. Mốc giờ chỉ biểu diễn trình tự, không tạo quy tắc tốc độ hoặc TTL mới.

### VD01 — Tiếp cận và đã qua không tạo hai tàu

```text
08:00:00 A: Tàu lẻ
08:01:00 A: Tàu lẻ đã qua đường ngang
08:02:00 A: Khôi phục hệ thống
08:04:00 B: Tàu lẻ
08:05:00 B: Tàu lẻ đã qua đường ngang
```

Kết quả: trước 08:01 sáng phía đến A; từ 08:01 sáng A–B; 08:02 không mất A–B; 08:04 vẫn một đoạn A–B của tàu đang tới B; 08:05 chuyển B–C, không còn đoạn A–B của cùng lượt.

### VD02 — Chiều chẵn đối xứng

```text
09:00 C: Tàu chẵn
09:01 C: Tàu chẵn đã qua đường ngang
09:02 C: Khôi phục hệ thống
09:04 B: Tàu chẵn
09:05 B: Tàu chẵn đã qua đường ngang
```

Kết quả C–B rồi B–A, nhãn chẵn quay trái. Khôi phục C không xóa C–B trước khi tàu tới B.

### VD03 — Cần chắn nâng không khôi phục phần cứng

A có tiếp cận, sau đó Cần chắn 1 đã nâng. Chiếm dụng A vẫn còn cho tới Khôi phục hệ thống đúng A. Lỗi Chuông 2 hỏng đang tồn tại vẫn tồn tại sau cả cần nâng lẫn khôi phục hệ thống.

### VD04 — Đường ngang kế tiếp cách xa

A đã qua và khôi phục; B chưa có bản tin trong 20 phút. Giữ đoạn A–B, không hết hạn theo 20 phút. Ghi dữ liệu cũ nếu nguồn chưa cập nhật, không làm tàu biến mất để tránh “treo”.

### VD05 — Điểm sau thiếu tiếp cận nhưng có đã qua

A đã qua, B không có dòng Tàu lẻ, sau đó có Tàu lẻ đã qua đường ngang tại B. Nối được lượt phù hợp, tắt A–B và chuyển B–C. Không giữ thêm một tàu A–B vì thiếu tiếp cận B.

### VD06 — Hoạt động cần chắn đơn lẻ

Chỉ có B: Cần chắn 1 đã hạ, Cần chắn 2 đã nâng, Khôi phục hệ thống, không có nguồn tàu đi tới. Không tạo Journey tàu thật hoặc đoạn xanh mới. Vẫn lưu mọi dòng trong lịch sử.

### VD07 — Có nguồn đi tới, thiếu báo tàu ở B

Có nguồn lẻ phù hợp từ A đang đi tới B. B có hoạt động cần chắn đủ điều kiện nhưng thiếu tiếp cận. Được dùng Journey chuyển động nội bộ để nối đúng lượt; không thêm raw Tàu lẻ giả. B nhận tiếp cận bổ sung đúng lượt thì thay ID/ghép vào, không có hai tàu.

### VD08 — Hai tàu nối tiếp

A có lượt A1 rồi A2 ở thời điểm sau. B đang nhận lượt tương ứng A1. Không dùng sự kiện B của lượt A1 để xóa A2 mới hơn. Nếu có dấu treo cũ hơn nguồn phù hợp tại đúng đoạn/hướng thì dọn dấu cũ đó, giữ A2 theo sau và các đoạn khác.

### VD09 — Cùng điểm có lượt lỗi và lượt bình thường

A có Journey S bị selfTriggered/stalled và Journey N độc lập bình thường. A nhận Phục hồi cho lượt gián đoạn. Đóng đúng S, giữ N; không chọn N chỉ vì N có timestamp mới hơn. Raw lỗi thiết bị không bị xóa.

### VD10 — Hai hướng và đoạn khác nhau

Một tàu lẻ ở A–B và một tàu chẵn ở D–C. B có xác nhận mới chỉ liên quan lượt A–B. Không xóa D–C. Không dùng một biến `currentTrain` toàn tuyến.

### VD11 — Ga chặn liên kết

A–Ga X–B. A báo lẻ không phải bằng chứng nguồn của B để suy luận qua ga. B phía ra có báo tàu thì mới tạo đoạn Ga X–B. A đã qua hướng vào ga thì A–Ga X giữ tới khôi phục A. Bản tin ga không giữ nét xanh cả ngày.

### VD12 — Thiếu điểm giữa, có chuỗi rõ hướng

A–B–C cùng khu đoạn. A có lẻ trước, B thiếu, C có lẻ theo thứ tự đủ điều kiện. Sử dụng quy tắc chuỗi của bản 0.26 để nối/gỡ đúng suy luận. Thêm ga giữa A và C thì không được bắc cầu. Đổi C thành chẵn thì không được dùng A lẻ để giải thích.

### VD13 — Nghi tự kích và bằng chứng tới muộn

Lần đọc 10:00 thấy B tiếp cận lẻ mới, không nguồn đủ điều kiện: chỉ pending. Lần đọc thành công sau ≥30 giây vẫn thiếu: tạo `self:<id>` Nghi tự kích. Sau đó nguồn/bằng chứng cùng lượt từ A đến muộn: gỡ riêng self, giữ bản tin B, Chuông 2 hỏng nếu có và cập nhật phân loại sơ đồ phù hợp.

### VD14 — Nguồn đã khôi phục vẫn dùng được

A có lẻ rồi Khôi phục hệ thống, chưa bị dùng cho B. B có lẻ phù hợp sau đó. Không suy luận Nghi tự kích chỉ vì A hiện không còn chiếm dụng. Xét lịch sử lượt, không chỉ mảng occupied hiện tại.

### VD15 — Thao tác thử không loại nguồn

A có nguồn rõ hướng chưa dùng. B: Duy tu → hạ cần → Phục hồi → nâng cần, rồi B báo lẻ thật. Chuỗi thao tác không tự là một lượt thực trước đó làm nguồn A vô hiệu. Sau khi A được dùng cho B1, B2 thiếu nguồn mới vẫn có thể bị nghi; không tái dùng A.

### VD16 — Đúng điểm và đúng số thiết bị

```text
11:00 A: Chuông 1 hỏng (đỏ)
11:01 A: Chuông 2 hỏng (đỏ)
11:02 B: Chuông 1 khôi phục tốt
11:03 A: Chuông 2 khôi phục tốt
```

Tại A chỉ Chuông 1 còn mở; cặp Chuông 2 được đánh dấu khôi phục lúc 11:03. Bản tin ở B không khép Chuông 1 của A.

### VD17 — Điện áp cảm biến khác cảm biến hỏng

A: Cảm biến 6 hỏng + Điện áp cảm biến 6 < 5V. Sau Điện áp cảm biến 6 khôi phục tốt, vẫn còn Cảm biến 6 hỏng. Cả hai lúc lỗi thuộc nhóm Cảm biến; không chuyển điện áp cảm biến vào Điện áp nguồn chung.

### VD18 — Nguồn đặc biệt không tô đỏ

Cầu chì 50A bị đứt không có CSS đỏ vẫn thành lỗi; Cầu chì 50A khôi phục tốt cùng điểm khép nó. Cầu chì 30A khôi phục tốt hoặc khôi phục tại điểm khác không khép lỗi 50A. Nguồn 24V khôi phục tốt khép đúng lỗi nguồn DC >29v, giữ nguồn AC/cảm biến khác.

### VD19 — Ngưỡng trục nhỏ

CB3 tại một điểm không ngoại lệ có sai lệch lần lượt +3,+1,-3,+2,+3,+3,-3,+3 trong ngày. Các |delta|≥3 là sáu lần: lượt cuối tạo cảnh báo ngày. Hai lần +1,+2 không cộng số lần ngưỡng, nhưng vẫn cộng tổng thừa. Một lần đúng sau đó không xóa cảnh báo trước 00:00.

### VD20 — Ngưỡng lớn và chống lặp

CB3 lệch +10: một cảnh báo lớn ngay. Tải lại cùng ID không cảnh báo nữa. Một source ID khác có CB3 lệch -10 sau đó là cảnh báo mới. CB4 đạt ngưỡng có cảnh báo riêng. Nếu +10 đúng lần đủ thứ 6 thì chỉ một cảnh báo CB3 cho source ID đó.

### VD21 — Ngoại lệ trục không bỏ lỗi gốc

Tại 320+445 có sáu/more cột số lệch và một dòng đỏ Chuông 1 hỏng. Không tự sinh ghi chú/cảnh báo trục, vẫn hiển thị và báo Chuông 1 hỏng, vẫn có tàu. Lặp cùng ca cho cả năm điểm ngoại lệ còn lại.

### VD22 — Ngày cũ/máy mới

Chưa có cache, tải ngày D có đủ sáu lần sai CB3. Tab Sự cố phải có cảnh báo, Chi tiết Cảm biến cũng có cảnh báo và Đếm trục lỗi có thống kê. Không âm/Push. Sang máy có cache cùng raw phải ra kết quả giống. Từ lỗi mở chi tiết giữ ngày D.

### VD23 — XÁC NHẬN khác Im lặng

Chế độ Giọng nói đang đọc A/B. Bấm XÁC NHẬN: A/B ngừng đọc và rung ngay, vẫn đỏ, mode vẫn Giọng nói, running vẫn true. C phát sinh sau đó: hai chuông + sáu lượt đọc C. Nếu bấm Tắt âm thanh thay vì XÁC NHẬN thì mode sang Im lặng, C chỉ thông báo/hiển thị.

### VD24 — Restart không replay

Có ba lỗi cache đang mở. Đóng/mở trang, tạo lại worker hoặc service: vẫn ba lỗi nhưng không đọc ba lỗi cũ. Một lỗi thứ tư vừa được nhập hợp lệ sau đó phải báo. Không dùng bộ Set rỗng mới trong RAM làm “tất cả đều là lỗi mới”.

### VD25 — Mất kết nối và retry thật

14:00:00 bắt đầu đọc, 14:00:25 timeout. LastSuccess vẫn mốc trước, mất LIVE, giữ fault/segments, ô giữa summary/sơ đồ hiện nút đỏ và Mất kết nối. Không có browser mở, backend bắt đầu lại khoảng 14:02:25. Nếu user bấm 14:01 thì thử ngay, không đợi. Thành công mới xóa loss và quay nhịp 30 giây.

### VD26 — HTTP 200 nhưng sai trang

Website trả trang login/HTML không có đúng ngày hoặc bảng. Không ghi mảng rỗng, không xóa lỗi, không đánh dấu lastSuccess=now. Phân biệt login failed với parser failed. Tài khoản sai thật không bị thử vô hạn mỗi hai phút.

### VD27 — Pagination bị thay đổi trong lúc đọc

Trang đầu ghi anchors X/Y/Z. Sau đọc nhiều trang, quay trang đầu còn Y nhưng có thêm W: nhập W và giữ đủ ngày. Nếu trang đầu mới không còn anchor nào, đọc lại snapshot từ đầu, không công bố một ngày bị hụt. Nếu trang 2 lặp trang 1 thì báo lỗi thay vì lặp vô hạn.

### VD28 — Qua 00:00

Lỗi phát sinh hôm qua ra khỏi khối hiện tại, archive hôm qua còn nguyên và chưa khôi phục nếu chưa có bản tin thật. Tàu A–B đang chờ vẫn xanh. Picker theo hôm nay tự sang ngày mới, người đang chủ động tra ngày D không bị kéo về hôm nay.

### VD29 — Dọn 30 ngày

Một Journey có raw nguồn 35 ngày trước còn đang chờ. Trước khi dọn, lưu checkpoint của nó bền vững. Xóa raw quá sàn không làm mất đoạn. Nâng quy tắc/rebuild retained raw cũng không được xóa Journey chỉ vì không tìm thấy source raw đã bị dọn hợp lệ.

### VD30 — Scope vẫn giữ toàn tuyến

Tuyến A–B–C–D, user chọn B đến C. List/alerts lọc B,C; schematic vẫn A,B,C,D; nguồn A vẫn đủ để giải thích B khi phù hợp. Không phát âm lỗi D ngoài phạm vi. Thay scope không dựng lại một topology chỉ có B,C.

### VD31 — URL/ID đổi trong lúc request đang chạy

Job A đang tải trang 5. User đổi sang B. Khi job A trả về, không được ghi route/lỗi/checkpoint vào B hoặc phát giọng A. Cookie website và data namespace tách biệt. B không kế thừa trạng thái Mất kết nối của A.

### VD32 — iPhone khóa màn hình

Không mở web vẫn có bản ghi worker 30 giây và notice mới trên server; Push được kiểm tra trên iPhone đã cấp quyền. Kết quả notification sound/vibration/media được ghi riêng theo iOS thật. Không lấy việc server poll được làm bằng chứng iPhone đã đọc TTS khi khóa, và ngược lại.

---

## 23. Ma trận nghiệm thu và bằng chứng phải nộp

Các mục dưới đây là **việc bên viết web phải kiểm chứng**, không phải tuyên bố bản web mới đã làm xong. Ca mô phỏng, website thật, trình duyệt và iPhone thật phải ghi tách riêng.

### 23.1. Nguồn website và đồng bộ

| ID | Ca bắt buộc | Đạt khi |
|---|---|---|
| SRC-01 | Login có cả nút Thoát và Đăng nhập | Chọn đúng login, giữ hidden fields/cookie |
| SRC-02 | Form action tương đối, base có thư mục | Resolve đúng, không hard-code API giả |
| SRC-03 | Cookie/VIEWSTATE đổi theo trang | Theo phiên mới nhất, không mất form state |
| SRC-04 | Filter mặc định một điểm/Trở ngại | Chủ động chọn Tất cả điểm và bản tin |
| SRC-05 | Tuyến có điểm chưa có event | Vẫn có điểm từ inventory |
| SRC-06 | Hơn hai trang dữ liệu | Đọc được mọi trang, không chỉ đầu |
| SRC-07 | Postback Page$N và href thường | Cả hai dạng được xử lý |
| SRC-08 | Ngày 1/6,01/06,01/6,1/06 | Cùng ngày, đúng năm/tháng |
| SRC-09 | Form/trang phụ sai ngày | Bỏ snapshot, giữ data trước |
| SRC-10 | Tuyến thay giữa các trang | Không publish snapshot mâu thuẫn |
| SRC-11 | Trang rỗng thật và trang HTML lạ | Phân biệt, không xóa dữ liệu bởi trang lạ |
| SRC-12 | Trang phân trang lặp | Có lỗi, không loop vô hạn/không báo đủ |
| SRC-13 | Bản tin đầu bảng tăng khi đọc | Quay kiểm tra anchor và nhập mới |
| SRC-14 | Mất anchor | Thử snapshot lại, không lưu nửa ngày |
| SRC-15 | Delta có/không overlap | Phân biệt connected coverage, không lẫn TCP loss |
| SRC-16 | Full refresh sau khoảng 15 phút | Nhận bổ sung muộn, không phát lại IDs cũ |
| SRC-17 | Login sai/hết phiên | Login lại hợp lệ; sai thật dừng lặp |
| SRC-18 | DNS/TCP/HTTP server lỗi | Không bảo nhập lại mật khẩu vô cớ |

### 23.2. Tàu, ga và liên kết giữa các điểm

| ID | Ca bắt buộc | Đạt khi |
|---|---|---|
| TRN-01 | Lẻ tiếp cận→đã qua→khôi phục | Đúng đoạn đến/đi, không mất giữa hai điểm |
| TRN-02 | Chẵn tương tự | Đối xứng hướng/đoạn |
| TRN-03 | Đã qua ở điểm sau thiếu tiếp cận | Nối đúng lượt, không nhân đôi |
| TRN-04 | Cần nâng chưa khôi phục | Occupancy vẫn có, không xóa lỗi khác |
| TRN-05 | Giữ khoảng giữa hai điểm lâu | Không TTL tự tắt |
| TRN-06 | Qua ngày/restart | Đoạn chưa kết thúc còn đúng |
| TRN-07 | Đường ngang–ga vào/ra | Không nối xuyên, tắt theo khôi phục điểm giáp |
| TRN-08 | Bản tin ga cả ngày | Không tạo tàu giữ cả ngày |
| TRN-09 | Nhiều tàu cùng hướng nối tiếp | Không xóa lượt theo sau |
| TRN-10 | Hai hướng ở đoạn độc lập | Cùng hiển thị đúng |
| TRN-11 | Nguồn cũ treo, tàu mới nhận đoạn | Dọn đúng dấu cũ ngay khi nhận, giữ đoạn khác |
| TRN-12 | Chuỗi hạ/nâng/nút không nguồn | Không tự tạo tàu |
| TRN-13 | Có nguồn, activity hỗ trợ | Journey nội bộ, không raw giả |
| TRN-14 | Activity rồi arrival thật | Promotion không sinh hai Journey cùng tàu |
| TRN-15 | Thiếu điểm giữa, chuỗi cùng hướng | Bắc cầu đúng khu đoạn, không qua ga |
| TRN-16 | Phục hồi/Duy tu/Cảnh báo/system reset | Chọn đúng lượt gián đoạn |
| TRN-17 | Lượt lỗi và bình thường cùng điểm | Không xóa nhầm lượt bình thường |
| TRN-18 | Report tới muộn/đảo thứ tự gần | Không hồi sinh dấu đã đóng; 120s chỉ cho matching |
| TRN-19 | Mất mạng/dữ liệu cũ | Đoạn và mũi tên vẫn xanh, ga vàng |

### 23.3. Lỗi, tự kích, đếm trục và cảnh báo

| ID | Ca bắt buộc | Đạt khi |
|---|---|---|
| FLT-01 | Nhiều chuông/đèn/cần cùng điểm | Khôi phục đúng thiết bị |
| FLT-02 | Cùng thiết bị khác điểm | Không xóa chéo |
| FLT-03 | Sensor hỏng và sensor voltage | Không ghép nhầm; cùng nhóm Cảm biến |
| FLT-04 | Khôi phục hệ thống còn hardware fault | Hardware vẫn đỏ |
| FLT-05 | Cần chắn khôi phục tập thể | Chỉ các cần tại đúng điểm |
| FLT-06 | Cầu chì 50A không CSS đỏ | Vẫn lỗi, khôi phục đúng cặp |
| FLT-07 | DC>29v→24V recovered | Đúng cặp, giữ AC/cảm biến |
| FLT-08 | Lỗi lạ đỏ chưa có group | Không làm rơi khỏi Sự cố |
| SELF-01 | Thiếu nguồn mới | Pending rồi ≥30 giây/lượt đọc thành công mới xác nhận |
| SELF-02 | Có nguồn đã khôi phục | Không coi mất bằng chứng |
| SELF-03 | Bằng chứng tới muộn | Gỡ riêng self, giữ raw/hardware |
| SELF-04 | Hai lượt dùng một nguồn | Chặn tái dùng |
| SELF-05 | Thao tác thử trước arrival | Không loại nguồn thật, không tạo tàu từ thử |
| SELF-06 | Nguồn xa, cùng hướng, có ga/ngược hướng | Chỉ chấp nhận đúng điều kiện trong khu đoạn |
| SELF-07 | Điểm đầu/cuối/giáp ga | Không suy luận vượt topology |
| SELF-08 | Mất kết nối thiết bị | Không thêm miễn suy luận trái yêu cầu |
| AXLE-01 | 1/2,3,9,10 trục, âm/dương | Đúng các biên đã chốt |
| AXLE-02 | Đủ lần thứ 5,6,7 | Chỉ lần thứ 6 theo số lần; lần 7 nhẹ không nhắc |
| AXLE-03 | Vừa ngưỡng 6 vừa ≥10 | Một cảnh báo/source/CB |
| AXLE-04 | CB khác/lỗi lớn mới | Vẫn có cảnh báo riêng |
| AXLE-05 | Hòa chuẩn, số V, thiếu/sai số | Ghi chú/không tính đúng loại |
| AXLE-06 | Sáu điểm ngoại lệ + lỗi đỏ thật | Bỏ derived axle, giữ lỗi thật/tàu |
| AXLE-07 | Reset/restart/tải bù | Không nhắc lại ngưỡng ngày |
| AXLE-08 | Cold/warm history | Sự cố + Cảm biến + Đếm trục lỗi đủ và giống |
| ALM-01 | Giọng nói/Chỉ chuông/Im lặng | Đúng 2+6,6,không âm/rung theo khả năng nền tảng |
| ALM-02 | XÁC NHẬN lúc đang đọc/rung | Dừng ngay batch, giữ mode/fault/running |
| ALM-03 | Lỗi mới sau confirm | Được phát, không callback cũ hủy nhầm |
| ALM-04 | Mở lại/restart/cùng IDs | Không replay lỗi cũ |
| ALM-05 | Global mute rồi bật lại | Nhớ chế độ có âm trước, không giả confirm |
| ALM-06 | Lịch sử/khôi phục/bác bỏ/stop | Không âm lịch sử, không tiếp tục đọc lỗi đã hết |
| ALM-07 | Không có giọng Việt | Báo rõ, không tiếng Anh/rung vô hạn |

### 23.4. Lưu trữ, UI và chạy nền

| ID | Ca bắt buộc | Đạt khi |
|---|---|---|
| DB-01 | 30 ngày qua tháng/năm/timezone | Đúng today-29, không theo UTC |
| DB-02 | Checkpoint trước prune, crash giữa chừng | Tàu nguồn cũ vẫn còn |
| DB-03 | Migration/backfill trước checkpoint | Rebuild giữ expired-source pending |
| DB-04 | Account đổi khi job đang tải | Không data/âm/route chéo |
| DB-05 | Redeploy/restart DB/key/VAPID | Không mất credential/history/Push |
| UI-01 | Tổng quan bố cục | Summary→ô kết nối nhỏ→sơ đồ→tàu→lỗi |
| UI-02 | Mất kết nối | Chỉ lúc lỗi có retry đỏ và Mất kết nối dưới nút |
| UI-03 | Loading request chậm thật | Spinner đúng vòng đời, không phần trăm giả |
| UI-04 | Thành công sau loss | Ẩn retry/warning, vẫn còn trạng thái kết nối |
| UI-05 | 320/375/390/430px và safe area | Không che nút/nav, không tràn ngang toàn trang |
| UI-06 | Poll/đổi tab/nhập Cài đặt | Không nháy trắng, giữ scroll/form |
| UI-07 | Ba filter đường ngang + ba filter Sự cố | Counts/scope/incident order đúng dữ liệu |
| UI-08 | Detail/ngày/history grouping | Giữ ngày, mỗi arrival tách lượt, loose 5/15 phút |
| UI-09 | Dữ liệu dài | Đọc được tất cả event/group/train, không mất sau page đầu |
| UI-10 | Tốt/Không lỗi/Chưa đủ/Đã đặt lại | Không gộp nhầm thành Đã khôi phục/an toàn |
| BG-01 | Không browser mở | Backend vẫn đọc nguồn theo nhịp |
| BG-02 | IO lỗi ở nền | Retry sau 120s kể từ kết thúc lỗi |
| BG-03 | Retry tay/poll chậm/restart | Không poll song song/callback cũ lên lịch |
| IOS-01 | A2HS và xin quyền thật | Icon/standalone/permission đúng iOS hỗ trợ |
| IOS-02 | Web Push lúc khóa/PWA đóng | Nhận trên iPhone thật, ghi độ trễ/Focus/network |
| IOS-03 | Giọng/chuông/rung foreground | Kiểm tra thật, không mock rồi nhận đã nghe |
| IOS-04 | Media nền nếu có | Thử khóa lâu, cuộc gọi/đổi mạng/timeout/confirm/new fault |
| IOS-05 | Từ chối quyền/không hỗ trợ | Báo rõ, không giả đã bật |

### 23.5. Bằng chứng phải giao khi hoàn tất

- URL HTTPS sản phẩm, revision/commit triển khai và mã nguồn đầy đủ.
- Ảnh bốn tab và chi tiết thiết bị/lịch sử/Đếm trục lỗi, gồm dữ liệu cũ và loading.
- Bảng tất cả ID nghiệm thu: đạt/chưa đạt/chưa thử + bằng chứng.
- Kết quả so với core Java trên cùng fixture, không chỉ ảnh “có vẻ giống”.
- Kết quả login/full pagination/delta website thật, không lộ dữ liệu riêng.
- Log sạch worker vẫn chạy khi không có browser và phép đo retry 120 giây từ lần lỗi kết thúc.
- Kết quả iPhone vật lý: máy/iOS, A2HS, permission, khóa màn hình, âm/rung/Push/media, mạng và thời lượng thử. Nếu chưa thử ghi đúng chưa thử.
- Hướng dẫn vận hành, cập nhật, giữ DB/key/VAPID, backup và xem lỗi.

Build pass/lint pass, Docker chạy hoặc Chromium chụp đẹp **không thay cho** kiểm tra đầy đủ nghiệp vụ/website/iPhone.

---

## 24. Bản đồ mã nguồn, tài liệu và cách chuyển thuật toán

### 24.1. Mã chuẩn đã cố định

Các file Android ở commit `ee1affe5ea024590ce356e0f1e23aafbf17df669`, thư mục `app/src/main/java/vn/duongngang/monitor/`:

| File | Vai trò/đọc để chuyển đúng |
|---|---|
| `Rules.java` | Topology, raw Event, direction, chiếm dụng, Journey/segments, ghép lượt, self evidence, axle, fault pairing, 30-day cutoff |
| `WebSource.java` | ASP.NET login, fields, cookie, date/filter, route, pagination, snapshot/delta, parse HTML |
| `Store.java` | Account namespace, mã hóa, retention/checkpoint, replay, derived cache, baseline, dấu chống báo trùng |
| `MonitorService.java` | Background scheduler 30/120, first/full/delta, xác thực, mute/confirm, notifications, pending/dispatch |
| `AlertOptions.java` | 3 modes, nhớ sound, 6 repeats, nội dung đọc và chia đoạn Unicode |
| `VoiceAlarm.java` | Hàng đợi/batch TTS Việt, token hủy, chime intro, focus, lỗi giọng và dừng rung |
| `AlarmSound.java` | Sáu hồi và vòng đời player/rung/chặn phát sai |
| `MainActivity.java` | Bốn tab, tất cả nhãn/màu/layout, ngày/scope, chi tiết sáu nhóm, thống kê trục, lịch sử nhóm, freshness |

Links mã nguồn cố định bắt đầu từ:

https://github.com/cuongthinkpad-ui/app-duong-ngang/tree/ee1affe5ea024590ce356e0f1e23aafbf17df669/app/src/main/java/vn/duongngang/monitor

APK đối chiếu: `downloads/DuongNgang-0.27.3.apk`, SHA-256:

```text
3ae382809ab011904c61b5d4e6ea0659d9b2e2f947325dea5c9bf64bf7999288
```

APK không cần đưa lên server web. Khóa riêng ký APK không nằm trong gói tham chiếu và không cần cho task này.

### 24.2. Những hàm nghiệp vụ không được thay bằng vài if đơn giản

| Yêu cầu | Điểm tham chiếu |
|---|---|
| Route, ga, Ga NL | Rules.Route, accountRoute, stationLabel, upstream, adjacent |
| Thuật toán đoạn sáng | travelState, trackSegments, compactJourneys |
| Ghép lượt mới/cũ/thiếu báo | linkPredecessor, movementJourney, promoteMovement, reconcileTraversal, linkEarlierDownstream |
| Kết thúc đúng lượt | endsTrain và nhánh selfTriggered/stalled trong travelState |
| Đối chiếu tự kích | EvidenceVisit.supports, suspectedSelfTriggers, confirmedSelfTriggers |
| Backfill tự kích | historicalSelfTriggers + Store.saveHistoricalDay |
| Trục | compareAxles, axleErrors, axleAlerts, dailyRecords, isAxleExempt |
| Khôi phục | faultKey, activeFaults, incidentHistory, deviceGroup |
| Ngày/retention/baseline | historyCutoff, monitoringDay, faultBaseline, needsNewBaseline |
| Nối website | WebSource.login/selectDay/readDelta/readSnapshot/validateReport/parseRoute/parse |
| Cảnh báo đúng đợt | MonitorService.playAlerts/onStartCommand + VoiceAlarm generation/retain |
| Giữ trạng thái trước prune | Store.maintainHistory/updateTravelState |

### 24.3. Bộ kiểm thử đã có

Trong `tests/`:

- RulesTest.java: **772 tình huống nghiệp vụ** theo kết quả đã ghi cho nguồn 0.27.
- ParserTest.java: **16 kiểm tra parser**.
- WebSourceReadTest.java: **39 kiểm tra đọc HTTP xác thực/phân trang với fixture**.
- AlertOptionsTest.java: **17 kiểm tra chế độ/nội dung đọc**.

Các bộ này là bằng chứng tham chiếu của nguồn Android, không tự xác nhận bản port web đã đạt. Nếu đổi ngôn ngữ, chuyển fixture/assertion sang bộ test bản port hoặc chạy core chuẩn song song và so kết quả. Không chỉ chuyển 3 ví dụ dễ rồi bỏ các nhánh còn lại.

Trong Android instrumentation có RouteStoreChecks, HistoryUiChecks, UiRefreshChecks, VoiceChecks, UpgradeChecks, TelemetryUiChecks, TabSwitchChecks. Dùng để hiểu các yêu cầu cache/UI/âm thanh; không bắt web phải chạy Android instrumentation, nhưng test web phải kiểm tra hành vi tương đương.

### 24.4. Tài liệu lịch sử và cách đọc đúng

Đọc sau đặc tả này:

- `docs/LICH_SU_TRAO_DOI.md`: diễn biến/lý do quyết định, không phải raw transcript.
- `docs/BAN_GIAO.md`: bối cảnh và các lần chuyển phiên.
- `docs/UI_REVIEW_0.27.3.md`: reconnect và bố cục cuối.
- `docs/UI_REVIEW_0.27.2.md`: không replay, XÁC NHẬN.
- `docs/UI_REVIEW_0.27.1.md`: nền tảng hành động dừng thông báo, nhãn modes.
- `docs/UI_REVIEW_0.27.md`: voice/power rules.
- `docs/UI_REVIEW_0.26.md`: chuỗi cùng hướng, lượt treo, 30 giây.
- `docs/UI_REVIEW_0.25.md`: cold/warm lịch sử và cảm biến.
- `docs/UI_REVIEW_0.24.md`: định dạng ngày và 30 ngày.
- `docs/FIX_DIAGRAM_AFTER_0.22.md`: cùng đoạn/hướng và giữ following trains.
- `docs/UI_REVIEW_0.22.md`, `0.21`, `0.20`, `0.19`, `0.18`, `0.17`, `0.16`, `0.15`, `0.14`, `0.12`: nguyên nhân các ràng buộc và mẫu giao diện.

Ảnh cũ chỉ minh họa phong cách/chức năng của thời điểm chụp. Nếu ảnh còn nút 2/5 phút, 15 ngày, nút Thử chuông hoặc thiếu ô reconnect, phải theo quyết định cuối trong phần 21 thay vì copy ảnh nguyên xi.

### 24.5. Không dựa vào việc “tôi đã gửi APK trước” để đoán

APK khó đọc lại thuật toán, và chatbot mới không tự có lịch sử chat của người khác. Gói này đưa đặc tả độc lập, mã nguồn tham chiếu cố định, bộ kiểm thử và tài liệu quyết định. Nếu cần thêm dữ liệu thực để kiểm tra parser, dùng kênh bí mật phù hợp và báo rõ mục tiêu kiểm tra; không yêu cầu gửi lại toàn bộ lịch sử hoặc khóa APK.

### 24.6. Trình tự thực hiện khuyến nghị cho người viết mới

1. Xác nhận đã đọc đặc tả, liệt kê map tính năng → module sẽ làm, không chỉ một kế hoạch UI.
2. Dựng adapter nguồn và kiểm thử HTML/login/date/pagination trước.
3. Giữ hoặc port core rules, chạy fixture/so kết quả.
4. Làm DB/checkpoint/retention/idempotency/account isolation.
5. Làm worker 30/120, lỗi xác thực, stale và batch alerts.
6. Dựng giao diện theo đúng wireframe và các màn con, giữ state/date/scope.
7. Nối Push/âm thanh iPhone theo khả năng nền tảng thật.
8. Chạy ma trận, triển khai staging HTTPS, đối chiếu nguồn thật.
9. Thử iPhone vật lý và ghi rõ hạn chế còn lại.
10. Bàn giao URL/mã/ảnh/kết quả kiểm thử và vận hành. Không đánh dấu xong những mục chưa được kiểm chứng.

---

## 25. Prompt giao việc nguyên khối cho AI Studio

Chủ sở hữu có thể gửi nguyên đoạn sau cùng file/gói này:

> Tôi muốn bạn viết **một ứng dụng web mới** để Giám sát Đường ngang, dựa chính xác trên bản Android 0.27.3 đang dùng. Hãy đọc toàn bộ BAN_GIAO_DAY_DU_VIET_LAI_WEB_0.27.3 và mã/bộ kiểm thử trong gói tham chiếu trước khi triển khai. Đây là đặc tả chính, ưu tiên hơn các ghi chú cũ đã bị thay thế. Không sửa app Android/APK. Không thiết kế lại giao diện và không dùng bản demo/data giả để thay sản phẩm thật.
>
> Phải giữ đầy đủ quy tắc đường ngang/cảm biến, hướng chẵn-lẻ, ga và Ga NL theo account Chosy, ghép nhiều lượt tàu/thiếu bản tin/giữ tàu không timeout, xử lý đúng lượt treo không xóa nhầm tàu khác, Nghi tự kích dùng bằng chứng đúng lượt một lần, lỗi và khôi phục đúng thiết bị, đếm trục/ngưỡng/6 ngoại lệ/Đếm trục lỗi, lịch sử 30 ngày/checkpoint trước prune và không phát âm lịch sử. Phần kết nối phải thực hiện thật ASP.NET login/cookie/hidden fields/date/Tất cả/full inventory/pagination/anchor/delta/full refresh với website chủ, không bịa API JSON của Visitec.
>
> Bốn tab và bố cục giống đặc tả. Ô kết nối nhỏ luôn nằm giữa Có sự cố và sơ đồ, mất kết nối mới có retry đỏ và Mất kết nối. Backend đọc 30 giây, lỗi mạng chờ 120 giây kể từ kết thúc lỗi, retry tay không song song. XÁC NHẬN chỉ dừng batch hiện tại, giữ mode/lỗi/theo dõi và để lỗi mới báo. Không replay lỗi cũ khi mở lại/restart. Không yêu cầu nhập lại cấu hình khi chưa xác định nguyên nhân lỗi mạng.
>
> Triển khai backend và DB bền vững lên hạ tầng Google Cloud phù hợp, trả URL HTTPS thật. Worker phải chạy khi không mở trình duyệt. PWA/Push iPhone phải làm theo khả năng iOS thật; không hứa timer/TTS/rung/action màn hình khóa giống Android khi chưa thử. Nếu có phiên media nền phải thử thật và nói rõ giới hạn. Dùng ma trận nghiệm thu trong tài liệu để báo từng mục đạt/chưa đạt/chưa thử, kèm ảnh, commit, kết quả test và đối chiếu website thật. Chỉ báo hoàn thành những gì đã chứng minh hoạt động.

**Kết thúc đặc tả chính.** Các file dưới `reference/` là nguồn đối chiếu và lịch sử, không thay các quyết định cuối ở trên. Không có mật khẩu/khóa ký riêng/dữ liệu vận hành xác thực trong gói bàn giao này.
