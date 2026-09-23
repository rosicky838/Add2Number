# Prompt tạo chiến lược kiểm thử - Create Work Order

Sử dụng prompt này trước khi triển khai test để tạo test plan có truy vết đầy đủ.

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

## Kết quả bắt buộc

Test plan phải có ma trận `Requirement ID -> TEST-ID`, nêu rõ test nào kiểm tra
contract, test nào kiểm tra business logic, test nào kiểm tra persistence và
test nào kiểm tra bảo mật. Những test phụ thuộc vào quyết định chưa phê duyệt
phải được đánh dấu `[BLOCKED]`, không được tự chọn công nghệ thay thế.
