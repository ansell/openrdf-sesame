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
