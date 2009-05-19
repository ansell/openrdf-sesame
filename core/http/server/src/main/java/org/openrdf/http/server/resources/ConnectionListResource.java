/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import java.util.Arrays;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.resources.helpers.TupleResultResource;
import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.MutableTupleResult;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class ConnectionListResource extends TupleResultResource {

	public ConnectionListResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	public TupleResult getTupleResult()
		throws ResourceException
	{
		List<String> bindingNames = Arrays.asList("id");
		MutableTupleResult result = new MutableTupleResult(bindingNames);

		ValueFactory vf = ValueFactoryImpl.getInstance();

		for (String connectionID : getRepository().getConnectionIDs()) {
			Literal idLit = vf.createLiteral(connectionID);
			result.append(new ListBindingSet(bindingNames, idLit));
		}

		return result;
	}

	@Override
	protected String getFilenamePrefix() {
		return "connections";
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	public void acceptRepresentation(Representation entity)
		throws ResourceException
	{
		try {
			String connectionID = getRepository().getConnection().getID();

			// String redirectURL = getRequest().getResourceRef().toString(false,
			// false);
			// if (!redirectURL.endsWith("/")) {
			// redirectURL += "/";
			// }
			// redirectURL += connectionID;

			Reference connectionRef = getRequest().getResourceRef().clone();
			connectionRef.addSegment(connectionID);
			getResponse().setLocationRef(connectionRef);

			getResponse().setStatus(Status.SUCCESS_CREATED);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}
}
