package cn.hein.idempotent.idempotent;

import cn.hein.idempotent.entity.IdempotentProperties;
import cn.hein.idempotent.enums.ComsumeStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * 基于 Jdbc 幂等处理器
 *
 * @author hein
 */
public class JdbcIdempotentHandler extends AbstractIdempotentHandler {

    private static final Logger log = LoggerFactory.getLogger(JdbcIdempotentHandler.class);

    private final JdbcTemplate template;

    public JdbcIdempotentHandler(JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public boolean setConsumingIfNX(IdempotentProperties prop) {
        long expire = getExpire(prop);
        try {
            template.update("""
                    INSERT INTO t_idempotent_message(application_name, unique_key, status, expire) values (?, ?, ?, ?)
                    """, prop.getApplicationName(), getKey(prop), ComsumeStatusEnum.CONSUMING.getCode(), expire);
        } catch (DuplicateKeyException e) {
            log.debug("found consuming or consumed record, setConsumingIfNX fail {}", prop);
            // MySQL 不支持消息 TTL，出现主键重复有可能是过期的记录，这里动态删除这里记录重试
            int del = delete(prop, true);
            if (del > 0) {
                log.debug("delete {} expire records, now retry again", del);
                return setConsumingIfNX(prop);
            }
            return false;
        } catch (Exception e) {
            log.error("Unknown error when jdbc insert, consider success", e);
            return true;
        }
        return true;
    }

    @Override
    public void setConsumed(IdempotentProperties prop) {
        template.update("""
                UPDATE t_idempotent_message SET status = ?, expire = ? WHERE application_name = ? AND unique_key = ?
                """, ComsumeStatusEnum.CONSUMED.getCode(), getExpire(prop), prop.getApplicationName(), getKey(prop));
    }

    @Override
    public void delete(IdempotentProperties prop) {
        delete(prop, false);
    }

    protected String getStatus(IdempotentProperties prop) {
        Map<String, Object> res = template.queryForMap("""
                SELECT status FROM t_idempotent_message where application_name = ? AND unique_key  = ? and expire > ?
                """, prop.getApplicationName(), getKey(prop), getTime());
        return (String) res.get("status");
    }

    private int delete(IdempotentProperties prop, boolean onlyExpire) {
        if (onlyExpire) {
            return template.update("""
                    DELETE FROM t_idempotent_message WHERE application_name = ? AND unique_key = ? AND expire < ?
                    """, prop.getApplicationName(), getKey(prop), getTime());
        }
        return template.update("""
                DELETE FROM t_idempotent_message WHERE application_name = ? AND unique_key = ?
                """, prop.getApplicationName(), getKey(prop));
    }
}