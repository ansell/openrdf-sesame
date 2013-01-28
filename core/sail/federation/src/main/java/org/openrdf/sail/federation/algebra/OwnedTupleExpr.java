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
package org.openrdf.sail.federation.algebra;

import java.util.Map;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.federation.evaluation.InsertBindingSetCursor;

/**
 * Indicates that the argument should be evaluated in a particular member.
 * 
 * @author James Leigh
 */
public class OwnedTupleExpr extends UnaryTupleOperator {

	private final RepositoryConnection owner;

	private TupleQuery query;

	private Map<String, String> variables;

	public OwnedTupleExpr(RepositoryConnection owner, TupleExpr arg) {
		super(arg);
		this.owner = owner;
	}

	public RepositoryConnection getOwner() {
		return owner;
	}

	public void prepare(QueryLanguage queryLn, String qry,
			Map<String, String> bindings) throws RepositoryException,
			MalformedQueryException {
		assert this.query == null;
		this.query = owner.prepareTupleQuery(queryLn, qry);
		this.variables = bindings;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			Dataset dataset, BindingSet bindings)
			throws QueryEvaluationException {
		CloseableIteration<BindingSet, QueryEvaluationException> rval = null;
		if (query != null) {
			try {
				synchronized (query) {
					for (String name : variables.keySet()) {
						if (bindings.hasBinding(name)) {
							Value value = bindings.getValue(name);
							query.setBinding(variables.get(name), value);
						} else {
							query.removeBinding(variables.get(name));
						}
					}
					query.setDataset(dataset);
					TupleQueryResult result = query.evaluate();
					rval = new InsertBindingSetCursor(result, bindings);
				}
			} catch (IllegalArgumentException e) { // NOPMD
				// query does not support BNode bindings
			}
		}
		return rval;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
			throws X {
		visitor.meetOther(this);
	}

	@Override
	public String getSignature() {
		return this.getClass().getSimpleName() + " " + owner.toString();
	}

}
