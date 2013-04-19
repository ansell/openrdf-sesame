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
package org.openrdf.rio.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.AbstractParserHandlingTest;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriter;

/**
 * Test for error handling by Binary Parser.
 * 
 * @author Peter Ansell
 */
public class BinaryHandlingTest extends AbstractParserHandlingTest {

	@Override
	protected InputStream getUnknownDatatypeStream(Model unknownDatatypeStatements)
		throws Exception
	{
		return writeBinary(unknownDatatypeStatements);
	}

	@Override
	protected InputStream getKnownDatatypeStream(Model knownDatatypeStatements)
		throws Exception
	{
		return writeBinary(knownDatatypeStatements);
	}

	@Override
	protected InputStream getUnknownLanguageStream(Model unknownLanguageStatements)
		throws Exception
	{
		return writeBinary(unknownLanguageStatements);
	}

	@Override
	protected InputStream getKnownLanguageStream(Model knownLanguageStatements)
		throws Exception
	{
		return writeBinary(knownLanguageStatements);
	}

	@Override
	protected RDFParser getParser() {
		return new BinaryRDFParser();
	}

	/**
	 * Helper method to write the given model to N-Triples and return an
	 * InputStream containing the results.
	 * 
	 * @param statements
	 * @return An {@link InputStream} containing the results.
	 * @throws RDFHandlerException
	 */
	private InputStream writeBinary(Model statements)
		throws RDFHandlerException
	{
		ByteArrayOutputStream output = new ByteArrayOutputStream(8096);

		RDFWriter binaryWriter = new BinaryRDFWriter(output);
		binaryWriter.startRDF();
		for (Statement nextStatement : statements) {
			binaryWriter.handleStatement(nextStatement);
		}
		binaryWriter.endRDF();

		return new ByteArrayInputStream(output.toByteArray());
	}

}
