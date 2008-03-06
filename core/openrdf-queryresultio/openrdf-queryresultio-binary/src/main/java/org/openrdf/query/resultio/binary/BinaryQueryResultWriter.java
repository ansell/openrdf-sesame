/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Writer for the binary table result format. The format is explained in
 * {@link BinaryQueryResultConstants}.
 * 
 * @author Arjohn Kampman
 */
public class BinaryQueryResultWriter implements TupleQueryResultWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The output stream to write the results table to.
	 */
	private DataOutputStream _out;

	/**
	 * Map containing the namespace IDs (Integer objects) that have been defined
	 * in the document, stored using the concerning namespace (Strings).
	 */
	private Map<String, Integer> _namespaceTable = new HashMap<String, Integer>(32);

	private int _nextNamespaceID;

	private BindingSet _previousBindings;

	private List<String> _bindingNames;

	/*---------*
	 * Methods *
	 *---------*/

	public void setOutputStream(OutputStream out) {
		_out = new DataOutputStream(out);
	}

	public final TupleQueryResultFormat getQueryResultFormat() {
		return TupleQueryResultFormat.BINARY;
	}

	public void startQueryResult(List<String> bindingNames, boolean distinct, boolean ordered)
		throws TupleQueryResultHandlerException
	{
		// Copy supplied column headers list and make it unmodifiable
		_bindingNames = new ArrayList<String>(bindingNames);
		_bindingNames = Collections.unmodifiableList(_bindingNames);

		try {
			_out.write(MAGIC_NUMBER);
			_out.writeInt(FORMAT_VERSION);

			_out.writeInt(_bindingNames.size());
			for (String bindingName : _bindingNames) {
				_out.writeUTF(bindingName);
			}

			List<Value> nullTuple = Collections.nCopies(_bindingNames.size(), (Value)null);
			_previousBindings = new ListBindingSet(_bindingNames, nullTuple);
			_nextNamespaceID = 0;
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		try {
			_out.writeByte(TABLE_END_RECORD_MARKER);
			_out.flush();
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			for (String bindingName : _bindingNames) {
				Value value = bindingSet.getValue(bindingName);

				if (value == null) {
					_writeNull();
				}
				else if (value.equals(_previousBindings.getValue(bindingName))) {
					_writeRepeat();
				}
				else if (value instanceof URI) {
					_writeQName((URI)value);
				}
				else if (value instanceof BNode) {
					_writeBNode((BNode)value);
				}
				else if (value instanceof Literal) {
					_writeLiteral((Literal)value);
				}
				else {
					throw new TupleQueryResultHandlerException("Unknown Value object type: " + value.getClass());
				}
			}

			_previousBindings = bindingSet;
		}
		catch (IOException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private void _writeNull()
		throws IOException
	{
		_out.writeByte(NULL_RECORD_MARKER);
	}

	private void _writeRepeat()
		throws IOException
	{
		_out.writeByte(REPEAT_RECORD_MARKER);
	}

	private void _writeQName(URI uri)
		throws IOException
	{
		// Check if the URI has a new namespace
		String namespace = uri.getNamespace();

		Integer nsID = _namespaceTable.get(namespace);

		if (nsID == null) {
			// Generate a ID for this new namespace
			nsID = _writeNamespace(namespace);
		}

		_out.writeByte(QNAME_RECORD_MARKER);
		_out.writeInt(nsID.intValue());
		_out.writeUTF(uri.getLocalName());
	}

	private void _writeBNode(BNode bnode)
		throws IOException
	{
		_out.writeByte(BNODE_RECORD_MARKER);
		_out.writeUTF(bnode.getID());
	}

	private void _writeLiteral(Literal literal)
		throws IOException
	{
		String label = literal.getLabel();
		String language = literal.getLanguage();
		URI datatype = literal.getDatatype();

		int marker = PLAIN_LITERAL_RECORD_MARKER;

		if (datatype != null) {
			String namespace = datatype.getNamespace();

			if (!_namespaceTable.containsKey(namespace)) {
				// Assign an ID to this new namespace
				_writeNamespace(namespace);
			}

			marker = DATATYPE_LITERAL_RECORD_MARKER;
		}
		else if (language != null) {
			marker = LANG_LITERAL_RECORD_MARKER;
		}

		_out.writeByte(marker);
		_out.writeUTF(label);

		if (datatype != null) {
			_writeQName(datatype);
		}
		else if (language != null) {
			_out.writeUTF(language);
		}
	}

	/**
	 * Writes an error msg to the stream.
	 * 
	 * @param errType
	 *        The error type.
	 * @param msg
	 *        The error message.
	 * @throws IOException
	 *         When the error could not be written to the stream.
	 */
	public void error(QueryErrorType errType, String msg)
		throws IOException
	{
		_out.writeByte(ERROR_RECORD_MARKER);

		if (errType == QueryErrorType.MALFORMED_QUERY_ERROR) {
			_out.writeByte(MALFORMED_QUERY_ERROR);
		}
		else {
			_out.writeByte(QUERY_EVALUATION_ERROR);
		}

		_out.writeUTF(msg);
	}

	private Integer _writeNamespace(String namespace)
		throws IOException
	{
		_out.writeByte(NAMESPACE_RECORD_MARKER);
		_out.writeInt(_nextNamespaceID);
		_out.writeUTF(namespace);

		Integer result = new Integer(_nextNamespaceID);
		_namespaceTable.put(namespace, result);

		_nextNamespaceID++;

		return result;
	}
}
