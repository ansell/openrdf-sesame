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
package org.openrdf.workbench.proxy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Resource cache expiry filter for Tomcat 6, slightly modifed from version
 * posted at the following page: {@link http://bit.ly/tomcat-6-caching}
 * 
 * @author Saket Kumar
 * @author Dale Visser
 */
public class CacheFilter implements Filter {

	private final static String KEY = "Cache-Control";

	private final static String PRAGMA = "Pragma";

	private final static String EXPIRES = "Expires";

	private String lifetimeSeconds = null;

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
		throws IOException, ServletException
	{

		if (lifetimeSeconds != null) {
			final long seconds = Long.parseLong(lifetimeSeconds);
			final HttpServletResponse hres = (HttpServletResponse)res;
			hres.setHeader(KEY, "max-age=" + seconds + ", public");
			hres.setHeader(PRAGMA, null);
			hres.setDateHeader(EXPIRES, System.currentTimeMillis() + seconds * 1000);
		}
		chain.doFilter(req, res);
	}

	public void init(FilterConfig config)
		throws ServletException
	{
		lifetimeSeconds = config.getInitParameter(KEY);
	}

	public void destroy() {
		lifetimeSeconds = null;
	}
}