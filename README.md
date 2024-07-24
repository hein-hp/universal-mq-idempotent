# universal-mq-idempotentHandler
通用 MQ 幂等处理
## 基于 MySQL
表结构如下：
```sql
DROP TABLE IF EXISTS `t_idempotent_message`;
CREATE TABLE `t_idempotent_message` (
  `application_name` VARCHAR (255) NOT NULL COMMENT '应用名',
  `unique_key` VARCHAR (255) NOT NULL COMMENT '消息的唯一键（建议使用业务主键）',
  `status` VARCHAR (16) NOT NULL COMMENT '消息的消费状态',
  `expire` BIGINT (20) NOT NULL COMMENT '去重记录的过期时间（时间戳）',
UNIQUE KEY `uniq_key` ( `application_name`, `unique_key` ) USING BTREE 
) ENGINE = InnoDB DEFAULT CHARSET = utf8 ROW_FORMAT = COMPACT;
```

## 如何使用

以 RocketMQ 为例：

```java
import cn.hein.idempotent.annotation.Idempotent;
import cn.hein.idempotent.enums.IdempotentMediumEnum;
import cn.hein.rocketmqtest.entity.MessageWrapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author hein
 */
@Component
@RocketMQMessageListener(
        topic = "test-topic", consumerGroup = "cg_group"
)
public class MessageConsumer implements RocketMQListener<MessageWrapper> {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    @Override
    @Idempotent(uniqueKey = "#message.getMessageId() + '_' + #message.hashCode()", uniqueKeyPrefix = "test",
            timeout = 10, unit = TimeUnit.MINUTES, mediumEnum = IdempotentMediumEnum.JDBC)
    public void onMessage(MessageWrapper message) {
        // Xxx
        log.info("收到消息：[{}]", message);
    }
}
```

