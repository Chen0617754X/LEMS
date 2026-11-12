# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 复制 pom.xml 先做依赖预热（利用 Docker 层缓存）
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests || true

# 复制源码并打包
COPY src ./src
RUN mvn -B package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/target/lems-backend-*.jar app.jar

EXPOSE 8081

# 生产环境 profile（数据库等配置来自环境变量）
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
