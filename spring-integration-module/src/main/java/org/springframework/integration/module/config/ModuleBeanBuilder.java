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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author David Turanski
 *
 */
public class ModuleBeanBuilder implements BeanBuilder<ModuleConfiguration>{

	/* (non-Javadoc)
	 * @see org.springframework.integration.module.config.BeanBuilder#build(java.lang.Object, org.springframework.beans.factory.support.BeanDefinitionRegistry)
	 */
	@Override
	public BeanDefinition build(ModuleConfiguration configuration, BeanDefinitionRegistry registry) {
		return null;
	}

	 
}
