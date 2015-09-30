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
package org.openrdf.rio.binary;

import static org.openrdf.rio.binary.BinaryRDFConstants.BNODE_VALUE;
import static org.openrdf.rio.binary.BinaryRDFConstants.COMMENT;
import static org.openrdf.rio.binary.BinaryRDFConstants.DATATYPE_LITERAL_VALUE;
import static org.openrdf.rio.binary.BinaryRDFConstants.END_OF_DATA;
import static org.openrdf.rio.binary.BinaryRDFConstants.FORMAT_VERSION;
import static org.openrdf.rio.binary.BinaryRDFConstants.LANG_LITERAL_VALUE;
import static org.openrdf.rio.binary.BinaryRDFConstants.MAGIC_NUMBER;
import static org.openrdf.rio.binary.BinaryRDFConstants.NAMESPACE_DECL;
import static org.openrdf.rio.binary.BinaryRDFConstants.NULL_VALUE;
import static org.openrdf.rio.binary.BinaryRDFConstants.PLAIN_LITERAL_VALUE;
import static org.openrdf.rio.binary.BinaryRDFConstants.STATEMENT;
import static org.openrdf.rio.binary.BinaryRDFConstants.URI_VALUE;
import static org.openrdf.rio.binary.BinaryRDFConstants.VALUE_DECL;
import static org.openrdf.rio.binary.BinaryRDFConstants.VALUE_REF;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;

import info.aduna.io.IOUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.AbstractRDFParser;

/**
 * @author Arjohn Kampman
 */
public class BinaryRDFParser extends AbstractRDFParser {

	private Value[] declaredValues = new Value[16];

	private DataInputStream in;

	private byte[] buf = null;

	public RDFFormat getRDFFormat() {
		return RDFFormat.BINARY;
	}

	public void parse(Reader reader, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		throw new UnsupportedOperationException();
	}

	public void parse(InputStream in, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (in == null) {
			throw new IllegalArgumentException("Input stream must not be null");
		}

		this.in = new DataInputStream(new BufferedInputStream(in));

		// Check magic number
		byte[] magicNumber = IOUtil.readBytes(in, MAGIC_NUMBER.length);
		if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
			reportFatalError("File does not contain a binary RDF document");
		}

		// Check format version (parser is backward-compatible with version 1 and
		// version 2)
		int formatVersion = this.in.readInt();
		if (formatVersion != FORMAT_VERSION) {
			reportFatalError("Incompatible format version: " + formatVersion);
		}

		if (rdfHandler != null) {
			rdfHandler.startRDF();
		}

		try {
			loop: while (true) {
				int recordType = this.in.readByte();

				switch (recordType) {
					case END_OF_DATA:
						break loop;
					case STATEMENT:
						readStatement();
						break;
					case VALUE_DECL:
						readValueDecl();
						break;
					case NAMESPACE_DECL:
						readNamespaceDecl();
						break;
					case COMMENT:
						readComment();
						break;
					default:
						reportFatalError("Invalid record type: " + recordType);
				}
			}
		}
		finally {
			clear();
		}

		if (rdfHandler != null) {
			rdfHandler.endRDF();
		}
	}

	private void readNamespaceDecl()
		throws IOException, RDFHandlerException
	{
		String prefix = readString();
		String namespace = readString();
		if (rdfHandler != null) {
			rdfHandler.handleNamespace(prefix, namespace);
		}
	}

	private void readComment()
		throws IOException, RDFHandlerException
	{
		String comment = readString();
		if (rdfHandler != null) {
			rdfHandler.handleComment(comment);
		}
	}

	private void readValueDecl()
		throws IOException, RDFParseException
	{
		int id = in.readInt();
		Value v = readValue();

		if (id >= declaredValues.length) {
			// grow array
			Value[] newArray = new Value[2 * declaredValues.length];
			System.arraycopy(declaredValues, 0, newArray, 0, declaredValues.length);
			declaredValues = newArray;
		}

		declaredValues[id] = v;
	}

	private void readStatement()
		throws RDFParseException, IOException, RDFHandlerException
	{
		Value v = readValue();
		Resource subj = null;
		if (v instanceof Resource) {
			subj = (Resource)v;
		}
		else {
			reportFatalError("Invalid subject type: " + v);
		}

		v = readValue();
		IRI pred = null;
		if (v instanceof IRI) {
			pred = (IRI)v;
		}
		else {
			reportFatalError("Invalid predicate type: " + v);
		}

		Value obj = readValue();
		if (obj == null) {
			reportFatalError("Invalid object type: null");
		}

		v = readValue();
		Resource context = null;
		if (v == null || v instanceof Resource) {
			context = (Resource)v;
		}
		else {
			reportFatalError("Invalid context type: " + v);
		}

		Statement st = createStatement(subj, pred, obj, context);
		if (rdfHandler != null) {
			rdfHandler.handleStatement(st);
		}
	}

	private Value readValue()
		throws RDFParseException, IOException
	{
		byte valueType = in.readByte();
		switch (valueType) {
			case NULL_VALUE:
				return null;
			case VALUE_REF:
				return readValueRef();
			case URI_VALUE:
				return readURI();
			case BNODE_VALUE:
				return readBNode();
			case PLAIN_LITERAL_VALUE:
				return readPlainLiteral();
			case LANG_LITERAL_VALUE:
				return readLangLiteral();
			case DATATYPE_LITERAL_VALUE:
				return readDatatypeLiteral();
			default:
				reportFatalError("Unknown value type: " + valueType);
				return null;
		}
	}

	private Value readValueRef()
		throws IOException, RDFParseException
	{
		int id = in.readInt();
		return declaredValues[id];
	}

	private IRI readURI()
		throws IOException, RDFParseException
	{
		String uri = readString();
		return createURI(uri);
	}

	private BNode readBNode()
		throws IOException, RDFParseException
	{
		String bnodeID = readString();
		return createBNode(bnodeID);
	}

	private Literal readPlainLiteral()
		throws IOException, RDFParseException
	{
		String label = readString();
		return createLiteral(label, null, null, -1, -1);
	}

	private Literal readLangLiteral()
		throws IOException, RDFParseException
	{
		String label = readString();
		String language = readString();
		return createLiteral(label, language, null, -1, -1);
	}

	private Literal readDatatypeLiteral()
		throws IOException, RDFParseException
	{
		String label = readString();
		String datatype = readString();
		IRI dtUri = createURI(datatype);
		return createLiteral(label, null, dtUri, -1, -1);
	}

	private String readString()
		throws IOException
	{
		int stringLength = in.readInt();
		int stringBytes = stringLength << 1;
		if (buf == null || buf.length < stringBytes) {
			// Allocate what we need plus some extra space
			buf = new byte[stringBytes << 1];
		}
		in.readFully(buf, 0, stringBytes);
		return new String(buf, 0, stringBytes, "UTF-16BE");
	}
}
