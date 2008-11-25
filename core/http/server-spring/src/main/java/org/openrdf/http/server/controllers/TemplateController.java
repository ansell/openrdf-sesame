/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.server.repository.RepositoryInterceptor.getRepositoryManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;

/**
 * Handles requests for repository configuration templates.
 * 
 * @author James Leigh
 */
@Controller
public class TemplateController {

	@ModelAttribute
	@RequestMapping(method = RequestMethod.GET, value = "/templates")
	public TupleQueryResult list(HttpServletRequest request)
		throws StoreConfigException
	{
		List<String> columnNames = Arrays.asList("id");
		List<BindingSet> ids = new ArrayList<BindingSet>();

		RepositoryManager manager = getRepositoryManager(request);
		for (String id : manager.getConfigTemplateIDs()) {
			ids.add(new ListBindingSet(columnNames, new LiteralImpl(id)));
		}

		return new TupleQueryResultImpl(columnNames, ids);
	}

	@ModelAttribute
	@RequestMapping(method = RequestMethod.GET, value = "/templates/*")
	public Model get(HttpServletRequest request)
		throws StoreConfigException, ClientHTTPException
	{
		String id = getPathParam(request);
		RepositoryManager manager = getRepositoryManager(request);
		return manager.getConfigTemplate(id).getModel();
	}

	private String getPathParam(HttpServletRequest request) {
		String pathInfoStr = request.getPathInfo();
		String[] pathInfo = pathInfoStr.substring(1).split("/");
		return pathInfo[pathInfo.length - 1];
	}
}
