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
import java.util.List;

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

	private final static Logger log = LoggerFactory.getLogger(BasicParserSettings.class);

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

	static {
		List<DatatypeHandler> defaultDatatypeHandlers = new ArrayList<DatatypeHandler>(4);
		try {
			DatatypeHandlerRegistry registry = DatatypeHandlerRegistry.getInstance();
			for (String nextHandler : Arrays.asList(DatatypeHandler.XMLSCHEMA, DatatypeHandler.RDFDATATYPES,
					DatatypeHandler.DBPEDIA, DatatypeHandler.VIRTUOSOGEOMETRY, DatatypeHandler.GEOSPARQL))
			{
				DatatypeHandler nextdt = registry.get(nextHandler);
				if (nextdt != null) {
					defaultDatatypeHandlers.add(nextdt);
				}
				else {
					log.warn("Could not find DatatypeHandler : {}", nextHandler);
				}
			}
		}
		catch (Exception e) {
			// Ignore exceptions so that service loading failures do not cause
			// class initialization errors.
			log.warn("Found an error loading DatatypeHandler services", e);
		}

		DATATYPE_HANDLERS = new RioSettingImpl<List<DatatypeHandler>>("org.openrdf.rio.datatypehandlers",
				"Datatype Handlers", defaultDatatypeHandlers);

		List<LanguageHandler> defaultLanguageHandlers = new ArrayList<LanguageHandler>(1);
		try {
			LanguageHandlerRegistry registry = LanguageHandlerRegistry.getInstance();
			for (String nextHandler : Arrays.asList(LanguageHandler.RFC3066)) {
				LanguageHandler nextlang = registry.get(nextHandler);
				if (nextlang != null) {
					defaultLanguageHandlers.add(nextlang);
				}
				else {
					log.warn("Could not find LanguageHandler : {}", nextHandler);
				}
			}
		}
		catch (Exception e) {
			// Ignore exceptions so that service loading failures do not cause
			// class initialization errors.
			log.warn("Found an error loading LanguageHandler services", e);
		}

		LANGUAGE_HANDLERS = new RioSettingImpl<List<LanguageHandler>>("org.openrdf.rio.languagehandlers",
				"Language Handlers", defaultLanguageHandlers);
	}

	/**
	 * Private default constructor.
	 */
	private BasicParserSettings() {
	}

}
