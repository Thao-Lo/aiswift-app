
CREATE TABLE `global_multi_tenant`.`payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `owner_id` BIGINT NOT NULL,
  `subscription_plan_detail_id` INT NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `status` ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'PENDING',
  `payment_intent_id` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_payment_owner` (`owner_id` ASC) VISIBLE,
  INDEX `idx_payment_subscription_plan_detail` (`subscription_plan_detail_id` ASC) VISIBLE,
  CONSTRAINT `fk_payment_owner`
    FOREIGN KEY (`owner_id`)
    REFERENCES `owners` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_payment_subscription_plan_detail`
    FOREIGN KEY (`subscription_plan_detail_id`)
    REFERENCES `subscription_plan_detail` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE);
