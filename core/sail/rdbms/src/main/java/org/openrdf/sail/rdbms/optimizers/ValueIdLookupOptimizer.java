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
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.AbstractQueryModelVisitor;
import org.openrdf.sail.rdbms.RdbmsValueFactory;

/**
 * Iterates through the query and converting the values into RDBMS values.
 * 
 * @author James Leigh
 * 
 */
public class ValueIdLookupOptimizer implements QueryOptimizer {

	RdbmsValueFactory vf;

	public ValueIdLookupOptimizer(RdbmsValueFactory vf) {
		super();
		this.vf = vf;
	}

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new VarVisitor());
	}

	protected class VarVisitor extends AbstractQueryModelVisitor<RuntimeException> {

		@Override
		public void meet(Var var) {
			if (var.hasValue()) {
				var.setValue(vf.asRdbmsValue(var.getValue()));
			}
		}
	}
}
