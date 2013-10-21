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

import org.openrdf.http.client.SparqlSession;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.impl.AbstractUpdate;


/**
 * Base class for any {@link Update} operation over HTTP.
 * 
 * @author Andreas Schwarte
 */
public abstract class AbstractHTTPUpdate extends AbstractUpdate {

	
	private final SparqlSession httpClient;

	protected final QueryLanguage queryLanguage;

	protected final String queryString;

	protected final String baseURI;

	
	/**
	 * @param httpClient
	 * @param queryLanguage
	 * @param queryString
	 * @param baseURI
	 */
	public AbstractHTTPUpdate(SparqlSession httpClient, QueryLanguage queryLanguage, String queryString, String baseURI) {
		super();
		this.httpClient = httpClient;
		this.queryLanguage = queryLanguage;
		this.queryString = queryString;
		this.baseURI = baseURI;
	}
	
	/**
	 * Return the {@link SparqlSession} to be used for all HTTP based interaction
	 * @return
	 */
	protected SparqlSession getHttpClient() {
		return httpClient;
	}
		
	public Binding[] getBindingsArray() {
		BindingSet bindings = this.getBindings();

		Binding[] bindingsArray = new Binding[bindings.size()];

		Iterator<Binding> iter = bindings.iterator();
		for (int i = 0; i < bindings.size(); i++) {
			bindingsArray[i] = iter.next();
		}

		return bindingsArray;
	}
	
	/**
	 * @return Returns the baseURI.
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @return Returns the queryLanguage.
	 */
	public QueryLanguage getQueryLanguage() {
		return queryLanguage;
	}

	/**
	 * @return Returns the queryString.
	 */
	public String getQueryString() {
		return queryString;
	}
	
	@Override
	public String toString() {
		return queryString;
	}
}
