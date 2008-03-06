/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

public interface GraphQuery extends Query {

	public GraphQueryResult evaluate()
		throws QueryEvaluationException;

	public void evaluate(RDFHandler handler)
		throws QueryEvaluationException, RDFHandlerException;
}
