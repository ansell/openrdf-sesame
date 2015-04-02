/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
import static org.openrdf.http.protocol.Protocol.getServerLocation;
import static org.openrdf.http.protocol.Protocol.getRepositoryID;

import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;

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
	
	public void testGetServerLocation() {
		String repositoryLocation = getRepositoryLocation(serverLocation, repositoryID);
		
		String result = getServerLocation(repositoryLocation);
		assertEquals(serverLocation, result);
	}
	
	
	public void testGetRepositoryID() {
		String repositoryLocation = getRepositoryLocation(serverLocation, repositoryID);
		
		String result = getRepositoryID(repositoryLocation);
		System.out.println(result);
		assertEquals(repositoryID, result);
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
