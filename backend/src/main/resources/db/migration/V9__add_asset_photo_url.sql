-- V9__add_asset_photo_url.sql
-- Add photo_url column to asset table (Flyway disabled, manual DDL)

ALTER TABLE asset ADD COLUMN photo_url VARCHAR(500) DEFAULT NULL COMMENT '资产照片URL';
