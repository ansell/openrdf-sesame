/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
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

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.sparql.SPARQLConnection;


/**
 *
 * @author jeen
 */
public class SPARQLUpdate extends SPARQLOperation implements Update {

	/**
	 * @param client
	 * @param url
	 * @param base
	 * @param operation
	 */
	public SPARQLUpdate(HttpClient client, String url, String base, String operation) {
		super(client, url, base, operation);
		// TODO Auto-generated constructor stub
	}

	public void execute()
		throws UpdateExecutionException
	{
		
		// TODO Auto-generated method stub
		
	}

	protected HttpMethodBase getResponse()
			throws HttpException, IOException, QueryEvaluationException
		{
			PostMethod post = new PostMethod(getUrl());
			post.addParameter("update", getQueryString());
			
			// TODO is setting parameters like this part of SPARQL protocol?
			Dataset dataset = getDataset();
			if (dataset != null) {
				for (URI graph : dataset.getDefaultGraphs()) {
					post.addParameter("default-graph-uri", String.valueOf(graph));
				}
				for (URI graph : dataset.getNamedGraphs()) {
					post.addParameter("named-graph-uri", String.valueOf(graph));
				}
			}
			
			// TODO check correct mime type and header name
			post.addRequestHeader("Content-type", "application/x-sparql-update");
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
				}
			}
		}

}
