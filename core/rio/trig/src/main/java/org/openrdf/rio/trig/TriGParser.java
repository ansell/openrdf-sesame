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
package org.openrdf.rio.trig;

import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.rio.turtle.TurtleUtil;

/**
 * RDF parser for <a
 * href="http://www.wiwiss.fu-berlin.de/suhl/bizer/TriG/Spec/">TriG</a> files.
 * This parser is not thread-safe, therefore its public methods are
 * synchronized.
 * <p>
 * This implementation is based on the 2005/06/06 version of the TriG
 * specification, but implemented as an extension of the <a
 * href="http://www.dajobe.org/2004/01/turtle/">Turtle</a> specification of
 * 2006/01/02.
 * 
 * @see TurtleParser
 * @author Arjohn Kampman
 */
public class TriGParser extends TurtleParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Resource context;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriGParser that will use a {@link ValueFactoryImpl} to
	 * create RDF model objects.
	 */
	public TriGParser() {
		super();
	}

	/**
	 * Creates a new TriGParser that will use the supplied ValueFactory to create
	 * RDF model objects.
	 * 
	 * @param valueFactory
	 *        A ValueFactory.
	 */
	public TriGParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIG;
	}

	@Override
	protected void parseStatement()
		throws IOException, RDFParseException, RDFHandlerException
	{
		StringBuilder sb = new StringBuilder(8);

		int c;
		// longest valid directive @prefix
		do {
			c = read();
			if (c == -1 || TurtleUtil.isWhitespace(c)) {
				unread(c);
				break;
			}
			sb.append((char)c);
		}
		while (sb.length() < 8);

		String directive = sb.toString();

		if (directive.startsWith("@") || directive.equalsIgnoreCase("prefix")
				|| directive.equalsIgnoreCase("base"))
		{
			parseDirective(directive);
			skipWSC();
			// SPARQL BASE and PREFIX lines do not end in .
			if (directive.startsWith("@")) {
				verifyCharacterOrFail(read(), ".");
			}
		}
		else {
			unread(directive);
			parseGraph();
		}
	}

	protected void parseGraph()
		throws IOException, RDFParseException, RDFHandlerException
	{
		int c = read();
		int c2 = peek();

		if (c == '<' || TurtleUtil.isPrefixStartChar(c) || (c == ':' && c2 != '-') || (c == '_' && c2 == ':')) {
			unread(c);

			Value value = parseValue();

			if (value instanceof Resource) {
				context = (Resource)value;
			}
			else {
				reportFatalError("Illegal graph name: " + value);
			}

			skipWSC();
			c = read();
		}
		else {
			context = null;
		}

		if (c == ':') {
			verifyCharacterOrFail(read(), "-");
			skipWSC();
			c = read();
		}

		verifyCharacterOrFail(c, "{");

		c = skipWSC();

		if (c != '}') {
			parseTriples();

			c = skipWSC();

			while (c == '.') {
				read();

				c = skipWSC();

				if (c == '}') {
					break;
				}

				parseTriples();

				c = skipWSC();
			}

			verifyCharacterOrFail(c, "}");
		}

		read();

		// FIXME: Blank nodes are scoped to the named graph?
		// clearBNodeIDMap();
	}

	@Override
	protected void reportStatement(Resource subj, URI pred, Value obj)
		throws RDFParseException, RDFHandlerException
	{
		Statement st = createStatement(subj, pred, obj, context);
		rdfHandler.handleStatement(st);
	}
}
