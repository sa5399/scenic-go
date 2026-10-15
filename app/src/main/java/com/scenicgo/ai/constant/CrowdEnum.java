package com.scenicgo.ai.constant;

import lombok.Getter;

/**
 * 人群 → 对应的人群标签名（tag.type = 2）。
 *
 * <p>规则版推荐用它把「带娃」这类输入翻成一个标签，不需要语义模型。
 */
@Getter
public enum CrowdEnum {

    FAMILY("family", "亲子"),
    COUPLE("couple", "情侣"),
    STUDENT("student", "学生党");

    private final String code;
    private final String tagName;

    CrowdEnum(String code, String tagName) {
        this.code = code;
        this.tagName = tagName;
    }

    /** 认不出来就返回 null，按"没指定人群"处理，不报错 */
    public static CrowdEnum of(String code) {
        for (CrowdEnum crowd : values()) {
            if (crowd.code.equals(code)) {
                return crowd;
            }
        }
        return null;
    }
}
