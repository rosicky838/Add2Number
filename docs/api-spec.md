# API Specification - Create Work Order

## 1. API Contract

| Thuộc tính | Giá trị |
|---|---|
| Tên API | Create Work Order |
| Method | `POST` |
| Endpoint | `/api/workorders` |
| Content-Type request | `application/json` |
| Content-Type success response | `application/json` |
| Content-Type error response | `application/problem+json` |
| Success status | `201 Created` |
| Validation error status | `400 Bad Request` |
| Resource access | Kỹ thuật viên có quyền tạo work order |

Endpoint sử dụng danh từ số nhiều theo `docs/api-rules.md`; không sử dụng `/create` trong URI.

## 2. Security and Headers

| Header | Bắt buộc | Giá trị | Xử lý khi thiếu/sai |
|---|---:|---|---|
| `Content-Type` | Có | `application/json` | Trả `400 Bad Request` hoặc status do Spring content negotiation quy định; response lỗi phải là `application/problem+json`. |
| `Accept` | Không | `application/json` | Mặc định trả JSON. |
| Thông tin xác thực | Có theo security configuration | Người gọi phải có role `TECHNICIAN` | Từ chối truy cập nếu không có quyền. |

> `[NEEDS CLARIFICATION]` BR chưa chỉ rõ cơ chế xác thực (JWT, session hay OAuth2) và status khi thiếu quyền. Quy tắc hiện tại yêu cầu RBAC; implementation không được tự thêm cơ chế xác thực mới.

## 3. Request

### 3.1 Body schema

| Tên trường | Kiểu JSON | Bắt buộc | Ràng buộc | Mô tả |
|---|---|---:|---|---|
| `equipmentId` | `string` | Có | Sau khi trim không rỗng; tối đa 50 ký tự | Mã thiết bị bị hỏng. |
| `priority` | `string` | Có | Chỉ nhận `LOW`, `MEDIUM`, `HIGH` | Mức độ ưu tiên. |

JSON hợp lệ:

```json
{
  "equipmentId": "EQ-10001",
  "priority": "HIGH"
}
```

Các trường `id`, `status`, `createdAt` không được phép xuất hiện trong request. Server phải bỏ qua hay từ chối unknown fields?

> `[NEEDS CLARIFICATION]` Xác nhận chính sách unknown fields. Khuyến nghị từ chối request có field ngoài contract để tránh client tưởng rằng các field đó có hiệu lực.

### 3.2 Validation rules

1. Nếu `equipmentId` bị thiếu, có giá trị `null`, rỗng hoặc chỉ chứa whitespace, request không hợp lệ.
2. Nếu độ dài `equipmentId` sau khi xử lý theo quy ước trim vượt quá 50 ký tự, request không hợp lệ.
3. Nếu `priority` bị thiếu hoặc có giá trị khác `LOW`, `MEDIUM`, `HIGH`, request không hợp lệ.
4. Validation phải chạy tại Controller Boundary bằng `@Valid` và Jakarta Validation.
5. Validation error phải trả `400`, không gọi Service và không ghi Database.
6. Không log toàn bộ request body nếu log có thể chứa dữ liệu không cần thiết.

## 4. Success Response

### 4.1 Response headers

| Header | Giá trị |
|---|---|
| `Content-Type` | `application/json` |
| `Location` | `/api/workorders/{id}` nếu API convention yêu cầu URI resource |

### 4.2 Response body

| Tên trường | Kiểu JSON | Bắt buộc | Mô tả |
|---|---|---:|---|
| `id` | `string` | Có | ID server sinh theo mẫu `WO-[0-9]{5}`. |
| `equipmentId` | `string` | Có | Mã thiết bị đã được lưu. |
| `priority` | `string` | Có | Một trong `LOW`, `MEDIUM`, `HIGH`. |
| `status` | `string` | Có | Luôn là `Open` khi tạo mới. |
| `createdAt` | `string` | Có | Timestamp ISO 8601 UTC, ví dụ `2026-09-23T07:30:00Z`. |

Ví dụ:

```json
{
  "id": "WO-10432",
  "equipmentId": "EQ-10001",
  "priority": "HIGH",
  "status": "Open",
  "createdAt": "2026-09-23T07:30:00Z"
}
```

## 5. Error Response - RFC 7807

Mọi lỗi phải có `Content-Type: application/problem+json` và không trả stack trace.

| Trường | Kiểu | Bắt buộc | Quy tắc |
|---|---|---:|---|
| `type` | `string` | Có | URI định danh loại lỗi, ví dụ `https://api.example.com/errors/bad-request`. |
| `title` | `string` | Có | `Bad Request`. |
| `status` | `integer` | Có | `400`. |
| `detail` | `string` | Có | Nêu chính xác field sai và lý do. |
| `instance` | `string` | Có | `/api/workorders`. |

Ví dụ thiếu `equipmentId`:

```json
{
  "type": "https://api.example.com/errors/bad-request",
  "title": "Bad Request",
  "status": 400,
  "detail": "Field 'equipmentId' is required and must not be blank.",
  "instance": "/api/workorders"
}
```

Ví dụ `priority` không hợp lệ:

```json
{
  "type": "https://api.example.com/errors/bad-request",
  "title": "Bad Request",
  "status": 400,
  "detail": "Field 'priority' must be one of: LOW, MEDIUM, HIGH.",
  "instance": "/api/workorders"
}
```

> `[NEEDS CLARIFICATION]` BR yêu cầu chỉ rõ trường lỗi nhưng chưa quy định khi nhiều field cùng sai. Khuyến nghị trả một response có `detail` liệt kê deterministic theo thứ tự `equipmentId`, sau đó `priority`, hoặc phê duyệt thêm extension `violations`.

## 6. Business Processing Contract

1. Nhận request JSON tại `POST /api/workorders`.
2. Chạy validation cho `equipmentId` và `priority`.
3. Nếu validation thất bại, tạo RFC 7807 response `400` và dừng xử lý.
4. Sinh ID theo quy tắc `WO-` + 5 chữ số và bảo đảm không trùng.
5. Gán `status = "Open"`.
6. Lấy thời gian hiện tại từ server clock dưới dạng `Instant` UTC.
7. Tạo domain/entity Work Order chỉ từ các field hợp lệ và các giá trị server sinh.
8. Lưu entity vào database thông qua repository.
9. Map entity đã lưu sang response DTO.
10. Trả HTTP `201 Created` với JSON response.

