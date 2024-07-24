package cn.hein.idempotent.config;

import cn.hein.idempotent.core.IdempotentAspect;
import cn.hein.idempotent.idempotent.IdempotentHandler;
import cn.hein.idempotent.idempotent.JdbcIdempotentHandler;
import cn.hein.idempotent.idempotent.RedisIdempotentHandler;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 自动配置类
 *
 * @author hein
 */
@AutoConfigureAfter({JdbcTemplateAutoConfiguration.class, RedisAutoConfiguration.class})
public class IdempotentAutoConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(ApplicationContext context) {
        return new IdempotentAspect(context);
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    public IdempotentHandler jdbcIdempotentHandler(JdbcTemplate template) {
        return new JdbcIdempotentHandler(template);
    }

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public IdempotentHandler redisIdempotentHandler(StringRedisTemplate template) {
        return new RedisIdempotentHandler(template);
    }
}