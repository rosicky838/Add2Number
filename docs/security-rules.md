# Security Rules

Bộ quy tắc an toàn bảo mật bắt buộc áp dụng cho toàn bộ các lớp Controller, Service và Data Access nhằm ngăn chặn các lỗ hổng bảo mật phổ biến (OWASP Top 10).

---

## 1. No Hardcoded Secrets (Cấm tuyệt đối bí mật trong mã nguồn)
- **Quy tắc:**
  - **CẤM HOÀN TOÀN** hardcode mật khẩu, API keys, database credentials, encryption keys, hoặc JWT tokens trong mã nguồn (`.java`), file cấu hình phiên bản (`application.properties` commit lên Git), hoặc trong code comments.
  - Sử dụng biến môi trường (Environment Variables) hoặc công cụ quản lý bí mật (HashiCorp Vault, AWS Secrets Manager, Kubernetes Secrets) để nạp giá trị khi triển khai.
- **[GOOD]:**
  ```java
  @Value("${app.jwt.secret}")
  private String jwtSecret;
  ```
- **[BAD]:**
  ```java
  private static final String API_KEY = "AKIA-SECRET-9988223311-PROD"; // Lộ secret trên Git
  ```

---

## 2. Input Sanitization & Boundary Validation (Kiểm tra ranh giới dữ liệu)
- **Quy tắc:**
  - Luôn kiểm tra, xác thực và làm sạch (sanitize) mọi dữ liệu đến từ client tại tầng Controller Boundary trước khi chuyển tiếp cho tầng Service.
  - Loại bỏ các ký tự đặc biệt nguy hại có thể gây XSS hoặc Command Injection (vd: `<script>`, `${...}`, `; rm -rf`).
- **[GOOD]:**
  ```java
  public String sanitizeInput(String input) {
      if (input == null) return null;
      return input.replaceAll("[<>\"']", "").trim();
  }
  ```
- **[BAD]:**
  ```java
  // Sử dụng trực tiếp dữ liệu thô từ client truyền vào hệ thống
  service.executeCommand(request.rawInput());
  ```

---

## 3. SQL & NoSQL Injection Protection (Phòng chống tiêm mã truy vấn)
- **Quy tắc:**
  - Luôn sử dụng Spring Data JPA, Hibernate ORM methods hoặc Parameterized Queries (`NamedParameterJdbcTemplate`, `@Param`).
  - **NGHIÊM CẤM** nối chuỗi ký tự (`+` hoặc `String.format`) để tạo câu truy vấn SQL hoặc JPQL động.
- **[GOOD]:**
  ```java
  @Query("SELECT w FROM WorkOrder w WHERE w.equipmentId = :equipmentId")
  List<WorkOrder> findByEquipmentId(@Param("equipmentId") Long equipmentId);
  ```
- **[BAD]:**
  ```java
  String sql = "SELECT * FROM work_orders WHERE equipment_id = " + userInputId; // Dính SQL Injection
  entityManager.createNativeQuery(sql).getResultList();
  ```

---

## 4. Role-Based Access Control - RBAC (Kiểm soát truy cập dựa trên vai trò)
- **Quy tắc:**
  - Mọi endpoint nhạy cảm (ghi, sửa, xóa, cấu hình) phải được khai báo rõ quyền hạn truy cập thông qua Spring Security `@PreAuthorize` hoặc Security Filter Chain.
  - Áp dụng nguyên tắc quyền tối thiểu (Principle of Least Privilege).
- **[GOOD]:**
  ```java
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN')")
  @PostMapping("/api/workorders")
  public ResponseEntity<?> createWorkOrder(...) { ... }
  ```
- **[BAD]:**
  ```java
  // Endpoint mở công khai không kiểm soát quyền người dùng
  @PostMapping("/api/workorders")
  public ResponseEntity<?> createWorkOrder(...) { ... }
  ```

---

## 5. Sensitive Data Masking & PII Protection (Bảo vệ thông tin cá nhân)
- **Quy tắc:**
  - Không bao giờ trả về các trường nhạy cảm trong response (vd: password hash, secret keys, số CMND/CCCD).
  - Khi cần ghi log các mã số định danh, bắt buộc phải che giấu (masking) một phần thông tin.
- **[GOOD]:**
  ```java
  // Che giấu số tài khoản hoặc thông tin định danh
  String maskedId = id.replaceAll("(?<=^.{2}).(?=.{2}$)", "*");
  log.info("Processing transaction for ID: {}", maskedId);
  ```
- **[BAD]:**
  ```java
  log.info("Full credit card number: {}", cardNumber); // Vi phạm tiêu chuẩn PCI-DSS
  ```
