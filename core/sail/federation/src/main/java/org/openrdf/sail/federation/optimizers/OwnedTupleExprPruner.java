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
package org.openrdf.sail.federation.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;

/**
 * Remove redundant {@link OwnedTupleExpr}.
 * 
 * @author James Leigh
 */
public class OwnedTupleExprPruner extends
		QueryModelVisitorBase<RuntimeException> implements QueryOptimizer {

	private OwnedTupleExpr owned;

	public void optimize(TupleExpr query, Dataset dataset, BindingSet bindings) {
		owned = null; // NOPMD
		query.visit(this);
	}

	@Override
	public void meetOther(QueryModelNode node) {
		if (node instanceof OwnedTupleExpr) {
			meetOwnedTupleExpr((OwnedTupleExpr) node);
		} else {
			super.meetOther(node);
		}
	}

	private void meetOwnedTupleExpr(OwnedTupleExpr node) {
		if (owned == null) {
			owned = node;
			super.meetOther(node);
			owned = null; // NOPMD
		} else {
			// no nested OwnedTupleExpr
			TupleExpr replacement = node.getArg().clone();
			node.replaceWith(replacement);
			replacement.visit(this);
		}
	}

}
