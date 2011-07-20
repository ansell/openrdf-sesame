/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.binary;

import static org.openrdf.rio.binary.BinaryRDFConstants.BNODE_VALUE;
import static org.openrdf.rio.binary.BinaryRDFConstants.CHARSET;
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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
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

	private final CharsetDecoder charsetDecoder = CHARSET.newDecoder();

	private DataInputStream in;

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

		rdfHandler.startRDF();

		this.in = new DataInputStream(in);

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

		rdfHandler.startRDF();

		loop: while (true) {
			int recordType = this.in.readByte();

			switch (recordType) {
				case END_OF_DATA:
					break loop;
				case STATEMENT:
					parseStatement();
					break;
				case NAMESPACE_DECL:
					parseNamespaceDecl();
					break;
				case COMMENT:
					parseComment();
					break;
				default:
					reportFatalError("Invalid record type: " + recordType);
			}
		}

		rdfHandler.endRDF();
	}

	private void parseNamespaceDecl()
		throws IOException, RDFHandlerException
	{
		String prefix = readString();
		String namespace = readString();
		rdfHandler.handleNamespace(prefix, namespace);
	}

	private void parseComment()
		throws IOException, RDFHandlerException
	{
		String comment = readString();
		rdfHandler.handleComment(comment);
	}

	private void parseStatement()
		throws RDFParseException, IOException, RDFHandlerException
	{
		Value v = parseValue();
		Resource subj = null;
		if (v instanceof Resource) {
			subj = (Resource)v;
		}
		else {
			reportFatalError("Invalid subject type: " + v);
		}

		v = parseValue();
		URI pred = null;
		if (v instanceof URI) {
			pred = (URI)v;
		}
		else {
			reportFatalError("Invalid predicate type: " + v);
		}

		Value obj = parseValue();
		if (obj == null) {
			reportFatalError("Invalid object type: null");
		}

		v = parseValue();
		Resource context = null;
		if (v == null || v instanceof Resource) {
			context = (Resource)v;
		}
		else {
			reportFatalError("Invalid context type: " + v);
		}

		Statement st = createStatement(subj, pred, obj, context);
		rdfHandler.handleStatement(st);
	}

	private Value parseValue()
		throws RDFParseException, IOException
	{
		byte valueType = in.readByte();
		switch (valueType) {
			case NULL_VALUE:
				return null;
			case URI_VALUE:
				return parseURI();
			case BNODE_VALUE:
				return parseBNode();
			case PLAIN_LITERAL_VALUE:
				return parsePlainLiteral();
			case LANG_LITERAL_VALUE:
				return parseLangLiteral();
			case DATATYPE_LITERAL_VALUE:
				return parseDatatypeLiteral();
			default:
				reportFatalError("Unknown value type: " + valueType);
				return null;
		}
	}

	private URI parseURI()
		throws IOException, RDFParseException
	{
		String uri = readString();
		return createURI(uri);
	}

	private BNode parseBNode()
		throws IOException, RDFParseException
	{
		String bnodeID = readString();
		return createBNode(bnodeID);
	}

	private Literal parsePlainLiteral()
		throws IOException, RDFParseException
	{
		String label = readString();
		return createLiteral(label, null, null);
	}

	private Literal parseLangLiteral()
		throws IOException, RDFParseException
	{
		String label = readString();
		String language = readString();
		return createLiteral(label, language, null);
	}

	private Literal parseDatatypeLiteral()
		throws IOException, RDFParseException
	{
		String label = readString();
		String datatype = readString();
		URI dtUri = createURI(datatype);
		return createLiteral(label, null, dtUri);
	}

	private String readString()
		throws IOException
	{
		int stringLength = in.readInt();
		byte[] encodedString = IOUtil.readBytes(in, stringLength);

		if (encodedString.length != stringLength) {
			throw new EOFException("Attempted to read " + stringLength + " bytes but no more than "
					+ encodedString.length + " were available");
		}

		ByteBuffer byteBuf = ByteBuffer.wrap(encodedString);
		CharBuffer charBuf = charsetDecoder.decode(byteBuf);

		return charBuf.toString();
	}
}
