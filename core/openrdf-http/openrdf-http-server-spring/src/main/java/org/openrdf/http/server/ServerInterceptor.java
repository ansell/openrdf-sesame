/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

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
	 * @param request
	 *        the request
	 * @return a name that makes sense based on the request
	 * @throws InternalServerException
	 *         if it was impossible to determine a name due to an internal error
	 */
	protected abstract String getThreadName()
		throws InternalServerException;

	/**
	 * Set attributes for this request. Called before the request is forwarded to
	 * a handler. By default, this method does nothing.
	 * 
	 * @param request
	 *        the request
	 * @throws ClientRequestException
	 *         if it was impossible to set one or more attributes due to a bad
	 *         request on the part of the client
	 * @throws InternalServerException
	 *         if it was impossible to set one or more attributes due to an
	 *         internal error
	 */
	protected void setRequestAttributes(HttpServletRequest request)
		throws ClientRequestException, InternalServerException
	{
	}

	/**
	 * Clean up resources used in handling this request. Called after the request
	 * is handled and a the view is rendered (or an exception has occurred). By
	 * default, this method does nothing.
	 * 
	 * @throws InternalServerException
	 *         if some resources could not be cleaned up because of an internal
	 *         error
	 */
	protected void cleanUpResources()
		throws InternalServerException
	{
	}
}
