-- 1. Categories table (self-referencing FK)
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level` int NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_category_parent_idx` (`parent_id`),
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
);

-- 2. Products table (depends on categories)
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `Updated_by` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `description` text,
  `image_data` longblob,
  `image_name` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `Updated_at` datetime(6) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_product_category_idx` (`category_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
);

-- 3. Sizes table (depends on products)
CREATE TABLE `sizes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `size_price` decimal(38,2) DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_size_product_idx` (`product_id`),
  CONSTRAINT `fk_size_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
);

-- 4. Orders table (no dependencies)
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_status_history` text,
  `created_at` datetime(6) DEFAULT NULL,
  `customerInfo` text,
  `date` datetime(6) DEFAULT NULL,
  `paypalId` varchar(255) DEFAULT NULL,
  `refund_processed_at` datetime(6) DEFAULT NULL,
  `refund_requested_at` datetime(6) DEFAULT NULL,
  `refundStatus` enum('APPROVED','AUTO_APPROVED','NOT_REQUESTED','PENDING_APPROVAL','REJECTED') DEFAULT NULL,
  `status` enum('CREATED','DELIVERED','PAID','PENDING','PROCESSING','REFUNDED','SHIPPED','VOIDED') DEFAULT NULL,
  `tenantId` bigint DEFAULT NULL,
  `total_price` decimal(10,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `user_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

-- 5. Order details table (depends on orders, products, sizes)
CREATE TABLE `order_details` (
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `quantity` int NOT NULL,
  `subtotal` decimal(38,2) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `size_id` bigint DEFAULT NULL,
  PRIMARY KEY (`order_id`,`product_id`),
  KEY `fk_orderdetail_product_idx` (`product_id`),
  KEY `fk_orderdetail_size_idx` (`size_id`),
  CONSTRAINT `fk_orderdetail_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_orderdetail_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_orderdetail_size` FOREIGN KEY (`size_id`) REFERENCES `sizes` (`id`)
);

-- 6. Order status history table (depends on orders)
CREATE TABLE `order_status_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `status` enum('CREATED','DELIVERED','PAID','PENDING','PROCESSING','REFUNDED','SHIPPED','VOIDED') NOT NULL,
  `updatedAt` datetime(6) NOT NULL,
  `updated_by_id` bigint DEFAULT NULL,
  `updated_by_type` varchar(255) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_statushistory_order_idx` (`order_id`),
  CONSTRAINT `fk_statushistory_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
);

-- 7. Paypal configs table (no dependencies)
CREATE TABLE `paypal_configs` (
  `id` varchar(255) NOT NULL,
  `client_id` varchar(255) DEFAULT NULL,
  `client_secret` varchar(255) DEFAULT NULL,
  `mode` varchar(255) DEFAULT NULL,
  `paypal_email` varchar(255) DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  `webhook_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);