/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.filters;

import javax.servlet.ServletException;

/**
 * AliasRedirectFilter
 * 
 * @author Herko ter Horst
 */
public class WelcomeRedirectFilter extends ResourceRedirectFilter {

	private String _welcomeView = "index.jsp";

	@Override
	protected void initialize() throws ServletException {
		String welcomeView = getFilterConfig().getInitParameter("welcome-view");
		if (welcomeView != null) {
			_welcomeView = welcomeView;
		}
	}

	@Override
	protected String getCanonicalPath(String originalPath) {
		String result = null;

		if (originalPath.endsWith("/")) {
			result = originalPath + _welcomeView;
		}

		return result;
	}
}
