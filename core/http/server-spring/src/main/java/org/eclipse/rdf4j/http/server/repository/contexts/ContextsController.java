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
package org.eclipse.rdf4j.http.server.repository.contexts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.http.server.ProtocolUtil;
import org.eclipse.rdf4j.http.server.ServerHTTPException;
import org.eclipse.rdf4j.http.server.repository.QueryResultView;
import org.eclipse.rdf4j.http.server.repository.RepositoryInterceptor;
import org.eclipse.rdf4j.http.server.repository.TupleQueryResultView;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.impl.IteratingTupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterRegistry;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Handles requests for the list of contexts in a repository.
 * 
 * @author Herko ter Horst
 */
public class ContextsController extends AbstractController {

	public ContextsController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, METHOD_HEAD });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Map<String, Object> model = new HashMap<String, Object>();
		TupleQueryResultWriterFactory factory = ProtocolUtil.getAcceptableService(request, response,
				TupleQueryResultWriterRegistry.getInstance());

		if (METHOD_GET.equals(request.getMethod())) {
			List<String> columnNames = Arrays.asList("contextID");
			List<BindingSet> contexts = new ArrayList<BindingSet>();
			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized(repositoryCon)
			{
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
				catch (RepositoryException e) {
					throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
				}
			}
			model.put(QueryResultView.QUERY_RESULT_KEY, new IteratingTupleQueryResult(columnNames, contexts));
		}

		model.put(QueryResultView.FILENAME_HINT_KEY, "contexts");
		model.put(QueryResultView.FACTORY_KEY, factory);
		model.put(QueryResultView.HEADERS_ONLY, METHOD_HEAD.equals(request.getMethod()));
		return new ModelAndView(TupleQueryResultView.getInstance(), model);
	}
}
