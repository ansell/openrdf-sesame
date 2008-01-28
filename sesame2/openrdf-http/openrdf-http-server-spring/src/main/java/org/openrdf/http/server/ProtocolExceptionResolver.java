/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import info.aduna.webapp.views.SimpleResponseView;

/**
 * Simple resolver for Exceptions: returns the correct response code and message
 * to the client.
 * 
 * @author Herko ter Horst
 */
public class ProtocolExceptionResolver implements HandlerExceptionResolver {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception exception)
	{
		logger.info("ProtocolExceptionResolver.resolveException() called");

		int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		String errMsg = exception.getMessage();

		if (exception instanceof HTTPException) {
			HTTPException httpExc = (HTTPException)exception;
			statusCode = httpExc.getStatusCode();

			if (exception instanceof ClientHTTPException) {
				logger.info("Client sent bad request ({}): {}", statusCode, errMsg);
			}
			else {
				logger.error("Error while handling request ({}): {}", statusCode, errMsg);
			}
		}
		else {
			logger.error("Error while handling request", exception);
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(SimpleResponseView.SC_KEY, Integer.valueOf(statusCode));
		model.put(SimpleResponseView.CONTENT_KEY, errMsg);

		return new ModelAndView(SimpleResponseView.getInstance(), model);
	}
}
