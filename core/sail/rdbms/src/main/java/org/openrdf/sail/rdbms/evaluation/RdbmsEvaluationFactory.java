/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import org.openrdf.query.Dataset;
import org.openrdf.sail.rdbms.RdbmsTripleRepository;

/**
 * Creates an {@link RdbmsEvaluation}.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsEvaluationFactory {
	private QueryBuilderFactory factory;
	private RdbmsTripleRepository triples;

	public void setQueryBuilderFactory(QueryBuilderFactory factory) {
		this.factory = factory;
	}

	public void setRdbmsTripleRepository(RdbmsTripleRepository triples) {
		this.triples = triples;
	}

	public RdbmsEvaluation createRdbmsEvaluation(Dataset dataset) {
		return new RdbmsEvaluation(factory, triples, dataset);
	}
}
