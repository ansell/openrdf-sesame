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
package org.openrdf.sail.memory.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * Unit tests for class {@link MemURI}.
 *
 * @author Arjohn Kampman
 */
public class MemURITest {

	/**
	 * Verifies that MemURI's hash code is the same as the hash code of an
	 * equivalent URIImpl.
	 */
	@Test
	public void testEqualsAndHash()
		throws Exception
	{
		compareURIs(RDF.NAMESPACE);
		compareURIs(RDF.TYPE.toString());
		compareURIs("foo:bar");
		compareURIs("http://www.example.org/");
		compareURIs("http://www.example.org/foo#bar");
	}

	private void compareURIs(String uri)
		throws Exception
	{
		URIImpl uriImpl = new URIImpl(uri);
		MemURI memURI = new MemURI(this, uriImpl.getNamespace(), uriImpl.getLocalName());

		assertEquals("MemURI not equal to URIImpl for: " + uri, uriImpl, memURI);
		assertEquals("MemURI has different hash code than URIImpl for: " + uri, uriImpl.hashCode(),
				memURI.hashCode());
	}
}
