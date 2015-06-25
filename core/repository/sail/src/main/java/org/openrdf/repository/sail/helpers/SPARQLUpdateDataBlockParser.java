/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.sail.helpers;

import java.io.IOException;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
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
	 * Creates a new parser that will use a {@link ValueFactoryImpl} to create
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
