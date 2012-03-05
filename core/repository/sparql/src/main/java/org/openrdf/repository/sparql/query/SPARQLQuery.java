/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.query;

import info.aduna.net.ParsedURI;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.sparql.SPARQLConnection;

/**
 * Provides an execution thread for background result parsing and inlines
 * binding in a SPARQL query.
 * 
 * @author James Leigh
 * 
 */
public abstract class SPARQLQuery implements Query {
	private static Executor executor = Executors.newCachedThreadPool();
	private HttpClient client;
	private String url;
	private String query;
	private Dataset dataset = new DatasetImpl();
	
	private int maxQueryTime = 0;
	
	private MapBindingSet bindings = new MapBindingSet();

	public SPARQLQuery(HttpClient client, String url, String base, String query) {
		this.url = url;
		this.query = query;
		this.client = client;
		boolean abs = base != null && base.length() > 0
				&& new ParsedURI(base).isAbsolute();
		if (abs && !query.toUpperCase().contains("BASE")) {
			this.query = "BASE <" + base + "> " + query;
		}
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

	public int getMaxQueryTime() {
		return maxQueryTime; 
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

	public void setMaxQueryTime(int maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
		this.client.getParams().setConnectionManagerTimeout(1000L * maxQueryTime);
	}

	public String getUrl() {
		return url;
	}

	protected HttpMethodBase getResponse() throws HttpException, IOException,
			QueryEvaluationException {
		PostMethod post = new PostMethod(url);
		post.addParameter("query", getQueryString());
		if (dataset != null) {
			for (URI graph : dataset.getDefaultGraphs()) {
				post.addParameter("default-graph-uri", graph.stringValue());
			}
			for (URI graph : dataset.getNamedGraphs()) {
				post.addParameter("named-graph-uri", graph.stringValue());
			}
		}
		post.addRequestHeader("Accept", getAccept());
		Map<String, String> additionalHeaders = (Map<String, String>)client.getParams().getParameter(SPARQLConnection.ADDITIONAL_HEADER_NAME);
		if (additionalHeaders!=null) {
			for (Entry<String, String> additionalHeader : additionalHeaders.entrySet())
				post.addRequestHeader(additionalHeader.getKey(), additionalHeader.getValue());
		}
		boolean completed = false;
		try {
			if (client.executeMethod(post) >= 400) {
				throw new QueryEvaluationException(
						post.getResponseBodyAsString());
			}
			completed = true;
			return post;
		} finally {
			if (!completed) {
				post.abort();
			}
		}
	}

	protected void execute(Runnable command) {
		executor.execute(command);
	}

	protected Set<String> getBindingNames() {
		if (bindings.size() == 0)
			return Collections.EMPTY_SET;
		Set<String> names = new HashSet<String>();
		String qry = query;
		int b = qry.indexOf('{');
		String select = qry.substring(0, b);
		for (String name : bindings.getBindingNames()) {
			String replacement = getReplacement(bindings.getValue(name));
			if (replacement != null) {
				String pattern = ".*[\\?\\$]" + name + "\\W.*";
				if (Pattern
						.compile(pattern, Pattern.MULTILINE | Pattern.DOTALL)
						.matcher(select).matches()) {
					names.add(name);
				}
			}
		}
		return names;
	}

	protected abstract String getAccept();

	private String getQueryString() {
		if (bindings.size() == 0)
			return query;
		String qry = query;
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
