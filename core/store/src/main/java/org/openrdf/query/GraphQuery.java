/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.result.GraphResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.store.StoreException;

public interface GraphQuery extends Query {

	public GraphResult evaluate()
		throws StoreException;

	public <H extends RDFHandler> H evaluate(H handler)
		throws StoreException, RDFHandlerException;
}
