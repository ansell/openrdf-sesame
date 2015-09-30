/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package org.eclipse.rdf4j.http.protocol;

import static org.eclipse.rdf4j.http.protocol.Protocol.CONFIG;
import static org.eclipse.rdf4j.http.protocol.Protocol.CONTEXTS;
import static org.eclipse.rdf4j.http.protocol.Protocol.NAMESPACES;
import static org.eclipse.rdf4j.http.protocol.Protocol.PROTOCOL;
import static org.eclipse.rdf4j.http.protocol.Protocol.REPOSITORIES;
import static org.eclipse.rdf4j.http.protocol.Protocol.getConfigLocation;
import static org.eclipse.rdf4j.http.protocol.Protocol.getContextsLocation;
import static org.eclipse.rdf4j.http.protocol.Protocol.getNamespacesLocation;
import static org.eclipse.rdf4j.http.protocol.Protocol.getProtocolLocation;
import static org.eclipse.rdf4j.http.protocol.Protocol.getRepositoriesLocation;
import static org.eclipse.rdf4j.http.protocol.Protocol.getRepositoryID;
import static org.eclipse.rdf4j.http.protocol.Protocol.getRepositoryLocation;
import static org.eclipse.rdf4j.http.protocol.Protocol.getServerLocation;
import static org.junit.Assert.assertEquals;

import org.eclipse.rdf4j.http.protocol.Protocol;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Test;

public class ProtocolTest {

	private static final String serverLocation = "http://localhost/openrdf";

	private static final String repositoryID = "mem-rdf";

	private static final String repositoryLocation = serverLocation + "/" + REPOSITORIES + "/" + repositoryID;

	@Test
	public void testGetProtocolLocation() {
		String result = getProtocolLocation(serverLocation);
		assertEquals(result, serverLocation + "/" + PROTOCOL);
	}

	@Test
	public void testGetConfigLocation() {
		String result = getConfigLocation(serverLocation);
		assertEquals(result, serverLocation + "/" + CONFIG);
	}

	@Test
	public void testGetRepositoriesLocation() {
		String result = getRepositoriesLocation(serverLocation);
		assertEquals(result, serverLocation + "/" + REPOSITORIES);
	}

	@Test
	public void testGetServerLocation() {
		String repositoryLocation = getRepositoryLocation(serverLocation, repositoryID);

		String result = getServerLocation(repositoryLocation);
		assertEquals(serverLocation, result);
	}


	@Test
	public void testGetRepositoryID() {
		String repositoryLocation = getRepositoryLocation(serverLocation, repositoryID);

		String result = getRepositoryID(repositoryLocation);
		assertEquals(repositoryID, result);
	}

	@Test
	public void testGetRepositoryLocation() {
		String result = getRepositoryLocation(serverLocation, repositoryID);
		assertEquals(result, repositoryLocation);
	}

	@Test
	public void testGetContextsLocation() {
		String result = getContextsLocation(repositoryLocation);
		assertEquals(result, repositoryLocation + "/" + CONTEXTS);
	}

	@Test
	public void testGetNamespacesLocation() {
		String result = getNamespacesLocation(repositoryLocation);
		assertEquals(result, repositoryLocation + "/" + NAMESPACES);
	}

	@Test
	public void testEncodeValueRoundtrip() {
		final ValueFactory vf = SimpleValueFactory.getInstance();
		IRI uri = vf.createIRI("http://example.org/foo-bar");

		String encodedUri = Protocol.encodeValue(uri);
		IRI decodedUri = (IRI)Protocol.decodeValue(encodedUri, vf);

		assertEquals(uri, decodedUri);

		BNode bnode = vf.createBNode("foo-bar-1");
		String encodedBnode = Protocol.encodeValue(bnode);

		BNode decodedNode = (BNode)Protocol.decodeValue(encodedBnode, vf);
		assertEquals(bnode, decodedNode);

	}
}
