@echo off

cd /d %~dp0CareCenter_frontend

where npm >nul 2>nul
if errorlevel 1 (
    echo [ERROR] 未找到 npm 命令，请安装 Node.js 并配置 PATH 环境变量
    pause
    exit /b 1
)

if not exist "node_modules" (
    echo [提示] 首次运行，正在安装依赖，请耐心等待...
    call npm install
    if errorlevel 1 (
        echo [ERROR] npm install 失败，请检查网络后重试
        pause
        exit /b 1
    )
)

echo 正在启动前端开发服务器: http://localhost:8080  （需先启动后端 9090）
call npm run serve
pause
