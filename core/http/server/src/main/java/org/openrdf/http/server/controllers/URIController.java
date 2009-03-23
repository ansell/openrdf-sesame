/*
 * Copyright James Leigh (c) 2008-2009.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.http.server.controllers;

import static java.util.Collections.singleton;
import static org.openrdf.query.QueryLanguage.SERQL;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.interceptors.ConditionalRequestInterceptor;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.util.ModelOrganizer;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Returns a DESCRIBE RDF result using the request URL as the resource URI.
 * 
 * @author James Leigh
 */
@Controller
public class URIController {

	private static final String FILTER_NS = "CONSTRUCT {subj} pred {obj}\n" + "FROM {subj} pred {obj}\n"
			+ "WHERE NAMESPACE(subj) = ns";

	private static final String CONSTRUCT_ALL = "CONSTRUCT {subj} pred {obj}\n" + "FROM {subj} pred {obj}";

	private String repositoryId;

	public URIController(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	@ModelAttribute
	@RequestMapping(method = HEAD, value = "/**")
	public Model head(HttpServletRequest request)
		throws StoreConfigException, StoreException, RDFHandlerException, MalformedQueryException, NotFound
	{
		URI uri = new URIImpl(request.getRequestURL().toString());
		URI ns = new URIImpl(request.getRequestURL().append("#").toString());
		Dataset dataset = new DatasetImpl(singleton(uri), Collections.<URI> emptySet());

		RepositoryManager manager = RequestAtt.getRepositoryManager(request);

		for (String id : getRepositoryIDs(manager)) {
			Repository repository = manager.getRepository(id);
			RepositoryConnection con = repository.getConnection();
			try {
				if (con.hasMatch(uri, null, null, true)) {
					return new LinkedHashModel();
				}
				else if (hasNamespace(ns, con)) {
					GraphQuery query = con.prepareGraphQuery(SERQL, FILTER_NS + "\nLIMIT 1");
					query.setBinding("ns", ns);
					if (!query.evaluate().asList().isEmpty()) {
						return new LinkedHashModel();
					}
				}
				else {
					GraphQuery query = con.prepareGraphQuery(SERQL, CONSTRUCT_ALL + "\nLIMIT 1");
					query.setDataset(dataset);
					if (!query.evaluate().asList().isEmpty()) {
						return new LinkedHashModel();
					}
				}
			}
			finally {
				con.close();
			}
		}

		throw new NotFound("Not Found <" + uri.stringValue() + ">");
	}

	@ModelAttribute
	@RequestMapping(method = GET, value = "/**")
	public Model get(HttpServletRequest request)
		throws StoreConfigException, StoreException, RDFHandlerException, MalformedQueryException, NotFound
	{
		URI uri = new URIImpl(request.getRequestURL().toString());
		URI ns = new URIImpl(request.getRequestURL().append("#").toString());
		Dataset dataset = new DatasetImpl(singleton(uri), Collections.<URI> emptySet());

		StatementCollector rdf = new StatementCollector();

		RepositoryManager manager = RequestAtt.getRepositoryManager(request);

		for (String id : getRepositoryIDs(manager)) {
			Repository repository = manager.getRepository(id);
			RepositoryConnection con = repository.getConnection();
			try {
				GraphQuery query = con.prepareGraphQuery(SERQL, CONSTRUCT_ALL);
				query.setDataset(dataset);
				query.evaluate(rdf);
				con.exportMatch(uri, null, null, true, rdf);
				if (hasNamespace(ns, con)) {
					query = con.prepareGraphQuery(SERQL, FILTER_NS);
					query.setBinding("ns", ns);
					query.evaluate(rdf);
				}
			}
			finally {
				con.close();
			}
		}

		if (rdf.isEmpty()) {
			throw new NotFound("Not Found <" + uri.stringValue() + ">");
		}

		ModelOrganizer organizer = new ModelOrganizer(rdf.getModel());
		organizer.setSubjectOrder(uri, ns);
		return organizer.organize();
	}

	@ModelAttribute
	@RequestMapping(method = PUT, value = "/**")
	public void put(HttpServletRequest request)
		throws StoreConfigException, StoreException, ClientHTTPException, RDFParseException, IOException
	{
		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());
		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
		}
		URI uri = new URIImpl(request.getRequestURL().toString());
		RepositoryManager manager = RequestAtt.getRepositoryManager(request);
		Repository repository = getRepository(manager);

		RepositoryConnection con = repository.getConnection();
		try {
			con.begin();
			con.clear(uri);
			con.add(request.getInputStream(), uri.stringValue(), rdfFormat, uri);
			con.commit();
			ConditionalRequestInterceptor.managerModified(request);
		}
		finally {
			con.close();
		}
	}

	@ModelAttribute
	@RequestMapping(method = POST, value = "/**")
	public void post(HttpServletRequest request)
	throws StoreConfigException, StoreException, ClientHTTPException, RDFParseException, IOException
	{
		String mimeType = HttpServerUtil.getMIMEType(request.getContentType());
		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);
		if (rdfFormat == null) {
			throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
		}
		URI uri = new URIImpl(request.getRequestURL().toString());
		RepositoryManager manager = RequestAtt.getRepositoryManager(request);
		Repository repository = getRepository(manager);

		RepositoryConnection con = repository.getConnection();
		try {
			con.add(request.getInputStream(), uri.stringValue(), rdfFormat, uri);
			ConditionalRequestInterceptor.managerModified(request);
		}
		finally {
			con.close();
		}
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = "/**")
	public void delete(HttpServletRequest request)
		throws StoreConfigException, StoreException, ClientHTTPException
	{
		URI uri = new URIImpl(request.getRequestURL().toString());
		RepositoryManager manager = RequestAtt.getRepositoryManager(request);
		Repository repository = getRepository(manager);

		RepositoryConnection con = repository.getConnection();
		try {
			con.clear(uri);
			ConditionalRequestInterceptor.managerModified(request);
		}
		finally {
			con.close();
		}
	}

	private Set<String> getRepositoryIDs(RepositoryManager manager)
		throws StoreConfigException, NotFound, StoreException
	{
		if (repositoryId == null) {
			return manager.getRepositoryIDs();
		}
		if (manager.getRepository(repositoryId) == null) {
			throw new NotFound(repositoryId);
		}
		return Collections.singleton(repositoryId);
	}

	private Repository getRepository(RepositoryManager manager)
		throws StoreConfigException, StoreException, ClientHTTPException
	{
		if (repositoryId == null) {
			throw ClientHTTPException.create(405, "Method Not Allowed");
		}
		Repository repository = manager.getRepository(repositoryId);
		if (repository == null) {
			throw new NotFound(repositoryId);
		}
		return repository;
	}

	private boolean hasNamespace(URI namespace, RepositoryConnection con)
		throws StoreException
	{
		for (Namespace ns : con.getNamespaces().asList()) {
			if (ns.getName().equals(namespace.stringValue())) {
				return true;
			}
		}
		return false;
	}
}
