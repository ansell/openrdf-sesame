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
package org.eclipse.rdf4j.rio.binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.AbstractParserHandlingTest;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.binary.BinaryRDFParser;
import org.eclipse.rdf4j.rio.binary.BinaryRDFWriter;

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
