/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.workbench.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.LiteralUtilException;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.algebra.evaluation.QueryBindingSet;
import org.eclipse.rdf4j.query.impl.SimpleBinding;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;

/**
 * A small wrapper around {@link QueryResultWriter} to make it easier to
 * generate results in servlets.
 * 
 * @author peter
 */
public class TupleResultBuilder {

	private final QueryResultWriter out;

	private final ValueFactory vf;

	private List<String> variables = new ArrayList<String>();

	public TupleResultBuilder(QueryResultWriter writer, ValueFactory valueFactory) {
		this.out = writer;
		this.vf = valueFactory;
	}

	public void prefix(String prefix, String namespace)
		throws QueryResultHandlerException
	{
		out.handleNamespace(prefix, namespace);
	}

	public TupleResultBuilder transform(String path, String xsl)
		throws QueryResultHandlerException
	{
		out.handleStylesheet(path + "/" + xsl);
		return this;
	}

	/**
	 * This must be called before calling {@link #namedResult(String, Object)} or
	 * {@link #result(Object...)}.
	 * 
	 * @param variables
	 *        one or more variable names
	 * @return this builder, for the convenience of chaining calls
	 * @throws QueryResultHandlerException
	 */
	public TupleResultBuilder start(String... variables)
		throws QueryResultHandlerException
	{
		variables(variables);
		return this;
	}

	public TupleResultBuilder startBoolean() {
		// Do not need to do anything here currently
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

	public TupleResultBuilder bool(boolean result)
		throws QueryResultHandlerException
	{
		out.handleBoolean(result);
		return this;
	}

	/**
	 * {@link #start(String...)} must be called before using this method.
	 * 
	 * @param result
	 *        a single result, one value for each variable, in the same order as
	 *        the variable names were provided
	 * @return this builder, for the convenience of chaining calls
	 * @throws QueryResultHandlerException
	 */
	public TupleResultBuilder result(Object... result)
		throws QueryResultHandlerException
	{
		QueryBindingSet bindingSet = new QueryBindingSet();
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null)
				continue;
			bindingSet.addBinding(outputNamedResult(variables.get(i), result[i]));
		}
		out.handleSolution(bindingSet);
		return this;
	}

	/**
	 * {@link #start(String...)} must be called before using this method.
	 * 
	 * @param name
	 *        the variable name, from the set of provided variable names
	 * @param result
	 *        the result value associated with the given variable name
	 * @return this builder, for the convenience of chaining calls
	 * @throws QueryResultHandlerException
	 */
	public TupleResultBuilder namedResult(String name, Object result)
		throws QueryResultHandlerException
	{
		QueryBindingSet bindingSet = new QueryBindingSet();
		bindingSet.addBinding(outputNamedResult(name, result));
		out.handleSolution(bindingSet);
		return this;
	}

	private Binding outputNamedResult(String name, Object result)
		throws QueryResultHandlerException
	{
		final Value nextValue;
		if (result instanceof Value) {
			nextValue = (Value)result;
		}
		else if (result instanceof URL) {
			nextValue = vf.createIRI(result.toString());
		}
		else {
			try {
				nextValue = Literals.createLiteralOrFail(vf, result);
			}
			catch (LiteralUtilException e) {
				throw new QueryResultHandlerException("Could not convert an object to a Value", e);
			}
		}
		return new SimpleBinding(name, nextValue);
	}

	/**
	 * This must be called if {@link #start(String...)} is used, after all
	 * results are generated using either {@link #namedResult(String, Object)} or
	 * {@link #result(Object...)}.
	 * <p>
	 * This must not be called if {@link #bool(boolean)} or {@link #endBoolean()}
	 * have been called.
	 * 
	 * @return This object, for chaining with other calls.
	 * @throws QueryResultHandlerException
	 */
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

	public void flush() {
	}

}
