package cn.hein.idempotent.idempotent;

import cn.hein.idempotent.entity.IdempotentProperties;
import cn.hein.idempotent.enums.ComsumeStatusEnum;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于 Redis 幂等处理器
 *
 * @author hein
 */
public class RedisIdempotentHandler extends AbstractIdempotentHandler {

    private final StringRedisTemplate template;

    public RedisIdempotentHandler(StringRedisTemplate template) {
        this.template = template;
    }

    @Override
    public boolean setConsumingIfNX(IdempotentProperties prop) {
        return Boolean.TRUE.equals(template.opsForValue().setIfAbsent(getKey(prop),
                ComsumeStatusEnum.CONSUMING.getCode(), prop.getTimeout(), prop.getUnit()));
    }

    @Override
    public void setConsumed(IdempotentProperties prop) {
        template.opsForValue().set(getKey(prop),
                ComsumeStatusEnum.CONSUMED.getCode(), prop.getTimeout(), prop.getUnit());
    }

    @Override
    public void delete(IdempotentProperties prop) {
        template.delete(getKey(prop));
    }

    @Override
    public boolean isConsumed(IdempotentProperties prop) {
        return Boolean.TRUE.equals(ComsumeStatusEnum.CONSUMED.getCode().equals(getStatus(prop)));
    }

    protected String getStatus(IdempotentProperties prop) {
        return template.opsForValue().get(getKey(prop));
    }
}