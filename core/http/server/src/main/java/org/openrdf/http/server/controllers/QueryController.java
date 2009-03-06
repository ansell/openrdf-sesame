/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.BINDINGS_QUERY;
import static org.openrdf.http.protocol.Protocol.BOOLEAN_QUERY;
import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.protocol.Protocol.GRAPH_QUERY;
import static org.openrdf.http.protocol.Protocol.X_QUERY_TYPE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.cursor.EmptyCursor;
import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NotImplemented;
import org.openrdf.http.server.helpers.QueryBuilder;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;
import org.openrdf.result.Result;
import org.openrdf.result.impl.BooleanResultImpl;
import org.openrdf.result.impl.GraphResultImpl;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
@Controller
public class QueryController {

	@ModelAttribute
	@RequestMapping(method = HEAD, value = CONN_PATH + "/queries")
	public void type(HttpServletRequest req, HttpServletResponse resp)
		throws StoreException, HTTPException, IOException, MalformedQueryException
	{
		Query query = new QueryBuilder(req).prepareQuery();
		setQueryTypeHeader(query, resp);
	}

	@RequestMapping(method = POST, value = CONN_PATH + "/queries")
	public void prepare(HttpServletRequest req, HttpServletResponse resp)
		throws StoreException, HTTPException, IOException, MalformedQueryException
	{
		Query query = new QueryBuilder(req).prepareQuery();
		setQueryTypeHeader(query, resp);

		// Store the query for later reference
		String id = RepositoryInterceptor.saveQuery(req, query);

		StringBuffer url = req.getRequestURL();
		String location = url.append("/").append(id).toString();
		resp.setStatus(HttpServletResponse.SC_CREATED);
		resp.setHeader("Location", location);
	}

	private void setQueryTypeHeader(Query query, HttpServletResponse resp)
		throws NotImplemented
	{
		if (query instanceof TupleQuery) {
			resp.setHeader(X_QUERY_TYPE, BINDINGS_QUERY);
		}
		else if (query instanceof GraphQuery) {
			resp.setHeader(X_QUERY_TYPE, GRAPH_QUERY);
		}
		else if (query instanceof BooleanQuery) {
			resp.setHeader(X_QUERY_TYPE, BOOLEAN_QUERY);
		}
		else {
			throw new NotImplemented("Unsupported query type: " + query.getClass().getName());
		}
	}

	@ModelAttribute
	@RequestMapping(method = HEAD, value = CONN_PATH + "/queries/*")
	public Result<?> head(HttpServletRequest request)
		throws StoreException, BadRequest, HTTPException, IOException
	{
		String queryID = HttpServerUtil.getLastPathSegment(request);
		Query query = RepositoryInterceptor.getQuery(request, queryID);

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
			return new BooleanResultImpl(new EmptyCursor<Boolean>());
		}
		else {
			throw new NotImplemented("Unsupported query type: " + query.getClass().getName());
		}
	}

	@ModelAttribute
	@RequestMapping(method = { GET, POST }, value = CONN_PATH + "/queries/*")
	public Result<?> get(HttpServletRequest request)
		throws StoreException, BadRequest, HTTPException, IOException
	{
		String queryID = HttpServerUtil.getLastPathSegment(request);
		Query query = RepositoryInterceptor.getQuery(request, queryID);

		synchronized (query) {
			new QueryBuilder(request).prepareQuery(query);
			return query.evaluate();
		}
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = CONN_PATH + "/queries/*")
	public void delete(HttpServletRequest request)
		throws StoreException
	{
		String queryID = HttpServerUtil.getLastPathSegment(request);
		RepositoryInterceptor.deleteQuery(request, queryID);
	}
}
