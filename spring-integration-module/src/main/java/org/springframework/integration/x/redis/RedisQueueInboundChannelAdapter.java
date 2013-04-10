package org.springframework.integration.x.redis;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.Message;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;

/**
 * @author Mark Fisher
 */
public class RedisQueueInboundChannelAdapter extends MessageProducerSupport {

	private final String queueName;

	private volatile boolean extractPayload = true;

	private final StringRedisTemplate redisTemplate = new StringRedisTemplate();

	private volatile TaskScheduler taskScheduler;

	private volatile ScheduledFuture<?> listenerTask;

	private final ObjectMapper objectMapper = new ObjectMapper();


	public RedisQueueInboundChannelAdapter(String queueName, RedisConnectionFactory connectionFactory) {
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
	protected void onInit() {
		super.onInit();
		this.taskScheduler = this.getTaskScheduler();
		if (this.taskScheduler == null) {
			ThreadPoolTaskScheduler tpts = new ThreadPoolTaskScheduler();
			tpts.afterPropertiesSet();
			this.taskScheduler = tpts;
		}
	}

	@Override
	protected void doStart() {
		super.doStart();
		this.listenerTask = this.taskScheduler.schedule(new ListenerTask(), new Date());
	}

	@Override
	protected void doStop() {
		super.doStop();
		if (this.listenerTask != null) {
			this.listenerTask.cancel(true);
		}
	}


	private class ListenerTask implements Runnable {

		@Override
		public void run() {
			while (isRunning()) {
				String next = redisTemplate.boundListOps(queueName).rightPop(5, TimeUnit.SECONDS);
				if (next != null) {
					try {
						Message<?> message = null;
						if (extractPayload) {
							message = MessageBuilder.withPayload(next).build();
						}
						else {
							MessageDeserializationWrapper wrapper = objectMapper.readValue(next, MessageDeserializationWrapper.class);
							message = wrapper.getMessage();
						}
						sendMessage(message);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


	@SuppressWarnings("unused") // used by object mapper
	private static class MessageDeserializationWrapper {

		private volatile Map<String, Object> headers;

		private volatile Object payload;

		private volatile Message<?> message;

		void setHeaders(Map<String, Object> headers) {
			this.headers = headers;
		}

		void setPayload(Object payload) {
			this.payload = payload;
		}

		Message<?> getMessage() {
			if (this.message == null) {
				this.message = MessageBuilder.withPayload(this.payload).copyHeaders(this.headers).build();
			}
			return this.message;
		}
	}

}
