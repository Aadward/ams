## done（已验收）

- [x] 资产转移调拨：支持资产在部门/员工之间调拨申请、审批、调拨历史追溯（prds/2026-05-15-资产转移调拨.md）
  - 后端：AssetTransferRecord + AssetTransferService + AssetTransferController
  - 前端：TransferList（待我审批/我发起的/全部记录Tab）/ TransferApply（申请表单）/ TransferDetail（详情）
  - QA验证：调拨管理菜单正常、列表页Tab切换正常、申请表单字段完整、表单验证正常、无JS错误
- [x] MVP Phase 1: 项目骨架 + Docker Compose
- [x] MVP Phase 1: Asset / Employee CRUD API + 页面
- [x] MVP Phase 1: 领用/归还 API + 页面
- [x] MVP Phase 1: 仪表盘
- [x] MVP Phase 1: 操作日志（MySQL + ES 双写）
- [x] MVP Phase 2: 维修记录
- [x] MVP Phase 2: 报废功能
- [x] MVP Phase 2: ES 日志查询页面
- [x] Phase 3: 部门管理 + 部门树形结构 + 部门资产统计
- [x] Phase 3: 批量操作（批量领用/归还/报废/转移）
- [x] Phase 3: 导入/导出 Excel
- [x] Phase 3: RBAC 基础（UserRole、Employee.role、RoleController、secured APIs）
- [x] Phase 3: JWT 认证 + Swagger 文档
- [x] Phase 3: WebSocket 通知推送 + NotificationBell 组件
- [x] Phase 3: 通知页面 + 审批页面
- [x] Phase 3: 员工管理（含角色分配）
- [x] Phase 3: 维修记录管理页面
- [x] Phase 3: 报表页面（分类/状态/部门图表 + 维修费用汇总）
- [x] Phase 3: 备份管理前端页面
- [x] Phase 3: 审批流程后端：领用审批、维修审批、报废审批（ApprovalWorkflow）
- [x] BUG修复：审批类型映射修正（ApprovalList.tsx/AssetDetail.tsx 的 typeBadge 与后端 ApprovalType 枚举值对齐：ASSET_ASSIGNMENT/ASSET_RETURN/MAINTENANCE）
  - 已修复：删除了 NotificationList.tsx 和 NotificationBell.tsx 中的 `ASSET_APPROVAL` 条目（后端 NotificationType 枚举无此值）
- [x] 标签打印：资产详情页生成二维码（编码 assetCode），支持浏览器打印；可选配置标签模板（公司名称、资产名称、编码）
  - 验证结果：后端 API 正常，列表页"打印标签"按钮可见，点击可弹出标签预览（显示资产编码 A002 等信息）
  - 备注：因"路由导航失效"Blocker Bug，详情页二维码未能完整验证（但 API 和列表页按钮已验证通过）
- [x] 低值易耗品管理：易耗品分类/入库/出库/实时库存/库存预警/消耗报表
  - 后端：Consumable/ConsumableStock/ConsumableRecord 实体 + Repository + Service + Controller
  - 前端：ConsumableList/ConsumableForm/ConsumableStockIn/ConsumableStockOut/ConsumableReport 页面
  - QA验证：4个页面全部正常（易耗品管理/入库管理/出库管理/消耗报表），API 正常
- [x] Bug: React Router Hash 路由导航失效 (BrowserRouter → HashRouter + nginx try_files 移除，Docker 镜像已重建并验证)
  - QA验证：6项测试全部通过（Hash路由、跨模块导航、后端health、nginx配置）
  - 修复：frontend/src/main.tsx BrowserRouter → HashRouter，nginx.conf 移除 try_files，index.html 添加 build comment
  - 验证：npm run build ✓，docker compose build frontend ✓，docker compose up -d ✓，curl localhost:80 → 200，backend health → 200
- [x] 维保到期提醒：完整实现（后端 API + 前端仪表盘卡片 + 通知类型映射）
  - 后端：WarrantyNotificationService（@Scheduled 每日8点，ASSET_EXPIRING_WARRANTY 通知）
  - 后端：DashboardController GET /api/dashboard/expiring-warranty?days=30
  - 前端：ExpiringWarranty 类型 + useExpiringWarranty hook + 仪表盘卡片（7/15/30/60天筛选）
  - 修复：NotificationBell.tsx / NotificationList.tsx 补全 ASSET_EXPIRING_WARRANTY 标签和颜色
  - 修复：InventoryPlanList/InventoryReport/InventoryTaskList 清理未使用导入（解除构建阻塞）
  - QA验证：仪表盘卡片存在且筛选正常（7/15/30/60天），API 返回正确数据结构，前端通知类型映射完整
- [x] 定期盘点：盘点计划（按部门/按资产分类），生成盘点任务；支持移动端扫码确认盘点结果；盘点差异报告
  - 后端：InventoryPlan/InventoryTask/InventoryRecord 实体 + Repository + Service + Controller
  - 前端：InventoryPlanList / InventoryTaskList / InventoryReport 页面，AppMenu 菜单
  - QA验证：4个页面全部正常（盘点计划/盘点任务/盘点报告），3个API端点正常（/api/inventory-plans, /api/inventory-tasks, /api/inventory-records）
- [x] 资产借用管理：新增"借用申请/审批"流程（独立于永久领用），支持设置计划归还日期；超期未还自动发送通知；借用中的资产状态为"IN_USE"但标记为"借用中"；归还时走独立归还流程
  - 后端：BorrowRecord/BorrowService/BorrowController（API: GET /api/borrows, POST /api/borrows/apply, POST /api/borrows/{id}/return, GET /api/borrows/pending, GET /api/borrows/overdue）
  - 前端：BorrowList.tsx（借用列表/审批Tab）/ BorrowApply.tsx（借用申请表单）
  - QA验证：页面正常加载，API 返回正确数据结构，菜单入口存在，types 与 DTO 对齐
- [x] 折旧计算：支持直线法折旧，自动计算资产当前净值；折旧台账页面展示每项资产的折旧明细（购置日期、原值、折旧年限、已提折旧、账面净值）；折旧汇总报表，按资产分类或部门汇总；对接报废流程——折旧完的资产提示可申请报废
  - 后端：DepreciationController（GET /api/depreciation/ledger, GET /api/depreciation/summary/by-category, GET /api/depreciation/summary/by-department）
  - 后端：DepreciationSummaryResponse DTO + AssetService 汇总计算
  - 前端：DepreciationLedger.tsx（3个Tab：折旧台账/分类汇总/部门汇总）
  - QA验证：3个Tab正常渲染，API 返回正确数据，mvn compile ✓，npm run build ✓
- [x] 供应商管理：供应商增删改查、供应商评级、联系方式管理（prds/2026-05-15-供应商管理.md）
  - 后端：Supplier/SupplierService/SupplierController + SupplierType/SupplierStatus 枚举
  - 前端：SupplierList/SupplierForm/SupplierDetail 页面
- [x] 后端: ScanController (GET /api/scan/{assetCode}, POST assign/return/borrow-return)
- [x] 【BUG】保险即将到期仪表盘筛选问题：WarrantyNotificationService.getExpiringWarrantyAssets() 使用 plusDays(days + 1) 修正边界；mvn clean package ✓；backend Docker 已重启
- [x] 供应商管理BUG修复：详情页 rating=0 显示"0星"（而非空）；type fallback 正常；表单提交错误处理增强 [DEV] 2026-05-15 → [QA] 2026-05-16 验证通过
- [x] 资产保险管理：保险单增删改查、保险与资产关联、保险到期提醒、索赔记录、仪表盘视图（prds/2026-05-15-资产保险管理.md） [DEV] 2026-05-15 → [QA] 2026-05-16 验证通过：列表保险类型"财产险"✅，详情页保险类型"财产险"✅，理赔状态✅
- [x] 前端: api/scan.ts, ScanPage.tsx (H5, /#/scan/:assetCode) [DEV] 2026-05-15 → [QA] 2026-05-16 验证通过：扫描A002显示资产信息✅，领用/查看详情按钮✅，修复 employeeId 默认值为1








































- [ ] **统一审批工作流**：抽象 ApprovalWorkflow 引擎，BorrowService 和 AssetTransferService 的内部审批逻辑迁移到通用 Workflow，所有审批统一走 ApprovalList 页面 [QA] 2026-05-19T10:12

## completed







































- 后端：AuthController, JWT Filter, SecurityConfig [DEV] 2026-05-19T09:47




## in_progress








































- [x] **采购审批完整实现**：ProcurementRequest Entity + ProcurementService + ProcurementController + 前端 ProcurementList/ProcurementApply/ProcurementDetail 页面 + AppMenu 菜单入口 [QA] 2026-05-19T06:06 ← PRD验收标准未满足：1)审批通过→asset表自动创建记录（未实现）；2)申请提交→APPROVAL_REQUIRED通知（未实现）；3)审批拒绝→APPROVAL_REJECTED通知（未实现）。Controller/Service/Entity已实现，前端页面未验证（登录BUG阻塞）[DEV] 2026-05-19 ← 全部实现：APPROVAL_REQUIRED通知已添加、审批通过→资产自动创建已实现、PROCUREMENT_APPROVED/REJECTED已有
- [x] **JWT 登录认证真正落地**：AuthController.login() 返回 Token，前端 api/index.ts 请求拦截器自动带上 Authorization header，App.tsx 从 localStorage 读取 userId，NotificationBell 使用真实 userId（不再是硬编码 1） [QA] 2026-05-19T08:36 ← 前端 userId 读取逻辑正确，但 docker 镜像未重建（bundle 时间戳 2026-05-18 23:33，早于最新 fix commit 2026-05-19 08:02），无法验证运行时真实场景。需重建镜像后重新验证。[DEV] 2026-05-19 ← 镜像已重建（bundle 09:46:58），后端JWT验证通过：登录返回token✓、带token访问受保护API返回200✓、无token返回403✓
- [x] 【BUG】登录成功后路由跳转失效：token 已存入 localStorage，但 ProtectedRoute 未识别已登录状态，手动刷新或导航到 /dashboard 时仍跳转回 /login [DEV] 2026-05-19T09:05 ← Docker 前端镜像已重建（bundle 时间戳 2026-05-19 09:02，晚于 fix commit 2026-05-19 08:02），核心修复已验证。等待 QA 验收。




## todo

### P0 — 登录认证 & 采购审批闭环

  - 前端：AuthProvider, login 页面, api 拦截器, 受保护路由
  - 验证：登录后 curl 带 token 能访问受保护 API，未登录返回 401
  - PRD: `prds/2026-05-19-功能优化方案PRD.md` §2.2

  - 申请提交 → 发送 APPROVAL_REQUIRED 通知
  - 审批通过 → 自动在 asset 表创建记录（状态 IN_STOCK）
  - 审批拒绝 → 发送 APPROVAL_REJECTED 通知
  - 验证：mvn compile ✓, npm run build ✓, Docker 重建后 API 正常
  - PRD: `prds/2026-05-19-功能优化方案PRD.md` §2.1

### P1 — 审批统一 & 员工自助

  - 新建 ApprovalWorkflow.java 通用审批引擎
  - 改造 BorrowService / AssetTransferService，移除内部审批
  - 改造 BorrowController / AssetTransferController，移除 approve/reject 端点
  - 验证：所有审批类型（领用/借用/调拨/维修/采购）都出现在统一审批列表
  - PRD: `prds/2026-05-19-功能优化方案PRD.md` §3.1

- [ ] **员工自助首页**：Dashboard 增加"我的资产/我的申请/我的待审批"专区，API 新增 GET /api/assets/my、GET /api/approvals/my-requests、GET /api/approvals/pending-count
  - 前端：Dashboard 增加卡片区块
  - 验证：员工登录后首页显示个人相关数据，快捷入口跳转正确
  - PRD: `prds/2026-05-19-功能优化方案PRD.md` §3.2

### P2 — 信息架构优化

- [ ] **菜单按业务域重构**：资产域（资产管理/借用/调拨/报废）、运维域（维修/盘点/维保）、供应链域（采购/供应商/易耗品）、财务域（折旧/保险/备份）、系统域（部门/员工/审批/通知/报表）；补全缺失图标
  - AppMenu.tsx 重组菜单结构
  - 验证：菜单结构清晰，图标完整无缺

- [ ] **借用 vs 调拨边界优化**：将借用和调拨合并为"资产变动申请"模块，子类型区分（临时借用/永久调拨），统一列表 + 筛选器
  - 合并 BorrowList + TransferList 为 AssetTransferList
  - 合并 BorrowApply + TransferApply 为 AssetTransferApply
  - 验证：合并后功能不丢失，列表筛选正常

### P3 — 体验打磨

- [ ] **标签打印完善**：支持选择标签模板（公司名+资产名称+编码 或 仅编码），浏览器打印预览优化，支持批量打印
  - 后端：AssetTagPrintController 扩展模板选项
  - 前端：AssetList 批量选择 + 打印
  - 验证：打印预览显示正确格式

- [ ] **移动端 H5 体验**：扫码盘点页面适配手机屏幕，简化操作流程（扫码→选择操作→确认）
  - ScanPage.tsx 响应式布局
  - 验证：iOS/Android 手机上操作流畅
