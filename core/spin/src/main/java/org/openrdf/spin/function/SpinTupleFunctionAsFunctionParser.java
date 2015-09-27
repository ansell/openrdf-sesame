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

import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.algebra.evaluation.util.Statements;
import org.openrdf.spin.SpinParser;


public class SpinTupleFunctionAsFunctionParser implements FunctionParser
{
	private final SpinParser parser;

	public SpinTupleFunctionAsFunctionParser(SpinParser parser)
	{
		this.parser = parser;
	}

	@Override
	public Function parse(URI funcUri, TripleSource store)
		throws OpenRDFException
	{
		Statement magicPropStmt = Statements.single(funcUri, RDF.TYPE, SPIN.MAGIC_PROPERTY_CLASS, store);
		if(magicPropStmt == null) {
			return null;
		}

		Value body = Statements.singleValue(funcUri, SPIN.BODY_PROPERTY, store);
		if (!(body instanceof Resource)) {
			return null;
		}
		final TupleFunction tupleFunc = parser.parseMagicProperty(funcUri, store);
		return new Function()
		{
			@Override
			public String getURI() {
				return tupleFunc.getURI();
			}

			@Override
			public Value evaluate(ValueFactory valueFactory, Value... args)
				throws ValueExprEvaluationException
			{
				try {
					CloseableIteration<? extends List<? extends Value>,QueryEvaluationException> iter = tupleFunc.evaluate(valueFactory, args);
					try {
						if(iter.hasNext()) {
							return iter.next().get(0);
						}
						else {
							return null;
						}
					}
					finally {
						iter.close();
					}
				}
				catch(QueryEvaluationException e) {
					throw new ValueExprEvaluationException(e);
				}
			}
		};
	}
}
