/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.parser.QueryParserUtil;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class QueryFormController extends SimpleFormController {

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request)
	{
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, QueryLanguage> queryLanguages = new TreeMap<String, QueryLanguage>();

		for (QueryParserFactory factory : QueryParserUtil.getQueryParserRegistry().getAll()) {
			QueryLanguage ql = factory.getQueryLanguage();
			// FIXME: webclient produces an error when the ql name is not upper cased. Why?
			queryLanguages.put(ql.getName().toUpperCase(), ql);
		}

		result.put("queryLanguages", queryLanguages);

		return result;
	}
}
