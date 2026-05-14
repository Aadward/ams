-- V4__add_notification_approval.sql
-- Add notification and approval_request tables

CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '通知接收用户 FK→employee.id',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `message` TEXT COMMENT '通知内容',
    `type` VARCHAR(30) NOT NULL COMMENT '通知类型：ASSET_ASSIGNED/ASSET_RETURNED/MAINTENANCE_DUE/APPROVAL_REQUIRED/SYSTEM',
    `is_read` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `read_at` DATETIME COMMENT '读取时间',
    INDEX `idx_notification_user_id` (`user_id`),
    INDEX `idx_notification_is_read` (`is_read`),
    INDEX `idx_notification_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

CREATE TABLE IF NOT EXISTS `approval_request` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `requester_id` BIGINT NOT NULL COMMENT '申请人 FK→employee.id',
    `asset_id` BIGINT NOT NULL COMMENT '关联资产 FK→asset.id',
    `department_id` BIGINT NOT NULL COMMENT '所属部门 FK→department.id',
    `type` VARCHAR(30) NOT NULL COMMENT '申请类型：ASSET_ASSIGNMENT/ASSET_RETURN/MAINTENANCE',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/APPROVED/REJECTED',
    `reason` VARCHAR(500) COMMENT '申请理由',
    `manager_comment` VARCHAR(500) COMMENT '审批意见',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `resolved_at` DATETIME COMMENT '处理时间',
    INDEX `idx_approval_requester` (`requester_id`),
    INDEX `idx_approval_asset` (`asset_id`),
    INDEX `idx_approval_status` (`status`),
    INDEX `idx_approval_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产审批请求表';
