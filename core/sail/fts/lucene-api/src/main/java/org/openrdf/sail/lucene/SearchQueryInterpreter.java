package org.openrdf.sail.lucene;

import java.util.Collection;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailException;

public interface SearchQueryInterpreter {
	void setIncompleteQueryFails(boolean f);
	/**
	 * Processes a TupleExpr into a set of SearchQueryEvaluators.
	 * @param tupleExpr the TupleExpr to process.
	 * @param bindings any bindings.
	 * @param specs the Collection to add any SearchQueryEvaluators to.
	 */
	void process(TupleExpr tupleExpr, BindingSet bindings, Collection<SearchQueryEvaluator> specs) throws SailException;
}
