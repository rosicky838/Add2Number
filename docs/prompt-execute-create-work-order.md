# Prompt thực thi Create Work Order

Chỉ sử dụng prompt này sau khi các blocker về database, security, retry
collision và unknown fields đã được giải quyết và cập nhật vào các artifact
liên quan.

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

## Quy tắc dừng

Agent phải dừng trước khi sửa code nếu:

- Còn quyết định `[NEEDS CLARIFICATION]` ảnh hưởng đến implementation.
- File cần sửa không nằm trong implementation plan hoặc allowlist của task.
- Có mâu thuẫn giữa `docs/api-spec.md`, `openapi.yaml` và implementation plan.
- Không thể chạy test do thiếu dependency hoặc môi trường chưa được phê duyệt.

Khi dừng, phải báo rõ blocker, artifact liên quan và quyết định cần người dùng
phê duyệt; không tạo success-shaped fallback.
