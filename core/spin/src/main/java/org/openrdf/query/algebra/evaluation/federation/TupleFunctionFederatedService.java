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
package org.openrdf.query.algebra.evaluation.federation;

import info.aduna.iteration.CloseableIteration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.spin.QueryContext;


public abstract class TupleFunctionFederatedService implements FederatedService {
	private final URI uri;

	private QueryPreparer queryPreparer;

	private volatile boolean isInitialized;

	protected TupleFunctionFederatedService(URI magicProperty) {
		this.uri = magicProperty;
	}

	public QueryPreparer getQueryPreparer() {
		return queryPreparer;
	}
	
	public void setQueryPreparer(QueryPreparer queryPreparer) {
		this.queryPreparer = queryPreparer;
	}

	@Override
	public boolean isInitialized() {
		return isInitialized;
	}

	@Override
	public void initialize()
	{
		isInitialized = true;
	}

	@Override
	public void shutdown()
	{
		isInitialized = false;
	}

	@Override
	public final CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service service,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings, String baseUri)
		throws QueryEvaluationException
	{
		QueryPreparer qp = (queryPreparer != null) ? queryPreparer : QueryContext.getQueryPreparer();
		if(qp == null) {
			throw new IllegalStateException("No QueryPreparer!");
		}

		TupleExpr expr = service.getArg();
		ListIndexer visitor = new ListIndexer(uri);
		expr.visit(visitor);

		List<Var> args = readList(visitor.subj, visitor.listEntries, visitor.listNexts);
		List<Var> resultBindings = readList(visitor.obj, visitor.listEntries, visitor.listNexts);

		try {
			return evaluate(args, resultBindings, bindings, qp);
		}
		catch (QueryEvaluationException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new QueryEvaluationException(e);
		}
	}

	protected abstract CloseableIteration<BindingSet,QueryEvaluationException> evaluate(List<Var> args, List<Var> resultBindings, CloseableIteration<BindingSet, QueryEvaluationException> bindings, QueryPreparer qp)
		throws OpenRDFException;

	protected static Value getValue(Var var, BindingSet bs)
		throws ValueExprEvaluationException
	{
		Value v = var.getValue();
		if(v == null) {
			v = bs.getValue(var.getName());
		}
		if(v == null) {
			throw new ValueExprEvaluationException("No value for binding "+var.getName());
		}
		return v;
	}

	private static List<Var> readList(Var var, Map<String,Var> listEntries, Map<String,Var> listNexts) {
		List<Var> arr = null;
		Var next = var;
		do {
			String name = next.getName();
			Var entry = listEntries.get(name);
			if(entry != null) {
				if(arr == null) {
					arr = new ArrayList<Var>(4);
				}
				arr.add(entry);
			}
			next = listNexts.get(name);
		} while(next != null && !RDF.NIL.equals(next.getValue()));
		if(arr == null) {
			arr = Collections.singletonList(var);
		}
		return arr;
	}



	static class ListIndexer extends QueryModelVisitorBase<RuntimeException> {
		final URI magicProperty;
		final Map<String,Var> listEntries = new HashMap<String,Var>();
		final Map<String,Var> listNexts = new HashMap<String,Var>();
		Var subj;
		Var obj;

		ListIndexer(URI magicProperty) {
			this.magicProperty = magicProperty;
		}

		@Override
		public void meet(StatementPattern node) {
			URI pred = (URI) node.getPredicateVar().getValue();
			if(magicProperty.equals(pred)) {
				subj = node.getSubjectVar();
				obj = node.getObjectVar();
			}
			else if(RDF.FIRST.equals(pred)) {
				listEntries.put(node.getSubjectVar().getName(), node.getObjectVar());
			}
			else if(RDF.REST.equals(pred)) {
				listNexts.put(node.getSubjectVar().getName(), node.getObjectVar());
			}
		}
	}
}
