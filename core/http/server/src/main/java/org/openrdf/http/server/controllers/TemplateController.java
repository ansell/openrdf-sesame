/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.server.repository.RepositoryInterceptor.getReadOnlyManager;
import static org.openrdf.http.server.repository.RepositoryInterceptor.getRepositoryManager;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.templates.ConfigTemplate;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Handles requests for repository configuration templates.
 * 
 * @author James Leigh
 */
@Controller
public class TemplateController {

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = "/templates")
	public TupleResult list(HttpServletRequest request)
		throws StoreConfigException
	{
		List<String> columnNames = Arrays.asList("id");
		List<BindingSet> ids = new ArrayList<BindingSet>();

		RepositoryManager manager = getReadOnlyManager(request);
		for (String id : manager.getConfigTemplateManager().getIDs()) {
			ids.add(new ListBindingSet(columnNames, new LiteralImpl(id)));
		}

		return new TupleResultImpl(columnNames, ids);
	}

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = "/templates/*")
	public Model get(HttpServletRequest request)
		throws StoreConfigException, ClientHTTPException
	{
		String id = getPathParam(request);
		RepositoryManager manager = getReadOnlyManager(request);
		ConfigTemplate template = manager.getConfigTemplateManager().getTemplate(id);
		if (template == null) {
			throw new NotFound(id);
		}
		return template.getModel();
	}

	@ModelAttribute
	@RequestMapping(method = PUT, value = "/templates/*")
	public void put(HttpServletRequest request)
		throws Exception
	{
		String id = getPathParam(request);
		Model model = getModel(request);
		RepositoryManager manager = getRepositoryManager(request);
		manager.getConfigTemplateManager().addTemplate(id, model);
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = "/templates/*")
	public void delete(HttpServletRequest request)
		throws StoreConfigException, ClientHTTPException, StoreException
	{
		String id = getPathParam(request);
		RepositoryManager manager = getRepositoryManager(request);
		manager.getConfigTemplateManager().removeTemplate(id);
	}

	private String getPathParam(HttpServletRequest request) {
		String pathInfoStr = request.getPathInfo();
		String[] pathInfo = pathInfoStr.substring(1).split("/");
		return pathInfo[pathInfo.length - 1];
	}

	private Model getModel(HttpServletRequest request)
		throws Exception
	{
		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());
		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
		}
		RDFParserFactory factory = RDFParserRegistry.getInstance().get(rdfFormat);
		RDFParser parser = factory.getParser();
		StatementCollector statements = new StatementCollector();
		parser.setRDFHandler(statements);
		parser.parse(request.getInputStream(), "");
		return new LinkedHashModel(statements.getStatements());
	}
}
