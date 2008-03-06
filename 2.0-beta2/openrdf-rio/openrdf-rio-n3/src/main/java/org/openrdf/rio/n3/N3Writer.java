/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.n3;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.Statement;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * An implementation of the RDFWriter interface that writes RDF documents in N3
 * format.  Note: the current implementation simply wraps a {@link TurtleWriter}
 * and writes documents in Turtle format, which is a subset of N3.
 */
public class N3Writer implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private TurtleWriter _ttlWriter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new N3Writer object. Note that, before this writer can be used, an
	 * OutputStream or Writer needs to be supplied to it using
	 * {@link #setOutputStream(OutputStream)} or {@link #setWriter(Writer)}.
	 */
	public N3Writer() {
		_ttlWriter = new TurtleWriter();
	}
	
	/**
	 * Creates a new N3Writer that will write to the supplied OutputStream.
	 * 
	 * @param out The OutputStream to write the N3 document to.
	 */
	public N3Writer(OutputStream out) {
		this();
		setOutputStream(out);
	}

	/**
	 * Creates a new N3Writer that will write to the supplied Writer.
	 * 
	 * @param writer The Writer to write the N3 document to.
	 */
	public N3Writer(Writer writer) {
		this();
		setWriter(writer);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFWriter.getRDFFormat()
	public RDFFormat getRDFFormat() {
		return RDFFormat.N3;
	}
	
	public void setOutputStream(OutputStream out) {
		_ttlWriter.setOutputStream(out);
	}

	public void setWriter(Writer writer) {
		_ttlWriter.setWriter(writer);
	}

	// implements RDFHandler.startRDF()
	public void startRDF()
		throws RDFHandlerException
	{
		_ttlWriter.startRDF();
	}

	// implements RDFHandler.endRDF()
	public void endRDF()
		throws RDFHandlerException
	{
		_ttlWriter.endRDF();
	}

	// implements RDFHandler.handleNamespace(...)
	public void handleNamespace(String prefix, String name)
		throws RDFHandlerException
	{
		_ttlWriter.handleNamespace(prefix, name);
	}

	// implements RDFHandler.handleStatement(...)
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		_ttlWriter.handleStatement(st);
	}

	// implements RDFWriter.handleComment(...)
	public void handleComment(String comment)
		throws RDFHandlerException
	{
		_ttlWriter.handleComment(comment);
	}
}
