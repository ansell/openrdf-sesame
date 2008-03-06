/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.binary;

import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.BNODE_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.DATATYPE_LITERAL_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.ERROR_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.FORMAT_VERSION;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.LANG_LITERAL_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.MAGIC_NUMBER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.MALFORMED_QUERY_ERROR;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.NAMESPACE_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.NULL_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.PLAIN_LITERAL_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.QNAME_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.QUERY_EVALUATION_ERROR;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.REPEAT_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.TABLE_END_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.URI_RECORD_MARKER;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import info.aduna.io.IOUtil;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultParserBase;

/**
 * Reader for the binary tuple result format. The format is explained in
 * {@link BinaryQueryResultConstants}.
 */
public class BinaryQueryResultParser extends TupleQueryResultParserBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private DataInputStream in;

	private int formatVersion;

	private CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();

	private String[] namespaceArray = new String[32];

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser for the binary query result format that will use an
	 * instance of {@link ValueFactoryImpl} to create Value objects.
	 */
	public BinaryQueryResultParser() {
		super();
	}

	/**
	 * Creates a new parser for the binary query result format that will use the
	 * supplied ValueFactory to create Value objects.
	 */
	public BinaryQueryResultParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.BINARY;
	}

	public synchronized void parse(InputStream in)
		throws IOException, TupleQueryResultParseException, TupleQueryResultHandlerException
	{
		if (in == null) {
			throw new IllegalArgumentException("Input stream can not be 'null'");
		}
		if (handler == null) {
			throw new IllegalArgumentException("listener can not be 'null'");
		}

		this.in = new DataInputStream(in);

		// Check magic number
		byte[] magicNumber = IOUtil.readBytes(in, MAGIC_NUMBER.length);
		if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
			throw new TupleQueryResultParseException("File does not contain a binary RDF table result");
		}

		// Check format version (parser is backward-compatible with version 1 and version 2)
		formatVersion = this.in.readInt();
		if (formatVersion != FORMAT_VERSION && formatVersion != 1 && formatVersion != 2) {
			throw new TupleQueryResultParseException("Incompatible format version: " + formatVersion);
		}
		
		if (formatVersion == 2) {
			// read format version 2 FLAG byte (ordered and distinct flags) and ignore them
			this.in.readByte();
		}
		
		// Read column headers
		int columnCount = this.in.readInt();
		if (columnCount < 1) {
			throw new TupleQueryResultParseException("Illegal column count specified: " + columnCount);
		}

		List<String> columnHeaders = new ArrayList<String>(columnCount);
		for (int i = 0; i < columnCount; i++) {
			columnHeaders.add(readString());
		}
		columnHeaders = Collections.unmodifiableList(columnHeaders);

		handler.startQueryResult(columnHeaders);

		// Read value tuples
		List<Value> currentTuple = new ArrayList<Value>(columnCount);
		List<Value> previousTuple = Collections.nCopies(columnCount, (Value)null);

		int recordTypeMarker = this.in.readByte();

		while (recordTypeMarker != TABLE_END_RECORD_MARKER) {
			if (recordTypeMarker == ERROR_RECORD_MARKER) {
				processError();
			}
			else if (recordTypeMarker == NAMESPACE_RECORD_MARKER) {
				processNamespace();
			}
			else {
				Value value = null;
				switch (recordTypeMarker) {
					case NULL_RECORD_MARKER:
						break; // do nothing
					case REPEAT_RECORD_MARKER:
						value = previousTuple.get(currentTuple.size());
						break;
					case QNAME_RECORD_MARKER:
						value = readQName();
						break;
					case URI_RECORD_MARKER:
						value = readURI();
						break;
					case BNODE_RECORD_MARKER:
						value = readBnode();
						break;
					case PLAIN_LITERAL_RECORD_MARKER:
					case LANG_LITERAL_RECORD_MARKER:
					case DATATYPE_LITERAL_RECORD_MARKER:
						value = readLiteral(recordTypeMarker);
						break;
					default:
						throw new IOException("Unkown record type: " + recordTypeMarker);
				}

				currentTuple.add(value);

				if (currentTuple.size() == columnCount) {
					previousTuple = Collections.unmodifiableList(currentTuple);
					currentTuple = new ArrayList<Value>(columnCount);

					handler.handleSolution(new ListBindingSet(columnHeaders, previousTuple));
				}
			}

			recordTypeMarker = this.in.readByte();
		}

		handler.endQueryResult();
	}

	private void processError()
		throws IOException, TupleQueryResultParseException
	{
		byte errTypeFlag = in.readByte();

		QueryErrorType errType = null;
		if (errTypeFlag == MALFORMED_QUERY_ERROR) {
			errType = QueryErrorType.MALFORMED_QUERY_ERROR;
		}
		else if (errTypeFlag == QUERY_EVALUATION_ERROR) {
			errType = QueryErrorType.QUERY_EVALUATION_ERROR;
		}
		else {
			throw new TupleQueryResultParseException("Unkown error type: " + errTypeFlag);
		}

		String msg = readString();

		// FIXME: is this the right thing to do upon encountering an error?
		throw new TupleQueryResultParseException(errType + ": " + msg);
	}

	private void processNamespace()
		throws IOException
	{
		int namespaceID = in.readInt();
		String namespace = readString();

		if (namespaceID >= namespaceArray.length) {
			int newSize = Math.max(namespaceID, namespaceArray.length * 2);
			String[] newArray = new String[newSize];
			System.arraycopy(namespaceArray, 0, newArray, 0, namespaceArray.length);
			namespaceArray = newArray;
		}

		namespaceArray[namespaceID] = namespace;
	}

	private URI readQName()
		throws IOException
	{
		int nsID = in.readInt();
		String localName = readString();

		return valueFactory.createURI(namespaceArray[nsID], localName);
	}

	private URI readURI()
		throws IOException
	{
		String uri = readString();

		return valueFactory.createURI(uri);
	}

	private BNode readBnode()
		throws IOException
	{
		String bnodeID = readString();
		return valueFactory.createBNode(bnodeID);
	}

	private Literal readLiteral(int recordTypeMarker)
		throws IOException, TupleQueryResultParseException
	{
		String label = readString();

		if (recordTypeMarker == DATATYPE_LITERAL_RECORD_MARKER) {
			URI datatype = null;

			int dtTypeMarker = in.readByte();
			switch (dtTypeMarker) {
				case QNAME_RECORD_MARKER:
					datatype = readQName();
					break;
				case URI_RECORD_MARKER:
					datatype = readURI();
					break;
				default:
					throw new TupleQueryResultParseException("Illegal record type marker for literal's datatype");
			}

			return valueFactory.createLiteral(label, datatype);
		}
		else if (recordTypeMarker == LANG_LITERAL_RECORD_MARKER) {
			String language = readString();
			return valueFactory.createLiteral(label, language);
		}
		else {
			return valueFactory.createLiteral(label);
		}
	}

	private String readString()
		throws IOException
	{
		if (formatVersion == 1) {
			return readStringV1();
		}
		else {
			return readStringV2();
		}
	}

	/**
	 * Reads a string from the version 1 format, i.e. in Java's
	 * {@link DataInput#modified-utf-8 Modified UTF-8}.
	 */
	private String readStringV1()
		throws IOException
	{
		return in.readUTF();
	}

	/**
	 * Reads a string from the version 2 format. Strings are encoded as UTF-8 and
	 * are preceeded by a 32-bit integer (high byte first) specifying the length
	 * of the encoded string.
	 */
	private String readStringV2()
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
