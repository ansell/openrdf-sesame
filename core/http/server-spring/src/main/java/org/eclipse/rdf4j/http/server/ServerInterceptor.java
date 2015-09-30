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
package org.eclipse.rdf4j.http.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


/**
 * Base class for single-use request interceptors. This implementation sets the
 * thread name to something sensible at the start of the request handling and
 * resets the name at the end. This is useful for logging frameworks that make
 * use of thread names, such as Log4J. Should not be a singleton bean! Configure
 * as inner bean in openrdf-servlet.xml
 * 
 * @author Herko ter Horst
 */
public abstract class ServerInterceptor extends HandlerInterceptorAdapter {

	private String origThreadName;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception
	{
		origThreadName = Thread.currentThread().getName();
		Thread.currentThread().setName(getThreadName());

		setRequestAttributes(request);

		return super.preHandle(request, response, handler);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception exception)
		throws Exception
	{
		cleanUpResources();
		Thread.currentThread().setName(origThreadName);
	}

	/**
	 * Determine the thread name to use. Called before the request is forwarded
	 * to a handler.
	 * 
	 * @return a name that makes sense based on the request
	 * @throws ServerHTTPException
	 *         if it was impossible to determine a name due to an internal error
	 */
	protected abstract String getThreadName()
		throws ServerHTTPException;

	/**
	 * Set attributes for this request. Called before the request is forwarded to
	 * a handler. By default, this method does nothing.
	 * 
	 * @param request
	 *        the request
	 * @throws ClientHTTPException
	 *         if it was impossible to set one or more attributes due to a bad
	 *         request on the part of the client
	 * @throws ServerHTTPException
	 *         if it was impossible to set one or more attributes due to an
	 *         internal error
	 */
	protected void setRequestAttributes(HttpServletRequest request)
		throws ClientHTTPException, ServerHTTPException
	{
	}

	/**
	 * Clean up resources used in handling this request. Called after the request
	 * is handled and a the view is rendered (or an exception has occurred). By
	 * default, this method does nothing.
	 * 
	 * @throws ServerHTTPException
	 *         if some resources could not be cleaned up because of an internal
	 *         error
	 */
	protected void cleanUpResources()
		throws ServerHTTPException
	{
	}
}
