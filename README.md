# 校外托管服务中心管理系统（Spring Boot + Vue）

## 1. 项目简介
本项目为校外托管服务中心管理系统，采用前后端分离架构（Spring Boot + Vue），面向 **管理员 / 学生 / 家长** 三类角色，覆盖 **床位与房间管理、请假审批、进出登记、访客预约、菜谱与就餐、账单缴费、通知消息、讨论与反馈** 等核心业务，强化家校联动和多角色协同。

---

## 2. 最近新增功能（基于当前代码梳理）

### 2.1 房间与床位管理增强
- 新增 **家长选床申请闭环**：支持查看可选床位、提交选床申请、取消申请、查看审批结果
- 新增 **房间信息展示增强**：支持按房间查看床位占用状态、可视化展示可分配情况
- 新增 **房间接口扩展**：新增/完善房间维度查询、床位可用性判断、家长申请相关接口

### 2.2 家长端能力扩展
- 新增 **家长-学生绑定申请**：家长可发起与学生账号的绑定申请，跟踪学生确认与管理员审核进度
- 新增 **家长仪表盘**：集中展示请假、进出、订餐等关键统计信息
- 新增 **家长请假代办**：家长可直接发起学生请假并跟踪审批状态
- 新增 **家长访客预约**：支持预约提交、状态查看与记录查询
- 新增 **家长就餐预约**：支持单日与批量预约、取消预约、审核状态追踪
- 新增 **家长进出记录查看**：按学生维度查看进出明细与异常记录

### 2.3 学生端能力扩展
- 新增 **绑定申请确认**：学生可查看家长发来的绑定申请，进行同意/拒绝操作
- 新增 **学生请假申请页面**：学生可发起请假、查看审核进度与处理结果
- 新增 **学生进出记录查询**：支持学生侧按时间维度查看进出记录

### 2.4 管理员端能力扩展
- 新增 **绑定申请审核**：管理员可对学生确认后的家长绑定申请进行最终审核
- 新增 **就餐预约审核页面**：管理员可对家长就餐预约进行审核、确认与状态更新
- 新增 **进出管理页面增强**：支持对异常进出记录进行识别与处理
- 新增 **访客信息管理增强**：支持访客申请记录的统一查询与处理

### 2.5 菜谱展示能力扩展
- 新增 **随机菜谱页面**：用于按规则随机展示推荐菜谱
- 新增 **每周菜谱页面优化**：支持更直观的周维度菜单查看

---

## 3. 功能模块

### 3.1 管理员端
- 用户与角色管理：学生/家长信息维护、家长-学生绑定申请审核
- 服务点/房间/床位管理：分店、房间、床位分配与维护
- 公告管理：公告发布与维护
- 统一审批中心：请假、访客预约、就餐预约、床位申请统一审核
- 进出管理：进出记录、异常处理、违规提醒
- 菜谱与餐务：随机菜谱、每周菜谱、每日菜品图片
- 就餐预约审核：家长预约审核、确认/取消确认
- 账单与缴费：托管费/餐费账单管理、统计与批量处理
- 互动管理：菜品建议处理、讨论组管理

### 3.2 学生端
- 个人信息维护
- 家长绑定申请确认（同意/拒绝家长的绑定请求）
- 房间/床位查看
- 请假申请与进度查询
- 进出记录查询
- 每周菜谱与每日菜品图片查看
- 缴费状态查看
- 讨论组参与与菜品建议提交

### 3.3 家长端
- 家长账号登录与信息维护
- 家长-学生绑定申请（发起绑定、跟踪学生确认与管理员审核进度）
- 通知中心（请假、异常进出、缴费提醒等）
- 代提交请假、查看审批进度
- 访客预约申请与记录查询
- 就餐预约（单日/批量预约、取消、审核状态跟踪）
- 床位申请（可选床位查看、申请、取消、审批结果）
- 学生账单与缴费状态查看
- 家长仪表盘（请假、进出、就餐预约统计、出勤率）
- 学生进出记录查看
- 学生状况报告查看（最新状态 + 历史记录）
- 讨论组参与与菜品反馈提交

---

## 4. 技术栈

### 后端（`CareCenter_backend`）
- Java 8
- Spring Boot 2.6.3
- MyBatis / MyBatis-Plus 3.5.1
- MySQL 5.7+
- Maven
- Apache POI（Excel 导入导出）

### 前端（`CareCenter_frontend`）
- Vue 3
- Vue Router 4
- Vuex 4
- Element Plus
- Axios
- ECharts
- wangEditor（公告富文本）

---

## 5. 项目结构
```text
CareCenterSystem
├─ CareCenter_backend                # Spring Boot 后端
│  ├─ src/main/java/com/example/springboot
│  │  ├─ controller                  # REST 接口层
│  │  ├─ service / impl              # 业务层
│  │  ├─ mapper                      # 数据访问层
│  │  └─ entity                      # 实体层
│  ├─ src/main/resources
│  │  ├─ application.properties      # 后端配置
│  │  └─ files                       # 上传文件目录
│  └─ pom.xml
├─ CareCenter_frontend               # Vue 前端
│  ├─ src/views                      # 页面
│  ├─ src/components                 # 公共组件
│  ├─ src/router                     # 路由与守卫
│  ├─ src/assets/js                  # 页面逻辑脚本
│  │  └─ Home.js / RoomInfo.js       # 首页与房间管理脚本
│  └─ package.json
├─ doc/carecentermanagement.sql      # 数据库初始化脚本（库名：carecentermanagement）
└─ README.md
```

---

## 6. 核心业务说明

### 6.1 统一审批中心
- 将 **请假/访客预约/就餐预约/床位申请** 统一进入 `approval_request`
- 前端按类型分标签处理审批任务
- 进出记录支持异常标记与家长提醒

### 6.2 菜谱与餐务
- 菜品库管理、每周菜谱维护、随机菜谱推荐
- 每日菜品图片上传与查看
- 家长预约就餐 + 管理员审核/确认/取消确认

### 6.3 账单与缴费
- 托管缴费记录管理、批量操作与统计
- 餐费账单按月生成，支持请假抵扣与人工调整
- Excel 导入/导出与批量提醒缴费

### 6.4 家校互动
- 通知中心（未读统计、已读标记、删除）
- 讨论组互动与菜品建议反馈闭环

### 6.5 家长-学生绑定
- 三级确认闭环：**家长发起申请 → 学生确认/拒绝 → 管理员审核**
- 申请经学生同意、管理员审核通过后正式建立绑定关系
- 防重复机制：学生已被绑定或存在待处理申请时，不允许重复发起

---

## 7. 接口分组（概要）
- `/admin`、`/stu`、`/parent`
- `/room`、`/building`
- `/leaveRequest`、`/entryExit`
- `/weeklyMenu`、`/menu`、`/dailyMenuPhoto`
- `/mealReservation`、`/mealBill`、`/studentStatus`
- `/binding`
- `/payment`
- `/discussion`、`/dishSuggestion`
- `/notice`

---

## 8. 主要新增页面（前端）
- 家长端：`ParentDashboard.vue`、`ParentBedSelection.vue`、`ParentLeaveRequest.vue`、`ParentVisitorAppointment.vue`、`ParentMealReservation.vue`、`ParentEntryExitView.vue`
- 学生端：`StudentLeaveRequest.vue`、`StudentEntryExitView.vue`
- 管理端：`MealReservationAudit.vue`、`EntryExitManagement.vue`
- 通用/信息页：`RoomInfo.vue`、`SelfInfo.vue`、`VisitorInfo.vue`、`WeeklyMenu.vue`、`RandomMenu.vue`

> 以上页面均位于 `CareCenter_frontend/src/views/`。

---

## 9. 主要新增后端模块（接口/服务）
- 房间与床位：`CenterRoomController`、`CenterRoomService`、`CenterRoomImpl`、`CenterRoomMapper`
- 家长-学生绑定：`StudentParentBindingController`、`StudentParentBindingRequestService`
- 家长聚合能力：`ParentController`
- 学生能力扩展：`StudentController`

> 后端代码路径：`CareCenter_backend/src/main/java/com/example/springboot/`

---

## 10. 数据库说明
初始化脚本：`doc/carecentermanagement.sql`（库名：`carecentermanagement`）

核心业务表：
- 用户与组织：`user_entity`、`admin`、`student`、`parent`、`center_building`、`center_room`、`center_room_bed`
- 家长-学生绑定：`student_parent_binding_request`
- 审批中心：`approval_request`
- 访客：`visitor`、`visitor_appointment`
- 菜谱与餐务：`menu`、`weekly_menu`、`daily_menu_photo`、`meal_billing_config`
- 状态与通知：`student_status_report`、`entry_exit_record`、`parent_notification`
- 费用与账单：`billing_record`、`payment_record`、`meal_bill`
- 互动：`discussion_message`、`dish_suggestion`
- 公告：`notice`

---

## 11. 本地运行

### 11.1 环境要求
- JDK 1.8+
- Maven 3.6+
- Node.js 14+（建议 16）
- MySQL 5.7+

### 11.2 初始化数据库
1. 创建数据库：`carecentermanagement`
2. 导入脚本：`doc/carecentermanagement.sql`

### 11.3 配置后端
编辑 `CareCenter_backend/src/main/resources/application.properties`：
```properties
server.port=9090
spring.datasource.url=jdbc:mysql://localhost:3306/carecentermanagement?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=true
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### 11.4 启动后端
```bash
cd CareCenter_backend
mvn clean package
java -jar target/CareCenter_backend-1.0.0.jar
```
后端默认地址：`http://localhost:9090`

### 11.5 启动前端
```bash
cd CareCenter_frontend
npm install
npm run serve
```
前端默认地址：`http://localhost:8080`

---

## 12. Docker 部署（一键启动，推荐）

### 12.1 前提条件
- 安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)（Windows 需启用 WSL2），并确保其正在运行
- 无需本机安装 JDK / Maven / Node.js / MySQL，全部在容器内完成

### 12.2 一键启动
在项目根目录执行：
```bash
docker compose up -d --build
```
首次构建需要拉取镜像并下载 Maven / npm 依赖，耗时较长（已内置阿里云 / npmmirror 国内加速），请耐心等待。

### 12.3 访问方式
| 服务 | 地址 | 说明 |
| --- | --- | --- |
| 前端 | `http://localhost:8080` | 入口，nginx 托管并将 `/api` 反代到后端 |
| 后端 | `http://localhost:9090` | 调试用，可直接访问 |
| MySQL | `localhost:3307`（root / 123456） | 宿主机映射端口，避免与本机 3306 冲突 |

- 首次启动会自动创建数据库 `carecentermanagement` 并导入 `doc/carecentermanagement.sql`
- 默认账号不变：管理员 `admin/admin`，学生 `stu1/123456`，家长 `parent1/123456`

### 12.4 数据持久化
- `mysql-data` 卷：MySQL 数据，删除容器不丢数据
- `menu-files` 卷：上传的每日菜品图片（`/data/files`）

### 12.5 常用运维命令
```bash
docker compose ps                 # 查看服务状态
docker compose logs -f backend    # 跟踪后端日志（mysql / frontend 同理）
docker compose restart backend    # 重启某个服务
docker compose down               # 停止并移除容器（数据保留）
docker compose down -v            # 停止并清空所有数据（恢复初始状态）
```

### 12.6 修改端口 / 密码
编辑根目录 `docker-compose.yml` 中对应的环境变量或端口映射后，重新执行 `docker compose up -d` 即可，无需改动代码。

---

## 13. 默认测试账号
- 管理员：`admin / admin`
- 学生：`stu1 / 123456`
- 家长：`parent1 / 123456`

---

## 14. 特色亮点
- 多角色分权 + 路由级权限控制 + 动态菜单
- 统一审批模型（`approval_request` + `request_data` JSON）
- 家长深度参与（请假、访客、订餐、选床、账单、仪表盘、学生状态）
- 家长-学生绑定三级确认闭环（家长申请 → 学生确认 → 管理员审核）
- 餐费账单支持自动生成、人工调整、批量提醒、Excel 导入导出
- 进出异常联动家长通知
- 菜谱可视化与每日菜品图片展示
- 讨论组与菜品建议形成闭环反馈

