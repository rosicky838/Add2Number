# Project Add2Num - Thuật toán cộng 2 số lớn (Mô phỏng Tiểu học)

Dự án này cài đặt hàm cộng hai số lớn được biểu diễn dưới dạng chuỗi trong ngôn ngữ Java, ghi nhận lịch sử từng bước thực hiện bằng Logger và bao gồm bộ kiểm thử tự động (Unit Test).

---

## 1. Thông tin tổng quan
- **Tên dự án:** Add2Num
- **Tác giả:** Thach.Le
- **Phiên bản:** `0.0.1`
- **Môi trường yêu cầu:** Java JDK 11+, Apache Maven 3.6+

---

## 2. Quy ước Clone Dự án về Máy cục bộ
Theo quy ước tài liệu hướng dẫn, thư mục clone được tổ chức theo đường dẫn tiêu chuẩn:

### Trên hệ điều hành Windows:
```cmd
mkdir D:\Projects\github.com\<youraccount>
cd /d D:\Projects\github.com\<youraccount>
git clone https://github.com/<youraccount>/Add2Num.git
cd Add2Num
```

### Trên hệ điều hành MacOS / Linux:
```bash
mkdir -p ~/Projects/github.com/<youraccount>
cd ~/Projects/github.com/<youraccount>
git clone https://github.com/<youraccount>/Add2Num.git
cd Add2Num
```

---

## 3. Hướng dẫn Biên dịch và Chạy Chương trình

### 3.1 Biên dịch Source Code
Mở Terminal / Command Prompt tại thư mục gốc của project và chạy:

```bash
mvn clean compile
```

### 3.2 Chạy Ứng dụng Console
Chạy phương thức `main` để theo dõi các log minh họa:

```bash
mvn exec:java -Dexec.mainClass="com.mybignumber.Main"
```

---

## 4. Hướng dẫn Chạy Unit Test
Bộ Unit Test được viết bằng **JUnit 5**. Để thực thi toàn bộ các test case:

```bash
mvn test
```

Nếu muốn xem báo cáo chi tiết về Unit Test:
- Báo cáo kết quả kiểm thử dạng văn bản sẽ lưu tại: `target/surefire-reports/`.

---

## 5. Ví dụ Output Log
Khi thực hiện gọi hàm `myBigNumber.sum("1234", "897")`, từng bước tính toán sẽ được ghi lại qua Logger:

```text
=== THỰC HIỆN PHÉP CỘNG: 1234 + 897 ===
Bước 1: Lấy 4 cộng với 7 được 11. Lưu 1 vào kết quả và nhớ 1.
Bước 2: Lấy 3 cộng với 9 được 12. Cộng tiếp với nhớ 1 được 13. Lưu 3 vào kết quả được kết quả mới là "31". Ghi nhớ 1.
Bước 3: Lấy 2 cộng với 8 được 10. Cộng tiếp với nhớ 1 được 11. Lưu 1 vào kết quả được kết quả mới là "131". Ghi nhớ 1.
Bước 4: Lấy 1 cộng với 0 được 1. Cộng tiếp với nhớ 1 được 2. Lưu 2 vào kết quả được kết quả mới là "2131". Nhớ 0.
-> KẾT QUẢ CỦA 1234 + 897 = 2131
```

---

## 6. Quy trình đẩy code lên Git & Đánh tag phiên bản `0.0.1`

Để nộp sản phẩm chuẩn yêu cầu bài tập:

1. Khởi tạo Git và thêm remote:
   ```bash
   git add .
   git commit -m "Initial commit - Complete Add2Num core and unit tests v0.0.1"
   git push -u origin main
   ```

2. Đánh Tag phiên bản `0.0.1` và đẩy lên GitHub:
   ```bash
   git tag -a 0.0.1 -m "Release version 0.0.1"
   git push origin 0.0.1
   ```