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

import org.openrdf.rio.RioSetting;
import org.openrdf.rio.RDFParser.DatatypeHandling;

/**
 * A class encapsulating the basic parser settings that most parsers may
 * support.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class BasicParserSettings {

	/**
	 * Boolean setting for parser to determine whether data values are verified. <br>
	 * Defaults to true.
	 */
	public static final RioSetting<Boolean> VERIFY_DATA = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.verifydata", "Verify data", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether parser should stop at
	 * first error or continue if that is possible. If the parser is unable to
	 * continue after an error it will still fail regardless of this setting.<br>
	 * Defaults to true.
	 */
	public static final RioSetting<Boolean> STOP_AT_FIRST_ERROR = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.stopatfirsterror", "Stop at first error", Boolean.TRUE);

	/**
	 * Boolean setting for parser to determine whether parser should attempt to
	 * preserve identifiers for blank nodes. If the blank node did not have an
	 * identifier in the document a new identifier will be generated for it.<br>
	 * Defaults to false.
	 */
	public static final RioSetting<Boolean> PRESERVE_BNODE_IDS = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.preservebnodeids", "Preserve blank node identifiers", Boolean.FALSE);

	/**
	 * Determines which mode of {@link DatatypeHandling} will be used by the
	 * parser.<br>
	 * Defaults to {@link DatatypeHandling#VERIFY}.
	 */
	public static final RioSetting<DatatypeHandling> DATATYPE_HANDLING = new RioSettingImpl<DatatypeHandling>(
			"org.openrdf.rio.datatypehandling", "Datatype handling", DatatypeHandling.VERIFY);

	/**
	 * Private default constructor.
	 */
	private BasicParserSettings() {
	}

}
