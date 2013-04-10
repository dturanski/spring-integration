package org.springframework.integration.module.registry;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.MessageChannel;

/**
 * @author Mark Fisher
 */
public interface ChannelRegistry extends DisposableBean {

	void inbound(String name, MessageChannel channel);

	void outbound(String name, MessageChannel channel);

	void tap(String name, MessageChannel channel);

}
