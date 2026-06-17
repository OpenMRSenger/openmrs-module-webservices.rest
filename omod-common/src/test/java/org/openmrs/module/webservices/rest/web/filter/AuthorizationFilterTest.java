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

import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the {@link AuthorizationFilter} class, in particular the brute-force lockout
 * handling added on top of OpenMRS core's built-in
 * {@code security.allowedFailedLoginsBeforeLockout} / {@code security.unlockAccountWaitingTime}
 * mechanism.
 */
public class AuthorizationFilterTest extends BaseModuleWebContextSensitiveTest {

	private static final String ADMIN_USERNAME = "admin";

	private static final String ADMIN_PASSWORD = "test";

	private static final String ALLOWED_LOGINS_GP = "security.allowedFailedLoginsBeforeLockout";

	private AuthorizationFilter filter;

	private MockFilterChain chain;

	private MockHttpServletRequest req;

	private MockHttpServletResponse resp;

	@Before
	public void setUp() {
		filter = new AuthorizationFilter();
		chain = new MockFilterChain();
		req = new MockHttpServletRequest();
		resp = new MockHttpServletResponse();
		req.setSecure(true);
	}

	private static String basicAuthHeader(String username, String password) {
		String credentials = username + ":" + password;
		return "Basic " + new String(Base64.encodeBase64(credentials.getBytes(Charset.forName("UTF-8"))));
	}

	@Test
	public void doFilter_shouldAuthenticateWithValidCredentialsOverHttps() throws Exception {
		Context.logout();
		req.addHeader("Authorization", basicAuthHeader(ADMIN_USERNAME, ADMIN_PASSWORD));

		filter.doFilter(req, resp, chain);

		Assert.assertTrue(Context.isAuthenticated());
		Assert.assertEquals(200, resp.getStatus());
	}

	@Test
	public void doFilter_shouldRejectBasicAuthOverPlainHttp() throws Exception {
		Context.logout();
		req.setSecure(false);
		req.addHeader("Authorization", basicAuthHeader(ADMIN_USERNAME, ADMIN_PASSWORD));

		filter.doFilter(req, resp, chain);

		Assert.assertFalse(Context.isAuthenticated());
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatus());
	}

	@Test
	public void doFilter_shouldReturn400WhenBasicAuthHeaderIsBlank() throws Exception {
		Context.logout();
		req.addHeader("Authorization", "Basic ");

		filter.doFilter(req, resp, chain);

		Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
	}

	@Test
	public void doFilter_shouldReturn400WhenDecodedCredentialsHaveNoColon() throws Exception {
		Context.logout();
		String encoded = new String(Base64.encodeBase64("nocolonhere".getBytes(Charset.forName("UTF-8"))));
		req.addHeader("Authorization", "Basic " + encoded);

		filter.doFilter(req, resp, chain);

		Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, resp.getStatus());
	}

	@Test
	public void doFilter_shouldNotBlockRequestOnOrdinaryFailedLogin() throws Exception {
		Context.logout();
		req.addHeader("Authorization", basicAuthHeader(ADMIN_USERNAME, "wrong-password"));

		filter.doFilter(req, resp, chain);

		Assert.assertFalse(Context.isAuthenticated());
		Assert.assertEquals(200, resp.getStatus());
	}

	@Test
	public void doFilter_shouldReturn429AndRetryAfterOnceAccountIsLockedOut() throws Exception {
		Context.getAdministrationService().saveGlobalProperty(new GlobalProperty(ALLOWED_LOGINS_GP, "1"));
		Context.logout();

		// 1st failed attempt: recorded by core, threshold (1) not yet exceeded
		req.addHeader("Authorization", basicAuthHeader(ADMIN_USERNAME, "wrong-password"));
		filter.doFilter(req, resp, chain);
		Assert.assertNotEquals(429, resp.getStatus());

		// 2nd failed attempt: exceeds the threshold, core marks the account as locked out
		// (it still throws an ordinary "bad credentials" exception for this attempt itself)
		MockHttpServletRequest req2 = new MockHttpServletRequest();
		req2.setSecure(true);
		req2.addHeader("Authorization", basicAuthHeader(ADMIN_USERNAME, "wrong-password"));
		MockHttpServletResponse resp2 = new MockHttpServletResponse();
		filter.doFilter(req2, resp2, new MockFilterChain());
		Assert.assertNotEquals(429, resp2.getStatus());

		// 3rd attempt, even with the correct password, is rejected while locked out
		MockHttpServletRequest req3 = new MockHttpServletRequest();
		req3.setSecure(true);
		req3.addHeader("Authorization", basicAuthHeader(ADMIN_USERNAME, ADMIN_PASSWORD));
		MockHttpServletResponse resp3 = new MockHttpServletResponse();
		filter.doFilter(req3, resp3, new MockFilterChain());
		Assert.assertEquals(429, resp3.getStatus());
		Assert.assertEquals("300", resp3.getHeader("Retry-After"));
		Assert.assertFalse(Context.isAuthenticated());
	}

	@Test
	public void doFilter_shouldReturn403WhenIpNotAllowed() throws Exception {
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME, "192.0.2.1"));
		req.setRemoteAddr("203.0.113.5");

		filter.doFilter(req, resp, chain);

		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatus());
	}
}
