/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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

import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.QueryResultView;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.http.server.repository.TupleQueryResultView;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.impl.IteratingTupleQueryResult;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
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
		setSupportedMethods(new String[] { METHOD_GET, METHOD_HEAD, "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		String reqMethod = request.getMethod();
		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET namespace list");
			return getExportNamespacesResult(request, response);
		}
		if (METHOD_HEAD.equals(reqMethod)) {
			logger.info("HEAD namespace list");
			return getExportNamespacesResult(request, response);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE namespaces");
			return getClearNamespacesResult(request, response);
		}

		throw new ClientHTTPException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"Method not allowed: " + reqMethod);
	}

	private ModelAndView getExportNamespacesResult(HttpServletRequest request, HttpServletResponse response)
		throws ClientHTTPException, ServerHTTPException
	{
		final boolean headersOnly = METHOD_HEAD.equals(request.getMethod());

		Map<String, Object> model = new HashMap<String, Object>();
		if (!headersOnly) {
			List<String> columnNames = Arrays.asList("prefix", "namespace");
			List<BindingSet> namespaces = new ArrayList<BindingSet>();

			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized (repositoryCon) {
				final ValueFactory vf = repositoryCon.getValueFactory();
				try {
					CloseableIteration<? extends Namespace, RepositoryException> iter = repositoryCon.getNamespaces();

					try {
						while (iter.hasNext()) {
							Namespace ns = iter.next();

							Literal prefix = vf.createLiteral(ns.getPrefix());
							Literal namespace = vf.createLiteral(ns.getName());

							BindingSet bindingSet = new ListBindingSet(columnNames, prefix, namespace);
							namespaces.add(bindingSet);
						}
					}
					finally {
						iter.close();
					}
				}
				catch (RepositoryException e) {
					throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
				}
			}
			model.put(QueryResultView.QUERY_RESULT_KEY, new IteratingTupleQueryResult(columnNames, namespaces));
		}

		TupleQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(request, response,
				TupleQueryResultWriterRegistry.getInstance());

		model.put(QueryResultView.FILENAME_HINT_KEY, "namespaces");
		model.put(QueryResultView.HEADERS_ONLY, headersOnly);
		model.put(QueryResultView.FACTORY_KEY, factory);

		return new ModelAndView(TupleQueryResultView.getInstance(), model);
	}

	private ModelAndView getClearNamespacesResult(HttpServletRequest request, HttpServletResponse response)
		throws ServerHTTPException
	{
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
		synchronized (repositoryCon) {
			try {
				repositoryCon.clearNamespaces();
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
			}
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}
}
