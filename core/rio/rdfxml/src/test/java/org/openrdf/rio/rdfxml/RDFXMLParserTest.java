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
package org.openrdf.rio.rdfxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;


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
