/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.StoreException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public interface GraphQuery extends Query {

	public GraphQueryResult evaluate()
		throws StoreException;

	public void evaluate(RDFHandler handler)
		throws StoreException, RDFHandlerException;
}
