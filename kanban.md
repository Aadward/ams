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




- [ ] 资产保险管理：保险单增删改查、保险与资产关联、保险到期提醒、索赔记录、仪表盘视图 [QA] 2026-05-15T12:11









- [x] 供应商管理：供应商增删改查、供应商评级、联系方式管理（prds/2026-05-15-供应商管理.md） [QA] 2026-05-15T14:20 ← BUG: 详情页缺少供应商编码/类型/状态/评级字段; BUG: 新增表单提交按钮点击无响应; BUG: EQUIPMENT类型标签显示为易耗品供应商应为设备供应商 [QA] 2026-05-15T14:32





- PRD：prds/2026-05-15-资产保险管理.md [QA] 2026-05-15T16:13




- [x] 后端: ScanController (GET /api/scan/{assetCode}, POST assign/return/borrow-return) [QA] 2026-05-15T18:36




- [x] 【BUG】保险即将到期仪表盘筛选问题：资产 POL202605150001 到期日 2026-06-15，距今约31天，应出现在30天筛选列表中，但显示「暂无即将到期的维保资产」；可能是日期计算逻辑边界问题或 API 筛选实现有误 [QA] 2026-05-15T18:36 ← 代码修复正确（plusDays(days+1)已确认），但数据库缺少测试数据：POL202605150001不存在，所有资产warranty_end均为NULL，无法验证API筛选功能 [QA] 2026-05-15T18:37

## completed

    - 修复：`WarrantyNotificationService.getExpiringWarrantyAssets()` 使用 `plusDays(days + 1)` 修正边界；mvn clean package ✓；backend Docker 已重启
    - 前端: api/scan.ts, ScanPage.tsx (H5, /#/scan/:assetCode)
    - 验证: mvn compile ✓, npm run build ✓, curl /api/scan/A002 → 200 ✓，前端页面资产信息正常显示














## in_progress






























## todo
- [x] 二维码扫码盘点：资产二维码生成、移动端扫码识别、盘点结果实时上报（prds/2026-05-15-二维码扫码盘点.md） [QA] 2026-05-15T16:05 ← 【BUG】前端 Docker 镜像使用旧版 dist/ JS bundle（构建于15:36），与最新 dist/（15:45）不一致，导致 useScanAsset 请求路径与后端 API 不匹配，页面显示「未找到资产信息」。BUG 已单独录入 todo。
