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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Universal abstract base class for Search Handlers containing generic helper methods
 * for parameter parsing, type conversion, and domain object resolution.
 */
public abstract class BaseSearchHandler implements SearchHandler {
	
	@Autowired
	@Qualifier("patientService")
	protected PatientService patientService;
	
	@Autowired
	@Qualifier("orderService")
	protected OrderService orderService;
	
	@Autowired
	@Qualifier("conceptService")
	protected ConceptService conceptService;
	
	/**
	 * Resolves a patient from a UUID string. Throws ObjectNotFoundException if not found.
	 */
	protected Patient getPatient(String patientUuid) throws ObjectNotFoundException {
		if (StringUtils.isNotBlank(patientUuid)) {
			Patient patient = patientService.getPatientByUuid(patientUuid);
			if (patient == null) {
				throw new ObjectNotFoundException();
			}
			return patient;
		}
		return null;
	}
	
	/**
	 * Resolves a care setting from a UUID string. Throws ObjectNotFoundException if not found.
	 */
	protected CareSetting getCareSetting(String careSettingUuid) throws ObjectNotFoundException {
		if (StringUtils.isNotBlank(careSettingUuid)) {
			CareSetting careSetting = orderService.getCareSettingByUuid(careSettingUuid);
			if (careSetting == null) {
				throw new ObjectNotFoundException();
			}
			return careSetting;
		}
		return null;
	}
	
	/**
	 * Resolves a comma-separated list of concept UUIDs into a List of Concepts.
	 * Throws ObjectNotFoundException if concept list is provided but no concepts are resolved.
	 */
	protected List<Concept> getConcepts(String conceptUuids) throws ObjectNotFoundException {
		if (StringUtils.isNotBlank(conceptUuids)) {
			List<Concept> concepts = new ArrayList<Concept>();
			for (String conceptUuid : conceptUuids.split(",")) {
				if (StringUtils.isNotBlank(conceptUuid)) {
					Concept concept = conceptService.getConceptByUuid(conceptUuid.trim());
					if (concept != null) {
						concepts.add(concept);
					}
				}
			}
			if (concepts.isEmpty()) {
				throw new ObjectNotFoundException();
			}
			return concepts;
		}
		return null;
	}
	
	/**
	 * Resolves a comma-separated list of order type UUIDs into a List of OrderTypes.
	 * Throws ObjectNotFoundException if orderType list is provided but no types are resolved.
	 */
	protected List<OrderType> getOrderTypes(String orderTypeUuids) throws ObjectNotFoundException {
		if (StringUtils.isNotBlank(orderTypeUuids)) {
			List<OrderType> orderTypes = new ArrayList<OrderType>();
			for (String orderTypeUuid : orderTypeUuids.split(",")) {
				if (StringUtils.isNotBlank(orderTypeUuid)) {
					OrderType orderType = orderService.getOrderTypeByUuid(orderTypeUuid.trim());
					if (orderType != null) {
						orderTypes.add(orderType);
					}
				}
			}
			if (orderTypes.isEmpty()) {
				throw new ObjectNotFoundException();
			}
			return orderTypes;
		}
		return null;
	}
	
	/**
	 * Resolves a concept source from a UUID string. Throws ObjectNotFoundException if not found.
	 */
	protected ConceptSource getConceptSource(String sourceUuid) throws ObjectNotFoundException {
		if (StringUtils.isNotBlank(sourceUuid)) {
			ConceptSource source = conceptService.getConceptSourceByUuid(sourceUuid);
			if (source == null) {
				throw new ObjectNotFoundException();
			}
			return source;
		}
		return null;
	}
	
	/**
	 * Resolves a comma-separated list of concept map type UUIDs into a List of ConceptMapTypes.
	 * Throws ObjectNotFoundException if list is provided but no types are resolved.
	 */
	protected List<ConceptMapType> getConceptMapTypes(String mapTypesUuids) throws ObjectNotFoundException {
		if (StringUtils.isNotBlank(mapTypesUuids)) {
			List<ConceptMapType> mapTypes = new ArrayList<ConceptMapType>();
			for (String uuid : mapTypesUuids.split(",")) {
				if (StringUtils.isNotBlank(uuid)) {
					ConceptMapType mapType = conceptService.getConceptMapTypeByUuid(uuid.trim());
					if (mapType == null) {
						throw new ObjectNotFoundException();
					}
					mapTypes.add(mapType);
				}
			}
			return mapTypes;
		}
		return null;
	}
	
	/**
	 * Converts a string-based ISO date representation into a Date object.
	 */
	protected Date parseDate(String dateStr) {
		return StringUtils.isNotBlank(dateStr) ? (Date) ConversionUtil.convert(dateStr, Date.class) : null;
	}
	
	/**
	 * Converts a string boolean representation into a boolean value.
	 */
	protected boolean parseBoolean(String boolStr) {
		return StringUtils.isNotBlank(boolStr) ? Boolean.parseBoolean(boolStr) : false;
	}
	
	/**
	 * Converts a string boolean representation into a nullable Boolean object.
	 */
	protected Boolean parseNullableBoolean(String boolStr) {
		return StringUtils.isNotBlank(boolStr) ? Boolean.valueOf(boolStr) : null;
	}
}
