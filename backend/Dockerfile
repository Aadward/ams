# ===== 阶段1：依赖下载 =====
FROM docker.1ms.run/eclipse-temurin:21-jdk-alpine AS deps
WORKDIR /app
COPY pom.xml .
RUN apk add --no-cache maven
RUN mvn dependency:go-offline -B

# ===== 阶段2：构建 =====
FROM docker.1ms.run/eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY --from=deps /root/.m2 /root/.m2
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -B

# ===== 阶段3：运行 =====
FROM docker.1ms.run/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]