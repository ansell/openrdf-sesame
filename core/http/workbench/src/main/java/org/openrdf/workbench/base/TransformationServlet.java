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
package org.openrdf.workbench.base;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.helpers.BasicParserSettings;
import org.openrdf.workbench.exceptions.MissingInitParameterException;
import org.openrdf.workbench.util.CookieHandler;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public abstract class TransformationServlet extends AbstractRepositoryServlet {

	protected static final ParserConfig NON_VERIFYING_PARSER_CONFIG;
	
	static {
		NON_VERIFYING_PARSER_CONFIG = new ParserConfig();
		NON_VERIFYING_PARSER_CONFIG.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
		NON_VERIFYING_PARSER_CONFIG.set(BasicParserSettings.VERIFY_LANGUAGE_TAGS, false);
		NON_VERIFYING_PARSER_CONFIG.set(BasicParserSettings.VERIFY_RELATIVE_URIS, false);
	}
	
	public static final String CONTEXT = "context";

	protected static final String INFO = "info";

	private static final String TRANSFORMATIONS = "transformations";

	private static final Logger LOGGER = LoggerFactory.getLogger(TransformationServlet.class);

	private final Map<String, String> defaults = new HashMap<String, String>();

	protected CookieHandler cookies;

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		cookies = new CookieHandler(config, this);
		if (config.getInitParameter(TRANSFORMATIONS) == null) {
			throw new MissingInitParameterException(TRANSFORMATIONS);
		}
		if (config != null) {
			final Enumeration<?> names = config.getInitParameterNames();
			while (names.hasMoreElements()) {
				final String name = (String)names.nextElement();
				if (name.startsWith("default-")) {
					defaults.put(name.substring("default-".length()), config.getInitParameter(name));
				}
			}
		}
	}

	public String[] getCookieNames() {
		return new String[0];
	}

	@Override
	public void service(final HttpServletRequest req, final HttpServletResponse resp)
		throws ServletException, IOException
	{
		if (req.getCharacterEncoding() == null) {
			req.setCharacterEncoding("UTF-8");
		}
		resp.setCharacterEncoding("UTF-8");
		resp.setDateHeader("Expires", new Date().getTime() - 10000L);
		resp.setHeader("Cache-Control", "no-cache, no-store");

		final String contextPath = req.getContextPath();
		final String path = config.getInitParameter(TRANSFORMATIONS);
		final String xslPath = contextPath + path;
		try {
			final WorkbenchRequest wreq = new WorkbenchRequest(repository, req, defaults);

			cookies.updateCookies(wreq, resp);
			if ("POST".equals(req.getMethod())) {
				doPost(wreq, resp, xslPath);
			}
			else {
				service(wreq, resp, xslPath);
			}
		}
		catch (RuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}

	protected void doPost(final WorkbenchRequest wreq, final HttpServletResponse resp, final String xslPath)
		throws Exception
	{
		service(wreq, resp, xslPath);
	}

	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws Exception
	{
		service(getTupleResultBuilder(req, resp, resp.getOutputStream()), xslPath);
	}

	protected void service(final TupleResultBuilder writer, final String xslPath)
		throws Exception
	{
		LOGGER.info("Call made to empty superclass implementation of service(PrintWriter,String) for path: {}",
				xslPath);
	}

}