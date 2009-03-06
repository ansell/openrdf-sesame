/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.protocol.Protocol.REPO_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import info.aduna.io.IOUtil;
import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.NamespaceResult;
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
	@RequestMapping(method = { GET, HEAD }, value = { REPO_PATH + "/namespaces", CONN_PATH + "/namespaces" })
	public NamespaceResult list(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		return repositoryCon.getNamespaces();
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = { REPO_PATH + "/namespaces", CONN_PATH + "/namespaces" })
	public void clear(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		repositoryCon.clearNamespaces();
	}

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = { REPO_PATH + "/namespaces/*", CONN_PATH + "/namespaces/*" })
	public StringReader get(HttpServletRequest request)
		throws StoreException, NotFound
	{
		String prefix = HttpServerUtil.getLastPathSegment(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		String namespace = repositoryCon.getNamespace(prefix);

		if (namespace == null) {
			throw new NotFound("Undefined prefix: " + prefix);
		}

		return new StringReader(namespace);
	}

	@ModelAttribute
	@RequestMapping(method = PUT, value = { REPO_PATH + "/namespaces/*", CONN_PATH + "/namespaces/*" })
	public void put(HttpServletRequest request)
		throws StoreException, IOException, BadRequest
	{
		String prefix = HttpServerUtil.getLastPathSegment(request);
		String namespace = IOUtil.readString(request.getReader());
		namespace = namespace.trim();

		if (namespace.length() == 0) {
			throw new BadRequest("No namespace name found in request body");
		}
		// FIXME: perform some sanity checks on the namespace string

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		repositoryCon.setNamespace(prefix, namespace);
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = { REPO_PATH + "/namespaces/*", CONN_PATH + "/namespaces/*" })
	public void delete(HttpServletRequest request)
		throws StoreException
	{
		String prefix = HttpServerUtil.getLastPathSegment(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		repositoryCon.removeNamespace(prefix);
	}
}
