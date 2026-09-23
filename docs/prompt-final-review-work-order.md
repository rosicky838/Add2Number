# Prompt review cuối - Create Work Order

Sử dụng prompt này trước khi phát hành tính năng.

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

## Tiêu chí phát hành

Chỉ đề xuất `PASS` khi:

- Mọi requirement đều có source file và test tương ứng.
- Contract OpenAPI khớp implementation thực tế.
- Validation và RFC 7807 error response đã được kiểm thử.
- Persistence, security, collision handling và unknown-field policy đã được
  phê duyệt và triển khai đúng.
- Build, regression test và các test của Work Order đều đạt.
