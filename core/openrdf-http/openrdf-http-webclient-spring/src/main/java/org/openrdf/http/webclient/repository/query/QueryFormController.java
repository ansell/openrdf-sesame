/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.QueryParserFactory;
import org.openrdf.query.parser.QueryParserRegistry;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class QueryFormController extends SimpleFormController {

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, QueryLanguage> queryLanguages = new TreeMap<String, QueryLanguage>();

		for (QueryParserFactory factory : QueryParserRegistry.getInstance().getAll()) {
			QueryLanguage ql = factory.getQueryLanguage();
			// FIXME: webclient produces an error when the ql name is not upper
			// cased. Why?
			queryLanguages.put(ql.getName().toUpperCase(), ql);
		}

		result.put("queryLanguages", queryLanguages);

		return result;
	}

	/**
	 * @param errors
	 * @param lineNumber
	 * @param columnNumber
	 * @param encounteredToken
	 * @param expectedTokens
	 */
	protected Object[] getMalformedQueryMessageArguments(long lineNumber, long columnNumber,
			String encounteredToken, Set<String> expectedTokens)
	{
		Object[] result = new Object[4];

		result[0] = lineNumber;
		result[1] = columnNumber;
		result[2] = encounteredToken;

		if (expectedTokens.size() > 0) {
			StringBuffer expectedTokensMessage = new StringBuffer();

			Iterator<String> expectedIter = expectedTokens.iterator();
			while (expectedIter.hasNext()) {
				String token = expectedIter.next();
				String code = "repository.query.error.malformed.parsed.expected";
				if (!expectedIter.hasNext()) {
					code = "repository.query.error.malformed.parsed.expected.last";
				}
				expectedTokensMessage.append(getMessageSourceAccessor().getMessage(code, new Object[] { token }));
			}

			result[3] = expectedTokensMessage.toString();
		}
		else {
			result[3] = getMessageSourceAccessor().getMessage(
					"repository.query.error.malformed.parsed.expected.other");
		}

		return result;
	}
}
