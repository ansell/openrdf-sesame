/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import java.util.HashMap;
import java.util.Map;

import info.aduna.net.ParsedURI;

import org.openrdf.model.BNode;
import org.openrdf.model.BNodeFactory;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.MappedBNodeFactoryImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;

/**
 * Base class for {@link RDFParser}s offering common functionality for RDF
 * parsers.
 * 
 * @author Arjohn Kampman
 */
public abstract class RDFParserBase implements RDFParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The RDFHandler that will handle the parsed RDF.
	 */
	protected RDFHandler rdfHandler;

	/**
	 * An optional ParseErrorListener to report parse errors to.
	 */
	private ParseErrorListener errListener;

	/**
	 * An optional ParseLocationListener to report parse progress in the form of
	 * line- and column numbers to.
	 */
	private ParseLocationListener locationListener;

	/**
	 * The ValueFactory passed to the parser.
	 */
	private ValueFactory originalValueFactory;

	/**
	 * The ValueFactory to use for creating RDF model objects.
	 */
	private ValueFactory valueFactory;

	/**
	 * Flag indicating whether the parser should verify the data it parses.
	 */
	private boolean verifyData;

	/**
	 * Flag indicating whether the parser should preserve bnode identifiers from
	 * the parsed data in the created BNode objects.
	 */
	private boolean preserveBNodeIDs;

	/**
	 * Flag indicating whether the parser should immediately stop parsing when it
	 * finds an error in the data.
	 */
	private boolean stopAtFirstError;

	/**
	 * Indicates how datatyped literals should be handled.
	 */
	private DatatypeHandling datatypeHandling;

	/**
	 * The base URI for resolving relative URIs.
	 */
	private ParsedURI baseURI;

	/**
	 * Mapping from namespace prefixes to namespace names.
	 */
	private Map<String, String> namespaceTable;

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
		namespaceTable = new HashMap<String, String>(16);

		setValueFactory(valueFactory);
		setVerifyData(true);
		setPreserveBNodeIDs(false);
		setStopAtFirstError(true);
		setDatatypeHandling(DatatypeHandling.VERIFY);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setValueFactory(ValueFactory vf) {
		this.originalValueFactory = vf;
		clearBNodeIDMap();
	}

	public void setRDFHandler(RDFHandler handler) {
		rdfHandler = handler;
	}

	public RDFHandler getRDFHandler() {
		return rdfHandler;
	}

	public void setParseErrorListener(ParseErrorListener el) {
		errListener = el;
	}

	public ParseErrorListener getParseErrorListener() {
		return errListener;
	}

	public void setParseLocationListener(ParseLocationListener el) {
		locationListener = el;
	}

	public ParseLocationListener getParseLocationListener() {
		return locationListener;
	}

	public void setVerifyData(boolean verifyData) {
		this.verifyData = verifyData;
	}

	public boolean verifyData() {
		return verifyData;
	}

	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		this.preserveBNodeIDs = preserveBNodeIDs;
		clearBNodeIDMap();
	}

	public boolean preserveBNodeIDs() {
		return preserveBNodeIDs;
	}

	public void setStopAtFirstError(boolean stopAtFirstError) {
		this.stopAtFirstError = stopAtFirstError;
	}

	public boolean stopAtFirstError() {
		return stopAtFirstError;
	}

	public void setDatatypeHandling(DatatypeHandling datatypeHandling) {
		this.datatypeHandling = datatypeHandling;
	}

	public DatatypeHandling datatypeHandling() {
		return datatypeHandling;
	}

	/**
	 * Parses and normalizes the supplied URI-string and sets it as the base URI
	 * for resolving relative URIs.
	 */
	protected void setBaseURI(String uriSpec) {
		// Store normalized base URI
		ParsedURI baseURI = new ParsedURI(uriSpec);
		baseURI.normalize();
		setBaseURI(baseURI);
	}

	/**
	 * Sets the base URI for resolving relative URIs.
	 */
	protected void setBaseURI(ParsedURI baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * Associates the specified prefix to the specified namespace.
	 */
	protected void setNamespace(String prefix, String namespace) {
		namespaceTable.put(prefix, namespace);
	}

	/**
	 * Gets the namespace that is associated with the specified prefix, if any.
	 */
	protected String getNamespace(String prefix) {
		return namespaceTable.get(prefix);
	}

	/**
	 * Clears any information that has been collected while parsing. This method
	 * must be called by subclasses when finishing the parse process.
	 */
	protected void clear() {
		baseURI = null;
		clearBNodeIDMap();
		namespaceTable.clear();
	}

	/**
	 * Clears the map that keeps track of blank nodes that have been parsed.
	 * Normally, this map is clear when the document has been parsed completely,
	 * but subclasses can clear the map at other moments too, for example when a
	 * bnode scope ends.
	 */
	protected void clearBNodeIDMap() {
		if (preserveBNodeIDs) {
			valueFactory = originalValueFactory;
		}
		else if (originalValueFactory != null) {
			BNodeFactory map = new MappedBNodeFactoryImpl(originalValueFactory);
			valueFactory = new ValueFactoryImpl(map, originalValueFactory);
		}
	}

	/**
	 * Resolves a URI-string against the base URI and creates a {@link URI}
	 * object for it.
	 */
	protected URI resolveURI(String uriSpec)
		throws RDFParseException
	{
		if (baseURI == null) {
			reportFatalError("Unable to resolve URIs, no base URI has been set");
		}

		// Resolve relative URIs against base URI
		ParsedURI uri = new ParsedURI(uriSpec);

		if (verifyData) {
			if (uri.isRelative() && !uri.isSelfReference() && baseURI.isOpaque()) {
				reportError("Relative URI '" + uriSpec + "' cannot be resolved using the opaque base URI '"
						+ baseURI + "'");
			}
		}

		uri = baseURI.resolve(uri);

		return createURI(uri.toString());
	}

	/**
	 * Creates a {@link URI} object for the specified URI-string.
	 */
	protected URI createURI(String uri)
		throws RDFParseException
	{
		try {
			return valueFactory.createURI(uri);
		}
		catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link BNode} object.
	 */
	protected BNode createBNode()
		throws RDFParseException
	{
		try {
			return valueFactory.createBNode();
		}
		catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a {@link BNode} object for the specified identifier.
	 */
	protected BNode createBNode(String nodeID)
		throws RDFParseException
	{
		try {
			return valueFactory.createBNode(nodeID);
		}
		catch (RuntimeException e) {
			reportFatalError(e);
			throw e;
		}
	}

	/**
	 * Creates a {@link Literal} object with the supplied parameters.
	 */
	protected Literal createLiteral(String label, String lang, URI datatype)
		throws RDFParseException
	{
		if (datatype != null) {
			if (datatypeHandling == DatatypeHandling.VERIFY) {
				if (!XMLDatatypeUtil.isValidValue(label, datatype)) {
					reportError("'" + label + "' is not a valid value for datatype " + datatype);
				}
			}
			else if (datatypeHandling == DatatypeHandling.NORMALIZE) {
				try {
					label = XMLDatatypeUtil.normalize(label, datatype);
				}
				catch (IllegalArgumentException e) {
					reportError("'" + label + "' is not a valid value for datatype " + datatype + ": "
							+ e.getMessage());
				}
			}
		}

		try {
			if (datatype != null) {
				return valueFactory.createLiteral(label, datatype);
			}
			else if (lang != null) {
				return valueFactory.createLiteral(label, lang);
			}
			else {
				return valueFactory.createLiteral(label);
			}
		}
		catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link Statement} object with the supplied components.
	 */
	protected Statement createStatement(Resource subj, URI pred, Value obj)
		throws RDFParseException
	{
		try {
			return valueFactory.createStatement(subj, pred, obj);
		}
		catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link Statement} object with the supplied components.
	 */
	protected Statement createStatement(Resource subj, URI pred, Value obj, Resource context)
		throws RDFParseException
	{
		try {
			return valueFactory.createStatement(subj, pred, obj, context);
		}
		catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Reports the specified line- and column number to the registered
	 * {@link ParseLocationListener}, if any.
	 */
	protected void reportLocation(int lineNo, int columnNo) {
		if (locationListener != null) {
			locationListener.parseLocationUpdate(lineNo, columnNo);
		}
	}

	/**
	 * Reports a warning to the registered ParseErrorListener, if any. This
	 * method simply calls {@link #reportWarning(String,int,int)} supplying
	 * <tt>-1</tt> for the line- and column number.
	 */
	protected void reportWarning(String msg) {
		reportWarning(msg, -1, -1);
	}

	/**
	 * Reports a warning with associated line- and column number to the
	 * registered ParseErrorListener, if any.
	 */
	protected void reportWarning(String msg, int lineNo, int columnNo) {
		if (errListener != null) {
			errListener.warning(msg, lineNo, columnNo);
		}
	}

	/**
	 * Reports an error to the registered ParseErrorListener, if any. This method
	 * simply calls {@link #reportError(String,int,int)} supplying <tt>-1</tt>
	 * for the line- and column number. This method throws a
	 * <tt>ParseException</tt> when 'stop-at-first-error' has been set to
	 * <tt>true</tt>.
	 * 
	 * @see #setStopAtFirstError
	 */
	protected void reportError(String msg)
		throws RDFParseException
	{
		reportError(msg, -1, -1);
	}

	/**
	 * Reports an error with associated line- and column number to the registered
	 * ParseErrorListener, if any. This method throws a <tt>ParseException</tt>
	 * when 'stop-at-first-error' has been set to <tt>true</tt>.
	 * 
	 * @see #setStopAtFirstError
	 */
	protected void reportError(String msg, int lineNo, int columnNo)
		throws RDFParseException
	{
		if (errListener != null) {
			errListener.error(msg, lineNo, columnNo);
		}

		if (stopAtFirstError) {
			throw new RDFParseException(msg, lineNo, columnNo);
		}
	}

	/**
	 * Reports a fatal error to the registered ParseErrorListener, if any, and
	 * throws a <tt>ParseException</tt> afterwards. This method simply calls
	 * {@link #reportFatalError(String,int,int)} supplying <tt>-1</tt> for the
	 * line- and column number.
	 */
	protected void reportFatalError(String msg)
		throws RDFParseException
	{
		reportFatalError(msg, -1, -1);
	}

	/**
	 * Reports a fatal error with associated line- and column number to the
	 * registered ParseErrorListener, if any, and throws a
	 * <tt>ParseException</tt> afterwards.
	 */
	protected void reportFatalError(String msg, int lineNo, int columnNo)
		throws RDFParseException
	{
		if (errListener != null) {
			errListener.fatalError(msg, lineNo, columnNo);
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
	 * This method simply calls {@link #reportFatalError(Exception,int,int)}
	 * supplying <tt>-1</tt> for the line- and column number.
	 */
	protected void reportFatalError(Exception e)
		throws RDFParseException
	{
		reportFatalError(e, -1, -1);
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
	protected void reportFatalError(Exception e, int lineNo, int columnNo)
		throws RDFParseException
	{
		if (e instanceof RDFParseException) {
			throw (RDFParseException)e;
		}
		else {
			if (errListener != null) {
				errListener.fatalError(e.getMessage(), lineNo, columnNo);
			}

			throw new RDFParseException(e, lineNo, columnNo);
		}
	}

}
