/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription.Property;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.util.OpenmrsUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class ResourcePropertyBinder {
	
	@SuppressWarnings("unchecked")
	public static <T> void setConvertedProperties(BaseDelegatingResource<T> resource, T delegate, Map<String, Object> propertyMap,
	        DelegatingResourceDescription description, boolean mustIncludeRequiredProperties) throws ConversionException {
		
		Map<String, Property> allowedProperties = new LinkedHashMap<String, Property>(description.getProperties());
		Map<String, Object> propertiesToSet = new HashMap<String, Object>(propertyMap);
		propertiesToSet.keySet().removeAll(resource.propertiesIgnoredWhenUpdating);
		
		for (String property : allowedProperties.keySet()) {
			if (propertiesToSet.containsKey(property)) {
				Object oldValue = resource.getProperty(delegate, property);
				Object newValue = propertiesToSet.get(property);
				if (unchangedValue(oldValue, newValue)) {
					propertiesToSet.remove(property);
					continue;
				}
				resource.setProperty(delegate, property, propertiesToSet.get(property));
			}
		}
		
		Collection<String> notAllowedProperties = CollectionUtils.subtract(propertiesToSet.keySet(), allowedProperties.keySet());
		for (Iterator<String> iterator = notAllowedProperties.iterator(); iterator.hasNext();) {
			String property = iterator.next();
			Object oldValue = resource.getProperty(delegate, property);
			Object newValue = propertiesToSet.get(property);
			if (unchangedValue(oldValue, newValue)) {
				iterator.remove();
			}
		}
		if (!notAllowedProperties.isEmpty()) {
			throw new ConversionException("Some properties are not allowed to be set: "
			        + StringUtils.join(notAllowedProperties, ", "));
		}
		
		if (mustIncludeRequiredProperties) {
			Set<String> missingProperties = new HashSet<String>();
			for (Map.Entry<String, Property> prop : allowedProperties.entrySet()) {
				if (prop.getValue().isRequired() && !propertyMap.containsKey(prop.getKey())) {
					missingProperties.add(prop.getKey());
				}
			}
			if (!missingProperties.isEmpty()) {
				throw new ConversionException("Some required properties are missing: "
				        + StringUtils.join(missingProperties, ", "));
			}
		}
	}
	
	public static boolean unchangedValue(Object oldValue, Object newValue) {
		if (newValue instanceof Map && oldValue != null && !(oldValue instanceof Map)) {
			newValue = ConversionUtil.convert(newValue, oldValue.getClass());
			if (oldValue instanceof OpenmrsObject) {
				return ((OpenmrsObject) oldValue).getUuid().equals(((OpenmrsObject) newValue).getUuid());
			}
		}
		return OpenmrsUtil.nullSafeEquals(oldValue, newValue);
	}
}
