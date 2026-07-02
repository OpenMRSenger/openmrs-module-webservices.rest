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

import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.util.LocaleUtility;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Locale;

public class StringConverter implements TypeConverter {
	
	@Override
	public boolean canConvert(Object source, Class<?> toClass, Type toType) {
		return source instanceof String;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object convert(Object source, Class<?> toClass, Type toType) throws ConversionException {
		String string = (String) source;
		Converter<?> converter = ConversionUtil.getConverter(toClass);
		if (converter != null) {
			return converter.getByUniqueId(string);
		}
		
		if (toClass.isAssignableFrom(Date.class)) {
			return convertToDate(string);
		} else if (toClass.isAssignableFrom(Locale.class)) {
			return LocaleUtility.fromSpecification(string);
		} else if (toClass.isEnum()) {
			return Enum.valueOf((Class<? extends Enum>) toClass, string.toUpperCase());
		} else if (toClass.isAssignableFrom(Class.class)) {
			return convertToClass(string, toType);
		}
		
		return convertUsingValueOf(string, toClass, toType);
	}

	private Date convertToDate(String string) throws ConversionException {
		Exception pex = null;
		String[] supportedFormats = {
			"yyyy-MM-dd'T'HH:mm:ss.SSSZ",
			"yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
			"yyyy-MM-dd'T'HH:mm:ss.SSSXX",
			"yyyy-MM-dd'T'HH:mm:ss.SSSx",
			"yyyy-MM-dd'T'HH:mm:ss.SSSX",
			"yyyy-MM-dd'T'HH:mm:ss.SSS",
			"yyyy-MM-dd'T'HH:mm:ssZ",
			"yyyy-MM-dd'T'HH:mm:ssXXX",
			"yyyy-MM-dd'T'HH:mm:ssXX",
			"yyyy-MM-dd'T'HH:mm:ssx",
			"yyyy-MM-dd'T'HH:mm:ssX",
			"yyyy-MM-dd'T'HH:mm:ss",
			"yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd"
		};
		for (String format : supportedFormats) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
				TemporalAccessor accessor = formatter.parseBest(string,
					ZonedDateTime::from,
					OffsetDateTime::from,
					LocalDateTime::from,
					LocalDate::from
				);
				
				Instant instant;
				if (accessor instanceof ZonedDateTime) {
					instant = ((ZonedDateTime) accessor).toInstant();
				} else if (accessor instanceof OffsetDateTime) {
					instant = ((OffsetDateTime) accessor).toInstant();
				} else if (accessor instanceof LocalDateTime) {
					instant = ((LocalDateTime) accessor).atZone(ZoneId.systemDefault()).toInstant();
				} else {
					instant = ((LocalDate) accessor).atStartOfDay(ZoneId.systemDefault()).toInstant();
				}
				return Date.from(instant);
			}
			catch (Exception ex) {
				pex = ex;
			}
		}
		throw new ConversionException(
		        "Error converting date - correct format (ISO8601 Long): yyyy-MM-dd'T'HH:mm:ss.SSSZ", pex);
	}

	private Class<?> convertToClass(String string, Type toType) throws ConversionException {
		try {
			return Context.loadClass(string);
		}
		catch (ClassNotFoundException e) {
			throw new ConversionException("Could not convert from String to " + toType, e);
		}
	}

	private Object convertUsingValueOf(String string, Class<?> toClass, Type toType) throws ConversionException {
		try {
			Method method = toClass.getMethod("valueOf", String.class);
			if (Modifier.isStatic(method.getModifiers()) && toClass.isAssignableFrom(method.getReturnType())) {
				return method.invoke(null, string);
			}
		}
		catch (Exception ex) {}
		
		throw new ConversionException("Don't know how to convert from String to " + toType, null);
	}
}
