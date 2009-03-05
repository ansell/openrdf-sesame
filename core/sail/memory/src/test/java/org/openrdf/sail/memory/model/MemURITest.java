/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import junit.framework.TestCase;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 * Unit tests for class {@link MemURI}.
 * 
 * @author Arjohn Kampman
 */
public class MemURITest extends TestCase {

	/**
	 * Verifies that MemURI's hash code is the same as the hash code of an
	 * equivalent URIImpl.
	 */
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
