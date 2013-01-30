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
package org.openrdf.workbench.base;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.aduna.app.AppConfiguration;
import info.aduna.app.AppVersion;
import info.aduna.io.MavenUtil;

public abstract class BaseServlet implements Servlet {

	protected static final String SERVER_USER = "server-user";

	protected static final String SERVER_PASSWORD = "server-password";

	protected ServletConfig config;

	protected AppConfiguration appConfig;

	public ServletConfig getServletConfig() {
		return config;
	}

	public String getServletInfo() {
		return getClass().getSimpleName();
	}

	public void init(final ServletConfig config)
		throws ServletException
	{
		this.config = config;
		this.appConfig = new AppConfiguration("openrdf-workbench", "OpenRDF Sesame Workbench",
				AppVersion.parse(MavenUtil.loadVersion("org.openrdf.sesame", "sesame-http-workbench", "dev")));
		try {
			// Suppress loading of log configuration.
			this.appConfig.init(false);
		}
		catch (IOException e) {
			throw new ServletException(e);
		}
	}

	public void destroy() {
	}

	public final void service(final ServletRequest req, final ServletResponse resp)
		throws ServletException, IOException
	{
		final HttpServletRequest hreq = (HttpServletRequest)req;
		final HttpServletResponse hresp = (HttpServletResponse)resp;
		service(hreq, hresp);
	}

	public void service(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		// default empty implementation
	}
}
