/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.cursor;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.cursor.CollectionCursor;
import org.openrdf.cursor.ConvertingCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.store.StoreException;

/**
 * {@link Namespace} typed {@link Cursor}.
 * 
 * @author James Leigh
 */
public class NamespaceCursor extends ConvertingCursor<Map.Entry<String, String>, Namespace> {

	public NamespaceCursor(Iterator<Map.Entry<String, String>> iter) {
		super(new CollectionCursor<Map.Entry<String, String>>(iter));
	}

	@Override
	protected Namespace convert(Entry<String, String> next)
		throws StoreException
	{
		return new NamespaceImpl(next.getKey(), next.getValue());
	}
}
