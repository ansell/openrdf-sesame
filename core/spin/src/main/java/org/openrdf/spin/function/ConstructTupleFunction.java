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
package org.openrdf.spin.function;

import info.aduna.iteration.CloseableIteration;

import java.util.Arrays;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.spin.SpinParser;


public class ConstructTupleFunction extends AbstractSpinFunction implements TupleFunction {

	private SpinParser parser;

	public ConstructTupleFunction() {
		super(SPIN.CONSTRUCT_PROPERTY.stringValue());
	}

	public ConstructTupleFunction(SpinParser parser) {
		this();
		this.parser = parser;
	}

	public SpinParser getSpinParser() {
		return parser;
	}

	public void setSpinParser(SpinParser parser) {
		this.parser = parser;
	}

	@Override
	public CloseableIteration<? extends List<? extends Value>,QueryEvaluationException> evaluate(ValueFactory valueFactory, Value... args)
		throws QueryEvaluationException
	{
		QueryPreparer qp = getCurrentQueryPreparer();
		if(args.length == 0 || !(args[0] instanceof Resource)) {
			throw new QueryEvaluationException("First argument must be a resource");
		}
		if((args.length % 2) == 0) {
			throw new QueryEvaluationException("Old number of arguments required");
		}
		try {
			ParsedGraphQuery graphQuery = parser.parseConstructQuery((Resource) args[0], qp.getTripleSource());
			GraphQuery queryOp = qp.prepare(graphQuery);
			addBindings(queryOp, args);
			final GraphQueryResult queryResult = queryOp.evaluate();
			return new GraphQueryResultIteration(queryResult);
		}
		catch (QueryEvaluationException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new ValueExprEvaluationException(e);
		}
	}


	static class GraphQueryResultIteration implements
			CloseableIteration<List<Value>, QueryEvaluationException>
	{

		private final GraphQueryResult queryResult;

		GraphQueryResultIteration(GraphQueryResult queryResult) {
			this.queryResult = queryResult;
		}

		@Override
		public boolean hasNext()
			throws QueryEvaluationException
		{
			return queryResult.hasNext();
		}

		@Override
		public List<Value> next()
			throws QueryEvaluationException
		{
			Statement stmt = queryResult.next();
			Resource ctx = stmt.getContext();
			if(ctx != null) {
				return Arrays.asList(stmt.getSubject(), stmt.getPredicate(), stmt.getObject(), ctx);
			}
			else {
				return Arrays.asList(stmt.getSubject(), stmt.getPredicate(), stmt.getObject());
			}
		}

		@Override
		public void remove()
			throws QueryEvaluationException
		{
			queryResult.remove();
		}

		@Override
		public void close()
			throws QueryEvaluationException
		{
			queryResult.close();
		}
	}
}
