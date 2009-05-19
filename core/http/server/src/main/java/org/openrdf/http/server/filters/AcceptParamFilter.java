/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.filters;

import com.noelios.restlet.http.PreferenceUtils;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;

import info.aduna.net.http.RequestHeaders;

/**
 * Filter that stores any Accept request parameters in the request's headers,
 * effectively overriding the existing Accept headers.
 * 
 * @author Arjohn Kampman
 */
public class AcceptParamFilter extends Filter {

	public AcceptParamFilter(Context context, Restlet next) {
		super(context, next);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		Form queryParams = request.getResourceRef().getQueryAsForm();
		// FIXME: process the Accept-parameter in a case-insensitive way
		String[] acceptValues = queryParams.getValuesArray(RequestHeaders.ACCEPT);

		if (acceptValues.length > 0) {
			ClientInfo clientInfo = request.getClientInfo();

			// Remove all existing media types, the parameter overrides them
			clientInfo.getAcceptedMediaTypes().clear();

			for (String value : acceptValues) {
				PreferenceUtils.parseMediaTypes(value, clientInfo);
			}
		}

		return Filter.CONTINUE;
	}
}
