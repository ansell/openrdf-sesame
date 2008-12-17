/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.protocol.Protocol.REPO_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.cursor.EmptyCursor;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.ContextResult;
import org.openrdf.result.impl.ContextResultImpl;
import org.openrdf.store.StoreException;

/**
 * Handles requests for the list of contexts in a repository.
 * 
 * @author Herko ter Horst
 * @author James Leigh
 */
@Controller
public class ContextController {

	@ModelAttribute
	@RequestMapping(method = HEAD, value = { REPO_PATH + "/contexts", CONN_PATH + "/contexts" })
	public ContextResult head(HttpServletRequest request) {
		return new ContextResultImpl(new EmptyCursor<Resource>());
	}

	@ModelAttribute
	@RequestMapping(method = GET, value = { REPO_PATH + "/contexts", CONN_PATH + "/contexts" })
	public ContextResult get(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		return repositoryCon.getContextIDs();
	}
}
