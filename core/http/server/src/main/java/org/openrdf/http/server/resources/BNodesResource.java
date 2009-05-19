/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.helpers.ServerUtil;
import org.openrdf.http.server.resources.helpers.TupleResultResource;
import org.openrdf.model.BNode;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;

/**
 * @author Arjohn Kampman
 */
public class BNodesResource extends TupleResultResource {

	public BNodesResource(Context context, Request request, Response response) {
		super(context, request, response);
		setReadable(false);
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void acceptRepresentation(Representation representation)
		throws ResourceException
	{
		getResponse().setEntity(represent());
	}

	@Override
	public TupleResult getTupleResult()
		throws ResourceException
	{
		Form params = getQuery();
		int amount = ServerUtil.parseIntegerParam(params, Protocol.AMOUNT, 1);
		String nodeID = params.getFirstValue(Protocol.NODE_ID);

		ValueFactory vf = getConnection().getValueFactory();

		List<String> columns = Arrays.asList(Protocol.BNODE);
		List<BindingSet> bnodes = new ArrayList<BindingSet>(amount);
		for (int i = 0; i < amount; i++) {
			BNode bnode = createBNode(vf, nodeID, i);
			bnodes.add(new ListBindingSet(columns, bnode));
		}

		return new TupleResultImpl(columns, bnodes);
	}

	private BNode createBNode(ValueFactory vf, String nodeID, int i) {
		if (i == 0 && nodeID != null) {
			return vf.createBNode(nodeID);
		}
		return vf.createBNode();
	}

	@Override
	protected String getFilenamePrefix() {
		return "bnodes";
	}
}
