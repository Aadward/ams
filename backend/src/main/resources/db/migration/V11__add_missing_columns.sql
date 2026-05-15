-- V11__add_missing_columns.sql
-- Add columns that exist in JPA entities but are missing from Flyway migrations
-- Note: For fresh database (which this is), these columns don't exist yet and will be added.
-- For existing databases, these columns should already exist via prior manual fixes.

-- Add missing columns to inventory_plan (remark field in InventoryPlan entity)
-- Using subquery to avoid duplicate column error on re-run
-- (In fresh DB this is a simple add; in existing DB the column already exists)
SET @dbname = DATABASE();
SET @tablename = 'inventory_plan';
SET @columnname = 'remark';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) = 0,
    'ALTER TABLE `inventory_plan` ADD COLUMN `remark` TEXT NULL COMMENT ''备注''',
    'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- inventory_task remark
SET @columnname = 'remark';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'inventory_task' AND COLUMN_NAME = @columnname) = 0,
    'ALTER TABLE `inventory_task` ADD COLUMN `remark` TEXT NULL COMMENT ''备注''',
    'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- inventory_record remark
SET @columnname = 'remark';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'inventory_record' AND COLUMN_NAME = @columnname) = 0,
    'ALTER TABLE `inventory_record` ADD COLUMN `remark` TEXT NULL COMMENT ''备注（盘盈/盘亏说明）''',
    'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- maintenance_record requestor_id (who submitted the repair request)
SET @columnname = 'requestor_id';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'maintenance_record' AND COLUMN_NAME = @columnname) = 0,
    'ALTER TABLE `maintenance_record` ADD COLUMN `requestor_id` BIGINT NULL COMMENT ''申请人ID''',
    'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- maintenance_record status (may already exist from V5)
SET @columnname = 'status';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'maintenance_record' AND COLUMN_NAME = @columnname) = 0,
    'ALTER TABLE `maintenance_record` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT ''PENDING'' COMMENT ''维修状态：PENDING/APPROVED/COMPLETED/REJECTED''',
    'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- asset depreciation_years
SET @columnname = 'depreciation_years';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'asset' AND COLUMN_NAME = @columnname) = 0,
    'ALTER TABLE `asset` ADD COLUMN `depreciation_years` INT NULL COMMENT ''折旧年限（年）''',
    'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- asset photo_url
SET @columnname = 'photo_url';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'asset' AND COLUMN_NAME = @columnname) = 0,
    'ALTER TABLE `asset` ADD COLUMN `photo_url` VARCHAR(500) NULL COMMENT ''资产照片URL''',
    'SELECT 1'
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
