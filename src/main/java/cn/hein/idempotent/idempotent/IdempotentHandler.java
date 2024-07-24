package cn.hein.idempotent.idempotent;

import cn.hein.idempotent.entity.IdempotentProperties;

/**
 * 幂等处理器
 *
 * @author hein
 */
public interface IdempotentHandler {

    /**
     * 向去重表插入 {@link cn.hein.idempotent.enums.ComsumeStatusEnum#CONSUMING} 状态，
     * @return 插入成功返回 true，否则返回 false
     */
    boolean setConsumingIfNX(IdempotentProperties prop);

    /**
     * 在去重表中设置消息消费状态为 {@link cn.hein.idempotent.enums.ComsumeStatusEnum#CONSUMED}
     */
    void setConsumed(IdempotentProperties prop);

    /**
     * 删除消息记录
     */
    void delete(IdempotentProperties prop);

    /**
     * 判断去重表中的消息是否是 {@link cn.hein.idempotent.enums.ComsumeStatusEnum#CONSUMED} 状态
     */
    boolean isConsumed(IdempotentProperties prop);
}