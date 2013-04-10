package org.springframework.integration.x.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHandlingException;
import org.springframework.util.Assert;

/**
 * @author Mark Fisher
 */
public class RedisLastMessageStore {

	private final Log logger = LogFactory.getLog(this.getClass());

	private final StringRedisTemplate template = new StringRedisTemplate();

	@SuppressWarnings("rawtypes")
	public RedisLastMessageStore(RedisConnectionFactory connectionFactory) {
		Assert.notNull(connectionFactory, "connectionFactory must not be null");
		this.template.setConnectionFactory(connectionFactory);
		this.template.setHashValueSerializer(new JacksonJsonRedisSerializer<Message>(Message.class));
		this.template.afterPropertiesSet();
	}

	public void store(Message<?> message) {
		String stream = message.getHeaders().get("stream", String.class);
		if (stream != null) {
			try {
				this.template.boundHashOps("recents").put(stream, message);
			}
			catch (Exception e) {
				throw new MessageHandlingException(message, "failed to store Message", e);
			}
		}
		else {
			if (logger.isWarnEnabled()) {
				logger.warn("no stream header on message: " + message);
			}
		}
	}

}
