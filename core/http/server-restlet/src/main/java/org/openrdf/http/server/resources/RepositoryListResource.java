/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import java.util.Arrays;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.resources.helpers.TupleResultResource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.MutableTupleResult;
import org.openrdf.store.StoreConfigException;

/**
 * @author Arjohn Kampman
 */
public class RepositoryListResource extends TupleResultResource {

	public RepositoryListResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public TupleResult getTupleResult()
		throws ResourceException
	{
		try {
			MutableTupleResult result = new MutableTupleResult(Arrays.asList("uri", "id", "title"));

			// Determine the repository's URI
			String namespace = getRequest().getResourceRef().toString(false, false);
			if (!namespace.endsWith("/")) {
				namespace += "/";
			}

			ValueFactory vf = ValueFactoryImpl.getInstance();

			for (RepositoryInfo info : getRepositoryManager().getAllRepositoryInfos()) {
				String id = info.getId();

				MapBindingSet bindings = new MapBindingSet(3);
				bindings.addBinding("uri", vf.createURI(namespace, id));
				bindings.addBinding("id", vf.createLiteral(id));

				if (info.getDescription() != null) {
					bindings.addBinding("title", vf.createLiteral(info.getDescription()));
				}

				result.append(bindings);
			}

			return result;
		}
		catch (StoreConfigException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return "repositories";
	}
}
