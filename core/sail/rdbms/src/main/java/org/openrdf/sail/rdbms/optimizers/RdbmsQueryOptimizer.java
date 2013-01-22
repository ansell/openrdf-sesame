/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.rdbms.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.sail.rdbms.optimizers.SameTermFilterRdbmsOptimizer;
import org.openrdf.sail.rdbms.RdbmsValueFactory;
import org.openrdf.sail.rdbms.schema.BNodeTable;
import org.openrdf.sail.rdbms.schema.HashTable;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.URITable;

/**
 * Facade to the underlying RDBMS optimizations.
 * 
 * @author James Leigh
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

	public TupleExpr optimize(TupleExpr expr, Dataset dataset, BindingSet bindings, EvaluationStrategy strategy)
	{
		// Clone the tuple expression to allow for more aggressive optimisations
		TupleExpr tupleExpr = expr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimisers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		coreOptimizations(strategy, tupleExpr, dataset, bindings);

		rdbmsOptimizations(tupleExpr, dataset, bindings);

		new SqlConstantOptimizer().optimize(tupleExpr, dataset, bindings);

		return tupleExpr;
	}

	private void coreOptimizations(EvaluationStrategy strategy, TupleExpr expr, Dataset dataset,
			BindingSet bindings)
	{
		new BindingAssigner().optimize(expr, dataset, bindings);
		new ConstantOptimizer(strategy).optimize(expr, dataset, bindings);
		new CompareOptimizer().optimize(expr, dataset, bindings);
		new ConjunctiveConstraintSplitter().optimize(expr, dataset, bindings);
		new DisjunctiveConstraintOptimizer().optimize(expr, dataset, bindings);
		new SameTermFilterRdbmsOptimizer().optimize(expr, dataset, bindings);
	}

	protected void rdbmsOptimizations(TupleExpr expr, Dataset dataset, BindingSet bindings) {
		new ValueIdLookupOptimizer(vf).optimize(expr, dataset, bindings);
		factory.createRdbmsFilterOptimizer().optimize(expr, dataset, bindings);
		new VarColumnLookupOptimizer().optimize(expr, dataset, bindings);
		ValueJoinOptimizer valueJoins = new ValueJoinOptimizer();
		valueJoins.setBnodeTable(bnodes);
		valueJoins.setUriTable(uris);
		valueJoins.setLiteralTable(literals);
		valueJoins.setHashTable(hashTable);
		valueJoins.optimize(expr, dataset, bindings);
	}

}
