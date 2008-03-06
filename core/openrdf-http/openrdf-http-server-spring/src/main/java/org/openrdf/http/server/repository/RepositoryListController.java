/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import org.openrdf.http.server.InternalServerException;
import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.manager.LocalRepositoryManager;

/**
 * Handles requests for the list of repositories available on this server.
 * 
 * @author Herko ter Horst
 */
public class RepositoryListController extends AbstractController {

	private static final String REPOSITORY_LIST_QUERY;

	static {
		StringBuilder query = new StringBuilder(256);
		query.append("SELECT id, type, title, \"true\"^^xsd:boolean as \"readable\", \"true\"^^xsd:boolean as \"writable\"");
		query.append("FROM {} rdf:type {sys:Repository};");
		query.append("        [rdfs:label {title}];");
		query.append("        sys:repositoryID {id};");
		query.append("        sys:repositoryImpl {} sys:repositoryType {type} ");
		query.append("USING NAMESPACE sys = <http://www.openrdf.org/config/repository#>");
		REPOSITORY_LIST_QUERY = query.toString();
	}

	private LocalRepositoryManager repositoryManager;

	public RepositoryListController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET });
	}

	public void setRepositoryManager(LocalRepositoryManager repMan) {
		repositoryManager = repMan;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Repository systemRepository = repositoryManager.getSystemRepository();
		ValueFactory vf = systemRepository.getValueFactory();

		try {
			RepositoryConnection con = systemRepository.getConnection();
			try {
				// FIXME: The query result is cached here as we need to close the
				// connection before returning. Would be much better to stream the
				// query result directly to the client.

				List<String> bindingNames = new ArrayList<String>();
				List<BindingSet> bindingSets = new ArrayList<BindingSet>();

				TupleQueryResult queryResult = con.prepareTupleQuery(QueryLanguage.SERQL, REPOSITORY_LIST_QUERY).evaluate();
				try {
					// Determine the repository's URI
					StringBuffer requestURL = request.getRequestURL();
					if (requestURL.charAt(requestURL.length() - 1) != '/') {
						requestURL.append('/');
					}
					String namespace = requestURL.toString();

					while (queryResult.hasNext()) {
						QueryBindingSet bindings = new QueryBindingSet(queryResult.next());

						Literal idValue = (Literal)bindings.getValue("id");
						bindings.addBinding("uri", vf.createURI(namespace, idValue.getLabel()));

						bindingSets.add(bindings);
					}

					bindingNames.add("uri");
					bindingNames.addAll(queryResult.getBindingNames());
				}
				finally {
					queryResult.close();
				}

				Map<String, Object> model = new HashMap<String, Object>();
				model.put(QueryResultView.QUERY_RESULT_KEY, new TupleQueryResultImpl(bindingNames, bindingSets));
				model.put(QueryResultView.FILENAME_HINT_KEY, "repositories");

				return new ModelAndView(TupleQueryResultView.getInstance(), model);
			}
			finally {
				con.close();
			}
		}
		catch (RepositoryException e) {
			throw new InternalServerException(e.getMessage(), e);
		}
	}
}
