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
import info.aduna.iteration.SingletonIteration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.spin.SpinParser;


public class SelectTupleFunction extends AbstractSpinFunction implements TupleFunction {

	private SpinParser parser;

	public SelectTupleFunction() {
		super(SPIN.SELECT_PROPERTY.stringValue());
	}

	public SelectTupleFunction(SpinParser parser) {
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
			ParsedQuery parsedQuery = parser.parseQuery((Resource) args[0], qp.getTripleSource());
			if(parsedQuery instanceof ParsedTupleQuery) {
				ParsedTupleQuery tupleQuery = (ParsedTupleQuery) parsedQuery;
				TupleQuery queryOp = qp.prepare(tupleQuery);
				addBindings(queryOp, args);
				final TupleQueryResult queryResult = queryOp.evaluate();
				return new TupleQueryResultIteration(queryResult);
			}
			else if(parsedQuery instanceof ParsedBooleanQuery) {
				ParsedBooleanQuery booleanQuery = (ParsedBooleanQuery) parsedQuery;
				BooleanQuery queryOp = qp.prepare(booleanQuery);
				addBindings(queryOp, args);
				Value result = BooleanLiteralImpl.valueOf(queryOp.evaluate());
				return new SingletonIteration<List<Value>,QueryEvaluationException>(Collections.singletonList(result));
			}
			else {
				throw new QueryEvaluationException("First argument must be a SELECT or ASK query");
			}
		}
		catch (QueryEvaluationException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new ValueExprEvaluationException(e);
		}
	}


	static class TupleQueryResultIteration implements
		CloseableIteration<List<Value>, QueryEvaluationException>
	{

		private final TupleQueryResult queryResult;

		private final List<String> bindingNames;

		TupleQueryResultIteration(TupleQueryResult queryResult)
			throws QueryEvaluationException
		{
			this.queryResult = queryResult;
			this.bindingNames = queryResult.getBindingNames();
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
			BindingSet bs = queryResult.next();
			List<Value> values = new ArrayList<Value>(bindingNames.size());
			for(String bindingName : bindingNames) {
				values.add(bs.getValue(bindingName));
			}
			return values;
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
