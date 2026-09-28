# 瑞吉外卖（reggie_2402）

基于 Spring Boot 2.7.6 + MyBatis-Plus 的外卖订餐系统，为《Java 软件开发综合实训》课程项目。包含管理端（员工/分类/菜品/套餐/订单管理）与用户端（验证码登录/地址管理/菜品浏览/购物车/下单），并嵌入了基于大模型的知识问答 AI 智能客服。

## 技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| JDK | 11+（本项目用 17 编译） | 运行环境 |
| Spring Boot | 2.7.6 | 核心框架 |
| MyBatis-Plus | 3.4.2 | ORM、分页插件、雪花算法主键、公共字段自动填充 |
| MySQL | 8.0 | 业务数据库（11 张表） |
| Druid | 1.1.23 | 数据库连接池 |
| FastJSON | 1.2.76 | JSON 处理 |
| Lombok | 1.18.36 | 简化实体类 |
| Redis | 5.0+（可选） | 缓存，本项目运行不强制依赖 |
| OpenAI 兼容 API | DeepSeek 等 | AI 智能客服（JDK 自带 HttpClient 调用） |

前端为教师分发资源：`backend/`（管理端，Vue + ElementUI）、`front/`（用户端，移动端 H5），以静态资源方式由后端托管。

## 功能模块

### 管理端（/backend）
- **员工登录**：MD5 密码校验、Session 存储、登录检查过滤器（白名单 + AntPathMatcher）
- **分类管理**：新增/分页/修改/删除（删除前校验是否被菜品或套餐关联），公共字段（createTime/createUser 等）由 MyMetaObjectHandler + ThreadLocal 自动填充
- **菜品管理**：图片上传/回显（UUID 重命名）、新增含口味菜品（dish + dish_flavor 两表事务保存）、分页查询（回填分类名）、修改回显（口味先删后增）、批量启售/停售、批量删除（启售中禁止）
- **套餐管理**：新增含关联菜品套餐（setmeal + setmeal_dish 两表事务）、分页/回显/修改/启售停售/删除，模式与菜品一致
- **订单管理**：订单分页（订单号模糊 + 时间范围）、查看订单明细、派送/完成状态流转

### 用户端（/front）
- **验证码登录**：4 位验证码存 Session（无短信服务，验证码输出到控制台模拟发送），新手机号自动注册
- **地址管理**：增删改查 + 唯一默认地址（先全部置 0 再目标置 1）
- **菜品/套餐展示**：按分类查启售菜品（含口味，供“选择规格”弹窗）与套餐
- **购物车**：加购（已存在数量 +1）、减购（数量 1 时删除）、查看、清空，全部按用户隔离
- **下单**：六步流程——查购物车 → 雪花订单号 → BigDecimal 算总价 → 补地址/用户信息 → 存订单 + 明细 → 清空购物车，全程事务

### AI 智能客服（/ai/chat）
- 三段式编排：**意图识别 → 工具路由 → 模型总结**
- 四类意图：ORDER_QUERY（查订单）/ ORDER_DETAIL_QUERY（查明细）/ DISH_QUERY（查菜品）/ SMALL_TALK（闲聊）
- 工具层复用业务 Service 查询真实数据，提示词约束“仅根据查询结果回答，不编造数据”
- 容错：JSON 解析失败自动纠正重试（3 次）；模型调用失败指数退避重试（500ms→1s→2s），最终返回兜底话术，接口不报 500

## 项目结构

```
reggie_2402
├── src/main/java/org/example
│   ├── Reggie_2402Application.java   # 启动类（@ServletComponentScan + @EnableTransactionManagement）
│   └── reggie_2402
│       ├── ai          # AI 智能客服（LlmConfig/LlmClient/JsonOutputService/AiToolsService/AiAgentService/AiAgentController）
│       ├── common      # R 统一返回、BaseContext(ThreadLocal)、全局异常处理、公共字段填充、Jackson 配置
│       ├── config      # 静态资源映射 + 消息转换器、MyBatis-Plus 分页插件
│       ├── controller  # 员工/分类/菜品/套餐/用户/地址/购物车/订单/文件上传 控制器
│       ├── dto         # DishDto / SetmealDto / OrdersDto
│       ├── entity      # 11 个实体类（与 11 张表一一对应）
│       ├── filter      # LoginCheckFilter 登录检查（员工 + 用户双体系）
│       ├── mapper      # BaseMapper 数据访问层
│       ├── service     # 业务接口与实现（impl 子包）
│       └── utils       # ValidateCodeUtils 验证码工具
├── src/main/resources
│   ├── application.yml
│   ├── backend/        # 管理端前端
│   └── front/          # 用户端前端（page/ai-chat.html 为 AI 问答页）
└── chapter01/          # 教师分发的基础代码与前端资源（备份）
```

## 快速开始

### 1. 环境要求
JDK 11+、Maven 3.8+、MySQL 8.0、IDEA（装 Lombok 插件）

### 2. 数据库准备
```sql
CREATE DATABASE db_reggie DEFAULT CHARACTER SET utf8;
USE db_reggie;
SOURCE D:/db_reggie.sql;   -- 教师分发的脚本
```
确认有 11 张表，且 `employee` 表存在账号 **admin**（明文密码 123456，库中为 MD5）。

### 3. 修改配置 `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    druid:
      username: root
      password: 你的MySQL密码
llm:
  api-key: 你的大模型API Key   # 不用 AI 功能可留空
```
> URL 中 `useSSL=false&allowPublicKeyRetrieval=true` 两个参数必须保留，否则 MySQL 8 会报 `Public Key Retrieval is not allowed`。

### 4. 启动
运行 `Reggie_2402Application.main()`，控制台出现“启动成功”即可。

### 5. 访问
| 入口 | 地址 |
|---|---|
| 管理端登录页 | http://localhost:8080/backend/page/login/login.html |
| 用户端登录页 | http://localhost:8080/front/page/login.html （F12 切手机模式） |
| AI 问答页 | http://localhost:8080/front/page/ai-chat.html （需先用户端登录） |

> 端口以 `application.yml` 的 `server.port` 为准（当前 8080）。

## 综合验收流程（自测路径）
管理端登录 → 新增分类 → 新增菜品（含口味）→ 新增套餐 → 启售 → 用户端登录（控制台看验证码）→ 浏览菜品加购 → 下单 → 管理端查看订单并派送/完成 → AI 问答页提问“我的订单到哪了”。

## 常见问题（踩坑记录）

**1. 页面能打开但接口全 404**
检查浏览器地址栏端口：如果是 `63342`，说明你是从 IDEA 右键预览打开的页面（IDEA 内置服务器，没有后端）。必须通过 `http://localhost:8080/backend/...` 访问，让 Spring Boot 同时托管页面和接口。

**2. 登录无响应/提示后端接口错误**
- MySQL 服务没启动：`services.msc` 找到 MySQL80 启动；服务不存在则用管理员 PowerShell 执行：
  ```powershell
  & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqld.exe" --install MySQL80 --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.0\my.ini"
  net start MySQL80
  ```
- 密码不匹配：确认 `application.yml` 的密码与 MySQL root 实际密码一致（`mysql -uroot -p你的密码` 验证）
- MySQL 8 认证报错 `Public Key Retrieval is not allowed`：URL 缺少 `allowPublicKeyRetrieval=true`

**3. 用户端接口返回 NOTLOGIN**
未登录或 Session 丢失；AI 问答、地址、购物车、订单接口都不在过滤器白名单中，需先在**同一浏览器窗口**完成用户端登录。管理端和用户端建议分开窗口（或无痕窗口）测试，避免 Session 互相影响。

**4. AI 问答返回兜底话术**
检查 `application.yml` 的 `llm.api-key` 是否已填写有效 Key；控制台会打印重试日志，重试 3 次后返回“智能客服暂时开小差”属正常容错行为。

## 主要接口一览

| 模块 | 接口 |
|---|---|
| 员工 | POST /employee/login、POST /employee/logout |
| 分类 | GET /category/page、POST/PUT/DELETE /category、GET /category/list |
| 菜品 | POST/PUT/DELETE /dish、GET /dish/page、GET /dish/{id}、POST /dish/status/{n}、GET /dish/list |
| 套餐 | POST/PUT/DELETE /setmeal、GET /setmeal/page、GET /setmeal/{id}、POST /setmeal/status/{n}、GET /setmeal/list |
| 文件 | POST /common/upload、GET /common/download |
| 用户 | POST /user/sendMsg、POST /user/login |
| 地址 | POST/PUT/DELETE /addressBook、GET /addressBook/list、GET /addressBook/default、PUT /addressBook/default |
| 购物车 | POST /shoppingCart/add、POST /shoppingCart/sub、GET /shoppingCart/list、DELETE /shoppingCart/clean |
| 订单 | POST /order/submit、GET /order/page、GET /order/userPage、PUT /order |
| AI | POST /ai/chat（body: {"question":"..."}）|

---

*实训课程项目 · 岳阳职业技术学院 信息工程学院 · 参考指导书《Java 软件开发综合实训指导书》*
