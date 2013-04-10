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

import java.util.List;
import java.util.Properties;

/**
 * @author David Turanski
 *
 */
public class ModuleConfiguration {
	private String id;
	private String moduleName;
	private Properties properties;
	private String propertiesReference;
	private List<String> referencedBeanLocations;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return moduleName;
	}
	/**
	 * @param moduleName the moduleName to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}
	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	/**
	 * @return the referencedBeanLocations
	 */
	public List<String> getReferencedBeanLocations() {
		return referencedBeanLocations;
	}
	/**
	 * @param referencedBeanLocations the referencedBeanLocations to set
	 */
	public void setReferencedBeanLocations(List<String> referencedBeanLocations) {
		this.referencedBeanLocations = referencedBeanLocations;
	}
	/**
	 * @return the propertiesReference
	 */
	public String getPropertiesReference() {
		return propertiesReference;
	}
	/**
	 * @param propertiesReference the propertiesReference to set
	 */
	public void setPropertiesReference(String propertiesReference) {
		this.propertiesReference = propertiesReference;
	}
	
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new Properties();
		}
		properties.setProperty(key, value);
	}
}
