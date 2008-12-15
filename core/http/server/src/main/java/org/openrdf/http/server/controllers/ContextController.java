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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleResult;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleResultImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
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
	public TupleResult head(HttpServletRequest request) {
		List<String> columnNames = Arrays.asList("contextID");
		List<BindingSet> contexts = new ArrayList<BindingSet>();

		return new TupleResultImpl(columnNames, contexts);
	}

	@ModelAttribute
	@RequestMapping(method = GET, value = { REPO_PATH + "/contexts", CONN_PATH + "/contexts" })
	public TupleResult get(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);

		List<String> columnNames = Arrays.asList("contextID");
		List<BindingSet> contexts = new ArrayList<BindingSet>();
		RepositoryResult<Resource> contextIter = repositoryCon.getContextIDs();

		try {
			while (contextIter.hasNext()) {
				BindingSet bindingSet = new ListBindingSet(columnNames, contextIter.next());
				contexts.add(bindingSet);
			}
		}
		finally {
			contextIter.close();
		}

		return new TupleResultImpl(columnNames, contexts);
	}
}
