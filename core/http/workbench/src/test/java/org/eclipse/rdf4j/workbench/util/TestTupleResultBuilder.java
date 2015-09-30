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
package org.eclipse.rdf4j.workbench.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLBooleanXMLWriter;
import org.eclipse.rdf4j.workbench.util.TupleResultBuilder;
import org.junit.Test;

/**
 * @author Dale Visser
 */
public class TestTupleResultBuilder {

	@Test
	public final void testSES1780regression()
		throws Exception
	{
		TupleResultBuilder builder = new TupleResultBuilder(new SPARQLResultsJSONWriter(
				new ByteArrayOutputStream()), SimpleValueFactory.getInstance());
		builder.start("test");
		builder.namedResult("test", new URL("http://www.foo.org/bar#"));
		builder.end();
	}

	@Test
	public final void testSES1726regression()
		throws Exception
	{
		TupleResultBuilder builder = new TupleResultBuilder(new SPARQLResultsJSONWriter(
				new ByteArrayOutputStream()), SimpleValueFactory.getInstance());
		try {
			builder.namedResult("test", new URL("http://www.foo.org/bar#"));
			fail("Did not receive expected exception for calling namedResult before start");
		}
		catch (IllegalStateException ise) {
			// Expected exception
		}
	}

	@Test
	public final void testSES1846Normal()
		throws Exception
	{
		TupleResultBuilder builder = new TupleResultBuilder(new SPARQLBooleanXMLWriter(
				new ByteArrayOutputStream()), SimpleValueFactory.getInstance());
		builder.startBoolean();
		builder.bool(true);
		builder.endBoolean();
	}

	@Test
	public final void testSES1846regression()
		throws Exception
	{
		TupleResultBuilder builder = new TupleResultBuilder(new SPARQLBooleanXMLWriter(
				new ByteArrayOutputStream()), SimpleValueFactory.getInstance());
		try {
			builder.start();
			builder.bool(true);
			fail("Did not receive expected exception for calling bool after start");
		}
		catch (QueryResultHandlerException qrhe) {
			// Expected exception
		}
	}

}
