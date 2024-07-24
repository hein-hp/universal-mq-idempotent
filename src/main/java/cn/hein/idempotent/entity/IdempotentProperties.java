package cn.hein.idempotent.entity;

import cn.hein.idempotent.enums.IdempotentMediumEnum;
import lombok.Builder;
import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * 幂等参数
 *
 * @author hein
 */
@Data
@Builder
public class IdempotentProperties {

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 幂等 Key
     */
    private String uniqueKey;

    /**
     * 幂等 Key 前缀，{@link IdempotentMediumEnum#REDIS} 可选
     */
    private String uniqueKeyPrefix;

    /**
     * 幂等 Key 过期时间
     */
    private long timeout;

    /**
     * 幂等 Key 过期时间单位
     */
    private TimeUnit unit;

    /**
     * 幂等处理媒介
     * @see IdempotentMediumEnum
     */
    private IdempotentMediumEnum mediumEnum;
}