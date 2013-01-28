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
