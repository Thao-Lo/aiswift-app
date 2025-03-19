CREATE TABLE `tenant_action_type` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `description` VARCHAR(255) NULL,
  PRIMARY KEY (`id`));

INSERT INTO `tenant_action_type` (`name`, `description`) 
VALUES 
('ADD_ADDITIONAL_ADMIN', 'Owner adds an extra admin to their subscription.'),
('REMOVE_ADDITIONAL_ADMIN', 'Owner removes an extra admin from their plan.'),
('ALLOCATED_ADDITIONAL_ADMIN', 'Owner assigns an extra admin to a specific tenant.'),
('REMOVE_ALLOCATED_ADDITIONAL_ADMIN', 'Owner removes an assigned extra admin from a tenant.'),
('ADD_ADDITIONAL_TENANT', 'Owner adds an extra tenant to their subscription.'),
('REMOVE_ADDITIONAL_TENANT', 'Owner deletes a tenant from their plan.'),
('CREATE_NEW_TENANT', 'Owner creates a new tenant (shop).'),
('UPGRADE_PLAN', 'Owner upgrades their subscription plan.'),
('DOWNGRADE_PLAN', 'Owner downgrades their subscription plan.');
