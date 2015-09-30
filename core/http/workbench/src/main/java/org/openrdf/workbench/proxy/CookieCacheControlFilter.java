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
package org.openrdf.workbench.proxy;

import static java.lang.System.currentTimeMillis;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Browsers do not expire cache if Cookies have changed. Even if "Vary: Cookie"
 * header is set. This filter intercepts the Last-Modified and If-Modified-Since
 * headers to include modification of browser cookies.
 * 
 * @author James Leigh
 */
public class CookieCacheControlFilter implements Filter {

	private static final String LAST_MODIFIED_COOKIE = "Last-Modified";

	private static final String HEADER_IFMODSINCE = "If-Modified-Since";

	private static final String HEADER_LASTMOD = "Last-Modified";

	public void init(FilterConfig config)
		throws ServletException
	{
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
		throws IOException, ServletException
	{
		CacheAwareRequest request = new CacheAwareRequest(req);
		CacheAwareResponse response = new CacheAwareResponse(req, resp);
		chain.doFilter(request, response);
	}

	private class CacheAwareRequest extends HttpServletRequestWrapper {

		public CacheAwareRequest(ServletRequest request) {
			this((HttpServletRequest)request);
		}

		public CacheAwareRequest(HttpServletRequest request) {
			super(request);
		}

		/**
		 * This method hides the If-Modified-Since header if the browser's cookies
		 * have changed since this page was cached.
		 */
		@Override
		public long getDateHeader(String name) {
			long value = super.getDateHeader(name);
			if (HEADER_IFMODSINCE.equals(name)) {
				Cookie[] cookies = getCookies();
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (LAST_MODIFIED_COOKIE.equals(cookie.getName())) {
							long lastModified = Long.parseLong(cookie.getValue());
							if (lastModified > value) {
								// cookies have changed since this cache
								return -1;
							}
						}
					}
				}
			}
			return value;
		}

	}

	private class CacheAwareResponse extends HttpServletResponseWrapper {

		private HttpServletRequest req;

		private boolean controlCookieAdded;

		public CacheAwareResponse(ServletRequest req, ServletResponse response) {
			this((HttpServletRequest)req, (HttpServletResponse)response);
		}

		public CacheAwareResponse(HttpServletRequest req, HttpServletResponse response) {
			super(response);
			this.req = req;
		}

		/**
		 * This method tells the browser to track the last time the cookies
		 * changed.
		 */
		@Override
		public void addCookie(Cookie c) {
			boolean changed = true;
			Cookie existing = getCookie(c.getName());
			if (existing != null) {
				changed = !c.getValue().equals(existing.getValue());
			}
			super.addCookie(c);
			if (!controlCookieAdded && changed) {
				long date = currentTimeMillis() / 1000 * 1000;
				String value = String.valueOf(date);
				Cookie cookie = new Cookie(LAST_MODIFIED_COOKIE, value);
				String contextPath = req.getContextPath();
				if (contextPath == null) {
					cookie.setPath("/");
				}
				else {
					cookie.setPath(contextPath);
				}
				super.addCookie(cookie);
				super.setDateHeader(HEADER_LASTMOD, date);
			}
		}

		/**
		 * The Last-Modified will include the last time the cookies changed for
		 * this browser.
		 */
		@Override
		public void setDateHeader(String name, long date) {
			if (HEADER_LASTMOD.equals(name)) {
				Cookie cookie = getCookie(LAST_MODIFIED_COOKIE);
				if (cookie != null) {
					long lastModified = Long.parseLong(cookie.getValue());
					if (lastModified > date) {
						// cookies have changed since, use that instead
						super.setDateHeader(name, lastModified);
						return;
					}
				}
			}
			super.setDateHeader(name, date);
		}

		private Cookie getCookie(String name) {
			Cookie[] cookies = req.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (name.equals(cookie.getName())) {
						return cookie;
					}
				}
			}
			return null;
		}
	}

}
