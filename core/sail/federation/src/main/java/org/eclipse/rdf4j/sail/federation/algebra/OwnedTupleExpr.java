/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.sail.federation.algebra;

import java.util.Map;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.algebra.QueryModelVisitor;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.UnaryTupleOperator;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.sail.federation.evaluation.InsertBindingSetCursor;

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

	public void prepare(QueryLanguage queryLn, String qry, Map<String, String> bindings)
		throws RepositoryException, MalformedQueryException
	{
		assert this.query == null;
		this.query = owner.prepareTupleQuery(queryLn, qry);
		this.variables = bindings;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Dataset dataset,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		CloseableIteration<BindingSet, QueryEvaluationException> rval = null;
		if (query != null) {
			try {
				synchronized (query) {
					for (String name : variables.keySet()) {
						if (bindings.hasBinding(name)) {
							Value value = bindings.getValue(name);
							query.setBinding(variables.get(name), value);
						}
						else {
							query.removeBinding(variables.get(name));
						}
					}
					query.setDataset(dataset);
					TupleQueryResult result = query.evaluate();
					rval = new InsertBindingSetCursor(result, bindings);
				}
			}
			catch (IllegalArgumentException e) { // NOPMD
				// query does not support BNode bindings
			}
		}
		return rval;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meetOther(this);
	}

	@Override
	public String getSignature() {
		return this.getClass().getSimpleName() + " " + owner.toString();
	}

}
