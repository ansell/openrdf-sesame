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
import static org.openrdf.rio.binary.BinaryRDFConstants.VALUE_REF;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * @author Arjohn Kampman
 */
public class BinaryRDFWriter implements RDFWriter {

	private final CharsetEncoder charsetEncoder = CHARSET.newEncoder();

	private final BlockingQueue<Statement> statementQueue;

	private final Map<Value, AtomicInteger> valueFreq;

	private final Map<Value, Integer> valueIdentifiers;

	private final AtomicInteger maxValueId = new AtomicInteger(-1);

	private final DataOutputStream out;

	private boolean writingStarted = false;

	public BinaryRDFWriter(OutputStream out) {
		this(out, 100);
	}

	public BinaryRDFWriter(OutputStream out, int bufferSize) {
		this.out = new DataOutputStream(out);
		this.statementQueue = new ArrayBlockingQueue<Statement>(bufferSize);
		this.valueFreq = new HashMap<Value, AtomicInteger>(3 * bufferSize);
		this.valueIdentifiers = new LinkedHashMap<Value, Integer>(bufferSize);
	}

	public RDFFormat getRDFFormat() {
		return RDFFormat.BINARY;
	}

	public void startRDF()
		throws RDFHandlerException
	{
		if (!writingStarted) {
			writingStarted = true;
			try {
				out.write(MAGIC_NUMBER);
				out.writeInt(FORMAT_VERSION);
			}
			catch (IOException e) {
				throw new RDFHandlerException(e);
			}
		}
	}

	public void endRDF()
		throws RDFHandlerException
	{
		startRDF();
		try {
			while (!statementQueue.isEmpty()) {
				writeStatement();
			}
			out.writeByte(END_OF_DATA);
			out.flush();
			writingStarted = false;
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleNamespace(String prefix, String uri)
		throws RDFHandlerException
	{
		startRDF();
		try {
			out.writeByte(NAMESPACE_DECL);
			writeString(prefix);
			writeString(uri);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		startRDF();
		try {
			out.writeByte(COMMENT);
			writeString(comment);
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		statementQueue.add(st);
		incValueFreq(st.getSubject());
		incValueFreq(st.getPredicate());
		incValueFreq(st.getObject());
		incValueFreq(st.getContext());

		if (statementQueue.remainingCapacity() > 0) {
			// postpone statement writing until queue is filled
			return;
		}

		// Process the first statement from the queue
		startRDF();
		try {
			writeStatement();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	/** Writes the first statement from the statement queue */
	private void writeStatement()
		throws RDFHandlerException, IOException
	{
		Statement st = statementQueue.remove();
		int subjId = getValueId(st.getSubject());
		int predId = getValueId(st.getPredicate());
		int objId = getValueId(st.getObject());
		int contextId = getValueId(st.getContext());

		decValueFreq(st.getSubject());
		decValueFreq(st.getPredicate());
		decValueFreq(st.getObject());
		decValueFreq(st.getContext());

		out.writeByte(STATEMENT);
		writeValueOrId(st.getSubject(), subjId);
		writeValueOrId(st.getPredicate(), predId);
		writeValueOrId(st.getObject(), objId);
		writeValueOrId(st.getContext(), contextId);
	}

	private void incValueFreq(Value v) {
		if (v != null) {
			AtomicInteger freq = valueFreq.get(v);
			if (freq != null) {
				freq.incrementAndGet();
			}
			else {
				valueFreq.put(v, new AtomicInteger(1));
			}
		}
	}

	private void decValueFreq(Value v) {
		if (v != null) {
			AtomicInteger freq = valueFreq.get(v);
			if (freq != null) {
				int newFreq = freq.decrementAndGet();
				if (newFreq == 0) {
					valueFreq.remove(v);
				}
			}
		}
	}

	private int getValueId(Value v)
		throws IOException, RDFHandlerException
	{
		if (v == null) {
			return -1;
		}
		Integer id = valueIdentifiers.get(v);
		if (id == null) {
			// Assign an id if valueFreq >= 2
			AtomicInteger freq = valueFreq.get(v);
			if (freq != null && freq.get() >= 2) {
				id = assignValueId(v);
			}
		}
		if (id != null) {
			return id.intValue();
		}
		return -1;
	}

	private Integer assignValueId(Value v)
		throws IOException, RDFHandlerException
	{
		// Check if a previous value can be overwritten
		Integer id = null;
		for (Value key : valueIdentifiers.keySet()) {
			if (!valueFreq.containsKey(key)) {
				id = valueIdentifiers.remove(key);
				break;
			}
		}
		if (id == null) {
			// no previous value could be overwritten
			id = maxValueId.incrementAndGet();
		}
		out.writeByte(BinaryRDFConstants.VALUE_DECL);
		out.writeInt(id);
		writeValue(v);
		valueIdentifiers.put(v, id);
		return id;
	}

	private void writeValueOrId(Value value, int id)
		throws RDFHandlerException, IOException
	{
		if (value == null) {
			out.writeByte(NULL_VALUE);
		}
		else if (id >= 0) {
			out.writeByte(VALUE_REF);
			out.writeInt(id);
		}
		else {
			writeValue(value);
		}
	}

	private void writeValue(Value value)
		throws RDFHandlerException, IOException
	{
		if (value instanceof URI) {
			writeURI((URI)value);
		}
		else if (value instanceof BNode) {
			writeBNode((BNode)value);
		}
		else if (value instanceof Literal) {
			writeLiteral((Literal)value);
		}
		else {
			throw new RDFHandlerException("Unknown Value object type: " + value.getClass());
		}
	}

	private void writeURI(URI uri)
		throws IOException
	{
		out.writeByte(URI_VALUE);
		writeString(uri.toString());
	}

	private void writeBNode(BNode bnode)
		throws IOException
	{
		out.writeByte(BNODE_VALUE);
		writeString(bnode.getID());
	}

	private void writeLiteral(Literal literal)
		throws IOException
	{
		String label = literal.getLabel();
		String language = literal.getLanguage();
		URI datatype = literal.getDatatype();

		if (datatype != null) {
			out.writeByte(DATATYPE_LITERAL_VALUE);
			writeString(label);
			writeString(datatype.toString());
		}
		else if (language != null) {
			out.writeByte(LANG_LITERAL_VALUE);
			writeString(label);
			writeString(language);
		}
		else {
			out.writeByte(PLAIN_LITERAL_VALUE);
			writeString(label);
		}
	}

	private void writeString(String s)
		throws IOException
	{
		ByteBuffer byteBuf = charsetEncoder.encode(CharBuffer.wrap(s));
		out.writeInt(byteBuf.remaining());
		out.write(byteBuf.array(), 0, byteBuf.remaining());
	}
}
