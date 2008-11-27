/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.OBJECT_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.PREDICATE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.SUBJECT_PARAM_NAME;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

/**
 * Handles requests for the size of (set of contexts in) a repository.
 * 
 * @author Herko ter Horst
 * @author James Leigh
 */
@Controller
public class SizeController {

	@ModelAttribute
	@RequestMapping(method = { GET, HEAD }, value = "/repositories/*/size")
	public StringReader size(HttpServletRequest request)
		throws Exception
	{
		ProtocolUtil.logRequestParameters(request);

		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);

		ValueFactory vf = repositoryCon.getValueFactory();
		Resource subj = ProtocolUtil.parseResourceParam(request, SUBJECT_PARAM_NAME, vf);
		URI pred = ProtocolUtil.parseURIParam(request, PREDICATE_PARAM_NAME, vf);
		Value obj = ProtocolUtil.parseValueParam(request, OBJECT_PARAM_NAME, vf);
		Resource[] contexts = ProtocolUtil.parseContextParam(request, Protocol.CONTEXT_PARAM_NAME, vf);
		boolean useInferencing = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, false);

		if (HEAD.equals(RequestMethod.valueOf(request.getMethod())))
				return new StringReader("");

		long size = repositoryCon.size(subj, pred, obj, useInferencing, contexts);
		return new StringReader(String.valueOf(size));
	}
}
