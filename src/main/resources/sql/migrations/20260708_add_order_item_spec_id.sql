ALTER TABLE `order_items`
    ADD COLUMN `spec_id` BIGINT DEFAULT NULL COMMENT '规格ID' AFTER `product_id`;
