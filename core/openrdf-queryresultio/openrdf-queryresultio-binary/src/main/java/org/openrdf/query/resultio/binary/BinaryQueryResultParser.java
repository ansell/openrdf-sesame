/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.binary;

import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.BNODE_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.DATATYPE_LITERAL_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.DISTINCT_FLAG;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.ERROR_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.FORMAT_VERSION;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.LANG_LITERAL_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.MAGIC_NUMBER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.MALFORMED_QUERY_ERROR;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.NAMESPACE_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.NULL_RECORD_MARKER;
import static org.openrdf.query.resultio.binary.BinaryQueryResultConstants.ORDERED_FLAG;
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

	private DataInputStream _in;

	private int _formatVersion;

	private CharsetDecoder _charsetDecoder = Charset.forName("UTF-8").newDecoder();

	private String[] _namespaceArray = new String[32];

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
		if (_handler == null) {
			throw new IllegalArgumentException("listener can not be 'null'");
		}

		_in = new DataInputStream(in);

		// Check magic number
		byte[] magicNumber = IOUtil.readBytes(_in, MAGIC_NUMBER.length);
		if (!Arrays.equals(magicNumber, MAGIC_NUMBER)) {
			throw new TupleQueryResultParseException("File does not contain a binary RDF table result");
		}

		// Check format version (parser is backward-compatible with version 1)
		_formatVersion = _in.readInt();
		if (_formatVersion != FORMAT_VERSION && _formatVersion != 1) {
			throw new TupleQueryResultParseException("Incompatible format version: " + _formatVersion);
		}

		boolean distinct = false, ordered = false;
		if (_formatVersion == FORMAT_VERSION) {
			// Read 'distinct' and 'ordered' flags
			byte flags = _in.readByte();
			distinct = (flags & DISTINCT_FLAG) != 0;
			ordered = (flags & ORDERED_FLAG) != 0;
		}

		// Read column headers
		int columnCount = _in.readInt();
		if (columnCount < 1) {
			throw new TupleQueryResultParseException("Illegal column count specified: " + columnCount);
		}

		List<String> columnHeaders = new ArrayList<String>(columnCount);
		for (int i = 0; i < columnCount; i++) {
			columnHeaders.add(_readString());
		}
		columnHeaders = Collections.unmodifiableList(columnHeaders);

		_handler.startQueryResult(columnHeaders, distinct, ordered);

		// Read value tuples
		List<Value> currentTuple = new ArrayList<Value>(columnCount);
		List<Value> previousTuple = Collections.nCopies(columnCount, (Value)null);

		int recordTypeMarker = _in.readByte();

		while (recordTypeMarker != TABLE_END_RECORD_MARKER) {
			if (recordTypeMarker == ERROR_RECORD_MARKER) {
				_processError();
			}
			else if (recordTypeMarker == NAMESPACE_RECORD_MARKER) {
				_processNamespace();
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
						value = _readQName();
						break;
					case URI_RECORD_MARKER:
						value = _readURI();
						break;
					case BNODE_RECORD_MARKER:
						value = _readBnode();
						break;
					case PLAIN_LITERAL_RECORD_MARKER:
					case LANG_LITERAL_RECORD_MARKER:
					case DATATYPE_LITERAL_RECORD_MARKER:
						value = _readLiteral(recordTypeMarker);
						break;
					default:
						throw new IOException("Unkown record type: " + recordTypeMarker);
				}

				currentTuple.add(value);

				if (currentTuple.size() == columnCount) {
					previousTuple = Collections.unmodifiableList(currentTuple);
					currentTuple = new ArrayList<Value>(columnCount);

					_handler.handleSolution(new ListBindingSet(columnHeaders, previousTuple));
				}
			}

			recordTypeMarker = _in.readByte();
		}

		_handler.endQueryResult();
	}

	private void _processError()
		throws IOException, TupleQueryResultParseException
	{
		byte errTypeFlag = _in.readByte();

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

		String msg = _readString();

		// FIXME: is this the right thing to do upon encountering an error?
		throw new TupleQueryResultParseException(errType + ": " + msg);
	}

	private void _processNamespace()
		throws IOException
	{
		int namespaceID = _in.readInt();
		String namespace = _readString();

		if (namespaceID >= _namespaceArray.length) {
			int newSize = Math.max(namespaceID, _namespaceArray.length * 2);
			String[] newArray = new String[newSize];
			System.arraycopy(_namespaceArray, 0, newArray, 0, _namespaceArray.length);
			_namespaceArray = newArray;
		}

		_namespaceArray[namespaceID] = namespace;
	}

	private URI _readQName()
		throws IOException
	{
		int nsID = _in.readInt();
		String localName = _readString();

		return _valueFactory.createURI(_namespaceArray[nsID], localName);
	}

	private URI _readURI()
		throws IOException
	{
		String uri = _readString();

		return _valueFactory.createURI(uri);
	}

	private BNode _readBnode()
		throws IOException
	{
		String bnodeID = _readString();
		return _valueFactory.createBNode(bnodeID);
	}

	private Literal _readLiteral(int recordTypeMarker)
		throws IOException, TupleQueryResultParseException
	{
		String label = _readString();

		if (recordTypeMarker == DATATYPE_LITERAL_RECORD_MARKER) {
			URI datatype = null;

			int dtTypeMarker = _in.readByte();
			switch (dtTypeMarker) {
				case QNAME_RECORD_MARKER:
					datatype = _readQName();
					break;
				case URI_RECORD_MARKER:
					datatype = _readURI();
					break;
				default:
					throw new TupleQueryResultParseException("Illegal record type marker for literal's datatype");
			}

			return _valueFactory.createLiteral(label, datatype);
		}
		else if (recordTypeMarker == LANG_LITERAL_RECORD_MARKER) {
			String language = _readString();
			return _valueFactory.createLiteral(label, language);
		}
		else {
			return _valueFactory.createLiteral(label);
		}
	}

	private String _readString()
		throws IOException
	{
		if (_formatVersion == 1) {
			return _readStringV1();
		}
		else {
			return _readStringV2();
		}
	}

	/**
	 * Reads a string from the version 1 format, i.e. in Java's
	 * {@link DataInput#modified-utf-8 Modified UTF-8}.
	 */
	private String _readStringV1()
		throws IOException
	{
		return _in.readUTF();
	}

	/**
	 * Reads a string from the version 2 format. Strings are encoded as UTF-8 and
	 * are preceeded by a 32-bit integer (high byte first) specifying the length
	 * of the encoded string.
	 */
	private String _readStringV2()
		throws IOException
	{
		int stringLength = _in.readInt();
		byte[] encodedString = IOUtil.readBytes(_in, stringLength);

		if (encodedString.length != stringLength) {
			throw new EOFException("Attempted to read " + stringLength + " bytes but no more than "
					+ encodedString.length + " were available");
		}

		ByteBuffer byteBuf = ByteBuffer.wrap(encodedString);
		CharBuffer charBuf = _charsetDecoder.decode(byteBuf);

		return charBuf.toString();
	}
}
