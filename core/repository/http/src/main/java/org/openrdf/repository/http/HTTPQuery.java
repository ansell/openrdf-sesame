/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.util.Iterator;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.AbstractQuery;

/**
 * @author Arjohn Kampman
 */
public abstract class HTTPQuery extends AbstractQuery {

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

	@Override
	public String toString()
	{
		return queryString;
	}
}
