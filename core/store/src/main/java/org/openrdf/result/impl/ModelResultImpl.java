/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import java.util.Collections;
import java.util.Map;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.result.ModelResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class ModelResultImpl extends ResultImpl<Statement> implements ModelResult {

	public ModelResultImpl(Cursor<? extends Statement> delegate) {
		super(delegate);
	}

	public Map<String, String> getNamespaces()
		throws StoreException
	{
		return Collections.emptyMap();
	}

	public Model asModel()
		throws StoreException
	{
		return addTo(new LinkedHashModel(getNamespaces()));
	}
}
