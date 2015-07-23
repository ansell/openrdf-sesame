package org.openrdf.model;

import info.aduna.iteration.CloseableIteration;

/**
 * Minimal interface for accessing RDF data.
 */
public interface StatementSource<X extends Exception> {
	public CloseableIteration<? extends Statement, X> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws X;
}
