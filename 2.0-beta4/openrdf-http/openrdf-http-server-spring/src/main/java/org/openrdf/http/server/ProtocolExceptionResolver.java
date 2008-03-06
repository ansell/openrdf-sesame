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
import org.springframework.web.servlet.View;

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

		View view = SimpleResponseView.getInstance();
		Map<String, Object> model = new HashMap<String, Object>();

		if (exception instanceof ClientRequestException) {
			ClientRequestException cre = (ClientRequestException)exception;
			model.put(SimpleResponseView.SC_KEY, Integer.valueOf(cre.getStatusCode()));
			logger.info("Client sent bad request. {}", cre.getMessage());
		}
		else {
			model.put(SimpleResponseView.SC_KEY, Integer.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
			logger.error("Error while handling request.", exception);
		}

		model.put(SimpleResponseView.CONTENT_KEY, exception.getMessage());

		return new ModelAndView(view, model);
	}
}
