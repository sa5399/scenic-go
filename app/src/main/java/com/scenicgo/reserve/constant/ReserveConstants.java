package com.scenicgo.reserve.constant;

public class ReserveConstants {

    /** 待支付保留时长（分钟），超时关单按它判 */
    public static final int PAY_TIMEOUT_MINUTES = 15;

    /** 订单号前缀，形如 SG20261005xxxxxxxx */
    public static final String ORDER_NO_PREFIX = "SG";

    /** 核销码前缀，形如 CK20261005xxxxxxxx */
    public static final String CHECKIN_CODE_PREFIX = "CK";

    /** 随机后缀的取值上界（8 位数字），撞号由 uk_order_no / uk_checkin_code 兜底 */
    public static final int RANDOM_BOUND = 100_000_000;

    /** 超时关单一次最多处理多少张单，避免积压时一把捞太多 */
    public static final int TIMEOUT_BATCH_SIZE = 200;

    private ReserveConstants() {
    }
}
