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
package org.springframework.integration.module;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.message.GenericMessage;

/**
 * @author David Turanski
 *
 */
public class ModuleContainerTests {
	@Test
	public void test() throws Exception {
		ApplicationContext ctx = new GenericApplicationContext();
		
		ModuleContainer moduleContainer = new ModuleContainer();
		moduleContainer.setApplicationContext(ctx);
		moduleContainer.afterPropertiesSet();
		moduleContainer.resolveModules();
		
		MessageChannel input1 = ctx.getBean("flow1.input",MessageChannel.class);
		SubscribableChannel output1 = ctx.getBean("flow1.output",SubscribableChannel.class);
		
		MessageChannel input2 = ctx.getBean("flow2.input",MessageChannel.class);
		SubscribableChannel output2 = ctx.getBean("flow2.output",SubscribableChannel.class);
		
		final AtomicBoolean messageReceived = new AtomicBoolean();
		
		output1.subscribe(new MessageHandler() {
			
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				messageReceived.set(true);
				assertEquals("hello",message.getPayload());
			}
		});
		
		messageReceived.set(false);
		input1.send(new GenericMessage<String>("hello"));
		
		
		output2.subscribe(new MessageHandler() {
			
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				messageReceived.set(true);
				assertEquals("HELLO",message.getPayload());
			}
		});
		
		messageReceived.set(false);
		input2.send(new GenericMessage<String>("hello"));
		
		assertTrue(messageReceived.get());
	}
}
