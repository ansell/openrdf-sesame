/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletOutputStream;
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
public class ExceptionWritter implements HandlerExceptionResolver, View {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception exception)
	{
		logger.debug("ProtocolExceptionResolver.resolveException() called");

		if (exception instanceof IllegalStateException && exception.getCause() != null) {
			// Spring wraps exceptions in IllegalStateException: Unexpected
			// exception thrown
			return new ModelAndView(this, Collections.singletonMap("throwable", exception.getCause()));
		}
		else {
			return new ModelAndView(this, Collections.singletonMap("throwable", exception));
		}
	}

	public String getContentType() {
		return "text/plain";
	}

	@SuppressWarnings("unchecked")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Throwable e = (Throwable)model.get("throwable");

		int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		String errMsg = e.getMessage();

		HTTPException httpExc = findHTTPException(e);
		if (httpExc != null) {
			statusCode = httpExc.getStatusCode();

			if (e instanceof ClientHTTPException) {
				logger.info("Client sent bad request ({}): {}", statusCode, errMsg);
			}
			else {
				logger.error("Error while handling request ({}): {}", statusCode, errMsg);
			}
		}
		else if (e instanceof RDFParseException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_DATA, e.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
		}
		else if (e instanceof SAXParseException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_DATA, e.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
		}
		else if (e instanceof IllegalArgumentException) {
			statusCode = SC_BAD_REQUEST;
			errMsg = e.getMessage();
		}
		else if (e instanceof UnsupportedQueryLanguageException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.UNSUPPORTED_QUERY_LANGUAGE, e.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
		}
		else if (e instanceof MalformedQueryException) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_QUERY, e.getMessage());
			statusCode = SC_BAD_REQUEST;
			errMsg = errInfo.toString();
		}
		else {
			logger.error("Error while handling request", e);
		}

		if ("HEAD".equals(request.getMethod())) {
			response.setStatus(statusCode, errMsg);
		}
		else {
			response.setStatus(statusCode);
			if (statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
				PrintStream stream = new PrintStream(response.getOutputStream());
				try {
					e.printStackTrace(stream);
				}
				finally {
					stream.close();
				}
			}
			else {
				ServletOutputStream out = response.getOutputStream();
				try {
					out.println(errMsg);
				}
				finally {
					out.close();
				}
			}
		}
	}

	private HTTPException findHTTPException(Throwable exception) {
		if (exception == null) {
			return null;
		}
		if (exception instanceof HTTPException) {
			return (HTTPException)exception;
		}
		return findHTTPException(exception.getCause());
	}
}
