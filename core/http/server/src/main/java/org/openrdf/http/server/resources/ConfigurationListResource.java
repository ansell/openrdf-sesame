/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.resource.ResourceException;

import org.openrdf.http.server.resources.helpers.TupleResultResource;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.store.StoreConfigException;

/**
 * @author Arjohn Kampman
 */
public class ConfigurationListResource extends TupleResultResource {

	@Override
	public TupleResult getTupleResult()
		throws ResourceException
	{
		try {
			List<String> columnNames = Arrays.asList("id");
			List<BindingSet> ids = new ArrayList<BindingSet>();

			for (String id : getRepositoryManager().getRepositoryIDs()) {
				ids.add(new ListBindingSet(columnNames, new LiteralImpl(id)));
			}

			return new TupleResultImpl(columnNames, ids);
		}
		catch (StoreConfigException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return "configurations";
	}
}
