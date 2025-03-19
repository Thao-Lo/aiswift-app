CREATE TABLE `global_multi_tenant`.`subscription_plan_detail` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `owner_id` BIGINT NOT NULL,
  `subscription_plan_id` INT NOT NULL,
  `active_tenant_count` INT NULL DEFAULT 0,
  `additional_tenant_count` INT NULL DEFAULT 0,
  `max_tenant` INT NOT NULL,
  `allocated_additional_admin` INT NULL DEFAULT 0,
  `additional_admin_count` INT NULL DEFAULT 0,
  `subscription_start` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `next_billing_date` TIMESTAMP NULL,
  `status` ENUM('ACTIVE', 'INACTIVE', 'CANCELLED', 'EXPIRED') NULL DEFAULT 'INACTIVE',
  PRIMARY KEY (`id`),
  INDEX `idx_fk_plan_detail_owner` (`owner_id` ASC) VISIBLE,
  INDEX `idx_fk_plan_detail_subscription_plan` (`subscription_plan_id` ASC) VISIBLE,
  CONSTRAINT `fk_plan_detail_owner`
    FOREIGN KEY (`owner_id`)
    REFERENCES `owners` (`id`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_plan_detail_subscription_plan`
    FOREIGN KEY (`subscription_plan_id`)
    REFERENCES `subscription_plan` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE);
