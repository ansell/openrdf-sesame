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
package org.openrdf.spin;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.LookAheadIteration;
import info.aduna.iteration.UnionIteration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.federation.FederatedService;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.parser.ParsedGraphQuery;


public class ConstructFederatedService implements FederatedService {

	private QueryPreparer queryPreparer;

	private SpinParser parser;

	private volatile boolean isInitialized;

	public ConstructFederatedService(SpinParser parser) {
		this.parser = parser;
	}

	public QueryPreparer getQueryPreparer() {
		return queryPreparer;
	}
	
	public void setQueryPreparer(QueryPreparer queryPreparer) {
		this.queryPreparer = queryPreparer;
	}

	public SpinParser getSpinParser() {
		return parser;
	}

	public void setSpinParser(SpinParser parser) {
		this.parser = parser;
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
	public boolean ask(Service service, BindingSet bindings, String baseUri)
		throws QueryEvaluationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> select(Service service,
			Set<String> projectionVars, BindingSet bindings, String baseUri)
		throws QueryEvaluationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service service,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings, String baseUri)
		throws QueryEvaluationException
	{
		TupleExpr expr = service.getArg();
		ListIndexer visitor = new ListIndexer();
		expr.visit(visitor);

		List<Var> args = readList(visitor.subjName, visitor.listEntryVars, visitor.listNexts);
		final List<Var> resultBindings = readList(visitor.objName, visitor.listEntryVars, visitor.listNexts);

		if(args.size() == 0) {
			throw new QueryEvaluationException("There must be at least one argument");
		}
		if((args.size() % 2) == 0) {
			throw new QueryEvaluationException("Old number of arguments required");
		}
		if(resultBindings.size() != 3) {
			throw new QueryEvaluationException("Three result bindings are required");
		}

		QueryPreparer qp = (queryPreparer != null) ? queryPreparer : QueryContext.getQueryPreparer();
		if(qp == null) {
			throw new IllegalStateException("No QueryPreparer!");
		}

		try {
			List<CloseableIteration<BindingSet,QueryEvaluationException>> resultIters = new ArrayList<CloseableIteration<BindingSet,QueryEvaluationException>>();
			while(bindings.hasNext()) {
				final BindingSet bs = bindings.next();
				Value query = getValue(args.get(0), bs);
				if(!(query instanceof Resource)) {
					throw new QueryEvaluationException("First argument must be a resource");
				}
				ParsedGraphQuery graphQuery = parser.parseConstructQuery((Resource) query, qp.getTripleSource());
				GraphQuery queryOp = qp.prepare(graphQuery);
				addBindings(queryOp, args, bs);
				final GraphQueryResult result = queryOp.evaluate();
				resultIters.add(new LookAheadIteration<BindingSet, QueryEvaluationException>()
				{
					@Override
					public BindingSet getNextElement()
						throws QueryEvaluationException
					{
						QueryBindingSet resultBindingSet;
						if(result.hasNext()) {
							Statement stmt = result.next();
							String subjVar = resultBindings.get(0).getName();
							Value subjValue = bs.getValue(subjVar);
							if(subjValue != null && !stmt.getSubject().equals(subjValue)) {
								return null;
							}
							String predVar = resultBindings.get(1).getName();
							Value predValue = bs.getValue(predVar);
							if(predValue != null && !stmt.getPredicate().equals(predValue)) {
								return null;
							}
							String objVar = resultBindings.get(2).getName();
							Value objValue = bs.getValue(objVar);
							if(objValue != null && !stmt.getObject().equals(objValue)) {
								return null;
							}
							resultBindingSet = new QueryBindingSet();
							resultBindingSet.addBinding(subjVar, stmt.getSubject());
							resultBindingSet.addBinding(predVar, stmt.getPredicate());
							resultBindingSet.addBinding(objVar, stmt.getObject());
						}
						else {
							resultBindingSet = null;
						}
						return resultBindingSet;
					}

					@Override
					protected void handleClose()
						throws QueryEvaluationException
					{
						result.close();
					}
				});
			}
			return new DistinctIteration<BindingSet, QueryEvaluationException>(new UnionIteration<BindingSet, QueryEvaluationException>(resultIters));
		}
		catch (QueryEvaluationException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new QueryEvaluationException(e);
		}
	}

	private void addBindings(Query query, List<Var> args, BindingSet bs)
		throws ValueExprEvaluationException
	{
		for(int i=1; i<args.size(); i+=2) {
			Value arg = getValue(args.get(i), bs);
			if(!(arg instanceof Literal)) {
				throw new ValueExprEvaluationException("Argument "+i+" must be a literal");
			}
			query.setBinding(((Literal)arg).getLabel(), getValue(args.get(i+1), bs));
		}
	}

	private Value getValue(Var var, BindingSet bs)
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


	static <T> List<T> readList(String list, Map<String,T> listEntries, Map<String,String> listNexts) {
		List<T> arr = new ArrayList<T>();
		do {
			T v = listEntries.get(list);
			if(v != null) {
				arr.add(v);
			}
			list = listNexts.get(list);
		} while(list != null);
		return arr;
	}



	static class ListIndexer extends QueryModelVisitorBase<RuntimeException> {
		final Map<String,Var> listEntryVars = new HashMap<String,Var>();
		final Map<String,String> listNexts = new HashMap<String,String>();
		String subjName;
		String objName;

		@Override
		public void meet(StatementPattern node) {
			URI pred = (URI) node.getPredicateVar().getValue();
			if(SPIN.CONSTRUCT_PROPERTY.equals(pred)) {
				subjName = node.getSubjectVar().getName();
				objName = node.getObjectVar().getName();
			}
			else if(RDF.FIRST.equals(pred)) {
				listEntryVars.put(node.getSubjectVar().getName(), node.getObjectVar());
			}
			else if(RDF.REST.equals(pred)) {
				listNexts.put(node.getSubjectVar().getName(), node.getObjectVar().getName());
			}
		}
	}
}
