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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Auditable;
import org.openmrs.Retireable;
import org.openmrs.Voidable;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Converter;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription.Property;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.util.HandlerUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.openmrs.module.webservices.rest.web.representation.Representation.DEFAULT;
import static org.openmrs.module.webservices.rest.web.representation.Representation.FULL;
import static org.openmrs.module.webservices.rest.web.representation.Representation.REF;

@SuppressWarnings({ "java:S2143", "squid:S2143" })
public class ConversionUtil {
	
	private ConversionUtil() {
	}
	
	static final Log log = LogFactory.getLog(ConversionUtil.class);
	
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	

	
	private static ConcurrentMap<Class<?>, Converter> converterCache;
	
	private static final Converter nullConverter;
	
	static {
		converterCache = new ConcurrentHashMap<Class<?>, Converter>();
		nullConverter = new Converter() {
			
			@Override
			public Object newInstance(String type) {
				return null;
			}
			
			@Override
			public Object getByUniqueId(String string) {
				return null;
			}
			
			@Override
			public SimpleObject asRepresentation(Object instance, Representation rep) throws ConversionException {
				return new SimpleObject();
			}
			
			@Override
			public Object getProperty(Object instance, String propertyName) throws ConversionException {
				return null;
			}
			
			@Override
			public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
				// No implementation needed for null converter
			}
		};
	}
	
	public static void clearCache() {
		converterCache = new ConcurrentHashMap<Class<?>, Converter>();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Converter<T> getConverter(Class<T> clazz) {
		Converter<T> result = converterCache.get(clazz);
		if (result != null) {
			return result == nullConverter ? null : result;
		}
		
		try {
			result = getConverterFromRestService(clazz);
			
			if (result == null) {
				result = HandlerUtil.getPreferredHandler(Converter.class, clazz);
			}
		}
		catch (APIException ex) {
			result = null;
		}
		
		// At this point, we don't really care if a result was found or not, we cache it regardless so that repeated
		// searches are not performed.
		if (result == null) {
			converterCache.put(clazz, nullConverter);
		} else {
			converterCache.put(clazz, result);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Converter<T> getConverterFromRestService(Class<T> clazz) {
		try {
			Resource resource = Context.getService(RestService.class).getResourceBySupportedClass(clazz);
			if (resource instanceof Converter) {
				return (Converter<T>) resource;
			}
		}
		catch (APIException e) {
			// Ignore exception and return null
		}
		return null;
	}
	
	/**
	 * Converts the given object to the given type
	 * 
	 * @param object The value to convert
	 * @param toType The type to convert the value to
	 * @param instance The source object instance
	 * @return The specified object converted to the specified type
	 * <strong>Should</strong> resolve TypeVariables to actual type
	 */
	public static Object convert(Object object, Type toType, Object instance) throws ConversionException {
		if (instance != null && toType instanceof TypeVariable<?>) {
			TypeVariable<?> temp = ((TypeVariable<?>) toType);
			toType = getTypeVariableClass(instance.getClass(), temp);
		}
		
		return convert(object, toType);
	}
	
	/**
	 * Converts the given object to the given type
	 * 
	 * @param object
	 * @param toType a simple class or generic type
	 * @return
	 * @throws ConversionException
	 * <strong>Should</strong> convert strings to locales
	 * <strong>Should</strong> convert strings to enum values
	 * <strong>Should</strong> convert to an array
	 * <strong>Should</strong> convert to a class
	 */
	private static final List<TypeConverter> converters = new ArrayList<TypeConverter>();

	static {
		converters.add(new CollectionConverter());
		converters.add(new AssignableConverter());
		converters.add(new FloatCoercionConverter());
		converters.add(new StringConverter());
		converters.add(new MapConverter());
		converters.add(new NumberCoercionConverter());
		converters.add(new BooleanStringConverter());
	}

	public static Object convert(Object object, Type toType) throws ConversionException {
		if (object == null) {
			return null;
		}
		
		Class<?> toClass = toType instanceof Class ? ((Class<?>) toType) : (Class<?>) (((ParameterizedType) toType)
		        .getRawType());
		
		for (TypeConverter converter : converters) {
			if (converter.canConvert(object, toClass, toType)) {
				return converter.convert(object, toClass, toType);
			}
		}
		
		throw new ConversionException("Don't know how to convert from " + object.getClass() + " to " + toType, null);
	}
	
	/**
	 * Converts a map to the given type, using the registered converter
	 * 
	 * @param map the map (typically a SimpleObject submitted as json) to convert
	 * @param toClass the class to convert map to
	 * @return the result of using a converter to instantiate a new class and set map's properties
	 *         on it
	 * @throws ConversionException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object convertMap(Map<String, ?> map, Class<?> toClass) throws ConversionException {
		Converter converter = getConverter(toClass);
		if (converter == null) {
			throw new ConversionException("No converter found for class: " + toClass.getName(), null);
		}
		
		Object ret = null;
		Object uuid = map.get(RestConstants.PROPERTY_UUID);
		if (uuid instanceof String) {
			String uuidStr = uuid.toString();
			if (uuidStr.contains("/ws/rest/")) {
				uuidStr = uuidStr.substring(uuidStr.lastIndexOf('/') + 1);
			}
			ret = converter.getByUniqueId(uuidStr);
		}
		
		if (ret == null) {
			String type = (String) map.get(RestConstants.PROPERTY_FOR_TYPE);
			ret = converter.newInstance(type);
		}
		
		applyPropertiesToInstance(ret, converter, map);
		
		for (Map.Entry<String, ?> prop : map.entrySet()) {
			if (RestConstants.PROPERTY_FOR_TYPE.equals(prop.getKey()))
				continue;
			converter.setProperty(ret, prop.getKey(), prop.getValue());
		}
		return ret;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void applyPropertiesToInstance(Object ret, Converter converter, Map<String, ?> map) {
		if (converter instanceof DelegatingResourceHandler) {
			DelegatingResourceHandler handler = (DelegatingResourceHandler) converter;
			DelegatingResourceDescription resDesc = handler.getRepresentationDescription(new DefaultRepresentation());
			if (resDesc != null) {
				for (Map.Entry<String, Property> prop : resDesc.getProperties().entrySet()) {
					if (map.containsKey(prop.getKey()) && !RestConstants.PROPERTY_FOR_TYPE.equals(prop.getKey())) {
						converter.setProperty(ret, prop.getKey(), map.get(prop.getKey()));
					}
				}
			}
		}
	}
	
	/**
	 * Gets a property from the delegate, with the given representation
	 * 
	 * @param propertyName
	 * @param rep
	 * @return
	 * @throws ConversionException
	 */
	public static Object getPropertyWithRepresentation(Object bean, String propertyName, Representation rep)
	        throws ConversionException {
		Object o;
		try {
			o = PropertyUtils.getProperty(bean, propertyName);
		}
		catch (Exception ex) {
			throw new ConversionException(null, ex);
		}
		if (o instanceof Collection) {
			List<Object> ret = new ArrayList<Object>();
			for (Object element : (Collection<?>) o)
				ret.add(convertToRepresentation(element, rep));
			return ret;
		} else {
			o = convertToRepresentation(o, rep);
			return o;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <S> Object convertToRepresentation(S o, Representation rep) throws ConversionException {
		return convertToRepresentation(o, rep, (Converter) null);
	}
	
	@SuppressWarnings("unchecked")
	public static <S> Object convertToRepresentation(S o, Representation rep, Class<?> convertAs) throws ConversionException {
		Converter<?> converter = convertAs != null ? getConverter(convertAs) : null;
		return convertToRepresentation(o, rep, converter);
	}
	
	public static <S> Object convertToRepresentation(S o, Representation rep, Converter specificConverter)
	        throws ConversionException {
		if (o == null)
			return null;
		o = new HibernateLazyLoader().load(o);
		
		if (o instanceof Collection) {
			return convertCollectionToRepresentation((Collection) o, rep, specificConverter);
		} else if (o instanceof Map) {
			return convertMapToRepresentation((Map<?, ?>) o, rep);
		} else {
			return convertSingleObjectToRepresentation(o, rep, specificConverter);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static List convertCollectionToRepresentation(Collection<?> col, Representation rep, Converter specificConverter) {
		List ret = new ArrayList();
		for (Object item : col) {
			ret.add(convertToRepresentation(item, rep, specificConverter));
		}
		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object convertMapToRepresentation(Map<?, ?> map, Representation rep) {
		if (rep instanceof CustomRepresentation) {
			return convertToCustomRepresentation(map, (CustomRepresentation) rep);
		}
		SimpleObject ret = new SimpleObject();
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			ret.put(entry.getKey().toString(),
			    convertToRepresentation(entry.getValue(), Representation.REF, (Converter) null));
		}
		return ret;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <S> Object convertSingleObjectToRepresentation(S o, Representation rep, Converter specificConverter) {
		Converter<S> converter = specificConverter != null ? specificConverter : (Converter) getConverter(o.getClass());
		if (converter == null) {
			if (o instanceof Date) {
				return java.time.format.DateTimeFormatter.ofPattern(DATE_FORMAT)
					.withZone(java.time.ZoneId.systemDefault())
					.format(((Date) o).toInstant());
			}
			return o;
		}
		try {
			return converter.asRepresentation(o, rep);
		}
		catch (Exception ex) {
			throw new ConversionException("converting " + o.getClass() + " to " + rep, ex);
		}
	}
	
	/**
	 * Converts an object to its custom representation
	 * This could be used to convert any domain objects that does not have any specific converter associated with them
	 * such as SimpleObject's, Map's, etc
	 */
	private static SimpleObject convertToCustomRepresentation(Object o, CustomRepresentation rep) {
		DelegatingResourceDescription drd = ConversionUtil.getCustomRepresentationDescription(rep);
		
		SimpleObject result = new SimpleObject();
		for (String propertyName : drd.getProperties().keySet()) {
			DelegatingResourceDescription.Property property = drd.getProperties().get(propertyName);
			Object propertyValue = ConversionUtil.getPropertyWithRepresentation(o, propertyName, property.getRep());
			result.add(propertyName, propertyValue);
		}
		
		return result;
	}
	
	/**
	 * Gets the type for the specified generic type variable.
	 * 
	 * @param instanceClass An instance of the class with the specified generic type variable.
	 * @param typeVariable The generic type variable.
	 * @return The actual type of the generic type variable or {@code null} if not found.
	 * <strong>Should</strong> return the actual type if defined on the parent class
	 * <strong>Should</strong> return the actual type if defined on the grand-parent class
	 * <strong>Should</strong> return null when actual type cannot be found
	 * <strong>Should</strong> return the correct actual type if there are multiple generic types
	 * <strong>Should</strong> throw IllegalArgumentException when instance class is null
	 * <strong>Should</strong> throw IllegalArgumentException when typeVariable is null
	 */
	public static Type getTypeVariableClass(Class<?> instanceClass, TypeVariable<?> typeVariable) {
		return TypeVariableResolver.getTypeVariableClass(instanceClass, typeVariable);
	}
	
	/**
	 * Gets extra book-keeping info, for the full representation
	 * 
	 * @param delegate
	 * @return
	 */
	public static SimpleObject getAuditInfo(Object delegate) {
		SimpleObject ret = new SimpleObject();
		
		if (delegate instanceof Auditable) {
			Auditable auditable = (Auditable) delegate;
			ret.put("creator", getPropertyWithRepresentation(auditable, "creator", Representation.REF));
			ret.put("dateCreated", convertToRepresentation(auditable.getDateCreated(), Representation.DEFAULT));
			ret.put("changedBy", getPropertyWithRepresentation(auditable, "changedBy", Representation.REF));
			ret.put("dateChanged", convertToRepresentation(auditable.getDateChanged(), Representation.DEFAULT));
		}
		if (delegate instanceof Retireable) {
			Retireable retireable = (Retireable) delegate;
			if (Boolean.TRUE.equals(retireable.isRetired())) {
				ret.put("retiredBy", getPropertyWithRepresentation(retireable, "retiredBy", Representation.REF));
				ret.put("dateRetired", convertToRepresentation(retireable.getDateRetired(), Representation.DEFAULT));
				ret.put("retireReason", convertToRepresentation(retireable.getRetireReason(), Representation.DEFAULT));
			}
		}
		if (delegate instanceof Voidable) {
			Voidable voidable = (Voidable) delegate;
			if (Boolean.TRUE.equals(voidable.isVoided())) {
				ret.put("voidedBy", getPropertyWithRepresentation(voidable, "voidedBy", Representation.REF));
				ret.put("dateVoided", convertToRepresentation(voidable.getDateVoided(), Representation.DEFAULT));
				ret.put("voidReason", convertToRepresentation(voidable.getVoidReason(), Representation.DEFAULT));
			}
		}
		
		return ret;
	}

	/**
	 * <strong>Should</strong> return delegating resource description
	 */
	public static DelegatingResourceDescription getCustomRepresentationDescription(CustomRepresentation representation) {
		DelegatingResourceDescription desc = new DelegatingResourceDescription();
		if (representation == null || representation.getRepresentation() == null) {
			return desc;
		}

		String def = representation.getRepresentation();
		def = def.startsWith("(") ? def.substring(1) : def;
		def = def.endsWith(")") ? def.substring(0, def.length() - 1) : def;

		int startIndex = 0;
		List<String> properties = new ArrayList<String>();
		int nestingLevel = 0;
		for (int i=0; i < def.length(); i++) {
			char c = def.charAt(i);
			if (c == '(') {
				nestingLevel++;
			}
			else if (c == ')') {
				nestingLevel--;
			}
			else if (c == ',' && nestingLevel == 0) {
				properties.add(def.substring(startIndex, i));
				startIndex = i + 1;
			}
		}
		properties.add(def.substring(startIndex));

		for (String propertyDefinition : properties) {
			parsePropertyDefinition(desc, propertyDefinition);
		}

		return desc;
	}

	private static void parsePropertyDefinition(DelegatingResourceDescription desc, String propertyDefinition) {
		if (propertyDefinition.contains(":")) {
			String[] propertyAndRepresentation = propertyDefinition.split(":", 2);
			String property = propertyAndRepresentation[0];
			String rep = propertyAndRepresentation[1];
			Representation r;
			if (rep.startsWith("(")) {
				r = new CustomRepresentation(rep);
			}
			else {
				r = getRepresentationFromRepString(rep);
			}
			desc.addProperty(property, r);
		}
		else {
			if (propertyDefinition.equals("links")) {
				desc.addSelfLink();
				desc.addLink("default", ".?v=" + RestConstants.REPRESENTATION_DEFAULT);
			}
			else {
				desc.addProperty(propertyDefinition);
			}
		}
	}

	private static Representation getRepresentationFromRepString(String rep) {
		if (rep.equalsIgnoreCase("REF")) {
			return REF;
		}
		else if (rep.equalsIgnoreCase("FULL")) {
			return FULL;
		}
		return DEFAULT;
	}
}
