# Domain Model - Work Order

## 1. Phạm vi

Tài liệu này đặc tả model cho tính năng **Tạo mới Đơn công việc bảo trì (Create Work Order)**.

Project hiện tại:

- Package gốc: `com.add2numweb`
- Java: 17
- Spring Boot: 3.2.0
- Hiện chưa có entity Work Order.
- `pom.xml` hiện chưa khai báo Spring Data JPA, JDBC driver hoặc cấu hình database.

Vì Business Requirement yêu cầu lưu vào Database nhưng chưa chỉ rõ công nghệ database, loại database và tên bảng, các nội dung liên quan persistence phải được xác nhận trước khi implement.

## 2. Entity `WorkOrder`

### 2.1 Bảng dữ liệu logic

Tên bảng đề xuất: `work_orders`

> `[NEEDS CLARIFICATION]` Xác nhận tên database, tên bảng thực tế, cơ chế migration và chiến lược khóa chính trước khi thêm dependency persistence.

| Tên trường | Kiểu dữ liệu | Ràng buộc (Null/Unique/Length) | Mô tả |
|---|---|---|---|
| `id` | `String` | `NOT NULL`, `UNIQUE`, độ dài chính xác 8 ký tự theo mẫu `WO-` + 5 chữ số | Mã định danh đơn công việc do server sinh tự động, ví dụ `WO-10432`. |
| `equipmentId` | `String` | `NOT NULL`, độ dài từ 1 đến 50 ký tự | Mã thiết bị do client gửi. Không được để trống sau khi trim. |
| `priority` | `Priority`/`String` | `NOT NULL`, chỉ nhận `LOW`, `MEDIUM`, `HIGH` | Mức độ ưu tiên của đơn công việc. |
| `status` | `WorkOrderStatus`/`String` | `NOT NULL`, giá trị mặc định khi tạo là `Open` | Trạng thái ban đầu của đơn công việc. |
| `createdAt` | `Instant` | `NOT NULL` | Thời điểm tạo do server sinh, biểu diễn theo ISO 8601 UTC. |

### 2.2 Enum

| Tên enum | Giá trị hợp lệ | Quy tắc |
|---|---|---|
| `Priority` | `LOW`, `MEDIUM`, `HIGH` | Không nhận giá trị khác, không tự động ánh xạ giá trị không hợp lệ. |
| `WorkOrderStatus` | `Open` | Khi tạo mới luôn gán `Open`; client không được gửi hoặc ghi đè trường này. |

### 2.3 Invariant

1. `id` phải khớp regex `^WO-[0-9]{5}$`.
2. `id` phải duy nhất trong phạm vi database.
3. `equipmentId` phải là chuỗi không rỗng sau khi trim và không dài quá 50 ký tự.
4. `priority` phải thuộc chính xác một trong ba giá trị `LOW`, `MEDIUM`, `HIGH`.
5. `status` phải luôn là `Open` tại thời điểm tạo.
6. `createdAt` phải được lấy từ clock phía server, không lấy từ request body.
7. `createdAt` phải được lưu và trả về dưới dạng UTC.
8. Request không được phép cung cấp `id`, `status` hoặc `createdAt`.

## 3. Quy tắc sinh ID

1. Sinh một số nguyên ngẫu nhiên trong khoảng `00000` đến `99999`.
2. Định dạng số thành đúng 5 chữ số, có padding bằng `0` nếu cần.
3. Ghép tiền tố `WO-` với số đã định dạng.
4. Kiểm tra ID chưa tồn tại trong repository.
5. Nếu ID đã tồn tại, sinh lại trong giới hạn số lần retry được cấu hình.
6. Nếu vẫn không tạo được ID duy nhất, trả lỗi server và không ghi bản ghi không hoàn chỉnh.

> `[NEEDS CLARIFICATION]` Business Requirement chưa định nghĩa giới hạn retry, hành vi khi xảy ra collision và có chấp nhận dùng UUID thay thế hay không. Không được tự đổi format `WO-` + 5 chữ số nếu chưa có phê duyệt.

## 4. Mapping dự kiến trong Java

Các class dự kiến đặt dưới `src/main/java/com/add2numweb/workorder/`:

- `WorkOrder`
- `Priority`
- `WorkOrderStatus`
- `CreateWorkOrderRequest`
- `WorkOrderResponse`

Ưu tiên sử dụng Java 17 `record` cho request/response DTO theo `docs/coding-rules.md`. Entity persistence phải tuân theo công nghệ database được chọn.

