# Sử dụng Eclipse Temurin 17 JDK làm base image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy file jar sau khi đã build
COPY target/*.jar app.jar

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]