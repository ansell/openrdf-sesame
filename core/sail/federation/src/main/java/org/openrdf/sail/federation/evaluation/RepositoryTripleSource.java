/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ExceptionConvertingIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Allow a single repository member to control a EvaulationStrategy.
 * 
 * @author James Leigh
 */
public class RepositoryTripleSource implements TripleSource {

	private RepositoryConnection repo;

	public RepositoryTripleSource(RepositoryConnection repo) {
		this.repo = repo;
	}

	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
			Resource subj, URI pred, Value obj, Resource... contexts)
			throws QueryEvaluationException {
		RepositoryResult<Statement> result;
		try {
			result = repo.getStatements(subj, pred, obj, true, contexts);
		} catch (RepositoryException e) {
			throw new QueryEvaluationException(e);
		}
		return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(result){

			@Override
			protected QueryEvaluationException convert(Exception e) {
				return new QueryEvaluationException(e);
			}};
	}

	public ValueFactory getValueFactory() {
		return repo.getValueFactory();
	}

}
