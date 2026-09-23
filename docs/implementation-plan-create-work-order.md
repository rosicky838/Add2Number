# Implementation Plan - Create Work Order

**Vai trò:** Senior Technical Lead  
**Ngày:** 23/09/2026  
**Trạng thái:** `DRAFT - BLOCKED trước khi implementation`

## 1. Nguồn đầu vào và phạm vi

Plan này được xây dựng dựa trên:

- [domain-model.md](./domain-model.md)
- [api-spec.md](./api-spec.md)
- [draft-create-work-order.md](./drafts/draft-create-work-order.md)
- [openapi-contract-work-order.md](./openapi-contract-work-order.md)
- [architecture-analysis-work-order.md](./architecture-analysis-work-order.md)
- [architecture-design-work-order.md](./architecture-design-work-order.md)
- [coding-rules.md](./coding-rules.md)
- [api-rules.md](./api-rules.md)
- [security-rules.md](./security-rules.md)
- [pom.xml](../pom.xml)

> Repository hiện chưa có file `openapi.yaml` vật lý. Hợp đồng OpenAPI hiện được
> lưu trong [openapi-contract-work-order.md](./openapi-contract-work-order.md)
> dưới dạng YAML fenced block. Nếu pipeline yêu cầu file YAML độc lập, cần tạo
> `docs/openapi.yaml` ở một task riêng trước khi chạy contract validation.

### 1.1 In scope

- `POST /api/workorders`.
- Request gồm `equipmentId`, `priority`.
- Validation và RFC 7807 error response.
- Server-generated `id`, `status`, `createdAt`.
- Persistence Work Order.
- RBAC cho actor được phê duyệt.
- Unit, MVC/API, security, integration và regression tests.

### 1.2 Out of scope

- Sửa hoặc di chuyển chức năng cộng số.
- Thêm GET/PUT/PATCH/DELETE Work Order.
- Thêm UI Work Order.
- Thay đổi Java/Spring Boot version.
- Tự chọn database, authentication, migration hoặc retry policy.

## 2. Blocking decisions trước khi chạy TASK-002 trở đi

Các quyết định sau phải được phê duyệt:

1. Database engine.
2. Persistence technology: JPA hay JDBC.
3. Tên bảng/schema và key strategy.
4. Migration tool.
5. Local/test database strategy.
6. Authentication mechanism và identity provider.
7. Role được phép: `TECHNICIAN`, `ADMIN` hay role khác.
8. HTTP status cho unauthenticated/forbidden.
9. ID collision retry limit và error mapping.
10. Unknown JSON fields: reject hay ignore.
11. Multiple validation errors: `detail` hay extension `violations`.
12. `Location` header có bắt buộc hay không.
13. `equipmentId` trim và character allowlist.

Nếu còn bất kỳ quyết định blocking nào chưa chốt, dừng trước khi sửa `pom.xml`,
thêm persistence/security dependency hoặc tạo migration.

## 3. Dependency graph và thứ tự thực thi

```text
TASK-001 Decisions
    |
    v
TASK-002 Dependencies/Configuration
    |
    +--> TASK-003 Enum/Domain/Entity
    |
    +--> TASK-004 DTO/Validation
    |
    +--> TASK-005 Repository
              |
              v
         TASK-006 Service/ID Generator
              |
              v
         TASK-007 Controller
              |
              v
         TASK-008 RFC 7807 Handler
              |
              +--> TASK-009 Unit Tests
              +--> TASK-010 MockMvc/API Tests
              +--> TASK-011 Integration Tests
              |
              v
         TASK-012 Regression
              |
              v
         TASK-013 Traceability/Completeness
```

TASK-003 và TASK-004 có thể thực hiện song song sau TASK-001/TASK-002 nếu
domain mapping và validation decisions đã ổn định. TASK-005 phụ thuộc persistence
decision. TASK-007 phụ thuộc DTO, Service và security decision.

## 4. Task breakdown

### TASK-001 - Chốt persistence và security decisions

**Mục tiêu:** Đóng toàn bộ quyết định blocking trước khi thay đổi dependency hoặc code.

**Requirement IDs:** `REQ-002`, `REQ-005`, `REQ-006`, `REQ-009`, `REQ-012`, `REQ-013`, `REQ-015`.

**File được phép tạo/sửa:**

- `docs/domain-model.md`
- `docs/api-spec.md`
- `docs/drafts/draft-create-work-order.md`
- `docs/architecture-design-work-order.md`
- `docs/openapi-contract-work-order.md`
- `docs/openapi.yaml` nếu cần artifact YAML độc lập

**Test-first step:**

- Chưa viết test code.
- Tạo decision checklist và cập nhật acceptance criteria trước implementation.

**Lệnh verification:**

```powershell
git diff --check
```

**Dependency:** Không có; đây là task gate.

**Risk:**

- Nếu bỏ qua task này, Copilot có thể tự chọn database/security và tạo code không tương thích.
- Thay đổi contract sau khi viết code sẽ gây rework.

**Exit criteria:**

- Không còn `[NEEDS CLARIFICATION]` blocking.
- Persistence, authentication, RBAC, retry và error policies đã có owner approval.

---

### TASK-002 - Cập nhật dependency và configuration

**Mục tiêu:** Thêm đúng các dependency/configuration được phê duyệt.

**Requirement IDs:** `REQ-002`, `REQ-003`, `REQ-009`, `REQ-012`, `REQ-015`.

**File được phép tạo/sửa:**

- `pom.xml`
- `src/main/resources/application.properties`
- `src/test/resources/application.properties` nếu cần

**Test-first step:**

1. Xác định build smoke test và context startup test.
2. Ghi rõ dependency expected trong plan đã approved.
3. Không commit secret hoặc database credential.

**Lệnh verification:**

```powershell
.\mvnw.cmd -q dependency:tree
.\mvnw.cmd test
```

**Dependency:** TASK-001.

**Risk:**

- Sai driver hoặc version có thể làm context startup fail.
- Thêm security dependency có thể làm test hiện tại bị `401/403`.
- System-scoped Add2Num JAR có thể làm build portability kém.

**Exit criteria:**

- Dependency tree đúng decision.
- Existing tests vẫn pass hoặc failure được phân loại do security config đã được phê duyệt.

---

### TASK-003 - Tạo enum và domain/entity

**Mục tiêu:** Tạo model Work Order và các invariant domain.

**Requirement IDs:** `REQ-005`, `REQ-006`, `REQ-007`, `REQ-008`, `REQ-009`, `REQ-013`.

**File được phép tạo/sửa:**

- `src/main/java/com/add2numweb/workorder/WorkOrder.java`
- `src/main/java/com/add2numweb/workorder/Priority.java`
- `src/main/java/com/add2numweb/workorder/WorkOrderStatus.java`

**Test-first step:**

1. Test enum values.
2. Test ID pattern invariant.
3. Test default `Open`.
4. Test server-managed fields không lấy từ request.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=WorkOrderDomainTest test
```

**Dependency:** TASK-001, TASK-002.

**Risk:**

- Entity mapping sai nếu tự suy diễn JPA/JDBC.
- Enum value `Open` khác convention uppercase của `Priority`.
- ID dạng 8 ký tự có không gian hữu hạn.

**Exit criteria:**

- Model chỉ chứa fields đã được phê duyệt.
- Mapping tương thích persistence decision.

---

### TASK-004 - Tạo request/response DTO và validation

**Mục tiêu:** Định nghĩa API data boundary đúng contract.

**Requirement IDs:** `REQ-003`, `REQ-004`, `REQ-010`, `REQ-011`, `REQ-012`, `REQ-013`.

**File được phép tạo/sửa:**

- `src/main/java/com/add2numweb/workorder/CreateWorkOrderRequest.java`
- `src/main/java/com/add2numweb/workorder/WorkOrderResponse.java`
- Validation annotation/helper file chỉ khi implementation plan cho phép

**Test-first step:**

1. Test request hợp lệ.
2. Test missing/null/blank `equipmentId`.
3. Test `equipmentId` dài hơn 50 ký tự.
4. Test missing/invalid `priority`.
5. Test request không chứa server-managed fields.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=CreateWorkOrderRequestTest test
```

**Dependency:** TASK-001, TASK-002.

**Risk:**

- `priority` parsing có thể vô tình chấp nhận lowercase hoặc giá trị ngoài enum.
- Trim behavior chưa đúng decision sẽ làm response/database không nhất quán.
- DTO thêm field thừa làm vỡ contract.

**Exit criteria:**

- Request chỉ có `equipmentId`, `priority`.
- Response chỉ có `id`, `equipmentId`, `priority`, `status`, `createdAt`.
- Validation rules khớp OpenAPI contract.

---

### TASK-005 - Tạo repository

**Mục tiêu:** Cung cấp persistence abstraction và uniqueness enforcement.

**Requirement IDs:** `REQ-005`, `REQ-006`, `REQ-009`.

**File được phép tạo/sửa:**

- `src/main/java/com/add2numweb/workorder/WorkOrderRepository.java`
- Persistence adapter/entity mapping files theo decision
- Migration file dưới `src/main/resources/db/migration/` hoặc location đã phê duyệt

**Test-first step:**

1. Test save Work Order.
2. Test lookup ID tồn tại.
3. Test unique constraint.
4. Test enum/status/timestamp mapping.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=WorkOrderRepositoryTest test
```

**Dependency:** TASK-001, TASK-002, TASK-003.

**Risk:**

- Race condition nếu chỉ check `exists` mà không có database unique constraint.
- Migration không tương thích local/test database.
- Timestamp timezone mapping sai.

**Exit criteria:**

- Repository behavior được test với database strategy đã phê duyệt.
- Migration chạy được.
- Unique ID được enforce ở persistence layer.

---

### TASK-006 - Tạo service và ID generator

**Mục tiêu:** Implement business flow tạo Work Order mà không chứa HTTP-specific logic.

**Requirement IDs:** `REQ-003`, `REQ-004`, `REQ-005`, `REQ-006`, `REQ-007`, `REQ-008`, `REQ-009`.

**File được phép tạo/sửa:**

- `src/main/java/com/add2numweb/workorder/WorkOrderService.java`
- `src/main/java/com/add2numweb/workorder/WorkOrderIdGenerator.java`
- Exception riêng cho ID generation nếu được quyết định

**Test-first step:**

1. Test tạo thành công với từng priority.
2. Test status luôn `Open`.
3. Test `createdAt` từ fixed UTC clock.
4. Test ID match `^WO-[0-9]{5}$`.
5. Test collision retry.
6. Test retry exhausted theo error policy.
7. Test repository save chỉ xảy ra sau validation/business checks.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=WorkOrderServiceTest,WorkOrderIdGeneratorTest test
```

**Dependency:** TASK-003, TASK-004, TASK-005, TASK-001 retry decision.

**Risk:**

- Retry policy chưa chốt có thể gây loop vô hạn.
- Check-then-save không đủ bảo vệ race condition.
- Dùng system time trực tiếp làm test không deterministic.

**Exit criteria:**

- Injected `Clock`.
- ID generator deterministic trong unit test.
- Retry và collision behavior đúng decision.

---

### TASK-007 - Tạo Controller

**Mục tiêu:** Expose endpoint `POST /api/workorders` theo OpenAPI contract.

**Requirement IDs:** `REQ-001`, `REQ-002`, `REQ-003`, `REQ-004`, `REQ-010`, `REQ-011`, `REQ-013`.

**File được phép tạo/sửa:**

- `src/main/java/com/add2numweb/workorder/WorkOrderController.java`
- Security annotation/config file chỉ theo security decision

**Test-first step:**

1. Test route và method.
2. Test JSON request hợp lệ.
3. Test success `201`.
4. Test `Content-Type`.
5. Test service được gọi đúng một lần.
6. Test authorization theo role decision.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=WorkOrderControllerTest test
```

**Dependency:** TASK-004, TASK-006, TASK-001 security decision.

**Risk:**

- Copy `Map<String,Object>` pattern từ `Add2NumController`.
- Dùng HTTP `200` thay vì `201`.
- Security config làm hỏng endpoint hiện tại nếu cấu hình quá rộng.

**Exit criteria:**

- Endpoint đúng path/method.
- Không truy cập repository trực tiếp.
- Không tạo server-managed fields từ request.

---

### TASK-008 - Tạo RFC 7807 exception handler

**Mục tiêu:** Chuẩn hóa mọi lỗi API Work Order theo Problem Details.

**Requirement IDs:** `REQ-011`, `REQ-012`, `REQ-013`, `REQ-015`.

**File được phép tạo/sửa:**

- `src/main/java/com/add2numweb/exception/GlobalExceptionHandler.java`
- `src/main/java/com/add2numweb/exception/ProblemDetails.java` nếu cần
- Error constants/types theo approved contract

**Test-first step:**

1. Test `MethodArgumentNotValidException`.
2. Test malformed JSON.
3. Test unknown fields theo policy.
4. Test ID collision failure.
5. Test persistence failure.
6. Test `type`, `title`, `status`, `detail`, `instance`.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=GlobalExceptionHandlerTest test
```

**Dependency:** TASK-004, TASK-006, TASK-007, TASK-001 error decisions.

**Risk:**

- Global handler làm thay đổi error behavior của `/api/calculate`.
- Dùng field ngoài RFC contract chưa được phê duyệt.
- Trả stack trace hoặc infrastructure detail cho client.

**Exit criteria:**

- Error Work Order có `application/problem+json`.
- Validation lỗi trả `400`.
- Không tạo success-shaped error response.

---

### TASK-009 - Tạo unit tests

**Mục tiêu:** Kiểm tra domain/service logic độc lập với Spring context.

**Requirement IDs:** `REQ-003` đến `REQ-009`, `REQ-015`.

**File được phép tạo/sửa:**

- `src/test/java/com/add2numweb/workorder/WorkOrderDomainTest.java`
- `src/test/java/com/add2numweb/workorder/WorkOrderServiceTest.java`
- `src/test/java/com/add2numweb/workorder/WorkOrderIdGeneratorTest.java`

**Test-first step:**

Đây chính là task test-first cho domain/service; viết test trước khi chốt implementation details.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=WorkOrderDomainTest,WorkOrderServiceTest,WorkOrderIdGeneratorTest test
```

**Dependency:** TASK-003, TASK-004, TASK-005, TASK-006.

**Risk:**

- Test phụ thuộc random/time thật sẽ flaky.
- Mock repository không kiểm tra được database uniqueness.

**Exit criteria:**

- Unit tests deterministic.
- Bao phủ success, validation boundary, collision, clock và failure paths.

---

### TASK-010 - Tạo MockMvc/API tests

**Mục tiêu:** Kiểm tra HTTP contract và RFC 7807 behavior.

**Requirement IDs:** `REQ-001`, `REQ-002`, `REQ-003`, `REQ-004`, `REQ-010`, `REQ-011`, `REQ-012`, `REQ-013`.

**File được phép tạo/sửa:**

- `src/test/java/com/add2numweb/workorder/WorkOrderControllerTest.java`
- `src/test/java/com/add2numweb/exception/GlobalExceptionHandlerTest.java`

**Test-first step:**

1. POST hợp lệ cho LOW/MEDIUM/HIGH.
2. Thiếu/null/blank/too-long `equipmentId`.
3. Thiếu/invalid `priority`.
4. Malformed JSON.
5. Multiple validation errors.
6. Unknown fields.
7. Server-managed fields trong request.
8. Assert response schema, status và content type.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=WorkOrderControllerTest,GlobalExceptionHandlerTest test
```

**Dependency:** TASK-007, TASK-008, TASK-001 API policy decisions.

**Risk:**

- MockMvc có thể không phản ánh database transaction.
- Security test không hợp lệ nếu chưa cấu hình identity test.

**Exit criteria:**

- Tất cả API acceptance criteria có test.
- Không dùng `success=false` cho error.

---

### TASK-011 - Tạo integration test với database test phù hợp

**Mục tiêu:** Xác minh migration, persistence mapping và uniqueness trong môi trường database test.

**Requirement IDs:** `REQ-005`, `REQ-006`, `REQ-008`, `REQ-009`, `REQ-015`.

**File được phép tạo/sửa:**

- `src/test/java/com/add2numweb/workorder/WorkOrderRepositoryTest.java`
- `src/test/java/com/add2numweb/workorder/WorkOrderIntegrationTest.java`
- `src/test/resources/application.properties`
- Test fixture/config theo database decision

**Test-first step:**

1. Migration startup.
2. Save và read-back.
3. Unique ID.
4. Enum/status mapping.
5. UTC timestamp.
6. Collision/concurrent create theo approved policy.

**Lệnh verification:**

```powershell
.\mvnw.cmd -Dtest=WorkOrderRepositoryTest,WorkOrderIntegrationTest test
```

**Dependency:** TASK-002, TASK-003, TASK-005, TASK-006, database/test decision TASK-001.

**Risk:**

- Test phụ thuộc service database ngoài môi trường.
- H2 behavior khác production database.
- Migration và test transaction có thể không tương thích.

**Exit criteria:**

- Integration tests chạy reproducibly.
- Test database strategy được ghi lại.
- Không leak credential.

---

### TASK-012 - Chạy regression test hiện có

**Mục tiêu:** Bảo đảm Work Order không phá chức năng cộng số hoặc startup hiện tại.

**Requirement IDs:** `REQ-014`, gián tiếp tất cả requirements tích hợp.

**File được phép tạo/sửa:**

- Không tạo file mới.
- Chỉ sửa test hiện hữu nếu failure chứng minh regression do thay đổi Work Order.

**Test-first step:**

- Không thêm test mới; sử dụng test baseline hiện có.

**Lệnh verification:**

```powershell
.\mvnw.cmd clean test
```

**Dependency:** TASK-002 đến TASK-011.

**Risk:**

- Security config mới có thể thay đổi behavior endpoint cũ.
- Global exception handler có thể ảnh hưởng `/api/calculate`.
- Dependency conflict làm context startup fail.

**Exit criteria:**

- Existing Add2Num tests pass.
- Không có thay đổi ngoài scope.

---

### TASK-013 - Review traceability và completeness

**Mục tiêu:** Xác nhận mọi requirement đã có source, test và evidence trước release.

**Requirement IDs:** `REQ-001` đến `REQ-015`.

**File được phép tạo/sửa:**

- `docs/implementation-plan-create-work-order.md`
- `docs/domain-model.md`
- `docs/api-spec.md`
- `docs/drafts/draft-create-work-order.md`
- `docs/openapi-contract-work-order.md`
- `docs/openapi.yaml` nếu đã tạo

**Test-first step:**

- Không viết code; kiểm tra traceability matrix và test evidence.

**Lệnh verification:**

```powershell
git diff --check
.\mvnw.cmd clean test
```

**Dependency:** TASK-012.

**Risk:**

- Đánh dấu PASS khi còn `[NEEDS CLARIFICATION]`.
- Requirement có spec nhưng không có executable test.
- OpenAPI artifact và implementation bị lệch.

**Exit criteria:**

- Mỗi requirement có acceptance criteria, source location và test.
- Không còn blocker chưa được owner phê duyệt.
- API contract khớp OpenAPI.
- Regression suite pass.

## 5. Requirement traceability matrix

| Requirement | Acceptance evidence | Task chính | Test |
|---|---|---|---|
| `REQ-001` | `POST /api/workorders` | TASK-007 | TASK-010 |
| `REQ-002` | Approved auth/RBAC policy | TASK-001, TASK-007 | TASK-010 |
| `REQ-003` | `equipmentId` required/non-blank/max 50 | TASK-004 | TASK-009, TASK-010 |
| `REQ-004` | `priority` enum | TASK-004 | TASK-009, TASK-010 |
| `REQ-005` | ID pattern and generation | TASK-003, TASK-006 | TASK-009, TASK-011 |
| `REQ-006` | Database uniqueness | TASK-005 | TASK-011 |
| `REQ-007` | `status = Open` | TASK-003, TASK-006 | TASK-009, TASK-010 |
| `REQ-008` | UTC server clock | TASK-006 | TASK-009, TASK-011 |
| `REQ-009` | Persisted Work Order | TASK-005, TASK-006 | TASK-011 |
| `REQ-010` | `201` and response schema | TASK-007 | TASK-010 |
| `REQ-011` | `400` validation errors | TASK-008 | TASK-010 |
| `REQ-012` | RFC 7807/problem+json | TASK-008 | TASK-010 |
| `REQ-013` | No client override/server fields | TASK-004, TASK-007 | TASK-010 |
| `REQ-014` | Existing behavior preserved | TASK-012 | Existing test suite |
| `REQ-015` | Security/injection/secret rules | TASK-001, TASK-002, TASK-007 | TASK-010, TASK-011 |

## 6. File change allowlist

### 6.1 Feature files

- `src/main/java/com/add2numweb/workorder/**`
- `src/main/java/com/add2numweb/exception/**`
- `src/test/java/com/add2numweb/workorder/**`
- `src/test/java/com/add2numweb/exception/**`

### 6.2 Conditional configuration files

- `pom.xml`
- `src/main/resources/application.properties`
- `src/test/resources/application.properties`
- `src/main/resources/db/migration/**`
- `src/test/resources/**` theo database/security decision

### 6.3 Protected existing files

- `src/main/java/com/add2numweb/Add2NumController.java`
- `src/main/java/com/add2numweb/service/BigNumberService.java`
- `src/main/java/com/add2numweb/dto/CalculationResult.java`
- `src/test/java/com/add2numweb/Add2NumControllerTest.java`
- `src/main/resources/templates/index.html`
- `libs/Add2Num-0.0.1.jar`
- `Add2Num/src/**`

## 7. Implementation gate

**Current decision: `BLOCKED`.**

Chỉ được bắt đầu TASK-002 sau khi TASK-001 có đủ approval cho persistence và
security. Không được dùng plan này như prompt để tự chọn công nghệ.

Khi tất cả blocker được giải quyết, thực thi từng task độc lập, viết test trước,
chạy lệnh verification tương ứng và cập nhật traceability sau mỗi task.
