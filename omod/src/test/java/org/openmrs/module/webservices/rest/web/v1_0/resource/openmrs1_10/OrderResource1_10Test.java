/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.junit.Before;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class OrderResource1_10Test extends BaseDelegatingResourceTest<OrderResource1_10, Order> {
	
	protected static final String ORDER_ENTRY_DATASET_XML = "org/openmrs/api/include/OrderEntryIntegrationTest-other.xml";
	
	@Before
	public void before() throws Exception {
		executeDataSet(ORDER_ENTRY_DATASET_XML);
	}
	
	/**
	 * @see BaseDelegatingResourceTest#validateDefaultRepresentation()
	 */
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("orderNumber");
		assertPropPresent("accessionNumber");
		assertPropPresent("patient");
		assertPropPresent("concept");
		assertPropPresent("action");
		assertPropPresent("careSetting");
		assertPropPresent("previousOrder");
		assertPropPresent("dateActivated");
		assertPropPresent("scheduledDate");
		assertPropPresent("dateStopped");
		assertPropPresent("autoExpireDate");
		assertPropPresent("encounter");
		assertPropPresent("orderer");
		assertPropPresent("orderReason");
		assertPropPresent("orderReasonNonCoded");
		assertPropPresent("orderType");
		assertPropPresent("urgency");
		assertPropPresent("instructions");
		assertPropPresent("commentToFulfiller");
		assertPropPresent("display");
	}
	
	/**
	 * @see BaseDelegatingResourceTest#validateFullRepresentation()
	 */
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("orderNumber");
		assertPropPresent("accessionNumber");
		assertPropPresent("patient");
		assertPropPresent("concept");
		assertPropPresent("action");
		assertPropPresent("careSetting");
		assertPropPresent("previousOrder");
		assertPropPresent("dateActivated");
		assertPropPresent("scheduledDate");
		assertPropPresent("dateStopped");
		assertPropPresent("autoExpireDate");
		assertPropPresent("encounter");
		assertPropPresent("orderer");
		assertPropPresent("orderReason");
		assertPropPresent("orderReasonNonCoded");
		assertPropPresent("orderType");
		assertPropPresent("urgency");
		assertPropPresent("instructions");
		assertPropPresent("commentToFulfiller");
		assertPropPresent("display");
		assertPropPresent("auditInfo");
	}
	
	@Override
	public Order newObject() {
		return Context.getOrderService().getOrderByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "CD4 COUNT";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_10.ORDER_UUID;
	}
	
	@org.junit.Test
	public void getOrders_shouldGetOrdersForPatient() throws Exception {
		org.openmrs.Patient patient = Context.getPatientService().getPatientByUuid("5946f880-b197-400b-9caa-a3c661d23041");
		org.openmrs.module.webservices.rest.web.RequestContext context = new org.openmrs.module.webservices.rest.web.RequestContext();
		org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
		request.setParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		request.setParameter("status", "any");
		request.setParameter("careSetting", "6f0c9a92-6f24-11e3-af88-005056821db0");
		context.setRequest(request);
		
		org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order> result = (org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order>) getResource().getOrders(patient, null, context);
		org.junit.Assert.assertNotNull(result);
		org.junit.Assert.assertEquals(2, result.getPageOfResults().size());
	}
	
	@org.junit.Test
	public void getOrders_shouldFilterByOrderType() throws Exception {
		org.openmrs.Patient patient = Context.getPatientService().getPatientByUuid("5946f880-b197-400b-9caa-a3c661d23041");
		org.openmrs.OrderType orderType = Context.getOrderService().getOrderTypeByUuid("131168f4-15f5-102d-96e4-000c29c2a5d7"); // Drug order type
		org.openmrs.module.webservices.rest.web.RequestContext context = new org.openmrs.module.webservices.rest.web.RequestContext();
		org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
		request.setParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		request.setParameter("status", "any");
		request.setParameter("careSetting", "6f0c9a92-6f24-11e3-af88-005056821db0");
		context.setRequest(request);
		
		org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order> result = (org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order>) getResource().getOrders(patient, orderType, context);
		org.junit.Assert.assertNotNull(result);
		org.junit.Assert.assertEquals(2, result.getPageOfResults().size());
	}
	
	@org.junit.Test
	public void getOrders_shouldSortOrders() throws Exception {
		org.openmrs.Patient patient = Context.getPatientService().getPatientByUuid("5946f880-b197-400b-9caa-a3c661d23041");
		org.openmrs.module.webservices.rest.web.RequestContext context = new org.openmrs.module.webservices.rest.web.RequestContext();
		org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
		request.setParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		request.setParameter("sort", "ASC");
		request.setParameter("status", "any");
		request.setParameter("careSetting", "6f0c9a92-6f24-11e3-af88-005056821db0");
		context.setRequest(request);
		
		org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order> result = (org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order>) getResource().getOrders(patient, null, context);
		org.junit.Assert.assertNotNull(result);
		org.junit.Assert.assertEquals(2, result.getPageOfResults().size());
	}
	
	@org.junit.Test
	public void doSearch_shouldDelegateToDrugOrderSubclassHandler() throws Exception {
		org.openmrs.module.webservices.rest.web.RequestContext context = new org.openmrs.module.webservices.rest.web.RequestContext();
		org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
		request.setParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		request.setParameter("status", "any");
		request.setParameter("careSetting", "6f0c9a92-6f24-11e3-af88-005056821db0");
		context.setRequest(request);
		context.setType("drugorder");
		
		org.openmrs.module.webservices.rest.web.resource.api.PageableResult result = getResource().doSearch(context);
		org.junit.Assert.assertNotNull(result);
		org.junit.Assert.assertTrue(result instanceof org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging);
		org.junit.Assert.assertEquals(2, ((org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order>) result).getPageOfResults().size());
	}
	
	@org.junit.Test
	public void doSearch_shouldDelegateToTestOrderSubclassHandler() throws Exception {
		org.openmrs.module.webservices.rest.web.RequestContext context = new org.openmrs.module.webservices.rest.web.RequestContext();
		org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest();
		request.setParameter("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		request.setParameter("status", "any");
		request.setParameter("careSetting", "6f0c9a92-6f24-11e3-af88-005056821db0");
		context.setRequest(request);
		context.setType("testorder");
		
		org.openmrs.module.webservices.rest.web.resource.api.PageableResult result = getResource().doSearch(context);
		org.junit.Assert.assertNotNull(result);
		org.junit.Assert.assertTrue(result instanceof org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging);
		// Let's assert that it successfully ran and returned the pageable result (even if size is 0 for test orders)
		org.junit.Assert.assertNotNull(((org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging<Order>) result).getPageOfResults());
	}
}


