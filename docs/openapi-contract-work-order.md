# OpenAPI Contract - Create Work Order

## Trạng thái

Hợp đồng dưới đây được tạo theo [api-spec.md](./api-spec.md). Đây là contract
độc lập với implementation, không phải mã Spring.

Các điểm vẫn cần phê duyệt trước khi triển khai:

- Authentication mechanism và security scheme.
- Chính sách xử lý unknown JSON fields.
- Schema khi nhiều validation errors cùng lúc.
- Có bắt buộc trả `Location` header hay không.

## Requirement mapping

Operation `POST /api/workorders` được ánh xạ tới:

- `REQ-001`: Expose endpoint tạo Work Order.
- `REQ-002`: Chỉ actor có quyền phù hợp được tạo.
- `REQ-003`: Validate `equipmentId`.
- `REQ-004`: Validate `priority`.
- `REQ-005`: Server sinh ID theo format.
- `REQ-007`: Gán status `Open`.
- `REQ-008`: Gán `createdAt` UTC.
- `REQ-009`: Lưu Work Order.
- `REQ-010`: Trả `201 Created`.
- `REQ-011`: Trả `400` khi validation lỗi.
- `REQ-012`: Trả RFC 7807 Problem Details.
- `REQ-013`: Không cho client ghi đè server-managed fields.

## OpenAPI 3.1 document

```yaml
openapi: 3.1.0
info:
  title: Add2Num Work Order API
  version: 1.0.0
  description: |
    API tạo mới đơn công việc bảo trì khi Kỹ thuật viên phát hiện thiết bị bị hỏng.
    Contract này không định nghĩa authentication scheme vì cơ chế xác thực chưa
    được phê duyệt.

servers:
  - url: http://localhost:8080
    description: Local development server

paths:
  /api/workorders:
    post:
      operationId: createWorkOrder
      summary: Create a work order
      description: Tạo Work Order với trạng thái ban đầu là Open.
      x-requirement-ids:
        - REQ-001
        - REQ-002
        - REQ-003
        - REQ-004
        - REQ-005
        - REQ-007
        - REQ-008
        - REQ-009
        - REQ-010
        - REQ-011
        - REQ-012
        - REQ-013
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateWorkOrderRequest'
            examples:
              highPriority:
                summary: High priority work order
                value:
                  equipmentId: EQ-10001
                  priority: HIGH
      responses:
        '201':
          description: Work Order created successfully.
          headers:
            Location:
              description: URI của Work Order vừa tạo. Tính bắt buộc cần được phê duyệt.
              schema:
                type: string
                pattern: '^/api/workorders/WO-[0-9]{5}$'
                example: /api/workorders/WO-10432
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/WorkOrderResponse'
              examples:
                createdWorkOrder:
                  summary: Created work order
                  value:
                    id: WO-10432
                    equipmentId: EQ-10001
                    priority: HIGH
                    status: Open
                    createdAt: '2026-09-23T07:30:00Z'
        '400':
          description: Request validation or JSON syntax error.
          content:
            application/problem+json:
              schema:
                $ref: '#/components/schemas/ProblemDetails'
              examples:
                missingEquipmentId:
                  summary: Missing equipmentId
                  value:
                    type: https://api.example.com/errors/bad-request
                    title: Bad Request
                    status: 400
                    detail: "Field 'equipmentId' is required and must not be blank."
                    instance: /api/workorders
                equipmentIdTooLong:
                  summary: equipmentId exceeds maximum length
                  value:
                    type: https://api.example.com/errors/bad-request
                    title: Bad Request
                    status: 400
                    detail: "Field 'equipmentId' must not exceed 50 characters."
                    instance: /api/workorders
                missingPriority:
                  summary: Missing priority
                  value:
                    type: https://api.example.com/errors/bad-request
                    title: Bad Request
                    status: 400
                    detail: "Field 'priority' is required."
                    instance: /api/workorders
                invalidPriority:
                  summary: Invalid priority
                  value:
                    type: https://api.example.com/errors/bad-request
                    title: Bad Request
                    status: 400
                    detail: "Field 'priority' must be one of: LOW, MEDIUM, HIGH."
                    instance: /api/workorders
                malformedJson:
                  summary: Malformed JSON body
                  value:
                    type: https://api.example.com/errors/bad-request
                    title: Bad Request
                    status: 400
                    detail: Request body is not valid JSON.
                    instance: /api/workorders

components:
  schemas:
    CreateWorkOrderRequest:
      type: object
      additionalProperties: false
      required:
        - equipmentId
        - priority
      properties:
        equipmentId:
          type: string
          minLength: 1
          maxLength: 50
          pattern: '.*\S.*'
          description: Mã thiết bị bị hỏng, không được blank.
          example: EQ-10001
        priority:
          type: string
          enum:
            - LOW
            - MEDIUM
            - HIGH
          description: Mức độ ưu tiên của Work Order.
          example: HIGH

    WorkOrderResponse:
      type: object
      additionalProperties: false
      required:
        - id
        - equipmentId
        - priority
        - status
        - createdAt
      properties:
        id:
          type: string
          pattern: '^WO-[0-9]{5}$'
          minLength: 8
          maxLength: 8
          description: ID do server sinh theo tiền tố WO- và 5 chữ số.
          example: WO-10432
        equipmentId:
          type: string
          minLength: 1
          maxLength: 50
          description: Mã thiết bị đã được validate và lưu.
          example: EQ-10001
        priority:
          type: string
          enum:
            - LOW
            - MEDIUM
            - HIGH
          description: Mức độ ưu tiên đã được validate.
          example: HIGH
        status:
          type: string
          enum:
            - Open
          default: Open
          description: Trạng thái mặc định khi tạo mới.
          example: Open
        createdAt:
          type: string
          format: date-time
          description: Thời điểm server tạo bản ghi, biểu diễn theo ISO 8601 UTC.
          example: '2026-09-23T07:30:00Z'

    ProblemDetails:
      type: object
      additionalProperties: false
      required:
        - type
        - title
        - status
        - detail
        - instance
      properties:
        type:
          type: string
          format: uri
          description: URI định danh loại lỗi.
          example: https://api.example.com/errors/bad-request
        title:
          type: string
          description: Tiêu đề ngắn gọn của lỗi.
          example: Bad Request
        status:
          type: integer
          const: 400
          description: HTTP status code.
          example: 400
        detail:
          type: string
          description: Mô tả cụ thể field sai và lý do sai.
          example: "Field 'equipmentId' is required and must not be blank."
        instance:
          type: string
          description: URI của request gây lỗi.
          example: /api/workorders
```

## Contract constraints

1. Request schema chỉ có `equipmentId` và `priority`.
2. Response schema chỉ có `id`, `equipmentId`, `priority`, `status`, `createdAt`.
3. `additionalProperties: false` ngăn field ngoài contract ở mức OpenAPI schema.
4. `id`, `status` và `createdAt` chỉ xuất hiện trong response, không xuất hiện trong request.
5. `createdAt` dùng `format: date-time` và example UTC có hậu tố `Z`.
6. Lỗi validation dùng `application/problem+json`, không dùng response envelope `success: false`.
7. OpenAPI document không tự định nghĩa JWT, OAuth2 hoặc session security scheme.
