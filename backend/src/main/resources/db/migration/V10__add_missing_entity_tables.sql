-- V10__add_missing_entity_tables.sql
-- Missing tables that were created by Hibernate auto-DDL but not managed by Flyway
-- These correspond to JPA entities without dedicated migration files

-- Asset Transfer Record table
CREATE TABLE IF NOT EXISTS `asset_transfer_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_id` BIGINT NOT NULL,
    `from_employee_id` BIGINT NOT NULL,
    `to_employee_id` BIGINT NOT NULL,
    `from_department_id` BIGINT NOT NULL,
    `to_department_id` BIGINT NOT NULL,
    `approval_id` BIGINT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `reason` VARCHAR(500) NULL,
    `manager_comment` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `resolved_at` DATETIME NULL,
    INDEX `idx_atr_asset_id` (`asset_id`),
    INDEX `idx_atr_from_emp` (`from_employee_id`),
    INDEX `idx_atr_to_emp` (`to_employee_id`),
    INDEX `idx_atr_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Borrow Record table
CREATE TABLE IF NOT EXISTS `borrow_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `asset_id` BIGINT NOT NULL,
    `borrower_id` BIGINT NOT NULL,
    `department_id` BIGINT NOT NULL,
    `approval_id` BIGINT NULL,
    `borrow_date` DATE NOT NULL,
    `expected_return_date` DATE NOT NULL,
    `actual_return_date` DATE NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'BORROWED',
    `reason` VARCHAR(500) NULL,
    `manager_comment` VARCHAR(500) NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `resolved_at` DATETIME NULL,
    INDEX `idx_br_asset_id` (`asset_id`),
    INDEX `idx_br_borrower` (`borrower_id`),
    INDEX `idx_br_status` (`status`),
    INDEX `idx_br_borrow_date` (`borrow_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Supplier table
CREATE TABLE IF NOT EXISTS `supplier` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `contact_person` VARCHAR(100) NULL,
    `phone` VARCHAR(50) NULL,
    `email` VARCHAR(255) NULL,
    `address` VARCHAR(500) NULL,
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NULL,
    `updated_at` DATETIME NULL,
    INDEX `idx_supplier_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insurance Policy table
CREATE TABLE IF NOT EXISTS `insurance_policy` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `policy_number` VARCHAR(50) NOT NULL UNIQUE,
    `asset_id` BIGINT NOT NULL,
    `type` VARCHAR(30) NOT NULL,
    `insurance_company` VARCHAR(200) NULL,
    `premium` DECIMAL(12,2) NULL,
    `coverage_amount` DECIMAL(14,2) NULL,
    `start_date` DATE NOT NULL,
    `end_date` DATE NOT NULL,
    `status` VARCHAR(20) NOT NULL,
    `policy_document` VARCHAR(500) NULL,
    `remarks` TEXT NULL,
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_ip_asset` (`asset_id`),
    INDEX `idx_ip_policy_number` (`policy_number`),
    INDEX `idx_ip_status` (`status`),
    CONSTRAINT `fk_ip_asset` FOREIGN KEY (`asset_id`) REFERENCES `asset` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insurance Claim table
CREATE TABLE IF NOT EXISTS `insurance_claim` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `claim_number` VARCHAR(50) NOT NULL UNIQUE,
    `policy_id` BIGINT NOT NULL,
    `incident_date` DATE NULL,
    `claim_amount` DECIMAL(12,2) NULL,
    `settled_amount` DECIMAL(12,2) NULL,
    `status` VARCHAR(20) NOT NULL,
    `incident_description` VARCHAR(1000) NULL,
    `settlement_notes` VARCHAR(1000) NULL,
    `filed_by` BIGINT NULL,
    `deleted` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    INDEX `idx_ic_policy` (`policy_id`),
    INDEX `idx_ic_claim_number` (`claim_number`),
    INDEX `idx_ic_status` (`status`),
    CONSTRAINT `fk_ic_policy` FOREIGN KEY (`policy_id`) REFERENCES `insurance_policy` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
