# Java Coding & Logging Rules (WorkOrder Module)

Bộ quy tắc nền tảng áp dụng cho toàn bộ dự án Java / Spring Boot nhằm chuẩn hóa mã nguồn, tối ưu hiệu năng và ngăn chặn ảo giác (hallucination) từ các công cụ AI hỗ trợ sinh mã (GitHub Copilot).

---

## 1. Language & Framework Versions
- **Quy tắc:** Bắt buộc sử dụng cú pháp chuẩn **Java 17+** (LTS) và **Spring Boot 3.2+** / **3.3+**. Tận dụng các tính năng hiện đại như Records, Pattern Matching, Text Blocks, `var` khi thích hợp.
- **[GOOD]:**
  ```java
  public record WorkOrderResponse(String id, String title, Instant createdAt) {}
  ```
- **[BAD]:**
  ```java
  // Sử dụng boilerplate code cũ hoặc cú pháp Java 8
  public class WorkOrderResponse {
      private String id;
      // getters & setters thủ công...
  }
  ```

---

## 2. Naming Conventions (Quy ước đặt tên)
- **Quy tắc:**
  - Class, Record, Interface, Enum: `PascalCase` (vd: `WorkOrderController`, `PriorityLevel`).
  - Method, Variable, Parameter: `camelCase` (vd: `calculateTotal()`, `equipmentId`).
  - Constants: `UPPER_SNAKE_CASE` (vd: `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE`).
- **[GOOD]:**
  ```java
  public static final int MAX_BUFFER_SIZE = 1024;
  public void processWorkOrder(String workOrderId) { ... }
  ```
- **[BAD]:**
  ```java
  public static final int max_buffer_size = 1024;
  public void Process_work_order(String WorkOrderId) { ... }
  ```

---

## 3. Exception Handling (Xử lý ngoại lệ)
- **Quy tắc:**
  - **TUYỆT ĐỐI KHÔNG** ném ngoại lệ chung chung như `throw new RuntimeException(...)` hoặc `throw new Exception(...)`.
  - Sử dụng các ngoại lệ chuẩn có ngữ nghĩa (vd: `IllegalArgumentException`, `IllegalStateException`) hoặc Custom Business Exception (vd: `WorkOrderNotFoundException`).
  - Thông báo lỗi phải cụ thể, nêu rõ giá trị hoặc vị trí gây lỗi nếu có.
- **[GOOD]:**
  ```java
  if (num == null) {
      throw new IllegalArgumentException("Input string 'num' cannot be null.");
  }
  ```
- **[BAD]:**
  ```java
  if (num == null) {
      throw new RuntimeException("Error!");
  }
  ```

---

## 4. Logging Standards (Chuẩn ghi log & Bảo vệ dữ liệu)
- **Quy tắc:**
  - Sử dụng SLF4J logger được khởi tạo: `private static final Logger log = LoggerFactory.getLogger(ClassName.class);` (hoặc Lombok `@Slf4j`).
  - **NGHIÊM CẤM** ghi log các dữ liệu nhạy cảm hoặc thông tin định danh cá nhân (PII - Personally Identifiable Information) như: mật khẩu thô, token, số thẻ, số CMND/CCCD, email, số điện thoại cá nhân.
  - Sử dụng định dạng tham số `{}` thay vì cộng chuỗi `+` trong log.
- **[GOOD]:**
  ```java
  log.info("Processing work order id: {}, equipmentId: {}", id, equipmentId);
  ```
- **[BAD]:**
  ```java
  log.info("User logged in with password: " + rawPassword + " and email: " + email);
  ```

---

## 5. Dependency Injection (Tiêm phụ thuộc)
- **Quy tắc:**
  - **LUÔN LUÔN** sử dụng Constructor Injection với các trường `private final`.
  - **CẤM** sử dụng Field Injection (`@Autowired` trực tiếp trên field) vì gây khó khăn khi viết Unit Test và che giấu sự phụ thuộc vòng.
- **[GOOD]:**
  ```java
  @Service
  public class WorkOrderService {
      private final WorkOrderRepository repository;

      public WorkOrderService(WorkOrderRepository repository) {
          this.repository = repository;
      }
  }
  ```
- **[BAD]:**
  ```java
  @Service
  public class WorkOrderService {
      @Autowired
      private WorkOrderRepository repository; // Field Injection bị cấm
  }
  ```

---

## 6. Code Simplicity & Vertical Slice (Đơn giản hóa mã nguồn)
- **Quy tắc:**
  - Viết mã nguồn phẳng (flat architecture), tránh over-engineering, không lạm dụng các design pattern phức tạp (AbstractFactory, Bridge...) khi nghiệp vụ đơn giản.
  - Mỗi Service/Controller nên tập trung xử lý một nghiệp vụ rõ ràng, dễ bảo trì.

---

## 7. Variable Declaration Scope Outside Loops (Phạm vi khai báo biến ngoài vòng lặp)
- **Quy tắc:**
  - **CẤM** khai báo biến bên trong thân vòng lặp (`for`, `while`, `do-while`).
  - Toàn bộ các biến chạy (loop counter), con trỏ (index/pointer), biến tạm (temporary/buffer variable) phải được khai báo **BÊN NGOÀI** khối vòng lặp.
  - **Mục tiêu:** Tránh cấp phát bộ nhớ lặp đi lặp lại, tối ưu hiệu năng bộ nhớ đệm (stack/register allocation) và đảm bảo kiểm soát phạm vi biến rõ ràng.
- **[GOOD]:**
  ```java
  // Khai báo biến bên ngoài vòng lặp
  String item = "";
  for (int i = 0; i < list.size(); i++) {
      item = list.get(i);
      process(item);
  }
  ```
- **[BAD]:**
  ```java
  for (int i = 0; i < list.size(); i++) {
      String item = list.get(i); // Khai báo biến lặp lại trong thân loop
      process(item);
  }
  ```

---

## 8. Immutability & DTO Records (Bất biến & Bản ghi DTO)
- **Quy tắc:**
  - Sử dụng Java 17 `record` cho tất cả Request DTO và Response DTO.
  - Bản ghi (Record) đảm bảo tính bất biến (immutable), tự động sinh `equals`, `hashCode`, `toString` và loại bỏ hoàn toàn boilerplate.
- **[GOOD]:**
  ```java
  public record CreateWorkOrderRequest(
      @NotBlank String title,
      String description,
      @NotNull Long equipmentId,
      @NotBlank String priority
  ) {}
  ```
- **[BAD]:**
  ```java
  public class CreateWorkOrderRequest {
      private String title;
      // mutable getters/setters...
  }
  ```

---

## 9. Collection Handling (Xử lý tập hợp)
- **Quy tắc:**
  - Luôn sử dụng `collection.isEmpty()` thay vì `collection.size() == 0`.
  - Phương thức trả về Collection không được trả về `null`; hãy trả về `Collections.emptyList()`, `Collections.emptySet()` hoặc `List.of()`.
- **[GOOD]:**
  ```java
  if (items.isEmpty()) {
      return Collections.emptyList();
  }
  ```
- **[BAD]:**
  ```java
  if (items.size() == 0) {
      return null;
  }
  ```

---

## 10. Null Safety & Optional
- **Quy tắc:**
  - Sử dụng `Optional<T>` làm kiểu trả về cho các phương thức tìm kiếm có thể không có kết quả.
  - Không truyền `Optional` làm tham số hàm và không khai báo `Optional` làm thuộc tính của entity/record.
- **[GOOD]:**
  ```java
  public Optional<WorkOrder> findById(String id) { ... }
  ```
- **[BAD]:**
  ```java
  public WorkOrder findById(String id) {
      return null; // Dễ gây NullPointerException ở caller
  }
  ```

---

## 11. Resource Management (Quản lý tài nguyên)
- **Quy tắc:**
  - Bắt buộc sử dụng `try-with-resources` với mọi tài nguyên hiện thực `AutoCloseable` (Streams, DB Connections, Sockets, Readers/Writers).
- **[GOOD]:**
  ```java
  try (BufferedReader reader = Files.newBufferedReader(path)) {
      return reader.readLine();
  }
  ```
- **[BAD]:**
  ```java
  BufferedReader reader = Files.newBufferedReader(path);
  String line = reader.readLine();
  reader.close(); // Sẽ rò rỉ nếu ném ngoại lệ trước đó
  ```

---

## 12. Pure Utility Functions & Mảng Ký Tự (Tối ưu thuật toán chuỗi lớn)
- **Quy tắc:**
  - Các hàm tính toán thuật toán phụ trợ (như `sum(String, String)`) phải là **Pure Function** (hàm thuần túy, không có side-effects, không phụ thuộc trạng thái ngoài).
  - Đối với bài toán xử lý chuỗi ký tự số lớn:
    1. **Không sử dụng `reverse()`** (như `StringBuilder.reverse()`) vì tạo thêm bản sao chuỗi trong bộ nhớ.
    2. Sử dụng mảng ký tự nguyên thủy `char[]` để ghi trực tiếp kết quả từ phải sang trái (cuối lên đầu).
    3. Chỉ chuyển đổi từ `char[]` sang `String` **1 lần duy nhất** ở lệnh return cuối cùng.
    4. Kiểm tra hợp lệ (validate) ký tự số trực tiếp trong vòng lặp tính toán duy nhất, không dùng vòng lặp riêng biệt.
- **[GOOD]:**
  ```java
  char[] result = new char[maxLen + 1];
  // Duyệt và validate trực tiếp, ghi vào result từ cuối
  return new String(result, startIdx, length);
  ```
- **[BAD]:**
  ```java
  StringBuilder sb = new StringBuilder();
  // ... sb.append(...)
  return sb.reverse().toString(); // Tốn chi phí cấp phát và reverse
  ```
