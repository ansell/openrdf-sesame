/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static org.openrdf.http.protocol.Protocol.ACCEPT_PARAM_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.Rio;

/**
 * Utilities to help with the transition between HTTP requests/responses and
 * values expected by the protocol.
 * 
 * @author Herko ter Horst
 */
public class ProtocolUtil {

	public static Value parseValueParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeValue(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	public static Resource parseResourceParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeResource(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	public static URI parseURIParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String paramValue = request.getParameter(paramName);
		try {
			return Protocol.decodeURI(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
					+ paramValue);
		}
	}

	public static Resource[] parseContextParam(HttpServletRequest request, String paramName, ValueFactory vf)
		throws ClientRequestException
	{
		String[] paramValues = request.getParameterValues(paramName);
		try {
			return Protocol.decodeContexts(paramValues, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Invalid value for parameter '" + paramName + "': "
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

	public static void logAcceptableFormats(HttpServletRequest request) {
		Logger logger = LoggerFactory.getLogger(ProtocolUtil.class);
		if (logger.isDebugEnabled()) {
			StringBuilder acceptable = new StringBuilder(64);

			@SuppressWarnings("unchecked")
			Enumeration<String> acceptHeaders = request.getHeaders(ACCEPT_PARAM_NAME);

			while (acceptHeaders.hasMoreElements()) {
				acceptable.append(acceptHeaders.nextElement());

				if (acceptHeaders.hasMoreElements()) {
					acceptable.append(',');
				}
			}

			logger.debug("Acceptable formats: " + acceptable);
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

	/**
	 * Determines an appropriate tuple query result format based on the 'Accept'
	 * parameter or -header specified in the supplied request. The parameter
	 * value, if present, takes presedence over the header value. If the header
	 * value is used, a "Vary: Accept" header will be added to the response as
	 * required by RFC 2616.
	 * 
	 * @param request
	 *        The request.
	 * @param response
	 *        The response.
	 * @return A TupleQueryResultFormat object representing an query result
	 *         format that can be handled by the client, or <tt>null</tt> if no
	 *         such format could be determined.
	 */
	public static TupleQueryResultWriterFactory getAcceptableQueryResultWriterFactory(
			HttpServletRequest request, HttpServletResponse response)
		throws ClientRequestException
	{
		// Accept-parameter takes precedence over request headers
		String mimeType = request.getParameter(ACCEPT_PARAM_NAME);

		if (mimeType == null) {
			// Find an acceptable MIME type based on the request headers
			logAcceptableFormats(request);

			Collection<String> mimeTypes = new ArrayList<String>(8);
			for (TupleQueryResultFormat format : TupleQueryResultWriterRegistry.getInstance().getKeys()) {
				mimeTypes.addAll(format.getMIMETypes());
			}

			mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypes.iterator(), request);

			response.setHeader("Vary", Protocol.ACCEPT_PARAM_NAME);
		}

		if (mimeType != null) {
			TupleQueryResultFormat format = QueryResultUtil.getWriterFormatForMIMEType(mimeType);

			if (format != null) {
				TupleQueryResultWriterFactory factory = TupleQueryResultWriterRegistry.getInstance().get(
						format);

				if (factory != null) {
					return factory;
				}
			}
		}

		// No acceptable format was found, send 406 as required by RFC 2616
		throw new ClientRequestException(SC_NOT_ACCEPTABLE, "No acceptable query result format found");
	}

	/**
	 * Determines an appropriate RDF format based on the 'Accept' parameter or
	 * -header specified in the supplied request and returns a RDF writer factory
	 * for this format. The parameter value, if present, overrules the header
	 * value. If the header value is used, a "Vary: Accept" header will be added
	 * to the response as required by RFC 2616.
	 * 
	 * @param request
	 *        The request.
	 * @param response
	 *        The response.
	 * @return An RDFWriterFactory for an RDF file format that can be handled by
	 *         the client.
	 * @throws ClientRequestException
	 *         If no acceptable format could be determined a "NOT ACCEPTABLE"
	 *         exception is thrown.
	 */
	public static RDFWriterFactory getAcceptableRDFWriterFactory(HttpServletRequest request,
			HttpServletResponse response)
		throws ClientRequestException
	{
		// Accept-parameter takes precedence over request headers
		String mimeType = request.getParameter(ACCEPT_PARAM_NAME);

		if (mimeType == null) {
			// Find an acceptable MIME type based on the request headers
			logAcceptableFormats(request);

			Collection<String> mimeTypes = new ArrayList<String>(16);
			for (RDFFormat format : RDFWriterRegistry.getInstance().getKeys()) {
				mimeTypes.addAll(format.getMIMETypes());
			}

			mimeType = HttpServerUtil.selectPreferredMIMEType(mimeTypes.iterator(), request);

			response.setHeader("Vary", Protocol.ACCEPT_PARAM_NAME);
		}

		if (mimeType != null) {
			RDFFormat format = Rio.getWriterFormatForMIMEType(mimeType);

			if (format != null) {
				RDFWriterFactory factory = RDFWriterRegistry.getInstance().get(format);

				if (factory != null) {
					return factory;
				}
			}
		}

		// No acceptable format was found, send 406 as required by RFC 2616
		throw new ClientRequestException(SC_NOT_ACCEPTABLE, "No acceptable RDF format found.");
	}
}
