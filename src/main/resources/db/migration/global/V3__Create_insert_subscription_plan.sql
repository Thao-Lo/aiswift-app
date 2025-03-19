CREATE TABLE `subscription_plan` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(45) NOT NULL UNIQUE,
    `tenant_limit` INT NOT NULL,
    `admin_limit_per_tenant` INT NOT NULL,
    `base_cost` DECIMAL(10,2) NOT NULL,
    `additional_tenant_fee` DECIMAL(10,2) NOT NULL,
    `additional_admin_fee` DECIMAL(10,2) NOT NULL,
    `billing_cycle` ENUM('monthly', 'yearly') NOT NULL,
    PRIMARY KEY (`id`)
);

INSERT INTO `subscription_plan` 
(`name`, `tenant_limit`, `admin_limit_per_tenant`, `base_cost`, `additional_tenant_fee`, `additional_admin_fee`, `billing_cycle`) 
VALUES 
('Basic', 1, 1, 39.00, 29.00, 10.00, 'monthly'),
('Business', 3, 1, 89.00, 29.00, 10.00, 'monthly'),
('Enterprise', 10, 1, 279.00, 29.00, 10.00, 'monthly');
