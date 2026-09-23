# ShareMoney

Spring Boot 4.0.2, Java 17, Maven, Spring Data JPA/Hibernate và MySQL.

## Chạy local trong IntelliJ

1. Chọn JDK 17 cho Project SDK và Run Configuration. Maven cũng cần JDK 17.
2. Mở **Run → Edit Configurations → ShareMoneyApplication**. Trong
   **Modify options → Environment variables**, khai báo riêng từng biến:

   ```text
   DB_USERNAME=root
   DB_PASSWORD=<mysql-local-password>
   JWT_SECRET=<jwt-secret>
   ```

   Thay các giá trị trong dấu `<...>` bằng secret của bạn. JWT secret cần ngẫu nhiên,
   tối thiểu 32 byte cho HS256. Không đưa giá trị thật vào source, README hoặc run
   configuration được chia sẻ qua Git.
3. Mặc định profile là `local` nhờ
   `spring.profiles.active=${SPRING_PROFILES_ACTIVE:local}`. Có thể để trống trường
   **Active profiles**; kiểm tra không còn `prod` trong trường này hoặc arguments.
4. Không cần đặt `DB_URL` khi MySQL chạy trên máy tại cổng 3306 và có database
   `share_money`. Nếu cần đổi máy chủ/cổng/database, đặt toàn bộ JDBC URL qua `DB_URL`.
5. Run `ShareMoneyApplication`. Redis và Kafka mặc định lần lượt dùng
   `localhost:6379` và `localhost:9092`; cần chạy các dịch vụ này khi sử dụng tính năng tương ứng.

Spring Boot không tự đọc `.env` trong project này. Docker Compose dùng `.env` để
nội suy cấu hình container; điều đó không cấp biến môi trường cho Run Configuration
của IntelliJ. Không nạp nguyên `.env` dành cho Docker vào IntelliJ vì nó có thể chọn
profile `prod` và hostname `mysql`, chỉ dùng được trong mạng Docker.

## Datasource lấy cấu hình từ đâu?

`application.properties` giữ cấu hình chung (bao gồm `jwt.secret=${JWT_SECRET}` và
cấu hình pool Hikari). `application-local.properties` chứa:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/share_money?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

`application-prod.properties` yêu cầu `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` và
`JWT_SECRET` từ môi trường triển khai; chọn bằng `SPRING_PROFILES_ACTIVE=prod`.
Các tùy chọn tắt SSL trong URL local chỉ dành cho phát triển trên máy.

Thứ tự override thường gặp, ưu tiên cao trước:

| Nguồn | Ví dụ |
| --- | --- |
| Program arguments | `--spring.datasource.password=...`, `--spring.profiles.active=...` |
| Spring application JSON | `SPRING_APPLICATION_JSON` |
| JVM system properties / VM options | `-Dspring.datasource.password=...` |
| Environment variables | `SPRING_DATASOURCE_PASSWORD` |
| File cấu hình ngoài ứng dụng | `application-local.properties` trong working directory hoặc `config/` |
| File profile trong classpath | `src/main/resources/application-local.properties` |
| File chung trong classpath | `src/main/resources/application.properties` |

`DB_PASSWORD` là biến được placeholder trong file tham chiếu. Nếu tồn tại
`SPRING_DATASOURCE_PASSWORD`, nó ghi đè trực tiếp `spring.datasource.password`,
kể cả khi `DB_PASSWORD` đã đúng. Quy tắc tương tự áp dụng cho URL và username.
`spring.config.location`, `spring.config.additional-location`, `spring.config.import`
và các biến `SPRING_CONFIG_*` tương ứng cũng có thể thay đổi nguồn file được nạp.
Tham khảo [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html).

Khi nghi có cấu hình cũ, kiểm tra Environment variables, Active profiles,
Program arguments, VM options và đường dẫn file môi trường trong Run Configuration.
Kiểm tra cả Windows **User variables** và **System variables** cho:

```text
DB_URL, DB_USERNAME, DB_PASSWORD
SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
SPRING_PROFILES_ACTIVE, SPRING_APPLICATION_JSON, SPRING_CONFIG_LOCATION
SPRING_CONFIG_ADDITIONAL_LOCATION, SPRING_CONFIG_IMPORT
JAVA_TOOL_OPTIONS, JDK_JAVA_OPTIONS, _JAVA_OPTIONS
```

Xóa override cũ hoặc cập nhật tại đúng nơi đã khai báo; khởi động lại IntelliJ sau
khi đổi biến Windows để tiến trình nhận môi trường mới. Không in secret ra log.

## MySQL authentication và Docker

Nếu gặp `Access denied for user 'root'@'localhost' (using password: YES)`, trước hết
đảm bảo `DB_PASSWORD` thực sự có trong môi trường chạy. Trong lần rà soát này,
debugger xác nhận Hikari nhận nguyên chuỗi `${DB_PASSWORD}` khi biến bị thiếu;
`using password: YES` không chứng minh password đã được cấu hình đúng.
Lỗi `Unable to determine Dialect without JDBC metadata` tiếp theo là hậu quả của
việc không lấy được kết nối. Không thêm Hibernate dialect để che lỗi authentication.

Sau khi cấp đúng biến môi trường, kiểm tra độc lập bằng lệnh (nhập password ở prompt):

```shell
mysql -h localhost -P 3306 -u root -p
```

Nếu CLI cũng báo Access denied, credential hoặc quyền của tài khoản MySQL bên ngoài
project chưa đúng; cần kiểm tra đúng MySQL instance và tài khoản `root@localhost`.
Không tự reset password hay thay đổi user MySQL để xử lý cấu hình ứng dụng.
Nếu CLI đăng nhập được mà app vẫn lỗi, kiểm tra lại các nguồn override ở trên.

Compose ánh xạ MySQL **localhost:3307 → mysql:3306**. Khi chạy Java bằng IntelliJ
nhưng dùng MySQL của Compose, đặt:

```text
DB_URL=jdbc:mysql://localhost:3307/share_money?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
```

Backend chạy trong Compose dùng hostname `mysql:3306`. Đảm bảo DB_URL chọn đúng
database có trong instance/volume của bạn. `MYSQL_DATABASE` chỉ khởi tạo database
trên volume mới; volume cũ có thể vẫn chứa database `apifirst`.
Compose lấy `MYSQL_ROOT_PASSWORD` từ `DB_PASSWORD` và không chứa password cố định.
Thay `.env` hoặc biến này không đổi password trong volume MySQL đã được khởi tạo.
Không xóa volume để sửa lỗi đăng nhập.

## Build và bảo vệ secret

```shell
./mvnw clean compile
```

Trên Windows PowerShell dùng `./mvnw.cmd clean compile`. `JAVA_HOME` cần trỏ tới
JDK 17; compile không kiểm tra đăng nhập MySQL.

Commit `application-local.properties` vì file chỉ chứa placeholder và mặc định
không nhạy cảm. `.env`, `.env.*`, `application-secret.*`, `.idea/` và `.run/` được
ignore; `.dockerignore` cũng ngăn đưa chúng vào Docker build context.
`.gitignore` không bảo vệ file đã được Git theo dõi. `.env` từng được theo dõi trong
repository này và đã được bỏ khỏi index, vẫn giữ trên máy để developer tự quản lý.
Việc bỏ theo dõi không xóa secret khỏi lịch sử Git. Nếu secret cũ từng được chia sẻ,
chủ sở hữu cần chủ động thu hồi/thay mới và xử lý lịch sử theo quy trình của nhóm.
