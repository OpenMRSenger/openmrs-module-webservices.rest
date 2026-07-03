/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.search.openmrs1_10;

import java.util.Collections;
import java.util.List;

import org.openmrs.Drug;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.search.BaseSearchHandler;
import org.springframework.stereotype.Component;

/**
 * Allows finding drugs by mapping
 */
@Component
public class DrugsSearchByMappingHandler1_10 extends BaseSearchHandler {
	
	SearchQuery searchQuery = buildDrugMappingSearchQuery(
	        "Allows you to find drugs by source, code and preferred map types(comma delimited). "
	                + "Gets the best matching drug, i.e. matching the earliest ConceptMapType passed if there are "
	                + "multiple matches for the highest-priority ConceptMapType");
	
	private final SearchConfig searchConfig = new SearchConfig("getDrugsByMapping", RestConstants.VERSION_1 + "/drug",
			Collections.singletonList("1.10.* - 9.*"), searchQuery);
	
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
		DrugMappingSearchCriteria criteria = parseDrugMappingCriteria(context);
		
		List<Drug> drugs = conceptService.getDrugsByMapping(criteria.code, criteria.source, criteria.mapTypes,
		    context.getIncludeAll());
		return new NeedsPaging<Drug>(drugs, context);
	}
}
