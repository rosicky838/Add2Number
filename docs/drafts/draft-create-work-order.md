# Draft Code Generation Guide - Create Work Order

## 1. Ngữ cảnh (Context)

### 1.1 Tính năng

Implement tính năng **Tạo mới Đơn công việc bảo trì (Create Work Order)** cho ứng dụng Add2Number.

### 1.2 Repository context đã kiểm tra

- Root project: `C:\Add2Number`
- Framework: Spring Boot `3.2.0`
- Java: `17`
- Package gốc hiện tại: `com.add2numweb`
- Controller hiện tại: `src/main/java/com/add2numweb/Add2NumController.java`
- Service hiện tại: `src/main/java/com/add2numweb/service/BigNumberService.java`
- Test hiện tại: `src/test/java/com/add2numweb/Add2NumControllerTest.java`
- Dependency web và test đã có trong `pom.xml`.
- Chưa có Spring Data JPA, database driver, entity Work Order hoặc repository persistence.

### 1.3 Artifact nguồn sự thật

Trước khi sinh code, đọc bắt buộc:

1. `docs/domain-model.md`
2. `docs/api-spec.md`
3. `docs/coding-rules.md`
4. `docs/api-rules.md`
5. `docs/security-rules.md`
6. `pom.xml`

Không suy diễn khác với các artifact trên. Nếu có mâu thuẫn, dừng và báo cáo mâu thuẫn trước khi sửa code.

### 1.4 Persistence boundary

Business Requirement yêu cầu lưu Database nhưng repository chưa chọn database technology.

`[BLOCKING CLARIFICATION]` Phải xác nhận:

- Database engine.
- Spring Data JPA, JDBC hay công nghệ khác.
- Tên bảng và migration tool.
- Connection configuration cho local/test.
- Chính sách unknown JSON fields.
- Cơ chế authentication/RBAC.
- Retry limit khi ID `WO-` bị collision.

Không tự thêm database driver hoặc connection secret trước khi các quyết định này được phê duyệt.

## 2. Quy tắc tuân thủ (Rules)

### 2.1 API and schema

1. Endpoint phải là `POST /api/workorders`.
2. Không thêm field ngoài contract: `equipmentId`, `priority` trong request; `id`, `equipmentId`, `priority`, `status`, `createdAt` trong response.
3. Không cho client gửi hoặc ghi đè `id`, `status`, `createdAt`.
4. Success phải trả `201 Created` và `application/json`.
5. Lỗi validation phải trả `400 Bad Request` và `application/problem+json`.
6. Error body phải tuân RFC 7807 với `type`, `title`, `status`, `detail`, `instance`.

### 2.2 Java/Spring conventions

1. Dùng Java 17 và Spring Boot 3.2 conventions hiện có.
2. Dùng Constructor Injection với dependency `private final`.
3. Dùng `record` cho request/response DTO.
4. Dùng `@Valid` tại `@RequestBody`.
5. Dùng Jakarta Validation rõ ràng: `@NotBlank`, `@Size(max = 50)` và validation enum cho `priority`.
6. Không dùng `Map<String, Object>` làm contract response.
7. Không dùng `RuntimeException` chung chung; dùng exception có ngữ nghĩa.
8. Không bắt lỗi rộng rồi trả response thành công giả.
9. Không tạo field, endpoint, database table hoặc business rule ngoài đặc tả.
10. Không dùng field injection.

### 2.3 Security and logging

1. Endpoint phải được bảo vệ bằng RBAC cho role `TECHNICIAN`, theo security configuration được phê duyệt.
2. Không hardcode secret hoặc database credential.
3. Không nối chuỗi input để tạo SQL/JPQL.
4. Không log raw request body hoặc dữ liệu không cần thiết.
5. Không trả stack trace cho client.

### 2.4 Deterministic ID and time

1. ID phải khớp `^WO-[0-9]{5}$`.
2. `createdAt` phải lấy từ server clock dưới dạng UTC `Instant`.
3. Nếu cần test thời gian, inject `Clock` thay vì gọi trực tiếp `Instant.now()` trong business logic.
4. ID collision phải được kiểm tra trước khi save.
5. Retry limit phải lấy từ quyết định đã phê duyệt, không tự chọn khi chưa rõ.

## 3. Luồng xử lý (Business Logic)

### 3.1 Luồng thành công

1. `WorkOrderController` nhận `POST /api/workorders` với `@Valid CreateWorkOrderRequest`.
2. Spring/Jakarta Validation kiểm tra `equipmentId` không blank và không quá 50 ký tự.
3. Validation kiểm tra `priority` có đúng một trong `LOW`, `MEDIUM`, `HIGH`.
4. `WorkOrderController` chuyển request hợp lệ cho `WorkOrderService`.
5. `WorkOrderService` chuẩn hóa `equipmentId` theo quy ước trim đã được phê duyệt.
6. `WorkOrderService` sinh ID theo mẫu `WO-` + 5 chữ số.
7. `WorkOrderService` gọi repository để kiểm tra ID đã tồn tại chưa.
8. Nếu ID bị collision, service retry theo retry limit đã cấu hình.
9. Service gán `status = Open`.
10. Service lấy `createdAt` từ injected UTC clock.
11. Service tạo Work Order entity/domain object.
12. Service lưu object qua repository.
13. Service map object đã lưu sang `WorkOrderResponse`.
14. Controller trả `201 Created`, JSON response và `Location` nếu convention đã được phê duyệt.

### 3.2 Luồng validation lỗi

1. Nhận request.
2. Chạy validation trước khi gọi service.
3. Xác định field lỗi và lý do lỗi.
4. Tạo RFC 7807 Problem Details.
5. Gán `status = 400`, `instance = /api/workorders`.
6. Không sinh ID.
7. Không gọi repository save.
8. Trả `application/problem+json`.

### 3.3 Luồng persistence failure

1. Nếu database save thất bại, không trả `201`.
2. Ghi log server-side với correlation context phù hợp, không ghi secret.
3. Map lỗi theo error policy đã phê duyệt.
4. Không trả stack trace hoặc chi tiết connection/database cho client.

## 4. Xử lý lỗi (Error Handling)

| Điều kiện lỗi | Hành động xử lý | Mã trả về | Content-Type |
|---|---|---:|---|
| Thiếu `equipmentId` | Tạo Problem Details, `detail` nêu `equipmentId` bắt buộc | `400` | `application/problem+json` |
| `equipmentId` rỗng/whitespace | Tạo Problem Details, nêu field không được blank | `400` | `application/problem+json` |
| `equipmentId` dài hơn 50 ký tự | Tạo Problem Details, nêu giới hạn 50 ký tự | `400` | `application/problem+json` |
| Thiếu `priority` | Tạo Problem Details, nêu `priority` bắt buộc | `400` | `application/problem+json` |
| `priority` không thuộc `LOW`, `MEDIUM`, `HIGH` | Tạo Problem Details, nêu danh sách giá trị hợp lệ | `400` | `application/problem+json` |
| JSON malformed | Tạo Problem Details cho malformed request body | `400` | `application/problem+json` |
| Không có role `TECHNICIAN` | Áp dụng security handler theo cơ chế xác thực đã phê duyệt | `[NEEDS CLARIFICATION]` | `application/problem+json` |
| ID collision vượt retry limit | Không save; ghi log; trả lỗi server theo policy | `[NEEDS CLARIFICATION]` | `application/problem+json` |
| Database unavailable/save failure | Không trả thành công giả; ghi log và map lỗi theo policy | `[NEEDS CLARIFICATION]` | `application/problem+json` |

## 5. Data Contract

### 5.1 Request

| Field | Type | Required | Constraint |
|---|---|---:|---|
| `equipmentId` | `string` | Yes | Non-blank, max 50 chars |
| `priority` | `string` | Yes | `LOW` or `MEDIUM` or `HIGH` |

### 5.2 Response

| Field | Type | Source |
|---|---|---|
| `id` | `string` | Server-generated |
| `equipmentId` | `string` | Validated request |
| `priority` | `string` | Validated request |
| `status` | `string` | Server default `Open` |
| `createdAt` | `string` | Server UTC clock, ISO 8601 |

## 6. Danh sách File (Implementation Checklist)

### 6.1 File cần tạo

1. `src/main/java/com/add2numweb/workorder/WorkOrder.java`
2. `src/main/java/com/add2numweb/workorder/Priority.java`
3. `src/main/java/com/add2numweb/workorder/WorkOrderStatus.java`
4. `src/main/java/com/add2numweb/workorder/WorkOrderController.java`
5. `src/main/java/com/add2numweb/workorder/WorkOrderService.java`
6. `src/main/java/com/add2numweb/workorder/CreateWorkOrderRequest.java`
7. `src/main/java/com/add2numweb/workorder/WorkOrderResponse.java`
8. `src/main/java/com/add2numweb/workorder/WorkOrderRepository.java` - chỉ tạo sau khi persistence technology được phê duyệt.
9. `src/main/java/com/add2numweb/exception/GlobalExceptionHandler.java`
10. `src/main/java/com/add2numweb/exception/ProblemDetails.java`
11. `src/main/java/com/add2numweb/exception/WorkOrderIdGenerationException.java`
12. `src/test/java/com/add2numweb/workorder/WorkOrderServiceTest.java`
13. `src/test/java/com/add2numweb/workorder/WorkOrderControllerTest.java`
14. `src/test/java/com/add2numweb/exception/GlobalExceptionHandlerTest.java`

### 6.2 File có thể cần chỉnh sửa

1. `pom.xml` - thêm persistence dependency và database driver sau khi database được chọn.
2. `src/main/resources/application.properties` - chỉ thêm cấu hình không chứa secret; dùng environment variable cho credential.
3. `src/test/resources/application.properties` - cấu hình test database nếu cần.

### 6.3 Không được sửa nếu không có lý do được phê duyệt

1. `src/main/java/com/add2numweb/Add2NumController.java`
2. `src/main/java/com/add2numweb/service/BigNumberService.java`
3. `src/main/java/com/add2numweb/dto/CalculationResult.java`
4. Existing tests cho chức năng cộng số.

## 7. Test Checklist

1. Request hợp lệ với `equipmentId = EQ-10001`, `priority = HIGH` trả `201`.
2. Response có ID khớp `^WO-[0-9]{5}$`.
3. Response có `status = Open`.
4. Response có `createdAt` parse được thành ISO 8601 UTC.
5. Thiếu `equipmentId` trả `400` và `application/problem+json`.
6. `equipmentId` blank trả `400`.
7. `equipmentId` dài 51 ký tự trả `400`.
8. Thiếu `priority` trả `400`.
9. `priority = URGENT` trả `400`.
10. Request lỗi không gọi repository save.
11. Request không có role phù hợp bị từ chối theo security policy.
12. Existing tests của chức năng cộng hai số vẫn pass.

## 8. Prompt triển khai cho Copilot

```text
Đọc bắt buộc docs/domain-model.md, docs/api-spec.md, docs/coding-rules.md,
docs/api-rules.md, docs/security-rules.md và file pom.xml trước khi sửa code.

Implement chỉ tính năng Create Work Order theo đúng các artifact trên.
Chỉ sửa các file trong Implementation Checklist của draft này.
Không tự chọn database, retry policy, authentication mechanism hoặc thêm field
nếu các mục [NEEDS CLARIFICATION] chưa được phê duyệt.

Viết test trước, sau đó implement Controller/Service/DTO/Exception handling.
Đảm bảo POST /api/workorders trả 201 khi thành công và RFC 7807
application/problem+json với 400 khi validation thất bại.
Chạy mvnw.cmd test và báo cáo file đã đổi, test result, assumption và blocker.
```
