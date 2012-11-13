/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.base;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BaseServlet implements Servlet {
	protected static final String SERVER_USER = "server-user";
	
	protected static final String SERVER_PASSWORD = "server-password";

	protected ServletConfig config;

	public ServletConfig getServletConfig() {
		return config;
	}

	public String getServletInfo() {
		return getClass().getSimpleName();
	}

	public void init(final ServletConfig config) throws ServletException {
		this.config = config;
	}

	public void destroy() {
	}

	public final void service(final ServletRequest req, final ServletResponse resp)
			throws ServletException, IOException {
		final HttpServletRequest hreq = (HttpServletRequest) req;
		final HttpServletResponse hresp = (HttpServletResponse) resp;
		service(hreq, hresp);
	}

	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// default empty implementation
	}
}
