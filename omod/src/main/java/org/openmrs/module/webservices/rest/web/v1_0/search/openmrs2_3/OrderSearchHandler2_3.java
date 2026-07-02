/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs2_3;

import org.apache.commons.lang.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.search.BaseOrderSearchHandler;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.parameter.OrderSearchCriteriaBuilder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class OrderSearchHandler2_3 extends BaseOrderSearchHandler {

	public static final String REQUEST_PARAM_PATIENT = "patient";

	public static final String REQUEST_PARAM_CARE_SETTING = "careSetting";

	public static final String REQUEST_PARAM_CONCEPTS = "concepts";

	public static final String REQUEST_PARAM_ORDER_TYPES = "orderTypes";

	public static final String REQUEST_PARAM_ORDER_NUMBER = "orderNumber";

	public static final String REQUEST_PARAM_ACCESSION_NUMBER = "accessionNumber";

	public static final String REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE = "activatedOnOrBeforeDate";

	public static final String REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE = "activatedOnOrAfterDate";

	public static final String REQUEST_PARAM_IS_STOPPED = "isStopped";

	public static final String REQUEST_PARAM_AUTO_EXPIRE_ON_OR_BEFORE_DATE = "autoExpireOnOrBeforeDate";

	public static final String REQUEST_PARAM_CANCELED_OR_AUTO_EXPIRE_ON_OR_BEFORE_DATE = "canceledOrExpiredOnOrBeforeDate";

	public static final String REQUEST_PARAM_ACTION = "action";

	public static final String REQUEST_PARAM_FULFILLER_STATUS = "fulfillerStatus";

	public static final String REQUEST_PARAM_INCLUDE_NULL_FULFILLER_STATUS = "includeNullFulfillerStatus";

	public static final String REQUEST_PARAM_EXCLUDE_CANCELED_AND_EXPIRED = "excludeCanceledAndExpired";

	public static final String REQUEST_PARAM_EXCLUDE_DISCONTINUE_ORDERS = "excludeDiscontinueOrders";

	public static final String REQUEST_PARAM_INCLUDE_VOIDED = "includeVoided";

	SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for orders, it matches on "
	        + "patient, care setting, concepts (comma delimited), order types (comma delimited), "
	        + "date activated (before or after), fulfiller status, action, canceled or expired, stopped, voided flag")
	        .withOptionalParameters(REQUEST_PARAM_PATIENT,
	            REQUEST_PARAM_CARE_SETTING,
	            REQUEST_PARAM_CONCEPTS,
	            REQUEST_PARAM_ORDER_TYPES,
	            REQUEST_PARAM_ORDER_NUMBER,
	            REQUEST_PARAM_ACCESSION_NUMBER,
	            REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE,
	            REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE,
	            REQUEST_PARAM_IS_STOPPED,
	            REQUEST_PARAM_AUTO_EXPIRE_ON_OR_BEFORE_DATE,
	            REQUEST_PARAM_CANCELED_OR_AUTO_EXPIRE_ON_OR_BEFORE_DATE,
	            REQUEST_PARAM_ACTION,
	            REQUEST_PARAM_FULFILLER_STATUS,
	            REQUEST_PARAM_INCLUDE_NULL_FULFILLER_STATUS,
	            REQUEST_PARAM_EXCLUDE_CANCELED_AND_EXPIRED,
	            REQUEST_PARAM_EXCLUDE_DISCONTINUE_ORDERS,
	            REQUEST_PARAM_INCLUDE_VOIDED).build();

	private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1
	        + "/order", Collections.singletonList("2.3.* - 9.*"), searchQuery);

	/**
	 * @see SearchHandler#getSearchConfig()
	 */
	@Override
	public SearchConfig getSearchConfig() {
		return searchConfig;
	}

	/**
	 * @see SearchHandler#search(RequestContext)
	 */
	@Override
	public PageableResult search(RequestContext context) throws ResponseException {
		// get input parameters
		String orderNumber = context.getParameter(REQUEST_PARAM_ORDER_NUMBER);
		String accessionNumber = context.getParameter(REQUEST_PARAM_ACCESSION_NUMBER);
		String actionStr = context.getParameter(REQUEST_PARAM_ACTION);
		String fulfillerStatusStr = context.getParameter(REQUEST_PARAM_FULFILLER_STATUS);

		// parse parameters using BaseOrderSearchHandler helper methods
		Patient patient = getPatient(context.getParameter(REQUEST_PARAM_PATIENT));
		CareSetting careSetting = getCareSetting(context.getParameter(REQUEST_PARAM_CARE_SETTING));
		List<Concept> concepts = getConcepts(context.getParameter(REQUEST_PARAM_CONCEPTS));
		List<OrderType> orderTypes = getOrderTypes(context.getParameter(REQUEST_PARAM_ORDER_TYPES));
		
		Date activatedOnOrBeforeDate = parseDate(context.getParameter(REQUEST_PARAM_ACTIVATED_ON_OR_BEFORE_DATE));
		Date activatedOnOrAfterDate = parseDate(context.getParameter(REQUEST_PARAM_ACTIVATED_ON_OR_AFTER_DATE));
		Date autoExpireOnOrBeforeDate = parseDate(context.getParameter(REQUEST_PARAM_AUTO_EXPIRE_ON_OR_BEFORE_DATE));
		Date canceledOrExpiredOnOrBeforeDate = parseDate(context.getParameter(REQUEST_PARAM_CANCELED_OR_AUTO_EXPIRE_ON_OR_BEFORE_DATE));
		
		boolean includeVoided = parseBoolean(context.getParameter(REQUEST_PARAM_INCLUDE_VOIDED));
		boolean excludeDiscontinueOrders = parseBoolean(context.getParameter(REQUEST_PARAM_EXCLUDE_DISCONTINUE_ORDERS));
		boolean excludeCanceledAndExpired = parseBoolean(context.getParameter(REQUEST_PARAM_EXCLUDE_CANCELED_AND_EXPIRED));
		boolean isStopped = parseBoolean(context.getParameter(REQUEST_PARAM_IS_STOPPED));
		
		Boolean includeNullFulfillerStatus = parseNullableBoolean(context.getParameter(REQUEST_PARAM_INCLUDE_NULL_FULFILLER_STATUS));

		Order.Action action = StringUtils.isNotBlank(actionStr) ? Order.Action.valueOf(actionStr) : null;
		Order.FulfillerStatus fulfillerStatus = StringUtils.isNotBlank(fulfillerStatusStr) ? Order.FulfillerStatus.valueOf(fulfillerStatusStr) : null;

		OrderSearchCriteriaBuilder builder = new OrderSearchCriteriaBuilder();
		OrderSearchCriteria orderSearchCriteria = builder
		        .setPatient(patient)
		        .setCareSetting(careSetting)
		        .setConcepts(concepts)
		        .setOrderTypes(orderTypes)
                .setOrderNumber(StringUtils.isNotEmpty(orderNumber) ? orderNumber : null)
                .setAccessionNumber(StringUtils.isNotEmpty(accessionNumber) ? accessionNumber : null)
		        .setActivatedOnOrBeforeDate(activatedOnOrBeforeDate)
		        .setActivatedOnOrAfterDate(activatedOnOrAfterDate)
		        .setIsStopped(isStopped)
		        .setAutoExpireOnOrBeforeDate(autoExpireOnOrBeforeDate)
		        .setCanceledOrExpiredOnOrBeforeDate(canceledOrExpiredOnOrBeforeDate)
		        .setAction(action)
		        .setFulfillerStatus(fulfillerStatus)
		        .setIncludeNullFulfillerStatus(includeNullFulfillerStatus)
		        .setExcludeDiscontinueOrders(excludeDiscontinueOrders)
		        .setExcludeCanceledAndExpired(excludeCanceledAndExpired)
		        .setIncludeVoided(includeVoided)
		        .build();

		// invoke order service and return results
		List<Order> orders = orderService.getOrders(orderSearchCriteria);

		return new NeedsPaging<Order>(orders, context);
	}

}
