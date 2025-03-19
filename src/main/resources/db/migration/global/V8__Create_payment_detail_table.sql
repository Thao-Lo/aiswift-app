CREATE TABLE `payment_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `payment_id` BIGINT NOT NULL,
  `payment_type_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  `unit_price` DECIMAL(10,2) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_payment_detail_payment` (`payment_id` ASC),
  INDEX `idx_payment_detail_payment_type` (`payment_type_id` ASC),
  CONSTRAINT `fk_payment_detail_payment`
    FOREIGN KEY (`payment_id`)
    REFERENCES `payment` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
