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
package org.openrdf.rio.helpers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import info.aduna.net.ParsedURI;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RioSetting;
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

	/**
	 * Vocabulary Prefixes of W3C Documents (Recommendations or Notes)
	 * 
	 * @see http://www.w3.org/2011/rdfa-context/rdfa-1.1
	 */
	private static final Map<String, String> defaultPrefix;
	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("grddl", "http://www.w3.org/2003/g/data-view#");
		map.put("ma", "http://www.w3.org/ns/ma-ont#");
		map.put("owl", "http://www.w3.org/2002/07/owl#");
		map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		map.put("rdfa", "http://www.w3.org/ns/rdfa#");
		map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		map.put("rif", "http://www.w3.org/2007/rif#");
		map.put("skos", "http://www.w3.org/2004/02/skos/core#");
		map.put("skosxl", "http://www.w3.org/2008/05/skos-xl#");
		map.put("wdr", "http://www.w3.org/2007/05/powder#");
		map.put("void", "http://rdfs.org/ns/void#");
		map.put("wdrs", "http://www.w3.org/2007/05/powder-s#");
		map.put("xhv", "http://www.w3.org/1999/xhtml/vocab#");
		map.put("xml", "http://www.w3.org/XML/1998/namespace");
		map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		defaultPrefix = Collections.unmodifiableMap(new HashMap<String, String>(map));
	}

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
	 * The ValueFactory to use for creating RDF model objects.
	 */
	protected ValueFactory valueFactory;

	/**
	 * The base URI for resolving relative URIs.
	 */
	private ParsedURI baseURI;

	/**
	 * Enables a consistent global mapping of blank node identifiers without
	 * using a map, but concatenating this as a prefix for the blank node
	 * identifiers supplied by the parser.
	 */
	private String nextBNodePrefix;

	/**
	 * Mapping from namespace prefixes to namespace names.
	 */
	private Map<String, String> namespaceTable;

	/**
	 * A collection of configuration options for this parser.
	 */
	private ParserConfig parserConfig;

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
	 * Creates a new RDFParserBase that will use the supplied ValueFactory to
	 * create RDF model objects.
	 * 
	 * @param valueFactory
	 *        A ValueFactory.
	 */
	public RDFParserBase(ValueFactory valueFactory) {
		namespaceTable = new HashMap<String, String>(16);
		nextBNodePrefix = UUID.randomUUID().toString();

		setValueFactory(valueFactory);
		setParserConfig(new ParserConfig());
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public void setRDFHandler(RDFHandler handler) {
		rdfHandler = handler;
	}

	public RDFHandler getRDFHandler() {
		return rdfHandler;
	}

	@Override
	public void setParseErrorListener(ParseErrorListener el) {
		errListener = el;
	}

	public ParseErrorListener getParseErrorListener() {
		return errListener;
	}

	@Override
	public void setParseLocationListener(ParseLocationListener el) {
		locationListener = el;
	}

	public ParseLocationListener getParseLocationListener() {
		return locationListener;
	}

	@Override
	public void setParserConfig(ParserConfig config) {
		this.parserConfig = config;
	}

	@Override
	public ParserConfig getParserConfig() {
		return this.parserConfig;
	}

	/*
	 * Default implementation, specific parsers are encouraged to override this method as necessary.
	 */
	@Override
	public Collection<RioSetting<?>> getSupportedSettings() {
		Collection<RioSetting<?>> result = new HashSet<RioSetting<?>>();

		// Supported in RDFParserHelper.createLiteral
		result.add(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
		result.add(BasicParserSettings.VERIFY_DATATYPE_VALUES);
		result.add(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
		result.add(BasicParserSettings.DATATYPE_HANDLERS);

		// Supported in RDFParserHelper.createLiteral
		result.add(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES);
		result.add(BasicParserSettings.VERIFY_LANGUAGE_TAGS);
		result.add(BasicParserSettings.NORMALIZE_LANGUAGE_TAGS);
		result.add(BasicParserSettings.LANGUAGE_HANDLERS);

		// Supported in RDFParserBase.resolveURI
		result.add(BasicParserSettings.VERIFY_RELATIVE_URIS);
		
		// Supported in RDFParserBase.createBNode(String)
		result.add(BasicParserSettings.PRESERVE_BNODE_IDS);
		
		// Supported in RDFParserBase.getNamespace
		result.add(RDFaParserSettings.FAIL_ON_RDFA_UNDEFINED_PREFIXES);
		
		return result;
	}

	@Override
	public void setVerifyData(boolean verifyData) {
		this.parserConfig.set(BasicParserSettings.VERIFY_RELATIVE_URIS, verifyData);
	}

	/**
	 * @deprecated Use specific settings instead.
	 */
	@Deprecated
	public boolean verifyData() {
		return this.parserConfig.verifyData();
	}

	@Override
	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		this.parserConfig.set(BasicParserSettings.PRESERVE_BNODE_IDS, preserveBNodeIDs);
	}

	public boolean preserveBNodeIDs() {
		return this.parserConfig.get(BasicParserSettings.PRESERVE_BNODE_IDS);
	}

	@Deprecated
	@Override
	public void setStopAtFirstError(boolean stopAtFirstError) {
		getParserConfig().set(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES, stopAtFirstError);
		if (!stopAtFirstError) {
			getParserConfig().addNonFatalError(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
		}
		else {
			// TODO: Add a ParserConfig.removeNonFatalError function to avoid this
			Set<RioSetting<?>> set = new HashSet<RioSetting<?>>(getParserConfig().getNonFatalErrors());
			set.remove(NTriplesParserSettings.FAIL_ON_NTRIPLES_INVALID_LINES);
			getParserConfig().setNonFatalErrors(set);
		}
	}

	/**
	 * @deprecated Check specific settings instead.
	 */
	@Deprecated
	public boolean stopAtFirstError() {
		return this.parserConfig.stopAtFirstError();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setDatatypeHandling(DatatypeHandling datatypeHandling) {
		if (datatypeHandling == DatatypeHandling.VERIFY) {
			this.parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, true);
			this.parserConfig.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, true);
		}
		else if (datatypeHandling == DatatypeHandling.NORMALIZE) {
			this.parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, true);
			this.parserConfig.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, true);
			this.parserConfig.set(BasicParserSettings.NORMALIZE_DATATYPE_VALUES, true);
		}
		else {
			// Only ignore if they have not explicitly set any of the relevant
			// settings before this point
			if (!this.parserConfig.isSet(BasicParserSettings.NORMALIZE_DATATYPE_VALUES)
					&& !this.parserConfig.isSet(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES)
					&& !this.parserConfig.isSet(BasicParserSettings.NORMALIZE_DATATYPE_VALUES))
			{
				this.parserConfig.set(BasicParserSettings.VERIFY_DATATYPE_VALUES, false);
				this.parserConfig.set(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, false);
				this.parserConfig.set(BasicParserSettings.NORMALIZE_DATATYPE_VALUES, false);
			}
		}
	}

	/**
	 * @deprecated Use {@link BasicParserSettings#VERIFY_DATATYPE_VALUES} and
	 *             {@link BasicParserSettings#FAIL_ON_UNKNOWN_DATATYPES} and
	 *             {@link BasicParserSettings#NORMALIZE_DATATYPE_VALUES} instead.
	 */
	@Deprecated
	public DatatypeHandling datatypeHandling() {
		return this.parserConfig.datatypeHandling();
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
	 * Gets the namespace that is associated with the specified prefix or throws
	 * an {@link RDFParseException}.
	 * 
	 * @throws RDFParseException
	 *         if no namespace is associated with this prefix
	 */
	protected String getNamespace(String prefix)
		throws RDFParseException
	{
		if (namespaceTable.containsKey(prefix))
			return namespaceTable.get(prefix);
		String msg = "Namespace prefix '" + prefix + "' used but not defined";
		if (defaultPrefix.containsKey(prefix)) {
			reportError(msg, RDFaParserSettings.FAIL_ON_RDFA_UNDEFINED_PREFIXES);
			return defaultPrefix.get(prefix);
		}
		else if ("".equals(prefix)) {
			msg = "Default namespace used but not defined";
		}
		reportFatalError(msg);
		throw new RDFParseException(msg);
	}

	/**
	 * Clears any information that has been collected while parsing. This method
	 * must be called by subclasses when finishing the parse process.
	 */
	protected void clear() {
		baseURI = null;
		nextBNodePrefix = UUID.randomUUID().toString();
		namespaceTable.clear();
	}

	/**
	 * Clears the map that keeps track of blank nodes that have been parsed.
	 * Normally, this map is clear when the document has been parsed completely,
	 * but subclasses can clear the map at other moments too, for example when a
	 * bnode scope ends.
	 * 
	 * @deprecated Map is no longer used.
	 */
	@Deprecated
	protected void clearBNodeIDMap() {
	}

	/**
	 * Resolves a URI-string against the base URI and creates a {@link URI}
	 * object for it.
	 */
	protected URI resolveURI(String uriSpec)
		throws RDFParseException
	{
		// Resolve relative URIs against base URI
		ParsedURI uri = new ParsedURI(uriSpec);

		if (uri.isRelative()) {
			if (baseURI == null) {
				reportFatalError("Unable to resolve URIs, no base URI has been set");
			}

			if (getParserConfig().get(BasicParserSettings.VERIFY_RELATIVE_URIS)) {
				if (uri.isRelative() && !uri.isSelfReference() && baseURI.isOpaque()) {
					reportError("Relative URI '" + uriSpec + "' cannot be resolved using the opaque base URI '"
							+ baseURI + "'", BasicParserSettings.VERIFY_RELATIVE_URIS);
				}
			}

			uri = baseURI.resolve(uri);
		}

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
		// If we are preserving blank node ids then we do not prefix them to make
		// them globally unique
		if (preserveBNodeIDs()) {
			return valueFactory.createBNode(nodeID);
		}
		else {
			// Prefix the node ID with a unique UUID prefix to reduce
			// cross-document clashes
			// This is consistent as long as nextBNodePrefix is not modified
			// between parser runs
			return valueFactory.createBNode(nextBNodePrefix + nodeID);
		}
	}

	/**
	 * Creates a {@link Literal} object with the supplied parameters.
	 */
	protected Literal createLiteral(String label, String lang, URI datatype)
		throws RDFParseException
	{
		return RDFParserHelper.createLiteral(label, lang, datatype, getParserConfig(), getParseErrorListener(),
				valueFactory);
	}

	/**
	 * Creates a {@link Literal} object with the supplied parameters, using the
	 * lineNo and columnNo to enhance error messages or exceptions that may be
	 * generated during the creation of the literal.
	 * 
	 * @since 2.7.4
	 * @see org.openrdf.rio.helpers.RDFParserHelper#createLiteral(String, String,
	 *      URI, ParserConfig, ParseErrorListener, ValueFactory, int, int)
	 */
	protected Literal createLiteral(String label, String lang, URI datatype, int lineNo, int columnNo)
		throws RDFParseException
	{
		return RDFParserHelper.createLiteral(label, lang, datatype, getParserConfig(), getParseErrorListener(),
				valueFactory, lineNo, columnNo);
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
	 * Reports an error with associated line- and column number to the registered
	 * ParseErrorListener, if the given setting has been set to true.
	 * <p>
	 * This method also throws an {@link RDFParseException} when the given
	 * setting has been set to <tt>true</tt> and it is not a nonFatalError.
	 * 
	 * @param msg
	 *        The message to use for
	 *        {@link ParseErrorListener#error(String, int, int)} and for
	 *        {@link RDFParseException#RDFParseException(String, int, int)}.
	 * @param relevantSetting
	 *        The boolean setting that will be checked to determine if this is an
	 *        issue that we need to look at at all. If this setting is true, then
	 *        the error listener will receive the error, and if
	 *        {@link ParserConfig#isNonFatalError(RioSetting)} returns true an
	 *        exception will be thrown.
	 * @throws RDFParseException
	 *         If {@link ParserConfig#get(RioSetting)} returns true, and
	 *         {@link ParserConfig#isNonFatalError(RioSetting)} returns true for
	 *         the given setting.
	 */
	protected void reportError(String msg, RioSetting<Boolean> relevantSetting)
		throws RDFParseException
	{
		RDFParserHelper.reportError(msg, relevantSetting, getParserConfig(), getParseErrorListener());
	}

	/**
	 * Reports an error with associated line- and column number to the registered
	 * ParseErrorListener, if the given setting has been set to true.
	 * <p>
	 * This method also throws an {@link RDFParseException} when the given
	 * setting has been set to <tt>true</tt> and it is not a nonFatalError.
	 * 
	 * @param msg
	 *        The message to use for
	 *        {@link ParseErrorListener#error(String, int, int)} and for
	 *        {@link RDFParseException#RDFParseException(String, int, int)}.
	 * @param lineNo
	 *        Optional line number, should default to setting this as -1 if not
	 *        known. Used for {@link ParseErrorListener#error(String, int, int)}
	 *        and for
	 *        {@link RDFParseException#RDFParseException(String, int, int)}.
	 * @param columnNo
	 *        Optional column number, should default to setting this as -1 if not
	 *        known. Used for {@link ParseErrorListener#error(String, int, int)}
	 *        and for
	 *        {@link RDFParseException#RDFParseException(String, int, int)}.
	 * @param relevantSetting
	 *        The boolean setting that will be checked to determine if this is an
	 *        issue that we need to look at at all. If this setting is true, then
	 *        the error listener will receive the error, and if
	 *        {@link ParserConfig#isNonFatalError(RioSetting)} returns true an
	 *        exception will be thrown.
	 * @throws RDFParseException
	 *         If {@link ParserConfig#get(RioSetting)} returns true, and
	 *         {@link ParserConfig#isNonFatalError(RioSetting)} returns true for
	 *         the given setting.
	 */
	protected void reportError(String msg, int lineNo, int columnNo, RioSetting<Boolean> relevantSetting)
		throws RDFParseException
	{
		RDFParserHelper.reportError(msg, lineNo, columnNo, relevantSetting, getParserConfig(),
				getParseErrorListener());
	}

	/**
	 * Reports an error with associated line- and column number to the registered
	 * ParseErrorListener, if the given setting has been set to true.
	 * <p>
	 * This method also throws an {@link RDFParseException} when the given
	 * setting has been set to <tt>true</tt> and it is not a nonFatalError.
	 * 
	 * @param msg
	 *        The message to use for
	 *        {@link ParseErrorListener#error(String, int, int)} and for
	 *        {@link RDFParseException#RDFParseException(String, int, int)}.
	 * @param lineNo
	 *        Optional line number, should default to setting this as -1 if not
	 *        known. Used for {@link ParseErrorListener#error(String, int, int)}
	 *        and for
	 *        {@link RDFParseException#RDFParseException(String, int, int)}.
	 * @param columnNo
	 *        Optional column number, should default to setting this as -1 if not
	 *        known. Used for {@link ParseErrorListener#error(String, int, int)}
	 *        and for
	 *        {@link RDFParseException#RDFParseException(String, int, int)}.
	 * @param relevantSetting
	 *        The boolean setting that will be checked to determine if this is an
	 *        issue that we need to look at at all. If this setting is true, then
	 *        the error listener will receive the error, and if
	 *        {@link ParserConfig#isNonFatalError(RioSetting)} returns true an
	 *        exception will be thrown.
	 * @throws RDFParseException
	 *         If {@link ParserConfig#get(RioSetting)} returns true, and
	 *         {@link ParserConfig#isNonFatalError(RioSetting)} returns true for
	 *         the given setting.
	 */
	protected void reportError(Exception e, int lineNo, int columnNo, RioSetting<Boolean> relevantSetting)
		throws RDFParseException
	{
		RDFParserHelper.reportError(e, lineNo, columnNo, relevantSetting, getParserConfig(),
				getParseErrorListener());
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
		RDFParserHelper.reportFatalError(msg, getParseErrorListener());
	}

	/**
	 * Reports a fatal error with associated line- and column number to the
	 * registered ParseErrorListener, if any, and throws a
	 * <tt>ParseException</tt> afterwards.
	 */
	protected void reportFatalError(String msg, int lineNo, int columnNo)
		throws RDFParseException
	{
		RDFParserHelper.reportFatalError(msg, lineNo, columnNo, getParseErrorListener());
	}

	/**
	 * Reports a fatal error to the registered ParseErrorListener, if any, and
	 * throws a <tt>ParseException</tt> afterwards. An exception is made for the
	 * case where the supplied exception is a {@link RDFParseException}; in that
	 * case the supplied exception is not wrapped in another ParseException and
	 * the error message is not reported to the ParseErrorListener, assuming that
	 * it has already been reported when the original ParseException was thrown.
	 * <p>
	 * This method simply calls {@link #reportFatalError(Exception,int,int)}
	 * supplying <tt>-1</tt> for the line- and column number.
	 */
	protected void reportFatalError(Exception e)
		throws RDFParseException
	{
		RDFParserHelper.reportFatalError(e, getParseErrorListener());
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
		RDFParserHelper.reportFatalError(e, lineNo, columnNo, getParseErrorListener());
	}

}
