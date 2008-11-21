/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.schema.BNodeTable;
import org.openrdf.sail.rdbms.schema.HashTable;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.URITable;
import org.openrdf.store.StoreException;

/**
 * Facade to the underlying RDBMS optimizations.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsQueryOptimizer {

	private RdbmsValueFactory vf;

	private URITable uris;

	private BNodeTable bnodes;

	private LiteralTable literals;

	private SelectQueryOptimizerFactory factory;

	private HashTable hashTable;

	public void setSelectQueryOptimizerFactory(SelectQueryOptimizerFactory factory) {
		this.factory = factory;
	}

	public void setValueFactory(RdbmsValueFactory vf) {
		this.vf = vf;
	}

	public void setUriTable(URITable uris) {
		this.uris = uris;
	}

	public void setBnodeTable(BNodeTable bnodes) {
		this.bnodes = bnodes;
	}

	public void setLiteralTable(LiteralTable literals) {
		this.literals = literals;
	}

	public void setHashTable(HashTable hashTable) {
		this.hashTable = hashTable;
	}

	public TupleExpr optimize(QueryModel query, BindingSet bindings, EvaluationStrategy strategy)
		throws StoreException
	{
		// Clone the tuple expression to allow for more aggressive optimisations
		QueryModel tupleExpr = query.clone();

		coreOptimizations(strategy, tupleExpr, bindings);

		rdbmsOptimizations(tupleExpr, bindings);

		new SqlConstantOptimizer().optimize(tupleExpr, bindings);

		return tupleExpr;
	}

	private void coreOptimizations(EvaluationStrategy strategy, QueryModel expr, BindingSet bindings) throws StoreException {
		new BindingAssigner().optimize(expr, bindings);
		new ConstantOptimizer(strategy).optimize(expr, bindings);
		new CompareOptimizer().optimize(expr, bindings);
		new ConjunctiveConstraintSplitter().optimize(expr, bindings);
		new DisjunctiveConstraintOptimizer().optimize(expr, bindings);
		new SameTermFilterOptimizer().optimize(expr, bindings);
		new QueryJoinOptimizer().optimize(expr, bindings);
	}

	protected void rdbmsOptimizations(QueryModel expr, BindingSet bindings)
		throws RdbmsException
	{
		new ValueIdLookupOptimizer(vf).optimize(expr, bindings);
		factory.createRdbmsFilterOptimizer().optimize(expr, bindings);
		new VarColumnLookupOptimizer().optimize(expr, bindings);
		ValueJoinOptimizer valueJoins = new ValueJoinOptimizer();
		valueJoins.setBnodeTable(bnodes);
		valueJoins.setUriTable(uris);
		valueJoins.setLiteralTable(literals);
		valueJoins.setHashTable(hashTable);
		valueJoins.optimize(expr, bindings);
	}

}
