-- 易耗品基础表
CREATE TABLE consumable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(30) NOT NULL,
    spec VARCHAR(200),
    unit VARCHAR(20) NOT NULL,
    threshold INT NOT NULL DEFAULT 10,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME
);

-- 易耗品库存台账表
CREATE TABLE consumable_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumable_id BIGINT NOT NULL UNIQUE,
    current_stock INT NOT NULL DEFAULT 0,
    updated_at DATETIME,
    FOREIGN KEY (consumable_id) REFERENCES consumable(id)
);

-- 出入库记录表
CREATE TABLE consumable_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumable_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    related_user_id BIGINT,
    supplier VARCHAR(200),
    remark VARCHAR(500),
    created_at DATETIME,
    FOREIGN KEY (consumable_id) REFERENCES consumable(id)
);
