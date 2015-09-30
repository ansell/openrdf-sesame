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
package org.eclipse.rdf4j.rio.rdfxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DC;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;


public class RDFXMLParserTest {

	@Test
	public void rdfXmlLoadedFromInsideAJarResolvesRelativeUris() throws Exception {
		URL zipfileUrl = RDFXMLParserTest.class.getResource("sample-with-rdfxml-data.zip");

		assertNotNull("The sample-data.zip file must be present for this test", zipfileUrl);

		ValueFactory vf = SimpleValueFactory.getInstance();
		
		String url = "jar:" + zipfileUrl + "!/index.rdf";

		RDFParser parser = new RDFXMLParser();

		StatementCollector sc = new StatementCollector();
		parser.setRDFHandler(sc);

		InputStream in = new URL(url).openStream();
		parser.parse(in, url);
		in.close();

		Collection<Statement> stmts = sc.getStatements();

		assertThat(stmts, Matchers.<Statement>iterableWithSize(2));

		Iterator<Statement> iter = stmts.iterator();

		Statement stmt1 = iter.next(),
				stmt2 = iter.next();

		assertEquals(vf.createIRI("http://www.example.com/#"), stmt1.getSubject());
		assertEquals(vf.createIRI("http://www.example.com/ns/#document-about"), stmt1.getPredicate());

		Resource res = (Resource) stmt1.getObject();

		String resourceUrl = res.stringValue();

		assertThat(resourceUrl, CoreMatchers.startsWith("jar:" + zipfileUrl + "!"));

		URL javaUrl = new URL(resourceUrl);
		assertEquals("jar", javaUrl.getProtocol());

		InputStream uc = javaUrl.openStream();
		assertEquals("The resource stream should be empty", -1, uc.read());
		uc.close();

		assertEquals(res, stmt2.getSubject());
		assertEquals(DC.TITLE, stmt2.getPredicate());
		assertEquals(vf.createLiteral("Empty File"), stmt2.getObject());
	}
}
