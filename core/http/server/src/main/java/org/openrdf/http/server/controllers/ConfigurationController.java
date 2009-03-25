/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.ServerHTTPException;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.server.helpers.Paths;
import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.interceptors.ConditionalRequestInterceptor;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Handles requests for repository configurations.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
@Controller
public class ConfigurationController {

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = Paths.CONFIGURATIONS)
	public TupleResult list(HttpServletRequest request)
		throws StoreConfigException
	{
		List<String> columnNames = Arrays.asList("id");
		List<BindingSet> ids = new ArrayList<BindingSet>();

		RepositoryManager manager = RequestAtt.getRepositoryManager(request);
		for (String id : manager.getRepositoryIDs()) {
			ids.add(new ListBindingSet(columnNames, new LiteralImpl(id)));
		}

		return new TupleResultImpl(columnNames, ids);
	}

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = Paths.CONFIGURATION_ID)
	public Model get(HttpServletRequest request)
		throws StoreConfigException, ClientHTTPException
	{
		String id = getConfigID(request);
		RepositoryManager manager = RequestAtt.getRepositoryManager(request);
		Model config = manager.getRepositoryConfig(id);

		if (config == null) {
			throw new NotFound("No such configuration: " + id);
		}

		return config;
	}

	@ModelAttribute
	@RequestMapping(method = PUT, value = Paths.CONFIGURATION_ID)
	public void put(HttpServletRequest request)
		throws Exception
	{
		String id = getConfigID(request);
		Model model = parseContent(request);
		RepositoryManager manager = RequestAtt.getRepositoryManager(request);

		try {
			manager.addRepositoryConfig(id, model);
		}
		finally {
			ConditionalRequestInterceptor.managerModified(request);
		}
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = Paths.CONFIGURATION_ID)
	public void delete(HttpServletRequest request)
		throws StoreConfigException, ClientHTTPException, StoreException
	{
		String id = getConfigID(request);

		RepositoryManager manager = RequestAtt.getRepositoryManager(request);

		// default to true, also assume the manager changed in case of error
		boolean configChanged = true;

		try {
			configChanged = manager.removeRepositoryConfig(id);
		}
		finally {
			if (configChanged) {
				ConditionalRequestInterceptor.managerModified(request);
			}
		}

		if (!configChanged) {
			throw new NotFound("No such configuration: " + id);
		}
	}

	private String getConfigID(HttpServletRequest request) {
		return HttpServerUtil.getLastPathSegment(request);
	}

	private Model parseContent(HttpServletRequest request)
		throws HTTPException, RDFParseException, IOException
	{
		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());
		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);

		try {
			RDFParser parser = Rio.createParser(rdfFormat);

			Model model = new LinkedHashModel();
			parser.setRDFHandler(new StatementCollector(model));

			parser.parse(request.getInputStream(), "");

			return model;
		}
		catch (UnsupportedRDFormatException e) {
			throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
		}
		catch (RDFHandlerException e) {
			throw new ServerHTTPException(e.getMessage());
		}
	}
}
