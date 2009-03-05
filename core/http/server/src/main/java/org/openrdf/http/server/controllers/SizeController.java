/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.protocol.Protocol.REPO_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.helpers.RDFRequest;
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
	@RequestMapping(method = { GET, HEAD }, value = { REPO_PATH + "/size", CONN_PATH + "/size" })
	public StringReader size(HttpServletRequest request)
		throws Exception
	{
		ProtocolUtil.logRequestParameters(request);

		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);

		ValueFactory vf = repositoryCon.getValueFactory();
		RDFRequest req = new RDFRequest(vf, request);
		Resource subj = req.getSubject();
		URI pred = req.getPredicate();
		Value obj = req.getObject();
		Resource[] contexts = req.getContext();
		boolean useInferencing = req.isIncludeInferred();

		if (HEAD.equals(RequestMethod.valueOf(request.getMethod()))) {
			return new StringReader("");
		}

		long size = repositoryCon.sizeMatch(subj, pred, obj, useInferencing, contexts);
		return new StringReader(String.valueOf(size));
	}
}
