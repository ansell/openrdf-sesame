/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.xml.sax.SAXParseException;

import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.rio.RDFParseException;

/**
 * Simple resolver for Exceptions: returns the correct response code and message
 * to the client.
 * 
 * @author Herko ter Horst
 * @author James Leigh
 */
public class ExceptionWriter implements HandlerExceptionResolver, View {

	private static final String THROWABLE_KEY = "throwable";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*----------------------------------------*
	 * HandlerExceptionResolver functionality *
	 *----------------------------------------*/

	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception exception)
	{
		logger.trace("ExceptionWriter.resolveException() called");

		Throwable t = exception;

		if (exception instanceof IllegalStateException && exception.getCause() != null) {
			// Spring wraps exceptions in IllegalStateException: Unexpected
			// exception thrown
			t = exception.getCause();
		}

		return new ModelAndView(this, Collections.singletonMap(THROWABLE_KEY, t));
	}

	/*--------------------*
	 * View functionality *
	 *--------------------*/

	public String getContentType() {
		return "text/plain;charset=UTF-8";
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Throwable t = (Throwable)model.get(THROWABLE_KEY);

		int statusCode;
		String errMsg;

		HTTPException httpExc = findHTTPException(t);
		if (httpExc != null) {
			statusCode = httpExc.getStatusCode();
			errMsg = httpExc.getMessage();

			if (httpExc instanceof ClientHTTPException) {
				logger.info("Client sent bad request ({}): {}", statusCode, errMsg);
			}
			else {
				logger.error("Error while handling request ({}): {}", statusCode, errMsg);
			}
		}
		else if (t instanceof RDFParseException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_DATA, t.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
			logger.info("Client sent bad request ({})", errMsg);
		}
		else if (t instanceof SAXParseException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_DATA, t.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
			logger.info("Client sent bad request ({})", errMsg);
		}
		else if (t instanceof UnsupportedQueryLanguageException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.UNSUPPORTED_QUERY_LANGUAGE, t.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
			logger.info("Client sent bad request ({})", errMsg);
		}
		else if (t instanceof MalformedQueryException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_QUERY, t.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
			logger.info("Client sent bad request ({})", errMsg);
		}
		else {
			statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			errMsg = t.getMessage();
			logger.error("Unexpected error while handling request", t);
		}

		response.setStatus(statusCode);
		
		if (response.getContentType() == null) {
			// there seems to be a bug in spring that causes getContentType() to be ignored
			response.setContentType(getContentType());
		}

		PrintWriter writer = response.getWriter();
		try {
			writer.println(errMsg);
		}
		finally {
			writer.close();
		}
	}

	private HTTPException findHTTPException(Throwable t) {
		while (t != null && !(t instanceof HTTPException)) {
			t = t.getCause();
		}

		return (HTTPException)t;
	}
}
