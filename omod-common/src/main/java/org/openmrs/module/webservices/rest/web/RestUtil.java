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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.OpenmrsData;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.api.SubResource;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Convenient helper methods for the Rest Web Services module.
 */
public class RestUtil implements GlobalPropertyListener {
	
	private static Log log = LogFactory.getLog(RestUtil.class);
	
	private static boolean contextEnabled = true;

	/**
	 * Returns the global property value with the given name
	 * @param propertyName the global property to retrieve
	 * @return the global property value with the given name
	 */
	private static String getGlobalProperty(String propertyName, String defaultValue) {
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			return Context.getAdministrationService().getGlobalProperty(propertyName, defaultValue);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}
	
	/**
	 * Looks up the admin defined global property for the system limit
	 * 
	 * @return Integer limit
	 * @see RestConstants#MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME
	 */
	public static Integer getDefaultLimit() {
		String limit = getGlobalProperty(RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME, null);
		if (StringUtils.isNotEmpty(limit)) {
			try {
				return Integer.parseInt(limit);
			}
			catch (NumberFormatException nfex) {
				log.error(
				    RestConstants.MAX_RESULTS_DEFAULT_GLOBAL_PROPERTY_NAME + " must be an integer. " + nfex.getMessage());
				return RestConstants.MAX_RESULTS_DEFAULT;
			}
		} else {
			return RestConstants.MAX_RESULTS_DEFAULT;
		}
	}
	
	/**
	 * Looks up the admin defined global property for the absolute limit to results of REST calls
	 * 
	 * @return Integer limit
	 * @see RestConstants#MAX_RESULTS_ABSOLUTE_GLOBAL_PROPERTY_NAME
	 */
	public static Integer getAbsoluteLimit() {
		String limit = getGlobalProperty(RestConstants.MAX_RESULTS_ABSOLUTE_GLOBAL_PROPERTY_NAME, null);
		if (StringUtils.isNotEmpty(limit)) {
			try {
				return Integer.parseInt(limit);
			}
			catch (NumberFormatException nfex) {
				log.error(
				    RestConstants.MAX_RESULTS_ABSOLUTE_GLOBAL_PROPERTY_NAME + " must be an integer. " + nfex.getMessage());
				return RestConstants.MAX_RESULTS_ABSOLUTE;
			}
		} else {
			return RestConstants.MAX_RESULTS_ABSOLUTE;
		}
	}
	
	/**
	 * Tests whether or not a client's IP address is allowed to have access to the REST API (based on a
	 * admin-settable global property).
	 * 
	 * @param ip address of the client
	 * @return <code>true</code> if client should be allowed access
	 * @see RestConstants#ALLOWED_IPS_GLOBAL_PROPERTY_NAME
	 */
	public static boolean isIpAllowed(String ip) {
		return ipMatches(ip, getAllowedIps());
	}
	
	/**
	 * Tests whether or not there is a match between the given IP address and the candidates.
	 * 
	 * @param ip
	 * @param candidateIps
	 * @return <code>true</code> if there is a match <strong>Should</strong> return true if list is
	 *         empty <strong>Should</strong> return false if there is no match <strong>Should</strong>
	 *         return true for exact match <strong>Should</strong> return true for match with submask
	 *         <strong>Should</strong> return false if there is no match with submask
	 *         <strong>Should</strong> return true for exact ipv6 match <strong>Should</strong> throw
	 *         IllegalArgumentException for invalid mask
	 */
	public static boolean ipMatches(String ip, List<String> candidateIps) {
		if (candidateIps.isEmpty()) {
			return true;
		}
		
		InetAddress address;
		try {
			address = InetAddress.getByName(ip);
		}
		catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid IP in the ip parameter" + ip, e);
		}
		
		for (String candidateIp : candidateIps) {
			// split IP and mask
			String[] candidateIpPattern = candidateIp.split("/");
			
			InetAddress candidateAddress;
			try {
				candidateAddress = InetAddress.getByName(candidateIpPattern[0]);
			}
			catch (UnknownHostException e) {
				throw new IllegalArgumentException("Invalid IP in the candidateIps parameter", e);
			}
			
			if (candidateIpPattern.length == 1) { // there's no mask
				if (address.equals(candidateAddress)) {
					return true;
				}
			} else {
				if (address.getAddress().length != candidateAddress.getAddress().length) {
					continue;
				}
				
				int bits = Integer.parseInt(candidateIpPattern[1]);
				if (candidateAddress.getAddress().length < Math.ceil((double) bits / 8)) {
					throw new IllegalArgumentException(
					        "Invalid mask " + bits + " for IP " + candidateIp + " in the candidateIps parameter");
				}
				
				// compare bytes based on the given mask
				boolean matched = true;
				for (int bytes = 0; bits > 0; bytes++, bits -= 8) {
					int mask = 0x000000FF; // mask the entire byte
					if (bits < 8) {
						// mask only some first bits of a byte
						mask = (mask << (8 - bits));
					}
					if ((address.getAddress()[bytes] & mask) != (candidateAddress.getAddress()[bytes] & mask)) {
						matched = false;
						break;
					}
				}
				if (matched) {
					return true;
				}
			}
			
		}
		return false;
	}
	
	/**
	 * Returns a list of IPs which can access the REST API based on a global property. In case the
	 * property is empty, returns an empty list.
	 * <p>
	 * IPs should be separated by a whitespace or a comma. IPs can be declared with bit masks e.g.
	 * <code>10.0.0.0/30</code> matches <code>10.0.0.0 - 10.0.0.3</code> and <code>10.0.0.0/24</code>
	 * matches <code>10.0.0.0 - 10.0.0.255</code>.
	 * 
	 * @see RestConstants#ALLOWED_IPS_GLOBAL_PROPERTY_NAME
	 * @return the list of IPs
	 */
	public static List<String> getAllowedIps() {
		String allowedIpsProperty = "";
		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			allowedIpsProperty = getGlobalProperty(RestConstants.ALLOWED_IPS_GLOBAL_PROPERTY_NAME, allowedIpsProperty);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}

		
		if (allowedIpsProperty.isEmpty()) {
			return Collections.emptyList();
		} else {
			String[] allowedIps = allowedIpsProperty.split("[\\s,]+");
			return Arrays.asList(allowedIps);
		}
	}
	
	/**
	 * Determines the request representation, if not provided, uses default. <br/>
	 * Determines number of results to limit to, if not provided, uses default set by admin. <br/>
	 * Determines how far into a list to start with given the startIndex param. <br/>
	 * 
	 * @param request the current http web request
	 * @param response the current http web response
	 * @param defaultView the representation to use if none specified
	 * @return a {@link RequestContext} object filled with all the necessary values
	 * @see RestConstants#REQUEST_PROPERTY_FOR_LIMIT
	 * @see RestConstants#REQUEST_PROPERTY_FOR_REPRESENTATION
	 * @see RestConstants#REQUEST_PROPERTY_FOR_START_INDEX
	 * @see RestConstants#REQUEST_PROPERTY_FOR_INCLUDE_ALL
	 */
	public static RequestContext getRequestContext(HttpServletRequest request, HttpServletResponse response,
	                                               Representation defaultView) {
		if (defaultView == null)
			defaultView = Representation.DEFAULT;
		
		RequestContext ret = new RequestContext();
		ret.setRequest(request);
		ret.setResponse(response);
		
		// get the "v" param for the representations
		String temp = request.getParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION);
		if ("".equals(temp)) {
			throw new IllegalArgumentException("?v=(empty string) is not allowed");
		} else if (temp == null || temp.equalsIgnoreCase(defaultView.getRepresentation())) {
			ret.setRepresentation(defaultView);
		} else {
			ret.setRepresentation(Context.getService(RestService.class).getRepresentation(temp));
		}
		
		// get the "t" param for subclass-specific requests
		temp = request.getParameter(RestConstants.REQUEST_PROPERTY_FOR_TYPE);
		if ("".equals(temp)) {
			throw new IllegalArgumentException(
			        "?" + RestConstants.REQUEST_PROPERTY_FOR_TYPE + "=(empty string) is not allowed");
		} else {
			ret.setType(temp);
		}
		
		// fetch the "limit" param
		Integer limit = getIntegerParam(request, RestConstants.REQUEST_PROPERTY_FOR_LIMIT);
		if (limit != null) {
			ret.setLimit(limit);
		}
		
		// fetch the startIndex param
		Integer startIndex = getIntegerParam(request, RestConstants.REQUEST_PROPERTY_FOR_START_INDEX);
		if (startIndex != null) {
			ret.setStartIndex(startIndex);
		}
		
		Boolean includeAll = getBooleanParam(request, RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL);
		if (includeAll != null) {
			ret.setIncludeAll(includeAll);
		}
		return ret;
	}
	
	/**
	 * Determines the request representation with Representation.DEFAULT as the default view.
	 * 
	 * @param request the current http web request
	 * @param response the current http web response
	 * @return a {@link RequestContext} object filled with all the necessary values
	 * @see getRequestContext(javax.servlet.http.HttpServletRequest,
	 *      org.openmrs.module.webservices.rest.web.representation.Representation)
	 */
	public static RequestContext getRequestContext(HttpServletRequest request, HttpServletResponse response) {
		return getRequestContext(request, response, Representation.DEFAULT);
	}
	
	/**
	 * Convenience method to get the given param out of the given request.
	 * 
	 * @param request the WebRequest to look in
	 * @param param the string name to fetch
	 * @return null if the param doesn't exist or is not a valid integer
	 */
	private static Integer getIntegerParam(HttpServletRequest request, String param) {
		String paramString = request.getParameter(param);
		
		if (paramString != null) {
			try {
				return new Integer(paramString);// return the valid value
			}
			catch (NumberFormatException e) {
				log.debug("unable to parse '" + param + "' parameter into a valid integer: " + paramString);
			}
		}
		
		return null;
	}
	
	/**
	 * Convenience method to get the given param out of the given request as a boolean.
	 * 
	 * @param request the WebRequest to look in
	 * @param param the string name to fetch
	 * @return <code>true</code> if the param is equal to 'true', <code>false</code> for any empty
	 *         value, null value, or not equal to 'true', or missing param. <strong>Should</strong>
	 *         return true only if request param is 'true'
	 */
	public static Boolean getBooleanParam(HttpServletRequest request, String param) {
		try {
			return ServletRequestUtils.getBooleanParameter(request, param);
		}
		catch (ServletRequestBindingException e) {
			return false;
		}
	}
	
	/**
	 * Sets the HTTP status on the response according to the exception
	 * 
	 * @param ex
	 * @param response
	 */
	public static void setResponseStatus(Throwable ex, HttpServletResponse response) {
		ResponseStatus ann = ex.getClass().getAnnotation(ResponseStatus.class);
		if (ann != null) {
			if (StringUtils.isNotBlank(ann.reason())) {
				response.setStatus(ann.value().value(), ann.reason());
			} else {
				response.setStatus(ann.value().value());
			}
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Sets the HTTP status on the response to no content, and returns an empty value, suitable for
	 * returning from a @ResponseBody annotated Spring controller method.
	 * 
	 * @param response
	 * @return
	 */
	public static Object noContent(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		return "";
	}
	
	/**
	 * Sets the HTTP status for CREATED and (if 'created' has a uri) the Location header attribute
	 * 
	 * @param response
	 * @param created
	 * @return the object passed in
	 */
	public static Object created(HttpServletResponse response, Object created) {
		response.setStatus(HttpServletResponse.SC_CREATED);
		try {
			String uri = (String) PropertyUtils.getProperty(created, "uri");
			response.addHeader("Location", uri);
		}
		catch (Exception ex) {}
		return created;
	}
	
	/**
	 * Sets the HTTP status for UPDATED and (if 'updated' has a uri) the Location header attribute
	 * 
	 * @param response
	 * @param updated
	 * @return the object passed in
	 */
	public static Object updated(HttpServletResponse response, Object updated) {
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			String uri = (String) PropertyUtils.getProperty(updated, "uri");
			response.addHeader("Location", uri);
		}
		catch (Exception ex) {}
		return updated;
	}
	
	/**
	 * Updates the Uri prefix through which clients consuming web services will connect to the web app
	 * 
	 * @return the webapp's Url prefix
	 */
	public static void setUriPrefix() {
		if (contextEnabled) {
			RestConstants.URI_PREFIX = getGlobalProperty(RestConstants.URI_PREFIX_GLOBAL_PROPERTY_NAME, null);
		}
		
		if (StringUtils.isBlank(RestConstants.URI_PREFIX)) {
			RestConstants.URI_PREFIX = "";
		}
		
		// append the trailing slash in case the user forgot it
		if (!RestConstants.URI_PREFIX.endsWith("/")) {
			RestConstants.URI_PREFIX += "/";
		}
		
		RestConstants.URI_PREFIX = RestConstants.URI_PREFIX + "ws/rest/";
	}
	
	/**
	 * It allows to disable calls to Context. It should be used in TESTS ONLY.
	 */
	public static void disableContext() {
		contextEnabled = false;
	}
	
	/**
	 * A Set is returned by removing voided data from passed Collection. The Collection passed as
	 * parameter is not modified
	 * 
	 * @param input collection of OpenmrsData
	 * @return non-voided OpenmrsData
	 */
	public static <D extends OpenmrsData, C extends Collection<D>> Set<D> removeVoidedData(C input) {
		Set<D> data = new LinkedHashSet<D>();
		for (D d : input) {
			if (!d.isVoided()) {
				data.add(d);
			}
		}
		return data;
	}
	
	/**
	 * A Set is returned by removing retired data from passed Collection. The Collection passed as
	 * parameter is not modified
	 * 
	 * @param input collection of OpenmrsMetadata
	 * @return non-retired OpenmrsMetaData
	 */
	public static <M extends OpenmrsMetadata, C extends Collection<M>> Set<M> removeRetiredData(C input) {
		Set<M> data = new LinkedHashSet<M>();
		for (M m : input) {
			if (!m.isRetired()) {
				data.add(m);
			}
		}
		return data;
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#supportsPropertyName(java.lang.String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return propertyName.equals(RestConstants.URI_PREFIX_GLOBAL_PROPERTY_NAME);
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyChanged(org.openmrs.GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty newValue) {
		setUriPrefix();
	}
	
	/**
	 * @see org.openmrs.api.GlobalPropertyListener#globalPropertyDeleted(java.lang.String)
	 */
	@Override
	public void globalPropertyDeleted(String propertyName) {
		setUriPrefix();
	}
	
	/**
	 * Inspects the cause chain for the given throwable, looking for an exception of the given class
	 * (e.g. to find an APIAuthenticationException wrapped in an InvocationTargetException)
	 * 
	 * @param throwable
	 * @param causeClassToLookFor
	 * @return whether any exception in the cause chain of throwable is an instance of
	 *         causeClassToLookFor
	 */
	public static boolean hasCause(Throwable throwable, Class<? extends Throwable> causeClassToLookFor) {
		return ExceptionUtils.indexOfType(throwable, causeClassToLookFor) >= 0;
	}
	
	/**
	 * Gets a list of classes in a given package. Note that interfaces are not returned.
	 * 
	 * @param pkgname the package name.
	 * @param suffix the ending text on name. eg "Resource.class"
	 * @return the list of classes.
	 */
	public static ArrayList<Class<?>> getClassesForPackage(String pkgname, String suffix) throws IOException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		
		//Get a File object for the package
		File directory = null;
		String relPath = pkgname.replace('.', '/');
		Enumeration<URL> resources = OpenmrsClassLoader.getInstance().getResources(relPath);
		while (resources.hasMoreElements()) {
			
			URL resource = resources.nextElement();
			if (resource == null) {
				throw new RuntimeException("No resource for " + relPath);
			}
			
			try {
				directory = new File(resource.toURI());
			}
			catch (URISyntaxException e) {
				throw new RuntimeException(
				        pkgname + " (" + resource
				                + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...",
				        e);
			}
			catch (IllegalArgumentException ex) {}
			
			//If folder exists, look for all resource class files in it.
			if (directory != null && directory.exists()) {
				
				//Get the list of the files contained in the package
				String[] files = directory.list();
				
				for (int i = 0; i < files.length; i++) {
					
					//We are only interested in Resource.class files
					if (files[i].endsWith(suffix)) {
						
						//Remove the .class extension
						String className = pkgname + '.' + files[i].substring(0, files[i].length() - 6);
						
						try {
							Class<?> cls = Class.forName(className);
							if (!cls.isInterface())
								classes.add(cls);
						}
						catch (ClassNotFoundException e) {
							throw new RuntimeException("ClassNotFoundException loading " + className);
						}
					}
				}
			} else {
				
				//Directory does not exist, look in jar file.
				JarFile jarFile = null;
				try {
					String fullPath = resource.getFile();
					String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
					jarFile = new JarFile(jarPath);
					
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						
						String entryName = entry.getName();
						
						if (!entryName.endsWith(suffix))
							continue;
						
						if (entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
							String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
							
							try {
								Class<?> cls = Class.forName(className);
								if (!cls.isInterface())
									classes.add(cls);
							}
							catch (ClassNotFoundException e) {
								throw new RuntimeException("ClassNotFoundException loading " + className);
							}
						}
					}
				}
				catch (IOException e) {
					throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
				}
				finally {
					if (jarFile != null) {
						jarFile.close();
					}
				}
			}
		}
		
		return classes;
	}
	
	/**
	 * Wraps the exception message as a SimpleObject to be sent to client
	 * 
	 * @param ex
	 * @param reason
	 * @return
	 */
	public static SimpleObject wrapErrorResponse(Exception ex, String reason) {
		
		String message = ex.getMessage();
		Throwable cause = ex.getCause();
		while (cause != null) {
			String msg = cause.getMessage();
			if (StringUtils.isNotBlank(msg)) {
				if (StringUtils.isNotBlank(message)) {
					message += " => ";
				}
				else {
					message = "";
				}
				message += msg;
			}
			cause = cause.getCause();
		}
		
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		if (reason != null && !reason.isEmpty()) {
			map.put("message", reason + " [" + message + "]");
		} else {
			map.put("message", "[" + message + "]");
		}
		StackTraceElement[] stackTraceElements = ex.getStackTrace();
		if (stackTraceElements.length > 0) {
			StackTraceElement stackTraceElement = ex.getStackTrace()[0];
			String stackTraceDetailsEnabledGp = null;
			try {
				Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				stackTraceDetailsEnabledGp = getGlobalProperty(RestConstants.ENABLE_STACK_TRACE_DETAILS_GLOBAL_PROPERTY_NAME, "false");
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			}
			map.put("code", stackTraceElement.getClassName() + ":" + stackTraceElement.getLineNumber());
			if ("true".equalsIgnoreCase(stackTraceDetailsEnabledGp)) {
				map.put("detail", ExceptionUtils.getStackTrace(ex));
			} else {
				map.put("detail", "");
			}
		} else {
			map.put("code", "");
			map.put("detail", "");
		}
		map.put("rawMessage", ex.getMessage());
		String translatedMessage = Context.getMessageSourceService().getMessage(ex.getMessage(), null, null, Context.getLocale());
		map.put("translatedMessage", translatedMessage);
		
		return new SimpleObject().add("error", map);
	}
	
	/**
	 * Creates a SimpleObject to sent to the client with all validation errors (with message codes
	 * resolved)
	 * 
	 * @param ex
	 * @return
	 */
	public static SimpleObject wrapValidationErrorResponse(ValidationException ex) {
		
		MessageSourceService messageSourceService = Context.getMessageSourceService();
		
		SimpleObject errors = new SimpleObject();
		errors.add("message", messageSourceService.getMessage("webservices.rest.error.invalid.submission"));
		errors.add("code", "webservices.rest.error.invalid.submission");
		
		List<SimpleObject> globalErrors = new ArrayList<SimpleObject>();
		SimpleObject fieldErrors = new SimpleObject();
		
		if (ex.getErrors().hasGlobalErrors()) {
			
			for (Object errObj : ex.getErrors().getGlobalErrors()) {
				
				ObjectError err = (ObjectError) errObj;
				String message = messageSourceService.getMessage(err.getCode(), err.getArguments(), err.getDefaultMessage(), Context.getLocale());
				
				SimpleObject globalError = new SimpleObject();
				globalError.put("code", err.getCode());
				globalError.put("message", message);
				globalErrors.add(globalError);
			}
			
		}
		
		if (ex.getErrors().hasFieldErrors()) {
			
			for (Object errObj : ex.getErrors().getFieldErrors()) {
				FieldError err = (FieldError) errObj;
				String message = messageSourceService.getMessage(err.getCode(), err.getArguments(), err.getDefaultMessage(), Context.getLocale());
				
				SimpleObject fieldError = new SimpleObject();
				fieldError.put("code", err.getCode());
				fieldError.put("message", message);
				
				if (!fieldErrors.containsKey(err.getField())) {
					fieldErrors.put(err.getField(), new ArrayList<SimpleObject>());
				}
				
				((List<SimpleObject>) fieldErrors.get(err.getField())).add(fieldError);
			}
			
		}
		
		errors.put("globalErrors", globalErrors);
		errors.put("fieldErrors", fieldErrors);
		
		return new SimpleObject().add("error", errors);
	}
	
	/**
	 * Gets the supported type for the specified resource object
	 *
	 * @param resource the resource object whose supported type to look up
	 * @return the supported class object
	 */
	public static Class<?> getSupportedClass(Resource resource) {
		Class<? extends Resource> resourceClass = resource.getClass();
		if (resource instanceof SubResource) {
			return resourceClass.getAnnotation(org.openmrs.module.webservices.rest.web.annotation.SubResource.class)
			        .supportedClass();
		} else {
			return resourceClass.getAnnotation(org.openmrs.module.webservices.rest.web.annotation.Resource.class)
			        .supportedClass();
		}
	}

	public static boolean isValidUuid(String uuid) {
		return uuid != null && (uuid.length() == 36 || uuid.length() == 38 || uuid.indexOf(' ') < 0 || uuid.indexOf('.') < 0);
	}
}
