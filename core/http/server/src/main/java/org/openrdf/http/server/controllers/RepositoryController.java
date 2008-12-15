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
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NotImplemented;
import org.openrdf.http.server.BooleanQueryResult;
import org.openrdf.http.server.helpers.QueryBuilder;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.Result;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleResult;
import org.openrdf.query.impl.GraphResultImpl;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleResultImpl;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
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
	@RequestMapping(method = { GET, HEAD }, value = "/repositories")
	public TupleResult list(HttpServletRequest request)
		throws HTTPException, StoreConfigException
	{
		List<String> bindingNames = Arrays.asList("uri", "id", "title", "readable", "writable");
		List<BindingSet> bindingSets = new ArrayList<BindingSet>();

		// Determine the repository's URI
		StringBuffer requestURL = request.getRequestURL();
		if (requestURL.charAt(requestURL.length() - 1) != '/') {
			requestURL.append('/');
		}
		String namespace = requestURL.toString();

		ValueFactory vf = new ValueFactoryImpl();
		RepositoryManager repositoryManager = RepositoryInterceptor.getReadOnlyManager(request);
		for (RepositoryInfo info : repositoryManager.getAllRepositoryInfos()) {
			String id = info.getId();
			URI uri = vf.createURI(namespace, id);
			Literal idLit = vf.createLiteral(id);
			Literal title = vf.createLiteral(info.getDescription());
			Literal readable = vf.createLiteral(info.isReadable());
			Literal writable = vf.createLiteral(info.isWritable());

			BindingSet bindings = new ListBindingSet(bindingNames, uri, idLit, title, readable, writable);
			bindingSets.add(bindings);
		}

		return new TupleResultImpl(bindingNames, bindingSets);
	}

	@RequestMapping(method = POST, value = REPO_PATH + "/connections")
	public void post(HttpServletRequest request, HttpServletResponse response)
		throws StoreException
	{
		String id = RepositoryInterceptor.createConnection(request);
		StringBuffer url = request.getRequestURL();
		String location = url.append("/").append(id).toString();
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader("Location", location);
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = CONN_PATH)
	public void delete(HttpServletRequest request)
		throws StoreException
	{
		RepositoryInterceptor.closeConnection(request);
	}

	@ModelAttribute
	@RequestMapping(method = HEAD, value = { REPO_PATH, CONN_PATH })
	public Result<?> head(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException, IOException, StoreException, MalformedQueryException
	{
		Query query = new QueryBuilder(request).prepareQuery();
		if (query instanceof TupleQuery) {
			List<String> names = Collections.emptyList();
			Set<BindingSet> bindings = Collections.emptySet();
			return new TupleResultImpl(names, bindings);
		}
		else if (query instanceof GraphQuery) {
			Map<String, String> namespaces = Collections.emptyMap();
			Set<Statement> statements = Collections.emptySet();
			return new GraphResultImpl(namespaces, statements);
		}
		else if (query instanceof BooleanQuery) {
			// @ModelAttribute does not support a return type of boolean
			return BooleanQueryResult.EMPTY;
		}
		else {
			throw new NotImplemented("Unsupported query type: " + query.getClass().getName());
		}
	}

	@ModelAttribute
	@RequestMapping(method = { GET, POST }, value = { REPO_PATH, CONN_PATH })
	public Result<?> query(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException, IOException, StoreException, MalformedQueryException
	{
		Query query = new QueryBuilder(request).prepareQuery();
		if (query instanceof TupleQuery) {
			TupleQuery tQuery = (TupleQuery)query;
			return tQuery.evaluate();
		}
		else if (query instanceof GraphQuery) {
			GraphQuery gQuery = (GraphQuery)query;
			return gQuery.evaluate();
		}
		else if (query instanceof BooleanQuery) {
			BooleanQuery bQuery = (BooleanQuery)query;
			// @ModelAttribute does not support a return type of boolean
			return new BooleanQueryResult(bQuery.evaluate());
		}
		else {
			throw new NotImplemented("Unsupported query type: " + query.getClass().getName());
		}
	}
}
