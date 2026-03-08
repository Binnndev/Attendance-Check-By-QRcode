# Bước 1: Build ứng dụng bằng Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Tải các thư viện (dependencies) về trước để cache
RUN mvn dependency:go-offline -B
COPY src ./src
# Đóng gói ứng dụng thành file .jar
RUN mvn clean package -DskipTests

# Bước 2: Chạy ứng dụng với JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# (MỚI) Cài đặt tzdata và thiết lập múi giờ Việt Nam cho app điểm danh
RUN apk add --no-cache tzdata
ENV TZ=Asia/Ho_Chi_Minh

# (SỬA LẠI) Chỉ copy file jar chính, tránh copy nhầm file -plain.jar
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar

# Mở cổng 8080 (cổng mặc định của Spring Boot)
EXPOSE 8080

# Lệnh khởi chạy Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]