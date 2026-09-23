# Thiết kế kiến trúc - Create Work Order

**Vai trò:** Senior Spring Boot Architect  
**Ngày:** 23/09/2026  
**Trạng thái:** `BLOCKED - Thiết kế logical đã hoàn tất, chưa được phép sinh mã nguồn`

## 1. Phạm vi và nguyên tắc

Thiết kế này dành cho tính năng `POST /api/workorders` trong ứng dụng
`Add2NumWeb`.

Các artifact nguồn sự thật:

- [domain-model.md](./domain-model.md)
- [api-spec.md](./api-spec.md)
- [draft-create-work-order.md](./drafts/draft-create-work-order.md)
- [architecture-analysis-work-order.md](./architecture-analysis-work-order.md)
- [coding-rules.md](./coding-rules.md)
- [api-rules.md](./api-rules.md)
- [security-rules.md](./security-rules.md)

Nguyên tắc thiết kế:

1. Giữ nguyên chức năng cộng số hiện có.
2. Thêm Work Order như một vertical slice trong ứng dụng Spring Boot hiện tại.
3. Không chuyển project thành Maven multi-module nếu chưa có yêu cầu riêng.
4. Không thêm database, migration tool hoặc authentication mechanism khi chưa được phê duyệt.
5. Không tạo field ngoài API contract.
6. Không tạo mã nguồn trong tài liệu thiết kế này.

## 2. Trạng thái quyết định kiến trúc

| Quyết định | Trạng thái | Hệ quả |
|---|---|---|
| Package gốc | Đã quyết định: `com.add2numweb` | Có thể thiết kế package placement |
| Endpoint | Đã quyết định: `POST /api/workorders` | Có thể thiết kế Controller contract |
| Request/response fields | Đã quyết định trong API spec | Có thể thiết kế DTO |
| Java/Spring | Java 17, Spring Boot 3.2.0 | Có thể dùng records và Jakarta APIs |
| Database engine | Chưa quyết định | Chưa thể chốt entity mapping/driver |
| JPA hay JDBC | Chưa quyết định | Chưa thể chốt Repository implementation |
| Migration tool | Chưa quyết định | Chưa thể chốt migration files |
| Authentication | Chưa quyết định | Chưa thể chốt SecurityFilterChain/provider |
| Authorization status codes | Chưa quyết định | Chưa thể hoàn thiện security tests |
| ID collision retry limit | Chưa quyết định | Chưa thể hoàn thiện ID generator |
| Unknown JSON fields | Chưa quyết định | Chưa thể chốt Jackson behavior |
| Multiple validation errors | Chưa quyết định | Chưa thể chốt error extension schema |

## 3. Kiến trúc logical tối thiểu

```text
HTTP Client
    |
    v
Security Boundary (conditional)
    |
    v
WorkOrderController
    |
    v
Jakarta Validation
    |
    v
WorkOrderService
    |       \
    |        \-- IdGenerator + injected Clock
    v
WorkOrderRepository (port/abstraction)
    |
    v
Database (technology not selected)

Validation/Business/Infrastructure Exception
    |
    v
GlobalExceptionHandler
    |
    v
RFC 7807 Problem Details
```

Luồng chính:

```text
Controller -> Validation -> Service -> ID/Clock -> Repository -> Response DTO
```

Không cần thêm event bus, message broker, cache, CQRS hoặc nhiều architectural layer
cho phạm vi tạo Work Order hiện tại.

## 4. Package và file placement

### 4.1 Feature package

```text
src/main/java/com/add2numweb/workorder/
├── WorkOrderController.java
├── WorkOrderService.java
├── CreateWorkOrderRequest.java
├── WorkOrderResponse.java
├── WorkOrder.java
├── Priority.java
├── WorkOrderStatus.java
├── WorkOrderRepository.java       (conditional)
├── WorkOrderIdGenerator.java      (conditional/separate component)
└── WorkOrderPersistenceMapper.java (conditional, nếu mapping cần thiết)
```

### 4.2 Shared exception package

```text
src/main/java/com/add2numweb/exception/
├── GlobalExceptionHandler.java
├── ProblemDetails.java             (chỉ tạo nếu không dùng Spring ProblemDetail)
└── WorkOrderIdGenerationException.java
```

### 4.3 Test placement

```text
src/test/java/com/add2numweb/workorder/
├── WorkOrderServiceTest.java
├── WorkOrderIdGeneratorTest.java
├── WorkOrderControllerTest.java
└── WorkOrderRepositoryTest.java   (conditional)

src/test/java/com/add2numweb/exception/
└── GlobalExceptionHandlerTest.java
```

Không thay đổi các package của chức năng cộng số.

## 5. Trách nhiệm của từng component

### 5.1 `WorkOrderController`

Controller phải:

1. Expose `POST /api/workorders`.
2. Nhận `application/json`.
3. Bind body vào `CreateWorkOrderRequest`.
4. Kích hoạt `@Valid`.
5. Gọi `WorkOrderService` chỉ khi request hợp lệ.
6. Trả `201 Created` và `WorkOrderResponse`.
7. Không tự sinh ID, status hoặc createdAt.
8. Không truy cập trực tiếp Database.
9. Không bắt lỗi bằng `Map<String, Object>`.
10. Không trả stack trace.

### 5.2 `WorkOrderService`

Service phải:

1. Nhận request DTO đã qua boundary validation.
2. Chuẩn hóa `equipmentId` theo normalization policy được phê duyệt.
3. Gọi ID generator.
4. Kiểm tra ID collision qua repository.
5. Retry theo limit đã được phê duyệt.
6. Gán `status = Open`.
7. Lấy `createdAt` từ injected UTC `Clock`.
8. Tạo domain/persistence object.
9. Lưu qua repository.
10. Map object đã lưu thành response DTO.
11. Không chứa HTTP-specific logic.
12. Không tự nuốt exception persistence.

### 5.3 `WorkOrderRepository`

Repository là persistence abstraction. Tối thiểu cần khả năng:

1. Kiểm tra ID đã tồn tại.
2. Lưu Work Order.
3. Bảo đảm uniqueness ở database, không chỉ dựa vào check trước khi save.

Implementation cụ thể phụ thuộc quyết định JPA/JDBC/database. Chưa được sinh
repository code trước khi quyết định này được phê duyệt.

### 5.4 `GlobalExceptionHandler`

Handler phải map:

- `MethodArgumentNotValidException` -> `400`.
- Malformed JSON -> `400`.
- Unknown field error -> status theo unknown-field policy.
- ID generation failure -> status/error type theo retry policy.
- Persistence failure -> status/error type theo infrastructure policy.
- Authorization/authentication failure -> status theo security policy.

Response lỗi phải có:

| Field | Quy tắc |
|---|---|
| `type` | URI định danh error type |
| `title` | Tên lỗi ngắn |
| `status` | HTTP status |
| `detail` | Lý do cụ thể, không chứa stack trace |
| `instance` | `/api/workorders` |

## 6. Request và Response DTO

### 6.1 `CreateWorkOrderRequest`

DTO chỉ có hai field:

| Field | Type | Validation |
|---|---|---|
| `equipmentId` | `String` | `@NotBlank`, `@Size(max = 50)`; trim policy phải được chốt |
| `priority` | `String` hoặc enum boundary | Bắt buộc; chỉ `LOW`, `MEDIUM`, `HIGH` |

Khuyến nghị dùng Java 17 `record`. Không thêm:

- `id`
- `status`
- `createdAt`
- `createdBy`
- `assignedTo`
- `title`
- `description`

### 6.2 `WorkOrderResponse`

DTO success chỉ có:

| Field | Type | Nguồn |
|---|---|---|
| `id` | `String` | Server-generated |
| `equipmentId` | `String` | Validated request |
| `priority` | `String`/enum | Validated request |
| `status` | `String`/enum | Server default `Open` |
| `createdAt` | `Instant` serialized as ISO 8601 UTC | Server clock |

## 7. Domain/entity model

### 7.1 Domain invariant

1. `id` khớp `^WO-[0-9]{5}$`.
2. `id` unique.
3. `equipmentId` không blank và dài tối đa 50 ký tự.
4. `priority` thuộc `LOW`, `MEDIUM`, `HIGH`.
5. `status` là `Open` khi tạo.
6. `createdAt` do server sinh.
7. Client không kiểm soát server-managed fields.

### 7.2 Persistence mapping chưa thể chốt

| Domain field | Logical mapping | Quyết định còn thiếu |
|---|---|---|
| `id` | Primary/unique identifier dạng string | Có khóa nội bộ riêng không? |
| `equipmentId` | Non-null string, max 50 | Database column type/name |
| `priority` | Enum/string | Enum mapping strategy |
| `status` | Enum/string default `Open` | Database default hay service default |
| `createdAt` | Timestamp UTC | Database timestamp type/timezone |

Không tự chọn `@Entity`, `@Table`, `@Enumerated`, JDBC SQL hoặc column name
implementation cho đến khi persistence technology được phê duyệt.

## 8. Jakarta Validation

Boundary validation phải xảy ra trước Service và persistence:

1. Request body phải được bind vào DTO.
2. Controller dùng `@Valid`.
3. `equipmentId` kiểm tra null/blank và max length 50.
4. `priority` kiểm tra presence và allowed values.
5. Validation failure trả `400`.
6. Validation failure không sinh ID.
7. Validation failure không gọi repository save.
8. Error response là `application/problem+json`.

`spring-boot-starter-validation` chưa có trong `pom.xml`; phải xác nhận và thêm
dependency nếu dependency graph hiện tại chưa cung cấp Jakarta Validation.

## 9. RFC 7807 Problem Details

### 9.1 Contract

| Tình huống | Status | Content-Type |
|---|---:|---|
| Missing/blank/too-long `equipmentId` | `400` | `application/problem+json` |
| Missing/invalid `priority` | `400` | `application/problem+json` |
| Malformed JSON | `400` | `application/problem+json` |
| Unknown field | Chưa chốt | `application/problem+json` |
| Unauthorized/forbidden | Chưa chốt | `application/problem+json` |
| ID collision exhausted | Chưa chốt | `application/problem+json` |
| Database failure | Chưa chốt | `application/problem+json` |

### 9.2 Multiple validation errors

Chưa được quyết định có thêm extension `violations` hay không. Không được tự
thêm field vào public contract trước khi quyết định được phê duyệt.

## 10. ID generation và collision handling

Luồng logical:

1. Sinh số trong `00000..99999`.
2. Format thành 5 chữ số.
3. Ghép `WO-`.
4. Kiểm tra tồn tại.
5. Retry nếu collision.
6. Save với uniqueness constraint.
7. Nếu retry exhausted, trả error theo policy.

Các blocker:

- Retry limit chưa được chọn.
- Error status/type chưa được chọn.
- Race condition giữa `exists` và `save` cần database uniqueness constraint.
- Không được thay thế bằng UUID vì BR yêu cầu format `WO-` + 5 chữ số.

Có thể inject một ID generator interface/component để unit test deterministic,
nhưng implementation cụ thể chỉ tạo sau khi retry policy được phê duyệt.

## 11. UTC clock và test deterministic

Thiết kế Service nhận một `Clock` injectable:

1. Production dùng UTC clock.
2. Test dùng fixed clock.
3. Service lấy một timestamp cho một lần tạo.
4. Không đọc `createdAt` từ request.
5. Serialize response thành ISO 8601 UTC.

Không gọi trực tiếp `Instant.now()` trong business logic nếu điều đó làm test
không deterministic.

## 12. RBAC cho role `TECHNICIAN`

Logical boundary:

```text
Authentication -> Authorization -> WorkOrderController
```

Rule nghiệp vụ đã biết:

- Endpoint phải được bảo vệ.
- Actor cần role `TECHNICIAN`.
- Principle of least privilege.

Chưa được chốt:

1. Authentication mechanism.
2. Identity provider.
3. Có cho phép `ADMIN` không.
4. Status khi unauthenticated.
5. Status khi authenticated nhưng thiếu role.
6. Cách tạo security principal trong test.

Không thêm `spring-boot-starter-security`, JWT provider hoặc `@PreAuthorize`
implementation cho đến khi các mục trên được phê duyệt.

## 13. Database mapping và migration

Logical table đề xuất trong domain model là `work_orders`, nhưng chưa phải quyết
định cuối.

Migration cần tạo sau khi chốt:

1. Database engine.
2. Table/schema name.
3. Primary/unique key strategy.
4. Column types.
5. Enum storage.
6. UTC timestamp mapping.
7. Migration tool.
8. Local/test database.

Không tự tạo file dưới `src/main/resources/db/migration/` trong giai đoạn này.

## 14. Test boundaries

### 14.1 Unit tests

Phạm vi:

- `WorkOrderService`.
- ID generator.
- Validation helper/enum parser nếu có.
- Clock behavior.

Không khởi động Spring context. Dùng fake repository, fixed clock và deterministic
ID generator.

### 14.2 MVC/API tests

Phạm vi:

- Route `POST /api/workorders`.
- JSON request/response.
- `201 Created`.
- `400 Bad Request`.
- Content-Type.
- RFC 7807 fields.
- Không gọi save khi validation fail.
- Unknown fields theo policy sau khi được chốt.

Có thể dùng MockMvc như test hiện tại, nhưng không dùng `.param(...)` cho request
JSON.

### 14.3 Security tests

Chỉ tạo sau khi auth mechanism được chốt:

- Authenticated `TECHNICIAN` được phép.
- Unauthenticated bị từ chối.
- Wrong role bị từ chối.
- `ADMIN` được phép hay không theo decision.

### 14.4 Integration tests

Chỉ tạo sau khi database/persistence được chốt:

- Migration chạy thành công.
- Lưu và đọc lại Work Order.
- Unique ID constraint.
- Enum/status mapping.
- UTC timestamp mapping.
- Collision/race handling theo policy.

## 15. Dependency Maven cần đánh giá

`pom.xml` hiện có web, Thymeleaf, system-scoped Add2Num JAR và test starter.

| Nhu cầu | Dependency ứng viên | Trạng thái |
|---|---|---|
| Validation | `spring-boot-starter-validation` | Cần xác nhận |
| Persistence | JPA starter hoặc JDBC dependency | Chưa chọn |
| Driver | Theo database engine | Chưa chọn |
| Migration | Flyway hoặc Liquibase | Chưa chọn |
| Security | `spring-boot-starter-security` | Chưa chọn |
| Auth provider | Theo JWT/session/OAuth2 decision | Chưa chọn |
| Integration DB test | Theo test database decision | Chưa chọn |

Không có dependency nào được thêm trong tài liệu này.

## 16. Cách chạy local và test

### 16.1 Trạng thái hiện tại

Ứng dụng hiện có thể chạy bằng:

```powershell
.\mvnw.cmd spring-boot:run
```

Test hiện tại:

```powershell
.\mvnw.cmd test
```

### 16.2 Sau khi kiến trúc được phê duyệt

Quy trình dự kiến:

1. Cập nhật dependency theo quyết định.
2. Cấu hình local bằng environment variables, không commit secret.
3. Chạy migration trên database local/test.
4. Khởi động ứng dụng bằng `.\mvnw.cmd spring-boot:run`.
5. Gửi request JSON tới `POST /api/workorders`.
6. Chạy unit, MVC/API, security và integration tests.
7. Chạy lại toàn bộ regression test của Add2Num.

Các lệnh migration/database cụ thể chưa thể ghi vì technology chưa được chọn.

## 17. Blockers trước khi sinh mã nguồn

1. Chọn database engine.
2. Chọn JPA hay JDBC.
3. Chọn migration tool.
4. Chọn authentication mechanism và identity provider.
5. Chốt role được phép và HTTP status authorization.
6. Chốt retry limit/error khi ID collision.
7. Chốt unknown JSON fields: reject hoặc ignore.
8. Chốt schema nhiều validation errors.
9. Chốt `Location` header.
10. Chốt trim/normalization và allowed characters của `equipmentId`.

## 18. Kết luận

Kiến trúc tối thiểu được đề xuất là:

```text
WorkOrderController
    -> Jakarta Validation
    -> WorkOrderService
    -> WorkOrderRepository
    -> Database

GlobalExceptionHandler
    -> RFC 7807 Problem Details
```

Thiết kế logical đã đủ để lập implementation plan ở mức high-level, nhưng chưa
đủ để sinh mã nguồn an toàn. Trạng thái cuối cùng là **BLOCKED** cho đến khi
các quyết định persistence, authentication và retry policy được phê duyệt.
