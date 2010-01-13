/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.auth.cas;

/**
 * @author Arjohn Kampman
 */
class ProxySuccess implements ServiceResponse {

	final String proxyTicket;

	ProxySuccess(String proxyTicket) {
		this.proxyTicket = proxyTicket;
	}

	@Override
	public String toString() {
		return "proxyTicket=" + proxyTicket;
	}
}