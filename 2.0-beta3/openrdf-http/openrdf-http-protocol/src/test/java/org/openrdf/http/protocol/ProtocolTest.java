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
		assertEquals(result, serverLocation + "/" + REPOSITORIES + "/" + repositoryID);
	}

	public void testGetContextsLocation() {
		String result = getContextsLocation(serverLocation, repositoryID);
		assertEquals(result, serverLocation + "/" + REPOSITORIES + "/" + repositoryID + "/" + CONTEXTS);
	}

	public void testGetNamespacesLocation() {
		String result = getNamespacesLocation(serverLocation, repositoryID);
		assertEquals(result, serverLocation + "/" + REPOSITORIES + "/" + repositoryID + "/" + NAMESPACES);
	}
}
