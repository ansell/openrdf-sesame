/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.http;

import junit.framework.TestCase;

public class HttpSailTest extends TestCase {

	private HTTPSail _sail;

	private String _serverUrl;

	protected void setUp()
		throws Exception
	{
		super.setUp();
		_sail = new HTTPSail();
		// dummy server URL
		_serverUrl = "http://localhost:18080/openrdf";
	}

	protected void tearDown()
		throws Exception
	{
		super.tearDown();
	}

	public void testSetParams() {
		_sail.setParameter("serverurl", _serverUrl);
		_sail.setParameter("repositoryid", "mem-rdf");
		// don't initialize SAIL in unit test, it requires a running server
		// _sail.initialize();
		assertTrue(_sail.getRepositoryId().equals("mem-rdf"));
		assertTrue(_sail.getServerUrl().equals(_serverUrl));

	}

	public void testIllegalParam() {
		try {
			_sail.setParameter("illegal", "bla");
			fail("Should have raised an IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
			// expected.printStackTrace();
		}
	}
}
