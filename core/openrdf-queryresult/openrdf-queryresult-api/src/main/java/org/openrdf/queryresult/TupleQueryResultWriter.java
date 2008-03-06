/*  Copyright (C) 2001-2006 Aduna (http://www.aduna-software.com/)
 *
 *  This software is part of the Sesame Framework. It is licensed under
 *  the following two licenses as alternatives:
 *
 *   1. Open Software License (OSL) v3.0
 *   2. GNU Lesser General Public License (LGPL) v2.1 or any newer
 *      version
 *
 *  By using, modifying or distributing this software you agree to be 
 *  bound by the terms of at least one of the above licenses.
 *
 *  See the file LICENSE.txt that is distributed with this software
 *  for the complete terms and further details.
 */
package org.openrdf.queryresult;

import java.io.OutputStream;

/**
 * The interface of objects that writer query results in a specific query result
 * format.
 */
public interface TupleQueryResultWriter extends TupleQueryResultHandler {

	/**
	 * Gets the query result format that this writer uses.
	 */
	public TupleQueryResultFormat getQueryResultFormat();

	public void setOutputStream(OutputStream out);
}
