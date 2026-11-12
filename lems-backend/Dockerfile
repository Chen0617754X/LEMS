# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Render 免费实例只有 512MB，限制 Maven 堆内存避免构建 OOM
ENV MAVEN_OPTS="-Xmx320m -XX:MaxMetaspaceSize=128m"

# 复制 pom.xml 先做依赖预热（利用 Docker 层缓存）
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests || true

# 复制源码并打包
COPY src ./src
RUN mvn -B package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# 限制 JVM 堆内存以适配 512MB 实例
ENV JAVA_OPTS="-Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC"

COPY --from=builder /build/target/lems-backend-*.jar app.jar

EXPOSE 8081

# 生产环境 profile（数据库等配置来自环境变量）
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=prod"]
