-- V2__add_department.sql
-- Add department management and update employee with dept_id FK

CREATE TABLE IF NOT EXISTS `department` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `parent_id` BIGINT COMMENT '上级部门 FK→department.id，可为空（表示根部门）',
    `description` VARCHAR(500) COMMENT '部门描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_dept_parent` (`parent_id`),
    CONSTRAINT `fk_dept_parent` FOREIGN KEY (`parent_id`) REFERENCES `department` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- Add dept_id to employee table
ALTER TABLE `employee` ADD COLUMN `dept_id` BIGINT AFTER `name`;
ALTER TABLE `employee` CHANGE COLUMN `dept` `dept_name` VARCHAR(100) COMMENT '部门名称（冗余字段，方便查询）';
ALTER TABLE `employee` ADD INDEX `idx_employee_dept_id` (`dept_id`);
ALTER TABLE `employee` ADD CONSTRAINT `fk_employee_dept` FOREIGN KEY (`dept_id`) REFERENCES `department` (`id`) ON DELETE SET NULL;
