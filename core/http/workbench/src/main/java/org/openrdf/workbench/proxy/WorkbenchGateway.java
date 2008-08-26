/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.proxy;

import static java.util.Collections.singletonMap;
import static org.openrdf.workbench.proxy.WorkbenchServlet.SERVER_PARAM;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.workbench.base.BaseServlet;
import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.BasicServletConfig;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbenchGateway extends BaseServlet {
	private static final String COOKIE_AGE_PARAM = "cookie-max-age";
	private static final String DEFAULT_SERVER_PARAM = "default-server";
	private static final String ACCEPTED_SERVER_PARAM = "accepted-server-prefixes";
	private static final String CHANGE_SERVER_PARAM = "change-server-path";
	private static final String SERVER_COOKIE = "workbench-server";
	private static final String TRANSFORMATIONS_PARAM = "transformations";
	private Logger logger = LoggerFactory.getLogger(WorkbenchGateway.class);
	private Map<String, WorkbenchServlet> servlets = new ConcurrentHashMap<String, WorkbenchServlet>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (getDefaultServerPath() == null)
			throw new MissingInitParameterException(DEFAULT_SERVER_PARAM);
		if (config.getInitParameter(TRANSFORMATIONS_PARAM) == null)
			throw new MissingInitParameterException(TRANSFORMATIONS_PARAM);
	}

	@Override
	public void destroy() {
		for (WorkbenchServlet servlet : servlets.values()) {
			servlet.destroy();
		}
	}

	public String getAcceptServerPrefixes() {
		return config.getInitParameter(ACCEPTED_SERVER_PARAM);
	}

	public String getChangeServerPath() {
		return config.getInitParameter(CHANGE_SERVER_PARAM);
	}

	public String getDefaultServerPath() {
		return config.getInitParameter(DEFAULT_SERVER_PARAM);
	}

	public String getMaxAgeOfCookie() {
		return config.getInitParameter(COOKIE_AGE_PARAM);
	}

	public boolean isServerFixed() {
		return getChangeServerPath() == null;
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String change = getChangeServerPath();
		if (change != null && change.equals(req.getPathInfo())) {
			changeServer(req, resp);
		} else {
			WorkbenchServlet servlet = findWorkbenchServlet(req, resp);
			if (servlet == null) {
				String uri = req.getRequestURI();
				if (req.getPathInfo() != null) {
					uri = uri.substring(0, uri.length()
							- req.getPathInfo().length());
				}
				resp.sendRedirect(uri + getChangeServerPath());
			} else {
				servlet.service(req, resp);
			}
		}
	}

	private void resetCache() {
		for (WorkbenchServlet servlet : servlets.values()) {
			// inform browser that server changed and cache is invalid
			servlet.resetCache();
		}
	}

	private File asLocalFile(URL rdf) throws UnsupportedEncodingException {
		return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
	}

	private boolean canConnect(String server) {
		try {
			URL url = new URL(server + "/protocol");
			InputStreamReader in = new InputStreamReader(url.openStream());
			BufferedReader reader = new BufferedReader(in);
			try {
				Integer.parseInt(reader.readLine());
				return true;
			} finally {
				reader.close();
			}
		} catch (MalformedURLException e) {
			logger.warn(e.toString(), e);
			return false;
		} catch (IOException e) {
			logger.warn(e.toString(), e);
			return false;
		}
	}

	private void changeServer(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String server = req.getParameter(SERVER_COOKIE);
		if (server == null) {
			resp.setContentType("application/xml");
			TupleResultBuilder builder = new TupleResultBuilder(resp
					.getWriter());
			builder.transform(getTransformationUrl(req), "server.xsl");
			builder.start();
			builder.end();
		} else if (isValidServer(server)) {
			Cookie cookie = new Cookie(SERVER_COOKIE, server);
			initCookie(cookie, req);
			resp.addCookie(cookie);
			String uri = req.getRequestURI();
			uri = uri.substring(0, uri.length() - req.getPathInfo().length());
			resetCache();
			resp.sendRedirect(uri);
		} else {
			resp.setContentType("application/xml");
			TupleResultBuilder builder = new TupleResultBuilder(resp
					.getWriter());
			builder.transform(getTransformationUrl(req), "server.xsl");
			builder.start("error-message");
			builder.result("Invalid Server URL");
			builder.end();
		}
	}

	private boolean checkServerPrefixes(String server) {
		String prefixes = getAcceptServerPrefixes();
		if (prefixes == null)
			return true;
		for (String prefix : prefixes.split(" ")) {
			if (server.startsWith(prefix))
				return true;
		}
		logger
				.warn("server URL {} does not have a prefix {}", server,
						prefixes);
		return false;
	}

	private String findServer(HttpServletRequest req, HttpServletResponse resp) {
		if (isServerFixed())
			return getDefaultServer(req);
		String value = getServerCookie(req, resp);
		if (value == null)
			return getDefaultServer(req);
		if (isValidServer(value))
			return value;
		return getDefaultServer(req);
	}

	private WorkbenchServlet findWorkbenchServlet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		String server = findServer(req, resp);
		if (servlets.containsKey(server))
			return servlets.get(server);
		if (isServerFixed() || isValidServer(server)) {
			Map<String, String> params = singletonMap(SERVER_PARAM, server);
			ServletConfig cfg = new BasicServletConfig(server, config, params);
			WorkbenchServlet servlet = new WorkbenchServlet();
			servlet.init(cfg);
			synchronized (servlets) {
				if (servlets.containsKey(server))
					return servlets.get(server);
				servlets.put(server, servlet);
				return servlet;
			}
		} else {
			return null;
		}
	}

	private String getDefaultServer(HttpServletRequest req) {
		String server = getDefaultServerPath();
		if (server.startsWith("/")) {
			StringBuffer url = req.getRequestURL();
			StringBuilder path = getServerPath(req);
			url.setLength(url.indexOf(path.toString()));
			server = url.append(server).toString();
		}
		return server;
	}

	private String getServerCookie(HttpServletRequest req,
			HttpServletResponse resp) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			return null;
		for (Cookie cookie : cookies) {
			if (SERVER_COOKIE.equals(cookie.getName())) {
				resp.addHeader("Vary", "Cookie");
				initCookie(cookie, req);
				resp.addCookie(cookie);
				return cookie.getValue();
			}
		}
		return null;
	}

	private StringBuilder getServerPath(HttpServletRequest req) {
		StringBuilder path = new StringBuilder();
		if (req.getContextPath() != null) {
			path.append(req.getContextPath());
		}
		if (req.getServletPath() != null) {
			path.append(req.getServletPath());
		}
		if (req.getPathInfo() != null) {
			path.append(req.getPathInfo());
		}
		return path;
	}

	private String getTransformationUrl(HttpServletRequest req) {
		String contextPath = req.getContextPath();
		return contextPath + config.getInitParameter(TRANSFORMATIONS_PARAM);
	}

	private void initCookie(Cookie cookie, HttpServletRequest req) {
		if (req.getContextPath() != null) {
			cookie.setPath(req.getContextPath());
		} else {
			cookie.setPath("/");
		}
		String age = getMaxAgeOfCookie();
		if (age != null) {
			cookie.setMaxAge(Integer.parseInt(age));
		}
	}

	private boolean isDirectory(String server) {
		try {
			URL url = new URL(server);
			return asLocalFile(url).isDirectory();
		} catch (MalformedURLException e) {
			logger.warn(e.toString(), e);
			return false;
		} catch (IOException e) {
			logger.warn(e.toString(), e);
			return false;
		}
	}

	private boolean isValidServer(String server) {
		if (!checkServerPrefixes(server))
			return false;
		if (server.startsWith("http")) {
			return canConnect(server);
		} else if (server.startsWith("file:")) {
			return isDirectory(server);
		}
		return true;
	}

}
