# Render 部署检查清单 — LEMS 后端

## 部署参数

| 项 | 值 |
|---|---|
| 仓库 | `https://github.com/Chen0617754X/LEMS` |
| Root Directory | `lems-backend` |
| Runtime | Docker |
| Instance Type | Free |
| 构建上下文 | `./lems-backend`（Dockerfile 在此目录） |

## 环境变量（必填）

> 密码请从 Aiven 控制台自行获取，勿写入代码仓库。

```
DATABASE_URL=jdbc:mysql://lems-mysql-lems-lumos.f.aivencloud.com:15447/defaultdb?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=true&allowPublicKeyRetrieval=true&requireSSL=true
DATABASE_USERNAME=avnadmin
DATABASE_PASSWORD=<从 Aiven 控制台复制>
```

## 备选方案 A：原生 Java Runtime（如果 Docker 构建 OOM）

- Runtime: `Java`
- Build Command: `mvn clean install -DskipTests`
- Start Command: `java -jar target/lems-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod`
- 环境变量额外添加：`JAVA_VERSION=21`

## 备选方案 B：Docker 构建报内存不足

在 Dockerfile 的 build stage 加一行：
```dockerfile
ENV MAVEN_OPTS="-Xmx384m"
```

## 验证

部署成功后访问 `https://lems-backend.onrender.com/api/user/profile`
- 返回 `{"code":401,...}` → 服务正常，只是未登录
- 返回 502/503 → 服务未就绪
- 连接错误 → 检查 DATABASE_URL 环境变量

## 常见问题

**Q: 构建报 `Java version 21 not supported`**
A: 环境变量加 `JAVA_VERSION=21`

**Q: 构建报 `The goal you specified requires a project to execute but there is no POM in this directory`**
A: Root Directory 必须是 `lems-backend` 而不是仓库根目录

**Q: 启动报 `Table 'lems.sys_user' doesn't exist`**
A: 数据库未初始化。在本地运行 `AIVEN_PASSWORD=xxx node init-db.cjs`

**Q: 报 `Public Key Retrieval is not allowed`**
A: `DATABASE_URL` 里必须有 `allowPublicKeyRetrieval=true`

**Q: 报 `Communications link failure`**
A: Aiven 服务还在 Building 状态，或 DATABASE_URL 主机名写错
