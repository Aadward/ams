-- V5__add_approval_id_to_maintenance_record.sql
-- Add status and approval_id columns to maintenance_record for linking repair requests to approval workflow

ALTER TABLE `maintenance_record`
    ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING'
    COMMENT '维修状态：PENDING/APPROVED/COMPLETED/REJECTED'
    AFTER `vendor`;

ALTER TABLE `maintenance_record`
    ADD COLUMN `approval_id` BIGINT NULL
    COMMENT '关联的审批请求 ID FK→approval_request.id'
    AFTER `status`;

ALTER TABLE `maintenance_record`
    ADD CONSTRAINT `fk_maintenance_approval`
    FOREIGN KEY (`approval_id`)
    REFERENCES `approval_request`(`id`)
    ON DELETE SET NULL
    ON UPDATE CASCADE;
