/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.cas;

/**
 * @author Arjohn Kampman
 */
public class ProxySuccess implements ServiceResponse {

	private final String proxyTicket;

	ProxySuccess(String proxyTicket) {
		this.proxyTicket = proxyTicket;
	}

	public String getProxyTicket() {
		return proxyTicket;
	}

	@Override
	public String toString() {
		return "proxyTicket=" + proxyTicket;
	}
}