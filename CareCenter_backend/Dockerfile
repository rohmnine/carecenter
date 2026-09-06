# ===== 构建阶段：Maven + JDK 8 =====
# 使用国内镜像加速源拉取基础镜像（网络可直连 Docker Hub 时可去掉 dockerproxy.net/library/ 前缀）
FROM dockerproxy.net/library/maven:3.8.6-openjdk-8 AS builder

# 配置阿里云 Maven 镜像加速依赖下载
RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">' \
    '  <mirrors>' \
    '    <mirror>' \
    '      <id>aliyun</id>' \
    '      <name>Aliyun Maven</name>' \
    '      <url>https://maven.aliyun.com/repository/public</url>' \
    '      <mirrorOf>central</mirrorOf>' \
    '    </mirror>' \
    '  </mirrors>' \
    '</settings>' > /root/.m2/settings.xml

WORKDIR /build

# 先单独复制 pom.xml 拉取依赖，充分利用镜像构建缓存
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== 运行阶段：JRE 8 =====
FROM dockerproxy.net/library/eclipse-temurin:8-jre

WORKDIR /app

COPY --from=builder /build/target/CareCenter_backend-1.0.0.jar app.jar

# 示例菜品图片放入 /data/files，挂载命名卷后会被自动拷入卷中持久保存
COPY src/main/resources/files /data/files

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
