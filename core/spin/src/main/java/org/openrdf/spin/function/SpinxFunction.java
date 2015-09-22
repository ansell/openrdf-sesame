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

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;
import org.openrdf.spin.Argument;

import com.google.common.base.Joiner;


public class SpinxFunction implements Function {
	private final String uri;

	private final List<Argument> arguments = new ArrayList<Argument>(4);

	private ScriptEngine scriptEngine;
	private CompiledScript compiledScript;
	private String script;
	private URI returnType;

	public SpinxFunction(String uri) {
		this.uri = uri;
	}

	public void setScriptEngine(ScriptEngine engine) {
		this.scriptEngine = engine;
	}

	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getScript() {
		return script;
	}

	public void setReturnType(URI datatype) {
		this.returnType = datatype;
	}

	public URI getReturnType() {
		return returnType;
	}

	public void addArgument(Argument arg) {
		arguments.add(arg);
	}

	public List<Argument> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return uri+"("+ Joiner.on(", ").join(arguments)+")";
	}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public Value evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		Bindings bindings = scriptEngine.createBindings();
		for(int i=0; i<args.length; i++) {
			Argument argument = arguments.get(i);
			Value arg = args[i];
			Object jsArg;
			if (arg instanceof Literal) {
				Literal argLiteral = (Literal) arg;
				if(XMLSchema.INTEGER.equals(argLiteral.getDatatype())) {
					jsArg = argLiteral.intValue();
				}
				else if(XMLSchema.DECIMAL.equals(argLiteral.getDatatype())) {
					jsArg = argLiteral.doubleValue();
				}
				else {
					jsArg = argLiteral.getLabel();
				}
			}
			else {
				jsArg = arg.stringValue();
			}
			bindings.put(argument.getPredicate().getLocalName(), jsArg);
		}

		Object result;
		try {
			if(compiledScript == null && scriptEngine instanceof Compilable) {
				compiledScript = ((Compilable)scriptEngine).compile(script);
			}
			if(compiledScript != null) {
				result = compiledScript.eval(bindings);
			}
			else {
				result = scriptEngine.eval(script, bindings);
			}
		}
		catch (ScriptException e) {
			throw new ValueExprEvaluationException(e);
		}

		ValueFactory vf = ValueFactoryImpl.getInstance();
		return (returnType != null) ? vf.createLiteral(result.toString(), returnType) : vf.createURI(result.toString());
	}

}
