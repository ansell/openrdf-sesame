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
package org.openrdf.rio.jsonld;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.openrdf.model.Model;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.rio.AbstractParserHandlingTest;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.jsonld.JSONLDParser;
import org.openrdf.rio.jsonld.JSONLDWriter;

/**
 * Unit tests for {@link JSONLDParser} related to handling of datatypes and
 * languages.
 * 
 * @author Peter Ansell
 */
public class JSONLDParserHandlerTest extends AbstractParserHandlingTest {

	@Override
	protected InputStream getUnknownDatatypeStream(Model unknownDatatypeStatements)
		throws Exception
	{
		return writeJSONLD(unknownDatatypeStatements);
	}

	@Override
	protected InputStream getKnownDatatypeStream(Model knownDatatypeStatements)
		throws Exception
	{
		return writeJSONLD(knownDatatypeStatements);
	}

	@Override
	protected InputStream getUnknownLanguageStream(Model unknownLanguageStatements)
		throws Exception
	{
		return writeJSONLD(unknownLanguageStatements);
	}

	@Override
	protected InputStream getKnownLanguageStream(Model knownLanguageStatements)
		throws Exception
	{
		return writeJSONLD(knownLanguageStatements);
	}

	@Override
	protected RDFParser getParser() {
		return new JSONLDParser();
	}

	/**
	 * Helper method to write the given model to JSON-LD and return an
	 * InputStream containing the results.
	 * 
	 * @param statements
	 * @return An {@link InputStream} containing the results.
	 * @throws RDFHandlerException
	 */
	private InputStream writeJSONLD(Model statements)
		throws RDFHandlerException
	{
		final StringWriter writer = new StringWriter();

		final RDFWriter jsonldWriter = new JSONLDWriter(writer);
		jsonldWriter.startRDF();
		for (final Namespace prefix : statements.getNamespaces()) {
			jsonldWriter.handleNamespace(prefix.getPrefix(), prefix.getName());
		}
		for (final Statement nextStatement : statements) {
			jsonldWriter.handleStatement(nextStatement);
		}
		jsonldWriter.endRDF();

		// System.out.println(writer.toString());

		return new ByteArrayInputStream(writer.toString().getBytes(Charset.forName("UTF-8")));
	}

}
