/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.server.interceptors.RepositoryInterceptor.getModifyingConnection;
import static org.openrdf.http.server.interceptors.RepositoryInterceptor.getReadOnlyConnection;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.server.helpers.Paths;
import org.openrdf.http.server.interceptors.ConditionalRequestInterceptor;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

/**
 * Manages the Repository Connection over HTTP.
 * 
 * @author James Leigh
 */
@Controller
public class ConnectionController {

	@ModelAttribute
	@RequestMapping(method = POST, value = Paths.CONNECTION_BEGIN)
	public void begin(HttpServletRequest request)
		throws StoreException
	{
		ConditionalRequestInterceptor.notSafe(request);
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		// FIXME: should this generate an error if txn has already started?
		if (repositoryCon.isAutoCommit()) {
			repositoryCon.begin();
		}
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = Paths.CONNECTION_PING)
	public StringReader ping(HttpServletRequest request)
		throws StoreException
	{
		ConditionalRequestInterceptor.notSafe(request);
		return new StringReader("pong");
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = Paths.CONNECTION_COMMIT)
	public void commit(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getModifyingConnection(request);
		// FIXME: should this generate an error no txn has been started yet?
		if (!repositoryCon.isAutoCommit()) {
			repositoryCon.commit();
		}
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = Paths.CONNECTION_ROLLBACK)
	public void rollback(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		// FIXME: should this generate an error no txn has been started yet?
		if (!repositoryCon.isAutoCommit()) {
			repositoryCon.rollback();
		}
	}
}
