# Hướng dẫn Manual Testing - Create Work Order

## 1. Điều kiện local

- Java 17.
- PostgreSQL đang chạy.
- Có database local, ví dụ `add2num`.
- Port mặc định của Spring Boot: `8080`.
- Maven Wrapper có sẵn trong repository.

Ứng dụng runtime dùng PostgreSQL; H2 chỉ được khai báo cho test. Vì vậy cần
thay các giá trị kết nối bên dưới bằng thông tin PostgreSQL trên máy local.

## 2. Tài khoản test

In-memory user hiện tại:

| Thuộc tính | Giá trị |
|---|---|
| Username | `technician` |
| Password | `test-password` |
| Authority | `ROLE_TECHNICIAN` |

Đây chỉ là tài khoản local/test. Không dùng mật khẩu này ở môi trường
production.

## 3. Lệnh khởi động

Mở terminal tại thư mục repository `C:\Add2Number`.

### Nếu dùng PowerShell

Chạy lệnh sau trên **một lần gọi hoàn chỉnh**:

```powershell
.\mvnw.cmd spring-boot:run `
  "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:postgresql://localhost:5432/add2num --spring.datasource.username=postgres --spring.datasource.password=YOUR_LOCAL_POSTGRES_PASSWORD --spring.jpa.hibernate.ddl-auto=update --app.security.technician.username=technician --app.security.technician.password=test-password"
```

Trong PowerShell, dấu `` ` `` ở cuối dòng là ký hiệu nối dòng. Không thêm
dấu này khi dán lệnh vào `cmd.exe`.

### Nếu dùng Command Prompt (`cmd.exe`)

Khuyến nghị dùng biến môi trường trong phiên `cmd.exe`, sau đó chạy Maven.
Mỗi dòng dưới đây là một lệnh riêng:

```cmd
set "SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/add2num"
set "SPRING_DATASOURCE_USERNAME=postgres"
set "SPRING_DATASOURCE_PASSWORD=YOUR_LOCAL_POSTGRES_PASSWORD"
set "SPRING_JPA_HIBERNATE_DDL_AUTO=update"
set "APP_SECURITY_TECHNICIAN_USERNAME=technician"
set "APP_SECURITY_TECHNICIAN_PASSWORD=test-password"
mvnw.cmd spring-boot:run
```

Không gõ các dấu backtick `` ` `` trong `cmd.exe`. Nếu password có ký tự đặc
biệt, cách dùng biến môi trường này cũng tránh lỗi phân tích cú pháp do dấu
ngoặc hoặc dấu `&` trong command line.

Nếu muốn truyền trực tiếp bằng một dòng, đặt dấu ngoặc kép chỉ quanh **giá trị**
của property:

```cmd
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/add2num --spring.datasource.username=postgres --spring.datasource.password=YOUR_LOCAL_POSTGRES_PASSWORD --spring.jpa.hibernate.ddl-auto=update --app.security.technician.username=technician --app.security.technician.password=test-password"
```

Nếu lệnh trực tiếp vẫn báo lỗi cú pháp, dùng cách biến môi trường ở trên.

Thay các giá trị sau:

- `add2num`: tên database local.
- `postgres`: username PostgreSQL local, không phải username API.
- `YOUR_LOCAL_POSTGRES_PASSWORD`: password PostgreSQL local.

Có hai loại tài khoản độc lập:

- PostgreSQL: dùng cho `spring.datasource.username` và
  `spring.datasource.password`.
- API: dùng cho `app.security.technician.username` và
  `app.security.technician.password`.

Vì vậy không đặt `technician` vào `spring.datasource.username` trừ khi bạn
đã thực sự tạo một user PostgreSQL tên `technician`.

`spring.jpa.hibernate.ddl-auto=update` chỉ nên dùng cho manual testing local.
Không dùng tùy chọn này thay cho migration production.

Khi thấy log ứng dụng khởi động thành công, API ở:

```text
http://localhost:8080
```

## 3.1. Xử lý lỗi không kết nối được PostgreSQL

Nếu log có thông báo:

```text
Connection to localhost:5432 refused
```

thì ứng dụng đã nhận đúng cấu hình nhưng PostgreSQL chưa chạy, đang dùng port
khác, hoặc chưa mở TCP connection tại port `5432`.

Kiểm tra port trên Windows:

```cmd
netstat -ano | findstr :5432
```

Nếu không có dòng `LISTENING`, hãy khởi động PostgreSQL từ Services hoặc dùng
tên service tương ứng, ví dụ:

```cmd
sc query postgresql-x64-16
net start postgresql-x64-16
```

Tên service có thể khác theo phiên bản PostgreSQL đã cài. Có thể liệt kê service
liên quan bằng:

```cmd
sc query type= service state= all | findstr /I postgres
```

Sau khi PostgreSQL chạy, kiểm tra database và tài khoản bằng `psql`:

```cmd
psql -h localhost -p 5432 -U postgres -l
```

Nếu database `add2num` chưa tồn tại, tạo database:

```cmd
createdb -h localhost -p 5432 -U postgres add2num
```

Sau đó chạy lại lệnh khởi động ở mục 3. Nếu PostgreSQL dùng port khác, thay
`5432` trong `SPRING_DATASOURCE_URL` hoặc tham số JDBC bằng port thực tế.

## 4. Kịch bản 1 - Tạo Work Order thành công

Trong PowerShell, dùng `curl.exe` để gọi đúng chương trình cURL thay vì alias
`curl` của PowerShell:

```powershell
curl.exe -i -X POST "http://localhost:8080/api/workorders" `
  -u "technician:test-password" `
  -H "Content-Type: application/json" `
  -H "Accept: application/json" `
  --data-raw '{"equipmentId":"EQ-10001","priority":"HIGH"}'
```

Kỳ vọng:

- HTTP status: `201 Created`.
- Content-Type: `application/json`.
- Response có cấu trúc tương tự:

```json
{
  "id": "WO-10432",
  "equipmentId": "EQ-10001",
  "priority": "HIGH",
  "status": "Open",
  "createdAt": "2026-09-23T08:00:00Z"
}
```

`id` sẽ thay đổi vì được sinh ngẫu nhiên. `createdAt` cũng phụ thuộc thời điểm
gọi API nhưng phải là timestamp ISO 8601 UTC.

## 5. Kịch bản 2 - Validation thất bại

Ví dụ thiếu `equipmentId` và truyền `priority` không hợp lệ:

```powershell
curl.exe -i -X POST "http://localhost:8080/api/workorders" `
  -u "technician:test-password" `
  -H "Content-Type: application/json" `
  -H "Accept: application/problem+json" `
  --data-raw '{"priority":"URGENT"}'
```

Kỳ vọng:

- HTTP status: `400 Bad Request`.
- Content-Type tương thích `application/problem+json`.
- Response có các field RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "equipmentId: must not be blank; priority: must be one of: LOW, MEDIUM, HIGH",
  "instance": "/api/workorders"
}
```

Thứ tự chi tiết lỗi có thể phụ thuộc thứ tự validation của Spring, nhưng phải
chỉ rõ field sai và lý do.

## 6. Kịch bản 3 - Không có quyền xác thực

Gọi API không truyền Basic Authentication:

```powershell
curl.exe -i -X POST "http://localhost:8080/api/workorders" `
  -H "Content-Type: application/json" `
  -H "Accept: application/json" `
  --data-raw '{"equipmentId":"EQ-10001","priority":"HIGH"}'
```

Kỳ vọng:

- HTTP status: `401 Unauthorized`.
- Request không được gọi vào service hoặc repository.

Để kiểm tra user sai role, có thể dùng thông tin xác thực khác không có
`ROLE_TECHNICIAN`; request phải bị từ chối với `403 Forbidden` khi identity
provider cung cấp user đó.

## 7. Lưu ý an toàn

- Không commit password PostgreSQL hoặc password test vào source control.
- Không dùng `test-password` cho production.
- In-memory authentication hiện chỉ phù hợp local/RC; production cần identity
  provider được phê duyệt.
- Dừng ứng dụng bằng `Ctrl+C` trong cửa sổ PowerShell đang chạy Maven.
