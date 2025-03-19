CREATE TABLE `tenant_activity_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tenant_id` BIGINT NOT NULL,
    `owner_id` BIGINT NOT NULL,
    `action_type_id` INT NOT NULL,
    `old_value` VARCHAR(255) NULL,
    `new_value` VARCHAR(255) NULL,
    `detail` JSON NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),    
  
    INDEX `idx_tenant_activity_log_owner` (`owner_id`),
    INDEX `idx_tenant_activity_log_action_type` (`action_type_id`),

    CONSTRAINT `fk_tenant_activity_log_owner`
        FOREIGN KEY (`owner_id`)
        REFERENCES `owners` (`id`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);
