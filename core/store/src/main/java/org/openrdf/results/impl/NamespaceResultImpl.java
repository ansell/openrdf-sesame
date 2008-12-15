/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.results.impl;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.Namespace;
import org.openrdf.results.Cursor;
import org.openrdf.results.NamespaceResult;
import org.openrdf.results.base.ResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class NamespaceResultImpl extends ResultImpl<Namespace> implements NamespaceResult {

	public NamespaceResultImpl(Cursor<? extends Namespace> delegate) {
		super(delegate);
	}

	public Map<String, String> asMap()
		throws StoreException
	{
		Map<String, String> map = new HashMap<String, String>();
		Namespace ns;
		while ((ns = next()) != null) {
			map.put(ns.getPrefix(), ns.getName());
		}
		return map;
	}

}
