/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package info.aduna.webapp.filters;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 
 * @author Herko ter Horst
 */
public class PathFilter implements Filter {

	public void init(FilterConfig filterConf)
		throws ServletException
	{
		// do nothing
	}

	public void destroy() {
		// do nothing
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
		throws IOException, ServletException
	{
		if (req instanceof HttpServletRequest) {
			HttpServletRequest request = (HttpServletRequest)req;
			HttpServletResponse response = (HttpServletResponse)res;
			String path = request.getContextPath();

			PrintWriter out = response.getWriter();
			CharResponseWrapper wrapper = new CharResponseWrapper((HttpServletResponse)response);
			filterChain.doFilter(request, wrapper);
			CharArrayWriter caw = new CharArrayWriter();
			caw.write(wrapper.toString().replace("${path}", path));
			String result = caw.toString();
			response.setContentLength(result.length());
			out.write(result);
		}
	}

	private static class CharResponseWrapper extends HttpServletResponseWrapper {

		private CharArrayWriter output;

		@Override
		public String toString() {
			return output.toString();
		}

		public CharResponseWrapper(HttpServletResponse response) {
			super(response);
			output = new CharArrayWriter();
		}

		@Override
		public PrintWriter getWriter() {
			return new PrintWriter(output);
		}
	}
}
