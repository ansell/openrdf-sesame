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

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.spin.Argument;
import org.openrdf.spin.function.ConstructTupleFunction.GraphQueryResultIteration;
import org.openrdf.spin.function.SelectTupleFunction.TupleQueryResultIteration;

import com.google.common.base.Joiner;


public class SpinTupleFunction extends AbstractSpinFunction implements TransientTupleFunction {

	private ParsedQuery parsedQuery;

	private final List<Argument> arguments = new ArrayList<Argument>(4);

	public SpinTupleFunction(String uri) {
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
	public CloseableIteration<? extends List<? extends Value>,QueryEvaluationException> evaluate(ValueFactory valueFactory, Value... args)
		throws QueryEvaluationException
	{
		QueryPreparer qp = getCurrentQueryPreparer();
		CloseableIteration<? extends List<? extends Value>,QueryEvaluationException> iter;
		if(parsedQuery instanceof ParsedBooleanQuery) {
			ParsedBooleanQuery askQuery = (ParsedBooleanQuery) parsedQuery;
			BooleanQuery queryOp = qp.prepare(askQuery);
			addBindings(queryOp, arguments, args);
			Value result = BooleanLiteralImpl.valueOf(queryOp.evaluate());
			iter = new SingletonIteration<List<Value>,QueryEvaluationException>(Collections.singletonList(result));
		}
		else if(parsedQuery instanceof ParsedTupleQuery) {
			ParsedTupleQuery selectQuery = (ParsedTupleQuery) parsedQuery;
			TupleQuery queryOp = qp.prepare(selectQuery);
			addBindings(queryOp, arguments, args);
			iter = new TupleQueryResultIteration(queryOp.evaluate());
		}
		else if(parsedQuery instanceof ParsedGraphQuery) {
			ParsedGraphQuery graphQuery = (ParsedGraphQuery) parsedQuery;
			GraphQuery queryOp = qp.prepare(graphQuery);
			addBindings(queryOp, arguments, args);
			iter = new GraphQueryResultIteration(queryOp.evaluate());
		}
		else {
			throw new IllegalStateException("Unexpected query: "+parsedQuery);
		}
		return iter;
	}

	private static void addBindings(Query query, List<Argument> arguments, Value... args)
	{
		for(int i=0; i<args.length; i++) {
			Argument argument = arguments.get(i);
			query.setBinding(argument.getPredicate().getLocalName(), args[i]);
		}
	}
}
