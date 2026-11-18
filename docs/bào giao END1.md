# bào giao END1

Bản bàn giao Android **Giám sát Đường ngang** để chủ dự án hoặc trợ lý ở tài khoản khác có thể lấy đúng mã, hiểu các quyết định nghiệp vụ, chạy kiểm thử và phát hành tiếp. Mốc phần mềm của bàn giao này là **0.27.5**.

## 1. Lấy đúng mã và APK

| Nội dung | Giá trị |
| --- | --- |
| Repository | `cuongthinkpad-ui/app-duong-ngang` |
| Nhánh Android cần tiếp tục | `codex/android-0.27.5-recovery` |
| Commit chốt mã, APK và kiểm chứng 0.27.5 | `b971cbd63ef20244fdf0c6cdb9204c67b4496f2a` |
| Nền của bản cập nhật | Android 0.27.4, `1f0ef05d432bdf3e0afdeacea35604ca796e527b`, nhánh `codex/android-0.27.3-ui` |
| Ứng dụng | `vn.duongngang.monitor`, versionName `0.27.5`, versionCode `32` |
| Nền tảng | Android native Java, Java source/target 17, min SDK 26, target SDK 34, compile SDK 35 |
| APK bản bàn giao | `downloads/DuongNgang-0.27.5.apk` |
| SHA-256 APK | `4650f72d81808a53e092549bc0c085016117add882cd89a8e53b741c5422e432` |
| SHA-256 chứng chỉ ký gốc | `281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44` |

[Tải APK 0.27.5](../downloads/DuongNgang-0.27.5.apk) · [Danh sách checksum](../downloads/SHA256SUMS) · [Commit đã kiểm chứng](https://github.com/cuongthinkpad-ui/app-duong-ngang/tree/b971cbd63ef20244fdf0c6cdb9204c67b4496f2a)

Khi đổi tài khoản, dùng một tài khoản GitHub có quyền đọc repository và quyền đẩy nhánh nếu sẽ phát hành. Trong Codex Cloud, chọn repository và dùng checkout sẵn có. Với máy chưa có mã, clone nhánh Android bằng lệnh sau.

```bash
git clone --branch codex/android-0.27.5-recovery https://github.com/cuongthinkpad-ui/app-duong-ngang.git
cd app-duong-ngang
git branch --show-current
git status --short --branch
git log -3 --oneline
git merge-base --is-ancestor b971cbd63ef20244fdf0c6cdb9204c67b4496f2a HEAD
```

Các commit tài liệu sau `b971cbd` vẫn thuộc nhánh Android. Khi cần đối chiếu đúng mã đã tạo APK 0.27.5, đọc commit cố định ở bảng trên. Nếu checkout đang có thay đổi, kiểm tra diff và giữ thay đổi trước khi chuyển nhánh. `main` vẫn là nền Android 0.26 kèm các commit tài liệu minh họa. `codex/iphone-web` là sản phẩm web riêng. Thông số web 10/10/50 giây thuộc sản phẩm đó.

## 2. Nghiệp vụ đã chốt

### Khôi phục sự cố trên Android 0.27.5

- Cần 1 và cần 2 theo dõi độc lập. Một bản tin **đã hạ** hoặc **đã nâng** đủ để khép lỗi **mất trạng thái** của chính cần đó tại cùng điểm, với giờ bản tin thành công lớn hơn giờ lỗi. Cùng giây chưa đủ để kết luận thứ tự. Lượt tàu sau vẫn có thể cung cấp bằng chứng hợp lệ. Lỗi khác của cùng cần, cần còn lại và thiết bị khác giữ trạng thái riêng. Mất trạng thái sau lần khôi phục mở sự cố mới.
- Tại Km 332+350 ngày 10/11/2026, cần 1 và cần 2 cùng mất trạng thái lúc 20:16:20. Cần 1 đã hạ lúc 20:16:25 khép đúng lỗi cần 1 lúc 20:16:25. Cần 2 giữ trạng thái mở cho đến khi có bằng chứng thành công của cần 2.
- Cùng đường ngang, một **PLC khôi phục tốt** khép mọi ghi nhận **PLC hỏng** còn mở trước đó. Ca Km 321+780 có PLC hỏng lúc 08:52:38 và 08:59:51. Sau PLC khôi phục tốt lúc 09:00:09, cả hai ghi nhận lịch sử liên kết với bản tin phục hồi và trạng thái hiện hành hết lỗi PLC. Bản tin hỏng mới sau đó vẫn mở. Mỗi điểm giữ PLC riêng.
- Nguyên nhân đã xác minh trong mã cũ là hai chuỗi PLC sinh hai khóa khác nhau, còn nhánh ghép lỗi không xét hoạt động cần chắn làm bằng chứng. Ngoài ra, lỗi cùng một cần trước đây có thể dùng chung khóa và bộ đọc lịch sử bỏ qua bản tin hạ/nâng ở khoảng đọc sau. Quy tắc mới dùng đúng danh tính điểm, cần và loại lỗi. Lịch sử giữ từng bản tin lỗi và bản tin khôi phục gốc.
- Tổng quan, danh sách đường ngang, Sự cố và chi tiết thiết bị dùng chung cách xác định lỗi. Chi tiết có dòng PLC. Lịch sử chọn lỗi phát sinh trong ngày đang xem và tìm bằng chứng cần chắn/PLC trong dữ liệu cục bộ cả sau ngày đó. Dữ liệu đã qua hạn lưu hoặc chưa tải sẽ thiếu bằng chứng để ghép.

### Hành vi kế thừa cần giữ

- Android đọc bình thường **30 giây**, thử lại **120 giây** sau lỗi kết nối, timeout đọc web **25 giây**. Nút **Kết nối lại** thử ngay khi mất kết nối. Giữ dữ liệu đã đọc trong lúc lỗi kết nối và hiển thị trạng thái cũ. Kết nối thành công đưa dịch vụ về nhịp 30 giây.
- **XÁC NHẬN** trên thông báo hoặc Tổng quan dừng đợt cảnh báo đang phát, giữ sự cố và cấu hình âm. Chế độ **Giọng nói** dùng giọng Việt nếu có, hai hồi chuông rồi đọc cảnh báo sáu lượt kèm rung. **Chỉ chuông** dùng sáu hồi và rung. **Im lặng** giữ thông báo. Xem lịch sử và dựng lại cache cũ phải im lặng.
- Chấm điểm đỏ khi chính điểm có sự cố mở. Điểm hết lỗi trở về màu sơ đồ. Đoạn tàu, mũi tên và nhãn hướng giữ xanh khi dữ liệu cũ, ga giữ màu vàng. Danh sách điểm báo số lỗi, **Tốt** sau khi đã tải đủ dữ liệu hoặc **Chưa tải đủ dữ liệu**.
- Theo dõi và lưu 30 ngày theo múi giờ Việt Nam, tính cả hôm nay. Trước khi dọn bản tin cũ phải giữ checkpoint tàu. Giữ đúng phân trang, biên ngày, tài khoản, toàn tuyến và phạm vi lọc. Bổ sung Ga NL tại Km 303+350 chỉ cho cấu hình tài khoản đã được chủ dự án duyệt, theo điều kiện trong `AGENTS.md`.
- Giữ thuật toán ghép tàu theo hướng và khu đoạn giới hạn bởi ga. Thao tác bảo dưỡng lẻ không tạo tàu. Giải phóng lượt treo đúng tàu, giữ tàu nối tiếp và các lỗi thiết bị gốc. Giữ ngoại lệ đếm trục tại Km 280+500, 307+507, 309+056, 320+445, 338+900 và 343+350. Các ngoại lệ áp dụng cho kết quả suy ra từ số trục, còn bản tin gốc và theo dõi tàu vẫn được giữ.

## 3. Bản đồ mã và dữ liệu

Các lớp Android chính nằm tại `app/src/main/java/vn/duongngang/monitor/`.

| Tệp | Khi nào cần đọc |
| --- | --- |
| `Rules.java` | Quy tắc ghép lỗi và khôi phục, trạng thái hoạt động và lịch sử, lịch tàu, nghi tự kích, đếm trục và định danh điểm |
| `Store.java` | SQLite `events.db`, tài khoản, Android Keystore, raw, lịch sử, dựng cache lỗi và checkpoint tàu |
| `MainActivity.java` | Tổng quan, danh sách điểm, Sự cố, chi tiết thiết bị, ngày lịch sử, trạng thái kết nối |
| `WebSource.java` | Đăng nhập, đọc HTML, múi giờ và ngày, phân trang, định danh bản tin |
| `MonitorService.java` | Đọc nền, nhận dữ liệu mới, thử lại, cập nhật cache, phát hoặc dừng cảnh báo |
| `AlarmSound.java`, `AlertOptions.java`, `VoiceAlarm.java` | Chuông, rung, chế độ âm, phát giọng Việt và hủy cảnh báo |
| `tests/`, `app/src/androidTest/`, `scripts/test-*` | Kiểm thử Java, fixture HTTP và kiểm thử Android |

Đánh dấu cache đang dùng gồm `faultRulesVersion=2`, `travelVersion=6`, `selfEvidenceVersion=5` và `axleVersion=3`. Migration lỗi phiên bản 2 đọc lại raw cục bộ trong mốc hiển thị hiện hành. Việc sửa cache phải giữ lịch sử, tài khoản, checkpoint và dấu mốc cảnh báo cũ. Khi đổi bộ quy tắc, kiểm tra cả dữ liệu đã lưu, lần mở lại app, bản tin đến trễ và trường hợp lịch sử vượt nửa đêm. Giữ ranh giới giữa bản tin raw và trạng thái suy ra.

## 4. Dựng môi trường và chạy kiểm thử

Toolchain cloud chuẩn là **JDK 21.0.8**, **Gradle 8.9**, Android SDK 35, build-tools 34.0.0 và 35.0.0. Các script cloud dùng đường dẫn `/workspace/app-duong-ngang` và công cụ dưới `/workspace/toolchains`. Ngoài Codex Cloud, cài cùng phiên bản vào đường dẫn phù hợp rồi cung cấp `JAVA_HOME`, `ANDROID_HOME`, Gradle và ADB qua môi trường của máy. Các script có `source scripts/env.sh` dùng đường dẫn cloud cố định.

```bash
cd /workspace/app-duong-ngang
./scripts/setup-cloud.sh
source scripts/env.sh
./scripts/test-rules.sh
./scripts/test-parser.sh
./scripts/build-cloud.sh --offline assembleDebug assembleDebugAndroidTest lintDebug
```

Lần cài đầu cần tải toolchain và thư viện. Sau khi đã có đủ dependency, dùng `--offline` để tái chạy build. APK ứng dụng ra tại `app/build/outputs/apk/debug/app-debug.apk`, APK instrumentation ra tại `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`. Nếu cloud cũ đã publish cấu hình, vẫn kiểm tra đúng commit, công cụ và chạy cho ra kết quả ở phiên mới. Trạng thái giả lập phải được khởi động lại và kiểm tra trong phiên hiện hành.

### Giả lập riêng

Bản 0.27.5 được xác minh trên AOSP Android 10/API 29 x86. Với cloud đã có image và AVD như phiên kiểm thử, dùng cách khởi động sau. Trên máy mới, dùng SDK Manager cài `emulator` và `system-images;android-29;default;x86`, rồi dùng `avdmanager` tạo AVD riêng cùng tên hoặc thay tên trong lệnh. Có thể dùng tăng tốc phần cứng nếu máy hỗ trợ.

```bash
source scripts/env.sh
export ANDROID_AVD_HOME=/workspace/toolchains/android-avd
"$ANDROID_HOME/emulator/emulator" -avd duongngang-aosp29 -port 5556 \
  -accel off -gpu swiftshader_indirect -feature -Vulkan \
  -no-window -no-audio -no-boot-anim -no-snapshot -cores 2 -memory 1536
```

Trong terminal khác, nạp `scripts/env.sh`, đặt `ANDROID_SERIAL=emulator-5556`, xác minh `adb shell getprop sys.boot_completed` trả `1` và `adb shell service check activity` trả trạng thái `found`. Máy giả lập phần mềm có thể mất vài phút. Cài hai APK tương thích chữ ký trên thiết bị thử. Bộ kiểm thử tự tạo dữ liệu giả lập. Ca seed dùng tài khoản giả và ghi dữ liệu, vì vậy dành riêng AVD cho ca cài đè.

```bash
export ANDROID_SERIAL=emulator-5556
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
rail_runner=vn.duongngang.monitor.test/vn.duongngang.monitor.RouteStoreChecks
adb shell am instrument -w -e mode equipment-recovery "$rail_runner"
adb shell am instrument -w -e mode fault-display "$rail_runner"
```

Xác nhận đầu ra có `5 equipment recovery storage checks passed` và `18 fault display checks passed`. Runner có thể kết thúc lệnh với mã 0 dù có chữ `FAILED`, vì vậy đọc cả kết quả. Bộ lưu trữ đầy đủ có lệnh `./scripts/test-android-route.sh`. Ưu tiên ca liên quan khi giới hạn tài nguyên giả lập.

### Kết quả của mốc 0.27.5

| Ca | Kết quả |
| --- | --- |
| Nghiệp vụ Java | 780 đạt, gồm 8 ca mới về thời điểm, số cần, vị trí, loại lỗi, lỗi lặp và tái phát |
| Parser và fixture HTTP | 16 parser và 39 ca đọc xác thực mô phỏng đạt |
| Build và lint | APK app, APK AndroidTest đạt. Lint 0 lỗi, 14 cảnh báo |
| Cache, mở lại Store và lịch sử | 5 ca đạt trong `equipment-recovery`, gồm PLC qua nửa đêm, bằng chứng hạ cần và giữ dấu mốc cảnh báo |
| Hiển thị | 18 ca đạt trong `fault-display`, gồm số lỗi, chấm đỏ và màu đoạn tàu |
| Cài đè | 0.27.4 → 0.27.5 trên AOSP API 29, giữ 6 nhóm dữ liệu thử gồm mật khẩu mã hóa, URL/ID, âm, phạm vi/chu kỳ, SQLite và tệp riêng |
| Bộ lưu trữ đầy đủ | Hết thời gian trên giả lập phần mềm trong phiên 0.27.5 |
| Xiaomi/HyperOS, điện thoại thật và Visitec thật | Chưa kiểm chứng trong phiên 0.27.5 |

[Xem biên bản gốc](UI_REVIEW_0.27.5.md)

## 5. Ký bản cài đè và bảo toàn dữ liệu

Tài khoản mới cần quyền truy cập GitHub và chủ dự án cung cấp riêng bộ sao lưu khóa khi cần ký. Bộ sao lưu gồm tệp mã hóa và tệp mật khẩu giải mã. Giữ riêng đường dẫn, mật khẩu, keystore, `signing-info.json`, thông tin đăng nhập vận hành và bản tin chụp sau xác thực. GitHub lưu APK đã ký, chứng chỉ công khai, mã, checksum và tài liệu để đối chiếu. Phiên cloud mới cần kiểm tra lại quyền truy cập kho khóa riêng. [Hướng dẫn khôi phục khóa gốc](SAO_LUU_KHOA_KY.md)

1. Dùng `scripts/restore-signing-key.py` theo tài liệu ký để khôi phục ngoài checkout. Python cần thư viện `cryptography`. Truyền đường dẫn `signing-info.json` bằng `DUONG_NGANG_SIGNING_INFO` khi build. Giữ nguyên package và tăng versionCode cho bản phát hành tiếp theo.
2. Dùng `apksigner verify --print-certs` đối chiếu APK cuối với APK gốc ở mục 1. Đối chiếu SHA-256 file bằng checksum riêng của APK và dùng `aapt dump badging` kiểm package/version. Bản build dùng khóa phát triển thường chỉ phù hợp với AVD phát triển.
3. Trên AVD sạch, cài APK đang dùng làm nền, cài AndroidTest ký tương thích, chạy runner với `-e mode upgrade-seed`. Cài đè APK mới bằng `adb install -r`, chạy `-e mode upgrade-verify`. Runner trong mốc này xác nhận nâng từ versionCode 31 lên 32. Kiểm đầu ra có `6 install-over data checks passed`.
4. Khi giao APK cho máy đang dùng, cài đè trực tiếp lên bản cũ bằng bản ký đúng chứng chỉ. Giữ nguyên app và dữ liệu trên máy. Việc chuyển tài khoản GitHub/Codex chỉ chuyển quyền làm việc với mã. Tài khoản vận hành và SQLite nằm trong ứng dụng Android trên thiết bị đang sử dụng.
5. Cập nhật APK, checksum và biên bản kiểm thử trên nhánh Android đã xác định. Ghi rõ ca đã chạy, ca thất bại, ca chưa chạy và thiết bị dùng để thử. Chỉ dùng kết quả website thật hoặc Xiaomi khi có phép đo của lần phát hành tương ứng.

## 6. Tài liệu và điểm bắt đầu cho phiên mới

- Quy tắc làm việc, bảo toàn thay đổi sẵn có, các mốc kinh nghiệm và yêu cầu chủ dự án. [AGENTS.md](../AGENTS.md)
- Bàn giao tổng quát, nguyên lý hệ thống, quy tắc kế thừa và nhật ký quyết định. Các mục theo phiên cũ có thể mô tả trạng thái trước khi đã sửa. Đối chiếu với mốc END1 và chỉ đạo mới nhất của chủ dự án. [Bàn giao tổng quát](BAN_GIAO.md) · [Nhật ký trao đổi](LICH_SU_TRAO_DOI.md)
- Mốc khôi phục kết nối, chấm sự cố và ghép lỗi thiết bị mới nhất. [Review 0.27.3](UI_REVIEW_0.27.3.md) · [Review 0.27.4](UI_REVIEW_0.27.4.md) · [Review 0.27.5](UI_REVIEW_0.27.5.md)

Có thể dán đoạn sau vào phiên ChatGPT/Codex ở tài khoản mới.

> Xưng em và gọi tôi là anh. Tiếp tục Android native **Giám sát Đường ngang** trong repository `cuongthinkpad-ui/app-duong-ngang`, nhánh `codex/android-0.27.5-recovery`. Đọc `docs/bào giao END1.md`, `AGENTS.md` và biên bản 0.27.5. Trước khi chỉnh sửa, kiểm HEAD/diff và giữ mọi thay đổi đã có. Mốc đã kiểm chứng là `b971cbd63ef20244fdf0c6cdb9204c67b4496f2a`. Bảo toàn thuật toán tàu, đếm trục, âm, raw, dữ liệu đăng nhập và mốc Android theo bàn giao. Chỉ sửa nghiệp vụ mới tôi yêu cầu. Khi build cài đè, dùng khóa ký gốc qua kênh riêng và đối chiếu đúng SHA-256 chứng chỉ. Ghi rõ các kiểm thử đã chạy cùng giới hạn giả lập và điện thoại thật.
