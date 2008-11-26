/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import info.aduna.io.IOUtil;

import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.store.StoreException;

/**
 * Handles requests for manipulating a specific namespace definition in a
 * repository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
@Controller
public class NamespaceController {

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = "/repositories/*/namespaces")
	public TupleQueryResult list(HttpServletRequest request)
		throws StoreException
	{
		List<String> columnNames = Arrays.asList("prefix", "namespace");
		List<BindingSet> namespaces = new ArrayList<BindingSet>();

		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		RepositoryResult<Namespace> iter = repositoryCon.getNamespaces();

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

		return new TupleQueryResultImpl(columnNames, namespaces);
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = "/repositories/*/namespaces")
	public void clear(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		repositoryCon.clearNamespaces();
	}

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = "/repositories/*/namespaces/*")
	public StringReader get(HttpServletRequest request)
		throws StoreException, NotFound
	{
		String prefix = getPrefix(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		String namespace = repositoryCon.getNamespace(prefix);

		if (namespace == null) {
			throw new NotFound("Undefined prefix: " + prefix);
		}

		return new StringReader(namespace);
	}

	@ModelAttribute
	@RequestMapping(method = PUT, value = "/repositories/*/namespaces/*")
	public void put(HttpServletRequest request)
		throws StoreException, IOException, BadRequest
	{
		String prefix = getPrefix(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		String namespace = IOUtil.readString(request.getReader());
		namespace = namespace.trim();

		if (namespace.length() == 0) {
			throw new BadRequest("No namespace name found in request body");
		}
		// FIXME: perform some sanity checks on the namespace string

		repositoryCon.setNamespace(prefix, namespace);
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = "/repositories/*/namespaces/*")
	public void delete(HttpServletRequest request)
		throws StoreException
	{
		String prefix = getPrefix(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		repositoryCon.removeNamespace(prefix);
	}

	private String getPrefix(HttpServletRequest request) {
		String pathInfoStr = request.getPathInfo();
		String[] pathInfo = pathInfoStr.substring(1).split("/");
		return pathInfo[pathInfo.length - 1];
	}
}
