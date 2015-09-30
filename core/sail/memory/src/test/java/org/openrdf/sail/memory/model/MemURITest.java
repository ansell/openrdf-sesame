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
package org.openrdf.sail.memory.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;

/**
 * Unit tests for class {@link MemIRI}.
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
		IRI uriImpl = SimpleValueFactory.getInstance().createIRI(uri);
		MemIRI memURI = new MemIRI(this, uriImpl.getNamespace(), uriImpl.getLocalName());

		assertEquals("MemURI not equal to URIImpl for: " + uri, uriImpl, memURI);
		assertEquals("MemURI has different hash code than URIImpl for: " + uri, uriImpl.hashCode(),
				memURI.hashCode());
	}
}
