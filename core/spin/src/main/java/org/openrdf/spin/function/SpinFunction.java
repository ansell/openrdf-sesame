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

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.spin.Argument;

import com.google.common.base.Joiner;


public class SpinFunction extends AbstractSpinFunction implements TransientFunction {

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
		Value result;
		if(parsedQuery instanceof ParsedBooleanQuery) {
			ParsedBooleanQuery askQuery = (ParsedBooleanQuery) parsedQuery;
			BooleanQuery queryOp = qp.prepare(askQuery);
			addBindings(queryOp, arguments, args);
			try {
				result = BooleanLiteralImpl.valueOf(queryOp.evaluate());
			}
			catch (QueryEvaluationException e) {
				throw new ValueExprEvaluationException(e);
			}
		}
		else if(parsedQuery instanceof ParsedTupleQuery) {
			ParsedTupleQuery selectQuery = (ParsedTupleQuery) parsedQuery;
			TupleQuery queryOp = qp.prepare(selectQuery);
			addBindings(queryOp, arguments, args);
			try {
				TupleQueryResult queryResult = queryOp.evaluate();
				if(queryResult.hasNext()) {
					BindingSet bs = queryResult.next();
					if(bs.size() != 1) {
						throw new ValueExprEvaluationException("Only a single result variables is supported: "+bs);
					}
					result = bs.iterator().next().getValue();
				}
				else {
					result = null;
				}
			}
			catch (QueryEvaluationException e) {
				throw new ValueExprEvaluationException(e);
			}
		}
		else {
			throw new IllegalStateException("Unexpected query: "+parsedQuery);
		}
		return result;
	}

	private static void addBindings(Query query, List<Argument> arguments, Value... args)
	{
		for(int i=0; i<args.length; i++) {
			Argument argument = arguments.get(i);
			query.setBinding(argument.getPredicate().getLocalName(), args[i]);
		}
	}
}
