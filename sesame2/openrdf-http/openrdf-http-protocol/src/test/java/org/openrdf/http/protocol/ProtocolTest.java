/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.http.protocol;

import static org.openrdf.http.protocol.Protocol.CONFIG;
import static org.openrdf.http.protocol.Protocol.CONTEXTS;
import static org.openrdf.http.protocol.Protocol.NAMESPACES;
import static org.openrdf.http.protocol.Protocol.PROTOCOL;
import static org.openrdf.http.protocol.Protocol.REPOSITORIES;
import static org.openrdf.http.protocol.Protocol.getConfigLocation;
import static org.openrdf.http.protocol.Protocol.getContextsLocation;
import static org.openrdf.http.protocol.Protocol.getNamespacesLocation;
import static org.openrdf.http.protocol.Protocol.getProtocolLocation;
import static org.openrdf.http.protocol.Protocol.getRepositoriesLocation;
import static org.openrdf.http.protocol.Protocol.getRepositoryLocation;
import junit.framework.TestCase;

public class ProtocolTest extends TestCase {

	private static final String serverLocation = "http://localhost/openrdf";

	private static final String repositoryID = "mem-rdf";

	private static final String repositoryLocation = serverLocation + "/" + REPOSITORIES + "/" + repositoryID;

	public void testGetProtocolLocation() {
		String result = getProtocolLocation(serverLocation);
		assertEquals(result, serverLocation + "/" + PROTOCOL);
	}

	public void testGetConfigLocation() {
		String result = getConfigLocation(serverLocation);
		assertEquals(result, serverLocation + "/" + CONFIG);
	}

	public void testGetRepositoriesLocation() {
		String result = getRepositoriesLocation(serverLocation);
		assertEquals(result, serverLocation + "/" + REPOSITORIES);
	}

	public void testGetRepositoryLocation() {
		String result = getRepositoryLocation(serverLocation, repositoryID);
		assertEquals(result, repositoryLocation);
	}

	public void testGetContextsLocation() {
		String result = getContextsLocation(repositoryLocation);
		assertEquals(result, repositoryLocation + "/" + CONTEXTS);
	}

	public void testGetNamespacesLocation() {
		String result = getNamespacesLocation(repositoryLocation);
		assertEquals(result, repositoryLocation + "/" + NAMESPACES);
	}
}
