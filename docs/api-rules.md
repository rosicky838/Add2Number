# API Design Rules

Bộ quy tắc thiết kế RESTful API chuẩn mực nhằm đảm bảo tính nhất quán, bảo mật và nghiêm ngặt của giao diện dịch vụ trong hệ thống Spring Boot.

---

## 1. REST Resource Naming (Quy ước đặt tên tài nguyên)
- **Quy tắc:**
  - Sử dụng danh từ số nhiều (plural nouns) viết thường, định dạng kebab-case hoặc lowercase liên tục cho tên tài nguyên (vd: `/api/workorders` hoặc `/api/work-orders`).
  - Tuyệt đối không sử dụng danh từ số ít hoặc chứa động từ trong URI (vd: cấm `/api/workOrder`, `/api/createWorkOrder`, `/api/getWorkOrder`).
- **[GOOD]:**
  - `POST /api/workorders` (Tạo mới work order)
  - `GET /api/workorders` (Lấy danh sách work orders)
  - `GET /api/workorders/{id}` (Lấy chi tiết work order)
- **[BAD]:**
  - `POST /api/workOrder`
  - `POST /api/workorders/create`
  - `GET /api/getWorkOrders`

---

## 2. HTTP Verbs & Status Codes (Phương thức HTTP & Mã trạng thái)
- **Quy tắc:**
  - `POST`: Dùng để tạo mới tài nguyên. Trả về mã **`201 Created`** kèm URI tài nguyên hoặc đối tượng vừa tạo; trường hợp validation thất bại trả về **`400 Bad Request`**.
  - `GET`: Dùng để truy vấn dữ liệu (Idempotent, Safe). Trả về **`200 OK`** hoặc **`404 Not Found`**.
  - `PUT`: Cập nhật toàn bộ tài nguyên. Trả về **`200 OK`**.
  - `PATCH`: Cập nhật một phần tài nguyên. Trả về **`200 OK`**.
  - `DELETE`: Xóa tài nguyên. Trả về **`204 No Content`**.
- **[GOOD]:**
  ```java
  return ResponseEntity.status(HttpStatus.CREATED).body(createdResponse);
  ```
- **[BAD]:**
  ```java
  return ResponseEntity.ok(createdResponse); // Trả về 200 cho thao tác POST tạo mới là sai chuẩn
  ```

---

## 3. Strict Schema Conformance (Tuân thủ nghiêm ngặt lược đồ dữ liệu)
- **Quy tắc:**
  - **TUYỆT ĐỐI KHÔNG TỰ Ý SINH THÊM TRƯỜNG DỮ LIỆU (ZERO EXTRA FIELDS)** không có trong đặc tả kỹ thuật của Request hoặc Response.
  - Các công cụ AI (Copilot) thường có xu hướng "tưởng tượng" ra các trường như `createdBy`, `assignedTo`, `estimatedHours`, `notes`, `tags`... nếu không được yêu cầu rõ ràng. Việc sinh trường thừa sẽ làm vỡ hợp đồng API (API contract) giữa Frontend và Backend.
- **[GOOD]:**
  ```java
  // Yêu cầu chỉ định rõ 4 trường: title, description, equipmentId, priority
  public record CreateWorkOrderRequest(
      @NotBlank String title,
      String description,
      @NotNull Long equipmentId,
      @NotBlank String priority
  ) {}
  ```
- **[BAD]:**
  ```java
  // Tự ý thêm các trường ngoài yêu cầu
  public record CreateWorkOrderRequest(
      String title,
      String description,
      Long equipmentId,
      String priority,
      String createdBy,       // [VI PHẠM]: Trường thừa
      String internalNotes,   // [VI PHẠM]: Trường thừa
      Double estimatedCost    // [VI PHẠM]: Trường thừa
  ) {}
  ```

---

## 4. Error Responses — RFC 7807 Problem Details (Chuẩn phản hồi lỗi quốc tế)
- **Quy tắc:**
  - Mọi phản hồi lỗi (4xx, 5xx) **BẮT BUỘC** phải tuân theo đặc tả chuẩn **RFC 7807 (Problem Details for HTTP APIs)**.
  - Cấu trúc Problem Details bao gồm các trường chuẩn:
    - `type` (URI tham chiếu định danh loại lỗi, vd: `https://api.example.com/errors/bad-request`)
    - `title` (Tiêu đề ngắn gọn của lỗi, vd: `Bad Request`)
    - `status` (Mã HTTP status, vd: `400`)
    - `detail` (Mô tả chi tiết nguyên nhân gây ra lỗi cụ thể)
    - `instance` (URI của endpoint bị lỗi, vd: `/api/workorders`)
- **[GOOD]:**
  ```json
  {
    "type": "https://api.example.com/errors/bad-request",
    "title": "Bad Request",
    "status": 400,
    "detail": "Field 'title' must not be blank.",
    "instance": "/api/workorders"
  }
  ```
- **[BAD]:**
  ```json
  // Trả về chuỗi thô hoặc format tự chế
  {
    "error": "Title is required",
    "code": 1001
  }
  ```
  ```text
  // Hoặc trả về raw stack trace:
  java.lang.IllegalArgumentException: Title is required at com.example...
  ```

---

## 5. Input Validation (Xác thực dữ liệu đầu vào)
- **Quy tắc:**
  - Mọi payload trong `@RequestBody` phải được kiểm tra tính hợp lệ bằng annotation `@Valid` (Jakarta Validation).
  - Sử dụng các annotations ràng buộc rõ ràng: `@NotNull`, `@NotBlank`, `@Size(max = ...)`, `@Pattern`, `@Min`, `@Max`.
- **[GOOD]:**
  ```java
  @PostMapping("/api/workorders")
  public ResponseEntity<?> createWorkOrder(@Valid @RequestBody CreateWorkOrderRequest request) {
      // ...
  }
  ```
- **[BAD]:**
  ```java
  @PostMapping("/api/workorders")
  public ResponseEntity<?> createWorkOrder(@RequestBody CreateWorkOrderRequest request) {
      // Thiếu @Valid, dữ liệu không được kiểm tra tự động
  }
  ```

---

## 6. Consistent Envelopes & Response Format (Định dạng phản hồi nhất quán)
- **Quy tắc:**
  - Tất cả các API phản hồi đều định dạng `Content-Type: application/json` (hoặc `application/problem+json` cho lỗi).
  - Không bao giờ trả về dữ liệu kiểu plain-text hoặc HTML khi gọi REST endpoint.
