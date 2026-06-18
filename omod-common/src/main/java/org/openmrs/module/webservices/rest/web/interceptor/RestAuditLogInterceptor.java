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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.time.Instant;

/**
 * Interceptor that logs access to medical data for audit purposes (NEN 7510 compliance).
 */
public class RestAuditLogInterceptor extends HandlerInterceptorAdapter {
	
	private static final Logger auditLog = LoggerFactory.getLogger("REST_AUDIT_LOGGER");
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String path = request.getRequestURI();
		String method = request.getMethod();
		String ipAddress = request.getRemoteAddr();
		
		// For IP, also check X-Forwarded-For to handle proxies correctly
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isEmpty()) {
			ipAddress = xff.split(",")[0].trim();
		}
		
		String user = Context.isAuthenticated() ? Context.getAuthenticatedUser().getUsername() : "ANONYMOUS";
		
		// check if the request touches medical data (patient, encounter, obs, etc.)
		if (isMedicalDataAccess(path)) {
			// Log in structured JSON-like format for SIEM ingestion
			auditLog.info("{{\"timestamp\":\"{}\", \"event\":\"MEDICAL_DATA_ACCESS\", \"user\":\"{}\", \"ip\":\"{}\", \"method\":\"{}\", \"uri\":\"{}\"}}",
			    Instant.now(), user, ipAddress, method, path);
		}
		
		return true;
	}
	
	/**
	 * Determines if the given path relates to medical data access.
	 * @param path the request path
	 * @return true if it's medical data access
	 */
	private boolean isMedicalDataAccess(String path) {
		return path.contains("/rest/v1/patient") || 
		       path.contains("/rest/v1/encounter") || 
		       path.contains("/rest/v1/obs") ||
		       path.contains("/rest/v1/visit") ||
		       path.contains("/rest/v1/order") ||
		       path.contains("/rest/v1/allergy") ||
		       path.contains("/rest/v1/condition") ||
		       path.contains("/rest/v1/diagnosis");
	}
}
