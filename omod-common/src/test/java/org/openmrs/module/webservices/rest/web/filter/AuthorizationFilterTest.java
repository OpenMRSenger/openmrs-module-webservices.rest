/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.filter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for {@link AuthorizationFilter}.
 */
public class AuthorizationFilterTest extends BaseModuleWebContextSensitiveTest {
	
	private AuthorizationFilter filter;
	
	@Before
	public void setup() {
		filter = new AuthorizationFilter();
	}
	
	@Test
	public void doFilter_shouldAllowAllowedIp() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME, "127.0.0.1"));
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("127.0.0.1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = Mockito.mock(FilterChain.class);
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(chain).doFilter(request, response);
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}
	
	@Test
	public void doFilter_shouldBlockForbiddenIp() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME, "127.0.0.1"));
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteAddr("1.2.3.4");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = Mockito.mock(FilterChain.class);
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(chain, Mockito.never()).doFilter(request, response);
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
	}
	
	@Test
	public void doFilter_shouldHandleXForwardedFor() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME, "1.2.3.4"));
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = Mockito.mock(FilterChain.class);
		
		filter.doFilter(request, response, chain);
		
		Mockito.verify(chain).doFilter(request, response);
		Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}
}
