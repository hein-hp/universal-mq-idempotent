package cn.hein.idempotent.enums;

import lombok.Getter;

/**
 * 消费状态枚举
 *
 * @author hein
 */
@Getter
public enum ComsumeStatusEnum {

    /**
     * 消费中
     */
    CONSUMING("0"),

    /**
     * 消费完成
     */
    CONSUMED("1");

    private final String code;

    ComsumeStatusEnum(String code) {
        this.code = code;
    }
}
