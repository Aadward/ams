-- V1__init_schema.sql
-- Initial database schema for AMS

CREATE TABLE IF NOT EXISTS `employee` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '姓名',
    `dept` VARCHAR(100) COMMENT '部门',
    `email` VARCHAR(255) COMMENT '邮箱',
    `phone` VARCHAR(50) COMMENT '电话',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_employee_name` (`name`),
    INDEX `idx_employee_dept` (`dept`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工/使用人表';

CREATE TABLE IF NOT EXISTS `asset` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '资产编码，如 PC-2024-0001',
    `name` VARCHAR(200) NOT NULL COMMENT '资产名称',
    `category` VARCHAR(30) NOT NULL COMMENT '分类：HARDWARE/NETWORK/PERIPHERAL/SOFTWARE_LICENSE',
    `status` VARCHAR(30) NOT NULL DEFAULT 'IN_STOCK' COMMENT '状态：IN_STOCK/IN_USE/MAINTENANCE/RETIRED',
    `spec` VARCHAR(500) COMMENT '规格描述',
    `purchase_date` DATE COMMENT '采购日期',
    `purchase_price` DECIMAL(12,2) COMMENT '采购价格（元）',
    `warranty_end` DATE COMMENT '保修到期日',
    `supplier` VARCHAR(255) COMMENT '供应商',
    `location` VARCHAR(255) COMMENT '当前存放/使用地点',
    `assignee_id` BIGINT COMMENT '领用人 FK→employee.id，可为空',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '软删除标记',
    INDEX `idx_asset_code` (`asset_code`),
    INDEX `idx_asset_category` (`category`),
    INDEX `idx_asset_status` (`status`),
    INDEX `idx_asset_assignee` (`assignee_id`),
    CONSTRAINT `fk_asset_assignee` FOREIGN KEY (`assignee_id`) REFERENCES `employee` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表';

CREATE TABLE IF NOT EXISTS `maintenance_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_id` BIGINT NOT NULL COMMENT 'FK→asset.id',
    `type` VARCHAR(30) NOT NULL COMMENT '维修类型：REPAIR/MAINTENANCE/INSPECTION',
    `description` VARCHAR(1000) COMMENT '维修描述',
    `cost` DECIMAL(12,2) DEFAULT 0 COMMENT '维修费用（元）',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE COMMENT '完成日期，NULL表示未完成',
    `vendor` VARCHAR(255) COMMENT '维修商',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_maint_asset` (`asset_id`),
    INDEX `idx_maint_start_date` (`start_date`),
    CONSTRAINT `fk_maint_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维修记录表';

CREATE TABLE IF NOT EXISTS `asset_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_id` BIGINT COMMENT 'FK→asset.id',
    `action` VARCHAR(30) NOT NULL COMMENT '操作类型：CREATE/UPDATE/ASSIGN/UNASSIGN/MAINTENANCE/RETIRE/RESTORE',
    `operator` VARCHAR(100) NOT NULL COMMENT '操作人',
    `detail` TEXT COMMENT '操作详情，JSON字符串',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_log_asset` (`asset_id`),
    INDEX `idx_log_action` (`action`),
    INDEX `idx_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产操作日志表（MySQL落库）';
