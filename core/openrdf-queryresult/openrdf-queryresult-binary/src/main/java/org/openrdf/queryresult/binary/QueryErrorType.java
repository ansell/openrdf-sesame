/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.binary;

/**
 * A type-safe enumeration for query error types.
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
