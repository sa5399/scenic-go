-- =====================================================================
-- 04-slot.sql  时段库存造数
--   维度：景点 × 日期 × 时段 × 票种（未来 7 天，含今天）
--   行数：12 个景点 × 7 天 × 4 时段 × 3 票种 = 1008 行
-- 依赖：01-schema.sql、02-dict.sql（sys_slot_template / sys_ticket_type）、03-scenic.sql
-- 幂等：只清理「今天及以后」的库存行，历史行（被预约单引用）不动
-- 说明：sold_stock 一律从 0 开始，真实已售由 06-reserve.sql 按订单对账回填，
--       不造「假已售」——对账一致性是这个项目本身的卖点
-- =====================================================================

SET NAMES utf8mb4;
USE scenic_go;

DELETE FROM booking_slot WHERE slot_date >= CURDATE();

INSERT INTO booking_slot (spot_id, slot_date, slot_start, slot_end,
                          ticket_type, price, total_stock, sold_stock, status)
SELECT s.id,
       CURDATE() + INTERVAL n.n DAY,
       t.slot_start,
       t.slot_end,
       tt.code,
       tt.price,
       t.total_stock,
       0,
       1
FROM spot s
         CROSS JOIN (SELECT 0 AS n
                     UNION ALL SELECT 1
                     UNION ALL SELECT 2
                     UNION ALL SELECT 3
                     UNION ALL SELECT 4
                     UNION ALL SELECT 5
                     UNION ALL SELECT 6) n
         CROSS JOIN sys_slot_template t
         CROSS JOIN sys_ticket_type tt
WHERE s.status = 1
ORDER BY s.id, n.n, t.sort, tt.code;

-- 自检：行数应为 1008，且每个景点每天每个时段都有三种票
SELECT '时段库存行数' AS 检查项, COUNT(*) AS 结果 FROM booking_slot
UNION ALL
SELECT '有库存的景点数', COUNT(DISTINCT spot_id) FROM booking_slot
UNION ALL
SELECT '覆盖天数', COUNT(DISTINCT slot_date) FROM booking_slot;
