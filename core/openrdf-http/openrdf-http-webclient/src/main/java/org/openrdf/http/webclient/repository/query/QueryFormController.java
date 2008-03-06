/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.query.QueryLanguage;

/**
 * @author Herko ter Horst
 */
public class QueryFormController extends SimpleFormController {

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request)
	{
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, QueryLanguage> queryLanguages = new TreeMap<String, QueryLanguage>();
		for (QueryLanguage ql : QueryLanguage.values()) {
			queryLanguages.put(ql.name(), ql);
		}

		result.put("queryLanguages", queryLanguages);

		return result;
	}
}
