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

		if (directive.startsWith("@")) {
			parseDirective(directive);
			skipWSC();
			verifyCharacterOrFail(read(), ".");
		}
		else if ((directive.length() >= 6 && directive.substring(0, 6).equalsIgnoreCase("prefix"))
				|| (directive.length() >= 4 && directive.substring(0, 4).equalsIgnoreCase("base")))
		{
			parseDirective(directive);
			skipWSC();
			// SPARQL BASE and PREFIX lines do not end in .
		}
		else if (directive.length() >= 5 && directive.substring(0, 5).equalsIgnoreCase("GRAPH")) {
			// Do not unread the directive if it was SPARQL GRAPH
			// Just continue with TriG parsing at this point
			skipWSC();

			parseGraph();
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
		Resource contextOrSubject = null;
		boolean foundContextOrSubject = false;
		if (c == '[') {
			skipWSC();
			c2 = read();
			if (c2 == ']') {
				contextOrSubject = createBNode();
				foundContextOrSubject = true;
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
				contextOrSubject = (Resource)value;
				foundContextOrSubject = true;
			}
			else {
				// NOTE: If a user parses Turtle using TriG, then the following
				// could actually be "Illegal subject name", but it should still
				// hold
				reportFatalError("Illegal graph name: " + value);
			}

			skipWSC();
			c = read();
		}
		else {
			contextOrSubject = null;
		}

		// The following syntax for naming graph seems to have only appeared in
		// the 2005/06/06 draft of the specification, as it doesn't appear in the
		// 2007 or later drafts
		// if (c == ':') {
		// verifyCharacterOrFail(read(), "-");
		// skipWSC();
		// c = read();
		// }

		if (c == '{') {
			setContext(contextOrSubject);

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
		}
		else {
			setContext(null);
			
			// Did not turn out to be a graph, so assign it to subject instead and
			// parse from here to triples
			if (foundContextOrSubject) {
				subject = contextOrSubject;
				unread(c);
				parsePredicateObjectList();
			}
			// Or if we didn't recognise anything, just parse as Turtle
			else {
				unread(c);
				parseTriples();
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
		Statement st = createStatement(subj, pred, obj, getContext());
		if(rdfHandler != null) {
			rdfHandler.handleStatement(st);
		}
	}
	
	protected void setContext(Resource context) {
		this.context = context;
	}
	
	protected Resource getContext() {
		return context;
	}
}
