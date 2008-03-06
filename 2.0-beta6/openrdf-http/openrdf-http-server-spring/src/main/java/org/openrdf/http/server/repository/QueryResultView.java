/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import org.springframework.web.servlet.View;

/**
 * Base class for rendering query results.
 * 
 * @author Herko ter Horst
 */
public abstract class QueryResultView implements View {

	/**
	 * Key by which the query result is stored in the model.
	 */
	public static final String QUERY_RESULT_KEY = "queryResult";

	/**
	 * Key by which a filename hint is stored in the model. The filename hint may
	 * be used to present the client with a suggestion for a filename to use for
	 * storing the result.
	 */
	public static final String FILENAME_HINT_KEY = "filenameHint";
}