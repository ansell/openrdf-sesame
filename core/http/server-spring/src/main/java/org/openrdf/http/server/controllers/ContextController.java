/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.repository.RepositoryConnection;
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
	@RequestMapping(value = "/repositories/*/contexts", method = RequestMethod.GET)
	public TupleQueryResult list(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		List<String> columnNames = Arrays.asList("contextID");
		List<BindingSet> contexts = new ArrayList<BindingSet>();
		CloseableIteration<? extends Resource, StoreException> contextIter = repositoryCon.getContextIDs();

		try {
			while (contextIter.hasNext()) {
				BindingSet bindingSet = new ListBindingSet(columnNames, contextIter.next());
				contexts.add(bindingSet);
			}
		}
		finally {
			contextIter.close();
		}

		return new TupleQueryResultImpl(columnNames, contexts);
	}
}
