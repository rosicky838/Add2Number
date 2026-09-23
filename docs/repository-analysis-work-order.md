# Báo cáo phân tích - Create Work Order

**Ngày:** 23/09/2026  
**Trạng thái:** `BLOCKED - Chưa được triển khai`  
**Phạm vi:** Phân tích yêu cầu và context repository, không tạo mã nguồn.

## 1. Nguồn đã kiểm tra

| Artifact | Kết quả sử dụng |
|---|---|
| `docs/domain-model.md` | Xác định model `WorkOrder`, invariant, ID format và persistence boundary. |
| `docs/api-spec.md` | Xác định endpoint, request/response, validation và RFC 7807. |
| `docs/drafts/draft-create-work-order.md` | Xác định rules, business flow, error mapping và implementation checklist. |
| `docs/coding-rules.md` | Xác định Java 17, Spring Boot 3.2+, constructor injection, DTO records, exception và logging rules. |
| `docs/api-rules.md` | Xác định REST naming, HTTP status, strict schema và `@Valid`. |
| `docs/security-rules.md` | Xác định input boundary validation, injection protection, RBAC và secret handling. |
| `pom.xml` | Xác định build/dependency hiện tại và các dependency còn thiếu. |

## 2. Kết luận điều hành

Không được chuyển sang implementation ở thời điểm hiện tại.

Các phần đã đủ rõ để thiết kế contract:

- Endpoint `POST /api/workorders`.
- Request gồm `equipmentId` và `priority`.
- `equipmentId` bắt buộc, non-blank, tối đa 50 ký tự.
- `priority` chỉ nhận `LOW`, `MEDIUM`, `HIGH`.
- Server sinh `id` theo `WO-` + 5 chữ số.
- Server gán `status = Open`.
- Server sinh `createdAt` theo ISO 8601 UTC.
- Thành công trả `201 Created`.
- Validation lỗi trả `400 Bad Request` theo RFC 7807.

Tuy nhiên, tính năng yêu cầu lưu Database và bảo vệ endpoint nhưng repository chưa có persistence layer hoặc Spring Security. Vì vậy chưa thể tạo implementation hợp lệ mà không tự đoán kiến trúc.

## 3. Mục tiêu nghiệp vụ

Cho phép Kỹ thuật viên tạo một Work Order khi phát hiện thiết bị bị hỏng. Hệ thống phải:

1. Nhận mã thiết bị và mức độ ưu tiên từ client.
2. Từ chối dữ liệu đầu vào không hợp lệ trước khi có side effect.
3. Tự sinh thông tin quản trị gồm ID, trạng thái và thời gian tạo.
4. Lưu Work Order vào Database.
5. Trả lại bản ghi vừa tạo để client có thể xác nhận kết quả.

## 4. Tác nhân và quyền truy cập

| Tác nhân | Quyền mong muốn | Trạng thái |
|---|---|---|
| Kỹ thuật viên | Gọi `POST /api/workorders` để tạo Work Order | Được yêu cầu trong BR |
| Người dùng không có role phù hợp | Không được tạo Work Order | Cần áp dụng RBAC |
| Hệ thống/server | Sinh ID, status, createdAt và lưu bản ghi | Được xác định |
| Database | Lưu bản ghi Work Order | Được yêu cầu nhưng công nghệ chưa xác định |

Rules yêu cầu RBAC với role `TECHNICIAN` và có thể cho phép `ADMIN`, nhưng BR chưa xác định:

- Cơ chế authentication: JWT, session hay OAuth2.
- Security provider/identity source.
- HTTP status khi thiếu authentication.
- HTTP status khi đã đăng nhập nhưng thiếu role.
- Có cho phép `ADMIN` ngoài `TECHNICIAN` hay không.

Đây là **BLOCKING** trước khi implement security.

## 5. Request, response và dữ liệu server tự sinh

### 5.1 Request

```json
{
  "equipmentId": "EQ-10001",
  "priority": "HIGH"
}
```

| Field | Kiểu | Bắt buộc | Quy tắc |
|---|---|---:|---|
| `equipmentId` | `string` | Có | Không blank sau trim, tối đa 50 ký tự |
| `priority` | `string` | Có | Chỉ `LOW`, `MEDIUM`, `HIGH` |

Client không được cung cấp:

- `id`
- `status`
- `createdAt`

### 5.2 Response thành công

```json
{
  "id": "WO-10432",
  "equipmentId": "EQ-10001",
  "priority": "HIGH",
  "status": "Open",
  "createdAt": "2026-09-23T07:30:00Z"
}
```

| Field | Nguồn | Quy tắc |
|---|---|---|
| `id` | Server | Khớp `^WO-[0-9]{5}$`, unique |
| `equipmentId` | Request đã validate | Tối đa 50 ký tự |
| `priority` | Request đã validate | Một trong ba enum |
| `status` | Server | `Open` khi tạo |
| `createdAt` | Server clock | `Instant`, ISO 8601 UTC |

## 6. Danh sách requirement

| ID | Requirement | Mức độ | Trạng thái |
|---|---|---|---|
| `REQ-001` | Cung cấp `POST /api/workorders` để tạo Work Order | Must | Đủ rõ |
| `REQ-002` | Chỉ actor có quyền Kỹ thuật viên được tạo | Must | Blocked bởi authentication/RBAC |
| `REQ-003` | `equipmentId` bắt buộc, non-blank, tối đa 50 ký tự | Must | Đủ rõ |
| `REQ-004` | `priority` bắt buộc và chỉ nhận `LOW`, `MEDIUM`, `HIGH` | Must | Đủ rõ |
| `REQ-005` | Server sinh ID theo `WO-` + 5 chữ số | Must | Retry/collision chưa rõ |
| `REQ-006` | ID Work Order phải unique trong persistence store | Must | Database chưa chọn |
| `REQ-007` | Server gán status mặc định `Open` | Must | Đủ rõ |
| `REQ-008` | Server gán `createdAt` theo ISO 8601 UTC | Must | Đủ rõ |
| `REQ-009` | Lưu bản ghi Work Order vào Database | Must | Database/persistence chưa chọn |
| `REQ-010` | Thành công trả `201 Created` với chi tiết Work Order | Must | Đủ rõ |
| `REQ-011` | Validation lỗi trả `400 Bad Request` | Must | Đủ rõ |
| `REQ-012` | Error response tuân RFC 7807 `application/problem+json` | Must | Format nhiều lỗi chưa chốt |
| `REQ-013` | Không cho client ghi đè server-managed fields | Must | Đủ rõ, unknown-field policy chưa chốt |
| `REQ-014` | Giữ nguyên chức năng cộng số hiện tại | Must | Cần regression test |
| `REQ-015` | Không hardcode secret và chống injection | Must | Đủ rõ theo security rules |

## 7. Luồng thành công

1. Client gửi `POST /api/workorders` với `Content-Type: application/json`.
2. Security layer xác thực actor và kiểm tra role theo policy được phê duyệt.
3. Controller bind JSON vào request DTO.
4. Jakarta Validation kiểm tra `equipmentId`.
5. Jakarta Validation kiểm tra `priority`.
6. Nếu dữ liệu hợp lệ, Controller gọi Work Order Service.
7. Service chuẩn hóa `equipmentId` theo trim policy được phê duyệt.
8. Service sinh ID đúng format `WO-[0-9]{5}`.
9. Repository kiểm tra ID có bị trùng không.
10. Nếu không trùng, Service gán `status = Open`.
11. Service lấy thời gian từ server clock dưới dạng UTC `Instant`.
12. Service tạo Work Order persistence object.
13. Repository lưu bản ghi vào Database.
14. Service map bản ghi đã lưu thành response DTO.
15. Controller trả `201 Created` và response JSON.

Bước 9 và retry ở bước 8-9 chưa thể implement cho đến khi có retry policy và database technology.

## 8. Luồng validation lỗi

1. Client gửi request tới `POST /api/workorders`.
2. Controller Boundary chạy `@Valid`.
3. Nếu thiếu hoặc blank `equipmentId`, ghi nhận lỗi field `equipmentId`.
4. Nếu `equipmentId` vượt 50 ký tự, ghi nhận lỗi độ dài.
5. Nếu thiếu hoặc sai `priority`, ghi nhận lỗi field `priority`.
6. Tạo RFC 7807 Problem Details.
7. Set `status = 400`.
8. Set `instance = /api/workorders`.
9. Set `Content-Type = application/problem+json`.
10. Không gọi service business.
11. Không sinh ID.
12. Không ghi Database.

Khi nhiều field cùng lỗi, thứ tự và schema chi tiết của danh sách violations chưa được quyết định.

## 9. Dependency kỹ thuật cần thêm

Danh sách dưới đây là dependency **cần đánh giá**, không phải quyết định công nghệ:

| Nhu cầu | Hiện trạng | Dependency/cấu hình cần đánh giá | Trạng thái |
|---|---|---|---|
| REST Controller | Đã có `spring-boot-starter-web` | Không nhất thiết thêm | Có thể tái sử dụng |
| Jakarta Validation | Chưa thấy dependency trực tiếp trong `pom.xml` | `spring-boot-starter-validation` | Cần xác nhận và thêm nếu chưa được kéo bắc cầu |
| Persistence | Chưa có | `spring-boot-starter-data-jpa` hoặc JDBC tương ứng | BLOCKING - chưa chọn |
| Database driver | Chưa có | Driver theo database được phê duyệt | BLOCKING - chưa chọn |
| Migration | Chưa có | Flyway/Liquibase hoặc policy khác | BLOCKING - chưa chọn |
| Security/RBAC | Chưa có Spring Security trong `pom.xml` | `spring-boot-starter-security` và provider tương ứng | BLOCKING - chưa chọn |
| API test | Đã có `spring-boot-starter-test` | MockMvc/JUnit hiện có thể tái sử dụng | Có thể tái sử dụng |
| Integration database test | Chưa có | Test database/driver theo persistence decision | BLOCKING - chưa chọn |

Không thêm dependency nào trong báo cáo này vì user yêu cầu không viết mã và không tự chọn công nghệ.

## 10. Rủi ro tích hợp vào project hiện tại

| Rủi ro | Bằng chứng/context | Ảnh hưởng | Biện pháp trước implementation |
|---|---|---|---|
| Chưa có Database layer | `pom.xml` chỉ có web, Thymeleaf, system JAR và test | Không thể thực hiện REQ-009 | Chốt database, persistence và migration |
| Chưa có Security layer | Không có Spring Security trong `pom.xml` | Không thể bảo vệ role `TECHNICIAN` | Chốt authentication/RBAC |
| API style hiện tại khác contract mới | `Add2NumController` trả `Map<String,Object>` cho `/api/calculate` | Có nguy cơ copy pattern không phù hợp | Tạo DTO và Problem Details riêng cho Work Order |
| Validation chưa được thiết lập rõ | Chưa có Work Order DTO hoặc validation handler | Có thể trả lỗi không đúng RFC 7807 | Chốt starter-validation và global handler |
| ID 5 chữ số có không gian hữu hạn | Chỉ có 100.000 giá trị | Collision tăng theo số lượng bản ghi | Chốt uniqueness constraint và retry/failure policy |
| Unknown JSON fields chưa chốt | API spec đặt câu hỏi bỏ qua hay từ chối | Contract không deterministic | Chọn một policy |
| Nhiều validation errors chưa chốt | RFC 7807 base fields chưa có `violations` | Client khó xử lý nhiều lỗi | Chốt detail format hoặc extension |
| Thay đổi root `pom.xml` | Project hiện là một Maven application, chưa phải multi-module | Có thể ảnh hưởng build hiện tại | Không biến thành multi-module nếu chưa có yêu cầu |
| System-scoped JAR hiện hữu | `Add2Num` dùng `systemPath` | Build portability có thể bị ảnh hưởng | Giữ nguyên và chạy regression |
| Security rules yêu cầu sanitize quá rộng | `security-rules.md` nêu loại bỏ ký tự đặc biệt | Có nguy cơ làm biến đổi mã thiết bị hợp lệ | Chốt allowlist/normalization cho `equipmentId` |
| Spring Boot 3.2 đã cũ so với latest | `pom.xml` dùng 3.2.0 | Có thể phát sinh compatibility/security work | Không tự nâng version trong task này |

## 11. Ma trận requirement -> artifact -> test

| Requirement | Artifact nguồn | Test cần có | Trạng thái |
|---|---|---|---|
| `REQ-001` | `docs/api-spec.md`, `draft-create-work-order.md` | MockMvc POST đúng path/method | Ready |
| `REQ-002` | `docs/api-spec.md`, `docs/security-rules.md` | Security test cho role hợp lệ/không hợp lệ | Blocked |
| `REQ-003` | `docs/domain-model.md`, `docs/api-spec.md` | Thiếu, null, blank, 51 ký tự | Ready |
| `REQ-004` | `docs/domain-model.md`, `docs/api-spec.md` | LOW/MEDIUM/HIGH và giá trị khác enum | Ready |
| `REQ-005` | `docs/domain-model.md` | Regex ID và generator unit test | Blocked bởi retry policy |
| `REQ-006` | `docs/domain-model.md` | Repository uniqueness/collision integration test | Blocked bởi database |
| `REQ-007` | `docs/domain-model.md`, `docs/api-spec.md` | Assert response `status = Open` | Ready |
| `REQ-008` | `docs/domain-model.md`, `docs/api-spec.md` | Parse timestamp UTC/Clock test | Ready sau khi chốt clock policy |
| `REQ-009` | `docs/domain-model.md`, draft | Persistence integration test | Blocked bởi database |
| `REQ-010` | `docs/api-spec.md`, `docs/api-rules.md` | Assert HTTP 201 và response schema | Ready |
| `REQ-011` | `docs/api-spec.md` | Assert HTTP 400 cho từng validation case | Ready |
| `REQ-012` | `docs/api-spec.md`, `docs/api-rules.md` | Assert content type và RFC 7807 fields | Ready sau khi chốt multi-error format |
| `REQ-013` | `docs/domain-model.md`, `docs/api-spec.md` | Request không cho ghi đè server fields | Blocked bởi unknown-field policy |
| `REQ-014` | `README.md`, existing tests | `.\mvnw.cmd test` và test hiện hữu | Ready |
| `REQ-015` | `docs/security-rules.md` | Static review, security tests, query parameterization review | Blocked bởi security design |

## 12. Danh sách toàn bộ `[NEEDS CLARIFICATION]`

### Blocking

1. Database engine nào được sử dụng?
2. Persistence technology là Spring Data JPA, JDBC hay công nghệ khác?
3. Tên bảng thực tế là gì? Có dùng `work_orders` không?
4. Migration tool là Flyway, Liquibase hay công cụ khác?
5. Khóa chính database có phải `id` dạng `String` hay cần khóa nội bộ khác?
6. Connection configuration cho local và test là gì?
7. Authentication dùng JWT, session hay OAuth2?
8. Identity provider/nguồn user là gì?
9. Role chính xác là chỉ `TECHNICIAN` hay `TECHNICIAN` và `ADMIN`?
10. HTTP status khi thiếu authentication là gì?
11. HTTP status khi thiếu role là gì?
12. Retry limit khi ID bị collision là bao nhiêu?
13. Nếu vượt retry limit, trả error code/status nào?
14. Có chấp nhận collision-safe alternative hay vẫn bắt buộc format 5 chữ số?
15. Request chứa unknown fields thì từ chối hay bỏ qua?
16. Khi nhiều field validation cùng lỗi, dùng `detail` duy nhất hay thêm extension `violations`?
17. Có bắt buộc trả `Location: /api/workorders/{id}` trong response `201` không?
18. `equipmentId` có được phép chứa ký tự đặc biệt hợp lệ nào? Quy tắc sanitize/allowlist cụ thể là gì?

### Không blocking cho contract cơ bản nhưng cần chốt trước release

19. Có cần correlation/request ID trong response hoặc log không?
20. Log những field nào và có cần masking `equipmentId` không?
21. Chính sách response khi Database unavailable là `500`, `503` hay error mapping riêng?
22. Có yêu cầu optimistic locking hoặc audit metadata trong tương lai không?

## 13. Điều kiện mở khóa implementation

Chỉ chuyển sang implementation khi:

1. Tất cả câu hỏi blocking từ mục 12 đã có quyết định được phê duyệt.
2. Cập nhật lại `domain-model.md`, `api-spec.md` và draft nếu quyết định làm thay đổi contract.
3. Chọn dependency persistence/security/migration cụ thể.
4. Chốt error status cho authentication, authorization, collision và database failure.
5. Chốt unknown fields và multi-error response format.
6. Tạo implementation plan có TASK-ID và file allowlist.
7. Tạo test plan bao phủ các requirement Ready và các quyết định mới.

**Kết luận:** Báo cáo đạt yêu cầu phân tích context nhưng trạng thái phát hành là `BLOCKED`; chưa được phép sinh mã nguồn.
