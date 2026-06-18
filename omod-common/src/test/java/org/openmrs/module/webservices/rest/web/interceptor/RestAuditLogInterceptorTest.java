/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.interceptor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link RestAuditLogInterceptor}.
 */
public class RestAuditLogInterceptorTest extends BaseModuleWebContextSensitiveTest {
	
	private RestAuditLogInterceptor interceptor;
	
	@Before
	public void setup() {
		interceptor = new RestAuditLogInterceptor();
	}
	
	@Test
	public void preHandle_shouldReturnTrue() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/v1/patient");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assert.assertTrue(interceptor.preHandle(request, response, new Object()));
	}
	
	@Test
	public void preHandle_shouldHandleXForwardedFor() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/v1/patient");
		request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assert.assertTrue(interceptor.preHandle(request, response, new Object()));
	}
	
	@Test
	public void preHandle_shouldIdentifyMedicalDataAccess() throws Exception {
		String[] medicalPaths = {
		        "/rest/v1/patient", "/rest/v1/encounter", "/rest/v1/obs", "/rest/v1/visit", "/rest/v1/order",
		        "/rest/v1/allergy", "/rest/v1/condition", "/rest/v1/diagnosis"
		};
		
		for (String path : medicalPaths) {
			MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
			MockHttpServletResponse response = new MockHttpServletResponse();
			Assert.assertTrue(interceptor.preHandle(request, response, new Object()));
		}
	}
	
	@Test
	public void preHandle_shouldIgnoreNonMedicalDataAccess() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rest/v1/concept");
		MockHttpServletResponse response = new MockHttpServletResponse();
		Assert.assertTrue(interceptor.preHandle(request, response, new Object()));
	}
}
