/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.binary;

/**
 * A type-safe enumeration for query error types.
 * 
 * @author Arjohn Kampman
 */
public enum QueryErrorType {

	/**
	 * Constant used for identifying a malformed query error.
	 */
	MALFORMED_QUERY_ERROR,

	/**
	 * Constant used for identifying a query evaluation error.
	 */
	QUERY_EVALUATION_ERROR;
}
