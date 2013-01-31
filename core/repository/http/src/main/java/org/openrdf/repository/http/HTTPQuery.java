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
package org.openrdf.repository.http;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.AbstractQuery;

/**
 * A query to be evaluated over a HTTP connection with a remote repository.
 * 
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public abstract class HTTPQuery extends AbstractQuery {

	private static Executor executor = Executors.newFixedThreadPool(20);

	protected final HTTPRepositoryConnection httpCon;

	protected final QueryLanguage queryLanguage;

	protected final String queryString;

	protected final String baseURI;

	public HTTPQuery(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		this.httpCon = con;
		this.queryLanguage = ql;
		this.queryString = queryString;
		this.baseURI = baseURI;
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

	protected void execute(Runnable command) {
		executor.execute(command);
	}

	@Override
	public void setMaxQueryTime(int maxQueryTime) {
		super.setMaxQueryTime(maxQueryTime);
		this.httpCon.getRepository().getHTTPClient().setConnectionTimeout(1000L * this.maxQueryTime);
	}

	@Override
	public String toString() {
		return queryString;
	}
}
