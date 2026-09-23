# Traceability Review - Create Work Order

Ngày review: 2026-09-23  
Phạm vi: Phần 1 (Domain & Data) và Phần 2 (Business Logic & API Controller)

## 1. Ma trận truy vết

| Yêu cầu/Tính năng | File code triển khai | File test bao phủ |
|---|---|---|
| `POST /api/workorders` | `src/main/java/com/add2numweb/workorder/WorkOrderController.java` | `src/test/java/com/add2numweb/workorder/WorkOrderControllerTest.java` |
| Request chỉ gồm `equipmentId`, `priority` | `src/main/java/com/add2numweb/workorder/CreateWorkOrderRequest.java` | `WorkOrderControllerTest.java`, `WorkOrderServiceTest.java` |
| `equipmentId` bắt buộc, non-blank, tối đa 50 ký tự | `CreateWorkOrderRequest.java` với `@NotBlank`, `@Size(max = 50)` | `WorkOrderControllerTest.java` kiểm tra blank; chưa có test riêng cho thiếu/null/51 ký tự |
| `priority` chỉ nhận `LOW`, `MEDIUM`, `HIGH` | `CreateWorkOrderRequest.java` với `@Pattern`; `Priority.java` | `WorkOrderServiceTest.java`, `WorkOrderControllerTest.java` |
| Sinh ID dạng `WO-[0-9]{5}` | `WorkOrderService.java`, `WorkOrder.java` | `WorkOrderServiceTest.java`, `WorkOrderTest.java` |
| Retry ID collision tối đa 3 lần | `WorkOrderService.java` | `WorkOrderServiceTest.java` |
| Throw `WorkOrderIdGenerationException` sau 3 lần | `WorkOrderService.java`, `WorkOrderIdGenerationException.java` | `WorkOrderServiceTest.java` |
| `status = Open` do server gán | `WorkOrderService.java`, `WorkOrderStatus.java` | `WorkOrderServiceTest.java`, `WorkOrderTest.java`, `WorkOrderControllerTest.java` |
| `createdAt` do server sinh theo UTC | `WorkOrderService.java` dùng `Clock.systemUTC()`, `WorkOrder.java` dùng `Instant` | `WorkOrderServiceTest.java` |
| Lưu bằng PostgreSQL/Spring Data JPA | `WorkOrder.java`, `WorkOrderRepository.java`, `pom.xml` | `WorkOrderRepositoryTest.java` chỉ kiểm tra generic type; chưa có persistence integration test |
| Repository dùng `JpaRepository<WorkOrder, String>` | `WorkOrderRepository.java` | `WorkOrderRepositoryTest.java` |
| Thành công trả HTTP `201` và JSON response | `WorkOrderController.java`, `WorkOrderResponse.java` | `WorkOrderControllerTest.java` |
| Validation error trả HTTP `400` | `GlobalExceptionHandler.java` | `WorkOrderControllerTest.java` |
| Error response dùng `ProblemDetail`/RFC 7807 | `ProblemDetails.java`, `GlobalExceptionHandler.java` | `WorkOrderControllerTest.java` kiểm tra `type`, `title`, `status`, `detail`, `instance` |
| Content-Type lỗi `application/problem+json` | Spring `ProblemDetail` serialization và `GlobalExceptionHandler.java` | `WorkOrderControllerTest.java` |
| RBAC role `TECHNICIAN` | `WorkOrderController.java` với `@PreAuthorize`; `SecurityConfig.java` | `WorkOrderControllerTest.java` kiểm tra `401` và `403` |
| Regression chức năng cộng số | Không sửa các controller/service cũ | Existing test suite trong `src/test/java/com/add2numweb/` |

## 2. Kết quả kiểm thử hiện tại

Lệnh đã chạy:

```powershell
.\mvnw.cmd test
```

Kết quả:

```text
Tests run: 29
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

`git diff --check` cũng đạt.

## 3. Review bảo mật và rủi ro

### BLOCKING trước Production

| Mức độ | Vấn đề | Bằng chứng | Khuyến nghị |
|---|---|---|---|
| HIGH | Spring Security tự sinh mật khẩu development | Test log xuất hiện `Using generated security password` | Chốt và triển khai authentication provider thực tế, không dùng generated user/password mặc định. Secret phải lấy từ môi trường hoặc secret manager. |
| HIGH | Chưa có user store/identity provider thực tế | `SecurityConfig` chỉ bật HTTP Basic, không khai báo `UserDetailsService`, provider, JWT hoặc OAuth2 | Phê duyệt cơ chế xác thực production và kiểm thử với identity thật trước release. |
| HIGH | `anyRequest().permitAll()` làm các endpoint khác không có rule ở filter chain | `SecurityConfig.java` | Giới hạn rule theo endpoint; bảo đảm mọi endpoint nhạy cảm có authorization rõ ràng và không mở rộng ngoài phạm vi. |

### MAJOR cần xử lý trước Release Candidate

| Mức độ | Vấn đề | Bằng chứng | Khuyến nghị |
|---|---|---|---|
| MAJOR | Unknown fields chưa bị từ chối | Request DTO không cấu hình Jackson `FAIL_ON_UNKNOWN_PROPERTIES`; spec vẫn để `[NEEDS CLARIFICATION]` | Phê duyệt policy và thêm test với `id`, `status`, `createdAt` hoặc field lạ trong request. |
| MAJOR | Chưa có persistence integration test thực tế | `WorkOrderRepositoryTest` chỉ phản ánh generic interface, không save/read-back với H2/PostgreSQL | Thêm test migration, save/read-back, uniqueness và enum/timestamp mapping. |
| MAJOR | Chưa có `GlobalExceptionHandlerTest` như implementation checklist | Draft yêu cầu file test này nhưng file chưa tồn tại | Tạo test cho validation, malformed JSON, collision và persistence failure. |
| MAJOR | `DataIntegrityViolationException` nào cũng bị coi là ID collision | `WorkOrderService.java` retry mọi lỗi integrity | Chỉ retry khi nguyên nhân là duplicate ID; map các lỗi dữ liệu khác thành persistence error phù hợp. |

### MINOR / Non-blocking

| Mức độ | Vấn đề | Khuyến nghị |
|---|---|---|
| MINOR | Response chưa thiết lập `Location` header | Thêm khi API convention được phê duyệt; hiện spec ghi là tùy convention. |
| MINOR | `ProblemDetail.type` đang dùng mặc định `about:blank` | Nếu contract yêu cầu URI lỗi riêng như ví dụ trong spec, cấu hình type URI cụ thể và thêm assertion. |
| MINOR | Test mới kiểm tra blank/invalid priority nhưng chưa bao phủ đầy đủ null, missing, 51 ký tự và cả hai field lỗi | Bổ sung các boundary/negative cases theo test checklist. |
| MINOR | Security test dùng mock user, chưa kiểm chứng authentication provider thực tế | Bổ sung integration/security test sau khi identity mechanism được chốt. |

## 4. Khuyến nghị phát hành

### Quyết định: `BLOCKED`

Không nên phát hành production tại thời điểm review này dù toàn bộ 29 test hiện tại
đều xanh. Lý do chính là authentication production chưa hoàn chỉnh, đang xuất hiện
generated password, và persistence/error contract chưa có đủ integration evidence.

Có thể xem là đạt mức **development prototype / internal review** cho luồng cơ bản:

- Tạo request hợp lệ.
- Validation boundary.
- Sinh ID và retry cơ bản.
- Gán status/time server-side.
- HTTP status và RBAC method-level trong test.

Trước khi chuyển `PASS`, cần hoàn thành tối thiểu các mục HIGH và MAJOR ở trên,
chạy lại toàn bộ test, sau đó review lại traceability matrix.

## 6. Hotfix Release Candidate

Ngày cập nhật: 2026-09-23

Đã xử lý:

- Thay generated password bằng in-memory user cấu hình qua properties.
- Bảo vệ `/api/workorders` bằng `ROLE_TECHNICIAN`.
- Giới hạn các route legacy công khai và dùng `anyRequest().authenticated()`.
- Retry chỉ khi thông tin lỗi xác định được duplicate/primary-key của Work Order.
- Bật strict JSON deserialization để từ chối unknown fields.
- Bổ sung `GlobalExceptionHandlerTest` cho validation, malformed JSON và unknown fields.
- Bổ sung test xác thực in-memory technician bằng HTTP Basic.

Kết quả kiểm thử sau hotfix:

```text
Tests run: 33
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

### Quyết định sau hotfix

Nhãn **BLOCKED chưa được gỡ cho Production Release Candidate**. Các hotfix đã
loại bỏ generated password và rule `permitAll` rộng, nhưng in-memory user vẫn chỉ
là cơ chế tạm thời theo quyết định hiện tại. Cần thay bằng identity provider/
authentication mechanism production đã được phê duyệt và chạy integration test
với PostgreSQL thật hoặc môi trường tương đương trước khi kết luận `PASS`.

## 5. Hai lệnh Git đề xuất

Tạo nhánh mới:

```powershell
git switch -c feature/create-work-order-ai
```

Stage và commit với nội dung minh bạch việc sử dụng AI:

```powershell
git add pom.xml src docs/traceability-review-create-work-order.md; git commit -m "feat: add create work order API (AI-assisted)" -m "Implementation and tests were generated with AI assistance and reviewed against the approved API specification." -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Trước khi commit, cần xử lý các finding BLOCKING/MAJOR nếu mục tiêu là release
production thay vì chỉ lưu checkpoint development.
