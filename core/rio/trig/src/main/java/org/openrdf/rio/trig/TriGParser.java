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
		else if (directive.equalsIgnoreCase("GRAPH")) {
			// Do not unread the directive if it was SPARQL GRAPH
			// Just continue with TriG parsing at this point
			skipWSC();

			parseGraph(true);
		}
		// If it looks like a Turtle document, then don't try to verify that there
		// is a graph around it
		else if (TurtleUtil.isNameStartChar(directive.charAt(0))
				|| TurtleUtil.isPrefixStartChar(directive.charAt(0)))
		{
			unread(directive);
			parseGraph(false);
		}
		else {
			unread(directive);
			parseGraph(true);
		}
	}

	protected void parseGraph(boolean isGraph)
		throws IOException, RDFParseException, RDFHandlerException
	{
		int c = read();
		if (isGraph) {
			int c2 = peek();

			if (c == '[') {
				skipWSC();
				c2 = read();
				if (c2 == ']') {
					context = createBNode();
					skipWSC();
				}
				else {
					unread(c2);
					unread(c);
				}
				c = read();
			}
			else if (c == '<' || TurtleUtil.isPrefixStartChar(c) || (c == ':' && c2 != '-')
					|| (c == '_' && c2 == ':'))
			{
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
				unread(c);
				context = null;
			}
		}
		else {
			unread(c);
			context = null;
		}

		c = skipWSC();

		if (isGraph) {
			if (c == ':') {
				verifyCharacterOrFail(c, "-");
				skipWSC();
			}
			else {
				verifyCharacterOrFail(c, "{");
			}
			c = read();
		}

		if (c != '}') {
			parseTriples();

			c = skipWSC();

			while (c == '.') {
				read();

				c = skipWSC();

				if (isGraph && c == '}') {
					break;
				}
				else if (!isGraph && (c == -1)) {
					break;
				}

				parseTriples();

				c = skipWSC();
			}

			if (isGraph) {
				verifyCharacterOrFail(c, "}");
			}
		}

		read();
	}

	@Override
	protected void parseTriples()
		throws IOException, RDFParseException, RDFHandlerException
	{
		int c = peek();

		// If the first character is an open bracket we need to decide which of
		// the two parsing methods for blank nodes to use
		if (c == '[') {
			c = read();
			skipWSC();
			c = peek();
			if (c == ']') {
				c = read();
				subject = createBNode();
				skipWSC();
				parsePredicateObjectList();
			}
			else {
				unread('[');
				subject = parseImplicitBlank();
			}
			skipWSC();
			c = peek();

			// if this is not the end of the statement, recurse into the list of
			// predicate and objects, using the subject parsed above as the subject
			// of the statement.
			if (c != '.' && c != '}') {
				parsePredicateObjectList();
			}
		}
		else {
			parseSubject();
			skipWSC();
			parsePredicateObjectList();
		}

		subject = null;
		predicate = null;
		object = null;
	}

	@Override
	protected void reportStatement(Resource subj, URI pred, Value obj)
		throws RDFParseException, RDFHandlerException
	{
		Statement st = createStatement(subj, pred, obj, context);
		if (rdfHandler != null) {
			rdfHandler.handleStatement(st);
		}
	}
}
