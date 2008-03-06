/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.ntriples;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in
 * N-Triples format. The N-Triples format is defined in <a
 * href="http://www.w3.org/TR/rdf-testcases/#ntriples">this section</a> of the
 * RDF Test Cases document.
 */
public class NTriplesWriter implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Writer _writer;

	private boolean _writingStarted;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NTriplesWriter. Note that, before this writer can be used,
	 * an OutputStream or Writer needs to be supplied to it using
	 * {@link #setOutputStream(OutputStream)} or {@link #setWriter(Writer)}.
	 */
	public NTriplesWriter() {
		_writingStarted = false;
	}

	/**
	 * Creates a new NTriplesWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the N-Triples document to.
	 */
	public NTriplesWriter(OutputStream out) {
		this();
		setOutputStream(out);
	}

	/**
	 * Creates a new NTriplesWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the N-Triples document to.
	 */
	public NTriplesWriter(Writer writer) {
		this();
		setWriter(writer);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFWriter.getRDFFormat()
	public RDFFormat getRDFFormat() {
		return RDFFormat.NTRIPLES;
	}

	public void setOutputStream(OutputStream out) {
		try {
			setWriter(new OutputStreamWriter(out, "US-ASCII"));
		}
		catch (UnsupportedEncodingException e) {
			// US-ASCII must be supported by all compliant JVM's
			throw new RuntimeException("US-ASCII character encoding not supported on this platform");
		}
	}

	public void setWriter(Writer writer) {
		_writer = writer;
	}

	// implements RDFHandler.startRDF()
	public void startRDF()
		throws RDFHandlerException
	{
		if (_writingStarted) {
			throw new RuntimeException("Document writing has already started");
		}

		_writingStarted = true;
	}

	// implements RDFHandler.endRDF()
	public void endRDF()
		throws RDFHandlerException
	{
		if (!_writingStarted) {
			throw new RuntimeException("Document writing has not yet started");
		}

		try {
			_writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
		finally {
			_writingStarted = false;
		}
	}

	// implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String name) {
		// N-Triples does not support namespace prefixes.
	}

	// implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!_writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		Resource subj = st.getSubject();
		URI pred = st.getPredicate();
		Value obj = st.getObject();

		try {
			// SUBJECT
			_writeResource(subj);
			_writer.write(" ");

			// PREDICATE
			_writeURI(pred);
			_writer.write(" ");

			// OBJECT
			if (obj instanceof Resource) {
				_writeResource((Resource)obj);
			}
			else if (obj instanceof Literal) {
				_writeLiteral((Literal)obj);
			}

			_writer.write(" .");
			_writeNewLine();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	// implements RDFWriter.handleComment(String)
	public void handleComment(String comment)
		throws RDFHandlerException
	{
		try {
			_writer.write("# ");
			_writer.write(comment);
			_writeNewLine();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	private void _writeResource(Resource res)
		throws IOException
	{
		if (res instanceof BNode) {
			_writeBNode((BNode)res);
		}
		else {
			_writeURI((URI)res);
		}
	}

	private void _writeURI(URI uri)
		throws IOException
	{
		_writer.write(NTriplesUtil.toNTriplesString(uri));
	}

	private void _writeBNode(BNode bNode)
		throws IOException
	{
		_writer.write(NTriplesUtil.toNTriplesString(bNode));
	}

	private void _writeLiteral(Literal lit)
		throws IOException
	{
		_writer.write(NTriplesUtil.toNTriplesString(lit));
	}

	private void _writeNewLine()
		throws IOException
	{
		_writer.write("\n");
	}
}
