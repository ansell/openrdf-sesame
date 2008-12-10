/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Cursor;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.store.StoreException;

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

	public Cursor<? extends Statement> getStatements(Resource subj, URI pred, Value obj, Resource... contexts)
		throws StoreException
	{
		RepositoryResult<Statement> result = repo.getStatements(subj, pred, obj, true, contexts);
		return new RepositoryResultCursor<Statement>(result);
	}

	public ValueFactory getValueFactory() {
		return repo.getValueFactory();
	}

}
