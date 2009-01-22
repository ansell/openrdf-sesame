/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.FORM_MIME_TYPE;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.LIMIT;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OFFSET;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;
import static org.openrdf.http.server.helpers.ProtocolUtil.parseBooleanParam;
import static org.openrdf.http.server.helpers.ProtocolUtil.parseContextParam;
import static org.openrdf.http.server.helpers.ProtocolUtil.parseIntegerParam;
import static org.openrdf.http.server.helpers.ProtocolUtil.parseResourceParam;
import static org.openrdf.http.server.helpers.ProtocolUtil.parseURIParam;
import static org.openrdf.http.server.helpers.ProtocolUtil.parseValueParam;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMethod;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * @author James Leigh
 */
public class RDFRequest {

	private ValueFactory vf;

	private HttpServletRequest request;

	public RDFRequest(ValueFactory vf, HttpServletRequest req)
		throws UnsupportedMediaType, IOException
	{
		this.vf = vf;
		this.request = req;
		String contentType = req.getContentType();
		if (contentType != null) {
			String mimeType = HttpServerUtil.getMIMEType(contentType);
			if (!FORM_MIME_TYPE.equals(mimeType)) {
				throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
			}
			RequestMethod reqMethod = RequestMethod.valueOf(req.getMethod());
			if (!POST.equals(reqMethod)) {
				// Include form data in parameters (already included for POST).
				this.request = ProtocolUtil.readFormData(req);
			}
		}
	}

	public Resource getSubject()
		throws BadRequest
	{
		return parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
	}

	public URI getPredicate()
		throws BadRequest
	{
		return parseURIParam(request, PREDICATE_PARAM_NAME, vf);
	}

	public Value getObject()
		throws BadRequest
	{
		return parseValueParam(request, OBJECT_PARAM_NAME, vf);
	}

	public Resource[] getContext()
		throws BadRequest
	{
		return parseContextParam(request, CONTEXT_PARAM_NAME, vf);
	}

	public boolean isIncludeInferred() {
		return parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);
	}

	public int getOffset() {
		return parseIntegerParam(request, OFFSET, 0);
	}

	public int getLimit() {
		return parseIntegerParam(request, LIMIT, -1);
	}

}
