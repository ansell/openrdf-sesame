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
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;

/**
 * @author Arjohn Kampman
 */
public class BinaryRDFParser extends RDFParserBase {

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
		URI pred = null;
		if (v instanceof URI) {
			pred = (URI)v;
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

	private URI readURI()
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
		URI dtUri = createURI(datatype);
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
