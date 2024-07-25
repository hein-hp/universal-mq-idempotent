package cn.hein.idempotent.core;

import cn.hein.idempotent.annotation.Idempotent;
import cn.hein.idempotent.entity.IdempotentProperties;
import cn.hein.idempotent.idempotent.IdempotentHandler;
import cn.hein.idempotent.idempotent.JdbcIdempotentHandler;
import cn.hein.idempotent.idempotent.RedisIdempotentHandler;
import cn.hein.idempotent.toolkits.SpELUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * 幂等切面
 *
 * @author hein
 */
@Aspect
public class IdempotentAspect {

    private static final Logger log = LoggerFactory.getLogger(IdempotentAspect.class);

    private final ApplicationContext context;

    public static final String APPLICATION_NAME_KEY = "spring.application.name";

    public IdempotentAspect(ApplicationContext context) {
        this.context = context;
    }

    @Around("@annotation(cn.hein.idempotent.annotation.Idempotent)")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
        IdempotentProperties prop = buildProp(joinPoint, getIdempotent(joinPoint));
        IdempotentHandler handler = Optional.ofNullable(getHandler(prop))
                .orElseThrow(() -> new IllegalArgumentException("no IdempotentHandler found."));
        Object res = null;
        if (handler.setConsumingIfNX(prop)) {
            try {
                res = joinPoint.proceed();
            } catch (Throwable e) {
                try {
                    handler.delete(prop);
                } catch (Exception ex) {
                    log.error("Error when delete idempotent message record {}.", prop, ex);
                }
                throw e;
            }
            try {
                log.debug("Set consume status as CONSUMED, {}", prop);
                handler.setConsumed(prop);
            } catch (Exception ex) {
                log.debug("Ignore ex {}", prop, ex);
            }
        } else {
            if (handler.isConsumed(prop)) {
                log.debug("The message has consumed, no need consume again.");
                return res;
            } else {
                throw new Exception("The same message record is considered consuming, try consume later.");
            }
        }
        return res;
    }

    private IdempotentHandler getHandler(IdempotentProperties prop) {
        IdempotentHandler handler = null;
        switch (prop.getMediumEnum()) {
            case REDIS -> handler = context.getBean(RedisIdempotentHandler.class);
            case JDBC -> handler = context.getBean(JdbcIdempotentHandler.class);
        }
        return handler;
    }

    private IdempotentProperties buildProp(ProceedingJoinPoint joinPoint, Idempotent idempotent) {
        return IdempotentProperties.builder()
                .uniqueKey(parseKey(idempotent.uniqueKey(), joinPoint))
                .uniqueKeyPrefix(idempotent.uniqueKeyPrefix())
                .mediumEnum(idempotent.mediumEnum())
                .timeout(idempotent.timeout())
                .unit(idempotent.unit())
                .applicationName(context.getEnvironment().getProperty(APPLICATION_NAME_KEY))
                .build();
    }

    private Idempotent getIdempotent(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return joinPoint.getTarget().getClass()
                .getDeclaredMethod(signature.getName(), signature.getMethod().getParameterTypes()).getAnnotation(Idempotent.class);
    }

    private String parseKey(String key, ProceedingJoinPoint joinPoint) {
        return SpELUtil.parseKey(key,
                ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getArgs()).toString();
    }
}