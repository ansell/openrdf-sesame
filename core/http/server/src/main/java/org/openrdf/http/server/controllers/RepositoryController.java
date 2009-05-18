/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.server.helpers.Paths;
import org.openrdf.http.server.helpers.QueryBuilder;
import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.result.Result;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.MutableTupleResult;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Handles queries on a repository and renders the results in a format suitable
 * to the type of query.
 * 
 * @author Herko ter Horst
 * @author James Leigh
 */
@Controller
public class RepositoryController {

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = Paths.REPOSITORIES)
	public TupleResult list(HttpServletRequest request)
		throws HTTPException, StoreConfigException
	{
		MutableTupleResult result = new MutableTupleResult(Arrays.asList("uri", "id", "title"));

		// Determine the repository's URI
		String namespace = request.getRequestURL().append('/').toString();

		ValueFactory vf = ValueFactoryImpl.getInstance();

		RepositoryManager repositoryManager = RequestAtt.getRepositoryManager(request);
		for (RepositoryInfo info : repositoryManager.getAllRepositoryInfos()) {
			String id = info.getId();

			MapBindingSet bindings = new MapBindingSet(3);
			bindings.addBinding("uri", vf.createURI(namespace, id));
			bindings.addBinding("id", vf.createLiteral(id));

			if (info.getDescription() != null) {
				bindings.addBinding("title", vf.createLiteral(info.getDescription()));
			}

			result.append(bindings);
		}

		return result;
	}

	@ModelAttribute
	@RequestMapping(method = HEAD, value = { Paths.REPOSITORY_ID })
	public Result<?> head(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException, IOException, StoreException, MalformedQueryException
	{
		return new QueryBuilder(request).getDummyResult();
	}

	@ModelAttribute
	@RequestMapping(method = { GET, POST }, value = { Paths.REPOSITORY_ID })
	public Result<?> query(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException, IOException, StoreException, MalformedQueryException
	{
		return new QueryBuilder(request).prepareQuery().evaluate();
	}
}
