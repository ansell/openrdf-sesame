/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.proxy;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.repository.Repository;
import org.openrdf.StoreException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.workbench.base.BaseServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.BasicServletConfig;
import org.openrdf.workbench.util.DynamicHttpRequest;

public class WorkbenchServlet extends BaseServlet {
	private static final String DEFAULT_PATH_PARAM = "default-path";
	private static final String NO_REPOSITORY_PARAM = "no-repository-id";
	public static String SERVER_PARAM = "server";
	private RepositoryManager manager;
	private ConcurrentMap<String, ProxyRepositoryServlet> repositories = new ConcurrentHashMap<String, ProxyRepositoryServlet>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		if (config.getInitParameter(DEFAULT_PATH_PARAM) == null)
			throw new MissingInitParameterException(DEFAULT_PATH_PARAM);
		String param = config.getInitParameter(SERVER_PARAM);
		if (param == null || param.trim().length() == 0)
			throw new MissingInitParameterException(SERVER_PARAM);
		try {
			manager = createRepositoryManager(param);
		} catch (IOException e) {
			throw new ServletException(e);
		} catch (RepositoryConfigException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		for (Servlet servlet : repositories.values()) {
			servlet.destroy();
		}
		manager.shutDown();
	}

	public void resetCache() {
		for (ProxyRepositoryServlet proxy : repositories.values()) {
			// inform browser that server changed and cache is invalid
			proxy.resetCache();
		}
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			String defaultPath = config.getInitParameter(DEFAULT_PATH_PARAM);
			resp.sendRedirect(req.getRequestURI() + defaultPath);
		} else if ("/".equals(pathInfo)) {
			String defaultPath = config.getInitParameter(DEFAULT_PATH_PARAM);
			resp.sendRedirect(req.getRequestURI() + defaultPath.substring(1));
		} else if (pathInfo.startsWith("/")) {
			int idx = pathInfo.indexOf('/', 1);
			if (idx < 0) {
				idx = pathInfo.length();
			}
			String id = pathInfo.substring(1, idx);
			try {
				service(id, req, resp);
			} catch (RepositoryConfigException e) {
				throw new ServletException(e);
			} catch (StoreException e) {
				throw new ServletException(e);
			}
		} else {
			throw new BadRequestException(
					"Request path must contain a repository ID");
		}
	}

	private RepositoryManager createRepositoryManager(String param)
			throws IOException, RepositoryConfigException {
		RepositoryManager manager;
		if (param.startsWith("file:")) {
			manager = new LocalRepositoryManager(asLocalFile(new URL(param)));
		} else {
			manager = new RemoteRepositoryManager(param);
		}
		manager.initialize();
		return manager;
	}

	private File asLocalFile(URL rdf) throws UnsupportedEncodingException {
		return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
	}

	private void service(String id, HttpServletRequest req,
			HttpServletResponse resp) throws RepositoryConfigException,
			StoreException, ServletException, IOException {
		DynamicHttpRequest http = new DynamicHttpRequest(req);
		String path = req.getPathInfo();
		int idx = path.indexOf(id) + id.length();
		http.setServletPath(http.getServletPath() + path.substring(0, idx));
		String pathInfo = path.substring(idx);
		if (pathInfo.length() == 0) {
			pathInfo = null;
		}
		http.setPathInfo(pathInfo);
		if (repositories.containsKey(id)) {
			repositories.get(id).service(http, resp);
		} else {
			Repository repository = manager.getRepository(id);
			if (repository == null) {
				String noId = config.getInitParameter(NO_REPOSITORY_PARAM);
				if (noId == null || !noId.equals(id))
					throw new BadRequestException("No such repository: " + id);
			}
			ProxyRepositoryServlet servlet = new ProxyRepositoryServlet();
			servlet.setRepositoryManager(manager);
			if (repository != null) {
				servlet.setRepositoryInfo(manager.getRepositoryInfo(id));
				servlet.setRepository(repository);
			}
			servlet.init(new BasicServletConfig(id, config));
			repositories.putIfAbsent(id, servlet);
			repositories.get(id).service(http, resp);
		}
	}

}
