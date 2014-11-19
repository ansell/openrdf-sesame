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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Arjohn Kampman
 */
public class URIUtilTest {

	@Test
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

	@Test
	public void testIsValidURIReference()
		throws Exception
	{
		assertTrue(URIUtil.isValidURIReference("http://example.org/foo/bar/"));
		assertTrue("whitespace should be allowed", URIUtil.isValidURIReference("http://example.org/foo/bar with a lot of space/"));
		assertTrue("unwise chars should be allowed", URIUtil.isValidURIReference("http://example.org/foo/bar/unwise{<characters>}"));
		assertTrue("query params in single quotes should be allowed", URIUtil.isValidURIReference("http://example.org/foo/bar?query='blah'"));
		assertTrue("query params in double quotes should be allowed", URIUtil.isValidURIReference("http://example.org/foo/bar?query=\"blah\"&foo=bar"));
		assertTrue("short simple urns should be allowed", URIUtil.isValidURIReference("urn:p1"));
		assertTrue("Escaped special char should be allowed",
				URIUtil.isValidURIReference("http://example.org/foo\\u00ea/bar/"));
		assertTrue("fragment identifier should be allowed", URIUtil.isValidURIReference("http://example.org/foo/bar#fragment1"));
		assertTrue("Unescaped special char should be allowed",
				URIUtil.isValidURIReference("http://example.org/foo®/bar/"));
		assertFalse("control char should not be allowed",
				URIUtil.isValidURIReference("http://example.org/foo\u0001/bar/"));
		assertFalse("relative uri should fail", URIUtil.isValidURIReference("foo/bar/"));
		assertFalse("single column is not a valid uri", URIUtil.isValidURIReference(":"));
		assertTrue("reserved char is allowed in non-conflicting spot", URIUtil.isValidURIReference("http://foo.com/b!ar/"));
		assertFalse("reserved char should not be allowed in conflicting spot", URIUtil.isValidURIReference("http;://foo.com/bar/"));
	}
}
