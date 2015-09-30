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
package org.openrdf.rio.jsonld;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.helpers.ParseErrorCollector;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.jsonld.JSONLDInternalTripleCallback;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

/**
 * @author Peter Ansell
 */
public class JSONLDInternalTripleCallbackTest {

	@Test
	public void triplesTest()
		throws JsonLdError, IOException
	{
		// String inputstring =
		// "{\"@id\":{\"@id\":\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/machine/DVC-1_8\"},\"http://igreen-projekt.de/ontologies/isoxml#deviceElement\":\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceelement/DET-1_8\",\"http://igreen-projekt.de/ontologies/isoxml#deviceID\":{\"@datatype\":\"http://www.w3.org/2001/XMLSchema#string\",\"@literal\":\"DVC-1\"},\"http://igreen-projekt.de/ontologies/isoxml#deviceLocalizationLabel\":{\"@datatype\":\"http://www.w3.org/2001/XMLSchema#string\",\"@literal\":\"FF000000406564\"},\"http://igreen-projekt.de/ontologies/isoxml#deviceProcessData\":[\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/13_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/6_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/14_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/11_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/8_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/4_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/5_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/10_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/2_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/21_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/15_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/16_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/19_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/17_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/3_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/12_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/7_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/18_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/9_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/22_8\",\"http://pc-4107.kl.dfki.de:38080/onlinebox/resource/deviceprocessdata/20_8\"],\"http://igreen-projekt.de/ontologies/isoxml#deviceSerialNumber\":{\"@datatype\":\"http://www.w3.org/2001/XMLSchema#string\",\"@literal\":\"12345\"},\"http://igreen-projekt.de/ontologies/isoxml#deviceSoftwareVersion\":{\"@datatype\":\"http://www.w3.org/2001/XMLSchema#string\",\"@literal\":\"01.009\"},\"http://igreen-projekt.de/ontologies/isoxml#deviceStructureLabel\":{\"@datatype\":\"http://www.w3.org/2001/XMLSchema#string\",\"@literal\":\"31303030303030\"},\"http://igreen-projekt.de/ontologies/isoxml#workingSetMasterNAME\":{\"@datatype\":\"http://www.w3.org/2001/XMLSchema#string\",\"@literal\":\"A000860020800001\"},\"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\":{\"@iri\":\"http://www.agroxml.de/rdfs#Machine\"},\"http://www.w3.org/2000/01/rdf-schema#label\":{\"@datatype\":\"http://www.w3.org/2001/XMLSchema#string\",\"@literal\":\"Krone Device\"}}";
		final String inputstring = "{ \"@id\":\"http://nonexistent.com/abox#Document1823812\", \"@type\":\"http://nonexistent.com/tbox#Document\" }";
		final String expectedString = "(http://nonexistent.com/abox#Document1823812, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://nonexistent.com/tbox#Document) [null]";
		final Object input = JsonUtils.fromString(inputstring);

		final Graph graph = new LinkedHashModel();
		final ParseErrorCollector parseErrorListener = new ParseErrorCollector();
		final ParserConfig parserConfig = new ParserConfig();
		final JSONLDInternalTripleCallback callback = new JSONLDInternalTripleCallback(new StatementCollector(
				graph), SimpleValueFactory.getInstance(), parserConfig, parseErrorListener);

		JsonLdProcessor.toRDF(input, callback);

		final Iterator<Statement> statements = graph.iterator();

		// contains only one statement (type)
		while (statements.hasNext()) {
			final Statement stmt = statements.next();

			System.out.println(stmt.toString());
			assertEquals("Output was not as expected", stmt.toString(), expectedString);
		}

		assertEquals(0, parseErrorListener.getFatalErrors().size());
		assertEquals(0, parseErrorListener.getErrors().size());
		assertEquals(0, parseErrorListener.getWarnings().size());
	}

}
