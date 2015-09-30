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
package org.openrdf.repository.sparql.query;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;

import info.aduna.net.ParsedURI;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.util.Literals;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Operation;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.query.impl.MapBindingSet;

/**
 * @author jeen
 */
@Deprecated
public abstract class SPARQLOperation implements Operation {

	private static Executor executor = Executors.newCachedThreadPool();
	protected HttpClient client;
	private String url;
	protected Dataset dataset = new SimpleDataset();

	private String operation;
	
	protected MapBindingSet bindings = new MapBindingSet();

	public SPARQLOperation(HttpClient client, String url, String base, String operation) {
		this.url = url;
		this.operation = operation;
		this.client = client;
		boolean abs = base != null && base.length() > 0
				&& new ParsedURI(base).isAbsolute();
		if (abs && !operation.toUpperCase().contains("BASE")) {
			this.operation = "BASE <" + base + "> " + operation;
		}
	}
	
	public String getUrl() {
		return url;
	}
	public BindingSet getBindings() {
		return bindings;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public boolean getIncludeInferred() {
		return true;
	}

	public void removeBinding(String name) {
		bindings.removeBinding(name);
	}

	public void setBinding(String name, Value value) {
		assert value instanceof Literal || value instanceof IRI;
		bindings.addBinding(name, value);
	}

	public void clearBindings() {
		bindings.clear();
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public void setIncludeInferred(boolean inf) {
		if (!inf) {
			throw new UnsupportedOperationException();
		}
	}

	protected void execute(Runnable command) {
		executor.execute(command);
	}

	protected Set<String> getBindingNames() {
		if (bindings.size() == 0)
			return Collections.EMPTY_SET;
		Set<String> names = new HashSet<String>();
		String qry = operation;
		int b = qry.indexOf('{');
		String select = qry.substring(0, b);
		for (String name : bindings.getBindingNames()) {
			String replacement = getReplacement(bindings.getValue(name));
			if (replacement != null) {
				String pattern = ".*[\\?\\$]" + name + "\\W.*";
				if (Pattern.compile(pattern, Pattern.MULTILINE | Pattern.DOTALL).matcher(select).matches()) {
					names.add(name);
				}
			}
		}
		return names;
	}
	
	protected String getQueryString() {
		if (bindings.size() == 0)
			return operation;
		String qry = operation;
		int b = qry.indexOf('{');
		String select = qry.substring(0, b);
		String where = qry.substring(b);
		for (String name : bindings.getBindingNames()) {
			String replacement = getReplacement(bindings.getValue(name));
			if (replacement != null) {
				String pattern = "[\\?\\$]" + name + "(?=\\W)";
				select = select.replaceAll(pattern, "");
				where = where.replaceAll(pattern, replacement);
			}
		}
		return select + where;
	}
	
	private String getReplacement(Value value) {
		StringBuilder sb = new StringBuilder();
		if (value instanceof IRI) {
			return appendValue(sb, (IRI) value).toString();
		} else if (value instanceof Literal) {
			return appendValue(sb, (Literal) value).toString();
		} else {
			throw new IllegalArgumentException(
					"BNode references not supported by SPARQL end-points");
		}
	}

	private StringBuilder appendValue(StringBuilder sb, IRI uri) {
		sb.append("<").append(uri.stringValue()).append(">");
		return sb;
	}

	private StringBuilder appendValue(StringBuilder sb, Literal lit) {
		sb.append('"');
		sb.append(lit.getLabel().replace("\"", "\\\""));
		sb.append('"');

		if (Literals.isLanguageLiteral(lit)) {
			sb.append('@');
			sb.append(lit.getLanguage());
		}
		else {
			sb.append("^^<");
			sb.append(lit.getDatatype().stringValue());
			sb.append('>');
		}
		return sb;
	}


}
