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
package org.openrdf.http.client.query;

import java.util.Iterator;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.AbstractQuery;


/**
 * Base class for any {@link Query} operation of the HTTP Repository
 * and SPARQL Repository.
 * 
 * @author Andreas Schwarte
 */
public class AbstractHTTPQuery extends AbstractQuery {

	
	private final HTTPClient httpClient;

	protected final QueryLanguage queryLanguage;

	protected final String queryString;

	protected final String baseURI;

	
	/**
	 * @param httpClient
	 * @param queryLanguage
	 * @param queryString
	 * @param baseURI
	 */
	public AbstractHTTPQuery(HTTPClient httpClient, QueryLanguage queryLanguage, String queryString, String baseURI) {
		super();
		this.httpClient = httpClient;
		this.queryLanguage = queryLanguage;
		this.queryString = queryString;
		this.baseURI = baseURI;
	}
	
	/**
	 * Return the {@link HTTPClient} to be used for all HTTP based interaction
	 * @return
	 */
	protected HTTPClient getHttpClient() {
		return httpClient;
	}
		
	protected Binding[] getBindingsArray() {
		BindingSet bindings = this.getBindings();

		Binding[] bindingsArray = new Binding[bindings.size()];

		Iterator<Binding> iter = bindings.iterator();
		for (int i = 0; i < bindings.size(); i++) {
			bindingsArray[i] = iter.next();
		}

		return bindingsArray;
	}
	
	@Override
	public void setMaxQueryTime(int maxQueryTime) {
		super.setMaxQueryTime(maxQueryTime);
		this.httpClient.setConnectionTimeout(1000L * this.maxQueryTime);
	}
	
	@Override
	public String toString() {
		return queryString;
	}
}
