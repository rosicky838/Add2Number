# Requirements Quality Gate - Create Work Order

**Ngày review:** 23/09/2026  
**Reviewer:** Requirements Quality Engineer  
**Phạm vi:** `domain-model.md`, `api-spec.md`, `draft-create-work-order.md`  
**Kết quả:** **BLOCKED - Chưa đủ điều kiện chuyển sang implementation**

## 1. Tóm tắt kết quả

Các yêu cầu API và validation cốt lõi đã được mô tả tương đối nhất quán:

- Endpoint là `POST /api/workorders`.
- Request chỉ có `equipmentId` và `priority`.
- `equipmentId` bắt buộc, non-blank, tối đa 50 ký tự.
- `priority` chỉ nhận `LOW`, `MEDIUM`, `HIGH`.
- Server sinh ID theo `^WO-[0-9]{5}$`.
- Server gán `status = Open`.
- Server sinh `createdAt` theo UTC ISO 8601.
- Success trả `201 Created`.
- Validation lỗi trả `400 Bad Request`.
- Error response yêu cầu `application/problem+json` và RFC 7807.
- Không có rule nào cho phép client ghi đè `id`, `status`, `createdAt`.

Tuy nhiên, chưa thể phê duyệt implementation vì các quyết định ảnh hưởng trực tiếp đến persistence, security, error contract và tính deterministic vẫn chưa được chốt.

## 2. Kết quả kiểm tra 11 tiêu chí

| # | Tiêu chí | Kết quả | Nhận xét |
|---:|---|---|---|
| 1 | Request chỉ có `equipmentId`, `priority` | PASS có điều kiện | Được nêu rõ trong `api-spec.md`; chính sách field lạ vẫn chưa chốt. |
| 2 | `equipmentId` bắt buộc, non-blank, tối đa 50 | PASS | Có trong domain model, API spec và draft. |
| 3 | `priority` chỉ `LOW`, `MEDIUM`, `HIGH` | PASS | Có enum và validation rule nhất quán. |
| 4 | ID có format `WO-[0-9]{5}` | PASS có điều kiện | Format rõ; uniqueness và retry khi collision chưa rõ. |
| 5 | `status` mặc định `Open` | PASS | Không cho client ghi đè. |
| 6 | `createdAt` server sinh, UTC ISO 8601 | PASS | Dùng `Instant`/server clock được nêu rõ. |
| 7 | Success HTTP `201` | PASS | Có trong API contract và processing contract. |
| 8 | Validation error HTTP `400` | PASS | Có trong API contract, validation rules và error table. |
| 9 | `application/problem+json` và RFC 7807 | PASS có điều kiện | Các field nền tảng đủ; format nhiều validation errors chưa chốt. |
| 10 | Không cho client ghi đè server fields | PASS có điều kiện | Cấm `id`, `status`, `createdAt`; hành vi khi field xuất hiện vẫn chưa chốt. |
| 11 | Persistence/auth/RBAC/retry/unknown fields đã quyết định | FAIL | Đây là nhóm blocker; nhiều mục vẫn `[NEEDS CLARIFICATION]`. |

## 3. Blocking issues

### BLOCK-001 - Chưa quyết định persistence

**Bằng chứng:** `domain-model.md` và draft vẫn yêu cầu xác nhận database engine, persistence technology, bảng, migration và connection configuration.

**Ảnh hưởng:**

- Không thể tạo `WorkOrder` entity đúng công nghệ.
- Không thể tạo `WorkOrderRepository`.
- Không thể thêm dependency hoặc test integration.
- Không thể xác nhận uniqueness của `id`.

**Quyết định cần có:**

1. Database engine.
2. Spring Data JPA, JDBC hay lựa chọn khác.
3. Tên bảng thực tế.
4. Chiến lược khóa chính.
5. Migration tool.
6. Cấu hình local/test.

### BLOCK-002 - Chưa quyết định authentication và RBAC

**Bằng chứng:** API yêu cầu role `TECHNICIAN`, security rules yêu cầu RBAC, nhưng cơ chế authentication và status authorization chưa được xác định.

**Ảnh hưởng:** Không thể implement hoặc test quyền truy cập một cách deterministic.

**Quyết định cần có:**

1. JWT, session hay OAuth2.
2. Identity provider.
3. Role được phép: chỉ `TECHNICIAN` hay thêm `ADMIN`.
4. Status khi chưa xác thực.
5. Status khi đã xác thực nhưng thiếu role.

### BLOCK-003 - Chưa quyết định retry khi ID collision

**Bằng chứng:** Domain model yêu cầu retry nhưng không nêu retry limit, error status hoặc error type.

**Ảnh hưởng:** Service có thể rơi vào vòng lặp vô hạn hoặc trả hành vi khác nhau giữa các implementation.

**Quyết định cần có:**

1. Số lần retry tối đa.
2. Cách sinh lại ID.
3. Status/error type khi vượt retry limit.
4. Có bắt buộc giữ format 5 chữ số trong mọi trường hợp hay không.
5. Cách đảm bảo uniqueness ở database trong race condition.

### BLOCK-004 - Chưa quyết định unknown fields

**Bằng chứng:** `api-spec.md` đặt câu hỏi server bỏ qua hay từ chối các field như `id`, `status`, `createdAt`.

**Ảnh hưởng:** Request cùng nội dung có thể cho kết quả khác nhau tùy Jackson configuration; contract chưa deterministic.

**Khuyến nghị:** Từ chối field ngoài schema với lỗi `400`, nhưng cần được product/architecture phê duyệt.

### BLOCK-005 - Chưa quyết định response khi nhiều field cùng lỗi

**Bằng chứng:** RFC 7807 base fields được định nghĩa, nhưng chưa quyết định có extension `violations` hay chỉ một `detail`.

**Ảnh hưởng:** Client không có contract ổn định để hiển thị nhiều lỗi validation.

**Quyết định cần có:**

1. Một `detail` duy nhất hay mảng lỗi.
2. Nếu dùng mảng, tên field extension và schema.
3. Thứ tự lỗi có deterministic không.
4. Quy tắc khi request vừa thiếu field vừa sai field.

## 4. Non-blocking issues

### NB-001 - `Location` header chưa bắt buộc

API spec ghi `Location` là “nếu API convention yêu cầu”. Nên chốt rõ có trả `Location: /api/workorders/{id}` hay không để test contract đầy đủ.

### NB-002 - Quy tắc trim chưa hoàn toàn rõ

Tài liệu nói `equipmentId` được trim để validation, nhưng chưa nêu response/database lưu giá trị trước hay sau trim. Nên chốt:

- Chuẩn hóa bằng trim trước khi lưu và trả response; hoặc
- Giữ nguyên chuỗi gốc nhưng validation trên giá trị đã trim.

Khuyến nghị lưu và trả giá trị đã trim.

### NB-003 - Quy tắc ký tự hợp lệ cho `equipmentId` chưa rõ

Security rules yêu cầu sanitize dữ liệu đầu vào, nhưng API spec chỉ quy định non-blank và max length. Cần xác định allowlist ký tự để tránh việc sanitize làm thay đổi mã thiết bị hợp lệ.

### NB-004 - Mâu thuẫn tiềm ẩn về role

API/draft nói role `TECHNICIAN`; ví dụ trong `security-rules.md` dùng `TECHNICIAN` hoặc `ADMIN`. Cần đồng bộ sau khi có quyết định RBAC.

### NB-005 - Error mapping cho persistence failure chưa có status

Draft yêu cầu không trả thành công giả nhưng chưa có status/error contract cho Database unavailable hoặc save failure. Đây là non-blocking đối với validation-only design, nhưng blocking trước production release.

### NB-006 - Correlation/request ID chưa được quyết định

API spec đề cập khả năng có `Location`, còn draft đề cập correlation context trong log; chưa có header hoặc format cụ thể.

## 5. Đề xuất chỉnh sửa artifact

Chưa sửa artifact trong lần review này. Sau khi các quyết định được phê duyệt, cập nhật như sau:

1. `docs/domain-model.md`
   - Ghi database engine, table name, persistence mapping và migration strategy.
   - Ghi retry limit, collision error và uniqueness strategy.
2. `docs/api-spec.md`
   - Chốt unknown-field policy.
   - Chốt multi-error schema.
   - Chốt auth/authorization status codes.
   - Chốt `Location` header.
   - Chốt trim/normalization và character allowlist.
3. `docs/drafts/draft-create-work-order.md`
   - Thay mọi `[NEEDS CLARIFICATION]` blocking bằng quyết định cụ thể.
   - Cập nhật file list theo persistence/security technology đã chọn.
   - Thêm test cases cho collision, authorization, unknown fields và multiple validation errors.

## 6. Ma trận requirement -> acceptance criteria -> test

| Requirement | Acceptance criteria | Test đề xuất | Trạng thái |
|---|---|---|---|
| `REQ-001` | `POST /api/workorders` được route đúng | MockMvc gửi đúng method/path | Ready |
| `REQ-002` | Actor có quyền được tạo; actor không có quyền bị từ chối | Security/API tests cho authenticated, unauthenticated và wrong-role | **Blocked - auth/RBAC** |
| `REQ-003` | Thiếu/null/blank `equipmentId` hoặc dài 51 ký tự trả `400` | Parameterized MockMvc validation tests | Ready |
| `REQ-004` | `LOW`, `MEDIUM`, `HIGH` hợp lệ; giá trị khác trả `400` | Enum validation tests | Ready |
| `REQ-005` | Response `id` khớp `^WO-[0-9]{5}$` | Service unit test và API contract test | Ready có điều kiện |
| `REQ-006` | Không có hai bản ghi cùng ID | Repository/database uniqueness integration test | **Blocked - persistence** |
| `REQ-007` | Mọi create success có `status = Open` | Assert response và persistence state | Ready |
| `REQ-008` | `createdAt` do server sinh, parse được ISO 8601 UTC | Injected Clock unit test và response test | Ready có điều kiện |
| `REQ-009` | Request hợp lệ được lưu và đọc lại từ Database | Persistence integration test | **Blocked - persistence** |
| `REQ-010` | Request hợp lệ trả `201`, JSON đủ 5 field | MockMvc/API contract test | Ready |
| `REQ-011` | Mọi validation failure trả `400`, không gọi save | Controller test với repository verify | Ready |
| `REQ-012` | Error response có đúng content type và `type/title/status/detail/instance` | RFC 7807 contract tests | Ready có điều kiện |
| `REQ-013` | Client không thể ghi đè `id/status/createdAt`; field lạ xử lý theo policy | Unknown-field and server-field injection tests | **Blocked - unknown fields** |
| `REQ-014` | Chức năng cộng số hiện tại vẫn pass | `.\mvnw.cmd test` và regression suite | Ready |
| `REQ-015` | Không có secret hardcode/injection; input boundary được bảo vệ | Static review, security tests, parameterized query review | Ready có điều kiện |

## 7. Quality gate decision

**Decision: BLOCKED**

Không được bắt đầu code generation hoặc thêm dependency cho Work Order cho đến khi hoàn tất tối thiểu năm quyết định blocking:

1. Persistence/database.
2. Authentication/RBAC.
3. ID collision retry.
4. Unknown fields.
5. Multiple validation error response.

Sau khi các quyết định được phê duyệt và cập nhật vào artifact, cần chạy lại quality gate này trước khi tạo implementation plan hoặc sinh mã nguồn.
