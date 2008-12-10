/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.server.repository.RepositoryInterceptor.getReadOnlyConnection;
import static org.openrdf.http.server.repository.RepositoryInterceptor.getRepositoryConnection;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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
	@RequestMapping(method = POST, value = CONN_PATH + "/begin")
	public void begin(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		repositoryCon.setAutoCommit(false);
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = CONN_PATH + "/commit")
	public void commit(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getRepositoryConnection(request);
		repositoryCon.commit();
		repositoryCon.setAutoCommit(true);
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = CONN_PATH + "/rollback")
	public void rollback(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		repositoryCon.rollback();
		repositoryCon.setAutoCommit(true);
	}
}
