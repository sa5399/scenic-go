# 游小景 · 前端

Vue 3 + TypeScript + Vite 单页应用，同工程双端：游客端 H5（`/`，Vant 4）与管理端桌面（`/admin`，Element Plus）。

> 选型与架构见 [《前端技术栈》](../docs/前端技术栈.md)，接口契约见 [《前端接口文档》](../docs/前端接口文档.md)——写代码前先读这两份。

## 常用命令

```sh
npm install        # 安装依赖（Node ≥ 20.19，推荐 22/24 LTS）
npm run dev        # 开发：http://localhost:5173，/api 代理到后端 8080（免跨域）
npm run build      # 类型检查 + 生产构建 → dist/
npm run preview    # 本地预览构建产物（同样走 /api 代理）
npm run lint       # oxlint + eslint
```

前提：后端跑在 `http://localhost:8080`（见仓库根的 Spring Boot 工程）。没起后端时数据页显示错误态/空态，属预期。

测试账号（密码统一 `123456`）：`linxiaoyu`（会员）、`checkin01`（核销员）、`admin`（管理员）。

## 结构速览

```
src/
├─ api/        # 10 个模块与后端接口一一对应（薄封装）
├─ types/api.ts# 全部接口的 TS 类型（与接口文档逐字段对齐）
├─ utils/      # request（axios 统一拆包/401 处理）、token、format、h5ui、message
├─ stores/     # auth（登录态）、tags（标签字典）
├─ router/     # h5.ts + admin.ts 两棵路由树 + 登录/角色守卫
├─ layouts/    # H5Layout（Tabbar）/ AdminLayout（侧边栏）
├─ views/      # h5/（9 页全量）、admin/（核销工作台/流水 + 4 个等后端的占位）
└─ styles/tokens.css  # 设计令牌：湖青主题唯一事实来源，换肤只改这里
```

## 静态图片

后端种子数据引用 `public/img/` 下 42 张图（`spot/01-12.jpg`、`exh/01-20.jpg`、`avatar/01-10.png`）。当前是**生成的主题占位图**（海报化湖区小景 / 青瓷器形 / 几何头像），重新生成：`node scripts/gen-placeholders.mjs`。真实照片到位后按同名路径覆盖即可，页面无感切换。

登录背景用真实照片（Pexels，免署名可商用）：`bg/login.jpg`（烟雨层山）、`bg/register.jpg`（青绿湖水）；生成的插画版保留为 `bg/poster-*.jpg`，文件名对调即可切回。

## 生产部署

构建产物 `dist/` 交 nginx 托管，接口反代见根目录 [nginx.conf](./nginx.conf)（`/api/` → 8080，history 兜底，assets 长缓存）。
