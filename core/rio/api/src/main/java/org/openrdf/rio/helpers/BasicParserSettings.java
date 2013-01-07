/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import org.openrdf.rio.ParserSetting;
import org.openrdf.rio.RDFParser.DatatypeHandling;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class BasicParserSettings {

	/**
	 * Boolean setting for parser to determine whether data values are verified. <br>
	 * Defaults to true.
	 */
	public static final ParserSetting<Boolean> VERIFY_DATA = new ParserSettingImpl<Boolean>("Verify data",
			Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether parser should stop at
	 * first error or continue if that is possible. If the parser is unable to
	 * continue after an error it will still fail regardless of this setting.<br>
	 * Defaults to true.
	 */
	public static final ParserSetting<Boolean> STOP_AT_FIRST_ERROR = new ParserSettingImpl<Boolean>(
			"Stop at first error", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether parser should attempt to
	 * preserve identifiers for blank nodes. If the blank node did not have an
	 * identifier in the document a new identifier will be generated for it.<br>
	 * Defaults to false.
	 */
	public static final ParserSetting<Boolean> PRESERVE_BNODE_IDS = new ParserSettingImpl<Boolean>(
			"Preserve blank node identifiers", Boolean.FALSE);

	/**
	 * Determines which mode of {@link DatatypeHandling} will be used by the parser.<br>
	 * Defaults to {@link DatatypeHandling#VERIFY}.
	 */
	public static final ParserSetting<DatatypeHandling> DATATYPE_HANDLING = new ParserSettingImpl<DatatypeHandling>(
			"Datatype handling", DatatypeHandling.VERIFY);

	/**
	 * Private default constructor.
	 */
	private BasicParserSettings() {
	}

}
