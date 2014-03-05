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

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.trig.TriGParser;
import org.openrdf.rio.turtle.TurtleUtil;

/**
 * An extension of {@link TriGParser} that processes data in the format
 * specified in the SPARQL 1.1 grammar for Quad data (assuming no variables, as
 * is the case for INSERT DATA and DELETE DATA operations). This format is
 * almost completely compatible with TriG, except for the fact that it
 * introduces the 'GRAPH' keyword in front of each named graph identifier.
 * 
 * @author Jeen Broekstra
 * @see <a href="http://www.w3.org/TR/sparql11-query/#rInsertData">SPARQL 1.1
 *      Grammar production for INSERT DATA</a>
 * @see <a href="http://www.w3.org/TR/sparql11-query/#rDeleteData">SPARQL 1.1
 *      Grammar production for DELETE DATA</a>
 */
public class SPARQLUpdateDataBlockParser extends TriGParser {

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

	protected void parseGraph()
		throws IOException, RDFParseException, RDFHandlerException
	{
		int c = read();

		// for now we just read away the GRAPH keyword if present, we don't
		// explicitly verify it's there.
		if (c == 'g' || c == 'G') {
			// graph keyword?
			StringBuilder sb = new StringBuilder(5);
			sb.append((char)c);
			do {
				c = read();
				if (c == -1 || TurtleUtil.isWhitespace(c)) {
					unread(c);
					break;
				}
				
				sb.append((char)c);
			}
			while (sb.length() < 5);

			if (!sb.toString().equalsIgnoreCase("graph")) {
				unread(sb.toString());
			}
			
			skipWSC();
			c = read();
			final int c2 = peek();

			if (c == '<' || TurtleUtil.isPrefixStartChar(c) || (c == ':' && c2 != '-') || (c == '_' && c2 == ':')) {
				unread(c);

				Value value = parseValue();

				if (value instanceof Resource) {
					setContext((Resource)value);
				}
				else {
					reportFatalError("Illegal graph name: " + value);
				}
			}
			else {
				setContext(null);
			}
		}
		else {
			unread(c);
		}

		c = skipWSC();
		
		if (c == '{') {
			read();
			c = skipWSC();
		}
		
		if (c != '}') {
			parseTriples();

			c = skipWSC();

			while (c == '.') {
				read();

				c = skipWSC();

				if (c == '}' || c == -1) {
					break;
				}

				parseTriples();

				c = skipWSC();
			}

		}

		read();
	}

}
