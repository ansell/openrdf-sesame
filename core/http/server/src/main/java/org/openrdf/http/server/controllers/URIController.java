/*
 * Copyright James Leigh (c) 2008.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.server.repository.RepositoryInterceptor.getReadOnlyManager;
import static org.openrdf.query.QueryLanguage.SERQL;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.openrdf.http.protocol.exceptions.NotFound;
import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
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

	private static final String FILTER_NS = "CONSTRUCT {subj} pred {obj}\nFROM {subj} pred {obj}\nWHERE NAMESPACE(subj) = ns";

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = "/**")
	public Model get(HttpServletRequest request)
		throws StoreConfigException, StoreException, RDFHandlerException, MalformedQueryException, NotFound
	{
		URI uri = new URIImpl(request.getRequestURL().toString());
		String namespace = request.getRequestURL().append("#").toString();
		URI ns = new URIImpl(namespace);
		StatementCollector rdf = new StatementCollector();
		boolean head = HEAD.equals(RequestMethod.valueOf(request.getMethod()));
		RepositoryManager manager = getReadOnlyManager(request);

		for (String id : manager.getRepositoryIDs()) {
			Repository repository = manager.getRepository(id);
			RepositoryConnection con = repository.getConnection();
			try {
				con.exportStatements(uri, null, null, true, rdf);
				if (con.hasStatement(null, null, null, true, uri)) {
					if (head)
						return new ModelImpl();
					con.exportStatements(null, null, null, true, rdf, uri);
				}
				else if (con.hasStatement(null, null, null, true, ns)) {
					if (head)
						return new ModelImpl();
					con.exportStatements(null, null, null, true, rdf, ns);
				}
				else if (hasNamespace(namespace, con)) {
					GraphQuery query = con.prepareGraphQuery(SERQL, FILTER_NS);
					query.setBinding("ns", ns);
					query.evaluate(rdf);
				}
			}
			finally {
				con.close();
			}
		}
		if (rdf.isEmpty())
			throw new NotFound("Not Found <" + uri.stringValue() + ">");
		return rdf.getModel();
	}

	private boolean hasNamespace(String namespace, RepositoryConnection con)
		throws StoreException
	{
		for (Namespace ns : con.getNamespaces().asList()) {
			if (ns.getName().equals(namespace))
				return true;
		}
		return false;
	}

}
