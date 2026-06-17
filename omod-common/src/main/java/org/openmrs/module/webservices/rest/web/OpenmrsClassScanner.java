/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

/**
 * Reflection utilities to search the classpath for classes
 */
public class OpenmrsClassScanner implements ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsClassScanner.class);

	private ApplicationContext applicationContext;

	private MetadataReaderFactory metadataReaderFactory;
	
	private String pattern = "classpath*:org/openmrs/**/*.class";

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.metadataReaderFactory = new CachingMetadataReaderFactory(applicationContext);
	}

	/**
	 * Searches for classes extending or implementing the given type.
	 * 
	 * @param <T>
	 * @param type
	 * @param concrete true if only concrete classes should be returned
	 * @return the list of found classes
	 * @throws IOException
	 */
	
	public <T> List<Class<? extends T>> getClasses(Class<? extends T> type, boolean concrete) throws IOException {
		
		List<Class<? extends T>> types = new ArrayList<Class<? extends T>>();
		
		Resource[] resources = applicationContext.getResources(pattern);
		
		TypeFilter typeFilter = new AssignableTypeFilter(type);
		
		for (Resource resource : resources) {
			
			try {
				
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
				
				if (typeFilter.match(metadataReader, metadataReaderFactory)
				        && concrete == metadataReader.getClassMetadata().isConcrete()) {
					
					String classname = metadataReader.getClassMetadata().getClassName();
					
					try {
						
						@SuppressWarnings("unchecked")
						Class<? extends T> metadata = (Class<? extends T>) OpenmrsClassLoader.getInstance()
						        .loadClass(classname);
						
						types.add(metadata);
						
					}
					
					catch (ClassNotFoundException e) {
						
						throw new IllegalStateException("Class cannot be loaded: " + classname, e);
						
					}

					
				}
				
			}
			
			catch (IOException e) {
				
				log.debug("Resource cannot be loaded: {}", resource);
				
			}
			
		}
		
		return types;
		
	}
}
