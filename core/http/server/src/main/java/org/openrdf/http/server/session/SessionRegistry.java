/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.session;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.Context;

import org.openrdf.store.Session;
import org.openrdf.store.SessionManager;

/**
 * @author Arjohn Kampman
 */
class SessionRegistry {

	private static final String CONTEXT_KEY = SessionManager.class.getName();

	public static SessionRegistry getFromContext(Context context) {
		return (SessionRegistry)context.getAttributes().get(CONTEXT_KEY);
	}

	private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();

	private final SecureRandom keyGenerator = new SecureRandom();

	public void storeInContext(Context context) {
		context.getAttributes().put(CONTEXT_KEY, this);
	}

	public String add(Session session) {
		String key = generateKey();
		while (sessions.putIfAbsent(key, session) != null) {
			// key collision
			key = generateKey();
		}
		return key;
	}

	public Session get(String key) {
		return sessions.get(key);
	}

	public Session remove(String key) {
		return sessions.remove(key);
	}

	private String generateKey() {
		// FIXME: re-seed the key generator once in a while, see
		// http://www.cigital.com/justiceleague/2009/08/14/proper-use-of-javas-securerandom/
		byte[] key = new byte[8];
		keyGenerator.nextBytes(key);
		String hexKey = new BigInteger(key).toString(16);
		return hexKey;
	}
}
