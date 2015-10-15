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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.SPIN;
import org.openrdf.model.vocabulary.SPINX;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.query.algebra.evaluation.util.Statements;
import org.openrdf.spin.Argument;
import org.openrdf.spin.SpinParser;


public class SpinxFunctionParser implements FunctionParser
{
	private final SpinParser parser;
	private final ScriptEngineManager scriptManager;

	public SpinxFunctionParser(SpinParser parser)
	{
		this.parser = parser;
		this.scriptManager = new ScriptEngineManager();
	}

	@Override
	public Function parse(URI funcUri, TripleSource store)
		throws OpenRDFException
	{
		Value codeValue = Statements.singleValue(funcUri, SPINX.JAVA_SCRIPT_CODE_PROPERTY, store);
		String code = (codeValue instanceof Literal) ? ((Literal)codeValue).getLabel() : null;
		Value fileValue = Statements.singleValue(funcUri, SPINX.JAVA_SCRIPT_FILE_PROPERTY, store);
		String file = (fileValue instanceof Literal) ? ((Literal)fileValue).getLabel() : null;
		if(code == null && file == null) {
			return null;
		}

		if(code == null) {
			code = funcUri.getLocalName();
		}

		ScriptEngine engine = scriptManager.getEngineByName("javascript");
		try {
			if(file != null) {
				String ns = funcUri.getNamespace();
				try{
					Reader reader = new InputStreamReader(new URL(new URL(ns.substring(0, ns.length()-1)), file).openStream());
					try {
						engine.eval(reader);
					}
					finally {
						try {
							reader.close();
						}
						catch(IOException e) {
							// ignore
						}
					}
				}
				catch(IOException e) {
					throw new QueryEvaluationException(e);
				}
			}
		}
		catch(ScriptException e) {
			throw new QueryEvaluationException(e);
		}

		Value returnValue = Statements.singleValue(funcUri, SPIN.RETURN_TYPE_PROPERTY, store);

		Map<URI,Argument> templateArgs = parser.parseArguments(funcUri, store);

		SpinxFunction func = new SpinxFunction(funcUri.stringValue());
		func.setScriptEngine(engine);
		func.setScript(code);
		func.setReturnType((returnValue instanceof URI) ? (URI) returnValue : null);
		List<URI> orderedArgs = SpinParser.orderArguments(templateArgs.keySet());
		for(URI uri : orderedArgs) {
			Argument arg = templateArgs.get(uri);
			func.addArgument(arg);
		}

		return func;
	}
}
