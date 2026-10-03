-- =====================================================================
-- 05-ugc.sql  用户与评价造数
-- 依赖：01-schema.sql、03-scenic.sql（评价引用 spot.id）
-- 幂等：会清空 user / review 两张表
-- 密码：所有用户统一 123456，下面是真实 BCrypt 哈希（强度 10，已校验通过），
--       由本地仓库 spring-security-crypto-6.3.10 的 BCrypt.hashpw 生成，
--       认证模块写好后用 BCryptPasswordEncoder 可直接校验
-- =====================================================================

SET NAMES utf8mb4;
USE scenic_go;

DELETE FROM review;
DELETE FROM `user`;

-- ---------------------------------------------------------------------
-- 用户：role 0游客 1会员 2核销员 3管理员
-- ---------------------------------------------------------------------
INSERT INTO `user` (id, username, password, nick_name, icon, phone, email, role, status) VALUES
(1,  'admin',     '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '系统管理员', '/img/avatar/01.png', '13800000001', 'admin@scenicgo.local',     3, 1),
(2,  'checkin01', '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '东门核销员', '/img/avatar/02.png', '13800000002', 'checkin01@scenicgo.local', 2, 1),
(3,  'checkin02', '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '北门核销员', '/img/avatar/03.png', '13800000003', 'checkin02@scenicgo.local', 2, 1),
(4,  'linxiaoyu', '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '林小雨',     '/img/avatar/04.png', '13800000004', 'linxiaoyu@example.com',    1, 1),
(5,  'chenmo',    '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '陈默',       '/img/avatar/05.png', '13800000005', 'chenmo@example.com',       1, 1),
(6,  'wangzhe',   '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '王哲',       '/img/avatar/06.png', '13800000006', 'wangzhe@example.com',      0, 1),
(7,  'zhaolei',   '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '赵磊',       '/img/avatar/07.png', '13800000007', 'zhaolei@example.com',      0, 1),
(8,  'sunqi',     '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '孙琪',       '/img/avatar/08.png', '13800000008', 'sunqi@example.com',        0, 1),
(9,  'zhoujing',  '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '周静',       '/img/avatar/09.png', '13800000009', 'zhoujing@example.com',     1, 1),
(10, 'guest01',   '$2a$10$.EkLO/ph0O2mo0MoRscJ/eHg/kxLDCiaq/ek/nrL5V/XYlq92ZK2O', '游客一号',   '/img/avatar/10.png', '13800000010', 'guest01@example.com',      0, 1);

-- ---------------------------------------------------------------------
-- 评价：31 条，覆盖 12 个景点/展馆，含 3 条 1-2 分差评
--   （差评是故意留的：AI 评价摘要要能聚类出「问题标签」）
-- ---------------------------------------------------------------------
INSERT INTO review (spot_id, user_id, content, score, liked, create_time) VALUES
(1,  4, '观景台的日落真的值，17:00 上去刚好，湖面被染成橘色。',                     5, 32, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1,  5, '视野开阔，但台阶有点陡，腿脚不好的建议走西门缓坡。',                       4, 12, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1,  6, '周末人巨多，栏杆边全是长枪短炮，想拍照得早到。',                           3, 5,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2,  7, '六月荷花最好的时候，栈道穿过去两边都是花。',                               5, 21, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2,  8, '下雨天木板有点滑，带小孩要牵好。',                                         3, 4,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3,  9, '古刹很安静，银杏季来最好看，钟声很治愈。',                                 5, 45, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3,  10,'门票 20 元不算贵，但香烛推销有点多。',                                     3, 6,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3,  4, '素斋在寺后，11 点开饭，去晚了要排队。',                                    4, 9,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4,  5, '摇橹船 50 元一位，船夫会讲湖上的老故事，值。',                             5, 38, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4,  6, '节假日排队一小时起步，建议一早来。',                                       4, 11, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4,  7, '岛上没什么遮阴，夏天记得带伞和水。',                                       3, 3,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(5,  8, '爬到顶层能看到整个云栖湖，值这个票价。',                                   5, 27, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5,  9, '塔内楼梯窄，人多的时候上下要错身。',                                       4, 8,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6,  10,'下雨天来才是对的，烟雨蒙蒙像水墨画。',                                     5, 19, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6,  4, '免费景点，路过顺便拍两张，不用特意安排。',                                 4, 7,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(7,  5, '夏天走这条路最凉快，竹林里比外面低五六度。',                               5, 24, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7,  6, '路标有点少，第一次来容易走岔，绕了半小时。',                               2, 2,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8,  7, '孩子玩了一下午不肯走，小火车和泡泡秀最受欢迎。',                           5, 30, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8,  8, '门票 80 元偏贵，里面还有项目单独收费，性价比一般。',                       2, 14, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8,  9, '沙池旁边就有洗手间和洗手台，带娃很方便。',                                 4, 10, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(9,  10,'常设展质量很高，青瓷那组展品的讲解写得专业。',                             5, 16, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9,  4, '周一闭馆，白跑一趟，出来才看到牌子，建议门口写清楚。',                     2, 20, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9,  5, '馆内很安静，适合慢慢看，冷气也足。',                                       5, 13, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(10, 6, '三月来满园茶花，人不多，很出片。',                                         5, 18, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 7, '花期短，过了三月中就没什么可看的了。',                                     3, 4,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(11, 8, '晚上灯一亮氛围很好，手作摊位挺有意思。',                                   4, 15, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(11, 9, '餐饮偏景区价，一碗面 38，味道一般。',                                      2, 9,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(11, 10,'有现场驻唱，坐着喝杯咖啡听歌很舒服。',                                     5, 11, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(12, 4, '扎染体验 60 元，老师教得细，成品能带走。',                                 5, 22, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(12, 5, '竹编和拓印都值得试，建议预留一小时。',                                     4, 12, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(12, 6, '体验项目要提前预约，现场去经常排不上。',                                   3, 7,  DATE_SUB(NOW(), INTERVAL 6 DAY));

-- review_summary 故意留空：这张表由 AI 摘要任务生成，不造数据

-- 自检
SELECT '用户数' AS 检查项, COUNT(*) AS 结果 FROM user
UNION ALL
SELECT '评价数', COUNT(*) FROM review
UNION ALL
SELECT '差评数(<=2分)', COUNT(*) FROM review WHERE score <= 2;
