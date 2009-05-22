/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.LIMIT;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OFFSET;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;
import static org.restlet.data.Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * @author James Leigh
 */
public class StatementPatternParams {

	private final ValueFactory vf;

	private Form params;

	public StatementPatternParams(Request req, ValueFactory valueFactory)
		throws ResourceException
	{
		vf = valueFactory;
		params = req.getResourceRef().getQueryAsForm();

		MediaType mediaType = req.getEntity().getMediaType();

		if (MediaType.APPLICATION_WWW_FORM.equals(mediaType, true)) {
			// Include parameters from request's body
			params = new Form(params);
			params.addAll(req.getEntityAsForm());
		}
		else if (mediaType != null) {
			throw new ResourceException(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "Unsupported MIME type: "
					+ mediaType.getName());
		}
	}

	public Resource getSubject()
		throws ResourceException
	{
		return ServerUtil.parseResourceParam(params, SUBJECT_PARAM_NAME, vf);
	}

	public URI getPredicate()
		throws ResourceException
	{
		return ServerUtil.parseURIParam(params, PREDICATE_PARAM_NAME, vf);
	}

	public Value getObject()
		throws ResourceException
	{
		return ServerUtil.parseValueParam(params, OBJECT_PARAM_NAME, vf);
	}

	public Resource[] getContext()
		throws ResourceException
	{
		return ServerUtil.parseContextParam(params, CONTEXT_PARAM_NAME, vf);
	}

	public boolean isIncludeInferred() {
		return ServerUtil.parseBooleanParam(params, INCLUDE_INFERRED_PARAM_NAME, true);
	}

	public int getOffset()
		throws ResourceException
	{
		return ServerUtil.parseIntegerParam(params, OFFSET, 0);
	}

	public int getLimit()
		throws ResourceException
	{
		return ServerUtil.parseIntegerParam(params, LIMIT, -1);
	}

}
