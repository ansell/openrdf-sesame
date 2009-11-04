/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.resource.ResourceException;

import org.openrdf.http.server.resources.helpers.TupleResultResource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.result.ContextResult;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class ContextsResource extends TupleResultResource {

	@Override
	public TupleResult getTupleResult()
		throws ResourceException
	{
		try {
			List<String> columnNames = Arrays.asList("contextID");
			List<BindingSet> contexts = new ArrayList<BindingSet>();

			ContextResult iter = getConnection().getContextIDs();

			try {
				while (iter.hasNext()) {
					contexts.add(new ListBindingSet(columnNames, iter.next()));
				}
			}
			finally {
				iter.close();
			}

			return new TupleResultImpl(columnNames, contexts);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return "contexts";
	}
}
