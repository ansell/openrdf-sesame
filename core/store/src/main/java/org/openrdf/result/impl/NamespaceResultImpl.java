/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.cursor.ConvertingCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.IteratorCursor;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.result.NamespaceResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class NamespaceResultImpl extends ResultImpl<Namespace> implements NamespaceResult {

	public NamespaceResultImpl(Map<String, String> namespaces) {
		this(new ConvertingCursor<Map.Entry<String, String>, Namespace>(
				new IteratorCursor<Map.Entry<String, String>>(namespaces.entrySet().iterator()))
		{
			@Override
			protected Namespace convert(Entry<String, String> e)
				throws StoreException
			{
				return new NamespaceImpl(e.getKey(), e.getValue());
			}
		});
	}

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
