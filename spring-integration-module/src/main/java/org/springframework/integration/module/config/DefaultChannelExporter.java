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
package org.springframework.integration.module.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.integration.MessageChannel;

/**
 * @author David Turanski
 * @since 3.0
 * 
 * The Default ChannelExporter for Modules. Selects zero or one input channels and zero to many output channels from a 
 * {@link DefaultListableBeanFactory}. Subclasses may override isInputChannel() and isOutputChannel() to implement an alternate 
 * strategy. 
 *
 */
public class DefaultChannelExporter implements ChannelExporter {
	 
	/* (non-Javadoc)
	 * @see org.springframework.integration.module.ChannelExporter#getInputChannel()
	 */
	@Override
	public Entry<String,MessageChannel> getInputChannel(Map<String,MessageChannel> channels) {
		
		for (Entry<String,MessageChannel> entry: channels.entrySet()) {
			if (isInputChannel(entry.getKey(), entry.getValue())) {
				return entry;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.ChannelExporter#getOutputChannels()
	 */
	@Override
	public Map<String,MessageChannel> getOutputChannels(Map<String,MessageChannel> channels) {
		Map <String,MessageChannel> outputChannels = new HashMap<String,MessageChannel>();
		for (Entry<String,MessageChannel> entry: channels.entrySet()) {
			if (isOutputChannel(entry.getKey(), entry.getValue())) {
				outputChannels.put(entry.getKey(),entry.getValue());
			}
		}
		return outputChannels;
	}
	
	/**
 * Determine if a given bean is an output channel
	 * @param channelName
	 * @param channel the MessageChannel
	 * @return true if this is an outputChannel
	 */
	protected boolean isOutputChannel(String channelName, MessageChannel channel) {
		return channelName.startsWith("output");
	}
	
	/**
	 * Determine if a given bean is the input channel
	 * @param channelName
	 * @param channel the MessageChannel
	 * @return true if this is an inputChannel
	 */
	protected boolean isInputChannel(String channelName, MessageChannel channel) {
		return channelName.equals("input");
	}
}
