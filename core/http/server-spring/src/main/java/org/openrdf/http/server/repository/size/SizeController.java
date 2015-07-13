/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.http.server.repository.size;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Handles requests for the size of (set of contexts in) a repository.
 * 
 * @author Herko ter Horst
 */
public class SizeController extends AbstractController {

	public SizeController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, METHOD_HEAD });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		ProtocolUtil.logRequestParameters(request);

		Map<String, Object> model = new HashMap<String, Object>();
		final boolean headersOnly = METHOD_HEAD.equals(request.getMethod());

		if (!headersOnly) {
			Repository repository = RepositoryInterceptor.getRepository(request);

			ValueFactory vf = repository.getValueFactory();
			Resource[] contexts = ProtocolUtil.parseContextParam(request, Protocol.CONTEXT_PARAM_NAME, vf);

			long size = -1;

			try {
				RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
				synchronized (repositoryCon) {
					size = repositoryCon.size(contexts);
				}
			}
			catch (RepositoryException e) {
				throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
			}
			model.put(SimpleResponseView.CONTENT_KEY, String.valueOf(size));
		}

		return new ModelAndView(SimpleResponseView.getInstance(), model);
	}
}
