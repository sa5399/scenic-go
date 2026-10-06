-- =====================================================================
-- 06-reserve.sql  预约单与核销流水造数
-- 依赖：01-schema.sql ~ 05-ugc.sql（需要 booking_slot 有库存、user 有用户）
-- 幂等：会清空 reserve / checkin_log 两张表
-- 说明：12 张单覆盖 5 种状态（0待支付 1已预约 2已核销 3已取消 4已过期），
--       下单量全部遵守 sys_ticket_type.limit_per_slot；核销码由订单号派生，
--       天然唯一（配合 reserve.uk_checkin_code）；最后按订单回填 sold_stock，
--       保证 DB 层的库存与订单一致
--       口径：待支付也占库存（下单即占用，取消/超时回补），所以对账含 status = 0
-- =====================================================================

SET NAMES utf8mb4;
USE scenic_go;

DELETE FROM checkin_log;
DELETE FROM reserve;

-- ---------------------------------------------------------------------
-- 预约单：从 booking_slot 里按 (景点, 日期, 时段, 票种) 精确取一行
--   status 0 待支付：不生成核销码
--   status 1/2    ：生成核销码，已支付
--   status 4 已过期：订的是今天早班、3 天前下的单，没来核销
-- ---------------------------------------------------------------------
INSERT INTO reserve (order_no, user_id, spot_id, slot_id, slot_date, slot_start, slot_end,
                     ticket_type, quantity, unit_price, total_amount, status, checkin_code,
                     pay_time, checkin_time, cancel_time, create_time)
SELECT CONCAT('SG', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(v.seq, 4, '0')),
       v.user_id,
       bs.spot_id,
       bs.id,
       bs.slot_date,
       bs.slot_start,
       bs.slot_end,
       bs.ticket_type,
       v.quantity,
       bs.price,
       bs.price * v.quantity,
       v.status,
       IF(v.status IN (1, 2), CONCAT('CK', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(v.seq, 4, '0')), NULL),
       IF(v.status IN (1, 2), DATE_ADD(DATE_SUB(NOW(), INTERVAL v.create_ago_min MINUTE), INTERVAL 3 MINUTE), NULL),
       IF(v.status = 2,      DATE_ADD(DATE_SUB(NOW(), INTERVAL v.create_ago_min MINUTE), INTERVAL 20 MINUTE), NULL),
       IF(v.status = 3,      DATE_ADD(DATE_SUB(NOW(), INTERVAL v.create_ago_min MINUTE), INTERVAL 10 MINUTE), NULL),
       DATE_SUB(NOW(), INTERVAL v.create_ago_min MINUTE)
FROM (SELECT 1 AS seq, 4 AS user_id, 1 AS spot_id, 1 AS day_offset, '08:00' AS slot_start, 1 AS ticket_type, 2 AS quantity, 0 AS status, 5 AS create_ago_min
      UNION ALL SELECT 2,  5,  9, 2, '13:00', 1, 1, 0, 12
      UNION ALL SELECT 3,  6,  2, 1, '10:00', 1, 2, 1, 2880
      UNION ALL SELECT 4,  7,  4, 1, '15:00', 1, 4, 1, 1440
      UNION ALL SELECT 5,  8,  5, 2, '10:00', 2, 2, 1, 1440
      UNION ALL SELECT 6,  9, 12, 1, '13:00', 3, 2, 1, 180
      UNION ALL SELECT 7,  5,  1, 0, '08:00', 1, 2, 2, 1440
      UNION ALL SELECT 8,  6,  9, 0, '10:00', 1, 1, 2, 1560
      UNION ALL SELECT 9,  7,  8, 0, '08:00', 2, 2, 2, 2880
      UNION ALL SELECT 10, 8,  2, 1, '13:00', 1, 2, 3, 2880
      UNION ALL SELECT 11, 9,  6, 2, '15:00', 3, 1, 3, 240
      UNION ALL SELECT 12, 10, 3, 0, '08:00', 1, 2, 4, 4320) v
         JOIN booking_slot bs
              ON bs.spot_id = v.spot_id
                  AND bs.slot_date = CURDATE() + INTERVAL v.day_offset DAY
                  AND bs.slot_start = v.slot_start
                  AND bs.ticket_type = v.ticket_type;

-- ---------------------------------------------------------------------
-- 核销流水：已核销的单各来一条（reserve_id 唯一键 = DB 层防重复核销）
-- ---------------------------------------------------------------------
INSERT INTO checkin_log (reserve_id, order_no, user_id, spot_id, operator_id, channel, checkin_time)
SELECT r.id, r.order_no, r.user_id, r.spot_id, 2, 2, r.checkin_time
FROM reserve r
WHERE r.status = 2;

-- ---------------------------------------------------------------------
-- 库存对账：sold_stock 回填为「占用中订单（待支付 + 已预约 + 已核销）的票数合计」
--   待支付那 15 分钟也占着票，Redis 与 DB 才在任何时刻都相等
--   先把全表清零：04 只清「今天及以后」的库存行，历史行会留着上一次的 sold_stock，
--   而它们的订单已经被本脚本删掉了，不清就会算出「已售合计 ≠ 占用中票数」
-- ---------------------------------------------------------------------
UPDATE booking_slot
SET sold_stock = 0;

UPDATE booking_slot bs
    JOIN (SELECT slot_id, SUM(quantity) AS q
          FROM reserve
          WHERE status IN (0, 1, 2)
          GROUP BY slot_id) r ON r.slot_id = bs.id
SET bs.sold_stock = r.q;

-- 自检：状态分布 / 核销流水 / 库存对账（两个数字必须相等）
SELECT '预约单状态分布' AS 检查项, CAST(status AS CHAR) AS 维度, COUNT(*) AS 结果
FROM reserve GROUP BY status
UNION ALL
SELECT '核销流水条数', '', COUNT(*) FROM checkin_log
UNION ALL
SELECT '库存已售合计(booking_slot)', '', SUM(sold_stock) FROM booking_slot
UNION ALL
SELECT '占用中票数(reserve)', '', IFNULL(SUM(quantity), 0) FROM reserve WHERE status IN (0, 1, 2);
