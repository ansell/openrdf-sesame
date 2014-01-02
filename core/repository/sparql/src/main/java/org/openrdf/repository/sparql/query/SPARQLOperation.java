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
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.util.Literals;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Operation;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.MapBindingSet;

/**
 * @author jeen
 */
@Deprecated
public abstract class SPARQLOperation implements Operation {

	private static Executor executor = Executors.newCachedThreadPool();
	protected HttpClient client;
	private String url;
	protected Dataset dataset = new DatasetImpl();

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
		assert value instanceof Literal || value instanceof URI;
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
		if (value instanceof URI) {
			return appendValue(sb, (URI) value).toString();
		} else if (value instanceof Literal) {
			return appendValue(sb, (Literal) value).toString();
		} else {
			throw new IllegalArgumentException(
					"BNode references not supported by SPARQL end-points");
		}
	}

	private StringBuilder appendValue(StringBuilder sb, URI uri) {
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
