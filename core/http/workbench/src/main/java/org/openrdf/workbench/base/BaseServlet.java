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
	protected ServletConfig config;

	public ServletConfig getServletConfig() {
		return config;
	}

	public String getServletInfo() {
		return getClass().getSimpleName();
	}

	public void init(ServletConfig config) throws ServletException {
		this.config = config;
	}

	public void destroy() {
	}

	public final void service(ServletRequest req, ServletResponse resp)
			throws ServletException, IOException {
		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse hresp = (HttpServletResponse) resp;
		service(hreq, hresp);
	}

	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
	}
}
