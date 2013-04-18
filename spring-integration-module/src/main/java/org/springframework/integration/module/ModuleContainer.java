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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.module.config.ChannelExporter;
import org.springframework.integration.module.config.DefaultChannelExporter;
import org.springframework.integration.module.registry.ChannelRegistry;
import org.springframework.integration.module.registry.LocalChannelRegistry;
import org.springframework.util.Assert;

import com.apple.eawt.ApplicationAdapter;

/**
 * @author David Turanski
 * @since 3.0
 */
public class ModuleContainer implements InitializingBean, ApplicationContextAware {
	private static Log logger = LogFactory.getLog(ModuleContainer.class);
	private static final String DEFAULT_ROOT_MODULE_PATH = "/META-INF/spring/integration/modules";
	private String modulePath = DEFAULT_ROOT_MODULE_PATH;

	private ChannelRegistry channelRegistry = new LocalChannelRegistry();
	private ApplicationContext applicationContext;
	private ChannelExporter channelExporter = new DefaultChannelExporter();

	/**
	 * @param modulePath the modulePath to set
	 */
	public void setModulePath(String modulePath) {
		this.modulePath = modulePath;
	}

	/**
	 * @param channelRegistry the channelRegistry to set
	 */
	public void setChannelRegistry(ChannelRegistry channelRegistry) {
		this.channelRegistry = channelRegistry;
	}

	public void setChannelExporter(ChannelExporter channelExporter) {
		this.channelExporter = channelExporter;
	}

	public ChannelRegistry getChannelRegistry() {
		return channelRegistry;
	}

	public void resolveModules() {
		PathMatchingResourcePatternResolver resourceResolver = (applicationContext == null) ? new PathMatchingResourcePatternResolver()
				: new PathMatchingResourcePatternResolver(applicationContext);

		try {
			Resource[] resources = resourceResolver.getResources("classpath:" + modulePath + "/*.xml");
			for (Resource res : resources) {
				if (logger.isDebugEnabled()) {
					logger.debug("found module definition - " + res.getFilename());
				}

				String moduleName = res.getFilename().replaceAll(".xml$", "");

				ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
				ctx.setConfigLocations(new String[] {modulePath + "/" + res.getFilename()});
				ctx.refresh();

				Map<String, MessageChannel> channels = ctx.getBeansOfType(MessageChannel.class);

				Entry<String, MessageChannel> inputChannel = channelExporter.getInputChannel(channels);
				Map<String, MessageChannel> outputChannels = channelExporter.getOutputChannels(channels);

				if (inputChannel != null) {
					channelRegistry.inbound(moduleName + "." + inputChannel.getKey(), inputChannel.getValue());
					for (Entry<String, MessageChannel> entry : outputChannels.entrySet()) {
						channelRegistry.outbound(moduleName + "." + entry.getKey(), entry.getValue());
					}
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(applicationContext, "applicationContext cannot be null");
		Assert.notNull(channelRegistry,"channelRegistry cannot be null");
		Assert.notNull(channelExporter,"channelExporter cannot be null");
		if (ApplicationContextAware.class.isAssignableFrom(channelRegistry.getClass())) {
			((ApplicationContextAware) channelRegistry).setApplicationContext(applicationContext);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
