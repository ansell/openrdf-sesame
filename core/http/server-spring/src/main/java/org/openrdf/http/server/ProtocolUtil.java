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
package org.openrdf.http.server;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;

import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.lang.FileFormat;
import info.aduna.lang.service.FileFormatServiceRegistry;
import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Utilities to help with the transition between HTTP requests/responses and
 * values expected by the protocol.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class ProtocolUtil {

	public static Value parseValueParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientHTTPException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeValue(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	public static Resource parseResourceParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientHTTPException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeResource(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	public static IRI parseURIParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientHTTPException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeURI(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	public static IRI parseGraphParam(HttpServletRequest request, ValueFactory vf)
		throws ClientHTTPException
	{
		String paramValue = request.getParameter(Protocol.GRAPH_PARAM_NAME);
		if (paramValue == null) {
			return null;
		}

		try {
			return Protocol.decodeURI("<" + paramValue + ">", vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "Invalid value for parameter '"
					+ Protocol.GRAPH_PARAM_NAME + "': " + paramValue);
		}
	}

	public static Resource[] parseContextParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientHTTPException
	{
		String[] paramValues = request.getParameterValues(paramName);
		try {
			return Protocol.decodeContexts(paramValues, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientHTTPException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ e.getMessage());
		}
	}

	public static boolean parseBooleanParam(HttpServletRequest request, String paramName, boolean defaultValue)
	{
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			return defaultValue;
		}
		else {
			return Boolean.parseBoolean(paramValue);
		}
	}

	/**
	 * Logs all request parameters of the supplied request.
	 */
	public static void logRequestParameters(HttpServletRequest request) {
		Logger logger = LoggerFactory.getLogger(ProtocolUtil.class);
		if (logger.isDebugEnabled()) {
			@SuppressWarnings("unchecked")
			Enumeration<String> paramNames = request.getParameterNames();
			while (paramNames.hasMoreElements()) {
				String name = paramNames.nextElement();
				for (String value : request.getParameterValues(name)) {
					logger.debug("{}=\"{}\"", name, value);
				}
			}
		}
	}

	public static <FF extends FileFormat, S> S getAcceptableService(HttpServletRequest request,
			HttpServletResponse response, FileFormatServiceRegistry<FF, S> serviceRegistry)
		throws ClientHTTPException
	{
		// Accept-parameter takes precedence over request headers
		String mimeType = request.getParameter(Protocol.ACCEPT_PARAM_NAME);
		boolean hasAcceptParam = mimeType != null;

		if (mimeType == null) {
			// Find an acceptable MIME type based on the request headers
			logAcceptableFormats(request);

			Collection<String> mimeTypes = new LinkedHashSet<String>(16);
			// Prefer the default mime types, explicitly before non-default
			for (FileFormat format : serviceRegistry.getKeys()) {
				mimeTypes.add(format.getDefaultMIMEType());
			}
			for (FileFormat format : serviceRegistry.getKeys()) {
				mimeTypes.addAll(format.getMIMETypes());
			}

			mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypes.iterator(), request);

			response.setHeader("Vary", "Accept");
		}

		if (mimeType != null) {
			Optional<FF> format = serviceRegistry.getFileFormatForMIMEType(mimeType);

			if (format.isPresent()) {
				return serviceRegistry.get(format.get()).get();
			}
		}

		if (hasAcceptParam) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.UNSUPPORTED_FILE_FORMAT, mimeType);
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		else {
			// No acceptable format was found, send 406 as required by RFC 2616
			throw new ClientHTTPException(SC_NOT_ACCEPTABLE, "No acceptable file format found.");
		}
	}

	public static void logAcceptableFormats(HttpServletRequest request) {
		Logger logger = LoggerFactory.getLogger(ProtocolUtil.class);
		if (logger.isDebugEnabled()) {
			StringBuilder acceptable = new StringBuilder(64);

			@SuppressWarnings("unchecked")
			Enumeration<String> acceptHeaders = request.getHeaders("Accept");

			while (acceptHeaders.hasMoreElements()) {
				acceptable.append(acceptHeaders.nextElement());

				if (acceptHeaders.hasMoreElements()) {
					acceptable.append(',');
				}
			}

			logger.debug("Acceptable formats: " + acceptable);
		}
	}
}
