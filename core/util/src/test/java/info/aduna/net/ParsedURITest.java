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
package info.aduna.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class ParsedURITest {
	@Test
	public void absoluteHttpUriIsDescribedCorrectly() {
		ParsedURI uri = new ParsedURI("http://example.test/");
		assertTrue(uri.isAbsolute());
		assertTrue(uri.isHierarchical());
		assertEquals("http", uri.getScheme());
		assertFalse(uri.isOpaque());
	}

	@Test
	public void uriReferenceIsDescribedCorrectly() {
		ParsedURI uri = new ParsedURI("/path");
		assertFalse(uri.isAbsolute());
		assertTrue(uri.isHierarchical());
		assertNull(uri.getScheme());
		assertFalse(uri.isOpaque());
	}

	@Test
	public void jarUrisAppearAsAbsoluteAndHierarchical() {
		ParsedURI uri = new ParsedURI("jar:http://example.test/bar/baz.jar!/COM/foo/Quux.class");
		assertTrue(uri.isAbsolute());
		assertTrue(uri.isHierarchical());
		assertFalse(uri.isOpaque());
		assertEquals("/COM/foo/Quux.class", uri.getPath());

		uri.normalize();
		assertEquals("/COM/foo/Quux.class", uri.getPath());
	}

	@Test
	public void jarUriWithHttpStringifiesToOriginalForm() {
		ParsedURI uri = new ParsedURI("jar:http://example.test/bar/baz.jar!/COM/foo/Quux.class");
		assertEquals("jar:http://example.test/bar/baz.jar!/COM/foo/Quux.class", uri.toString());
	}

	@Test
	public void jarUriWithFileStringifiesToOriginalForm() {
		ParsedURI uri = new ParsedURI("jar:file:///some-file.jar!/another-file");
		System.out.println(uri.getScheme());
		assertEquals("jar:file:///some-file.jar!/another-file", uri.toString());
	}

	@Test
	public void resolvesAnAbsoluteUriRelativeToABaseJarUri() {
		ParsedURI uri = new ParsedURI("jar:file:///some-file.jar!/some-nested-file");
		assertEquals("http://example.test/", uri.resolve("http://example.test/").toString());
	}

	@Test
	public void resolvesAPathRelativeUriRelativeToABaseJarUri() {
		ParsedURI uri = new ParsedURI("jar:file:///some-file.jar!/some-nested-file");
		assertEquals("jar:file:///some-file.jar!/another-file", uri.resolve("another-file").toString());
	}

	@Test
	public void resolvesAPathAbsoluteUriRelativeToABaseJarUri() {
		ParsedURI uri = new ParsedURI("jar:file:///some-file.jar!/nested-directory/some-nested-file");
		assertEquals("jar:file:///some-file.jar!/another-file", uri.resolve("/another-file").toString());
	}
}
