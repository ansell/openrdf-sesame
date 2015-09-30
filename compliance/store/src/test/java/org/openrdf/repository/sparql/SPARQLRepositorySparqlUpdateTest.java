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
package org.openrdf.repository.sparql;

import org.junit.Ignore;
import org.junit.Test;

import org.openrdf.query.parser.sparql.SPARQLUpdateTest;
import org.openrdf.repository.Repository;
import org.openrdf.repository.http.HTTPMemServer;

/**
 * @author Jeen Broekstra
 */
//public class SPARQLRepositorySparqlUpdateTest extends SPARQLUpdateTest {
//
//	private HTTPMemServer server;
//
//	@Override
//	public void setUp()
//		throws Exception
//	{
//		server = new HTTPMemServer();
//		
//		try {
//			server.start();
//			super.setUp();
//		}
//		catch (Exception e) {
//			server.stop();
//			throw e;
//		}
//	}
//
//	@Override
//	public void tearDown()
//		throws Exception
//	{
//		super.tearDown();
//		server.stop();
//	}
//
//	@Override
//	protected Repository newRepository()
//		throws Exception
//	{
//		return new SPARQLRepository(HTTPMemServer.REPOSITORY_URL, HTTPMemServer.REPOSITORY_URL + "/statements");
//	}
//
//	@Ignore
//	@Test
//	@Override
//	public void testAutoCommitHandling() 
//	{
//		// transaction isolation is not supported for HTTP connections. disabling test.
//		System.err.println("temporarily disabled testAutoCommitHandling() for HTTPRepository");
//	}
//}
