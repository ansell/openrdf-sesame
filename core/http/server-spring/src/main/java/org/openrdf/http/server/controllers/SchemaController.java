/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.server.repository.RepositoryInterceptor.getRepositoryManager;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.openrdf.model.Model;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;

/**
 * Handles requests for repository configuration schemas.
 * 
 * @author James Leigh
 */
@Controller
public class SchemaController {

	@ModelAttribute
	@RequestMapping(method = RequestMethod.GET, value = "/schemas")
	public Model get(HttpServletRequest request)
		throws StoreConfigException
	{
		RepositoryManager manager = getRepositoryManager(request);
		return manager.getConfigTemplateManager().getSchemas();
	}
}
