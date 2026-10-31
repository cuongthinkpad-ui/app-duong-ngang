# Bàn giao phiên làm việc — Đường Ngang 0.23

Ngày 31/10/2026. Đây là bản bàn giao kỹ thuật để tiếp tục ở tài khoản Codex/ChatGPT khác, không phải bản xuất nguyên văn cuộc trò chuyện hoặc bản sao môi trường cloud. Tài khoản mới không tự nhận lịch sử chat, bộ nhớ, tiến trình hay quyền GitHub của tài khoản cũ.

## Điểm tiếp tục

- Repository: `cuongthinkpad-ui/app-duong-ngang`, nhánh `main`.
- Commit mã/APK 0.23: `7b2439e` (các commit tài liệu sau đó không thay APK này).
- Phiên bản: **0.23.0**, versionCode **23**, applicationId `vn.duongngang.monitor`.
- APK: [DuongNgang-0.23.apk](../downloads/DuongNgang-0.23.apk). SHA-256: `a70702fa555c62e0fa9d5c7584b6277d89b6ef6a3bdfdcda1d9c5bf1d1e07373`.
- Đã đẩy mã và APK lên GitHub, tải lại APK từ GitHub và xác minh checksum đúng. Người dùng không tải được liên kết workspace; bàn giao APK qua GitHub bằng **Download raw file**.
- APK ký đúng chứng chỉ các bản trước: SHA-256 `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44`. Chưa nhận phản hồi xác nhận cài bản 0.23 trên điện thoại.

## Yêu cầu chủ dự án đã xác nhận trong phiên

1. Tàu cũ có thể treo trên sơ đồ khi đường ngang mất kết nối và thiếu bản tin. Khi tàu thực tế mới vào đúng đoạn liên quan, bỏ dấu vết cũ và nhận lượt mới ngay, cho cả tàu chẵn và tàu lẻ; không chờ bản tin đã qua/khôi phục.
2. Tàu thật vẫn hiện và chuyển tiếp đúng lộ trình theo các bản tin, không bị xóa cùng dấu vết cũ. Giữ lượt theo sau bắt đầu sau thời điểm đến của lượt đang ghép và tàu ở các đoạn độc lập.
3. Nút bấm, nâng/hạ cần và khôi phục không tự là lượt tàu mới. Hoạt động chỉ có thể đối chiếu với nguồn tàu có sẵn theo ràng buộc; bản tin tàu thật phải đối chiếu lại nếu trước đó hoạt động ghép nhầm nguồn cũ.
4. Khi chưa có bằng chứng mới, giữ trạng thái cuối, đoạn tàu xanh và cảnh báo dữ liệu cũ/thời điểm cập nhật; bỏ LIVE khi stale. Không dùng timeout để tự xóa tàu. Lỗi mất kết nối/thiết bị gốc vẫn riêng, không tự được chữa bởi việc sơ đồ hết treo.
5. Không thêm ngoại lệ mất kết nối cho suy luận nghi tự kích. Giữ `selfEvidenceVersion=4`, `axleVersion=3`, các ngoại lệ trục, giao diện, âm báo và lưu 15 ngày đã chốt trước đó.
6. Chủ dự án đã cung cấp lại hai tệp sao lưu khóa và đồng ý dùng khóa đó cho các lần sửa/ký tiếp theo, không hỏi lại khi khóa còn dùng được. Đã khôi phục và xác minh trực tiếp chứng chỉ. Khóa không nằm trong Git; môi trường mới cần người dùng cung cấp lại bản sao riêng.

## Những gì đã sửa và kiểm tra

Đọc [biên bản 0.23](FIX_DIAGRAM_AFTER_0.22.md), [AGENTS.md](../AGENTS.md), [bàn giao chung](BAN_GIAO.md) và [lịch sử yêu cầu](LICH_SU_TRAO_DOI.md).

- Ghép bản tin tàu thật vào nguồn mới nhất đủ điều kiện ở đường ngang liền trước, khép dấu vết cũ chưa ghép trên cùng đoạn/hướng. Không dùng ghép nguồn cũ nhất như 0.22.
- Sửa đối chiếu khi bản tin đã qua đến sau lúc điểm kế tiếp đã nhận tàu; sửa chu kỳ hoạt động/khôi phục rồi mới có bản tin tiếp cận thật.
- `travelVersion=5` phát lại raw còn lưu, bảo toàn seed checkpoint có nguồn đã quá hạn 15 ngày. Không dựng lại từ kho raw đã bị dọn mất dữ liệu.
- **629 ca quy tắc** đạt. Ca thay dấu vết ngay thất bại trên HEAD 0.22 trước sửa.
- Đã đọc 1.998 bản tin ngày 31/10 qua WebSource. **602 kiểm tra phát lại** đạt: từng giây 16:40:00–16:44:59 ở chế độ raw/có suy luận và chia checkpoint. Tàu lẻ không còn thành hai đoạn, tàu chẵn ở phía khác độc lập; lượt kết thúc theo khôi phục đoạn giáp ga. Dữ liệu xác thực/công cụ đọc nằm ngoài Git.
- **12 parser**, **15 fixture đọc xác thực** đạt; build debug/test APK và lint đạt, **0 lỗi/15 cảnh báo**.
- **Chưa chạy Android instrumentation, UI/service hoặc thử Xiaomi** ở môi trường mới. Ba ca nâng cache 4 → 5 đã thêm và biên dịch vào test APK nhưng chưa thực thi; runner dự kiến 174 ca storage. Không biến các kết quả giả lập cũ trong docs thành kết quả của bản mới.

## Dựng lại môi trường

Mỗi cloud task đã được cô lập, dùng checkout hiện có; không tạo worktree trừ khi chủ dự án yêu cầu. Checkout cloud dự kiến `/workspace/app-duong-ngang`, toolchain `/workspace/toolchains`. Repo có `scripts/setup-cloud.sh`, `scripts/env.sh` và `scripts/build-cloud.sh`; không có Gradle wrapper. Dùng JDK 21, Gradle 8.9, AGP 8.7.3, SDK 35. Setup mới cần tải toolchain/dependencies; chỉ dùng offline khi đã có cache. Giữ proxy/CA của nền tảng, không tắt TLS/checksum verification.

```sh
cd /workspace/app-duong-ngang
# Java SDK tools cần explicit proxy và trust store trên môi trường cloud loại này.
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 -Dhttp.proxyHost=proxy -Dhttp.proxyPort=8080 -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
bash scripts/setup-cloud.sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh --offline assembleDebug assembleDebugAndroidTest lintDebug
```

Kiểm tra chính sách proxy/trust store thực tế ở môi trường mới trước khi dùng các đường dẫn cloud trên. Các script có đường dẫn cloud cố định; môi trường khác cần đường dẫn/toolchain phù hợp. Bài parser mở HTTP fixture cục bộ, cần quyền local socket. Muốn chạy Android tests phải có thiết bị kiểm thử/giả lập phù hợp; không tự dùng máy vận hành.

## Khóa ký và tài khoản — giữ riêng ngoài GitHub

Người dùng cần giữ riêng **cả hai tệp** `DuongNgang-signing-backup.enc.txt` và `backup-password.txt`. Không đưa bất kỳ tệp nào trong hai tệp này vào repository. Mất cả khóa lẫn bản sao ngoài tài khoản cũ sẽ không thể tạo APK cùng chữ ký chỉ từ mã nguồn/APK công khai.

Hướng dẫn khôi phục: [SAO_LUU_KHOA_KY.md](SAO_LUU_KHOA_KY.md); công cụ `scripts/restore-signing-key.py` kiểm tra xác thực và không ghi đè khóa đã có. Môi trường phiên này giữ bản sao riêng tại `/workspace/private-backups/duong-ngang-signing/`, keystore/metadata tại `restored/`; không giả định những tệp đó có ở tài khoản mới.

Build cloud tự nhận metadata ký riêng ở vị trí đó, hoặc nhận đường dẫn khác qua `DUONG_NGANG_SIGNING_INFO`. Không có metadata thì build vẫn có thể sinh khóa debug mới; **phải kiểm tra chứng chỉ APK trước khi phát hành cài đè**, không chỉ thấy build thành công. Không đưa giá trị mật khẩu vào log, lệnh, mã nguồn hay Git.

Website vận hành cần đăng nhập, không lưu thông tin đăng nhập trong tài liệu. Khi cần đọc web thật ở phiên mới, người dùng cung cấp lại quyền truy cập riêng; không yêu cầu token GitHub chỉ vì thiếu biến token nếu HTTPS Git proxy của nền tảng đã hoạt động.

## Tin nhắn có thể dán vào tài khoản mới

> Tiếp tục app Đường Ngang tại https://github.com/cuongthinkpad-ui/app-duong-ngang, nhánh main. Đọc AGENTS.md, docs/TIEP_TUC_SAU_0.23.md, docs/BAN_GIAO.md và docs/LICH_SU_TRAO_DOI.md trước. Điểm bàn giao là APK 0.23.0/versionCode 23, commit 7b2439e. Giữ các yêu cầu đã chốt, phân biệt kiểm thử đã chạy với instrumentation/Xiaomi chưa chạy. Khóa ký và thông tin website được cung cấp riêng khi cần. Việc tiếp theo tôi muốn làm là: [ghi yêu cầu].

Người dùng có thể tải thêm **Code → Download ZIP** để giữ mã nguồn và các tài liệu bàn giao ngoài tài khoản Codex. Đây không thay thế bản sao riêng của hai tệp khóa ký, và không khôi phục nguyên trạng cuộc trò chuyện/cloud snapshot.
