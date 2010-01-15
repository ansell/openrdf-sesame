/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.cas;

import java.util.WeakHashMap;

import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;

/**
 * @author Arjohn Kampman
 */
public final class ProxyGrantingTicketRegistry {

	private static final WeakHashMap<Session, String> pgtMap = new WeakHashMap<Session, String>();

	public static String getProxyGrantingTicket() {
		return getProxyGrantingTicket(SessionManager.get());
	}

	public static String getProxyGrantingTicket(Session session) {
		return pgtMap.get(session);
	}

	public static String storeProxyGrantingTicket(String pgt) {
		return storeProxyGrantingTicket(SessionManager.getOrCreate(), pgt);
	}

	public static String storeProxyGrantingTicket(Session session, String pgt) {
		return pgtMap.put(session, pgt);
	}
}
