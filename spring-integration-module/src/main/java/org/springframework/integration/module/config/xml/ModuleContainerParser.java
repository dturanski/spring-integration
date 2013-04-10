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
package org.springframework.integration.module.config.xml;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.module.config.ModuleBeanBuilder;
import org.springframework.integration.module.config.ModuleConfiguration;
import org.springframework.integration.module.config.ModuleContainerBeanBuilder;
import org.springframework.integration.module.config.ModuleContainerConfiguration;
import org.springframework.integration.module.config.ModuleNamespaceUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author David Turanski
 *
 */
public class ModuleContainerParser extends AbstractXmlBeanDefinitionParser<ModuleContainerConfiguration>{

	public ModuleContainerParser() {
		super(new ModuleContainerBeanBuilder());
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.config.xml.AbstractXmlBeanDefinitionParser#buildBeanConfiguration(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	@Override
	protected ModuleContainerConfiguration buildBeanConfiguration(Element element, ParserContext parserContext) {
		ModuleContainerConfiguration moduleContainerConfiguration = new ModuleContainerConfiguration();
		return moduleContainerConfiguration;
	}

}
