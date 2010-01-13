/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.session;

import java.security.Principal;

import org.restlet.data.ClientInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;

import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;

/**
 * @author Arjohn Kampman
 */
public class SessionResource extends SesameResource {

	@Override
	protected void doInit() {
		super.doInit();
		setNegotiated(false);
	}

	@Override
	protected Representation get()
		throws ResourceException
	{
		// Get userID from the client info
		// TODO: find out whether to use getUser() or getPrincipals()
		String userID = getUserID(getRequest().getClientInfo());

		final Session session;

		if (userID != null) {
			session = SessionManager.getOrCreate();
			session.setUsername(userID);

			if (SessionUtil.getSessionID(getRequest()) == null) {
				// Register a new session for this client
				SessionRegistry registry = SessionRegistry.getFromContext(getContext());
				String sessionID = registry.add(session);
				SessionUtil.setSessionID(getResponse(), sessionID);
			}
		}
		else {
			session = SessionManager.get();
		}

		if (session != null && session.getUsername() != null) {
			return new StringRepresentation("logged in as '" + session.getUsername() + "'");
		}
		else {
			return new StringRepresentation("not logged in");
		}
	}

	@Override
	protected Representation delete()
		throws ResourceException
	{
		String sessionID = SessionUtil.getSessionID(getRequest());
		if (sessionID != null) {
			SessionRegistry registry = SessionRegistry.getFromContext(getContext());
			registry.remove(sessionID);
			SessionUtil.discardSessionID(getResponse());
		}

		return new StringRepresentation("no longer logged in");
	}

	private static String getUserID(ClientInfo clientInfo) {
		// TODO: find out whether to use getUser() or getPrincipals()
		User user = clientInfo.getUser();
		if (user != null && user.getIdentifier() != null) {
			return user.getIdentifier();
		}

		for (Principal principal : clientInfo.getPrincipals()) {
			if (principal.getName() != null) {
				return principal.getName();
			}
		}

		return null;
	}
}
