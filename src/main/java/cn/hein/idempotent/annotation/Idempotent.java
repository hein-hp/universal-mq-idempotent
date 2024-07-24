package cn.hein.idempotent.annotation;

import cn.hein.idempotent.enums.IdempotentMediumEnum;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等注解
 *
 * @author hein
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等 Key
     */
    String uniqueKey() default "";

    /**
     * 幂等 Key 前缀，{@link IdempotentMediumEnum#REDIS} 可选
     */
    String uniqueKeyPrefix() default "";

    /**
     * 幂等 Key 过期时间
     */
    long timeout() default 120L;

    /**
     * 幂等 Key 过期时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 幂等处理媒介
     * @see IdempotentMediumEnum
     */
    IdempotentMediumEnum mediumEnum() default IdempotentMediumEnum.REDIS;
}