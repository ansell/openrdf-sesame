/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

import junit.framework.TestCase;

/**
 * @author Arjohn Kampman
 */
public class ModelUtilTest extends TestCase {

	public void testIsCorrectURISplit()
		throws Exception
	{
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page#", ""));
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page#", "1"));
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page#", "1/2"));
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page#", "1:2"));
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page#", "1#2"));
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page/", ""));
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page/", "1"));
		assertTrue(ModelUtil.isCorrectURISplit("http://www.example.org/page/", "1:2"));
		assertTrue(ModelUtil.isCorrectURISplit("isbn:", ""));
		assertTrue(ModelUtil.isCorrectURISplit("isbn:", "1"));
		
		assertFalse(ModelUtil.isCorrectURISplit("http://www.example.org/page#1#", "2"));
		assertFalse(ModelUtil.isCorrectURISplit("http://www.example.org/page", "#1"));
		assertFalse(ModelUtil.isCorrectURISplit("http://www.example.org/page/", "1/2"));
		assertFalse(ModelUtil.isCorrectURISplit("http://www.example.org/page/", "1#2"));
		assertFalse(ModelUtil.isCorrectURISplit("http://www.example.org/page", "2"));
		assertFalse(ModelUtil.isCorrectURISplit("isbn:", "1#2"));
		assertFalse(ModelUtil.isCorrectURISplit("isbn:", "1/2"));
		assertFalse(ModelUtil.isCorrectURISplit("isbn:", "1:2"));
	}
}
