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
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.federation.TupleFunctionFederatedService;
import org.openrdf.query.parser.ParsedGraphQuery;


public class ConstructFederatedService extends TupleFunctionFederatedService {

	private SpinParser parser;

	public ConstructFederatedService(SpinParser parser) {
		super(SPIN.CONSTRUCT_PROPERTY);
		this.parser = parser;
	}

	public SpinParser getSpinParser() {
		return parser;
	}

	public void setSpinParser(SpinParser parser) {
		this.parser = parser;
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
	protected CloseableIteration<BindingSet, QueryEvaluationException> evaluate(List<Var> args, final List<Var> resultBindings, CloseableIteration<BindingSet, QueryEvaluationException> givenBindings, QueryPreparer qp)
		throws OpenRDFException
	{
		if(args.size() == 0) {
			throw new QueryEvaluationException("There must be at least one argument");
		}
		if((args.size() % 2) == 0) {
			throw new QueryEvaluationException("Old number of arguments required");
		}
		if(resultBindings.size() != 3) {
			throw new QueryEvaluationException("Three result bindings are required");
		}
		List<CloseableIteration<BindingSet,QueryEvaluationException>> resultIters = new ArrayList<CloseableIteration<BindingSet,QueryEvaluationException>>();

		while(givenBindings.hasNext()) {
			final BindingSet givenBinding = givenBindings.next();
			Value query = getValue(args.get(0), givenBinding);
			if(!(query instanceof Resource)) {
				throw new QueryEvaluationException("First argument must be a resource");
			}
			ParsedGraphQuery graphQuery = parser.parseConstructQuery((Resource) query, qp.getTripleSource());
			GraphQuery queryOp = qp.prepare(graphQuery);
			addBindings(queryOp, args, givenBinding);
			final GraphQueryResult queryResult = queryOp.evaluate();
			resultIters.add(new LookAheadIteration<BindingSet, QueryEvaluationException>()
			{
				@Override
				public BindingSet getNextElement()
					throws QueryEvaluationException
				{
					QueryBindingSet resultBindingSet;
					if(queryResult.hasNext()) {
						Statement stmt = queryResult.next();
						String subjVarName = resultBindings.get(0).getName();
						Value givenSubjValue = givenBinding.getValue(subjVarName);
						if(givenSubjValue != null && !stmt.getSubject().equals(givenSubjValue)) {
							return null;
						}
						String predVarName = resultBindings.get(1).getName();
						Value givenPredValue = givenBinding.getValue(predVarName);
						if(givenPredValue != null && !stmt.getPredicate().equals(givenPredValue)) {
							return null;
						}
						String objVarName = resultBindings.get(2).getName();
						Value givenObjValue = givenBinding.getValue(objVarName);
						if(givenObjValue != null && !stmt.getObject().equals(givenObjValue)) {
							return null;
						}
						resultBindingSet = new QueryBindingSet();
						resultBindingSet.addBinding(subjVarName, stmt.getSubject());
						resultBindingSet.addBinding(predVarName, stmt.getPredicate());
						resultBindingSet.addBinding(objVarName, stmt.getObject());
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
					queryResult.close();
				}
			});
		}
		return new DistinctIteration<BindingSet, QueryEvaluationException>(new UnionIteration<BindingSet, QueryEvaluationException>(resultIters));
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
}
