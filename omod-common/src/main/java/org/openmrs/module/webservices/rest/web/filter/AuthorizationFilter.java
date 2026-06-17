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

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter intended for all /ws/rest calls that allows the user to authenticate via Basic
 * authentication. (It will not fail on invalid or missing credentials. We count on the API to throw
 * exceptions if an unauthenticated user tries to do something they are not allowed to do.) <br/>
 * <br/>
 * IP address authorization is also performed based on the global property:
 * {@link RestConstants#ALLOWED_IPS_GLOBAL_PROPERTY_NAME}
 */
public class AuthorizationFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);

	/**
	 * Fragment of the message that OpenMRS core's {@code HibernateContextDAO} uses when a user is
	 * temporarily locked out after exceeding {@code security.allowedFailedLoginsBeforeLockout}
	 * failed login attempts (see {@code security.unlockAccountWaitingTime}). There is no public
	 * exception type or constant for this, so it is matched on the message text core throws.
	 */
	private static final String LOCKOUT_MESSAGE_FRAGMENT = "connection attempts";

	private static final int SC_TOO_MANY_REQUESTS = 429;

	private static final String RETRY_AFTER_SECONDS = "300";
	
	private static final Logger auditLog = LoggerFactory.getLogger("REST_AUDIT_LOGGER");
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.debug("Initializing REST WS Authorization filter");
	}
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying REST WS Authorization filter");
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		
		String ipAddress = null;
		if (request instanceof HttpServletRequest) {
			String xff = ((HttpServletRequest) request).getHeader("X-Forwarded-For");
			if (StringUtils.isNotBlank(xff)) {
				ipAddress = xff.split(",")[0].trim();
			}
		}
		if (StringUtils.isBlank(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}
		
		// check the IP address first.  If its not valid, return a 403
		if (!RestUtil.isIpAllowed(ipAddress)) {
			auditLog.warn("IP access denied: {}", ipAddress);
			// the ip address is not valid, set a 403 http error code
			HttpServletResponse httpresponse = (HttpServletResponse) response;
			httpresponse.sendError(HttpServletResponse.SC_FORBIDDEN,
			    "IP address '" + ipAddress + "' is not authorized");
			return;
		}
		
		// skip if the session has timed out, we're already authenticated, or it's not an HTTP request
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session timed out");
			}
			
			if (!Context.isAuthenticated()) {
				String basicAuth = httpRequest.getHeader("Authorization");
				if (basicAuth != null) {
					// check that header is in format "Basic ${base64encode(username + ":" + password)}"
					if (basicAuth.startsWith("Basic")) {
						try {
							// Enforce SSL/TLS for Basic Auth to prevent cleartext credentials leakage
							boolean isSecure = httpRequest.isSecure() || "https".equalsIgnoreCase(httpRequest.getHeader("X-Forwarded-Proto"));
							if (!isSecure) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "SSL/TLS is required for Basic Authentication");
								return;
							}
							
							// remove the leading "Basic "
							basicAuth = basicAuth.substring(6);
							if (StringUtils.isBlank(basicAuth)) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials provided");
								return;
							}
							
							String decoded = new String(Base64.decodeBase64(basicAuth), Charset.forName("UTF-8"));
							if (StringUtils.isBlank(decoded) || !decoded.contains(":")) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials provided");
								return;
							}
							
							String[] userAndPass = decoded.split(":");
							try {
								Context.authenticate(userAndPass[0], userAndPass[1]);
								log.debug("authenticated [{}]", userAndPass[0]);
							}
							catch (Exception ex) {
								auditLog.warn("Authentication failed for user '{}' from IP {}", userAndPass[0], ipAddress);
								throw ex;
							}
						}
						catch (Exception ex) {
							// This filter never stops execution. If the user failed to
							// authenticate, that will be caught later.
							log.debug("authentication exception ", ex);
						}
					}
				}
			}
		}
		
		// continue with the filter chain (unless IP is not allowed)
		chain.doFilter(request, response);
	}

	/**
	 * Attempts to authenticate with the given username/password pair, handling the OpenMRS core
	 * account lockout (security.allowedFailedLoginsBeforeLockout /
	 * security.unlockAccountWaitingTime) by responding with 429 instead of letting the lockout
	 * be treated as an ordinary failed login.
	 *
	 * @return true if the caller should stop processing the request (lockout response sent),
	 *         false otherwise
	 */
	private boolean authenticate(String[] userAndPass, String ipAddress, HttpServletResponse response) throws IOException {
		try {
			Context.authenticate(userAndPass[0], userAndPass[1]);
			log.debug("authenticated [{}]", userAndPass[0]);
		}
		catch (ContextAuthenticationException ex) {
			// OpenMRS core already tracks failed login attempts per user
			// (security.allowedFailedLoginsBeforeLockout /
			// security.unlockAccountWaitingTime) and throws this once a user
			// is locked out. Surface it as 429 instead of silently letting
			// the request fall through as an ordinary failed login, and log
			// it so brute-force attempts are detectable.
			if (StringUtils.contains(ex.getMessage(), LOCKOUT_MESSAGE_FRAGMENT)) {
				log.warn("Account temporarily locked due to too many failed login attempts: user [{}], IP [{}]",
				    userAndPass[0], ipAddress);
				response.setHeader("Retry-After", RETRY_AFTER_SECONDS);
				response.sendError(SC_TOO_MANY_REQUESTS, "Too many failed login attempts. Please try again later.");
				return true;
			}
			log.warn("Failed authentication attempt: user [{}], IP [{}]", userAndPass[0], ipAddress);
		}
		return false;
	}
}
