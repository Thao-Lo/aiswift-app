CREATE TABLE `payment_type` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`id`));
  
  INSERT INTO `payment_type` (`name`, `description`) 
VALUES 
('PLAN_PAYMENT', 'Monthly payment for the subscription plan.'),
('ADDITIONAL_ADMIN', 'Payment for additional admins beyond the free limit.'),
('ADDITIONAL_TENANT', 'Payment for additional tenants beyond the plan limit.'),
('DISCOUNT', 'Applied discount to the total payment.'),
('REFUND', 'Refund issued to the owner.'),
('CANCELLATION_FEE', 'Fee charged when canceling a subscription before the end of the billing cycle.');
