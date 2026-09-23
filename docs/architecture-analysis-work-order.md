# Báo cáo phân tích kiến trúc - Create Work Order

**Vai trò:** Senior Java/Spring Architect  
**Phạm vi:** Phân tích repository trước khi triển khai `Create Work Order`  
**Trạng thái:** Chưa triển khai, không sửa mã nguồn và không viết implementation

## 1. Module và package hiện có

### 1.1 Cấu trúc module

Project hiện chưa phải Maven multi-module.

| Thành phần | Vai trò | Trạng thái |
|---|---|---|
| Root Maven project `Add2NumWeb` | Ứng dụng Spring Boot chính | Đang hoạt động |
| Thư mục `Add2Num/` | Thư viện Java xử lý số lớn | Project Eclipse riêng, được đóng gói thành JAR |
| `libs/Add2Num-0.0.1.jar` | Dependency nội bộ của ứng dụng web | Khai báo bằng `systemPath` |
| `src/main/java` | Mã nguồn Spring Boot | Đang sử dụng |
| `src/test/java` | Unit test và integration test hiện có | Đang sử dụng |
| `docs/` | Rules và đặc tả AI Context | Đang sử dụng |

`pom.xml` chỉ có một Maven project và không có phần `<modules>`. Vì vậy, `Add2Num/` hiện chưa phải Maven submodule của project web; đây là thư viện riêng được liên kết thông qua file JAR.

### 1.2 Package hiện có

| Package | Thành phần | Trách nhiệm |
|---|---|---|
| `com.add2numweb` | `Add2NumWebApplication` | Spring Boot entry point |
| `com.add2numweb` | `Add2NumController` | Web MVC controller và API tính toán |
| `com.add2numweb.dto` | `CalculationResult` | DTO kết quả phép tính |
| `com.add2numweb.service` | `BigNumberService` | Validate và thực hiện phép cộng số lớn |
| `main.java.com.add2num` | `MyBigNumber` từ JAR | Logic cộng chuỗi số lớn |

Package Work Order chưa tồn tại. Package đề xuất trong draft:

```text
com.add2numweb.workorder
```

## 2. Java và Spring version

Theo [pom.xml](../pom.xml):

| Thành phần | Phiên bản |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.0 |
| Spring Web | Theo Spring Boot parent 3.2.0 |
| Build tool | Maven |
| Application artifact | `Add2NumWeb:0.0.1` |

Theo [coding-rules.md](./coding-rules.md), code mới phải:

- Sử dụng Java 17+.
- Tương thích Spring Boot 3.2+.
- Ưu tiên `record` cho request/response DTO.
- Sử dụng Jakarta Validation.
- Dùng constructor injection.
- Không dùng field injection.

Không tự động nâng Spring Boot hoặc Java trong phạm vi triển khai Work Order nếu chưa có task riêng.

## 3. Dependency hiện có và dependency còn thiếu

### 3.1 Dependency hiện có

Theo [pom.xml](../pom.xml):

| Dependency | Mục đích |
|---|---|
| `spring-boot-starter-web` | Spring MVC, embedded server, JSON serialization |
| `spring-boot-starter-thymeleaf` | Giao diện HTML hiện tại |
| `com.add2num:Add2Num:0.0.1` | Thư viện cộng số lớn nội bộ |
| `spring-boot-starter-test` | JUnit, Spring Test, MockMvc và test utilities |

### 3.2 Dependency cần đánh giá

Không được thêm các dependency dưới đây trước khi có quyết định kiến trúc chính thức.

| Nhu cầu | Dependency có thể cần | Trạng thái |
|---|---|---|
| Bean validation | `spring-boot-starter-validation` | Cần đánh giá |
| Persistence bằng JPA | `spring-boot-starter-data-jpa` | Chưa được chọn |
| Persistence bằng JDBC | `spring-jdbc` hoặc starter tương ứng | Chưa được chọn |
| Database driver | Phụ thuộc database engine | Chưa được chọn |
| Database migration | Flyway hoặc Liquibase | Chưa được chọn |
| Authentication/RBAC | `spring-boot-starter-security` | Chưa được chọn |
| JWT/OAuth2 | Dependency theo identity provider | Chưa được chọn |
| Integration test database | H2, Testcontainers hoặc database test khác | Chưa được chọn |

Các dependency bắt buộc chưa thể xác định vì còn các quyết định về database engine, JPA/JDBC, migration tool, authentication mechanism, security provider và local/test database strategy.

## 4. Pattern Controller/Service/DTO/Test hiện tại

### 4.1 Controller

[Add2NumController.java](../src/main/java/com/add2numweb/Add2NumController.java) sử dụng:

- `@Controller`, không phải `@RestController`.
- Constructor injection cho `BigNumberService`.
- Thymeleaf endpoints `GET /` và `POST /calculate`.
- API endpoint `POST /api/calculate`.
- API response hiện tại dùng `Map<String, Object>`.
- Exception được bắt trực tiếp trong controller bằng `catch (IllegalArgumentException)`.

Pattern hiện tại không nên sao chép nguyên trạng cho Work Order vì API mới yêu cầu JSON body, DTO rõ schema, HTTP `201`, RFC 7807, `@Valid` và RBAC.

### 4.2 Service

[BigNumberService.java](../src/main/java/com/add2numweb/service/BigNumberService.java) sử dụng:

- `@Service`.
- Constructor injection.
- Dependency `private final`.
- Validation thủ công bằng `IllegalArgumentException`.
- Logging bằng SLF4J.
- Logic nghiệp vụ nằm trong Service.
- Trả về DTO `CalculationResult`.

Pattern `Controller -> Service` có thể tái sử dụng cho Work Order:

```text
WorkOrderController -> WorkOrderService -> WorkOrderRepository
```

### 4.3 DTO

[CalculationResult.java](../src/main/java/com/add2numweb/dto/CalculationResult.java) là class immutable với `private final` fields, constructor và getter thủ công. Theo coding rules, DTO mới nên dùng Java 17 `record`.

Không nên sửa `CalculationResult` chỉ để áp dụng pattern mới cho Work Order.

### 4.4 Test

[Add2NumControllerTest.java](../src/test/java/com/add2numweb/Add2NumControllerTest.java) sử dụng:

- `@SpringBootTest`.
- `@AutoConfigureMockMvc`.
- `MockMvc`.
- Kiểm tra view rendering.
- Kiểm tra form submission.
- Kiểm tra JSON bằng `jsonPath`.
- Test success và invalid input.

Pattern này có thể tham khảo cho `WorkOrderControllerTest`, nhưng Work Order cần JSON body, HTTP `201`, `400`, `application/problem+json`, RFC 7807 và security tests.

## 5. Cách xử lý exception và response hiện tại

### 5.1 Web form

Trong `Add2NumController`:

- `IllegalArgumentException` được bắt trực tiếp.
- Error được đưa vào model bằng `model.addAttribute("error", ...)`.
- HTTP status vẫn là `200 OK`.
- Server render lại view `index`.

Đây là hành vi phù hợp cho form HTML nhưng không phù hợp cho REST API.

### 5.2 API hiện tại

`POST /api/calculate` hiện:

- Trả `Map<String, Object>`.
- Success trả `success`, `num1`, `num2`, `result`, `steps`.
- Error vẫn trả HTTP `200`.
- Error trả `success = false` và `error`.

Work Order không nên tái sử dụng cách biểu diễn lỗi này. API mới phải dùng status HTTP phù hợp và RFC 7807 theo [api-rules.md](./api-rules.md).

Không sửa `/api/calculate` trong task Work Order nếu không có yêu cầu migration riêng.

## 6. Các file có thể tái sử dụng

| File | Cách sử dụng |
|---|---|
| [Add2NumWebApplication.java](../src/main/java/com/add2numweb/Add2NumWebApplication.java) | Giữ entry point và component scanning |
| [pom.xml](../pom.xml) | Cơ sở thêm dependency sau khi có quyết định |
| [Add2NumControllerTest.java](../src/test/java/com/add2numweb/Add2NumControllerTest.java) | Tham khảo MockMvc test setup |
| [BigNumberService.java](../src/main/java/com/add2numweb/service/BigNumberService.java) | Tham khảo constructor injection, service boundary và logging |
| [CalculationResult.java](../src/main/java/com/add2numweb/dto/CalculationResult.java) | Tham khảo immutable DTO; DTO mới nên dùng `record` |
| [api-rules.md](./api-rules.md) | Áp dụng REST naming, status code, schema và RFC 7807 |
| [coding-rules.md](./coding-rules.md) | Áp dụng Java/Spring conventions |
| [security-rules.md](./security-rules.md) | Áp dụng validation, RBAC, secret và injection rules |

Không nên tái sử dụng trực tiếp:

- `Add2NumController` cho Work Order API.
- `CalculationResult` làm Work Order response.
- `Map<String, Object>` làm Work Order response.
- Cách bắt `IllegalArgumentException` riêng trong từng controller.
- Cách trả HTTP `200` cho error.

## 7. Các file không được sửa nếu không cần thiết

Trong task Create Work Order, không nên sửa:

1. [Add2NumController.java](../src/main/java/com/add2numweb/Add2NumController.java) - chức năng cộng số và giao diện hiện tại.
2. [BigNumberService.java](../src/main/java/com/add2numweb/service/BigNumberService.java) - logic cộng số không liên quan.
3. [CalculationResult.java](../src/main/java/com/add2numweb/dto/CalculationResult.java) - DTO của feature cộng số.
4. [Add2NumControllerTest.java](../src/test/java/com/add2numweb/Add2NumControllerTest.java) - test hiện có của feature cộng số.
5. `src/main/resources/templates/index.html` - giao diện cộng số.
6. `libs/Add2Num-0.0.1.jar` - binary library hiện tại.
7. `Add2Num/src/` - thư viện cộng số độc lập.

Có thể sửa [pom.xml](../pom.xml) và `application.properties` khi dependency/configuration đã được phê duyệt.

## 8. Rủi ro khi thêm Work Order API

### RISK-001 - Chưa có persistence layer

Project chưa có JPA/JDBC, entity, repository hoặc database driver. Không thể implement yêu cầu lưu Database mà không chọn công nghệ.

### RISK-002 - Chưa có authentication/RBAC

Project chưa có Spring Security. Cơ chế xác thực và phân quyền Kỹ thuật viên chưa được quyết định.

### RISK-003 - Xung đột API error convention

API hiện tại dùng `success=false` và HTTP `200`, trong khi Work Order yêu cầu RFC 7807 và HTTP `400`. Sao chép pattern cũ sẽ tạo API sai contract.

### RISK-004 - ID chỉ có 100.000 khả năng

Format `WO-` + 5 chữ số có không gian ID hữu hạn. Cần uniqueness constraint và retry policy, đặc biệt với concurrent requests.

### RISK-005 - Unknown fields chưa có policy

Chưa chốt request có `id`, `status`, `createdAt` hoặc field khác sẽ bị bỏ qua hay từ chối.

### RISK-006 - Security sanitize quá rộng

[security-rules.md](./security-rules.md) yêu cầu sanitize, nhưng `equipmentId` chưa có allowlist ký tự cụ thể. Sanitize tùy ý có thể làm thay đổi mã hợp lệ.

### RISK-007 - Dependency chưa đủ

Không nên giả định Jakarta Validation, JPA hoặc Security đã có sẵn trong Spring Boot 3.2.0. Từng dependency phải được khai báo rõ.

### RISK-008 - Coding rule và code hiện tại khác nhau

[coding-rules.md](./coding-rules.md) cấm khai báo biến trong thân loop, nhưng [BigNumberService.java](../src/main/java/com/add2numweb/service/BigNumberService.java) hiện có biến trong phần `for` và thân loop. Đây là vấn đề pre-existing, không sửa trong Work Order task nếu không liên quan.

### RISK-009 - Ví dụ trong API rules không phải Work Order contract

[api-rules.md](./api-rules.md) có ví dụ chứa `title`, `description` và `equipmentId` kiểu `Long`, trong khi Work Order contract chỉ có `equipmentId` kiểu `String` và `priority`. Phải ưu tiên `api-spec.md` và `domain-model.md`.

### RISK-010 - Chưa phải multi-module Spring project

Nếu mục tiêu là “thêm submodule Spring”, cần xác định rõ là thêm feature vào ứng dụng hiện tại hay tách thành Maven submodule/deployable application riêng. Không tự chuyển project thành multi-module trong task API đơn lẻ.

## 9. Danh sách file dự kiến tạo/sửa

### 9.1 File dự kiến tạo

| File | Lý do |
|---|---|
| `src/main/java/com/add2numweb/workorder/WorkOrderController.java` | Expose `POST /api/workorders` |
| `src/main/java/com/add2numweb/workorder/WorkOrderService.java` | Điều phối business logic |
| `src/main/java/com/add2numweb/workorder/CreateWorkOrderRequest.java` | Request DTO và validation |
| `src/main/java/com/add2numweb/workorder/WorkOrderResponse.java` | Success response DTO |
| `src/main/java/com/add2numweb/workorder/Priority.java` | Enum `LOW`, `MEDIUM`, `HIGH` |
| `src/main/java/com/add2numweb/workorder/WorkOrderStatus.java` | Trạng thái `Open` |
| `src/main/java/com/add2numweb/workorder/WorkOrder.java` | Domain/entity model |
| `src/main/java/com/add2numweb/exception/ProblemDetails.java` | RFC 7807 response model nếu cần |
| `src/main/java/com/add2numweb/exception/GlobalExceptionHandler.java` | Map exception sang Problem Details |
| `src/test/java/com/add2numweb/workorder/WorkOrderControllerTest.java` | MockMvc/API contract tests |
| `src/test/java/com/add2numweb/workorder/WorkOrderServiceTest.java` | Unit tests business logic |
| `src/test/java/com/add2numweb/exception/GlobalExceptionHandlerTest.java` | RFC 7807 và validation tests |

### 9.2 File chỉ tạo sau khi persistence được chọn

| File | Điều kiện |
|---|---|
| `src/main/java/com/add2numweb/workorder/WorkOrderRepository.java` | Chọn JPA/JDBC |
| `src/main/resources/db/migration/...` | Chọn Flyway/Liquibase |
| `src/test/java/com/add2numweb/workorder/WorkOrderRepositoryTest.java` | Chọn database test |
| `src/test/resources/application.properties` | Có test database configuration |

### 9.3 File có thể sửa có điều kiện

| File | Điều kiện sửa |
|---|---|
| [pom.xml](../pom.xml) | Thêm validation/persistence/security/database dependency sau khi phê duyệt |
| `src/main/resources/application.properties` | Thêm cấu hình không chứa secret |
| `src/test/resources/application.properties` | Cấu hình test database/security nếu cần |

### 9.4 File không nên sửa

- [Add2NumController.java](../src/main/java/com/add2numweb/Add2NumController.java)
- [BigNumberService.java](../src/main/java/com/add2numweb/service/BigNumberService.java)
- [CalculationResult.java](../src/main/java/com/add2numweb/dto/CalculationResult.java)
- [Add2NumControllerTest.java](../src/test/java/com/add2numweb/Add2NumControllerTest.java)
- `src/main/resources/templates/index.html`
- `libs/Add2Num-0.0.1.jar`
- `Add2Num/src/`

## 10. Kết luận kiến trúc

Project có thể thêm Work Order như một vertical slice trong cùng ứng dụng Spring Boot:

```text
WorkOrderController
        |
        v
WorkOrderService
        |
        v
WorkOrderRepository
        |
        v
Database
```

Chỉ Controller/Service/Test pattern hiện tại có thể tham khảo ngay. Persistence và security chưa thể thiết kế chi tiết vì còn thiếu quyết định kiến trúc.

Không bắt đầu sinh mã nguồn Work Order cho đến khi chốt:

1. Database engine và persistence technology.
2. Migration tool.
3. Authentication/RBAC.
4. ID collision retry policy.
5. Unknown JSON fields policy.
6. RFC 7807 response cho nhiều validation errors.

