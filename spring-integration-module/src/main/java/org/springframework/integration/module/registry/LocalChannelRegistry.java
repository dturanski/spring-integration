/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.integration.module.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.util.Assert;

/**
 * @author David Turanski
 *
 */
public class LocalChannelRegistry implements ChannelRegistry, ApplicationContextAware {
	private final Log logger = LogFactory.getLog(this.getClass());
	private AbstractApplicationContext applicationContext;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.registry.ChannelRegistry#inbound(java.lang.String, org.springframework.integration.MessageChannel)
	 */
	@Override
	public void inbound(String name, MessageChannel channel) {
		Assert.hasText(name, "A valid name is required to register an inbound channel");
		Assert.notNull(channel, "channel cannot be null");
		BridgeHandler handler = new BridgeHandler();
		 
		DirectChannel localChannel = new DirectChannel();
		localChannel.setComponentName(name);
		localChannel.setBeanFactory(applicationContext);
		localChannel.setBeanName(name);
		localChannel.afterPropertiesSet();

		handler.setOutputChannel(channel);
		handler.afterPropertiesSet();

		localChannel.subscribe(handler);
		applicationContext.getBeanFactory().registerSingleton(name, localChannel);

	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.registry.ChannelRegistry#outbound(java.lang.String, org.springframework.integration.MessageChannel)
	 */
	@Override
	public void outbound(String name, MessageChannel channel) {
		Assert.hasText(name, "A valid name is required to register an outbound channel");
		Assert.notNull(channel, "channel cannot be null");
		Assert.isTrue(channel instanceof SubscribableChannel,
				"channel must be of type " + SubscribableChannel.class.getName());
		BridgeHandler handler = new BridgeHandler();
		 
		DirectChannel localChannel = new DirectChannel();
		localChannel.setComponentName(name);
		localChannel.setBeanFactory(applicationContext);
		localChannel.setBeanName(name);
		localChannel.afterPropertiesSet();

		handler.setOutputChannel(localChannel);
		handler.afterPropertiesSet();

		((SubscribableChannel) channel).subscribe(handler);
		applicationContext.getBeanFactory().registerSingleton(name, localChannel);

	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.registry.ChannelRegistry#tap(java.lang.String, org.springframework.integration.MessageChannel)
	 */
	@Override
	public void tap(String name, MessageChannel channel) {
		Assert.hasText(name, "A valid name is required to register a tap channel");
		Assert.notNull(channel, "channel cannot be null");

		DirectChannel localChannel = new DirectChannel();
		localChannel.setComponentName(name);
		localChannel.setBeanFactory(applicationContext);
		localChannel.setBeanName(name);
		WireTap wiretap = new WireTap(localChannel);
		((AbstractMessageChannel) channel).addInterceptor(wiretap);

		 applicationContext.getBeanFactory().registerSingleton(name, localChannel);

	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext =(AbstractApplicationContext) applicationContext;

	}

}
