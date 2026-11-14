# Hợp đồng tích hợp AI Studio — phiên bản 1

Đây là API nội bộ của web mới, không phải API của Visitec. Cookie `rail_session` HttpOnly/SameSite=Strict; production Secure. Gọi cùng origin qua HTTPS, không chuyển mật khẩu/token vào URL/log. Body JSON, lỗi `{error: "nội dung"}` với HTTP 400/403/404/409/429/503. Mutation bắt buộc `X-CSRF-Token` lấy ở session; kiểm tra Origin. Không cache `/api/` tại CDN/Service Worker.

| API | Đầu vào | Kết quả/ý nghĩa |
|---|---|---|
| GET /api/session | Cookie nếu có | csrf, vapidPublicKey, configured; tạo phiên riêng nếu chưa có |
| POST /api/config | url,user,password,mode,rangeFrom,rangeTo,monitorDate | `{ok:true}` sau đọc nguồn hợp lệ; password rỗng giữ cũ chỉ khi URL/ID không đổi; chạy theo dõi |
| GET /api/state | day=YYYY-MM-DD, crossing=khóa điểm, since=seq tùy chọn | Snapshot dưới đây; bỏ since khi mở lại để không replay |
| POST /api/options | running boolean, mode voice/bell/silent, rangeFrom,rangeTo,monitorDate | Lưu tùy chọn đã kiểm tra; không đổi nguyên văn lỗi |
| POST /api/retry | `{}` | Cho lịch hiện tại đến hạn ngay; worker vẫn serialize/lease theo account |
| POST /api/history | day | Tải đủ ngày + bằng chứng ngày trước, không cảnh báo lịch sử |
| POST /api/confirm | `{}` | Chốt confirmedThrough ở seq hiện tại, kết thúc batch; giữ faults/mode/running; lỗi mới seq lớn vẫn báo |
| POST /api/subscribe | endpoint, keys:{p256dh,auth} | Lưu subscription mã hóa; chỉ endpoint provider cho phép |
| POST /api/unsubscribe | `{}` | Hủy subscription của phiên |
| POST /api/logout | `{}` | Xóa phiên/subscription này, không xóa dữ liệu account người khác |
| GET /health | Không cần account | 200 `{ok:true}` khi DB + engine + worker/push worker hoạt động, ngược lại 503 |

`state`: configured, options, settings:{url,user} (không password), route, points:{key,label,station}[], faults, incidents:{fault,recovery}[], records, devices, groups, axles, segments, occupancies, trains, announcements, lastSuccess, lastAttempt, loading, connectionLost, status, selectedDay, historyReady, historical, pushEnabled, serverTime, seq, alerts. Core event: `{id,at,crossing,text,red,counts}`; `at` mili giây UTC, ngày nghiệp vụ Asia/Ho_Chi_Minh. `axles[]` có `{source:Event,sensor,standard,observed,delta,dailyCount,missing,extra}`. Không đọc crossing trực tiếp trên AxleError. Checkpoint/travel/pending nội bộ không gửi ra frontend.

`route/points/segments` giữ toàn tuyến, faults/records/trains/axles/incidents lọc scope. Ga không mở thiết bị. Chỉ `historyReady`/snapshot đầy đủ mới cho hiển thị Tốt. LIVE cần đang chạy, không lịch sử, không mất kết nối và lastSuccess ≤60 giây. Frontend đọc cache mỗi 5 giây không phải lịch lấy nguồn 10 giây; không phụ thuộc số tab/browser mở.

## Điểm nối thông báo

`App.announce(mid,state,events)` chỉ nhận lỗi mới đủ điều kiện từ poll thành công. Nó lọc session running/ngày/scope và insert `announced(session,event)` duy nhất, rồi tạo `notices(seq,session,monitor,payload,created,sent,attempts,next_due)`. Đây là outbox bền vững để nối provider; không enqueue lại mọi faults trong snapshot/restart. Session/account phải cách ly.

`App.deliver()` là adapter Push tham khảo (pywebpush), kiểm tra lỗi còn mở, running/scope/ngày trước gửi; 404/410 xóa subscription. Payload `{seq,title,body,silent,tag}`; tag `rail-<seq>`. Service Worker `public/sw.js` hiển thị và mở Sự cố khi chạm, không TTS/timer nguồn. Provider có thể giao lặp khi mất ACK: giữ seq/tag để idempotent phía nhận. Bên tích hợp cần lease outbox nếu chạy nhiều worker, ghi lỗi sạch và thử provider thực; chưa coi mock là đã có Apple Push.

Foreground: `Alarm.enqueue(alerts, announcements)` đọc lỗi mới từ state?since; `Alarm.stop()` hủy generation/queue/player/TTS/rung ngay. XÁC NHẬN gọi stop tại client và POST confirm để chốt seq server. Im lặng vẫn lưu/hiển thị lỗi và gửi Push silent. Không phát lại khi mở lại, không phát lịch sử, không dùng callback cũ hủy đợt mới. Sau recovery phải loại alert đã hết khỏi queue. Không nối lại `/api/audio/stream`: endpoint đã bỏ, trả 404.

## Cấu hình máy chủ và trách nhiệm

AI Studio cung cấp PUBLIC_ORIGIN HTTPS, DATABASE_URL PostgreSQL, APP_KEY base64url 32 bytes, VAPID_PRIVATE_KEY PEM P-256, VAPID_SUBJECT mailto liên hệ thật, WEBSITE_HOSTS allowlist (mặc định visitec.vn), DATA_DIR thư mục runtime riêng, PORT/BIND. Không ghi giá trị secret vào repository/ZIP. Khóa và DB phải giữ qua redeploy, backup đi cùng khóa. PUBLIC_ORIGIN bật kiểm tra bắt buộc DB/khóa. SQLite chỉ local.

Lịch: thành công xấp xỉ 10 giây từ bắt đầu trước, tác vụ chậm thì không bù/chồng; network lỗi +50 giây từ kết thúc; auth dừng phiên; full refresh 900000 ms; self-confirm delay 10000 ms; stale 60000 ms. Giữ lease/lock khi nâng số replica. Retention checkpoint và prune trong transaction. Scheduler sống khi không có browser; cần CPU luôn cấp và min instance hoặc worker service tương đương. Thực hiện deploy/backup/load/restart/Push/iPhone tests trước nghiệm thu.

Biến thể core web và migration webRulesVersion=2: xem LATEST_REQUIREMENTS_1.2.0.md; loại ±1/±2 từng CB trước tính thống kê/ghi chú; không thay raw. historyReady là điều kiện Tốt. SpeechSynthesis foreground không thay cho kiểm chứng giọng nói khóa iPhone.
