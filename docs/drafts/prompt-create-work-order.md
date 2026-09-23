# Super Prompt - Create Work Order

> Prompt này dùng cho GitHub Copilot Chat hoặc LLM có quyền đọc repository Add2Number.
> Không chạy prompt triển khai cuối cùng cho đến khi các mục `[NEEDS CLARIFICATION]`
> trong tài liệu đặc tả đã được phê duyệt.

## 1. Prompt phân tích yêu cầu

```text
Bạn là Senior Product Analyst và AI Context Engineer.

Hãy đọc các file sau trong repository Add2Number:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md
- docs/coding-rules.md
- docs/api-rules.md
- docs/security-rules.md
- pom.xml

Mục tiêu tính năng:
Tạo API POST /api/workorders để Kỹ thuật viên tạo Work Order khi phát hiện
thiết bị bị hỏng.

Hãy tạo báo cáo phân tích, không viết mã nguồn, gồm:
1. Mục tiêu nghiệp vụ.
2. Tác nhân và quyền truy cập.
3. Request, response và dữ liệu server tự sinh.
4. Danh sách requirement có mã REQ-001, REQ-002...
5. Luồng thành công theo thứ tự từng bước.
6. Luồng validation lỗi.
7. Các dependency kỹ thuật cần thêm.
8. Các rủi ro khi tích hợp vào project hiện tại.
9. Ma trận requirement -> artifact -> test.
10. Danh sách toàn bộ câu hỏi [NEEDS CLARIFICATION].

Không được tự chọn database, migration tool, authentication mechanism hoặc retry
policy. Nếu thiếu thông tin, phải đánh dấu BLOCKING và dừng ở báo cáo.
```

## 2. Prompt kiểm tra chất lượng đặc tả

```text
Bạn là Requirements Quality Engineer.

Hãy review:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md

Kiểm tra cụ thể:
1. Request chỉ có equipmentId và priority.
2. equipmentId bắt buộc, non-blank, tối đa 50 ký tự.
3. priority chỉ nhận LOW, MEDIUM hoặc HIGH.
4. id có format WO-[0-9]{5}.
5. status mặc định là Open.
6. createdAt do server sinh ở UTC theo ISO 8601.
7. Success trả HTTP 201.
8. Validation error trả HTTP 400.
9. Error response có Content-Type application/problem+json và tuân RFC 7807.
10. Không có yêu cầu nào cho phép client ghi đè id, status hoặc createdAt.
11. Persistence, authentication, RBAC, retry collision và unknown fields đã được
    quyết định rõ hay chưa.

Kết quả phải gồm:
- Blocking issues.
- Non-blocking issues.
- Đề xuất chỉnh sửa.
- Ma trận requirement -> acceptance criteria -> test.

Không viết hoặc sửa mã nguồn.
```

## 3. Prompt phân tích repository

```text
Bạn là Senior Java/Spring Architect.

Hãy phân tích repository hiện tại trước khi triển khai Create Work Order.
Đọc:
- pom.xml
- src/main/java/com/add2numweb/Add2NumWebApplication.java
- src/main/java/com/add2numweb/Add2NumController.java
- src/main/java/com/add2numweb/service/BigNumberService.java
- src/test/java/com/add2numweb/Add2NumControllerTest.java
- docs/coding-rules.md
- docs/api-rules.md
- docs/security-rules.md

Báo cáo:
1. Module và package hiện có.
2. Java/Spring version.
3. Dependency đã có và dependency còn thiếu.
4. Pattern Controller/Service/DTO/Test đang sử dụng.
5. Cách xử lý exception và response hiện tại.
6. Các file có thể tái sử dụng.
7. Các file không được sửa nếu không cần thiết.
8. Rủi ro khi thêm Work Order API.
9. Danh sách file dự kiến tạo/sửa với lý do cho từng file.

Không sửa file và không viết mã nguồn.
```

## 4. Prompt thiết kế kiến trúc

```text
Bạn là Senior Spring Boot Architect.

Thiết kế kiến trúc tối thiểu cho Create Work Order dựa trên:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md
- repository analysis đã được phê duyệt

Phải mô tả:
1. Package và file placement dưới com.add2numweb.
2. Trách nhiệm của Controller, Service, Repository và Exception Handler.
3. Request/Response DTO.
4. Domain/entity model.
5. Jakarta Validation.
6. RFC 7807 Problem Details.
7. ID generation và collision handling.
8. UTC clock và khả năng test deterministic.
9. RBAC cho role TECHNICIAN.
10. Database mapping và migration.
11. Unit, MVC/API và integration test boundaries.
12. Dependency Maven cần thêm.
13. Cách chạy local và test.

Nếu database, authentication hoặc retry policy chưa được phê duyệt, hãy dừng
và liệt kê blocker thay vì tự chọn công nghệ.
Không tạo mã nguồn.
```

## 5. Prompt tạo OpenAPI contract

```text
Bạn là API Contract Designer.

Tạo openapi.yaml theo docs/api-spec.md cho:
POST /api/workorders

Contract bắt buộc có:
- OpenAPI 3.1.
- Request schema chỉ gồm equipmentId và priority.
- equipmentId: string, required, non-blank, maxLength 50.
- priority: enum [LOW, MEDIUM, HIGH], required.
- Success response 201.
- Response schema gồm id, equipmentId, priority, status, createdAt.
- id pattern ^WO-[0-9]{5}$.
- status mặc định Open.
- createdAt là ISO 8601 UTC.
- Error response 400 với application/problem+json.
- RFC 7807 fields: type, title, status, detail, instance.
- Ví dụ request, success response và từng lỗi validation.
- Liên kết operation với mã requirement tương ứng.

Không thêm field ngoài đặc tả. Không tạo mã Spring.
```

## 6. Prompt lập implementation plan

```text
Bạn là Senior Technical Lead.

Hãy tạo implementation plan cho Create Work Order dựa trên:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md
- openapi.yaml
- repository analysis
- architecture design

Chia thành các task độc lập, theo thứ tự:
1. Chốt persistence/security decisions.
2. Cập nhật dependency và configuration.
3. Tạo enum và domain/entity.
4. Tạo request/response DTO và validation.
5. Tạo repository.
6. Tạo service và ID generator.
7. Tạo Controller.
8. Tạo RFC 7807 exception handler.
9. Tạo unit tests.
10. Tạo MockMvc/API tests.
11. Tạo integration test với database test phù hợp.
12. Chạy regression test hiện có.
13. Review traceability và completeness.

Với mỗi task phải ghi:
- TASK-ID.
- Mục tiêu.
- Requirement IDs.
- File được phép tạo/sửa.
- Test-first step.
- Lệnh verification.
- Dependency.
- Risk.

Không implement plan.
```

## 7. Prompt triển khai từng task

```text
Bạn là Senior Java 17/Spring Boot 3.2 Engineer.

Implement duy nhất TASK-[ID] trong implementation plan đã được phê duyệt.

Đọc trước:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md
- docs/coding-rules.md
- docs/api-rules.md
- docs/security-rules.md
- openapi.yaml
- architecture design
- implementation plan

Quy tắc bắt buộc:
1. Viết hoặc cập nhật test trước khi implement.
2. Chỉ sửa các file được phép trong TASK-[ID].
3. Không sửa chức năng cộng số hiện có.
4. Không thêm field ngoài API contract.
5. Không tự chọn công nghệ cho mục [NEEDS CLARIFICATION].
6. Dùng Constructor Injection và field private final.
7. Dùng Java 17 và record cho request/response DTO.
8. Dùng @Valid và Jakarta Validation.
9. Trả 201 cho create success.
10. Trả application/problem+json và RFC 7807 cho lỗi.
11. Không trả stack trace.
12. Không hardcode secret/database credential.
13. Không nối chuỗi input để tạo truy vấn.
14. Không dùng Map<String, Object> làm API response.

Sau khi implement:
- Chạy .\mvnw.cmd test.
- Nếu test fail, sửa chỉ lỗi do TASK-[ID] gây ra.
- Báo cáo file đã thay đổi, test đã chạy, kết quả, giả định và blocker.
```

## 8. Prompt review sau mỗi task

```text
Bạn là Code Reviewer chuyên Java/Spring API.

Review thay đổi của TASK-[ID] bằng cách đối chiếu:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md
- openapi.yaml
- implementation plan
- git diff

Kiểm tra:
1. Requirement traceability.
2. API contract compliance.
3. equipmentId validation.
4. priority enum validation.
5. id format và uniqueness.
6. status Open.
7. createdAt UTC.
8. RFC 7807 và Content-Type.
9. HTTP status code.
10. Security/RBAC.
11. SQL/NoSQL injection.
12. Type safety.
13. Existing behavior preservation.
14. Test coverage.
15. Unnecessary changes.
16. Dependency/configuration correctness.

Trả về bảng:
| Severity | File | Line | Finding | Requirement | Recommended action |

Không tự sửa code.
```

## 9. Prompt tạo chiến lược kiểm thử

```text
Bạn là Senior Test Engineer.

Tạo test plan cho Create Work Order dựa trên docs/api-spec.md và
docs/domain-model.md.

Mỗi test phải có:
- TEST-ID.
- Requirement ID.
- Test layer: unit, MVC/API hoặc integration.
- Given/When/Then.
- Request input.
- Expected HTTP status.
- Expected Content-Type.
- Expected response fields.
- Boundary/negative case.

Bắt buộc bao phủ:
1. Request hợp lệ cho LOW, MEDIUM, HIGH.
2. equipmentId thiếu, null, blank và dài hơn 50 ký tự.
3. priority thiếu và giá trị ngoài enum.
4. Cả hai field cùng lỗi.
5. id khớp ^WO-[0-9]{5}$.
6. status bằng Open.
7. createdAt parse được là UTC ISO 8601.
8. Validation error không gọi save.
9. RFC 7807 fields đầy đủ.
10. Không có role phù hợp.
11. ID collision.
12. Database save failure.
13. Regression tests của Add2Num.

Không viết code trong bước này.
```

## 10. Prompt review cuối

```text
Bạn là Release Readiness Reviewer.

Review toàn bộ implementation Create Work Order bằng cách kiểm tra:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md
- openapi.yaml
- implementation plan
- toàn bộ git diff
- source code và test
- pom.xml
- application.properties

Trả về:
1. Ma trận requirement -> source file -> test.
2. Requirement chưa được triển khai.
3. API contract mismatch.
4. Validation/error handling issue.
5. Security issue.
6. Persistence/data integrity risk.
7. Build/runtime risk.
8. Backward compatibility risk.
9. Unnecessary complexity.
10. Blocking issues.
11. Non-blocking suggestions.
12. Release recommendation: PASS hoặc BLOCKED.

Không tự sửa code và không đánh dấu PASS nếu còn blocker.
```

## 11. Prompt thực thi đề xuất cho phiên hiện tại

Chỉ dùng prompt dưới đây sau khi đã trả lời các blocker về database, security,
retry collision và unknown fields:

```text
Hãy implement tính năng Create Work Order trong repository Add2Number.

Đọc và tuân thủ tuyệt đối:
- docs/domain-model.md
- docs/api-spec.md
- docs/drafts/draft-create-work-order.md
- docs/coding-rules.md
- docs/api-rules.md
- docs/security-rules.md
- pom.xml

Phạm vi:
- POST /api/workorders.
- Request: equipmentId và priority.
- equipmentId bắt buộc, non-blank, tối đa 50 ký tự.
- priority chỉ nhận LOW, MEDIUM, HIGH.
- Server sinh id theo ^WO-[0-9]{5}$.
- Server gán status = Open.
- Server gán createdAt là ISO 8601 UTC.
- Lưu Work Order vào database đã được phê duyệt.
- Thành công trả 201 application/json.
- Validation lỗi trả 400 application/problem+json theo RFC 7807.

Quy trình:
1. Trước tiên đọc repository và liệt kê file dự kiến thay đổi.
2. Nếu còn [NEEDS CLARIFICATION] có ảnh hưởng implementation, dừng và báo blocker.
3. Viết test trước.
4. Implement theo từng task nhỏ.
5. Chỉ sửa file thuộc implementation plan.
6. Chạy .\mvnw.cmd test.
7. Kiểm tra regression của chức năng cộng số.
8. Báo cáo file thay đổi, test result, coverage requirement, assumption và blocker.

Không tự thêm field, endpoint, database technology, security mechanism hoặc
business rule ngoài tài liệu.
```
