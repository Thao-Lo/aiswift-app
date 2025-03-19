-- table: roles
CREATE TABLE `roles` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`));

-- table: permissions
CREATE TABLE `permissions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`));

-- table: role_permission
CREATE TABLE `role_permission` (
  `role_id` INT NOT NULL,
  `permission_id` INT NOT NULL,
  INDEX `fk_role_permission_role_idx` (`role_id` ASC) VISIBLE,
  INDEX `fk_role_permission_permission_idx` (`permission_id` ASC) VISIBLE,
  CONSTRAINT `fk_role_permission_role`
    FOREIGN KEY (`role_id`)
    REFERENCES `roles` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_role_permission_permission`
    FOREIGN KEY (`permission_id`)
    REFERENCES `permissions` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
  
-- table: users
CREATE TABLE `users` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(45) NOT NULL,
  `last_name` VARCHAR(45) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `password` VARCHAR(255) NULL DEFAULT NULL,
  `status` ENUM('ACTIVE', 'DISABLE') NOT NULL DEFAULT 'ACTIVE',
  `role_id` INT NOT NULL,
  `created_by` VARCHAR(100) NOT NULL,
  `updated_by` VARCHAR(100) NULL DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `reset_token` VARCHAR(100) NULL DEFAULT NULL,
  `reset_token_expiry` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
   CONSTRAINT `fk_user_role`
  	FOREIGN KEY (`role_id`)
  	REFERENCES `roles` (`id`)
  	ON DELETE RESTRICT 
  	ON UPDATE CASCADE
  	);
  	