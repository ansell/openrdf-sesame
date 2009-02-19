/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.server.repository.RepositoryInterceptor.getReadOnlyManager;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import org.openrdf.model.Model;
import org.openrdf.model.util.ModelOrganizer;
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
	@RequestMapping(method = { GET, HEAD }, value = "/schemas")
	public Model get(HttpServletRequest request)
		throws StoreConfigException
	{
		RepositoryManager manager = getReadOnlyManager(request);
		Model schemas = manager.getConfigTemplateManager().getSchemas();
		return new ModelOrganizer(schemas).organize();
	}
}
