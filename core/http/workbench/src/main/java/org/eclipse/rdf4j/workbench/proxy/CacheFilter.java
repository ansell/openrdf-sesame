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
package org.eclipse.rdf4j.workbench.proxy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resource cache expiry filter for Tomcat 6, based on code authored by Saket
 * Kumar.
 * 
 * @see <a href="http://bit.ly/tomcat-6-caching">Enable Caching in Tomcat 6</a>
 * @author Dale Visser
 */
public class CacheFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheFilter.class);

	/**
	 * HTTP header key for controlling caching of resources.
	 */
	private final static String CACHE_CONTROL = "Cache-Control";

	/**
	 * Maximum allowed expiry lifetime in seconds, set to one year according to
	 * the advice in RFC 2616.
	 * 
	 * @see <a href="https://www.ietf.org/rfc/rfc2616.txt">RFC 2616: HTTP/1.1</a>
	 */
	public final static long MAX_EXPIRY = (365 * 24 + 6) * 60 * 60;

	/**
	 * Minimum allowed expiry lifetime, zero, which corresponds to not caching at
	 * all.
	 */
	public final static long MIN_EXPIRY = 0;

	private Long expiry = null;

	/**
	 * Set a maximum expiry Cache-Control header applicable to the client and to
	 * intermediate caching servers.
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
		throws IOException, ServletException
	{
		if (null != expiry) {
			((HttpServletResponse)res).setHeader(CACHE_CONTROL, "max-age=" + expiry + ", public");
		}
		chain.doFilter(req, res);
	}

	/**
	 * Parse the Cache-Control configuration parameter as a long integer, and set
	 * the filter expiry value, modulo the minimum and maximum expiry
	 * constraints. If the configuration parameter is not present, or not a valid
	 * long integer value, then no Cache-Control headers will be applied by the
	 * filter.
	 * 
	 * @see #MIN_EXPIRY
	 * @see #MAX_EXPIRY
	 */
	@Override
	public void init(FilterConfig config)
		throws ServletException
	{
		try {
			expiry = Math.min(Math.max(MIN_EXPIRY, Long.parseLong(config.getInitParameter(CACHE_CONTROL))),
					MAX_EXPIRY);
		}
		catch (NumberFormatException nfe) {
			LOGGER.warn("Failed to parse " + CACHE_CONTROL + " value.", nfe);
			expiry = null;
		}
	}

	/**
	 * Make stored references available for garbage collection.
	 */
	@Override
	public void destroy() {
		expiry = null;
	}
}