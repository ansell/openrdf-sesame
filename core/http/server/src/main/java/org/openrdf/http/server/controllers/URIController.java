/*
 * Copyright James Leigh (c) 2008-2009.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.http.server.controllers;

import static java.util.Collections.singleton;
import static org.openrdf.query.QueryLanguage.SERQL;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.http.server.helpers.RequestAtt;
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
import org.openrdf.rio.RDFHandlerException;
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

	@ModelAttribute
	@RequestMapping(method = HEAD, value = "/**")
	public Model head(HttpServletRequest request)
		throws StoreConfigException, StoreException, RDFHandlerException, MalformedQueryException, NotFound
	{
		URI uri = new URIImpl(request.getRequestURL().toString());
		URI ns = new URIImpl(request.getRequestURL().append("#").toString());
		Dataset dataset = new DatasetImpl(singleton(uri), Collections.<URI> emptySet());

		RepositoryManager manager = RequestAtt.getRepositoryManager(request);

		for (String id : manager.getRepositoryIDs()) {
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

		for (String id : manager.getRepositoryIDs()) {
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
