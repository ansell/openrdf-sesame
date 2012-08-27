/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.query;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;

import info.aduna.net.ParsedURI;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Operation;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.sparql.SPARQLConnection;

/**
 * @author jeen
 */
public abstract class SPARQLOperation implements Operation {

	private static Executor executor = Executors.newCachedThreadPool();
	protected HttpClient client;
	private String url;
	private Dataset dataset = new DatasetImpl();

	private String operation;
	
	private MapBindingSet bindings = new MapBindingSet();

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

		if (lit.getLanguage() != null) {
			sb.append('@');
			sb.append(lit.getLanguage());
		}

		if (lit.getDatatype() != null) {
			sb.append("^^<");
			sb.append(lit.getDatatype().stringValue());
			sb.append('>');
		}
		return sb;
	}


}
