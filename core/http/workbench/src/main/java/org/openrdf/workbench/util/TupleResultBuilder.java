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
package org.openrdf.workbench.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.query.resultio.QueryResultWriter;

public class TupleResultBuilder {

	private final QueryResultWriter out;

	private final ValueFactory vf;

	private List<String> variables = new ArrayList<String>();

	private Map<String, String> prefixes = new HashMap<String, String>();

	public TupleResultBuilder(QueryResultWriter writer, ValueFactory valueFactory) {
		this.out = writer;
		this.vf = valueFactory;
	}

	public void prefix(String prefix, String namespace) {
		prefixes.put(namespace, prefix);
	}

	public TupleResultBuilder transform(String path, String xsl)
		throws QueryResultHandlerException
	{
		out.handleStylesheet(path + "/" + xsl);
		return this;
	}

	public TupleResultBuilder start(String... variables)
		throws QueryResultHandlerException
	{
		variables(variables);
		return this;
	}

	public TupleResultBuilder variables(String... names)
		throws QueryResultHandlerException
	{
		variables = Arrays.asList(names);
		out.startQueryResult(variables);
		return this;
	}

	public TupleResultBuilder link(List<String> url)
		throws QueryResultHandlerException
	{
		out.handleLinks(url);
		return this;
	}

	public void bool(boolean result)
		throws QueryResultHandlerException
	{
		out.handleBoolean(result);
	}

	public TupleResultBuilder result(Object... result)
		throws QueryResultHandlerException
	{
		closeHead();
		QueryBindingSet bindingSet = new QueryBindingSet();
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null)
				continue;
			bindingSet.addBinding(outputNamedResult(variables.get(i), result[i]));
		}
		out.handleSolution(bindingSet);
		return this;
	}

	public TupleResultBuilder namedResult(String name, Object result)
		throws QueryResultHandlerException
	{
		closeHead();
		QueryBindingSet bindingSet = new QueryBindingSet();
		bindingSet.addBinding(outputNamedResult(name, result));
		out.handleSolution(bindingSet);
		return this;
	}

	private Binding outputNamedResult(String name, Object result)
		throws QueryResultHandlerException
	{
		final Value nextValue;
		if (result instanceof Boolean) {
			nextValue = vf.createLiteral(((Boolean)result).booleanValue());
		}
		else if (isQName(result)) {
			nextValue = (URI)result;
		}
		else if (result instanceof URI) {
			nextValue = (URI)result;
		}
		else if (result instanceof BNode) {
			nextValue = (BNode)result;
		}
		else if (result instanceof Literal) {
			nextValue = (Literal)result;
		}
		else {
			nextValue = vf.createLiteral(result);
		}
		return new BindingImpl(name, nextValue);
	}

	private boolean isQName(Object result) {
		if (result instanceof URI) {
			URI uri = (URI)result;
			return prefixes.containsKey(uri.getNamespace());
		}
		return false;
	}

	public TupleResultBuilder end()
		throws QueryResultHandlerException
	{
		out.endQueryResult();
		return this;
	}

	public TupleResultBuilder endBoolean() {
		// do nothing, as the call to handleBoolean always ends the document
		return this;
	}

	private void closeHead()
		throws QueryResultHandlerException
	{
		out.endHeader();
	}

	public void flush() {
	}

}
