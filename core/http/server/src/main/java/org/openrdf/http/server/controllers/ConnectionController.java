/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static info.aduna.net.http.ResponseHeaders.LOCATION;
import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static org.openrdf.http.server.interceptors.RepositoryInterceptor.getModifyingConnection;
import static org.openrdf.http.server.interceptors.RepositoryInterceptor.getReadOnlyConnection;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.server.helpers.Paths;
import org.openrdf.http.server.helpers.QueryBuilder;
import org.openrdf.http.server.interceptors.ConditionalRequestInterceptor;
import org.openrdf.http.server.interceptors.RepositoryInterceptor;
import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.result.Result;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Manages the Repository Connection over HTTP.
 * 
 * @author James Leigh
 */
@Controller
public class ConnectionController {

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = Paths.CONNECTIONS)
	public TupleResult listConnections(HttpServletRequest request)
		throws HTTPException, StoreConfigException
	{
		List<String> bindingNames = Arrays.asList("method", "url");
		List<BindingSet> bindingSets = new ArrayList<BindingSet>();

		ValueFactory vf = new ValueFactoryImpl();
		Collection<String> requests = RepositoryInterceptor.getActiveRequests(request);
		for (String req : requests) {
			String[] split = req.split(" ", 2);
			Literal method = vf.createLiteral(split[0]);
			Literal url = vf.createLiteral(split[1]);

			bindingSets.add(new ListBindingSet(bindingNames, method, url));
		}

		return new TupleResultImpl(bindingNames, bindingSets);
	}

	@RequestMapping(method = POST, value = Paths.CONNECTIONS)
	public void post(HttpServletRequest request, HttpServletResponse response)
		throws StoreException
	{
		String id = RepositoryInterceptor.createConnection(request);
		String location = request.getRequestURL().append('/').append(id).toString();
		response.setStatus(SC_CREATED);
		response.setHeader(LOCATION, location);
	}

	@ModelAttribute
	@RequestMapping(method = HEAD, value = { Paths.CONNECTION_ID })
	public Result<?> head(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException, IOException, StoreException, MalformedQueryException
	{
		return new QueryBuilder(request).getDummyResult();
	}

	@ModelAttribute
	@RequestMapping(method = { GET, POST }, value = { Paths.CONNECTION_ID })
	public Result<?> query(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException, IOException, StoreException, MalformedQueryException
	{
		return new QueryBuilder(request).prepareQuery().evaluate();
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = Paths.CONNECTION_ID)
	public void delete(HttpServletRequest request)
		throws StoreException
	{
		RepositoryInterceptor.closeConnection(request);
	}

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
	@RequestMapping(method = POST, value = Paths.CONNECTION_COMMIT)
	public void commit(HttpServletRequest request)
		throws StoreException
	{
		RepositoryConnection repositoryCon = getModifyingConnection(request);
		// FIXME: should this generate an error if no txn has been started yet?
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
		// FIXME: should this generate an error if no txn has been started yet?
		if (!repositoryCon.isAutoCommit()) {
			repositoryCon.rollback();
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
}
