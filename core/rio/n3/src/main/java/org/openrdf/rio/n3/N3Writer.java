/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
 * format. Note: the current implementation simply wraps a {@link TurtleWriter}
 * and writes documents in Turtle format, which is a subset of N3.
 */
public class N3Writer implements RDFWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private TurtleWriter ttlWriter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new N3Writer that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the N3 document to.
	 */
	public N3Writer(OutputStream out) {
		ttlWriter = new TurtleWriter(out);
	}

	/**
	 * Creates a new N3Writer that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the N3 document to.
	 */
	public N3Writer(Writer writer) {
		ttlWriter = new TurtleWriter(writer);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public RDFFormat getRDFFormat() {
		return RDFFormat.N3;
	}

	public void setBaseURI(String baseURI) {
		ttlWriter.setBaseURI(baseURI);
	}

	public void startRDF()
		throws RDFHandlerException
	{
		ttlWriter.startRDF();
	}

	public void endRDF()
		throws RDFHandlerException
	{
		ttlWriter.endRDF();
	}

	public void handleNamespace(String prefix, String name)
		throws RDFHandlerException
	{
		ttlWriter.handleNamespace(prefix, name);
	}

	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		ttlWriter.handleStatement(st);
	}

	public void handleComment(String comment)
		throws RDFHandlerException
	{
		ttlWriter.handleComment(comment);
	}
}
