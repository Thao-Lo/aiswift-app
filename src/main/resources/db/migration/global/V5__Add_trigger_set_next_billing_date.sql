DROP TRIGGER IF EXISTS `subscription_plan_detail_BEFORE_INSERT`;

DELIMITER $$
CREATE DEFINER = CURRENT_USER TRIGGER `subscription_plan_detail_BEFORE_INSERT` 
BEFORE INSERT ON `subscription_plan_detail`
FOR EACH ROW
BEGIN
	IF NEW.next_billing_date IS NULL THEN
		SET NEW.next_billing_date = NEW.subscription_start + INTERVAL 30 DAY;
	END IF;
END$$
DELIMITER ;

