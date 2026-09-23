# LAB 2.1 — Context Engineering: Thiết Lập Bộ Quy Tắc Nền Tảng (Rules Pack)
Chuyên đề: **Section 3 — Ngày 2: Context (Design AI context)**

---

## 1. Giới Thiệu
Bài Lab này tập trung vào kỹ thuật **Context Engineering** trong phát triển phần mềm có sự hỗ trợ của AI (GitHub Copilot). Bằng cách thiết lập một bộ quy tắc dự án rõ ràng, gọn gàng và có cấu trúc chuẩn mực trong thư mục `docs/`, chúng ta có thể:
- Loại bỏ các hành vi ảo giác (hallucinations) của AI.
- Đảm bảo mã nguồn sinh ra luôn tuân thủ chuẩn kiến trúc doanh nghiệp (Java 17+, Spring Boot 3.x, Clean Architecture).
- Tối ưu hiệu năng bộ nhớ và CPU thông qua các ràng buộc kỹ thuật cụ thể.

---

## 2. Cấu Trúc Thư Mục Tài Liệu

```text
Add2Number/
├── docs/
│   ├── coding-rules.md     # 12 quy tắc lập trình Java/Spring, scope biến, logging, injection
│   ├── api-rules.md        # Chuẩn RESTful, RFC 7807 Problem Details, Strict Schema Conformance
│   ├── security-rules.md   # Chuẩn bảo mật: chống injection, RBAC, cấm hardcode secret
│   └── README.md           # Tài liệu hướng dẫn bài lab & prompt engineering
├── ScratchHandler.java     # Mã nguồn mẫu kiểm chứng (REST API + sum tối ưu cao cấp)
├── SCORECARD.md            # Bảng chấm điểm tuân thủ (12 tiêu chí, 120/120 điểm PASS)
└── pom.xml                 # Cấu hình dự án Maven
```

---

## 3. Hướng Dẫn Cài Đặt GitHub Copilot Trên Eclipse IDE

1. **Yêu cầu hệ thống**: Eclipse IDE 2023-09 trở lên, Java 17+.
2. **Các bước cài đặt**:
   - Mở Eclipse -> Chọn menu **Help** -> **Eclipse Marketplace...**
   - Trong ô tìm kiếm, gõ từ khóa `GitHub Copilot`.
   - Bấm **Install** cạnh extension GitHub Copilot và chọn **Confirm**.
   - Chấp nhận các điều khoản giấy phép và bấm **Finish**.
   - Khởi động lại Eclipse IDE (Restart IDE) khi được yêu cầu.
3. **Đăng nhập và kích hoạt**:
   - Sau khi khởi động lại, bấm vào biểu tượng GitHub Copilot ở thanh trạng thái góc dưới bên phải.
   - Chọn **Login to GitHub**, trình duyệt sẽ mở trang cấp mã xác thực thiết bị (Device Code).
   - Nhập mã xác nhận và hoàn tất cấp quyền.
   - Mở **Copilot Chat View** qua: `Window` -> `Show View` -> `Other...` -> Tìm `GitHub Copilot Chat`.

---

## 4. Các Câu Prompt Có Ngữ Cảnh Ràng Buộc (Prompt Engineering)

### Prompt 1: Sinh REST API Handler
```text
Role: Senior Engineer.
Task: Write a POST /api/workorders handler in Java.
Context files: docs/coding-rules.md, docs/api-rules.md, docs/security-rules.md.
Constraints:
- Do not invent extra JSON fields not specified in requirements (title, description, equipmentId, priority).
- Use standard Jakarta validation.
- Return HTTP 201 on success.
- Return HTTP 400 with RFC 7807 Problem Details on validation error.
- Return HTTP 403 on insufficient role.
- Use Constructor Injection.
- Match repo style.
```

### Prompt 2: Hoàn Thiện Thuật Toán `sum(String, String)` Tối Ưu Cao Cấp
```text
Role: Senior Performance Engineer.
Task: Complete method sum(String, String) in Java.
Context files: docs/coding-rules.md.
Constraints:
- Use Java standard coding rules of Oracle and Google.
- Up version by don't use reverse(), use array of char (char[]) to keep the result from right to left.
- At the end, convert result from char[] to String to return.
- Up version by perform validate in loop while to avoid a separate loop to check valid number only which the performance is not good (Single-pass O(N)).
- Strictly obey Rule 7: DO NOT declare variables inside the while loop. All loop and temporary variables must be declared outside the loop block.
```

---

## 5. Hướng Dẫn Biên Dịch & Chạy Kiểm Thử

Mở terminal PowerShell tại thư mục gốc của dự án (`c:\Add2Number`) và chạy lệnh:

```powershell
# Biên dịch file ScratchHandler.java với mã hóa UTF-8
javac -encoding UTF-8 -d . ScratchHandler.java

# Chạy bộ kiểm thử tự động
java -cp . com.example.workorder.ScratchHandler
```

### Kết quả kỳ vọng:
- Toàn bộ các test case của `sum` (số nhỏ, số có nhớ, số lớn 30 chữ số, input lỗi) đều hiển thị `[PASSED]`.
- Toàn bộ 3 kịch bản REST API (Hợp lệ 201, Lỗi thiếu Title 400 Problem Detail, Lỗi vai trò 403 Problem Detail) đều được mô phỏng và xác nhận đạt chuẩn.
