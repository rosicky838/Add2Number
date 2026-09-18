# Add2NumWeb

Ứng dụng Web cộng 2 số lớn biểu diễn dưới dạng chuỗi, không bị giới hạn bởi kiểu dữ liệu số nguyên thông thường.

## Mô tả
- Logic cộng số lớn được tái sử dụng từ thư viện `Add2Num` (Task 1) dưới dạng file `.jar`
- Ứng dụng Web cho phép nhập 2 số và hiển thị kết quả cùng tiến trình từng bước
- Hỗ trợ số có độ dài lớn

## Công nghệ sử dụng
- Java 17
- Spring Boot 3.2.0
- Thymeleaf
- Bootstrap 5
- Maven

## Yêu cầu môi trường
- Java 17+
- Maven 3.6+

## Cách clone
Windows:
```bash
git clone https://github.com/rosicky838/Add2NumWeb.git D:\Projects\github.com\rosicky838\Add2NumWeb
```
Mac/Linux:
```bash
git clone https://github.com/rosicky838/Add2NumWeb.git ~/Projects/github.com/rosicky838/Add2NumWeb
```

## Cách chạy
```bash
cd Add2NumWeb
mvn spring-boot:run
```
Sau đó mở trình duyệt:
```
http://localhost:8080
```

## Cách chạy Test
```bash
mvn test
```

## Thuật toán
Phương thức `sum(String num1, String num2)` thực hiện phép cộng theo cách học sinh Tiểu học:
1. Duyệt từ phải sang trái
2. Cộng từng chữ số + số nhớ (carry)
3. Ghi kết quả từng bước
4. Ghép lại thành chuỗi kết quả


## Test Cases
| Số 1                  | Số 2 | Kết quả               |
|-----------------------|------|-----------------------|
| 1234                  | 897  | 2131                  |
| 999                   | 1    | 1000                  |
| 0                     | 0    | 0                     |
| 9999                  | 9999 | 19998                 |
| 99999999999999999999  | 1    | 100000000000000000000 |
