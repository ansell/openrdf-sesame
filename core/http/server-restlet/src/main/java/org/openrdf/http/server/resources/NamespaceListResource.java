/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.restlet.data.Status.SUCCESS_NO_CONTENT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.resources.helpers.TupleResultResource;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.result.NamespaceResult;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class NamespaceListResource extends TupleResultResource {

	public NamespaceListResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	public TupleResult getTupleResult()
		throws ResourceException
	{
		try {
			List<String> columnNames = Arrays.asList("prefix", "namespace");
			List<BindingSet> namespaces = new ArrayList<BindingSet>();

			NamespaceResult iter = getConnection().getNamespaces();

			try {
				while (iter.hasNext()) {
					Namespace ns = iter.next();

					Literal prefix = new LiteralImpl(ns.getPrefix());
					Literal namespace = new LiteralImpl(ns.getName());

					BindingSet bindingSet = new ListBindingSet(columnNames, prefix, namespace);
					namespaces.add(bindingSet);
				}
			}
			finally {
				iter.close();
			}

			return new TupleResultImpl(columnNames, namespaces);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return "namespaces";
	}

	@Override
	public void removeRepresentations()
		throws ResourceException
	{
		try {
			getConnection().clearNamespaces();
			getResponse().setStatus(SUCCESS_NO_CONTENT);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}
}
