/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.restlet.data.Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.restlet.Request;
import org.restlet.data.Dimension;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.resource.ResourceException;

import org.openrdf.http.protocol.Protocol;
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
public class ServerUtil {

	/**
	 * An unmodifiable set that contains the object {@link Dimension#MEDIA_TYPE}.
	 */
	public static final Set<Dimension> VARY_ACCEPT = Collections.unmodifiableSet(new HashSet<Dimension>(
			Arrays.asList(Dimension.MEDIA_TYPE)));

	public static final Random RANDOM = new Random(System.currentTimeMillis());

	/**
	 * Gets the parameters from the supplied request. For GET and HEAD requests,
	 * this returns the query parameter from the request's URL. For POST
	 * requests, this returns the parameters from the request's body.
	 */
	public static Form getParameters(Request request)
		throws ResourceException
	{
		Method reqMethod = request.getMethod();

		if (Method.GET.equals(reqMethod) || Method.HEAD.equals(reqMethod)) {
			return request.getResourceRef().getQueryAsForm();
		}
		else if (Method.POST.equals(reqMethod)) {
			MediaType mediaType = request.getEntity().getMediaType();

			if (MediaType.APPLICATION_WWW_FORM.equals(mediaType, true)) {
				return request.getEntityAsForm();
			}
			else {
				throw new ResourceException(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
			}
		}

		return null;
	}

	public static Value parseValueParam(Form queryParams, String paramName, ValueFactory vf)
		throws ResourceException
	{
		String paramValue = queryParams.getFirstValue(paramName);
		try {
			return Protocol.decodeValue(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Invalid value for parameter '" + paramName
					+ "': " + paramValue);
		}
	}

	public static Resource parseResourceParam(Form queryParams, String paramName, ValueFactory vf)
		throws ResourceException
	{
		String paramValue = queryParams.getFirstValue(paramName);
		try {
			return Protocol.decodeResource(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Invalid value for parameter '" + paramName
					+ "': " + paramValue);
		}
	}

	public static URI parseURIParam(Form queryParams, String paramName, ValueFactory vf)
		throws ResourceException
	{
		String paramValue = queryParams.getFirstValue(paramName);
		try {
			return Protocol.decodeURI(paramValue, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Invalid value for parameter '" + paramName
					+ "': " + paramValue);
		}
	}

	public static Resource[] parseContextParam(Form queryParams, String paramName, ValueFactory vf)
		throws ResourceException
	{
		String[] paramValues = queryParams.getValuesArray(paramName);
		try {
			return Protocol.decodeContexts(paramValues, vf);
		}
		catch (IllegalArgumentException e) {
			throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Invalid value for parameter '" + paramName
					+ "': " + e.getMessage());
		}
	}

	public static boolean parseBooleanParam(Form queryParams, String paramName, boolean defaultValue) {
		String paramValue = queryParams.getFirstValue(paramName);

		if (paramValue == null) {
			return defaultValue;
		}

		return Boolean.parseBoolean(paramValue);
	}

	public static int parseIntegerParam(Form queryParams, String paramName, int defaultValue)
		throws ResourceException
	{
		String paramValue = queryParams.getFirstValue(paramName);

		if (paramValue == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(paramValue);
		}
		catch (NumberFormatException e) {
			throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Invalid value for parameter '" + paramName
					+ "': " + e.getMessage());
		}
	}
}
