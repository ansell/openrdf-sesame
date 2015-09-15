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

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Query;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.parser.ParsedBooleanQuery;


public class AskFunction implements Function {

	private QueryPreparer queryPreparer;

	private SPINParser parser;

	public AskFunction() {
	}

	public AskFunction(SPINParser parser) {
		this.parser = parser;
	}

	@Override
	public String getURI() {
		return SPIN.ASK_FUNCTION.toString();
	}

	public QueryPreparer getQueryPreparer() {
		return queryPreparer;
	}
	
	public void setQueryPreparer(QueryPreparer queryPreparer) {
		this.queryPreparer = queryPreparer;
	}

	public SPINParser getSPINParser() {
		return parser;
	}

	public void setSPINParser(SPINParser parser) {
		this.parser = parser;
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if(args.length == 0 || !(args[0] instanceof Resource)) {
			throw new ValueExprEvaluationException("First argument must be a resource");
		}
		if((args.length % 2) == 0) {
			throw new ValueExprEvaluationException("Old number of arguments required");
		}
		QueryPreparer qp = (queryPreparer != null) ? queryPreparer : QueryContext.getQueryPreparer();
		if(qp == null) {
			throw new IllegalStateException("No QueryPreparer!");
		}
		try {
			ParsedBooleanQuery askQuery = parser.parseAskQuery((Resource) args[0], qp.getTripleSource());
			BooleanQuery queryOp = qp.prepare(askQuery);
			addBindings(queryOp, args);
			return BooleanLiteralImpl.valueOf(queryOp.evaluate());
		}
		catch (ValueExprEvaluationException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new ValueExprEvaluationException(e);
		}
	}

	private void addBindings(Query query, Value... args)
		throws ValueExprEvaluationException
	{
		for(int i=1; i<args.length; i+=2) {
			if(!(args[i] instanceof Literal)) {
				throw new ValueExprEvaluationException("Argument "+i+" must be a literal");
			}
			query.setBinding(((Literal)args[i]).getLabel(), args[i+1]);
		}
	}
}
