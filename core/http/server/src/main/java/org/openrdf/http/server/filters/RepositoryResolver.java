/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.filters;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.server.SesameApplication;
import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.helpers.ServerRepository;
import org.openrdf.http.server.helpers.ServerRepositoryManager;
import org.openrdf.repository.Repository;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Filter that resolves a {@link #REPOSITORY_ID_PARAM repository identifier} to
 * a {@link Repository} object and adds this object the a request's attributes.
 * This filter will produce an appropriate HTTP error when the concerning
 * repository could not be found or instantiated.
 * 
 * @author Arjohn Kampman
 */
public class RepositoryResolver extends Filter {

	public static final String REPOSITORY_ID_PARAM = "repositoryID";

	public static String getRepositoryID(Request request) {
		return (String)request.getAttributes().get(REPOSITORY_ID_PARAM);
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public RepositoryResolver(Context context, Restlet next) {
		super(context, next);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		String repositoryID = getRepositoryID(request);
		logger.debug("{}={}", REPOSITORY_ID_PARAM, repositoryID);

		ServerRepositoryManager manager = ((SesameApplication)getApplication()).getServerRepositoryManager();

		try {
			ServerRepository repository = manager.getRepository(repositoryID);

			if (repository != null) {
				RequestAtt.setRepository(request, repository);
				return Filter.CONTINUE;
			}

			// unknown repository
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
		}
		catch (StoreConfigException e) {
			logger.error("Failed to get repository from manager", e);
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Repository configuration problem: "
					+ e.getMessage());
		}
		catch (StoreException e) {
			logger.error("Failed to get repository from manager", e);
			response.setStatus(Status.SERVER_ERROR_INTERNAL, e, "Repository error: " + e.getMessage());
		}

		// Stop processing the request
		return Filter.STOP;
	}
}