/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.ntriples;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFParserBase;

/**
 * RDF parser for N-Triples files. A specification of NTriples can be found in
 * <a href="http://www.w3.org/TR/rdf-testcases/#ntriples">this section</a> of
 * the RDF Test Cases document. This parser is not thread-safe, therefore
 * its public methods are synchronized.
 */
public class NTriplesParser extends RDFParserBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Reader _reader;
	
	private int _lineNo;

	private Resource _subject;
	private URI _predicate;
	private Value _object;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NTriplesParser that will use a {@link ValueFactoryImpl} to
	 * create object for resources, bNodes and literals.
	 */
	public NTriplesParser() {
		super();
	}

	/**
	 * Creates a new NTriplesParser that will use the supplied
	 * <tt>ValueFactory</tt> to create RDF model objects.
	 *
	 * @param valueFactory A ValueFactory.
	 */
	public NTriplesParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFParser.getRDFFormat()
	public final RDFFormat getRDFFormat() {
		return RDFFormat.NTRIPLES;
	}

	/**
	 * Implementation of the <tt>parse(InputStream, String)</tt> method defined
	 * in the RDFParser interface. 	
	 * 
	 * @param in The InputStream from which to read the data, must not be
	 * <tt>null</tt>. The InputStream is supposed to contain 7-bit US-ASCII
	 * characters, as per the N-Triples specification.
	 * @param baseURI The URI associated with the data in the InputStream, must
	 * not be <tt>null</tt>.
	 * @throws IOException If an I/O error occurred while data was read from the
	 * InputStream.
	 * @throws RDFParseException If the parser has found an unrecoverable parse
	 * error.
	 * @throws RDFHandlerException If the configured statement handler
	 * encountered an unrecoverable error.
	 * @throws IllegalArgumentException If the supplied input stream or base URI
	 * is <tt>null</tt>.
	 */
	public synchronized void parse(InputStream in, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (in == null) {
			throw new IllegalArgumentException("Input stream can not be 'null'");
		}
		// Note: baseURI will be checked in parse(Reader, String)
		
		try {
			parse(new InputStreamReader(in, "US-ASCII"), baseURI);
		}
		catch (UnsupportedEncodingException e) {
			// Every platform should support the US-ASCII encoding...
			throw new RuntimeException(e);
		}
	}

	/**
	 * Implementation of the <tt>parse(Reader, String)</tt> method defined in
	 * the RDFParser interface. 	
	 * 
	 * @param reader The Reader from which to read the data, must not be
	 * <tt>null</tt>.
	 * @param baseURI The URI associated with the data in the Reader, must not
	 * be <tt>null</tt>.
	 * @throws IOException If an I/O error occurred while data was read from the
	 * InputStream.
	 * @throws RDFParseException If the parser has found an unrecoverable parse
	 * error.
	 * @throws RDFHandlerException If the configured statement handler
	 * encountered an unrecoverable error.
	 * @throws IllegalArgumentException If the supplied reader or base URI is
	 * <tt>null</tt>.
	 */
	public synchronized void parse(Reader reader, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (reader == null) {
			throw new IllegalArgumentException("Reader can not be 'null'");
		}
		if (baseURI == null) {
			throw new IllegalArgumentException("base URI can not be 'null'");
		}

		_rdfHandler.startRDF();

		_reader = reader;
		_lineNo = 1;

		_reportLocation(_lineNo, 1);

		try {
			int c = _reader.read();
			c = _skipWhitespace(c);

			while (c != -1) {
				if (c == '#') {
					// Comment, ignore
					c = _skipLine(c);
				}
				else if (c == '\r' || c == '\n') {
					// Empty line, ignore
					c = _skipLine(c);
				}
				else {
					c = _parseTriple(c);
				}

				c = _skipWhitespace(c);
			}
		}
		finally {
			_clear();
		}

		_rdfHandler.endRDF();
	}

	/**
	 * Reads characters from _reader until it finds a character that is not
	 * a space or tab, and returns this last character. In case the end of the
	 * character stream has been reached, -1 is returned.
	 */
	private int _skipWhitespace(int c)
		throws IOException
	{
		while (c == ' ' || c == '\t') {
			c = _reader.read();
		}

		return c;
	}

	/**
	 * Reads characters from _reader until the first EOL has been read. The
	 * first character after the EOL is returned. In case the end of the
	 * character stream has been reached, -1 is returned.
	 */
	private int _skipLine(int c)
		throws IOException
	{
		while (c != -1 && c != '\r' && c != '\n') {
			c = _reader.read();
		}

		// c is equal to -1, \r or \n. In case of a \r, we should
		// check whether it is followed by a \n.

		if (c == '\n') {
			c = _reader.read();

			_lineNo++;

			_reportLocation(_lineNo, 1);
		}
		else if (c == '\r') {
			c = _reader.read();

			if (c == '\n') {
				c = _reader.read();
			}

			_lineNo++;

			_reportLocation(_lineNo, 1);
		}

		return c;
	}

	private int _parseTriple(int c)
		throws IOException, RDFParseException, RDFHandlerException
	{
		c = _parseSubject(c);

		c = _skipWhitespace(c);

		c = _parsePredicate(c);

		c = _skipWhitespace(c);

		c = _parseObject(c);

		c = _skipWhitespace(c);

		if (c == -1) {
			_throwEOFException();
		}
		else if (c != '.') {
			_reportFatalError("Expected '.', found: " + (char)c);
		}

		c = _skipLine(c);

		Statement st = _createStatement(_subject, _predicate, _object);
		_rdfHandler.handleStatement(st);

		_subject = null;
		_predicate = null;
		_object = null;

		return c;
	}

	private int _parseSubject(int c)
		throws IOException, RDFParseException
	{
		StringBuilder sb = new StringBuilder(100);

		// subject is either an uriref (<foo://bar>) or a nodeID (_:node1)
		if (c == '<') {
			// subject is an uriref
			c = _parseUriRef(c, sb);
			_subject = _createURI(sb.toString());
		}
		else if (c == '_') {
			// subject is a bNode
			c = _parseNodeID(c, sb);
			_subject = _createBNode(sb.toString());
		}
		else if (c == -1) {
			_throwEOFException();
		}
		else {
			_reportFatalError("Expected '<' or '_', found: " + (char)c);
		}

		return c;
	}

	private int _parsePredicate(int c)
		throws IOException, RDFParseException
	{
		StringBuilder sb = new StringBuilder(100);

		// predicate must be an uriref (<foo://bar>)
		if (c == '<') {
			// predicate is an uriref
			c = _parseUriRef(c, sb);
			_predicate = _createURI(sb.toString());
		}
		else if (c == -1) {
			_throwEOFException();
		}
		else {
			_reportFatalError("Expected '<', found: " + (char)c);
		}

		return c;
	}

	private int _parseObject(int c)
		throws IOException, RDFParseException
	{
		StringBuilder sb = new StringBuilder(100);

		// object is either an uriref (<foo://bar>), a nodeID (_:node1) or a
		// literal ("foo"-en or "1"^^<xsd:integer>).
		if (c == '<') {
			// object is an uriref
			c = _parseUriRef(c, sb);
			_object = _createURI(sb.toString());
		}
		else if (c == '_') {
			// object is a bNode
			c = _parseNodeID(c, sb);
			_object = _createBNode(sb.toString());
		}
		else if (c == '"') {
			// object is a literal
			StringBuilder lang = new StringBuilder(8);
			StringBuilder datatype = new StringBuilder(40);
			c = _parseLiteral(c, sb, lang, datatype);
			_object = _createLiteral(sb.toString(), lang.toString(), datatype.toString());
		}
		else if (c == -1) {
			_throwEOFException();
		}
		else {
			_reportFatalError("Expected '<', '_' or '\"', found: " + (char)c);
		}

		return c;
	}

	private int _parseUriRef(int c, StringBuilder uriRef)
		throws IOException, RDFParseException
	{
		// Supplied char is '<', ignore it.

		// Read up to the next '>' character
		c = _reader.read();
		while (c != '>') {
			if (c == -1) {
				_throwEOFException();
			}
			uriRef.append( (char)c );
			c = _reader.read();
		}

		// c == '>', read next char
		c = _reader.read();

		return c;
	}

	private int _parseNodeID(int c, StringBuilder name)
		throws IOException, RDFParseException
	{
		// Supplied char is '_', ignore it.

		c = _reader.read();
		if (c == -1) {
			_throwEOFException();
		}
		else if (c != ':') {
			_reportError("Expected ':', found: " + (char)c);
		}

		c = _reader.read();
		if (c == -1) {
			_throwEOFException();
		}
		else if (!NTriplesUtil.isLetter(c)) {
			_reportError("Expected a letter, found: " + (char)c);
		}
		name.append( (char)c );

		// Read all following letter and numbers, they are part of the name
		c = _reader.read();
		while (c != -1 && NTriplesUtil.isLetterOrNumber(c)) {
			name.append( (char)c );
			c = _reader.read();
		}

		return c;
	}

	private int _parseLiteral(
		int c, StringBuilder value, StringBuilder lang, StringBuilder datatype)
		throws IOException, RDFParseException
	{
		// Supplied char is '"', ignore it.

		// Read up to the next '"' character
		c = _reader.read();
		while (c != '"') {
			if (c == -1) {
				_throwEOFException();
			}
			value.append( (char)c );

			if (c == '\\') {
				// This escapes the next character, which might be a double quote
				c = _reader.read();
				if (c == -1) {
					_throwEOFException();
				}
				value.append( (char)c );
			}

			c = _reader.read();
		}

		// c == '"', read next char
		c = _reader.read();

		if (c == '@') {
			// Read language
			c = _reader.read();
			while (c != -1 && c != '.' && c != '^' && c != ' ' && c != '\t') {
				lang.append( (char)c );
				c = _reader.read();
			}
		}
		else if (c == '^') {
			// Read datatype
			c = _reader.read();

			// c should be another '^'
			if (c == -1) {
				_throwEOFException();
			}
			else if (c != '^') {
				_reportError("Expected '^', found: " + (char)c);
			}

			c = _reader.read();

			// c should be a '<'
			if (c == -1) {
				_throwEOFException();
			}
			else if (c != '<') {
				_reportError("Expected '<', found: " + (char)c);
			}

			c = _parseUriRef(c, datatype);
		}

		return c;
	}

	protected URI _createURI(String uri)
		throws RDFParseException
	{
		try {
			uri = NTriplesUtil.unescapeString(uri);
		}
		catch (IllegalArgumentException e) {
			_reportError(e.getMessage());
		}

		return super._createURI(uri);
	}

	protected Literal _createLiteral(String label, String lang, String datatype)
		throws RDFParseException
	{
		try {
			label = NTriplesUtil.unescapeString(label);
		}
		catch (IllegalArgumentException e) {
			_reportError(e.getMessage());
		}

		if (lang.length() == 0) {
			lang = null;
		}

		if (datatype.length() == 0) {
			datatype = null;
		}

		URI dtURI = null;
		if (datatype != null) {
			dtURI = _createURI(datatype);
		}

		return super._createLiteral(label, lang, dtURI);
	}

	/**
	 * Overrides {@link RDFParserBase#_reportWarning(String)}, adding line
	 * number information to the error.
	 */
	protected void _reportWarning(String msg) {
		_reportWarning(msg, _lineNo, -1);
	}

	/**
	 * Overrides {@link RDFParserBase#_reportError(String)}, adding line
	 * number information to the error.
	 */
	protected void _reportError(String msg)
		throws RDFParseException
	{
		_reportError(msg, _lineNo, -1);
	}

	/**
	 * Overrides {@link RDFParserBase#_reportFatalError(String)}, adding line
	 * number information to the error.
	 */
	protected void _reportFatalError(String msg)
		throws RDFParseException
	{
		_reportFatalError(msg, _lineNo, -1);
	}

	/**
	 * Overrides {@link RDFParserBase#_reportFatalError(Exception)}, adding line
	 * number information to the error.
	 */
	protected void _reportFatalError(Exception e)
		throws RDFParseException
	{
		_reportFatalError(e, _lineNo, -1);
	}

	private void _throwEOFException()
		throws RDFParseException
	{
		throw new RDFParseException("Unexpected end of file");
	}
}
