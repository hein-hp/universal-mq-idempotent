package cn.hein.idempotent.idempotent;

import cn.hein.idempotent.entity.IdempotentProperties;
import cn.hein.idempotent.enums.ComsumeStatusEnum;

/**
 * 抽象幂等处理器
 *
 * @author hein
 */
public abstract class AbstractIdempotentHandler implements IdempotentHandler {

    @Override
    public boolean isConsumed(IdempotentProperties prop) {
        return Boolean.TRUE.equals(ComsumeStatusEnum.CONSUMED.getCode().equals(getStatus(prop)));
    }

    protected abstract String getStatus(IdempotentProperties prop);

    protected long getTime() {
        return System.currentTimeMillis();
    }

    protected String getKey(IdempotentProperties prop) {
        if (!prop.getUniqueKeyPrefix().isEmpty()) {
            return prop.getUniqueKeyPrefix() + ":" + prop.getUniqueKey();
        }
        return prop.getUniqueKey();
    }

    protected long getExpire(IdempotentProperties prop) {
        return getTime() + prop.getUnit().toMillis(prop.getTimeout());
    }
}