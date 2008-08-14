/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import junit.framework.TestCase;

/**
 * @author Arjohn Kampman
 */
public class URIUtilTest extends TestCase {

	public void testIsCorrectURISplit()
		throws Exception
	{
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page#", ""));
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page#", "1"));
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page#", "1/2"));
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page#", "1:2"));
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page#", "1#2"));
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page/", ""));
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page/", "1"));
		assertTrue(URIUtil.isCorrectURISplit("http://www.example.org/page/", "1:2"));
		assertTrue(URIUtil.isCorrectURISplit("isbn:", ""));
		assertTrue(URIUtil.isCorrectURISplit("isbn:", "1"));

		assertFalse(URIUtil.isCorrectURISplit("http://www.example.org/page#1#", "2"));
		assertFalse(URIUtil.isCorrectURISplit("http://www.example.org/page", "#1"));
		assertFalse(URIUtil.isCorrectURISplit("http://www.example.org/page/", "1/2"));
		assertFalse(URIUtil.isCorrectURISplit("http://www.example.org/page/", "1#2"));
		assertFalse(URIUtil.isCorrectURISplit("http://www.example.org/page", "2"));
		assertFalse(URIUtil.isCorrectURISplit("http://www.example.org/page/1:", "2"));
		assertFalse(URIUtil.isCorrectURISplit("isbn:", "1#2"));
		assertFalse(URIUtil.isCorrectURISplit("isbn:", "1/2"));
		assertFalse(URIUtil.isCorrectURISplit("isbn:", "1:2"));
	}
}
