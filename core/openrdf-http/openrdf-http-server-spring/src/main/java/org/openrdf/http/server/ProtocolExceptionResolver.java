/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import info.aduna.webapp.views.SimpleCustomResponseView;
import info.aduna.webapp.views.SimpleResponseView;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.base.ExceptionInfoBase;
import org.openrdf.http.protocol.error.ExceptionInfo;

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

		View view = null;
		Map<String, Object> model = new HashMap<String, Object>();

		if (exception instanceof ClientRequestException) {
			view = SimpleCustomResponseView.getInstance();

			ClientRequestException cre = (ClientRequestException)exception;
			model.put(SimpleCustomResponseView.SC_KEY, Integer.valueOf(cre.getStatusCode()));
			model.put(SimpleCustomResponseView.CONTENT_TYPE_KEY, "application/octet-stream");

			ExceptionInfo exInfo = null;
			if(cre.hasExceptionInfo()) {
				exInfo = cre.getExceptionInfo();
			} else if (cre.getCause() != null) {
				exInfo = new ExceptionInfoBase(cre.getCause(), cre.getMessage());
			} else {
				exInfo = new ExceptionInfoBase(cre, cre.getMessage());
			}
			
			try {
				byte[] content = Protocol.encodeExceptionInfo(exInfo);
				model.put(SimpleCustomResponseView.CONTENT_LENGTH_KEY, new Integer(content.length));
				model.put(SimpleCustomResponseView.CONTENT_KEY, new ByteArrayInputStream(content));
			}
			catch (IOException e) {
				logger.error("Unable to encode exception info: {}", e.getMessage());
			}
			logger.info("Client sent bad request. {}", cre.getMessage());
			logger.debug("Client sent bad request.", cre);
		}
		else {
			view = SimpleResponseView.getInstance();
			model.put(SimpleResponseView.SC_KEY, Integer.valueOf(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
			model.put(SimpleResponseView.CONTENT_KEY, exception.getMessage());
			logger.error("Error while handling request.", exception);
		}

		return new ModelAndView(view, model);
	}
}
