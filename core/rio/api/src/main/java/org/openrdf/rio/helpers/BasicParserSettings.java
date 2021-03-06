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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.SimpleNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.rio.DatatypeHandler;
import org.openrdf.rio.DatatypeHandlerRegistry;
import org.openrdf.rio.LanguageHandler;
import org.openrdf.rio.LanguageHandlerRegistry;
import org.openrdf.rio.RioSetting;

/**
 * A class encapsulating the basic parser settings that most parsers may
 * support.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class BasicParserSettings {

	/**
	 * Vocabulary Prefixes of W3C Documents (Recommendations or Notes)
	 *
	 * @see http://www.w3.org/2011/rdfa-context/rdfa-1.1
	 */
	private static final Set<Namespace> _DEFAULT_PREFIX;
	static {
		Set<Namespace> aNamespaces = new HashSet<Namespace>();

		aNamespaces.add(new SimpleNamespace("cat", "http://www.w3.org/ns/dcat#"));
		aNamespaces.add(new SimpleNamespace("qb", "http://purl.org/linked-data/cube#"));
		aNamespaces.add(new SimpleNamespace("grddl", "http://www.w3.org/2003/g/data-view#"));
		aNamespaces.add(new SimpleNamespace("ma", "http://www.w3.org/ns/ma-ont#"));
		aNamespaces.add(new SimpleNamespace("owl", "http://www.w3.org/2002/07/owl#"));
		aNamespaces.add(new SimpleNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		aNamespaces.add(new SimpleNamespace("rdfa", "http://www.w3.org/ns/rdfa#"));
		aNamespaces.add(new SimpleNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#"));
		aNamespaces.add(new SimpleNamespace("rif", "http://www.w3.org/2007/rif#"));
		aNamespaces.add(new SimpleNamespace("rr", "http://www.w3.org/ns/r2rml#"));
		aNamespaces.add(new SimpleNamespace("skos", "http://www.w3.org/2004/02/skos/core#"));
		aNamespaces.add(new SimpleNamespace("skosxl", "http://www.w3.org/2008/05/skos-xl#"));
		aNamespaces.add(new SimpleNamespace("wdr", "http://www.w3.org/2007/05/powder#"));
		aNamespaces.add(new SimpleNamespace("void", "http://rdfs.org/ns/void#"));
		aNamespaces.add(new SimpleNamespace("wdrs", "http://www.w3.org/2007/05/powder-s#"));
		aNamespaces.add(new SimpleNamespace("xhv", "http://www.w3.org/1999/xhtml/vocab#"));
		aNamespaces.add(new SimpleNamespace("xml", "http://www.w3.org/XML/1998/namespace"));
		aNamespaces.add(new SimpleNamespace("xsd", "http://www.w3.org/2001/XMLSchema#"));
		aNamespaces.add(new SimpleNamespace("prov", "http://www.w3.org/ns/prov#"));
		aNamespaces.add(new SimpleNamespace("sd", "http://www.w3.org/ns/sparql-service-description#"));
		aNamespaces.add(new SimpleNamespace("org", "http://www.w3.org/ns/org#"));
		aNamespaces.add(new SimpleNamespace("gldp", "http://www.w3.org/ns/people#"));
		aNamespaces.add(new SimpleNamespace("cnt", "http://www.w3.org/2008/content#"));
		aNamespaces.add(new SimpleNamespace("dcat", "http://www.w3.org/ns/dcat#"));
		aNamespaces.add(new SimpleNamespace("earl", "http://www.w3.org/ns/earl#"));
		aNamespaces.add(new SimpleNamespace("ht", "http://www.w3.org/2006/http#"));
		aNamespaces.add(new SimpleNamespace("ptr", "http://www.w3.org/2009/pointers#"));
		aNamespaces.add(new SimpleNamespace("cc", "http://creativecommons.org/ns#"));
		aNamespaces.add(new SimpleNamespace("ctag", "http://commontag.org/ns#"));
		aNamespaces.add(new SimpleNamespace("dc", "http://purl.org/dc/terms/"));
		aNamespaces.add(new SimpleNamespace("dc11", "http://purl.org/dc/elements/1.1/"));
		aNamespaces.add(new SimpleNamespace("dcterms", "http://purl.org/dc/terms/"));
		aNamespaces.add(new SimpleNamespace("foaf", "http://xmlns.com/foaf/0.1/"));
		aNamespaces.add(new SimpleNamespace("gr", "http://purl.org/goodrelations/v1#"));
		aNamespaces.add(new SimpleNamespace("ical", "http://www.w3.org/2002/12/cal/icaltzd#"));
		aNamespaces.add(new SimpleNamespace("og", "http://ogp.me/ns#"));
		aNamespaces.add(new SimpleNamespace("rev", "http://purl.org/stuff/rev#"));
		aNamespaces.add(new SimpleNamespace("sioc", "http://rdfs.org/sioc/ns#"));
		aNamespaces.add(new SimpleNamespace("v", "http://rdf.data-vocabulary.org/#"));
		aNamespaces.add(new SimpleNamespace("vcard", "http://www.w3.org/2006/vcard/ns#"));
		aNamespaces.add(new SimpleNamespace("schema", "http://schema.org/"));

		_DEFAULT_PREFIX = Collections.unmodifiableSet(aNamespaces);
	}

	private final static Logger _LOG = LoggerFactory.getLogger(BasicParserSettings.class);

	/**
	 * Boolean setting for parser to determine whether values for recognised
	 * datatypes are to be verified.
	 * <p>
	 * Verification is performed using registered DatatypeHandlers.
	 * <p>
	 * Defaults to false since 2.8.0, defaulted to true in 2.7.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> VERIFY_DATATYPE_VALUES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.verifydatatypevalues", "Verify recognised datatype values", Boolean.FALSE);

	/**
	 * Boolean setting for parser to determine whether to fail parsing if
	 * datatypes are not recognised.
	 * <p>
	 * Datatypes are recognised based on matching one of the registered
	 * {@link DatatypeHandler}s.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_UNKNOWN_DATATYPES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonunknowndatatypes", "Fail on unknown datatypes", Boolean.FALSE);

	/**
	 * Boolean setting for parser to determine whether recognised datatypes need
	 * to have their values be normalized.
	 * <p>
	 * Normalization is performed using registered DatatypeHandlers.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> NORMALIZE_DATATYPE_VALUES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.normalizedatatypevalues", "Normalize recognised datatype values", Boolean.FALSE);

	/**
	 * Setting used to specify which {@link DatatypeHandler} implementations are
	 * to be used for a given parser configuration.
	 * <p>
	 * Defaults to an XMLSchema DatatypeHandler implementation based on
	 * {@link DatatypeHandler#XMLSCHEMA} and an RDF DatatypeHandler
	 * implementation based on {@link DatatypeHandler#RDFDATATYPES}.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<List<DatatypeHandler>> DATATYPE_HANDLERS;

	/**
	 * Boolean setting for parser to determine whether to fail parsing if
	 * languages are not recognised.
	 * <p>
	 * Languages are recognised based on matching one of the registered
	 * {@link LanguageHandler}s.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_UNKNOWN_LANGUAGES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonunknownlanguages", "Fail on unknown languages", Boolean.FALSE);

	/**
	 * Boolean setting for parser to determine whether languages are to be
	 * verified based on a given set of definitions for valid languages.
	 * <p>
	 * Verification is performed using registered {@link LanguageHandler}s.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> VERIFY_LANGUAGE_TAGS = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.verifylanguagevalues", "Verify language tags", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether languages need to be
	 * normalized, and to which format they should be normalised.
	 * <p>
	 * Normalization is performed using registered {@link LanguageHandler}s.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> NORMALIZE_LANGUAGE_TAGS = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.normalizelanguagevalues", "Normalize recognised language tags", Boolean.FALSE);

	/**
	 * Setting used to specify which {@link LanguageHandler} implementations are
	 * to be used for a given parser configuration.
	 * <p>
	 * Defaults to an RFC3066 LanguageHandler implementation based on
	 * {@link LanguageHandler#RFC3066}.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<List<LanguageHandler>> LANGUAGE_HANDLERS;

	/**
	 * Boolean setting for parser to determine whether relative URIs are
	 * verified.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> VERIFY_RELATIVE_URIS = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.verifyrelativeuris", "Verify relative URIs", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether parser should attempt to
	 * preserve identifiers for blank nodes. If the blank node did not have an
	 * identifier in the document a new identifier will be generated for it.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> PRESERVE_BNODE_IDS = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.preservebnodeids", "Preserve blank node identifiers", Boolean.FALSE);

	/**
	 * Boolean setting for parser to determine whether parser should preserve,
	 * truncate, drop, or otherwise manipulate statements that contain long
	 * literals. The maximum length of literals if this setting is set to
	 * truncate or drop is configured using {@link #LARGE_LITERALS_LIMIT}.
	 * <p>
	 * Defaults to {@link LargeLiteralHandling#PRESERVE}.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<LargeLiteralHandling> LARGE_LITERALS_HANDLING = new RioSettingImpl<LargeLiteralHandling>(
			"org.openrdf.rio.largeliterals", "Large literals handling", LargeLiteralHandling.PRESERVE);

	/**
	 * If {@link #LARGE_LITERALS_HANDLING} is set to
	 * {@link LargeLiteralHandling#PRESERVE}, which it is by default, then the
	 * value of this setting is not used.
	 * <p>
	 * If {@link #LARGE_LITERALS_HANDLING} is set to
	 * {@link LargeLiteralHandling#DROP} , then the value of this setting
	 * corresponds to the maximum number of bytes for a literal before the
	 * statement it is a part of is dropped silently by the parser.
	 * <p>
	 * If {@link #LARGE_LITERALS_HANDLING} is set to
	 * {@link LargeLiteralHandling#TRUNCATE} , then the value of this setting
	 * corresponds to the maximum number of bytes for a literal before the value
	 * is truncated.
	 * <p>
	 * Defaults to 1048576 bytes, which is equivalent to 1 megabyte.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Long> LARGE_LITERALS_LIMIT = new RioSettingImpl<Long>(
			"org.openrdf.rio.largeliteralslimit", "Size limit for large literals", 1048576L);

	/**
	 * <p>
	 * Setting to provide a collection of {@link Namespace} objects which will be
	 * used when parsing RDF as the basis for the default set of namespaces of
	 * the document.
	 * </p>
	 * <p>
	 * Namespaces specified within the RDF document being parsed will override
	 * these defaults
	 * </p>
	 * <p>
	 * Defaults to <a href="http://www.w3.org/2011/rdfa-context/rdfa-1.1">this
	 * list</a>.
	 * </p>
	 *
	 * @since 2.8.5
	 */
	public static final RioSetting<Set<Namespace>> NAMESPACES = new RioSettingImpl<Set<Namespace>>(
			"org.openrdf.rio.namespaces", "Collection of default namespaces to use for parsing", _DEFAULT_PREFIX);

	static {
		List<DatatypeHandler> defaultDatatypeHandlers = new ArrayList<DatatypeHandler>(5);
		try {
			DatatypeHandlerRegistry registry = DatatypeHandlerRegistry.getInstance();
			for (String nextHandler : Arrays.asList(DatatypeHandler.XMLSCHEMA, DatatypeHandler.RDFDATATYPES,
					DatatypeHandler.DBPEDIA, DatatypeHandler.VIRTUOSOGEOMETRY, DatatypeHandler.GEOSPARQL))
			{
				Optional<DatatypeHandler> nextdt = registry.get(nextHandler);
				if (nextdt.isPresent()) {
					defaultDatatypeHandlers.add(nextdt.get());
				}
				else {
					_LOG.warn("Could not find DatatypeHandler : {}", nextHandler);
				}
			}
		}
		catch (Exception e) {
			// Ignore exceptions so that service loading failures do not cause
			// class initialization errors.
			_LOG.warn("Found an error loading DatatypeHandler services", e);
		}

		DATATYPE_HANDLERS = new RioSettingImpl<List<DatatypeHandler>>("org.openrdf.rio.datatypehandlers",
				"Datatype Handlers", Collections.unmodifiableList(defaultDatatypeHandlers));

		List<LanguageHandler> defaultLanguageHandlers = new ArrayList<LanguageHandler>(1);
		try {
			LanguageHandlerRegistry registry = LanguageHandlerRegistry.getInstance();
			for (String nextHandler : Arrays.asList(LanguageHandler.RFC3066)) {
				Optional<LanguageHandler> nextlang = registry.get(nextHandler);
				if (nextlang.isPresent()) {
					defaultLanguageHandlers.add(nextlang.get());
				}
				else {
					_LOG.warn("Could not find LanguageHandler : {}", nextHandler);
				}
			}
		}
		catch (Exception e) {
			// Ignore exceptions so that service loading failures do not cause
			// class initialization errors.
			_LOG.warn("Found an error loading LanguageHandler services", e);
		}

		LANGUAGE_HANDLERS = new RioSettingImpl<List<LanguageHandler>>("org.openrdf.rio.languagehandlers",
				"Language Handlers", Collections.unmodifiableList(defaultLanguageHandlers));
	}

	/**
	 * Private default constructor.
	 */
	private BasicParserSettings() {
	}

}
