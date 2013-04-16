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

import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.RioConfig;
import org.openrdf.rio.RioSetting;

/**
 * ParserSettings for the TriX parser features.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class TriXParserSettings {

	/**
	 * Boolean setting for parser to determine whether missing datatypes in TriX
	 * are ignored and only generate errors into the {@link ParseErrorListener}.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> IGNORE_TRIX_MISSING_DATATYPE = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.ignoretrixmissingdatatype", "Ignore TriX missing datatype", Boolean.FALSE);
	/**
	 * Boolean setting for parser to determine whether invalid statements are
	 * ignored in TriX and only generate errors into the
	 * {@link ParseErrorListener}.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> IGNORE_TRIX_INVALID_STATEMENT = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.ignoretrixmissingdatatype", "Ignore TriX missing datatype", Boolean.FALSE);

	/**
	 * Private constructor
	 */
	private TriXParserSettings() {
	}

}
