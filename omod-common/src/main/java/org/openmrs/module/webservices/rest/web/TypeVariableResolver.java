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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeVariableResolver {

	
	
	private TypeVariableResolver() {
	}

	private static final Map<String, Type> typeVariableMap = new ConcurrentHashMap<String, Type>();
	
	@SuppressWarnings("rawtypes")
	public static Type getTypeVariableClass(Class<?> instanceClass, TypeVariable<?> typeVariable) {
		if (instanceClass == null) {
			throw new IllegalArgumentException("The instance class is required.");
		}
		if (typeVariable == null) {
			throw new IllegalArgumentException("The type variable is required.");
		}
		
		String genericTypeName = typeVariable.getName();
		Type type = instanceClass;
		
		Type result = typeVariableMap.get(instanceClass.getName().concat(genericTypeName));
		
		while (result == null && type != null && !type.equals(Object.class)) {
			if (type instanceof Class) {
				type = ((Class) type).getGenericSuperclass();
			} else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class rawType = (Class) parameterizedType.getRawType();
				
				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					String name = typeParameters[i].getName();
					Type actualType = actualTypeArguments[i];
					
					typeVariableMap.put(instanceClass.getName().concat(name), actualType);
					
					if (name.equals(genericTypeName)) {
						result = actualType;
						break;
					}
				}
				
				type = rawType.getGenericSuperclass();
			}
		}
		
		return result;
	}
}
