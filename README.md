# ScenicGo · 游小景 — 景区预约系统

面向景区游客的预约全链路系统：**浏览 → 分时预约 → 支付 → 核销入园 → 评价**，
同一工程提供游客端 H5 与管理端桌面两套界面，重点打磨并发下单不超卖、缓存防护与核销幂等。

## 功能全景

**游客端（H5，`/`）**

| 模块 | 说明 |
|---|---|
| 景点 / 展馆 | 列表分页、多标签筛选、详情（含展品、标签）、搜索 |
| 入园预约 | 园区票分时预约，0 元单提交即出票、免支付 |
| 分时预约 | 按日期 × 时段 × 票种下单，15 分钟倒计时支付，取消 / 超时关单自动回补库存 |
| 我的订单 | 订单列表（状态筛选）、详情（核销码）、模拟支付 |
| 评价 | 已核销订单可关联评价，点赞（Redis Set 去重） |
| 导览 | 标签 + 人群 + 时间预算的推荐路线（纯规则实现）、附近设施（Redis GEO） |
| 公告 | 景区公告浏览 |

**管理端（`/admin`）**

| 模块 | 说明 |
|---|---|
| 概览 | 关键指标统计 |
| 景点管理 | 上下架、名称 / 简介 / 价格编辑（改库后删详情缓存） |
| 时段与库存 | 时段票种余量查看、总库存直调 |
| 订单查询 | 全量订单分页，支持日期范围与状态筛选 |
| 核销 | 核销工作台（核销员 role=2）、核销流水（管理员 role=3） |
| 公告维护 | 发布 / 下架 / 编辑 / 删除 |

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Java 21 · Spring Boot 3.5 · MyBatis-Plus · Redisson · springdoc-openapi · spring-security-crypto（BCrypt） |
| 前端 | Vue 3 · TypeScript · Vite · Vant 4（H5）· Element Plus（管理端）· Pinia |
| 存储 | MySQL 8 · Redis 8 · RabbitMQ 4（削峰预留，业务暂未启用） |
| 部署 | Docker Compose（中间件）· nginx（前端托管 + 接口反代） |

## 架构与目录

```
浏览器(H5 /admin) ──→ 前端 SPA ──/api──→ Spring Boot 单体(8080)
                                          ├─→ MySQL 8     (业务数据)
                                          ├─→ Redis 8     (登录态/缓存/库存/GEO/点赞)
                                          └─→ RabbitMQ    (预留)
```

```
scenic-go/
├─ app/            # 后端单体，按业务分包：spot / auth / user / reserve / review / guide / admin / mq
├─ common/         # 通用代码（Result / 分页 / 异常等）
├─ frontend/       # 双端一体 SPA（nginx 配置见 frontend/nginx.conf）
├─ init/sql/       # 建表 + 造数脚本（01~06 顺序执行，幂等可重跑）
└─ docker-compose.yml
```

> 后端各包内部分层为 `controller / service / mapper`，缓存与库存等横切逻辑在 `utils` 与 `config`。

## 快速启动

前置：Docker、JDK 21、Node ≥ 20.19。

```sh
# 1. 起中间件（MySQL / Redis / RabbitMQ）
docker compose up -d

# 2. 建表 + 造数（幂等，可重复执行；时段库存按当天日期向后生成 7 天）
for f in init/sql/*.sql; do
  docker exec -i scenic-mysql mysql -uroot -proot scenic_go < "$f"
done

# 3. 后端（8080）：IDEA 直接跑 ScenicGoApplication，或
mvn -pl app -am spring-boot:run
#    接口文档(Swagger)：http://localhost:8080/swagger-ui.html

# 4. 前端（5173，/api 代理到 8080）
cd frontend && npm install && npm run dev
```

浏览器打开 `http://localhost:5173`。生产部署：`npm run build` 后用 `frontend/nginx.conf`
托管 `dist/` 并反代 `/api/`（`history` 兜底、`assets` 长缓存已配好）。

> `frontend/public/img/` 下 42 张图（景点 / 展品 / 头像）是后端种子数据引用的**生成的主题占位图**，
> 真实照片到位后按同名路径覆盖即可；重新生成占位图：`node frontend/scripts/gen-placeholders.mjs`。

### 测试账号（密码统一 `123456`）

| 账号 | 角色 | 用途 |
|---|---|---|
| `linxiaoyu` | 会员 | 游客端全链路：预约 / 支付 / 评价 |
| `checkin01` | 核销员 | 管理端核销工作台 |
| `admin` | 管理员 | 管理端全部功能 + 核销流水 |

## 设计要点

- **并发不超卖**：下单走 Lua 脚本原子预扣 Redis 余量，DB 层 `CHECK` 约束兜底；
  `sold_stock` 记账口径含待支付（下单即占用，取消 / 超时回补），可对账：
  `Redis 余量 == total_stock - sold_stock`，`sold_stock 合计 == 占用中订单票数`
- **缓存防护**：景点详情逻辑过期 + Redisson 锁异步重建（可用性优先）；
  查不到的 id 缓存空值防穿透；改库后删缓存（Cache-Aside）
- **核销幂等**：带状态条件的 UPDATE + `uk_reserve` 唯一键，同码并发核销只成功一次
- **超时关单**：定时任务每分钟扫描，15 分钟未支付关单并回补库存
- **0 元单**：免费景点 / 园区票提交即出票，直接生成核销码，不走支付状态机

> 仓库中的数据库 / 中间件密码均为本地开发默认值（`root` / `guest`），非生产凭证。
