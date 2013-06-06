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

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.util.LiteralUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.DatatypeHandler;
import org.openrdf.rio.LanguageHandler;
import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RioSetting;

/**
 * Helper methods that may be used by {@link RDFParser} implementations.
 * <p>
 * This class contains reference implementations of the workflows for
 * {@link ParseErrorListener}, {@link RDFParseException}, {@link ParserConfig},
 * {@link DatatypeHandler} and {@link LanguageHandler} related methods
 * 
 * @author Peter Ansell
 * @since 2.7.1
 */
public class RDFParserHelper {

	/**
	 * Create a literal using the given parameters, including iterative
	 * verification and normalization by any {@link DatatypeHandler} or
	 * {@link LanguageHandler} implementations that are found in the
	 * {@link ParserConfig}.
	 * 
	 * @param label
	 *        The value for {@link Literal#getLabel()}, which may be iteratively
	 *        normalized.
	 * @param lang
	 *        If this is not null, and the datatype is either not null, or is
	 *        equal to {@link RDF#LANGSTRING}, then a language literal will be
	 *        created.
	 * @param datatype
	 *        If datatype is not null, and the datatype is not equal to
	 *        {@link RDF#LANGSTRING} with a non-null lang, then a datatype
	 *        literal will be created.
	 * @param parserConfig
	 *        The source of parser settings, including the desired list of
	 *        {@link DatatypeHandler} and {@link LanguageHandler}s to use for
	 *        verification and normalization of datatype and language literals
	 *        respectively.
	 * @param errListener
	 *        The {@link ParseErrorListener} to use for signalling errors. This
	 *        will be called if a setting is enabled by setting it to true in the
	 *        {@link ParserConfig}, after which the error may trigger an
	 *        {@link RDFParseException} if the setting is not present in
	 *        {@link ParserConfig#getNonFatalErrors()}.
	 * @param valueFactory
	 *        The {@link ValueFactory} to use for creating new {@link Literal}s
	 *        using this method.
	 * @return A {@link Literal} created based on the given parameters.
	 * @throws RDFParseException
	 *         If there was an error during the process that could not be
	 *         recovered from, based on settings in the given parser config.
	 * @since 2.7.1
	 */
	public static final Literal createLiteral(String label, String lang, URI datatype,
			ParserConfig parserConfig, ParseErrorListener errListener, ValueFactory valueFactory)
		throws RDFParseException
	{
		if (label == null) {
			throw new NullPointerException("Cannot create a literal using a null label");
		}

		Literal result = null;
		String workingLabel = label;
		String workingLang = lang;
		URI workingDatatype = datatype;

		if(workingLang == null && RDF.LANGSTRING.equals(workingDatatype)) {
			reportError("'" + workingLabel + "' had rdf:langString datatype, but no language", BasicWriterSettings.RDF_LANGSTRING_TO_LANG_LITERAL, parserConfig, errListener);
			// If they signalled that they wanted to support mapping of rdf:langString to language literal, then attempt to go on with an empty language
			workingLang = "";
			workingDatatype = null;
		}
		
		// In RDF-1.1 we must do lang check first as language literals will all
		// have datatype RDF.LANGSTRING, but only language literals would have a
		// non-null lang
		if (workingLang != null && (workingDatatype == null || RDF.LANGSTRING.equals(workingDatatype))) {
			if (parserConfig.get(BasicParserSettings.VERIFY_LANGUAGE_TAGS)) {
				boolean recognisedLanguage = false;
				for (LanguageHandler nextHandler : parserConfig.get(BasicParserSettings.LANGUAGE_HANDLERS)) {
					if (nextHandler.isRecognizedLanguage(workingLang)) {
						recognisedLanguage = true;
						try {
							if (!nextHandler.verifyLanguage(workingLabel, workingLang)) {
								reportError("'" + lang + "' is not a valid language tag ",
										BasicParserSettings.VERIFY_LANGUAGE_TAGS, parserConfig, errListener);
							}
						}
						catch (LiteralUtilException e) {
							reportError("'" + label
									+ " could not be verified by a language handler that recognised it. language was "
									+ lang, BasicParserSettings.VERIFY_LANGUAGE_TAGS, parserConfig, errListener);
						}
						if (parserConfig.get(BasicParserSettings.NORMALIZE_LANGUAGE_TAGS)) {
							try {
								result = nextHandler.normalizeLanguage(workingLabel, workingLang, valueFactory);
								workingLabel = result.getLabel();
								workingLang = result.getLanguage();
								workingDatatype = result.getDatatype();
							}
							catch (LiteralUtilException e) {
								reportError("'" + label + "' did not have a valid value for language " + lang + ": "
										+ e.getMessage() + " and could not be normalised",
										BasicParserSettings.NORMALIZE_LANGUAGE_TAGS, parserConfig, errListener);
							}
						}
					}
				}
				if (!recognisedLanguage) {
					reportError(
							"'"
									+ label
									+ "' was not recognised as a language literal, and could not be verified, with language "
									+ lang, BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES, parserConfig, errListener);
				}
			}

		}
		else if (workingDatatype != null) {
			if (parserConfig.get(BasicParserSettings.VERIFY_DATATYPE_VALUES)) {
				boolean recognisedDatatype = false;
				for (DatatypeHandler nextHandler : parserConfig.get(BasicParserSettings.DATATYPE_HANDLERS)) {
					if (nextHandler.isRecognizedDatatype(workingDatatype)) {
						recognisedDatatype = true;
						try {
							if (!nextHandler.verifyDatatype(workingLabel, workingDatatype)) {
								reportError("'" + label + "' is not a valid value for datatype " + datatype,
										BasicParserSettings.VERIFY_DATATYPE_VALUES, parserConfig, errListener);
							}
						}
						catch (LiteralUtilException e) {
							reportError("'" + label
									+ " could not be verified by a datatype handler that recognised it. datatype was "
									+ datatype, BasicParserSettings.VERIFY_DATATYPE_VALUES, parserConfig, errListener);
						}
						if (parserConfig.get(BasicParserSettings.NORMALIZE_DATATYPE_VALUES)) {
							try {
								result = nextHandler.normalizeDatatype(workingLabel, workingDatatype, valueFactory);
								workingLabel = result.getLabel();
								workingLang = result.getLanguage();
								workingDatatype = result.getDatatype();
							}
							catch (LiteralUtilException e) {
								reportError("'" + label + "' is not a valid value for datatype " + datatype + ": "
										+ e.getMessage() + " and could not be normalised",
										BasicParserSettings.NORMALIZE_DATATYPE_VALUES, parserConfig, errListener);
							}
						}
					}
				}

				if (!recognisedDatatype) {
					reportError("'" + label + "' was not recognised, and could not be verified, with datatype "
							+ datatype, BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES, parserConfig, errListener);
				}
			}

		}

		if (result == null) {
			try {
				// Backup for unnormalised language literal creation
				if (workingLang != null && (workingDatatype == null || RDF.LANGSTRING.equals(workingDatatype))) {
					result = valueFactory.createLiteral(workingLabel, workingLang);
				}
				// Backup for unnormalised datatype literal creation
				else if (workingDatatype != null) {
					result = valueFactory.createLiteral(workingLabel, workingDatatype);
				}
				else {
					result = valueFactory.createLiteral(workingLabel);
				}
			}
			catch (Exception e) {
				reportFatalError(e, errListener);
			}
		}

		return result;
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
	 * @param parserConfig
	 *        The {@link ParserConfig} to use for determining if the error is
	 *        first sent to the ParseErrorListener, and whether it is then also
	 *        non-fatal to avoid throwing an {@link RDFParseException}.
	 * @param errListener
	 *        The {@link ParseErrorListener} that will be sent messages about
	 *        errors that are enabled.
	 * @throws RDFParseException
	 *         If {@link ParserConfig#get(RioSetting)} returns true, and
	 *         {@link ParserConfig#isNonFatalError(RioSetting)} returns true for
	 *         the given setting.
	 * @since 2.7.1
	 */
	public static void reportError(String msg, RioSetting<Boolean> relevantSetting, ParserConfig parserConfig,
			ParseErrorListener errListener)
		throws RDFParseException
	{
		reportError(msg, -1, -1, relevantSetting, parserConfig, errListener);
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
	 * @param parserConfig
	 *        The {@link ParserConfig} to use for determining if the error is
	 *        first sent to the ParseErrorListener, and whether it is then also
	 *        non-fatal to avoid throwing an {@link RDFParseException}.
	 * @param errListener
	 *        The {@link ParseErrorListener} that will be sent messages about
	 *        errors that are enabled.
	 * @throws RDFParseException
	 *         If {@link ParserConfig#get(RioSetting)} returns true, and
	 *         {@link ParserConfig#isNonFatalError(RioSetting)} returns true for
	 *         the given setting.
	 * @since 2.7.1
	 */
	public static void reportError(String msg, int lineNo, int columnNo, RioSetting<Boolean> relevantSetting,
			ParserConfig parserConfig, ParseErrorListener errListener)
		throws RDFParseException
	{
		if (parserConfig.get(relevantSetting)) {
			if (errListener != null) {
				errListener.error(msg, lineNo, columnNo);
			}

			if (!parserConfig.isNonFatalError(relevantSetting)) {
				throw new RDFParseException(msg, lineNo, columnNo);
			}
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
	 * @param parserConfig
	 *        The {@link ParserConfig} to use for determining if the error is
	 *        first sent to the ParseErrorListener, and whether it is then also
	 *        non-fatal to avoid throwing an {@link RDFParseException}.
	 * @param errListener
	 *        The {@link ParseErrorListener} that will be sent messages about
	 *        errors that are enabled.
	 * @throws RDFParseException
	 *         If {@link ParserConfig#get(RioSetting)} returns true, and
	 *         {@link ParserConfig#isNonFatalError(RioSetting)} returns true for
	 *         the given setting.
	 * @since 2.7.1
	 */
	public static void reportError(Exception e, int lineNo, int columnNo, RioSetting<Boolean> relevantSetting,
			ParserConfig parserConfig, ParseErrorListener errListener)
		throws RDFParseException
	{
		if (parserConfig.get(relevantSetting)) {
			if (errListener != null) {
				errListener.error(e.getMessage(), lineNo, columnNo);
			}

			if (!parserConfig.isNonFatalError(relevantSetting)) {
				if (e instanceof RDFParseException) {
					throw (RDFParseException)e;
				}
				else {
					throw new RDFParseException(e, lineNo, columnNo);
				}
			}
		}
	}

	/**
	 * Reports a fatal error to the registered ParseErrorListener, if any, and
	 * throws a <tt>ParseException</tt> afterwards. This method simply calls
	 * {@link #reportFatalError(String,int,int)} supplying <tt>-1</tt> for the
	 * line- and column number.
	 * 
	 * @since 2.7.1
	 */
	public static void reportFatalError(String msg, ParseErrorListener errListener)
		throws RDFParseException
	{
		reportFatalError(msg, -1, -1, errListener);
	}

	/**
	 * Reports a fatal error with associated line- and column number to the
	 * registered ParseErrorListener, if any, and throws a
	 * <tt>ParseException</tt> afterwards.
	 * 
	 * @since 2.7.1
	 */
	public static void reportFatalError(String msg, int lineNo, int columnNo, ParseErrorListener errListener)
		throws RDFParseException
	{
		if (errListener != null) {
			errListener.fatalError(msg, lineNo, columnNo);
		}

		throw new RDFParseException(msg, lineNo, columnNo);
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
	 * 
	 * @since 2.7.1
	 */
	public static void reportFatalError(Exception e, ParseErrorListener errListener)
		throws RDFParseException
	{
		reportFatalError(e, -1, -1, errListener);
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
	 * 
	 * @since 2.7.1
	 */
	public static void reportFatalError(Exception e, int lineNo, int columnNo, ParseErrorListener errListener)
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

	/**
	 * Protected constructor to prevent direct instantiation.
	 */
	protected RDFParserHelper() {
	}
}
