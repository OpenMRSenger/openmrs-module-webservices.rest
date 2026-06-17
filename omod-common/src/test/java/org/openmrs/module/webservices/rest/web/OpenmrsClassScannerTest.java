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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReaderFactory;

public class OpenmrsClassScannerTest {

	private OpenmrsClassScanner scanner;
	private ApplicationContext mockContext;

	@Before
	public void setup() {
		scanner = new OpenmrsClassScanner();
		mockContext = mock(ApplicationContext.class);
		
		// Return empty resources for simple instantiation test
		try {
			when(mockContext.getResources(anyString())).thenReturn(new Resource[0]);
		} catch (IOException e) {
			// ignore
		}
		
		scanner.setApplicationContext(mockContext);
	}

	@Test
	public void shouldBeConfigurableWithPattern() {
		String defaultPattern = scanner.getPattern();
		assertEquals("classpath*:org/openmrs/**/*.class", defaultPattern);
		
		scanner.setPattern("classpath*:org/other/**/*.class");
		assertEquals("classpath*:org/other/**/*.class", scanner.getPattern());
	}
	
	@Test
	public void shouldReturnEmptyListIfNoResourcesMatch() throws Exception {
		List<Class<? extends Object>> classes = scanner.getClasses(Object.class, true);
		assertNotNull(classes);
		assertTrue(classes.isEmpty());
	}
}
