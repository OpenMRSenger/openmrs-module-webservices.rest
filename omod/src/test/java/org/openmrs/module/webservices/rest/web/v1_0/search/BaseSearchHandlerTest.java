/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;

import java.util.Date;
import java.util.List;

public class BaseSearchHandlerTest extends RestControllerTestUtils {
	
	private TestSearchHandler handler;
	
	private static class TestSearchHandler extends BaseSearchHandler {
		@Override
		public SearchConfig getSearchConfig() {
			return null;
		}
		
		@Override
		public PageableResult search(RequestContext context) {
			return null;
		}
	}
	
	@Before
	public void setup() {
		handler = new TestSearchHandler();
		handler.patientService = org.openmrs.api.context.Context.getPatientService();
		handler.orderService = org.openmrs.api.context.Context.getOrderService();
		handler.conceptService = org.openmrs.api.context.Context.getConceptService();
	}
	
	@Test
	public void getPatient_shouldReturnPatientForValidUuid() throws Exception {
		Patient patient = handler.getPatient(org.openmrs.module.webservices.rest.web.RestTestConstants1_8.PATIENT_UUID);
		Assert.assertNotNull(patient);
		Assert.assertEquals(org.openmrs.module.webservices.rest.web.RestTestConstants1_8.PATIENT_UUID, patient.getUuid());
	}
	
	@Test(expected = ObjectNotFoundException.class)
	public void getPatient_shouldThrowExceptionForInvalidUuid() throws Exception {
		handler.getPatient("INVALID_UUID");
	}
	
	@Test
	public void getPatient_shouldReturnNullForBlankUuid() throws Exception {
		Assert.assertNull(handler.getPatient(""));
		Assert.assertNull(handler.getPatient(null));
	}
	
	@Test
	public void getCareSetting_shouldReturnCareSettingForValidUuid() throws Exception {
		CareSetting careSetting = handler.getCareSetting("6f0c9a92-6f24-11e3-af88-005056821db0");
		Assert.assertNotNull(careSetting);
		Assert.assertEquals("6f0c9a92-6f24-11e3-af88-005056821db0", careSetting.getUuid());
	}
	
	@Test(expected = ObjectNotFoundException.class)
	public void getCareSetting_shouldThrowExceptionForInvalidUuid() throws Exception {
		handler.getCareSetting("INVALID_UUID");
	}
	
	@Test
	public void getCareSetting_shouldReturnNullForBlankUuid() throws Exception {
		Assert.assertNull(handler.getCareSetting(""));
		Assert.assertNull(handler.getCareSetting(null));
	}
	
	@Test
	public void getConcepts_shouldReturnConceptListForValidUuids() throws Exception {
		List<Concept> concepts = handler.getConcepts(org.openmrs.module.webservices.rest.web.RestTestConstants1_10.ASPIRIN_UUID);
		Assert.assertNotNull(concepts);
	}
	
	@Test(expected = ObjectNotFoundException.class)
	public void getConcepts_shouldThrowExceptionIfNoConceptsFound() throws Exception {
		handler.getConcepts("INVALID_UUID1,INVALID_UUID2");
	}
	
	@Test
	public void getConcepts_shouldReturnNullForBlankUuids() throws Exception {
		Assert.assertNull(handler.getConcepts(""));
		Assert.assertNull(handler.getConcepts(null));
	}
	
	@Test
	public void getOrderTypes_shouldReturnOrderTypeListForValidUuids() throws Exception {
		List<OrderType> orderTypes = handler.getOrderTypes("131168f4-15f5-102d-96e4-000c29c2a5d7");
		Assert.assertNotNull(orderTypes);
		Assert.assertEquals(1, orderTypes.size());
	}
	
	@Test(expected = ObjectNotFoundException.class)
	public void getOrderTypes_shouldThrowExceptionIfNoOrderTypesFound() throws Exception {
		handler.getOrderTypes("INVALID_UUID1,INVALID_UUID2");
	}
	
	@Test
	public void getOrderTypes_shouldReturnNullForBlankUuids() throws Exception {
		Assert.assertNull(handler.getOrderTypes(""));
		Assert.assertNull(handler.getOrderTypes(null));
	}
	
	@Test
	public void getConceptSource_shouldReturnConceptSourceForValidUuid() throws Exception {
		org.openmrs.ConceptSource source = org.openmrs.api.context.Context.getConceptService().getAllConceptSources(true).get(0);
		Assert.assertNotNull(handler.getConceptSource(source.getUuid()));
	}
	
	@Test(expected = ObjectNotFoundException.class)
	public void getConceptSource_shouldThrowExceptionForInvalidUuid() throws Exception {
		handler.getConceptSource("INVALID_UUID");
	}
	
	@Test
	public void getConceptSource_shouldReturnNullForBlankUuid() throws Exception {
		Assert.assertNull(handler.getConceptSource(""));
		Assert.assertNull(handler.getConceptSource(null));
	}
	
	@Test
	public void getConceptMapTypes_shouldReturnConceptMapTypeListForValidUuids() throws Exception {
		org.openmrs.ConceptMapType mapType = org.openmrs.api.context.Context.getConceptService().getConceptMapTypes(true, true).get(0);
		List<org.openmrs.ConceptMapType> mapTypes = handler.getConceptMapTypes(mapType.getUuid());
		Assert.assertNotNull(mapTypes);
		Assert.assertEquals(1, mapTypes.size());
	}
	
	@Test(expected = ObjectNotFoundException.class)
	public void getConceptMapTypes_shouldThrowExceptionIfNoConceptMapTypesFound() throws Exception {
		handler.getConceptMapTypes("INVALID_UUID1,INVALID_UUID2");
	}
	
	@Test
	public void getConceptMapTypes_shouldReturnNullForBlankUuids() throws Exception {
		Assert.assertNull(handler.getConceptMapTypes(""));
		Assert.assertNull(handler.getConceptMapTypes(null));
	}
	
	@Test
	public void parseDate_shouldParseValidDate() {
		Date date = handler.parseDate("2026-07-02");
		Assert.assertNotNull(date);
	}
	
	@Test
	public void parseDate_shouldReturnNullForBlank() {
		Assert.assertNull(handler.parseDate(""));
		Assert.assertNull(handler.parseDate(null));
	}
	
	@Test
	public void parseBoolean_shouldParseBoolean() {
		Assert.assertTrue(handler.parseBoolean("true"));
		Assert.assertFalse(handler.parseBoolean("false"));
		Assert.assertFalse(handler.parseBoolean(""));
	}
	
	@Test
	public void parseNullableBoolean_shouldParseNullableBoolean() {
		Assert.assertEquals(Boolean.TRUE, handler.parseNullableBoolean("true"));
		Assert.assertEquals(Boolean.FALSE, handler.parseNullableBoolean("false"));
		Assert.assertNull(handler.parseNullableBoolean(""));
		Assert.assertNull(handler.parseNullableBoolean(null));
	}
}
