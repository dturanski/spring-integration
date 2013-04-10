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
public class ModuleParser extends AbstractXmlBeanDefinitionParser<ModuleConfiguration>{

	public ModuleParser() {
		super(new ModuleBeanBuilder());
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.config.xml.AbstractXmlBeanDefinitionParser#buildBeanConfiguration(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
	 */
	@Override
	protected ModuleConfiguration buildBeanConfiguration(Element element, ParserContext parserContext) {
		List<String> definedAttributes = Arrays.asList("id","module-name","referenced-bean-locations","properties");
		
		Element props = DomUtils.getChildElementByTagName(element, "props");

		if (element.hasAttribute("properties") && props != null) {
			parserContext.getReaderContext().error(
					"Element cannot have both a 'properties' attribute and an inner 'props' element", element);
		}
		
		ModuleConfiguration moduleConfiguration = new ModuleConfiguration();
	 
		NamedNodeMap attributes = element.getAttributes();
		for (int i=0; i< attributes.getLength(); i++) {
			Node node = attributes.item(i);
			String name = node.getLocalName();
			if (!definedAttributes.contains(name)) {
				if (props != null) {
					parserContext.getReaderContext().error(
						"Element cannot have both the undeclared attribute '" + name +"' and an inner 'props' element", element);
				}
				moduleConfiguration.addProperty(name, node.getNodeValue());
			}
		}

		ModuleNamespaceUtils.setAttributeValueIfDefined(element, "id", moduleConfiguration);
		ModuleNamespaceUtils.setAttributeValueIfDefined(element, "module-name", moduleConfiguration);
		String referencedBeanLocations = element.getAttribute("referenced-bean-locations");
		
		if (StringUtils.hasText(referencedBeanLocations)) {
			moduleConfiguration.setReferencedBeanLocations(Arrays.asList(referencedBeanLocations.split(",")));
		}
				
		if (props != null) {
			moduleConfiguration.setProperties(parserContext.getDelegate().parsePropsElement(props));
		} else {
			ModuleNamespaceUtils.setAttributeValueIfDefined(element, "properties", moduleConfiguration, "propertiesReference");
		}
		return moduleConfiguration;
	}

	 
}
