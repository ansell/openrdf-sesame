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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;

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

	/**
	 * The output stream to write the results table to.
	 */
	private final DataOutputStream out;

	private CharsetEncoder charsetEncoder = CHARSET.newEncoder();
	
	private boolean writingStarted = false;

	public BinaryRDFWriter(OutputStream out) {
		this.out = new DataOutputStream(out);
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

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		startRDF();
		try {
			out.writeByte(STATEMENT);
			writeValue(st.getSubject());
			writeValue(st.getPredicate());
			writeValue(st.getObject());
			writeValue(st.getContext());
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

	private void writeValue(Value value)
		throws RDFHandlerException, IOException
	{
		if (value == null) {
			out.writeByte(NULL_VALUE);
		}
		else if (value instanceof URI) {
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
