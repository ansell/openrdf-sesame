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
package org.eclipse.rdf4j.rio.trig;

import java.io.IOException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.eclipse.rdf4j.rio.turtle.TurtleUtil;

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
	 * Creates a new TriGParser that will use a {@link SimpleValueFactory} to
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
			c = readCodePoint();
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
			verifyCharacterOrFail(readCodePoint(), ".");
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
		int c = readCodePoint();
		int c2 = peekCodePoint();
		Resource contextOrSubject = null;
		boolean foundContextOrSubject = false;
		if (c == '[') {
			skipWSC();
			c2 = readCodePoint();
			if (c2 == ']') {
				contextOrSubject = createBNode();
				foundContextOrSubject = true;
				skipWSC();
			}
			else {
				unread(c2);
				unread(c);
			}
			c = readCodePoint();
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
			c = readCodePoint();
		}
		else {
			setContext(null);
		}

		if (c == '{') {
			setContext(contextOrSubject);

			c = skipWSC();

			if (c != '}') {
				parseTriples();

				c = skipWSC();

				while (c == '.') {
					readCodePoint();

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

		readCodePoint();
	}

	@Override
	protected void parseTriples()
		throws IOException, RDFParseException, RDFHandlerException
	{
		int c = peekCodePoint();

		// If the first character is an open bracket we need to decide which of
		// the two parsing methods for blank nodes to use
		if (c == '[') {
			c = readCodePoint();
			skipWSC();
			c = peekCodePoint();
			if (c == ']') {
				c = readCodePoint();
				subject = createBNode();
				skipWSC();
				parsePredicateObjectList();
			}
			else {
				unread('[');
				subject = parseImplicitBlank();
			}
			skipWSC();
			c = peekCodePoint();

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
	protected void reportStatement(Resource subj, IRI pred, Value obj)
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
