/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

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
	@RequestMapping(method = RequestMethod.GET, value = "/repositories/*/size")
	public StringReader size(HttpServletRequest request)
		throws Exception
	{
		ProtocolUtil.logRequestParameters(request);

		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		ValueFactory vf = repositoryCon.getValueFactory();
		Resource[] contexts = ProtocolUtil.parseContextParam(request, Protocol.CONTEXT_PARAM_NAME, vf);

		long size = repositoryCon.size(null, null, null, false, contexts);

		return new StringReader(String.valueOf(size));
	}
}
