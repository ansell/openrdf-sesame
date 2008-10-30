/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository.namespaces;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import info.aduna.io.IOUtil;
import info.aduna.webapp.views.EmptySuccessView;
import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

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
		setSupportedMethods(new String[] { METHOD_GET, "PUT", "DELETE" });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		String pathInfoStr = request.getPathInfo();
		String[] pathInfo = pathInfoStr.substring(1).split("/");
		String prefix = pathInfo[pathInfo.length - 1];

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		String reqMethod = request.getMethod();
		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET namespace for prefix {}" + prefix);
			return getExportNamespaceResult(repositoryCon, prefix);
		}
		else if ("PUT".equals(reqMethod)) {
			logger.info("PUT prefix {}", prefix);
			return getUpdateNamespaceResult(repositoryCon, prefix, request);
		}
		else if ("DELETE".equals(reqMethod)) {
			logger.info("DELETE prefix {}", prefix);
			return getRemoveNamespaceResult(repositoryCon, prefix);
		}
		else {
			throw new ServerHTTPException("Unexpected request method: " + reqMethod);
		}
	}

	private ModelAndView getExportNamespaceResult(RepositoryConnection repositoryCon, String prefix)
		throws ServerHTTPException, ClientHTTPException
	{
		try {
			String namespace = repositoryCon.getNamespace(prefix);

			if (namespace == null) {
				throw new ClientHTTPException(SC_NOT_FOUND, "Undefined prefix: " + prefix);
			}

			Map<String, Object> model = new HashMap<String, Object>();
			model.put(SimpleResponseView.CONTENT_KEY, namespace);

			return new ModelAndView(SimpleResponseView.getInstance(), model);
		}
		catch (StoreException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}
	}

	private ModelAndView getUpdateNamespaceResult(RepositoryConnection repositoryCon, String prefix,
			HttpServletRequest request)
		throws IOException, ClientHTTPException, ServerHTTPException
	{
		String namespace = IOUtil.readString(request.getReader());
		namespace = namespace.trim();

		if (namespace.length() == 0) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "No namespace name found in request body");
		}
		// FIXME: perform some sanity checks on the namespace string

		try {
			repositoryCon.setNamespace(prefix, namespace);
		}
		catch (StoreException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}

	private ModelAndView getRemoveNamespaceResult(RepositoryConnection repositoryCon, String prefix)
		throws ServerHTTPException
	{
		try {
			repositoryCon.removeNamespace(prefix);
		}
		catch (StoreException e) {
			throw new ServerHTTPException("Repository error: " + e.getMessage(), e);
		}

		return new ModelAndView(EmptySuccessView.getInstance());
	}
}
