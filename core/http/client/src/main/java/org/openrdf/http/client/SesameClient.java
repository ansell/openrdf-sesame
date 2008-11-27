/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import static org.openrdf.http.protocol.Protocol.CONFIGURATIONS;
import static org.openrdf.http.protocol.Protocol.PROTOCOL;
import static org.openrdf.http.protocol.Protocol.REPOSITORIES;
import static org.openrdf.http.protocol.Protocol.SCHEMAS;
import static org.openrdf.http.protocol.Protocol.TEMPLATES;

import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.model.ValueFactory;

/**
 * Low-level HTTP client for Sesame's HTTP protocol. Methods correspond directly
 * to the functionality offered by the protocol.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
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
		return new ProtocolClient(server.slash(PROTOCOL));
	}

	public SchemaClient schemas() {
		return new SchemaClient(server.slash(SCHEMAS));
	}

	public TemplateClient templates() {
		return new TemplateClient(server.slash(TEMPLATES));
	}

	public ConfigurationClient configurations() {
		return new ConfigurationClient(server.slash(CONFIGURATIONS));
	}

	public RepositoriesClient repositories() {
		return new RepositoriesClient(server.slash(REPOSITORIES));
	}

}
