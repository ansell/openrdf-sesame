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

/**
 * ParserSettings for the N-Triples parser features.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class NTriplesParserSettings {

	/**
	 * Boolean setting for parser to determine whether syntactically invalid
	 * lines in N-Triples and N-Quads documents generate a parse error.
	 * <p>
	 * Defaults to true.
	 * 
	 * @since 2.7.0
	 */
	public static final RioSetting<Boolean> FAIL_ON_NTRIPLES_INVALID_LINES = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.failonntriplesinvalidlines", "Fail on N-Triples invalid lines", Boolean.TRUE);

	/**
	 * Private constructor
	 */
	private NTriplesParserSettings() {
	}

}
