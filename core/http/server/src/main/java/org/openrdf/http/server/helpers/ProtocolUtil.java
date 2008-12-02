/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.lang.FileFormat;
import info.aduna.lang.service.FileFormatServiceRegistry;
import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.protocol.exceptions.NotAcceptable;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
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
		throws BadRequest
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeValue(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new BadRequest("Invalid value for parameter '" + paramName + "': " + paramValue);
		}
	}

	public static Resource parseResourceParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws BadRequest
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeResource(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new BadRequest("Invalid value for parameter '" + paramName + "': " + paramValue);
		}
	}

	public static URI parseURIParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws BadRequest
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeURI(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new BadRequest("Invalid value for parameter '" + paramName + "': " + paramValue);
		}
	}

	public static Resource[] parseContextParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws BadRequest
	{
		String[] paramValues = request.getParameterValues(paramName);
		try {
			return Protocol.decodeContexts(paramValues, vf);
		}
		catch (IllegalArgumentException e) {
			throw new BadRequest("Invalid value for parameter '" + paramName + "': " + e.getMessage());
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

	public static Integer parseIntegerParam(HttpServletRequest request, String paramName, Integer defaultValue)
	{
		String paramValue = request.getParameter(paramName);
		if (paramValue == null) {
			return defaultValue;
		}
		else {
			return Integer.valueOf(paramValue);
		}
	}

	public static Map<String, List<String>> parseFormData(BufferedReader reader, String encoding)
		throws UnsupportedEncodingException
	{
		if (encoding == null) {
			encoding = "ISO-8859-1";
		}
		Map<String, List<String>> parameters = new HashMap<String, List<String>>();
		Scanner scanner = new Scanner(reader);
		scanner.useDelimiter("&");
		while (scanner.hasNext()) {
			final String[] nameValue = scanner.next().split("=");
			if (nameValue.length == 0 || nameValue.length > 2)
				throw new IllegalArgumentException("bad parameter");
			final String name = URLDecoder.decode(nameValue[0], encoding);
			String value = null;
			if (nameValue.length == 2)
				value = URLDecoder.decode(nameValue[1], encoding);
			if (!parameters.containsKey(name)) {
				parameters.put(name, new ArrayList<String>());
			}
			parameters.get(name).add(value);
		}
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public static HttpServletRequest readFormData(HttpServletRequest request)
		throws IOException
	{
		BufferedReader reader = request.getReader();
		String encoding = request.getCharacterEncoding();
		final Map<String, List<String>> parameters = parseFormData(reader, encoding);
		return new HttpServletRequestWrapper(request) {

			@Override
			public String getParameter(String name) {
				if (parameters.containsKey(name))
					return parameters.get(name).get(0);
				return super.getParameter(name);
			}

			@Override
			public Map getParameterMap() {
				Map<String, String> map = new HashMap<String, String>();
				map.putAll(super.getParameterMap());
				for (String name : parameters.keySet()) {
					map.put(name, parameters.get(name).get(0));
				}
				return map;
			}

			@Override
			public Enumeration getParameterNames() {
				return new Vector<String>(getParameterMap().keySet()).elements();
			}

			@Override
			public String[] getParameterValues(String name) {
				if (parameters.containsKey(name)) {
					return parameters.get(name).toArray(new String[0]);
				}
				return super.getParameterValues(name);
			}
		};
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
		String[] accept = request.getParameterValues(Protocol.ACCEPT_PARAM_NAME);
		boolean hasAcceptParam = accept != null;

		if (hasAcceptParam) {
			request = overrideHeader(HttpServerUtil.ACCEPT_HEADER_NAME, accept, request);
		}

		// Find an acceptable MIME type based on the request headers
		logAcceptableFormats(request);

		Collection<String> mimeTypes = new ArrayList<String>(16);
		for (FileFormat format : serviceRegistry.getKeys()) {
			mimeTypes.addAll(format.getMIMETypes());
		}

		String mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypes.iterator(), request);

		if (!hasAcceptParam){ 
			response.setHeader("Vary", HttpServerUtil.ACCEPT_HEADER_NAME);
		}

		if (mimeType != null) {
			FF format = serviceRegistry.getFileFormatForMIMEType(mimeType);

			if (format != null) {
				return serviceRegistry.get(format);
			}
		}

		if (hasAcceptParam) {
			throw new UnsupportedFileFormat(mimeType);
		}
		else {
			// No acceptable format was found, send 406 as required by RFC 2616
			throw new NotAcceptable("No acceptable file format found.");
		}
	}

	public static void logAcceptableFormats(HttpServletRequest request) {
		Logger logger = LoggerFactory.getLogger(ProtocolUtil.class);
		if (logger.isDebugEnabled()) {
			StringBuilder acceptable = new StringBuilder(64);

			@SuppressWarnings("unchecked")
			Enumeration<String> acceptHeaders = request.getHeaders(HttpServerUtil.ACCEPT_HEADER_NAME);

			while (acceptHeaders.hasMoreElements()) {
				acceptable.append(acceptHeaders.nextElement());

				if (acceptHeaders.hasMoreElements()) {
					acceptable.append(',');
				}
			}

			logger.debug("Acceptable formats: " + acceptable);
		}
	}

	private static HttpServletRequest overrideHeader(final String header, final String[] values,
			HttpServletRequest request)
	{
		return new HttpServletRequestWrapper(request) {

			@Override
			public String getHeader(String name) {
				if (header.equals(name) && values.length == 0)
					return null;
				if (header.equals(name))
					return values[0];
				return super.getHeader(name);
			}

			@Override
			@SuppressWarnings("unchecked")
			public Enumeration getHeaders(String name) {
				if (header.equals(name)) {
					Vector vector = new Vector();
					for (int i = 0; i < values.length; i++) {
						vector.add(values[i]);
					}
					return vector.elements();
				}
				return super.getHeaders(name);
			}
		};
	}
}
