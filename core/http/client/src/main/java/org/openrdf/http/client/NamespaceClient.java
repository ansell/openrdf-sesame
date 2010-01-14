/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.cursor.ConvertingCursor;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.StoreClient;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.result.NamespaceResult;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.NamespaceResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class NamespaceClient {

	private final StoreClient client;

	public NamespaceClient(HTTPConnectionPool pool) {
		this.client = new StoreClient(pool);
	}

	public int getMaxAge() {
		return client.getMaxAge();
	}

	public String getETag() {
		return client.getETag();
	}

	public void ifNoneMatch(String eTag) {
		client.ifNoneMatch(eTag);
	}

	public NamespaceResult list()
		throws StoreException
	{
		TupleResult result = client.list();
		if (result == null) {
			return null;
		}
		return new NamespaceResultImpl(new ConvertingCursor<BindingSet, Namespace>(result) {

			@Override
			protected Namespace convert(BindingSet bindings)
				throws StoreException
			{
				String prefix = bindings.getValue(Protocol.PREFIX).stringValue();
				String name = bindings.getValue(Protocol.NAMESPACE).stringValue();
				return new NamespaceImpl(prefix, name);
			}
		});
	}

	public String get(String prefix)
		throws StoreException
	{
		return client.get(prefix, String.class);
	}

	public void put(String prefix, String name)
		throws StoreException
	{
		client.put(prefix, name);
	}

	public void delete(String prefix)
		throws StoreException
	{
		client.delete(prefix);
	}

	public void delete()
		throws StoreException
	{
		client.delete();
	}
}
