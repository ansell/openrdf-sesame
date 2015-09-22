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

import java.util.ArrayList;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.BooleanQueryResultHandler;
import org.openrdf.query.Query;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.spin.Argument;

import com.google.common.base.Joiner;


public class SpinFunction extends AbstractSpinFunction implements Function {

	private ParsedQuery parsedQuery;

	private final List<Argument> arguments = new ArrayList<Argument>(4);

	public SpinFunction(String uri) {
		super(uri);
	}

	public void setParsedQuery(ParsedQuery query) {
		this.parsedQuery = query;
	}

	public ParsedQuery getParsedQuery() {
		return parsedQuery;
	}

	public void addArgument(Argument arg) {
		arguments.add(arg);
	}

	public List<Argument> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return getURI()+"("+ Joiner.on(", ").join(arguments)+")";
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		QueryPreparer qp = getCurrentQueryPreparer();
		ResultHandler handler = new ResultHandler();
		if(parsedQuery instanceof ParsedBooleanQuery) {
			ParsedBooleanQuery askQuery = (ParsedBooleanQuery) parsedQuery;
			BooleanQuery queryOp = qp.prepare(askQuery);
			addBindings(queryOp, arguments, args);
			try {
				handler.handleBoolean(queryOp.evaluate());
			}
			catch (OpenRDFException e) {
				throw new ValueExprEvaluationException(e);
			}
		}
		else if(parsedQuery instanceof ParsedTupleQuery) {
			ParsedTupleQuery selectQuery = (ParsedTupleQuery) parsedQuery;
			TupleQuery queryOp = qp.prepare(selectQuery);
			addBindings(queryOp, arguments, args);
			try {
				queryOp.evaluate(handler);
			}
			catch (OpenRDFException e) {
				throw new ValueExprEvaluationException(e);
			}
		}
		else {
			throw new IllegalStateException("Unexpected query: "+parsedQuery);
		}
		return handler.getResult();
	}

	private static void addBindings(Query query, List<Argument> arguments, Value... args)
	{
		for(int i=0; i<args.length; i++) {
			Argument argument = arguments.get(i);
			query.setBinding(argument.getPredicate().getLocalName(), args[i]);
		}
	}


	static class ResultHandler implements BooleanQueryResultHandler, TupleQueryResultHandler {
		Value result;

		Value getResult() {
			return result;
		}

		@Override
		public void handleBoolean(boolean value)
			throws QueryResultHandlerException
		{
			result = BooleanLiteralImpl.valueOf(value);
		}

		@Override
		public void handleLinks(List<String> linkUrls)
			throws QueryResultHandlerException
		{
		}

		@Override
		public void startQueryResult(List<String> bindingNames)
			throws TupleQueryResultHandlerException
		{
			result = null;
		}

		@Override
		public void handleSolution(BindingSet bindingSet)
			throws TupleQueryResultHandlerException
		{
			if(result == null) {
				if(bindingSet.size() != 1) {
					throw new TupleQueryResultHandlerException("Only a single result variables is supported: "+bindingSet);
				}
				result = bindingSet.iterator().next().getValue();
			}
		}

		@Override
		public void endQueryResult()
			throws TupleQueryResultHandlerException
		{
		}
	}
}
