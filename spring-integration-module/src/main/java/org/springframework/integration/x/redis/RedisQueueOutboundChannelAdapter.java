package org.springframework.integration.x.redis;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.Message;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.util.Assert;

/**
 * @author Mark Fisher
 */
public class RedisQueueOutboundChannelAdapter extends AbstractMessageHandler {

	private final String queueName;

	private volatile boolean extractPayload = true;

	private final StringRedisTemplate redisTemplate = new StringRedisTemplate();

	private final ObjectMapper objectMapper = new ObjectMapper();


	public RedisQueueOutboundChannelAdapter(String queueName, RedisConnectionFactory connectionFactory) {
		Assert.hasText(queueName, "queueName is required");
		Assert.notNull(connectionFactory, "connectionFactory must not be null");
		this.queueName = queueName;
		this.redisTemplate.setConnectionFactory(connectionFactory);
		this.redisTemplate.afterPropertiesSet();
	}


	public void setExtractPayload(boolean extractPayload) {
		this.extractPayload = extractPayload;
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		String s = (this.extractPayload) ? message.getPayload().toString() : this.objectMapper.writeValueAsString(message);
		if (logger.isDebugEnabled()) {
			logger.debug("sending to redis queue '" + this.queueName + "': " + s);
		}
		this.redisTemplate.boundListOps(this.queueName).leftPush(s);
	}

}
