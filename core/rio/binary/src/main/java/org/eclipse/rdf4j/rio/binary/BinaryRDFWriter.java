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

import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.BNODE_VALUE;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.COMMENT;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.DATATYPE_LITERAL_VALUE;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.END_OF_DATA;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.FORMAT_VERSION;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.LANG_LITERAL_VALUE;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.MAGIC_NUMBER;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.NAMESPACE_DECL;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.NULL_VALUE;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.PLAIN_LITERAL_VALUE;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.STATEMENT;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.URI_VALUE;
import static org.eclipse.rdf4j.rio.binary.BinaryRDFConstants.VALUE_REF;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFWriter;

/**
 * @author Arjohn Kampman
 */
public class BinaryRDFWriter extends AbstractRDFWriter implements RDFWriter {

	private final BlockingQueue<Statement> statementQueue;

	private final Map<Value, AtomicInteger> valueFreq;

	private final Map<Value, Integer> valueIdentifiers;

	private final AtomicInteger maxValueId = new AtomicInteger(-1);

	private final DataOutputStream out;

	private boolean writingStarted = false;
	
	private byte[] buf;

	public BinaryRDFWriter(OutputStream out) {
		this(out, 100);
	}

	public BinaryRDFWriter(OutputStream out, int bufferSize) {
		this.out = new DataOutputStream(new BufferedOutputStream(out));
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
		/* FIXME: This loop is very slow for large datasets
		for (Value key : valueIdentifiers.keySet()) {
			if (!valueFreq.containsKey(key)) {
				id = valueIdentifiers.remove(key);
				break;
			}
		}
		*/
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
		if (value instanceof IRI) {
			writeURI((IRI)value);
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

	private void writeURI(IRI uri)
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
		IRI datatype = literal.getDatatype();

		if (Literals.isLanguageLiteral(literal)) {
			out.writeByte(LANG_LITERAL_VALUE);
			writeString(label);
			writeString(literal.getLanguage().get());
		}
		else {
			out.writeByte(DATATYPE_LITERAL_VALUE);
			writeString(label);
			writeString(datatype.toString());
		}
	}

	private void writeString(String s)
		throws IOException
	{
		int strLen = s.length();
		out.writeInt(strLen);
		int stringBytes = strLen << 1;
		if(buf == null || buf.length < stringBytes) {
			buf = new byte[stringBytes << 1];
		}
		int pos = 0;
		for(int i = 0; i < strLen; i++) {
			char v = s.charAt(i);
			buf[pos++] = (byte) ((v >>> 8) & 0xFF);
			buf[pos++] = (byte) ((v >>> 0) & 0xFF);
		}
		out.write(buf, 0, stringBytes);
	}
}
