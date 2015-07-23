package org.openrdf.model;

import info.aduna.iteration.CloseableIteration;

public interface StatementSource<X extends Exception> {
	public CloseableIteration<? extends Statement, X> getStatements(Resource subj,
			URI pred, Value obj, Resource... contexts)
		throws X;
}
