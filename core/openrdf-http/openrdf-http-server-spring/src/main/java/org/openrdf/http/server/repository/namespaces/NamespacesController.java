/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.namespaces;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.iteration.CloseableIteration;
import info.aduna.webapp.views.EmptySuccessView;

import org.openrdf.http.server.InternalServerException;
import org.openrdf.http.server.repository.QueryResultView;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.http.server.repository.TupleQueryResultView;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Handles requests for the list of namespace definitions for a repository.
 * 
 * @author Herko ter Horst
 */
public class NamespacesController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public NamespacesController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		ModelAndView result = null;

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		String reqMethod = request.getMethod();
		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET namespace list");
			result = getExportNamespacesResult(repositoryCon);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE namespaces");
			result = getClearNamespacesResult(repositoryCon);
		}

		return result;
	}

	private ModelAndView getExportNamespacesResult(RepositoryConnection repositoryCon)
		throws InternalServerException
	{
		List<String> columnNames = Arrays.asList("prefix", "namespace");
		List<BindingSet> namespaces = new ArrayList<BindingSet>();

		try {
			CloseableIteration<? extends Namespace, RepositoryException> iter = repositoryCon.getNamespaces();

			try {
				while (iter.hasNext()) {
					Namespace ns = iter.next();

					Literal prefix = new LiteralImpl(ns.getPrefix());
					Literal namespace = new LiteralImpl(ns.getName());

					BindingSet bindingSet = new ListBindingSet(columnNames, prefix, namespace);
					namespaces.add(bindingSet);
				}
			}
			finally {
				iter.close();
			}
		}
		catch (RepositoryException e) {
			throw new InternalServerException("Repository error: " + e.getMessage(), e);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(QueryResultView.QUERY_RESULT_KEY, new TupleQueryResultImpl(columnNames, namespaces));
		model.put(QueryResultView.FILENAME_HINT_KEY, "namespaces");

		return new ModelAndView(TupleQueryResultView.getInstance(), model);
	}

	private ModelAndView getClearNamespacesResult(RepositoryConnection repositoryCon)
		throws InternalServerException
	{
		try {
			// REMIND: use clearNamespaces() when available
			CloseableIteration<? extends Namespace, RepositoryException> nsIter = repositoryCon.getNamespaces();
			while (nsIter.hasNext()) {
				repositoryCon.removeNamespace(nsIter.next().getPrefix());
			}
		}
		catch (RepositoryException e) {
			throw new InternalServerException("Repository error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}
}
