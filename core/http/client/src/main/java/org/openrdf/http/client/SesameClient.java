/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.ProtocolClient;
import org.openrdf.http.client.helpers.RepositoriesClient;
import org.openrdf.model.ValueFactory;

/**
 * Low-level HTTP client for Sesame's HTTP protocol. Methods correspond directly
 * to the functionality offered by the protocol.
 * 
 * @author James Leigh
 */
public class SesameClient {

	private HTTPConnectionPool server;

	public SesameClient(String serverURL) {
		this.server = new HTTPConnectionPool(serverURL);
	}

	public SesameClient(HTTPConnectionPool info) {
		this.server = info;
	}

	public String getURL() {
		return server.getURL();
	}

	public void setUsernameAndPassword(String username, String password) {
		server.setUsernameAndPassword(username, password);
	}

	public ValueFactory getValueFactory() {
		return server.getValueFactory();
	}

	public void setValueFactory(ValueFactory valueFactory) {
		server.setValueFactory(valueFactory);
	}

	public HTTPConnectionPool getPool() {
		return server;
	}

	public ProtocolClient protocol() {
		return new ProtocolClient(server.slash("protocol"));
	}

	public RepositoriesClient repositories() {
		return new RepositoriesClient(server.slash("repositories"));
	}

}
