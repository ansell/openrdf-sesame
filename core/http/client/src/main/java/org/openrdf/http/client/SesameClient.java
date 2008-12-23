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

	public SesameClient(HTTPConnectionPool info) {
		this.server = info;
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
