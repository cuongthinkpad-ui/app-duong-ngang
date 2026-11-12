# Cloud Run — triển khai toàn bộ backend

Thực hiện trong Google Cloud Shell / AI Studio có quyền dự án của chủ sở hữu. Không có lệnh nào trong gói này đã được dùng để tạo tài nguyên tính phí. Giá Cloud Run always-on + Cloud SQL phụ thuộc khu vực/gói.

## 1. Tài nguyên bền vững

Chọn PROJECT_ID, REGION (ví dụ asia-southeast1), SERVICE (ví dụ duong-ngang-web), một Cloud SQL PostgreSQL 16 có database riêng `railweb`, user riêng và backup tự động. Bật API Cloud Run, Cloud Build, Artifact Registry, SQL Admin và Secret Manager. Tạo service account runtime riêng có `roles/cloudsql.client`. Với từng secret, cấp service account `roles/secretmanager.secretAccessor` đúng secret đó.

Dùng `deploy/generate-secrets.py` trong môi trường đã cài requirements để tạo APP_KEY và VAPID ở thư mục riêng ngoài mã nguồn. Tạo secret `rail-app-key` và `rail-vapid-key` bằng `gcloud secrets create --data-file=...`. Lưu file riêng và Secret Manager, không gửi trong chat.

Tạo secret `rail-database-url` chứa chuỗi kết nối psycopg, ví dụ hình dạng:

```
postgresql://railuser:URL_ENCODED_PASSWORD@/railweb?host=/cloudsql/PROJECT:REGION:INSTANCE
```

Mật khẩu phải percent-encode ký tự đặc biệt. Không in chuỗi kết nối vào log. Dùng Cloud SQL Unix socket, `--add-cloudsql-instances=...` khi deploy. Database này chứa credentials website đã AES-GCM mã hóa bằng APP_KEY; không dùng database công khai/SQLite tạm. Sao lưu DB kèm APP_KEY và VAPID.

## 2. Build từ thư mục ZIP đã giải nén

```sh
gcloud builds submit --tag REGION-docker.pkg.dev/PROJECT/REPOSITORY/duong-ngang-web:1.0.0 .
```

Thay các tên PROJECT/REGION/REPOSITORY bằng tài nguyên thật. Dockerfile tự biên dịch đúng Java core bằng Java 21, cài ffmpeg/espeak-ng. Không deploy thư mục `dist/` riêng.

## 3. Deploy và đặt đúng origin

Lần đầu triển khai riêng tư, đặt PUBLIC_ORIGIN tạm `https://pending.invalid`, sau đó lấy URL thật và cập nhật trước khi cấp truy cập công khai. Điều này tránh đoán URL Cloud Run. VAPID_SUBJECT phải là email quản trị thật dạng `mailto:...`.

```sh
gcloud run deploy SERVICE --image REGION-docker.pkg.dev/PROJECT/REPOSITORY/duong-ngang-web:1.0.0 --region REGION --service-account RUNTIME_SERVICE_ACCOUNT --no-allow-unauthenticated --min-instances 1 --max-instances 1 --no-cpu-throttling --timeout 3600 --concurrency 40 --cpu 2 --memory 2Gi --add-cloudsql-instances PROJECT:REGION:INSTANCE --set-secrets APP_KEY=rail-app-key:1,VAPID_PRIVATE_KEY=rail-vapid-key:1,DATABASE_URL=rail-database-url:1 --set-env-vars PUBLIC_ORIGIN=https://pending.invalid,VAPID_SUBJECT=mailto:ADMIN_EMAIL,WEBSITE_HOSTS=visitec.vn
```

Lấy `status.url` bằng `gcloud run services describe SERVICE --region REGION --format='value(status.url)'`. Cập nhật biến bằng `gcloud run services update SERVICE --region REGION --update-env-vars PUBLIC_ORIGIN=ACTUAL_HTTPS_ORIGIN`. Sau khi kiểm tra cấu hình và `/health`, cấp invoker public theo chính sách dự án. Không thay các secret phiên bản gốc khi chỉ cập nhật code.

Không dùng request-based CPU hoặc min=0: worker sẽ bị đóng băng khi người dùng không mở trang. Lease database tránh đọc song song qua thời gian chuyển revision; max=1 giới hạn chi phí/nhân bản. Theo dõi `/health`; nền đọc lỗi giữ cache, không ghi thông tin đăng nhập/HTML vào log. Cần theo dõi lỗi worker trong vận hành và backup Cloud SQL.

## 4. Kiểm tra ngoài cloud

Kiểm tra backend truy cập visitec.vn:7447 trước; ở môi trường tạo mã hiện tại domain bị chặn nên chưa xác minh website thật. Không thay ID/mật khẩu chỉ để chữa TCP timeout. Lưu tài khoản qua HTTPS UI, chờ ít nhất hai lần đọc thật; thực hiện toàn bộ `docs/ACCEPTANCE.md` trên iPhone.

Web Push dùng endpoint Apple/Google/Mozilla/Microsoft. Cho phép HTTPS outbound đến các endpoint tương ứng. PWA cập nhật code giữ domain/VAPID/DB, không `/reset` tự động. Phiên MP3 request tối đa 55 phút; Cloud Run timeout 3600 là cấu hình bắt buộc cho phiên này, không bảo đảm iOS sẽ giữ phiên xuyên đêm.
