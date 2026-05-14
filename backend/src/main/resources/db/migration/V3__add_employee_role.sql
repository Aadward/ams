-- Add role column to employee table for RBAC
ALTER TABLE employee ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
