package cn.hein.idempotent.enums;

/**
 * 幂等处理媒介枚举
 *
 * @author hein
 */
public enum IdempotentMediumEnum {

    /**
     * 基于 MySQL 做幂等处理媒介
     */
    JDBC,

    /**
     * 基于 Redis 做幂等处理媒介
     */
    REDIS
}