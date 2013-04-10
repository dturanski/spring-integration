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

import org.springframework.core.Conventions;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author David Turanski
 *
 */
//TODO: This should be moved to core, e.g., merged with IntegrationNamespaceUtils
public class ModuleNamespaceUtils {
	public static void setAttributeValueIfDefined(Element element, String attributeName, Object object) {
		setAttributeValueIfDefined(element, attributeName, object, null);
	}
	
	public static void setAttributeValueIfDefined(Element element, String attributeName, Object object, String propertyName) {
		SpelExpressionParser parser = new SpelExpressionParser();

		if (!StringUtils.hasText(element.getAttribute(attributeName))) {
			return;
		}

		String propName = propertyName == null? Conventions.attributeNameToPropertyName(attributeName) : propertyName;
		Expression expression = parser.parseExpression(propName);
		expression.setValue(object, element.getAttribute(attributeName));
	}
}
