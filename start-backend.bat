@echo off

cd /d %~dp0

REM 必须在项目根目录启动：图片上传路径按 "启动目录 + CareCenter_backend/src/main/resources/files" 解析

where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] 未找到 java 命令，请安装 JDK 并配置 PATH 环境变量
    pause
    exit /b 1
)

if not exist "CareCenter_backend\target\CareCenter_backend-1.0.0.jar" (
    echo [ERROR] 未找到 CareCenter_backend\target\CareCenter_backend-1.0.0.jar
    echo 请先打包: cd CareCenter_backend 后执行 mvn clean package -DskipTests
    pause
    exit /b 1
)

echo 正在启动后端服务: http://localhost:9090  （Ctrl+C 或关闭窗口即停止）
java -jar CareCenter_backend\target\CareCenter_backend-1.0.0.jar
pause
