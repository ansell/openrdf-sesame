/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.server.repository.RepositoryInterceptor.getModifyingConnection;
import static org.openrdf.http.server.repository.RepositoryInterceptor.getReadOnlyConnection;
import static org.openrdf.http.server.repository.RepositoryInterceptor.notSafe;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.StringReader;

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
		notSafe(request);
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		repositoryCon.begin();
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = CONN_PATH + "/ping")
	public StringReader ping(HttpServletRequest request)
		throws StoreException
	{
		notSafe(request);
		return new StringReader("pong");
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = CONN_PATH + "/commit")
	public void commit(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getModifyingConnection(request);
		repositoryCon.commit();
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = CONN_PATH + "/rollback")
	public void rollback(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getReadOnlyConnection(request);
		repositoryCon.rollback();
	}
}
