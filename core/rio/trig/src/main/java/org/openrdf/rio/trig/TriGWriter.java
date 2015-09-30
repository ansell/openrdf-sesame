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
package org.openrdf.rio.trig;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;

/**
 * An extension of {@link TurtleWriter} that writes RDF documents in <a
 * href="http://www.wiwiss.fu-berlin.de/suhl/bizer/TriG/Spec/">TriG</a> format
 * by adding graph scopes to the Turtle document.
 * 
 * @author Arjohn Kampman
 */
public class TriGWriter extends TurtleWriter {

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean inActiveContext;

	private Resource currentContext;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriGWriter that will write to the supplied OutputStream.
	 * 
	 * @param out
	 *        The OutputStream to write the TriG document to.
	 */
	public TriGWriter(OutputStream out) {
		super(out);
	}

	/**
	 * Creates a new TriGWriter that will write to the supplied Writer.
	 * 
	 * @param writer
	 *        The Writer to write the TriG document to.
	 */
	public TriGWriter(Writer writer) {
		super(writer);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public RDFFormat getRDFFormat()
	{
		return RDFFormat.TRIG;
	}

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		super.startRDF();

		inActiveContext = false;
		currentContext = null;
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		super.endRDF();

		try {
			closeActiveContext();
			writer.flush();
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		if (!writingStarted) {
			throw new RuntimeException("Document writing has not yet been started");
		}

		try {
			Resource context = st.getContext();

			if (inActiveContext && !contextsEquals(context, currentContext)) {
				closePreviousStatement();
				closeActiveContext();
			}

			if (!inActiveContext) {
				writer.writeEOL();

				if (context != null) {
					writeResource(context);
					writer.write(" ");
				}

				writer.write("{");
				writer.increaseIndentation();

				currentContext = context;
				inActiveContext = true;
			}
		}
		catch (IOException e) {
			throw new RDFHandlerException(e);
		}

		super.handleStatement(st);
	}

	@Override
	protected void writeCommentLine(String line)
		throws IOException
	{
		closeActiveContext();
		super.writeCommentLine(line);
	}

	@Override
	protected void writeNamespace(String prefix, String name)
		throws IOException
	{
		closeActiveContext();
		super.writeNamespace(prefix, name);
	}

	protected void closeActiveContext()
		throws IOException
	{
		if (inActiveContext) {
			writer.decreaseIndentation();
			writer.write("}");
			writer.writeEOL();

			inActiveContext = false;
			currentContext = null;
		}
	}

	private static final boolean contextsEquals(Resource context1, Resource context2) {
		if (context1 == null) {
			return context2 == null;
		}
		else {
			return context1.equals(context2);
		}
	}
}
