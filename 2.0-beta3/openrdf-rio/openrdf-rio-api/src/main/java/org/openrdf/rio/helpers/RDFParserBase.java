/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import java.util.HashMap;
import java.util.Map;

import info.aduna.net.ParsedURI;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;

/**
 * Base class for {@link RDFParser}s offering common functionality for RDF
 * parsers.
 */
public abstract class RDFParserBase implements RDFParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The RDFHandler that will handle the parsed RDF.
	 */
	protected RDFHandler _rdfHandler;

	/**
	 * An optional ParseErrorListener to report parse errors to.
	 */
	private ParseErrorListener _errListener;

	/**
	 * An optional ParseLocationListener to report parse progress in the form of
	 * line- and column numbers to.
	 */
	private ParseLocationListener _locListener;

	/**
	 * The ValueFactory to use for creating RDF model objects.
	 */
	private ValueFactory _valueFactory;

	/**
	 * Flag indicating whether the parser should verify the data it parses.
	 */
	private boolean _verifyData;

	/**
	 * Flag indicating whether the parser should preserve bnode identifiers from
	 * the parsed data in the created BNode objects.
	 */
	private boolean _preserveBNodeIDs;

	/**
	 * Flag indicating whether the parser should immediately stop parsing when it
	 * finds an error in the data.
	 */
	private boolean _stopAtFirstError;

	/**
	 * Indicates how datatyped literals should be handled.
	 */
	private DatatypeHandling _datatypeHandling;

	/**
	 * The base URI for resolving relative URIs.
	 */
	private ParsedURI _baseURI;

	/**
	 * Mapping from blank node identifiers as used in the RDF document to the
	 * object created for it by the ValueFactory. This mapping is used to return
	 * identical BNode objects for recurring blank node identifiers.
	 */
	private Map<String, BNode> _bNodeIDMap;

	/**
	 * Mapping from namespace prefixes to namespace names.
	 */
	private Map<String, String> _namespaceTable;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFParserBase that will use a {@link ValueFactoryImpl} to
	 * create RDF model objects.
	 */
	public RDFParserBase() {
		this(new ValueFactoryImpl());
	}

	/**
	 * Creates a new TurtleParser that will use the supplied ValueFactory to
	 * create RDF model objects.
	 * 
	 * @param valueFactory
	 *        A ValueFactory.
	 */
	public RDFParserBase(ValueFactory valueFactory) {
		_bNodeIDMap = new HashMap<String, BNode>(16);
		_namespaceTable = new HashMap<String, String>(16);

		setValueFactory(valueFactory);
		setVerifyData(true);
		setPreserveBNodeIDs(false);
		setStopAtFirstError(true);
		setDatatypeHandling(DatatypeHandling.VERIFY);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setValueFactory(ValueFactory valueFactory) {
		_valueFactory = valueFactory;
	}

	public void setRDFHandler(RDFHandler handler) {
		_rdfHandler = handler;
	}

	public RDFHandler getRDFHandler() {
		return _rdfHandler;
	}

	public void setParseErrorListener(ParseErrorListener el) {
		_errListener = el;
	}

	public ParseErrorListener getParseErrorListener() {
		return _errListener;
	}

	public void setParseLocationListener(ParseLocationListener el) {
		_locListener = el;
	}

	public ParseLocationListener getParseLocationListener() {
		return _locListener;
	}

	public void setVerifyData(boolean verifyData) {
		_verifyData = verifyData;
	}

	public boolean verifyData() {
		return _verifyData;
	}

	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		_preserveBNodeIDs = preserveBNodeIDs;
	}

	public boolean preserveBNodeIDs() {
		return _preserveBNodeIDs;
	}

	public void setStopAtFirstError(boolean stopAtFirstError) {
		_stopAtFirstError = stopAtFirstError;
	}

	public boolean stopAtFirstError() {
		return _stopAtFirstError;
	}

	public void setDatatypeHandling(DatatypeHandling datatypeHandling) {
		_datatypeHandling = datatypeHandling;
	}

	public DatatypeHandling datatypeHandling() {
		return _datatypeHandling;
	}

	/**
	 * Parses and normalizes the supplied URI-string and sets it as the base URI
	 * for resolving relative URIs.
	 */
	protected void _setBaseURI(String uriSpec) {
		// Store normalized base URI
		ParsedURI baseURI = new ParsedURI(uriSpec);
		baseURI.normalize();
		_setBaseURI(baseURI);
	}

	/**
	 * Sets the base URI for resolving relative URIs.
	 */
	protected void _setBaseURI(ParsedURI baseURI) {
		_baseURI = baseURI;
	}

	/**
	 * Associates the specified prefix to the specified namespace.
	 */
	protected void _setNamespace(String prefix, String namespace) {
		_namespaceTable.put(prefix, namespace);
	}

	/**
	 * Gets the namespace that is associated with the specified prefix, if any.
	 */
	protected String _getNamespace(String prefix) {
		return _namespaceTable.get(prefix);
	}

	/**
	 * Clears any information that has been collected while parsing. This method
	 * must be called by subclasses when finishing the parse process.
	 */
	protected void _clear() {
		_baseURI = null;
		_bNodeIDMap.clear();
		_namespaceTable.clear();
	}

	/**
	 * Resolves a URI-string against the base URI and creates a {@link URI}
	 * object for it.
	 */
	protected URI _resolveURI(String uriSpec)
		throws RDFParseException
	{
		if (_baseURI == null) {
			_reportFatalError("Unable to resolve URIs, no base URI has been set");
		}

		// Resolve relative URIs against base URI
		ParsedURI uri = new ParsedURI(uriSpec);

		if (_verifyData) {
			if (uri.isRelative() && !uri.isSelfReference() && _baseURI.isOpaque()) {
				_reportError("Relative URI '" + uriSpec + "' cannot be resolved using the opaque base URI '"
						+ _baseURI + "'");
			}
		}

		uri = _baseURI.resolve(uri);

		return _createURI(uri.toString());
	}

	/**
	 * Creates a {@link URI} object for the specified URI-string.
	 */
	protected URI _createURI(String uri)
		throws RDFParseException
	{
		try {
			return _valueFactory.createURI(uri);
		}
		catch (Exception e) {
			_reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link BNode} object.
	 */
	protected BNode _createBNode()
		throws RDFParseException
	{
		try {
			return _valueFactory.createBNode();
		}
		catch (Exception e) {
			_reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a {@link BNode} object for the specified identifier.
	 */
	protected BNode _createBNode(String nodeID)
		throws RDFParseException
	{
		// Maybe the node ID has been used before:
		BNode result = _bNodeIDMap.get(nodeID);

		if (result == null) {
			// This is a new node ID, create a new BNode object for it
			try {
				if (_preserveBNodeIDs) {
					result = _valueFactory.createBNode(nodeID);
				}
				else {
					result = _valueFactory.createBNode();
				}
			}
			catch (Exception e) {
				_reportFatalError(e);
			}

			// Remember it, the nodeID might occur again.
			_bNodeIDMap.put(nodeID, result);
		}

		return result;
	}

	/**
	 * Creates a {@link Literal} object with the supplied parameters.
	 */
	protected Literal _createLiteral(String label, String lang, URI datatype)
		throws RDFParseException
	{
		if (datatype != null) {
			if (_datatypeHandling == DatatypeHandling.VERIFY) {
				if (!XMLDatatypeUtil.isValidValue(label, datatype)) {
					_reportError("'" + label + "' is not a valid value for datatype " + datatype);
				}
			}
			else if (_datatypeHandling == DatatypeHandling.NORMALIZE) {
				try {
					label = XMLDatatypeUtil.normalize(label, datatype);
				}
				catch (IllegalArgumentException e) {
					_reportError("'" + label + "' is not a valid value for datatype " + datatype + ": "
							+ e.getMessage());
				}
			}
		}

		try {
			if (datatype != null) {
				return _valueFactory.createLiteral(label, datatype);
			}
			else if (lang != null) {
				return _valueFactory.createLiteral(label, lang);
			}
			else {
				return _valueFactory.createLiteral(label);
			}
		}
		catch (Exception e) {
			_reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link Statement} object with the supplied components.
	 */
	protected Statement _createStatement(Resource subj, URI pred, Value obj)
		throws RDFParseException
	{
		try {
			return _valueFactory.createStatement(subj, pred, obj);
		}
		catch (Exception e) {
			_reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link Statement} object with the supplied components.
	 */
	protected Statement _createStatement(Resource subj, URI pred, Value obj, Resource context)
		throws RDFParseException
	{
		try {
			return _valueFactory.createStatement(subj, pred, obj, context);
		}
		catch (Exception e) {
			_reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Reports the specified line- and column number to the registered
	 * {@link ParseLocationListener}, if any.
	 */
	protected void _reportLocation(int lineNo, int columnNo) {
		if (_locListener != null) {
			_locListener.parseLocationUpdate(lineNo, columnNo);
		}
	}

	/**
	 * Reports a warning to the registered ParseErrorListener, if any. This
	 * method simply calls {@link #_reportWarning(String,int,int)} supplying
	 * <tt>-1</tt> for the line- and column number.
	 */
	protected void _reportWarning(String msg) {
		_reportWarning(msg, -1, -1);
	}

	/**
	 * Reports a warning with associated line- and column number to the
	 * registered ParseErrorListener, if any.
	 */
	protected void _reportWarning(String msg, int lineNo, int columnNo) {
		if (_errListener != null) {
			_errListener.warning(msg, lineNo, columnNo);
		}
	}

	/**
	 * Reports an error to the registered ParseErrorListener, if any. This method
	 * simply calls {@link #_reportError(String,int,int)} supplying <tt>-1</tt>
	 * for the line- and column number. This method throws a
	 * <tt>ParseException</tt> when 'stop-at-first-error' has been set to
	 * <tt>true</tt>.
	 * 
	 * @see #setStopAtFirstError
	 */
	protected void _reportError(String msg)
		throws RDFParseException
	{
		_reportError(msg, -1, -1);
	}

	/**
	 * Reports an error with associated line- and column number to the registered
	 * ParseErrorListener, if any. This method throws a <tt>ParseException</tt>
	 * when 'stop-at-first-error' has been set to <tt>true</tt>.
	 * 
	 * @see #setStopAtFirstError
	 */
	protected void _reportError(String msg, int lineNo, int columnNo)
		throws RDFParseException
	{
		if (_errListener != null) {
			_errListener.error(msg, lineNo, columnNo);
		}

		if (_stopAtFirstError) {
			throw new RDFParseException(msg, lineNo, columnNo);
		}
	}

	/**
	 * Reports a fatal error to the registered ParseErrorListener, if any, and
	 * throws a <tt>ParseException</tt> afterwards. This method simply calls
	 * {@link #_reportFatalError(String,int,int)} supplying <tt>-1</tt> for the
	 * line- and column number.
	 */
	protected void _reportFatalError(String msg)
		throws RDFParseException
	{
		_reportFatalError(msg, -1, -1);
	}

	/**
	 * Reports a fatal error with associated line- and column number to the
	 * registered ParseErrorListener, if any, and throws a
	 * <tt>ParseException</tt> afterwards.
	 */
	protected void _reportFatalError(String msg, int lineNo, int columnNo)
		throws RDFParseException
	{
		if (_errListener != null) {
			_errListener.fatalError(msg, lineNo, columnNo);
		}

		throw new RDFParseException(msg, lineNo, columnNo);
	}

	/**
	 * Reports a fatal error to the registered ParseErrorListener, if any, and
	 * throws a <tt>ParseException</tt> afterwards. An exception is made for
	 * the case where the supplied exception is a {@link RDFParseException}; in
	 * that case the supplied exception is not wrapped in another ParseException
	 * and the error message is not reported to the ParseErrorListener, assuming
	 * that it has already been reported when the original ParseException was
	 * thrown.
	 * <p>
	 * This method simply calls {@link #_reportFatalError(Exception,int,int)}
	 * supplying <tt>-1</tt> for the line- and column number.
	 */
	protected void _reportFatalError(Exception e)
		throws RDFParseException
	{
		_reportFatalError(e, -1, -1);
	}

	/**
	 * Reports a fatal error with associated line- and column number to the
	 * registered ParseErrorListener, if any, and throws a
	 * <tt>ParseException</tt> wrapped the supplied exception afterwards. An
	 * exception is made for the case where the supplied exception is a
	 * {@link RDFParseException}; in that case the supplied exception is not
	 * wrapped in another ParseException and the error message is not reported to
	 * the ParseErrorListener, assuming that it has already been reported when
	 * the original ParseException was thrown.
	 */
	protected void _reportFatalError(Exception e, int lineNo, int columnNo)
		throws RDFParseException
	{
		if (e instanceof RDFParseException) {
			throw (RDFParseException)e;
		}
		else {
			if (_errListener != null) {
				_errListener.fatalError(e.getMessage(), lineNo, columnNo);
			}

			throw new RDFParseException(e, lineNo, columnNo);
		}
	}

}
