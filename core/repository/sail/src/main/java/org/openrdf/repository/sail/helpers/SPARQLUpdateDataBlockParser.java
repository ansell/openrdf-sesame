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
package org.openrdf.repository.sail.helpers;

import java.io.IOException;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.trig.TriGParser;

/**
 * An extension of {@link TriGParser} that processes data in the format
 * specified in the SPARQL 1.1 grammar for Quad data (assuming no variables, as
 * is the case for INSERT DATA and DELETE DATA operations). This format is
 * almost completely compatible with TriG, except for three differences:
 * <ul>
 * <li>it introduces the 'GRAPH' keyword in front of each named graph identifier
 * <li>it does not allow the occurrence of blank nodes.
 * <li>it does not require curly braces around the default graph.
 * </ul>
 * 
 * @author Jeen Broekstra
 * @see <a href="http://www.w3.org/TR/sparql11-query/#rInsertData">SPARQL 1.1
 *      Grammar production for INSERT DATA</a>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#rDeleteData">SPARQL 1.1
 *      Grammar production for DELETE DATA</a>
 */
public class SPARQLUpdateDataBlockParser extends TriGParser {

	private boolean allowBlankNodes = true;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser that will use a {@link SimpleValueFactory} to create
	 * RDF model objects.
	 */
	public SPARQLUpdateDataBlockParser() {
		super();
	}

	/**
	 * Creates a new parser that will use the supplied ValueFactory to create RDF
	 * model objects.
	 * 
	 * @param valueFactory
	 *        A ValueFactory.
	 */
	public SPARQLUpdateDataBlockParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public RDFFormat getRDFFormat() {
		// TODO for now, we do not implement this as a fully compatible Rio
		// parser, and we're not introducing a new RDFFormat constant.
		return null;
	}

	@Override
	protected void parseGraph() throws RDFParseException, RDFHandlerException, IOException {
		super.parseGraph();
		skipOptionalPeriod();
	}
	
	@Override
	protected Resource parseImplicitBlank()
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (isAllowBlankNodes()) {
			return super.parseImplicitBlank();
		}
		else {
			throw new RDFParseException("blank nodes not allowed in data block");
		}
	}

	@Override
	protected BNode parseNodeID()
		throws IOException, RDFParseException
	{
		if (isAllowBlankNodes()) {
			return super.parseNodeID();
		}
		else {
			throw new RDFParseException("blank nodes not allowed in data block");
		}
	}

	/**
	 * @return Returns the allowBlankNodes.
	 */
	public boolean isAllowBlankNodes() {
		return allowBlankNodes;
	}

	/**
	 * @param allowBlankNodes
	 *        The allowBlankNodes to set.
	 */
	public void setAllowBlankNodes(boolean allowBlankNodes) {
		this.allowBlankNodes = allowBlankNodes;
	}

	private void skipOptionalPeriod()
		throws RDFHandlerException, IOException
	{
		skipWSC();
		int c = peekCodePoint();
		if (c == '.') {
			readCodePoint();
		}
	}
}
