/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sparql.query;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import org.openrdf.model.URI;
import org.openrdf.query.Dataset;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.sparql.SPARQLConnection;

/**
 * Provides an execution thread for background result parsing and inlines
 * binding in a SPARQL query.
 * 
 * @author James Leigh
 * 
 */
public abstract class SPARQLQuery extends SPARQLOperation implements Query {
	
	private int maxQueryTime = 0;

	/**
	 * @param client
	 * @param url
	 * @param base
	 * @param operation
	 */
	public SPARQLQuery(HttpClient client, String url, String base, String operation) {
		super(client, url, base, operation);
	}

	public int getMaxQueryTime() {
		return maxQueryTime; 
	}

	public void setMaxQueryTime(int maxQueryTime) {
		this.maxQueryTime = maxQueryTime;
		this.client.getParams().setConnectionManagerTimeout(1000L * maxQueryTime);
	}

	protected abstract String getAccept();
	
	protected HttpMethodBase getResponse()
			throws HttpException, IOException, QueryEvaluationException
		{
			PostMethod post = new PostMethod(getUrl());
			// We need to encode our data in utf-8 as that allows internationalized
			// queries.
			post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
			post.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET, "utf-8");
			post.addParameter("query", getQueryString());
			
			Dataset dataset = getDataset();
			if (dataset != null) {
				for (URI graph : dataset.getDefaultGraphs()) {
					post.addParameter("default-graph-uri", String.valueOf(graph));
				}
				for (URI graph : dataset.getNamedGraphs()) {
					post.addParameter("named-graph-uri", String.valueOf(graph));
				}
			}
			post.addRequestHeader("Accept", getAccept());
			Map<String, String> additionalHeaders = (Map<String, String>)client.getParams().getParameter(
					SPARQLConnection.ADDITIONAL_HEADER_NAME);
			if (additionalHeaders != null) {
				for (Entry<String, String> additionalHeader : additionalHeaders.entrySet())
					post.addRequestHeader(additionalHeader.getKey(), additionalHeader.getValue());
			}
			boolean completed = false;
			try {
				if (client.executeMethod(post) >= 400) {
					throw new QueryEvaluationException(post.getResponseBodyAsString());
				}
				completed = true;
				return post;
			}
			finally {
				if (!completed) {
					post.abort();
					post.releaseConnection();
				}
			}
		}
}
