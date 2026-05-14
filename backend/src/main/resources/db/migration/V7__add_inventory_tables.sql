-- Inventory Plan table
CREATE TABLE IF NOT EXISTS inventory_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '盘点计划名称',
    scope_type VARCHAR(20) NOT NULL COMMENT '范围类型：DEPARTMENT/CATEGORY',
    department_ids TEXT NULL COMMENT '按部门时的部门ID列表，JSON数组',
    category_ids TEXT NULL COMMENT '按分类时的分类ID列表，JSON数组',
    plan_date DATE NOT NULL COMMENT '计划盘点日期',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/IN_PROGRESS/COMPLETED',
    creator_id BIGINT NOT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_plan_date (plan_date),
    INDEX idx_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inventory Task table
CREATE TABLE IF NOT EXISTS inventory_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL COMMENT '关联盘点计划ID',
    asset_id BIGINT NOT NULL COMMENT '关联资产ID',
    assignee_id BIGINT NOT NULL COMMENT '盘点负责人ID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/CHECKED',
    checked_at DATETIME NULL COMMENT '盘点时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plan_id (plan_id),
    INDEX idx_asset_id (asset_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_status (status),
    UNIQUE KEY uk_plan_asset (plan_id, asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inventory Record table
CREATE TABLE IF NOT EXISTS inventory_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL COMMENT '关联盘点任务ID',
    plan_id BIGINT NOT NULL COMMENT '关联盘点计划ID',
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    asset_code VARCHAR(100) NOT NULL COMMENT '资产编码',
    asset_name VARCHAR(255) NOT NULL COMMENT '资产名称',
    department_id BIGINT NULL COMMENT '部门ID',
    department_name VARCHAR(255) NULL COMMENT '部门名称',
    result VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '盘点结果：NORMAL/SURPLUS/MISSING/PENDING',
    checked_by BIGINT NULL COMMENT '盘点人ID',
    checked_at DATETIME NULL COMMENT '盘点时间',
    remark TEXT NULL COMMENT '备注（盘盈/盘亏说明）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plan_id (plan_id),
    INDEX idx_task_id (task_id),
    INDEX idx_result (result),
    INDEX idx_checked_by (checked_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
