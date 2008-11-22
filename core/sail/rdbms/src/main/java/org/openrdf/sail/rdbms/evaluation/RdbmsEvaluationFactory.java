/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.evaluation;

import org.openrdf.query.algebra.QueryModel;
import org.openrdf.sail.rdbms.RdbmsTripleRepository;
import org.openrdf.sail.rdbms.schema.IdSequence;

/**
 * Creates an {@link RdbmsEvaluation}.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsEvaluationFactory {

	private QueryBuilderFactory factory;

	private RdbmsTripleRepository triples;

	private IdSequence ids;

	public void setQueryBuilderFactory(QueryBuilderFactory factory) {
		this.factory = factory;
	}

	public void setRdbmsTripleRepository(RdbmsTripleRepository triples) {
		this.triples = triples;
	}

	public void setIdSequence(IdSequence ids) {
		this.ids = ids;
	}

	public RdbmsEvaluation createRdbmsEvaluation(QueryModel query) {
		return new RdbmsEvaluation(factory, triples, query, ids);
	}
}
