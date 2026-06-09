# ==========================================
# 后端 Dockerfile - SpringBoot 3.3 + JDK 17
# 分层缓存策略：依赖层独立，源代码修改不触发依赖重下载
# ==========================================

ARG DOCKER_REGISTRY

# ---------- 构建阶段 ----------
FROM ${DOCKER_REGISTRY}/library/maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 1. 先复制 Maven 配置和阿里云镜像源（配置层，极少变更）
COPY settings.xml /root/.m2/settings.xml

# 2. 复制 pom.xml 单独下载依赖（依赖层，pom.xml 不变更则缓存复用）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 3. 最后复制源代码（业务层，频繁变更，仅触发编译）
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- 运行阶段 ----------
FROM ${DOCKER_REGISTRY}/library/eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9032

ENTRYPOINT ["java", "-jar", "app.jar"]
