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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * @author Herko ter Horst
 */
public class ClearController extends SimpleFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws ServletException, IOException
	{
		logger.info("Clearing data...");
		String actionResult = "repository.modify.remove.clear.success";

		RemovalSpecification toRemove = (RemovalSpecification)command;

		RepositoryInfo repoInfo = (RepositoryInfo)request.getSession().getAttribute(
				RepositoryInfo.REPOSITORY_KEY);
		RepositoryConnection conn = null;
		try {
			conn = repoInfo.getRepository().getConnection();
			conn.clear(toRemove.getContexts());
			conn.commit();
			logger.info("Clear committed.");
		}
		catch (RepositoryException e) {
			logger.warn("Unable to clear repository", e);
			actionResult = "repository.modify.remove.clear.failure";
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
