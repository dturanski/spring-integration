package org.springframework.integration.x.redis;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.Message;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.support.converter.MessageConverter;
import org.springframework.integration.support.converter.SimpleMessageConverter;
import org.springframework.util.Assert;

/**
 * This is a temporary replacement for the RedisInboundChannelAdapter until issue INT-2946 is
 * resolved. The only difference is that this one will create a PatternTopic instead of a
 * ChannelTopic any time one of the topic Strings contains a wildcard character. The solution for
 * INT-2946 should likely be more sophisticated (possibly enabling both 'topic' and 'pattern'
 * properties to be configured via corresponding attributes in the schema).
 * 
 * @author Mark Fisher
 */
public class RedisSubscribingChannelAdapter extends MessageProducerSupport {

	private final RedisMessageListenerContainer container = new RedisMessageListenerContainer();

	private volatile MessageConverter messageConverter = new SimpleMessageConverter();

	private volatile String[] topics;

	private volatile RedisSerializer<?> serializer = new StringRedisSerializer();

	public RedisSubscribingChannelAdapter(RedisConnectionFactory connectionFactory) {
		Assert.notNull(connectionFactory, "connectionFactory must not be null");
		this.container.setConnectionFactory(connectionFactory);
	}

	public void setSerializer(RedisSerializer<?> serializer) {
		Assert.notNull(serializer, "'serializer' must not be null");
		this.serializer = serializer;
	}

	public void setTopics(String... topics) {
		this.topics = topics;
	}

	public void setMessageConverter(MessageConverter messageConverter) {
		Assert.notNull(messageConverter, "messageConverter must not be null");
		this.messageConverter = messageConverter;
	}

	@Override
	public String getComponentType() {
		return "redis:inbound-channel-adapter";
	}

	@Override
	protected void onInit() {
		super.onInit();
		Assert.notEmpty(this.topics, "at least one topic is required for subscription");
		MessageListenerDelegate delegate = new MessageListenerDelegate();
		MessageListenerAdapter adapter = new MessageListenerAdapter(delegate);
		adapter.setSerializer(this.serializer);
		List<Topic> topicList = new ArrayList<Topic>();
		for (String topic : this.topics) {
			if (topic.contains("*")) {
				topicList.add(new PatternTopic(topic));
			}
			else {
				topicList.add(new ChannelTopic(topic));
			}
		}
		adapter.afterPropertiesSet();
		this.container.addMessageListener(adapter, topicList);
		this.container.afterPropertiesSet();
	}

	@Override
	protected void doStart() {
		super.doStart();
		this.container.start();
	}


	@Override
	protected void doStop() {
		super.doStop();
		this.container.stop();
	}

	private Message<?> convertMessage(String s) {
		return this.messageConverter.toMessage(s);
	}


	private class MessageListenerDelegate {

		@SuppressWarnings("unused")
		public void handleMessage(String s) {
			sendMessage(convertMessage(s));
		}
	}

}
