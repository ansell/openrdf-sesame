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
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.algebra.evaluation.QueryPreparer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.parser.ParsedBooleanQuery;


public class AskFunction extends AbstractSpinFunction implements Function {

	private SpinParser parser;

	public AskFunction() {
		super(SPIN.ASK_FUNCTION.stringValue());
	}

	public AskFunction(SpinParser parser) {
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
}
