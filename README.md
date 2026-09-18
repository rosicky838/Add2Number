Add2Num Web
Dự án Java + Spring Boot để cộng hai số lớn bằng chuỗi, không bị giới hạn bởi kiểu dữ liệu số nguyên thông thường.

Mô tả
Logic cộng số lớn nằm trong module Add2Num
Ứng dụng web cho phép nhập 2 số và hiển thị kết quả trực tiếp
Hỗ trợ số có độ dài lớn, ví dụ: 99999999999999999999 + 1
Cấu trúc project
Add2Number/
├── Add2Num/
│   ├── src/
│   │   ├── main/java/com/add2num/MyBigNumber.java
│   │   └── test/java/com/add2num/MyBigNumberTest.java
├── src/
│   ├── main/java/com/add2numweb/
│   │   ├── Add2NumController.java
│   │   └── Add2NumWebApplication.java
│   ├── main/resources/
│   │   ├── application.properties
│   │   └── templates/index.html
│   └── test/java/com/add2numweb/
│       └── Add2NumWebApplicationTests.java
├── libs/
│   └── Add2Num-0.0.1.jar
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
└── target/

Công nghệ sử dụng
Java 17
Spring Boot 3.2.0
Thymeleaf
Maven
JUnit 5
Chạy ứng dụng
Ở thư mục gốc của project:
./mvnw spring-boot:run

Sau đó mở:
http://localhost:8080

Chạy test
./mvnw test

huật toán
Phương thức chính sum(String num1, String num2) thực hiện phép cộng theo cách tương tự học sinh tiểu học:

cộng từ phải sang trái
tính số nhớ (carry)
ghép kết quả lại thành chuỗi
Ghi chú
Project hiện tại bao gồm:

logic xử lý cộng số lớn ở module Add2Num
giao diện web ở project gốc để người dùng nhập liệu và xem kết quả
