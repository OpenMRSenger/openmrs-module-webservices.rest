/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.api.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.APIException;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.api.RestHelperService;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchParameter;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.util.OpenmrsConstants;

import java.util.*;

public class SearchHandlerRegistry {
	
	private volatile Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> searchHandlersByParameter;
	
	private volatile Map<CompositeSearchHandlerKeyValue, SearchHandler> searchHandlersByIds;
	
	private volatile Map<String, Set<SearchHandler>> searchHandlersByResource;
	
	private volatile List<SearchHandler> allSearchHandlers;
	
	public synchronized void initializeSearchHandlers(RestHelperService restHelperService) {
		if (searchHandlersByResource != null) {
			return;
		}
		
		Map<CompositeSearchHandlerKeyValue, SearchHandler> tempSearchHandlersByIds = new HashMap<CompositeSearchHandlerKeyValue, SearchHandler>();
		Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters = new HashMap<CompositeSearchHandlerKeyValue, Set<SearchHandler>>();
		Map<String, Set<SearchHandler>> tempSearchHandlersByResource = new HashMap<String, Set<SearchHandler>>();
		
		List<SearchHandler> allSearchHandlers = restHelperService.getRegisteredSearchHandlers();
		for (SearchHandler searchHandler : allSearchHandlers) {
			addSearchHandler(tempSearchHandlersByIds, tempSearchHandlersByParameters, tempSearchHandlersByResource,
			    searchHandler);
		}
		this.allSearchHandlers = allSearchHandlers;
		searchHandlersByParameter = tempSearchHandlersByParameters;
		searchHandlersByIds = tempSearchHandlersByIds;
		searchHandlersByResource = tempSearchHandlersByResource;
	}
	
	public void clear() {
		searchHandlersByIds = null;
		searchHandlersByParameter = null;
		searchHandlersByResource = null;
		allSearchHandlers = null;
	}
	
	public List<SearchHandler> getAllSearchHandlers() {
		return allSearchHandlers;
	}
	
	public Set<SearchHandler> getSearchHandlers(String resourceName, RestHelperService restHelperService) {
		if (searchHandlersByResource == null) {
			initializeSearchHandlers(restHelperService);
		}
		return searchHandlersByResource.get(resourceName);
	}
	
	public SearchHandler getSearchHandler(String resourceName, Map<String, String[]> parameters, RestHelperService restHelperService) throws APIException {
		initializeSearchHandlers(restHelperService);
		
		Set<SearchParameter> searchParameters = new HashSet<SearchParameter>();
		for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
			if (!RestConstants.SPECIAL_REQUEST_PARAMETERS.contains(parameter.getKey())
			        || RestConstants.REQUEST_PROPERTY_FOR_TYPE.equals(parameter.getKey())) {
				searchParameters.add(new SearchParameter(parameter.getKey(), parameter.getValue()[0]));
			}
		}
		
		String[] searchIds = parameters.get(RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID);
		if (searchIds != null && searchIds.length > 0) {
			SearchHandler searchHandler = searchHandlersByIds.get(new CompositeSearchHandlerKeyValue(resourceName,
			        searchIds[0]));
			if (searchHandler == null) {
				throw new InvalidSearchException("The search with id '" + searchIds[0] + "' for '" + resourceName
				        + "' resource is not recognized");
			} else {
				return searchHandler;
			}
		}
		
		Set<SearchHandler> candidateSearchHandlers = null;
		for (SearchParameter param : searchParameters) {
			Set<SearchHandler> searchHandlers = searchHandlersByParameter.get(new CompositeSearchHandlerKeyValue(
			        resourceName, param.getName(), param.getValue()));
			if (searchHandlers == null) {
				searchHandlers = searchHandlersByParameter.get(new CompositeSearchHandlerKeyValue(resourceName, param
				        .getName()));
				if (searchHandlers == null)
					return null;
			}
			if (candidateSearchHandlers == null) {
				candidateSearchHandlers = new HashSet<SearchHandler>();
				candidateSearchHandlers.addAll(searchHandlers);
			} else {
				candidateSearchHandlers.retainAll(searchHandlers);
			}
		}
		
		if (candidateSearchHandlers == null) {
			return null;
		} else {
			eliminateCandidateSearchHandlersWithMissingRequiredParameters(candidateSearchHandlers, searchParameters);
			
			if (candidateSearchHandlers.isEmpty()) {
				return null;
			} else if (candidateSearchHandlers.size() == 1) {
				return candidateSearchHandlers.iterator().next();
			}
			
			for (SearchHandler candidateSearchHandler : candidateSearchHandlers) {
				if ("default".equals(candidateSearchHandler.getSearchConfig().getId())) {
					return candidateSearchHandler;
				}
			}

			List<String> candidateSearchHandlerIds = new ArrayList<String>();
			for (SearchHandler candidateSearchHandler : candidateSearchHandlers) {
				candidateSearchHandlerIds.add(RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID + "="
						+ candidateSearchHandler.getSearchConfig().getId());
			}
			throw new InvalidSearchException("The search is ambiguous. Please specify "
					+ StringUtils.join(candidateSearchHandlerIds, " or "));
		}
	}
	
	private void eliminateCandidateSearchHandlersWithMissingRequiredParameters(Set<SearchHandler> candidateSearchHandlers,
	        Set<SearchParameter> searchParameters) {
		Iterator<SearchHandler> it = candidateSearchHandlers.iterator();
		while (it.hasNext()) {
			SearchHandler candidateSearchHandler = it.next();
			boolean remove = true;
			
			for (SearchQuery candidateSearchQueries : candidateSearchHandler.getSearchConfig().getSearchQueries()) {
				Set<SearchParameter> requiredParameters = new HashSet<SearchParameter>(
				        candidateSearchQueries.getRequiredParameters());
				
				Iterator<SearchParameter> iterator = requiredParameters.iterator();
				while (iterator.hasNext()) {
					SearchParameter requiredParameter = iterator.next();
					for (SearchParameter param : searchParameters) {
						if (requiredParameter.getValue() == null) {
							if (requiredParameter.getName().equals(param.getName())) {
								iterator.remove();
							}
						} else {
							if (requiredParameter.equals(param)) {
								iterator.remove();
							}
						}
					}
				}
				if (requiredParameters.isEmpty()) {
					remove = false;
					break;
				}
			}
			
			if (remove) {
				it.remove();
			}
		}
	}
	
	private void addSearchHandler(Map<CompositeSearchHandlerKeyValue, SearchHandler> tempSearchHandlersByIds,
	        Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters,
	        Map<String, Set<SearchHandler>> tempSearchHandlersByResource, SearchHandler searchHandler) {
		for (String supportedVersion : searchHandler.getSearchConfig().getSupportedOpenmrsVersions()) {
			if (ModuleUtil.matchRequiredVersions(OpenmrsConstants.OPENMRS_VERSION_SHORT, supportedVersion)) {
				addSupportedSearchHandler(tempSearchHandlersByIds, tempSearchHandlersByParameters, searchHandler);
				addSearchHandlerToResourceMap(tempSearchHandlersByResource, searchHandler);
			}
		}
	}
	
	private void addSupportedSearchHandler(Map<CompositeSearchHandlerKeyValue, SearchHandler> tempSearchHandlersByIds,
	        Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters,
	        SearchHandler searchHandler) {
		CompositeSearchHandlerKeyValue searchHanlderIdKey = new CompositeSearchHandlerKeyValue(searchHandler
		        .getSearchConfig().getSupportedResource(), searchHandler.getSearchConfig().getId());
		SearchHandler previousSearchHandler = tempSearchHandlersByIds.put(searchHanlderIdKey, searchHandler);
		if (previousSearchHandler != null) {
			SearchConfig config = searchHandler.getSearchConfig();
			throw new IllegalStateException("Two search handlers (" + searchHandler.getClass() + ", "
			        + previousSearchHandler.getClass() + ") for the same resource (" + config.getSupportedResource()
			        + ") must not have the same ID (" + config.getId() + ")");
		}
		
		addSearchHandlerToParametersMap(tempSearchHandlersByParameters, searchHandler);
	}
	
	private void addSearchHandlerToParametersMap(
	        Map<CompositeSearchHandlerKeyValue, Set<SearchHandler>> tempSearchHandlersByParameters,
	        SearchHandler searchHandler) {
		for (SearchQuery searchQueries : searchHandler.getSearchConfig().getSearchQueries()) {
			Set<SearchParameter> parameters = new HashSet<SearchParameter>(searchQueries.getRequiredParameters());
			parameters.addAll(searchQueries.getOptionalParameters());
			
			for (SearchParameter parameter : parameters) {
				CompositeSearchHandlerKeyValue parameterKey = new CompositeSearchHandlerKeyValue(searchHandler
				        .getSearchConfig().getSupportedResource(), parameter.getName(), parameter.getValue());
				Set<SearchHandler> list = tempSearchHandlersByParameters.get(parameterKey);
				if (list == null) {
					list = new HashSet<SearchHandler>();
					tempSearchHandlersByParameters.put(parameterKey, list);
				}
				list.add(searchHandler);
			}
		}
	}
	
	private void addSearchHandlerToResourceMap(Map<String, Set<SearchHandler>> tempSearchHandlersByResource,
	        SearchHandler searchHandler) {
		SearchConfig config = searchHandler.getSearchConfig();
		Set<SearchHandler> handlers = tempSearchHandlersByResource.get(config.getSupportedResource());
		if (handlers == null) {
			handlers = new HashSet<SearchHandler>();
			tempSearchHandlersByResource.put(config.getSupportedResource(), handlers);
		}
		handlers.add(searchHandler);
	}
	
	private static class CompositeSearchHandlerKeyValue {
		
		public final String supportedResource;
		
		public final String secondKey;
		
		public final String secondKeyValue;
		
		public CompositeSearchHandlerKeyValue(String supportedResource, String additionalKeyProperty) {
			this.supportedResource = supportedResource;
			this.secondKey = additionalKeyProperty;
			this.secondKeyValue = null;
		}
		
		public CompositeSearchHandlerKeyValue(String supportedResource, String additionalKeyProperty,
		    String additionalKeyPropertyValue) {
			this.supportedResource = supportedResource;
			this.secondKey = additionalKeyProperty;
			this.secondKeyValue = additionalKeyPropertyValue;
		}
		
		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			
			CompositeSearchHandlerKeyValue that = (CompositeSearchHandlerKeyValue) o;
			
			if (!supportedResource.equals(that.supportedResource))
				return false;
			if (!secondKey.equals(that.secondKey))
				return false;
			return secondKeyValue != null ? secondKeyValue.equals(that.secondKeyValue) : that.secondKeyValue == null;
		}
		
		@Override
		public int hashCode() {
			int result = supportedResource.hashCode();
			result = 31 * result + secondKey.hashCode();
			result = 31 * result + (secondKeyValue != null ? secondKeyValue.hashCode() : 0);
			return result;
		}
	}
}
