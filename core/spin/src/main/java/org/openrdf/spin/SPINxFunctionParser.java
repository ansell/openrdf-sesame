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

import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.function.Function;


public class SPINxFunctionParser implements FunctionParser
{
	private final SPINParser parser;

	public SPINxFunctionParser(SPINParser parser)
	{
		this.parser = parser;
	}

	@Override
	public Function parse(URI funcUri, TripleSource store)
		throws OpenRDFException
	{

		Map<URI,Argument> templateArgs = parser.parseArguments(funcUri, store);

		SPINxFunction func = new SPINxFunction(funcUri);
		List<URI> orderedArgs = SPINParser.orderArguments(templateArgs.keySet());
		for(URI uri : orderedArgs) {
			Argument arg = templateArgs.get(uri);
			func.addArgument(arg);
		}

		return func;
	}
}
