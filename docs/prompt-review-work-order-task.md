# Prompt review sau mỗi task - Create Work Order

Sử dụng prompt này sau khi hoàn thành một task implementation.

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

## Quy tắc kết luận

- Phân loại rõ `BLOCKING`, `MAJOR`, `MINOR` và `INFO`.
- Với mỗi finding, nêu bằng chứng cụ thể từ file và dòng liên quan.
- Không kết luận PASS nếu còn finding BLOCKING hoặc MAJOR chưa được xử lý.
