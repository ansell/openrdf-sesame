/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.modify.remove;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.properties.ResourcePropertyEditor;
import org.openrdf.http.webclient.properties.UriPropertyEditor;
import org.openrdf.http.webclient.properties.ValuePropertyEditor;
import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * @author Herko ter Horst
 */
public class RemoveStatementsController extends SimpleFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
		throws ServletException
	{
		HttpSession session = request.getSession();
		RepositoryInfo repInfo = (RepositoryInfo)session.getAttribute(RepositoryInfo.REPOSITORY_KEY);

		binder.registerCustomEditor(Resource.class, new ResourcePropertyEditor(
				repInfo.getRepository().getValueFactory()));
		binder.registerCustomEditor(URI.class, new UriPropertyEditor(
				repInfo.getRepository().getValueFactory()));
		binder.registerCustomEditor(Value.class, new ValuePropertyEditor(
				repInfo.getRepository().getValueFactory()));
	}	
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws ServletException, IOException
	{
		logger.info("Removing statements...");
		String actionResult = "repository.modify.remove.statements.success";

		RemovalSpecification toRemove = (RemovalSpecification)command;

		RepositoryInfo repoInfo = (RepositoryInfo)request.getSession().getAttribute(
				RepositoryInfo.REPOSITORY_KEY);
		RepositoryConnection conn = null;
		try {
			conn = repoInfo.getRepository().getConnection();
			conn.remove(toRemove.getSubject(), toRemove.getPredicate(), toRemove.getObject(),
					toRemove.getContexts());
			conn.commit();
			logger.info("Remove committed.");
		}
		catch (RepositoryException e) {
			e.printStackTrace();
			actionResult = "repository.modify.remove.statements.failure";
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}

		return new ModelAndView(getSuccessView(), "actionResult", actionResult);
	}
}
