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
		logger.debug("ProtocolExceptionResolver.resolveException() called");

		int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		String errMsg = exception.getMessage();

		if (exception instanceof HTTPException) {
			HTTPException httpExc = (HTTPException)exception;
			statusCode = httpExc.getStatusCode();

			if (exception instanceof ClientHTTPException) {
				logger.info("Client sent bad request ( " + statusCode + ")", exception);
			}
			else {
				logger.error("Error while handling request (" + statusCode + ")", exception);
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
