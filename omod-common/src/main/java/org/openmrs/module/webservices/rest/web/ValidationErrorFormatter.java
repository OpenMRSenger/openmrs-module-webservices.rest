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
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.api.ValidationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.List;

public class ValidationErrorFormatter {
	
	private static final String PROPERTY_MESSAGE = "message";
	
	@SuppressWarnings("unchecked")
	public static SimpleObject wrapValidationErrorResponse(ValidationException ex) {
		MessageSourceService messageSourceService = Context.getMessageSourceService();
		
		SimpleObject errors = new SimpleObject();
		errors.add(PROPERTY_MESSAGE, messageSourceService.getMessage("webservices.rest.error.invalid.submission"));
		errors.add("code", "webservices.rest.error.invalid.submission");
		
		List<SimpleObject> globalErrors = new ArrayList<SimpleObject>();
		SimpleObject fieldErrors = new SimpleObject();
		
		if (ex.getErrors().hasGlobalErrors()) {
			for (Object errObj : ex.getErrors().getGlobalErrors()) {
				ObjectError err = (ObjectError) errObj;
				String message = messageSourceService.getMessage(err.getCode(), err.getArguments(), err.getDefaultMessage(), Context.getLocale());
				
				SimpleObject globalError = new SimpleObject();
				globalError.put("code", err.getCode());
				globalError.put(PROPERTY_MESSAGE, message);
				globalErrors.add(globalError);
			}
		}
		
		if (ex.getErrors().hasFieldErrors()) {
			for (Object errObj : ex.getErrors().getFieldErrors()) {
				FieldError err = (FieldError) errObj;
				String message = messageSourceService.getMessage(err.getCode(), err.getArguments(), err.getDefaultMessage(), Context.getLocale());
				
				SimpleObject fieldError = new SimpleObject();
				fieldError.put("code", err.getCode());
				fieldError.put(PROPERTY_MESSAGE, message);
				
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
}
