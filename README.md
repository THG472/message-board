# 留言板系统（Message Board）

一个基于 Spring Boot + Vue 的企业内部留言板系统，支持发布问题反馈/需求建议、评论回复、点赞、热榜排行、附件上传，并与企业现有用户系统（syngbs 库）集成，自动获取用户信息。

---

## 一、项目简介

本项目是一个前后端分离的企业内部留言板系统，用于员工反馈问题、提交需求建议。系统与企业已有的用户库（`syngbs.manex` / `syngbs.organ`）集成，发布人信息（姓名、机构）自动从外部数据库查询获取，无需用户重复填写。支持留言发布、评论回复、点赞、热榜排行、附件上传、操作日志记录等功能。

### 项目结构

```
message-board/
├── message-board/         # 后端服务（Spring Boot，war 包部署）
│   └── src/main/java/com/messageboard/
│       ├── controller/    # 控制器层（留言/评论/用户/文件）
│       ├── service/       # 业务逻辑层（含外部用户集成）
│       ├── mapper/        # MyBatis Plus 数据访问层（含外部库查询）
│       ├── entity/        # 数据库实体
│       ├── dto/           # 数据传输对象
│       ├── vo/            # 视图对象
│       ├── aop/           # 操作日志切面
│       ├── common/        # 通用返回结果、UID工具
│       └── config/        # 配置类（CORS/Jackson/MyBatis/文件上传/异常处理）
├── message/               # 前端项目（Vue 2）
│   └── src/
│       ├── views/         # 页面（列表/详情/热榜）
│       ├── api/           # 接口封装
│       ├── router/        # 路由
│       └── store/         # Vuex 状态管理
├── sql/                   # 数据库脚本
│   ├── message_board.sql  # 建库建表
│   └── upgrade.sql        # v1.1.0 增量升级（附件、手机号）
├── nginx.conf             # Nginx 部署配置
└── 开发过程记录.md         # 开发过程记录
```

---

## 二、技术架构

### 后端（message-board）

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 1.8 | 运行环境 |
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.0.33 | 数据库 |
| Spring AOP | - | 操作日志切面 |
| FastJSON | 1.2.83 | JSON 处理 |
| Jackson JSR310 | - | Java 8 时间序列化 |
| Validation | - | 参数校验 |
| Lombok | - | 简化代码 |

### 前端（message）

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 2.6.14 | 前端框架 |
| Vue Router | 3.5.1 | 路由管理 |
| Vuex | 3.6.2 | 状态管理 |
| Element UI | 2.15.14 | UI 组件库 |
| Axios | 0.27.2 | HTTP 请求 |

---

## 三、功能说明

### 1. 留言发布

- 发布留言，分类为**问题反馈**（question）或**需求建议**（requirement）
- 标题 + 内容 + 手机号 + 附件
- 发布人信息（UID、姓名、机构）**自动获取**，无需手动填写

### 2. 用户信息集成

- 用户信息来自企业现有用户库 `syngbs` 数据库
  - `syngbs.manex` 表：用户 ID、姓名（xm）、机构 ID（jgid）
  - `syngbs.organ` 表：机构名称（jgmc）
- 通过请求头/会话中的 UID 自动查询，返回完整用户信息

### 3. 评论与回复

- 多级评论树：顶级评论 + 子回复
- 回复可针对留言或某条评论，显示被回复人
- 任何人可回复任意留言/评论

### 4. 点赞

- 点赞/取消点赞（toggle 模式）
- 同一用户对同一留言只能点一次（唯一索引约束）
- 不能给自己发布的留言点赞

### 5. 热榜排行

- **点赞热榜**：按点赞数倒序，可指定 Top N（默认 10）
- **评论热榜**：按评论数倒序，可指定 Top N（默认 10）

### 6. 附件上传

- 支持多附件上传
- 允许类型：jpg、jpeg、png、gif、bmp、pdf、doc、docx、xls、xlsx、ppt、pptx、txt、zip、rar
- 单个文件最大 10MB，按日期归档存储

### 7. 操作日志

- AOP 切面自动记录操作日志
- 记录类型：发布留言（CREATE）、评论（COMMENT）、点赞（LIKE）、取消点赞（UNLIKE）
- 记录操作人、操作描述、关联留言 ID、请求 IP

---

## 四、数据库设计

数据库名：`message_board`（utf8mb4），共 4 张核心表：

| 表名 | 说明 |
|------|------|
| `tb_message` | 留言表（帖子） |
| `tb_comment` | 评论表 |
| `tb_like` | 点赞表 |
| `tb_operation_log` | 操作日志表 |

### 关键字段说明

- `tb_message.category`：question=问题反馈，requirement=需求建议
- `tb_message.uid`：发布人 UID（来自 syngbs.manex.id）
- `tb_message.user_name`：发布人姓名（syngbs.manex.xm）
- `tb_message.org_name`：发布人机构（syngbs.organ.jgmc）
- `tb_message.file_paths` / `file_names`：附件路径和原始文件名（逗号分隔）
- `tb_comment.parent_id`：0=顶级评论，非0=回复的评论 ID
- `tb_like` 唯一索引 `uk_message_uid`：防止重复点赞
- 所有表使用 `status` 字段做逻辑删除（MyBatis Plus 逻辑删除配置）

### 外部数据库

留言板依赖企业已有的 `syngbs` 数据库（只读），通过 `ExternalUserMapper` 查询：

| 表 | 用途 |
|----|------|
| `syngbs.manex` | 用户表（id、xm 姓名、jgid 机构ID） |
| `syngbs.organ` | 机构表（jgmc 机构名称） |

---

## 五、环境要求与部署

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 14+（前端构建）
- Tomcat 8.5+（war 部署）

### 后端部署（war 包）

1. 初始化数据库：
   ```bash
   mysql -u root -p < sql/message_board.sql
   ```
2. 修改 `message-board/src/main/resources/application.yml`：
   - 数据库连接（message_board 和 syngbs）
   - 附件上传路径 `file.upload.path`
3. 打包：
   ```bash
   cd message-board
   mvn clean package
   ```
4. 将生成的 `message-board.war` 部署到 Tomcat 的 `webapps` 目录

> Context Path 为 `/message-board`（已在 application.yml 配置，war 包名需与之一致）

### 前端部署

1. 安装依赖并构建：
   ```bash
   cd message
   npm install
   npm run build
   ```
2. 构建产物位于 `message/` 目录，部署到 Web 服务器（Nginx）
3. `publicPath` 为 `/message`，Nginx 配置参考项目根目录 `nginx.conf`

### 开发环境

- 后端：`mvn spring-boot:run`（端口 8088）
- 前端：`npm run serve`（端口 8080，已配置代理到 8088）

---

## 六、核心模块说明

### 后端模块

| 模块 | 说明 |
|------|------|
| `MessageController` | 留言列表/详情/发布/点赞/热榜 |
| `CommentController` | 评论发布 |
| `UserController` | 获取当前用户信息 |
| `FileController` | 附件上传 |
| `MessageService` | 留言业务逻辑 |
| `CommentService` | 评论业务逻辑 |
| `ExternalUserService` | 外部用户信息查询 |
| `OperationLogAspect` | 操作日志 AOP 切面 |
| `UidUtils` | 从请求头/会话获取当前用户 UID |

### 前端模块

| 模块 | 说明 |
|------|------|
| `MessageList.vue` | 留言列表（首页，右侧含热榜） |
| `MessageDetail.vue` | 留言详情（含评论树、回复、点赞） |
| `HotRank.vue` | 热榜排行组件 |

---

## 七、接口说明

后端接口统一前缀：`/message-board/api`

### 用户接口

| 接口 | 说明 |
|------|------|
| `GET /user/current` | 获取当前登录用户信息 |

### 留言接口

| 接口 | 说明 |
|------|------|
| `GET /message/list` | 分页查询留言列表（支持 category 筛选） |
| `GET /message/detail/{id}` | 留言详情（含评论树） |
| `POST /message/publish` | 发布留言 |
| `POST /message/like` | 点赞/取消点赞 |
| `GET /message/hot/like` | 点赞热榜 |
| `GET /message/hot/comment` | 评论热榜 |

### 评论接口

| 接口 | 说明 |
|------|------|
| `POST /comment/add` | 发表评论/回复 |

### 文件接口

| 接口 | 说明 |
|------|------|
| `POST /file/upload` | 上传附件 |

---

## 八、注意事项

1. **外部用户库依赖**：系统依赖企业已有 `syngbs` 数据库，需在配置中正确配置该数据源，否则发布人信息无法自动获取。
2. **UID 获取方式**：`UidUtils.getCurrentUid()` 从请求头（如 `X-User-Id` 或 Session）中获取当前登录用户的 UID，需与企业的统一认证体系对接。
3. **附件路径**：默认 `D:/uploads/message/`，生产环境请修改为实际存储路径并确保可写。
4. **点赞限制**：不能给自己的留言点赞，通过 `tb_like` 唯一索引 `(message_id, uid)` 防止重复点赞。
5. **逻辑删除**：留言和评论采用逻辑删除（`status` 字段），未物理删除数据。
6. **日志**：默认日志路径 `D:/opt/logs/message-board/`，单个文件 10MB，保留 30 天。
7. **数据库升级**：v1.1.0 新增了附件和手机号字段，旧库需执行 `sql/upgrade.sql`。
