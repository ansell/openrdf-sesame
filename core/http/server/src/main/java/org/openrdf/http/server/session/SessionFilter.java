/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.session;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;

import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;

/**
 * Filter that restores session information from request coookies.
 * 
 * @author Arjohn Kampman
 */
public class SessionFilter extends Filter {

	public SessionFilter(Context context, Restlet next) {
		super(context, next);
		new SessionRegistry().storeInContext(context);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		String sessionID = SessionUtil.getSessionID(request);
		if (sessionID != null) {
			SessionRegistry registry = SessionRegistry.getFromContext(getContext());
			Session session = registry.get(sessionID);
			if (session != null) {
				// restore the session for this request
				SessionManager.set(session);
			}
		}
		return Filter.CONTINUE;
	}
}
