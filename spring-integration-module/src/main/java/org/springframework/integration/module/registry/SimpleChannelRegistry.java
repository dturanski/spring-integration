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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.MessageChannel;

/**
 * @author David Turanski
 *
 */
public class SimpleChannelRegistry implements ChannelRegistry {
	private final Log logger = LogFactory.getLog(this.getClass());
	private Map<String, MessageChannel> inboundChannels = new HashMap<String, MessageChannel>();
	private Map<String, MessageChannel> outboundChannels = new HashMap<String, MessageChannel>();
	private Map<String, MessageChannel> tapChannels = new HashMap<String, MessageChannel>();

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
		inboundChannels.put(name, channel);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.registry.ChannelRegistry#outbound(java.lang.String, org.springframework.integration.MessageChannel)
	 */
	@Override
	public void outbound(String name, MessageChannel channel) {
		outboundChannels.put(name, channel);

	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.registry.ChannelRegistry#tap(java.lang.String, org.springframework.integration.MessageChannel)
	 */
	@Override
	public void tap(String name, MessageChannel channel) {
		tapChannels.put(name, channel);
	}

	public MessageChannel getTap(String name) {
		return tapChannels.get(name);
	}

	public MessageChannel getInput(String name) {
		return inboundChannels.get(name);
	}
	
	public MessageChannel getOutput(String name) {
		return outboundChannels.get(name);
	}
}
