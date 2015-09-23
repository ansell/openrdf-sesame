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

import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.algebra.evaluation.util.Statements;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.spin.Argument;
import org.openrdf.spin.SpinParser;


public class SpinTupleFunctionParser implements TupleFunctionParser
{
	private final SpinParser parser;

	public SpinTupleFunctionParser(SpinParser parser)
	{
		this.parser = parser;
	}

	@Override
	public TupleFunction parse(URI funcUri, TripleSource store)
		throws OpenRDFException
	{
		Value body = Statements.singleValue(funcUri, SPIN.BODY_PROPERTY, store);
		if (!(body instanceof Resource)) {
			return null;
		}
		ParsedQuery query = parser.parseQuery((Resource)body, store);

		Map<URI,Argument> templateArgs = parser.parseArguments(funcUri, store);

		SpinTupleFunction func = new SpinTupleFunction(funcUri.stringValue());
		func.setParsedQuery(query);
		List<URI> orderedArgs = SpinParser.orderArguments(templateArgs.keySet());
		for(URI uri : orderedArgs) {
			Argument arg = templateArgs.get(uri);
			func.addArgument(arg);
		}

		return func;
	}
}
