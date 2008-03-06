/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.turtle;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import info.aduna.text.ASCIIUtil;


import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFParserBase;

/**
 * RDF parser for Turtle files. A specification of Turtle can be found
 * <a href="http://www.dajobe.org/2004/01/turtle/">in this document</a>. This
 * parser is not thread-safe, therefore its public methods are synchronized.
 * <p>
 * This implementation is based on the 2006/01/02 version of the Turtle
 * specification, with slight deviations:
 * <ul>
 * <li>Normalization of integer, floating point and boolean values is dependent
 * on the specified datatype handling. According to the specification, integers
 * and booleans should be normalized, but floats don't.</li>
 * <li>Comments can be used anywhere in the document, and extend to the end of
 * the line. The Turtle grammar doesn't allow comments to be used inside triple
 * constructs that extend over multiple lines, but the author's own parser
 * deviates from this too.</li>
 * </ul>
 */
public class TurtleParser extends RDFParserBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	private LineNumberReader _lineReader;
	private PushbackReader _reader;

	private Resource _subject;
	private URI _predicate;
	private Value _object;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TurtleParser that will use a {@link ValueFactoryImpl} to
	 * create RDF model objects.
	 */
	public TurtleParser() {
		super();
	}

	/**
	 * Creates a new TurtleParser that will use the supplied ValueFactory to
	 * create RDF model objects.
	 *
	 * @param valueFactory A ValueFactory.
	 */
	public TurtleParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFParser.getRDFFormat()
	public final RDFFormat getRDFFormat() {
		return RDFFormat.TURTLE;
	}

	/**
	 * Implementation of the <tt>parse(InputStream, String)</tt> method defined
	 * in the RDFParser interface. 	
	 * 
	 * @param in The InputStream from which to read the data, must not be
	 * <tt>null</tt>. The InputStream is supposed to contain UTF-8 encoded
	 * Unicode characters, as per the Turtle specification.
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
			parse(new InputStreamReader(in, "UTF-8"), baseURI);
		}
		catch (UnsupportedEncodingException e) {
			// Every platform should support the UTF-8 encoding...
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

		_lineReader = new LineNumberReader(reader);
		// Start counting lines at 1:
		_lineReader.setLineNumber(1);

		// Allow at most 2 characters to be pushed back:
		_reader = new PushbackReader(_lineReader, 2);

		// Store normalized base URI
		_setBaseURI(baseURI);

		_reportLocation(_lineReader.getLineNumber(), -1);

		try {
			while (true) {
				int c = _skipWSC();

				if (c == -1) {
					break;
				}
				else if (c == '@') {
					_parsePrefix();
				}
				else {
					_parseTriple();
				}
			}
		}
		finally {
			_clear();
		}

		_rdfHandler.endRDF();
	}

	private void _parsePrefix()
		throws IOException, RDFParseException, RDFHandlerException
	{
		// Verify that the first characters form the string "prefix"
		_verifyCharacter(_read(), "@");
		_verifyCharacter(_read(), "p");
		_verifyCharacter(_read(), "r");
		_verifyCharacter(_read(), "e");
		_verifyCharacter(_read(), "f");
		_verifyCharacter(_read(), "i");
		_verifyCharacter(_read(), "x");

		_skipWSC();

		// Read prefix ID (e.g. "rdf:" or ":")
		StringBuilder prefixID = new StringBuilder(8);

		while (true) {
			int c = _read();

			if (c == ':') {
				// marks the end of the prefix
				_read();
				break;
			}
			else if (c == -1) {
				_throwEOFException();
			}

			prefixID.append( (char)c );
		}

		_skipWSC();

		// Read the namespace URI
		URI namespace = _parseURI();

		_skipWSC();

		// Read closing dot
		_verifyCharacter(_read(), ".");

		// Store and report this namespace mapping
		String prefixStr = prefixID.toString();
		String namespaceStr = namespace.toString();

		_setNamespace(prefixStr, namespaceStr);

		_rdfHandler.handleNamespace(prefixStr, namespaceStr);
	}

	private void _parseTriple()
		throws IOException, RDFParseException, RDFHandlerException
	{
		_parseSubject();

		_skipWSC();

		_parsePredicateObjectList();

		_skipWSC();

		_verifyCharacter(_read(), ".");

		_subject = null;
		_predicate = null;
		_object = null;
	}

	private void _parsePredicateObjectList()
		throws IOException, RDFParseException, RDFHandlerException
	{
		_predicate = _parsePredicate();

		_skipWSC();

		_parseObjectList();

		while (_skipWSC() == ';') {
			_read();

			int c = _skipWSC();

			if (c == '.' || // end of triple
				c == ']') // end of predicateObjectList inside blank node
			{
				break;
			}

			_predicate = _parsePredicate();

			_skipWSC();

			_parseObjectList();
		}
	}

	private void _parseObjectList()
		throws IOException, RDFParseException, RDFHandlerException
	{
		_parseObject();
		
		while (_skipWSC() == ',') {
			_read();

			_skipWSC();

			_parseObject();
		}
	}

	private void _parseSubject()
		throws IOException, RDFParseException, RDFHandlerException
	{
		int c = _peek();

		if (c == '(') {
			_subject = _parseCollection();
		}
		else if (c == '[') {
			_subject = _parseImplicitBlank();
		}
		else {
			Value value = _parseValue();

			if (value instanceof Resource) {
				_subject = (Resource)value;
			}
			else {
				_reportFatalError("Illegal subject value: " + value);
			}
		}
	}

	private URI _parsePredicate()
		throws IOException, RDFParseException
	{
		// Check if the short-cut 'a' is used
		int c1 = _read();

		if (c1 == 'a') {
			int c2 = _read();

			if (TurtleUtil.isWhitespace(c2)) {
				// Short-cut is used, return the rdf:type URI
				return RDF.TYPE;
			}

			// Short-cut is not used, unread all characters
			_unread(c2);
		}
		_unread(c1);

		// Predicate is a normal resource
		Value predicate = _parseValue();
		if (predicate instanceof URI) {
			return (URI)predicate;
		}
		else {
			_reportFatalError("Illegal predicate value: " + predicate);
			return null;
		}
	}

	private void _parseObject()
		throws IOException, RDFParseException, RDFHandlerException
	{
		int c = _peek();

		if (c == '(') {
			_object = _parseCollection();
		}
		else if (c == '[') {
			_object = _parseImplicitBlank();
		}
		else {
			_object = _parseValue();
		}

		_reportStatement(_subject, _predicate, _object);
	}

	/**
	 * Parses a collection, e.g. <tt>( item1 item2 item3 )</tt>.
	 */
	private Resource _parseCollection()
		throws IOException, RDFParseException, RDFHandlerException
	{
		_verifyCharacter(_read(), "(");

		int c = _skipWSC();

		if (c == ')') {
			// Empty list
			_read();
			return RDF.NIL;
		}
		else {
			BNode listRoot = _createBNode();

			// Remember current subject and predicate
			Resource oldSubject = _subject;
			URI oldPredicate = _predicate;

			// generated bNode becomes subject, predicate becomes rdf:first
			_subject = listRoot;
			_predicate = RDF.FIRST;

			_parseObject();

			BNode bNode = listRoot;

			while (_skipWSC() != ')') {
				// Create another list node and link it to the previous
				BNode newNode = _createBNode();
				_reportStatement(bNode, RDF.REST, newNode);

				// New node becomes the current
				_subject = bNode = newNode;

				_parseObject();
			}

			// Skip ')'
			_read();

			// Close the list
			_reportStatement(bNode, RDF.REST, RDF.NIL);

			// Restore previous subject and predicate
			_subject = oldSubject;
			_predicate = oldPredicate;

			return listRoot;
		}
	}

	/**
	 * Parses an implicit blank node. This method parses the token
	 * <tt>[]</tt> and predicateObjectLists that are surrounded by square
	 * brackets.
	 */
	private Resource _parseImplicitBlank()
		throws IOException, RDFParseException, RDFHandlerException
	{
		_verifyCharacter(_read(), "[");

		BNode bNode = _createBNode();

		int c = _read();
		if (c != ']') {
			_unread(c);

			// Remember current subject and predicate
			Resource oldSubject = _subject;
			URI oldPredicate = _predicate;

			// generated bNode becomes subject
			_subject = bNode;

			// Enter recursion with nested predicate-object list
			_skipWSC();

			_parsePredicateObjectList();

			_skipWSC();

			// Read closing bracket
			_verifyCharacter(_read(), "]");

			// Restore previous subject and predicate
			_subject = oldSubject;
			_predicate = oldPredicate;
		}

		return bNode;
	}

	/**
	 * Parses an RDF value. This method parses uriref, qname, node ID, quoted
	 * literal, integer, double and boolean.
	 */
	private Value _parseValue()
		throws IOException, RDFParseException
	{
		int c = _peek();

		if (c == '<') {
			// uriref, e.g. <foo://bar>
			return _parseURI();
		}
		else if (c == ':' || TurtleUtil.isPrefixStartChar(c)) {
			// qname or boolean
			return _parseQNameOrBoolean();
		}
		else if (c == '_') {
			// node ID, e.g. _:n1
			return _parseNodeID();
		}
		else if (c == '"') {
			// quoted literal, e.g. "foo" or """foo"""
			return _parseQuotedLiteral();
		}
		else if (ASCIIUtil.isNumber(c) || c == '.' || c == '+' || c == '-') {
			// integer or double, e.g. 123 or 1.2e3
			return _parseNumber();
		}
		else if (c == -1) {
			_throwEOFException();
			return null;
		}
		else {
			_reportFatalError("Expected an RDF value here, found '" + (char)c + "'");
			return null;
		}
	}

	/**
	 * Parses a quoted string, optionally followed by a language tag or
	 * datatype.
	 */
	private Literal _parseQuotedLiteral()
		throws IOException, RDFParseException
	{
		String label = _parseQuotedString();

		// Check for presence of a language tag or datatype
		int c = _peek();

		if (c == '@') {
			_read();

			// Read language
			StringBuilder lang = new StringBuilder(8);

			c = _read();
			if (c == -1) {
				_throwEOFException();
			}
			if (!TurtleUtil.isLanguageStartChar(c)) {
				_reportError("Expected a letter, found '" + (char)c + "'");
			}

			lang.append( (char)c );

			c = _read();
			while (TurtleUtil.isLanguageChar(c)) {
				lang.append( (char)c );
				c = _read();
			}

			_unread(c);

			return _createLiteral(label, lang.toString(), null);
		}
		else if (c == '^') {
			_read();

			// next character should be another '^'
			_verifyCharacter(_read(), "^");

			// Read datatype
			Value datatype = _parseValue();
			if (datatype instanceof URI) {
				return _createLiteral(label, null, (URI)datatype);
			}
			else {
				_reportFatalError("Illegal datatype value: " + datatype);
				return null;
			}
		}
		else {
			return _createLiteral(label, null, null);
		}
	}

	/**
	 * Parses a quoted string, which is either a "normal string" or a
	 * """long string""".
	 */
	private String _parseQuotedString()
		throws IOException, RDFParseException
	{
		String result = null;

		// First character should be '"'
		_verifyCharacter(_read(), "\"");

		// Check for long-string, which starts and ends with three double quotes
		int c2 = _read();
		int c3 = _read();

		if (c2 == '"' && c3 == '"') {
			// Long string
			result = _parseLongString();
		}
		else {
			// Normal string
			_unread(c3);
			_unread(c2);

			result = _parseString();
		}

		// Unescape any escape sequences
		try {
			result = TurtleUtil.decodeString(result);
		}
		catch (IllegalArgumentException e) {
			_reportError(e.getMessage());
		}

		return result;
	}

	/**
	 * Parses a "normal string". This method assumes that the first double quote
	 * has already been parsed.
	 */
	private String _parseString()
		throws IOException, RDFParseException
	{
		StringBuilder sb = new StringBuilder(32);

		while (true) {
			int c = _read();

			if (c == '"') {
				break;
			}
			else if (c == -1) {
				_throwEOFException();
			}

			sb.append( (char)c );

			if (c == '\\') {
				// This escapes the next character, which might be a '"'
				c = _read();
				if (c == -1) {
					_throwEOFException();
				}
				sb.append( (char)c );
			}
		}

		return sb.toString();
	}

	/**
	 * Parses a """long string""". This method assumes that the first three
	 * double quotes have already been parsed.
	 */
	private String _parseLongString()
		throws IOException, RDFParseException
	{
		StringBuilder sb = new StringBuilder(1024);

		int doubleQuoteCount = 0;
		int c;

		while (doubleQuoteCount < 3) {
			c = _read();

			if (c == -1) {
				_throwEOFException();
			}
			else if (c == '"') {
				doubleQuoteCount++;
			}
			else {
				doubleQuoteCount = 0;
			}

			sb.append( (char)c );

			if (c == '\\') {
				// This escapes the next character, which might be a '"'
				c = _read();
				if (c == -1) {
					_throwEOFException();
				}
				sb.append( (char)c );
			}
		}

		return sb.substring(0, sb.length() - 3);
	}

	private Literal _parseNumber()
		throws IOException, RDFParseException
	{
		StringBuilder value = new StringBuilder(8);
		URI datatype = XMLSchema.INTEGER;

		int c = _read();

		// read optional sign character
		if (c == '+' || c == '-') {
			value.append( (char)c );
			c = _read();
		}

		while (ASCIIUtil.isNumber(c)) {
			value.append( (char)c );
			c = _read();
		}

		if (c == '.' || c == 'e' || c == 'E') {
			// We're parsing a decimal or a double
			datatype = XMLSchema.DECIMAL;

			// read optional fractional digits
			if (c == '.') {
				value.append( (char)c );

				c = _read();
				while (ASCIIUtil.isNumber(c)) {
					value.append( (char)c );
					c = _read();
				}

				if (value.length() == 1) {
					// We've only parsed a '.'
					_reportFatalError("Object for statement missing");
				}
			}
			else {
				if (value.length() == 0) {
					// We've only parsed an 'e' or 'E'
					_reportFatalError("Object for statement missing");
				}
			}

			// read optional exponent
			if (c == 'e' || c == 'E') {
				datatype = XMLSchema.DOUBLE;
				value.append( (char)c );

				c = _read();
				if (c == '+' || c == '-') {
					value.append( (char)c );
					c = _read();
				}

				if (!ASCIIUtil.isNumber(c)) {
					_reportError("Exponent value missing");
				}

				value.append( (char)c );

				c = _read();
				while (ASCIIUtil.isNumber(c)) {
					value.append( (char)c );
					c = _read();
				}
			}
		}

		// Unread last character, it isn't part of the number
		_unread(c);

		// Return result as a typed literal
		return _createLiteral(value.toString(), null, datatype);
	}

	private URI _parseURI()
		throws IOException, RDFParseException
	{
		StringBuilder uriBuf = new StringBuilder(100);

		// First character should be '<'
		int c = _read();
		_verifyCharacter(c, "<");

		// Read up to the next '>' character
		while (true) {
			c = _read();

			if (c == '>') {
				break;
			}
			else if (c == -1) {
				_throwEOFException();
			}

			uriBuf.append( (char)c );

			if (c == '\\') {
				// This escapes the next character, which might be a '>'
				c = _read();
				if (c == -1) {
					_throwEOFException();
				}
				uriBuf.append( (char)c );
			}
		}

		String uri = uriBuf.toString();

		// Unescape any escape sequences
		try {
			uri = TurtleUtil.decodeString(uri);
		}
		catch (IllegalArgumentException e) {
			_reportError(e.getMessage());
		}

		return super._resolveURI(uri);
	}

	/**
	 * Parses qnames and boolean values, which have equivalent starting
	 * characters.
	 */
	private Value _parseQNameOrBoolean()
		throws IOException, RDFParseException
	{
		// First character should be a ':' or a letter
		int c = _read();
		if (c == -1) {
			_throwEOFException();
		}
		if (c != ':' && !TurtleUtil.isPrefixStartChar(c)) {
			_reportError("Expected a ':' or a letter, found '" + (char)c + "'");
		}

		String namespace = null;

		if (c == ':') {
			// qname using default namespace
			namespace = _getNamespace("");
			if (namespace == null) {
				_reportError("Default namespace used but not defined");
			}
		}
		else {
			// c is the first letter of the prefix
			StringBuilder prefix = new StringBuilder(8);
			prefix.append( (char)c );

			c = _read();
			while (TurtleUtil.isPrefixChar(c)) {
				prefix.append( (char)c );
				c = _read();
			}

			if (c != ':') {
				// prefix may actually be a boolean value
				String value = prefix.toString();

				if (value.equals("true") || value.equals("false")) {
					return _createLiteral(value, null, XMLSchema.BOOLEAN);
				}
			}

			_verifyCharacter(c, ":");

			namespace = _getNamespace(prefix.toString());
			if (namespace == null) {
				_reportError("Namespace prefix '" + prefix.toString() + "' used but not defined");
			}
		}

		// c == ':', read optional local name
		StringBuilder localName = new StringBuilder(16);
		c = _read();
		if (TurtleUtil.isNameStartChar(c)) {
			localName.append( (char)c );

			c = _read();
			while (TurtleUtil.isNameChar(c)) {
				localName.append( (char)c );
				c = _read();
			}
		}

		// Unread last character
		_unread(c);

		// Note: namespace has already been resolved
		return _createURI( namespace + localName.toString() );
	}

	/**
	 * Parses a blank node ID, e.g. <tt>_:node1</tt>.
	 */
	private BNode _parseNodeID()
		throws IOException, RDFParseException
	{
		// Node ID should start with "_:"
		_verifyCharacter(_read(), "_");
		_verifyCharacter(_read(), ":");

		// Read the node ID
		int c = _read();
		if (c == -1) {
			_throwEOFException();
		}
		else if (!TurtleUtil.isNameStartChar(c)) {
			_reportError("Expected a letter, found '" + (char)c + "'");
		}

		StringBuilder name = new StringBuilder(32);
		name.append( (char)c );

		// Read all following letter and numbers, they are part of the name
		c = _read();
		while (TurtleUtil.isNameChar(c)) {
			name.append( (char)c );
			c = _read();
		}

		_unread(c);

		return _createBNode( name.toString() );
	}

	private void _reportStatement(Resource subj, URI pred, Value obj)
		throws RDFParseException, RDFHandlerException
	{
		Statement st = _createStatement(subj, pred, obj);
		_rdfHandler.handleStatement(st);
	}

	/**
	 * Verifies that the supplied character <tt>c</tt> is one of the expected
	 * characters specified in <tt>expected</tt>. This method will throw a
	 * <tt>ParseException</tt> if this is not the case.
	 */
	private void _verifyCharacter(int c, String expected)
		throws RDFParseException
	{
		if (c == -1) {
			_throwEOFException();
		}
		else if (expected.indexOf((char)c) == -1) {
			StringBuilder msg = new StringBuilder(32);
			msg.append("Expected ");
			for (int i = 0; i < expected.length(); i++) {
				if (i > 0) {
					msg.append(" or ");
				}
				msg.append('\'');
				msg.append(expected.charAt(i));
				msg.append('\'');
			}
			msg.append(", found '");
			msg.append( (char)c );
			msg.append("'");

			_reportError(msg.toString());
		}
	}

	/**
	 * Consumes any white space characters (space, tab, line feed, newline) and
	 * comments (#-style) from <tt>_reader</tt>. After this method has been
	 * called, the first character that is returned by <tt>_reader</tt> is
	 * either a non-ignorable character, or EOF. For convenience, this character
	 * is also returned by this method.
	 *
	 * @return The next character that will be returned by <tt>_reader</tt>.
	 */
	private int _skipWSC()
		throws IOException
	{
		int c = _read();
		while (TurtleUtil.isWhitespace(c) || c == '#') {
			if (c == '#') {
				_skipLine();
			}

			c = _read();
		}

		_unread(c);

		return c;
	}

	/**
	 * Consumes characters from _reader until the first EOL has been read.
	 */
	private void _skipLine()
		throws IOException
	{
		int c = _read();
		while (c != -1 && c != 0xD && c != 0xA) {
			c = _read();
		}

		// c is equal to -1, \r or \n.
		// In case c is equal to \r, we should also read a following \n.
		if (c == 0xD) {
			c = _read();

			if (c != 0xA) {
				_unread(c);
			}
		}

		_reportLocation(_lineReader.getLineNumber(), -1);
	}

	private int _read()
		throws IOException
	{
		return _reader.read();
	}

	private void _unread(int c)
		throws IOException
	{
		if (c != -1) {
			_reader.unread(c);
		}
	}

	private int _peek()
		throws IOException
	{
		int result = _read();
		_unread(result);
		return result;
	}

	/**
	 * Overrides {@link RDFParserBase#_reportWarning(String)}, adding line
	 * number information to the error.
	 */
	protected void _reportWarning(String msg) {
		_reportWarning(msg, _lineReader.getLineNumber(), -1);
	}

	/**
	 * Overrides {@link RDFParserBase#_reportError(String)}, adding line
	 * number information to the error.
	 */
	protected void _reportError(String msg)
		throws RDFParseException
	{
		_reportError(msg, _lineReader.getLineNumber(), -1);
	}

	/**
	 * Overrides {@link RDFParserBase#_reportFatalError(String)}, adding line
	 * number information to the error.
	 */
	protected void _reportFatalError(String msg)
		throws RDFParseException
	{
		_reportFatalError(msg, _lineReader.getLineNumber(), -1);
	}

	/**
	 * Overrides {@link RDFParserBase#_reportFatalError(Exception)}, adding line
	 * number information to the error.
	 */
	protected void _reportFatalError(Exception e)
		throws RDFParseException
	{
		_reportFatalError(e, _lineReader.getLineNumber(), -1);
	}

	private void _throwEOFException()
		throws RDFParseException
	{
		throw new RDFParseException("Unexpected end of file");
	}
}
