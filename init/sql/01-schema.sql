-- =====================================================================
-- 游小景 · 景区分时预约与电子导览系统
-- 01-schema.sql  十四张表（11 张业务表 + 3 张字典表）
-- 约定：
--   1. 表名/字段名 snake_case，MyBatis-Plus 开启下划线转驼峰
--   2. 主键统一 bigint（业务键如订单号另外建唯一索引）
--   3. 金额单位：分（int），不用 decimal，避免浮点误差
--   4. 库存扣减以 Redis 为准，DB 只做记录与对账
--   5. status 用 tinyint + 代码枚举，不用 enum 类型
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS scenic_go
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE scenic_go;

-- ---------------------------------------------------------------------
-- 1. user  用户
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(32)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(128) NOT NULL DEFAULT ''     COMMENT '密码（BCrypt）',
    `nick_name`   VARCHAR(32)  NOT NULL DEFAULT ''     COMMENT '昵称',
    `icon`        VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '头像',
    `phone`       VARCHAR(20)  NOT NULL DEFAULT ''     COMMENT '手机号',
    `email`       VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '邮箱',
    `role`        TINYINT      NOT NULL DEFAULT 0      COMMENT '0游客 1会员 2核销员 3管理员',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '1正常 0封禁',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户';

-- ---------------------------------------------------------------------
-- 2. spot  景点
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `spot`;
CREATE TABLE `spot` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`            VARCHAR(64)  NOT NULL                COMMENT '景点名称',
    `summary`         VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '一句话简介（列表页展示）',
    `content`         TEXT                                 COMMENT '详细介绍（详情页，热点缓存对象）',
    `cover_img`       VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '封面图',
    `longitude`       DECIMAL(10, 6) NOT NULL DEFAULT 0    COMMENT '经度（GEO 用）',
    `latitude`        DECIMAL(10, 6) NOT NULL DEFAULT 0    COMMENT '纬度（GEO 用）',
    `open_time`       VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '开放时间，如 08:00-17:00',
    `avg_price`       INT          NOT NULL DEFAULT 0      COMMENT '人均价格（分）',
    `score`           DECIMAL(2, 1) NOT NULL DEFAULT 5.0   COMMENT '评分 0.0-5.0',
    `sold_count`      INT          NOT NULL DEFAULT 0      COMMENT '累计预约数（冗余，用于排序）',
    `recommend_order` INT          NOT NULL DEFAULT 0      COMMENT '推荐路线顺序，0 表示不进路线',
    `walk_minutes`    INT          NOT NULL DEFAULT 0      COMMENT '建议游览时长（分钟）',
    `type`          TINYINT      NOT NULL DEFAULT 1      COMMENT '1景点 0展馆',
    `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '1上架 0下架',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_route` (`recommend_order`),
    KEY `idx_status_score` (`status`, `score`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '景点/展馆';

-- ---------------------------------------------------------------------
-- 3. exhibition  展品
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `exhibition`;
CREATE TABLE `exhibition` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spot_id`       BIGINT       NOT NULL                COMMENT '所属景点',
    `name`          VARCHAR(64)  NOT NULL                COMMENT '展品名称',
    `category`      VARCHAR(32)  NOT NULL DEFAULT ''     COMMENT '类别，如 陶瓷/书画/青铜',
    `summary`       VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '简介',
    `narrative`     TEXT                                 COMMENT '讲解词（展品详情 / 语音导览用）',
    `cover_img`     VARCHAR(255) NOT NULL DEFAULT ''     COMMENT '图片',
    `sort`          INT          NOT NULL DEFAULT 0      COMMENT '馆内排序',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '1上架 0下架',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_spot_sort` (`spot_id`, `sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '展品';

-- ---------------------------------------------------------------------
-- 4. tag  标签
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(32) NOT NULL                COMMENT '标签名，如 亲子/拍照/室内',
    `type`        TINYINT     NOT NULL DEFAULT 1      COMMENT '1场景 2人群 3特色',
    `sort`        INT         NOT NULL DEFAULT 0      COMMENT '排序',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '标签';

-- ---------------------------------------------------------------------
-- 5. spot_tag  景点标签关联（多值标签，组合筛选用）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `spot_tag`;
CREATE TABLE `spot_tag` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spot_id` BIGINT NOT NULL                COMMENT '景点 ID',
    `tag_id`  BIGINT NOT NULL                COMMENT '标签 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spot_tag` (`spot_id`, `tag_id`),
    KEY `idx_tag` (`tag_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '景点标签关联';

-- ---------------------------------------------------------------------
-- 6. facility  设施（洗手间/餐厅/入口/停车等，GEO + 围栏对象）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `facility`;
CREATE TABLE `facility` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(64)  NOT NULL                COMMENT '设施名称',
    `type`        TINYINT      NOT NULL DEFAULT 0      COMMENT '1洗手间 2餐饮 3入口 4停车 5医疗 6商店',
    `longitude`   DECIMAL(10, 6) NOT NULL DEFAULT 0    COMMENT '经度',
    `latitude`    DECIMAL(10, 6) NOT NULL DEFAULT 0    COMMENT '纬度',
    `spot_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '所属景点，0 表示公共设施',
    `fence_radius` INT         NOT NULL DEFAULT 50     COMMENT '围栏半径（米）',
    `open_time`   VARCHAR(64)  NOT NULL DEFAULT ''     COMMENT '开放时间',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '1可用 0停用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_status` (`type`, `status`),
    KEY `idx_spot` (`spot_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '园区设施';

-- ---------------------------------------------------------------------
-- 7. booking_slot  时段库存  ★核心表
--    维度：景点 × 日期 × 时段 × 票种；日库存 = 该组合下所有时段的合计
--    Redis 预扣，DB 记录；唯一键保证不重复放票
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `booking_slot`;
CREATE TABLE `booking_slot` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键（slotId）',
    `spot_id`     BIGINT      NOT NULL                COMMENT '景点 ID',
    `slot_date`   DATE        NOT NULL                COMMENT '日期',
    `slot_start`  VARCHAR(5)  NOT NULL                COMMENT '时段开始，如 09:00',
    `slot_end`    VARCHAR(5)  NOT NULL                COMMENT '时段结束，如 11:00',
    `ticket_type` TINYINT     NOT NULL DEFAULT 1      COMMENT '1成人 2儿童 3学生',
    `price`       INT         NOT NULL DEFAULT 0      COMMENT '票价（分）',
    `total_stock` INT         NOT NULL DEFAULT 0      COMMENT '总库存',
    `sold_stock`  INT         NOT NULL DEFAULT 0      COMMENT '已售（对账基准）',
    `status`      TINYINT     NOT NULL DEFAULT 1      COMMENT '1可售 0停售',
    `version`     INT         NOT NULL DEFAULT 0      COMMENT '乐观锁',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slot` (`spot_id`, `slot_date`, `slot_start`, `ticket_type`),
    KEY `idx_date_status` (`slot_date`, `status`),
    KEY `idx_spot_date` (`spot_id`, `slot_date`),
    -- 兜底：Redis 预扣逻辑出问题时宁可报错，也不要静默记出一笔超卖数据
    CONSTRAINT `ck_booking_slot_stock` CHECK (`sold_stock` <= `total_stock`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '时段库存';

-- ---------------------------------------------------------------------
-- 8. reserve  预约单  ★核心表（状态机）
--    status: 0待支付 1已预约 2已核销 3已取消 4已过期
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `reserve`;
CREATE TABLE `reserve` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`      VARCHAR(32) NOT NULL                COMMENT '订单号（雪花 ID，对外展示）',
    `user_id`       BIGINT      NOT NULL                COMMENT '用户 ID',
    `spot_id`       BIGINT      NOT NULL                COMMENT '景点 ID',
    `slot_id`       BIGINT      NOT NULL                COMMENT '时段库存 ID',
    `slot_date`     DATE        NOT NULL                COMMENT '预约日期（冗余，便于按日查询/统计）',
    `slot_start`    VARCHAR(5)  NOT NULL DEFAULT ''     COMMENT '时段开始（冗余）',
    `slot_end`      VARCHAR(5)  NOT NULL DEFAULT ''     COMMENT '时段结束（冗余）',
    `ticket_type`   TINYINT     NOT NULL DEFAULT 1      COMMENT '1成人 2儿童 3学生',
    `quantity`      INT         NOT NULL DEFAULT 1      COMMENT '数量',
    `unit_price`    INT         NOT NULL DEFAULT 0      COMMENT '单价（分）',
    `total_amount`  INT         NOT NULL DEFAULT 0      COMMENT '总金额（分）',
    `status`        TINYINT     NOT NULL DEFAULT 0      COMMENT '0待支付 1已预约 2已核销 3已取消 4已过期',
    `checkin_code`  VARCHAR(64) DEFAULT NULL            COMMENT '核销码（一次性，支付成功后写入；NULL 可重复、空串不可，故用 NULL）',
    `pay_time`      DATETIME    DEFAULT NULL            COMMENT '支付时间',
    `checkin_time`  DATETIME    DEFAULT NULL            COMMENT '核销时间',
    `cancel_time`   DATETIME    DEFAULT NULL            COMMENT '取消时间',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    -- 核销码天然一次性：DB 层就挡住第二次使用，与 checkin_log.uk_reserve 形成双保险
    UNIQUE KEY `uk_checkin_code` (`checkin_code`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_slot` (`slot_id`),
    KEY `idx_spot_date` (`spot_id`, `slot_date`),
    KEY `idx_create_time` (`create_time`),
    -- 黄牛识别：同一用户短时间内的大量下单
    KEY `idx_user_create` (`user_id`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '预约单';

-- ---------------------------------------------------------------------
-- 9. checkin_log  核销流水  ★唯一键防重放
--    reserve_id 唯一 → 一张票只能核销一次，DB 层面兜底
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `checkin_log`;
CREATE TABLE `checkin_log` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `reserve_id`  BIGINT      NOT NULL                COMMENT '预约单 ID',
    `order_no`    VARCHAR(32) NOT NULL DEFAULT ''     COMMENT '订单号（冗余）',
    `user_id`     BIGINT      NOT NULL                COMMENT '用户 ID',
    `spot_id`     BIGINT      NOT NULL                COMMENT '景点 ID',
    `operator_id` BIGINT      NOT NULL DEFAULT 0      COMMENT '核销员 ID，0 表示闸机自动',
    `channel`     TINYINT     NOT NULL DEFAULT 1      COMMENT '1闸机 2人工 3小程序自助',
    `checkin_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '核销时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reserve` (`reserve_id`),
    KEY `idx_spot_time` (`spot_id`, `checkin_time`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '核销流水';

-- ---------------------------------------------------------------------
-- 10. review  评价
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spot_id`     BIGINT      NOT NULL                COMMENT '景点 ID',
    `user_id`     BIGINT      NOT NULL                COMMENT '用户 ID',
    `reserve_id`  BIGINT      NOT NULL DEFAULT 0      COMMENT '关联预约单，0 表示未预约也能评',
    `content`     TEXT                                COMMENT '评价内容（RAG 语料）',
    `score`       TINYINT     NOT NULL DEFAULT 5      COMMENT '评分 1-5',
    `liked`       INT         NOT NULL DEFAULT 0      COMMENT '点赞数（Redis Set 去重后回写）',
    `status`      TINYINT     NOT NULL DEFAULT 1      COMMENT '1显示 0隐藏',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_spot_time` (`spot_id`, `create_time`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评价';

-- ---------------------------------------------------------------------
-- 11. review_summary  评价摘要（预留表：第一版不生成、不读取，只留结构）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `review_summary`;
CREATE TABLE `review_summary` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spot_id`        BIGINT      NOT NULL                COMMENT '景点 ID',
    `summary`        VARCHAR(512) NOT NULL DEFAULT ''    COMMENT '摘要（一句话）',
    `tags_json`      VARCHAR(512) NOT NULL DEFAULT ''    COMMENT '亮点标签 JSON 数组',
    `issues_json`    VARCHAR(512) NOT NULL DEFAULT ''    COMMENT '问题标签 JSON 数组（差评聚类）',
    `avg_score`      DECIMAL(2, 1) NOT NULL DEFAULT 5.0  COMMENT '平均分',
    `total_count`    INT         NOT NULL DEFAULT 0      COMMENT '参与统计的评价数',
    `negative_ratio` DECIMAL(4, 3) NOT NULL DEFAULT 0    COMMENT '差评占比',
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spot` (`spot_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '评价摘要';

-- ---------------------------------------------------------------------
-- 12. sys_ticket_type  票种字典（价格 / 每时段限购张数）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_ticket_type`;
CREATE TABLE `sys_ticket_type` (
    `code`           TINYINT     NOT NULL           COMMENT '票种编码 1成人 2儿童 3学生',
    `name`           VARCHAR(16) NOT NULL           COMMENT '票种名称',
    `price`          INT         NOT NULL           COMMENT '价格（分）',
    `limit_per_slot` INT         NOT NULL DEFAULT 2 COMMENT '每时段限购张数',
    PRIMARY KEY (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '票种字典';

-- ---------------------------------------------------------------------
-- 13. sys_slot_template  时段模板（04-slot.sql 生成 booking_slot 的依据）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_slot_template`;
CREATE TABLE `sys_slot_template` (
    `id`          INT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `slot_start`  VARCHAR(5) NOT NULL                COMMENT '开始',
    `slot_end`    VARCHAR(5) NOT NULL                COMMENT '结束',
    `total_stock` INT        NOT NULL                COMMENT '默认总库存',
    `sort`        INT        NOT NULL DEFAULT 0      COMMENT '顺序',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '时段模板';

-- ---------------------------------------------------------------------
-- 14. announcement  公告（首页演示接口用）
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(128) NOT NULL                COMMENT '标题',
    `content`     TEXT                                 COMMENT '内容',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '1发布 0下架',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '公告';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 建库后建议单独执行（应用连接账号，避免用 root 跑业务）：
--   CREATE USER IF NOT EXISTS 'scenic'@'%' IDENTIFIED BY 'scenic123';
--   GRANT ALL PRIVILEGES ON scenic_go.* TO 'scenic'@'%';
--   FLUSH PRIVILEGES;
-- 然后 application.yml 里把 username/password 换成 scenic/scenic123。
-- =====================================================================
