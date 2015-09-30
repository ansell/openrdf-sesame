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
package org.eclipse.rdf4j.http.server.repository.namespaces;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.common.webapp.views.EmptySuccessView;
import org.eclipse.rdf4j.common.webapp.views.SimpleResponseView;
import org.eclipse.rdf4j.http.server.ClientHTTPException;
import org.eclipse.rdf4j.http.server.ServerHTTPException;
import org.eclipse.rdf4j.http.server.repository.RepositoryInterceptor;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Handles requests for manipulating a specific namespace definition in a
 * repository.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class NamespaceController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public NamespaceController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, METHOD_HEAD, "PUT", "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		String pathInfoStr = request.getPathInfo();
		String[] pathInfo = pathInfoStr.substring(1).split("/");
		String prefix = pathInfo[pathInfo.length - 1];

		String reqMethod = request.getMethod();

		if (METHOD_HEAD.equals(reqMethod)) {
			logger.info("HEAD namespace for prefix {}", prefix);

			Map<String, Object> model = new HashMap<String, Object>();
			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}

		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET namespace for prefix {}", prefix);
			return getExportNamespaceResult(request, prefix);
		}

		else if ("PUT".equals(reqMethod)) {
			logger.info("PUT prefix {}", prefix);
			return getUpdateNamespaceResult(request, prefix);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE prefix {}", prefix);
			return getRemoveNamespaceResult(request, prefix);
		}
		else {
			throw new ServerHTTPException("Unexpected request method: " + reqMethod);
		}
	}

	private ModelAndView getExportNamespaceResult(HttpServletRequest request, String prefix)
		throws ServerHTTPException, ClientHTTPException
	{
		try {
			String namespace = null;

			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized (repositoryCon) {
				namespace = repositoryCon.getNamespace(prefix);
			}

			if (namespace == null) {
				throw new ClientHTTPException(SC_NOT_FOUND, "Undefined prefix: " + prefix);
			}

			Map<String, Object> model = new HashMap<String, Object>();
			model.put(SimpleResponseView.CONTENT_KEY, namespace);

			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}
	}

	private ModelAndView getUpdateNamespaceResult(HttpServletRequest request, String prefix)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		String namespace = IOUtil.readString(request.getReader());
		namespace = namespace.trim();

		if (namespace.length() == 0) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "No namespace name found in request body");
		}
		// FIXME: perform some sanity checks on the namespace string

		try {
			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized (repositoryCon) {
				repositoryCon.setNamespace(prefix, namespace);
			}
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}

	private ModelAndView getRemoveNamespaceResult(HttpServletRequest request, String prefix)
		throws ServerHTTPException
	{
		try {
			RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);
			synchronized (repositoryCon) {
				repositoryCon.removeNamespace(prefix);
			}
		}
		catch (RepositoryException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}
}
