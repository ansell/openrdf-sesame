/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.contexts;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.http.server.ClientRequestException;
import org.openrdf.http.server.InternalServerException;
import org.openrdf.http.server.repository.QueryResultView;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.http.server.repository.TupleQueryResultView;
import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Handles requests for the list of contexts in a repository.
 * 
 * @author Herko ter Horst
 */
public class ContextsController extends AbstractController {

	public ContextsController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		List<String> columnNames = Arrays.asList("contextID");
		List<BindingSet> contexts = new ArrayList<BindingSet>();
		try {
			CloseableIteration<? extends Resource, RepositoryException> contextIter = repositoryCon.getContextIDs();

			try {
				while (contextIter.hasNext()) {
					BindingSet bindingSet = new ListBindingSet(columnNames, contextIter.next());
					contexts.add(bindingSet);
				}
			}
			finally {
				contextIter.close();
			}
		}
		catch (UnsupportedQueryResultFormatException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query result format: "
					+ e.getMessage());
		}
		catch (RepositoryException e) {
			throw new InternalServerException("Repository error: " + e.getMessage(), e);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(QueryResultView.QUERY_RESULT_KEY, new TupleQueryResultImpl(columnNames, contexts));
		model.put(QueryResultView.FILENAME_HINT_KEY, "contexts");

		return new ModelAndView(TupleQueryResultView.getInstance(), model);
	}

}
