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

import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.Statements;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedTupleQuery;
import org.openrdf.spin.SpinParser;


public class EvalFunction extends AbstractSpinFunction implements Function {

	private SpinParser parser;

	public EvalFunction() {
		super(SPIN.EVAL_FUNCTION.stringValue());
	}

	public EvalFunction(SpinParser parser) {
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
	public Value evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		QueryPreparer qp = getCurrentQueryPreparer();
		if(args.length == 0 || !(args[0] instanceof Resource)) {
			throw new ValueExprEvaluationException("First argument must be a resource");
		}
		if((args.length % 2) == 0) {
			throw new ValueExprEvaluationException("Old number of arguments required");
		}
		Value result;
		Resource subj = (Resource) args[0];
		try {
			Statement funcStmt;
			if(subj instanceof URI) {
				funcStmt = Statements.single(subj, RDF.TYPE, SPIN.FUNCTION_CLASS, qp.getTripleSource());
			}
			else {
				funcStmt = null;
			}
			if(funcStmt != null) {
				Value[] funcArgs = new Value[(args.length-1)/2];
				for(int i=0; i<funcArgs.length; i++) {
					funcArgs[i] = args[2*(i+1)];
				}
				Function func = parser.parseFunction((URI) subj, qp.getTripleSource());
				result = func.evaluate(valueFactory, funcArgs);
			}
			else {
				ParsedQuery parsedQuery = parser.parseQuery(subj, qp.getTripleSource());
				if(parsedQuery instanceof ParsedTupleQuery) {
					ParsedTupleQuery tupleQuery = (ParsedTupleQuery) parsedQuery;
					TupleQuery queryOp = qp.prepare(tupleQuery);
					addArguments(queryOp, args);
					TupleQueryResult queryResult = queryOp.evaluate();
					if(queryResult.hasNext()) {
						BindingSet bs = queryResult.next();
						Set<String> bindingNames = tupleQuery.getTupleExpr().getBindingNames();
						if(!bindingNames.isEmpty()) {
							result = bs.getValue(bindingNames.iterator().next());
						}
						else {
							result = null;
						}
					}
					else {
						result = null;
					}
				}
				else if(parsedQuery instanceof ParsedBooleanQuery) {
					ParsedBooleanQuery booleanQuery = (ParsedBooleanQuery) parsedQuery;
					BooleanQuery queryOp = qp.prepare(booleanQuery);
					addArguments(queryOp, args);
					result = BooleanLiteralImpl.valueOf(queryOp.evaluate());
				}
				else {
					throw new ValueExprEvaluationException("First argument must be a SELECT, ASK or expression");
				}
			}
		}
		catch (ValueExprEvaluationException e) {
			throw e;
		}
		catch (OpenRDFException e) {
			throw new ValueExprEvaluationException(e);
		}
		return result;
	}

	protected static void addArguments(Query query, Value... args)
		throws ValueExprEvaluationException
	{
		for(int i=1; i<args.length; i+=2) {
			if(!(args[i] instanceof URI)) {
				throw new ValueExprEvaluationException("Argument "+i+" must be a URI");
			}
			query.setBinding(((URI)args[i]).getLocalName(), args[i+1]);
		}
	}
}
