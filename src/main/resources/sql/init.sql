-- 创建数据库
CREATE DATABASE IF NOT EXISTS petshop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE petshop;

-- 商品分类表
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `main_image` VARCHAR(255) DEFAULT NULL COMMENT '主图',
    `images` TEXT DEFAULT NULL COMMENT '商品图片（JSON数组）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '零售价',
    `distributor_price` DECIMAL(10,2) DEFAULT NULL COMMENT '分销商价',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `commission_rate1` DECIMAL(5,2) DEFAULT 8.00 COMMENT '一级佣金比例(%)',
    `commission_rate2` DECIMAL(5,2) DEFAULT 3.00 COMMENT '二级佣金比例(%)',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1上架 0下架',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品规格表
CREATE TABLE IF NOT EXISTS `product_specs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规格ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `spec_name` VARCHAR(50) NOT NULL COMMENT '规格名称（如：10kg）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '规格价格',
    `stock` INT DEFAULT 0 COMMENT '规格库存',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';

-- 用户表
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` TINYINT DEFAULT 0 COMMENT '角色：0普通用户 1分销商 2店主',
    `distributor_level` TINYINT DEFAULT 0 COMMENT '分销等级：0普通 1高级',
    `parent_id` BIGINT DEFAULT NULL COMMENT '上级分销商ID',
    `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '可提现余额',
    `total_commission` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计佣金',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS `addresses` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `province` VARCHAR(50) NOT NULL COMMENT '省',
    `city` VARCHAR(50) NOT NULL COMMENT '市',
    `district` VARCHAR(50) NOT NULL COMMENT '区',
    `detail` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：1是 0否',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `spec_id` BIGINT DEFAULT NULL COMMENT '规格ID',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 订单表
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `address_id` BIGINT NOT NULL COMMENT '收货地址ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态：0待付款 1待发货 2待收货 3已完成 4已取消 5已退款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `deliver_time` DATETIME DEFAULT NULL COMMENT '发货时间',
    `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    `wx_transaction_id` VARCHAR(100) DEFAULT NULL COMMENT '微信支付交易号',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `from_distributor_id` BIGINT DEFAULT NULL COMMENT '推广来源分销商ID',
    `tracking_no` VARCHAR(100) DEFAULT NULL COMMENT '快递单号',
    `tracking_company` VARCHAR(50) DEFAULT NULL COMMENT '快递公司',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单商品明细表
CREATE TABLE IF NOT EXISTS `order_items` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `spec_id` BIGINT DEFAULT NULL COMMENT '规格ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称（快照）',
    `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片（快照）',
    `spec_name` VARCHAR(50) DEFAULT NULL COMMENT '规格名称（快照）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '单价（快照）',
    `quantity` INT NOT NULL COMMENT '数量',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '小计',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';

-- 分销佣金表
CREATE TABLE IF NOT EXISTS `commissions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '佣金ID',
    `user_id` BIGINT NOT NULL COMMENT '分销商用户ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '佣金金额',
    `level` TINYINT NOT NULL COMMENT '佣金层级：1一级 2二级',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0待结算 1已结算',
    `settle_time` DATETIME DEFAULT NULL COMMENT '结算时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销佣金表';

-- 分销商申请表
CREATE TABLE IF NOT EXISTS `distributor_apply` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `reason` VARCHAR(200) DEFAULT NULL COMMENT '申请理由',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0待审核 1已通过 2已拒绝',
    `reject_reason` VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分销商申请表';

-- 提现记录表
CREATE TABLE IF NOT EXISTS `withdraw_records` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提现ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '提现金额',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0待处理 1已打款 2已拒绝',
    `reject_reason` VARCHAR(200) DEFAULT NULL COMMENT '拒绝原因',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提现记录表';

-- 首页轮播图表
CREATE TABLE IF NOT EXISTS `banners` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
    `image` VARCHAR(255) NOT NULL COMMENT '图片地址',
    `link` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图表';

-- 优惠券表
CREATE TABLE IF NOT EXISTS `coupons` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
    `name` VARCHAR(50) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT NOT NULL COMMENT '类型：1满减 2折扣',
    `value` DECIMAL(10,2) NOT NULL COMMENT '优惠值（满减金额或折扣比例）',
    `min_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低使用金额',
    `total` INT DEFAULT 0 COMMENT '发放总量，0不限',
    `used` INT DEFAULT 0 COMMENT '已使用数量',
    `start_time` DATETIME DEFAULT NULL COMMENT '有效期开始',
    `end_time` DATETIME DEFAULT NULL COMMENT '有效期结束',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupons` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0未使用 1已使用 2已过期',
    `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
    `order_id` BIGINT DEFAULT NULL COMMENT '使用的订单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 初始化店主账号（openid先用占位符，实际登录后会更新）
INSERT INTO `users` (`openid`, `nickname`, `role`) VALUES ('admin_placeholder', '店主', 2);

-- 初始化商品分类
INSERT INTO `categories` (`name`, `sort`) VALUES
('猫粮', 1),
('狗粮', 2),
('零食', 3),
('营养品', 4),
('其他', 5);
