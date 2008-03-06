/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.filters;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * RedirectFilter base class
 * 
 * @author Herko ter Horst
 */
public abstract class ResourceRedirectFilter implements Filter {

	private FilterConfig _config;

	protected FilterConfig getFilterConfig() {
		return _config;
	}
	
	protected abstract void initialize() throws ServletException;

	/**
	 * 
	 * @param originalPath
	 * 
	 * @return the canonical path for the specified original path, or null if
	 *         the original path should be handled as-is according to this
	 *         filter
	 */
	protected abstract String getCanonicalPath(String originalPath);

	public void init(FilterConfig config) throws ServletException {
		_config = config;

		initialize();
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest) req;
			HttpServletResponse response = (HttpServletResponse) res;

			String contextPath = request.getContextPath();
			String requested = request.getRequestURI();
			String pathInfo = requested.substring(contextPath.length());
			String canonical = getCanonicalPath(pathInfo);

			if (canonical != null) {
				StringBuilder redirect = new StringBuilder();

				redirect.append(contextPath);
				redirect.append(canonical);

				// don't forget to append the query
				String queryString = request.getQueryString();
				if (queryString != null && !queryString.equals("")) {
					redirect.append("?");
					redirect.append(queryString);
				}

				response.sendRedirect(redirect.toString());
			}
			else {
				chain.doFilter(req, res);
			}
		}
		else {
			chain.doFilter(req, res);
		}
	}

	public void destroy() {
		_config = null;
	}
}
