package org.springframework.integration.module.registry;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.Lifecycle;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.util.Assert;

/**
 * @author Mark Fisher
 */
public class RabbitChannelRegistry implements ChannelRegistry {

	private final Log logger = LogFactory.getLog(this.getClass());

	private final JsonMessageConverter messageConverter = new JsonMessageConverter();

	private final ConnectionFactory connectionFactory;

	private final RabbitAdmin admin;

	private final List<Lifecycle> lifecycleBeans = new ArrayList<Lifecycle>();


	public RabbitChannelRegistry(ConnectionFactory connectionFactory) {
		Assert.notNull(connectionFactory, "connectionFactory must not be null");
		this.connectionFactory = connectionFactory;
		this.admin = new RabbitAdmin(this.connectionFactory);
	}

	@Override
	public void inbound(String name, MessageChannel channel) {
		this.admin.declareQueue(new Queue(name));
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(this.connectionFactory);
		container.setQueueNames(name);
		AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container);
		adapter.setOutputChannel(channel);
		adapter.setMessageConverter(messageConverter);
		adapter.afterPropertiesSet();
		this.lifecycleBeans.add(adapter);
		adapter.start();
	}

	@Override
	public void outbound(String name, MessageChannel channel) {
		this.admin.declareQueue(new Queue(name));
		this.admin.declareExchange(new FanoutExchange(name));
		MessageHandler handler = new CompositeHandler(name, this.connectionFactory, this.messageConverter);
		if (channel instanceof SubscribableChannel) {
			EventDrivenConsumer consumer = new EventDrivenConsumer((SubscribableChannel) channel, handler);
			consumer.afterPropertiesSet();
			this.lifecycleBeans.add(consumer);
			consumer.start();
		}
		else {
			throw new IllegalArgumentException("SubscribableChannel required");
		}
	}

	@Override
	public void tap(String name, MessageChannel channel) {
		Queue queue = this.admin.declareQueue();
		FanoutExchange exchange = new FanoutExchange(name);
		this.admin.declareExchange(exchange);
		Binding binding = BindingBuilder.bind(queue).to(exchange);
		this.admin.declareBinding(binding);
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(this.connectionFactory);
		container.setQueueNames(queue.getName());
		container.afterPropertiesSet();
		AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(container);
		adapter.setOutputChannel(channel);
		adapter.setMessageConverter(messageConverter);
		adapter.afterPropertiesSet();
		this.lifecycleBeans.add(adapter);
		adapter.start();
	}

	@Override
	public void destroy() {
		for (Lifecycle bean : this.lifecycleBeans) {
			try {
				bean.stop();
			}
			catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("failed to stop bean", e);
				}
			}
		}
	}

	private static class CompositeHandler extends AbstractMessageHandler {

		private final AmqpOutboundEndpoint mainEndpoint;

		private final AmqpOutboundEndpoint tapEndpoint;

		private CompositeHandler(String name, ConnectionFactory connectionFactory, MessageConverter messageConverter) {
			RabbitTemplate template = new RabbitTemplate(connectionFactory);
			template.setMessageConverter(messageConverter);
			AmqpOutboundEndpoint mainEndpoint = new AmqpOutboundEndpoint(template);
			mainEndpoint.setRoutingKey(name);
			mainEndpoint.afterPropertiesSet();
			this.mainEndpoint = mainEndpoint;
			AmqpOutboundEndpoint tapEndpoint = new AmqpOutboundEndpoint(template);
			tapEndpoint.setExchangeName(name);
			tapEndpoint.afterPropertiesSet();
			this.tapEndpoint = tapEndpoint;
		}

		@Override
		protected void handleMessageInternal(Message<?> message) throws Exception {
			this.tapEndpoint.handleMessage(message);
			this.mainEndpoint.handleMessage(message);
		}
	}
}
