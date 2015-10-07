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
package org.openrdf.sail.spin;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.FN;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.FunctionRegistry;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.spin.SpinParser;
import org.openrdf.spin.function.AskFunction;
import org.openrdf.spin.function.EvalFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueryOptimizer that adds support for SPIN functions.
 */
public class SpinFunctionInterpreter implements QueryOptimizer {
	private static final Logger logger = LoggerFactory.getLogger(SpinFunctionInterpreter.class);

	private final TripleSource tripleSource;
	private final SpinParser parser;
	private final FunctionRegistry functionRegistry;

	public SpinFunctionInterpreter(SpinParser parser, TripleSource tripleSource, FunctionRegistry functionRegistry) {
		this.parser = parser;
		this.tripleSource = tripleSource;
		this.functionRegistry = functionRegistry;
		if(!(functionRegistry.get(FN.CONCAT.toString()) instanceof org.openrdf.spin.function.Concat)) {
			functionRegistry.add(new org.openrdf.spin.function.Concat());
		}
		if(!functionRegistry.has(SPIN.EVAL_FUNCTION.toString())) {
			functionRegistry.add(new EvalFunction(parser));
		}
		if(!functionRegistry.has(SPIN.ASK_FUNCTION.toString())) {
			functionRegistry.add(new AskFunction(parser));
		}
	}

	@Override
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		try {
			tupleExpr.visit(new FunctionScanner());
		}
		catch(OpenRDFException e) {
			logger.warn("Failed to parse function");
		}
	}



	private class FunctionScanner extends QueryModelVisitorBase<OpenRDFException> {
		ValueFactory vf = tripleSource.getValueFactory();

		@Override
		public void meet(FunctionCall node)
			throws OpenRDFException
		{
			String name = node.getURI();
			if(!functionRegistry.has(name)) {
				URI funcUri = vf.createURI(name);
				Function f = parser.parseFunction(funcUri, tripleSource);
				functionRegistry.add(f);
			}
			super.meet(node);
		}
	}
}
