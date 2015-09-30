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
package org.eclipse.rdf4j.model.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.rdf4j.model.util.URIUtil;
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
