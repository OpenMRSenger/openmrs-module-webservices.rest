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

import org.openmrs.module.webservices.rest.web.response.ConversionException;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class CollectionConverter implements TypeConverter {
	
	@Override
	public boolean canConvert(Object source, Class<?> toClass, Type toType) {
		return Collection.class.isAssignableFrom(toClass) || toClass.isArray();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object convert(Object source, Class<?> toClass, Type toType) throws ConversionException {
		if (!(source instanceof Collection)) {
			throw new ConversionException("Can only convert a Collection to a Collection/Array. Not "
			        + source.getClass() + " to " + toType, null);
		}
		Collection input = (Collection) source;
		if (toClass.isArray()) {
			Class<?> targetElementType = toClass.getComponentType();
			Object ret = Array.newInstance(targetElementType, input.size());
			int i = 0;
			for (Object element : input) {
				Array.set(ret, i, ConversionUtil.convert(element, targetElementType));
				++i;
			}
			return ret;
		}
		
		Collection ret;
		if (SortedSet.class.isAssignableFrom(toClass)) {
			ret = new TreeSet();
		} else if (Set.class.isAssignableFrom(toClass)) {
			ret = new HashSet();
		} else if (List.class.isAssignableFrom(toClass)) {
			ret = new ArrayList();
		} else {
			throw new ConversionException("Don't know how to handle collection class: " + toClass, null);
		}
		
		if (toType instanceof ParameterizedType) {
			ParameterizedType toParameterizedType = (ParameterizedType) toType;
			Type targetElementType = toParameterizedType.getActualTypeArguments()[0];
			for (Object element : input) {
				ret.add(ConversionUtil.convert(element, targetElementType));
			}
		} else {
			ret.addAll(input);
		}
		return ret;
	}
}
