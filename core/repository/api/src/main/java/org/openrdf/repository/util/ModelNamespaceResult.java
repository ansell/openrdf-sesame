/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.util;

import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.query.Cursor;
import org.openrdf.query.impl.ModelResultImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class ModelNamespaceResult extends ModelResultImpl {

	private RepositoryConnection con;

	public ModelNamespaceResult(RepositoryConnection con, Cursor<? extends Statement> c) {
		super(c);
		this.con = con;
	}

	@Override
	public Map<String, String> getNamespaces()
		throws StoreException
	{
		return con.getNamespaces().asMap();
	}

}
