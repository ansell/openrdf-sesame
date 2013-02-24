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
 * A class encapsulating the basic writer settings that most writers may
 * support.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class BasicWriterSettings {

	/**
	 * Boolean setting for writer to determine whether pretty printing is preferred. <br>
	 * Defaults to true.
	 */
	public static final RioSetting<Boolean> PRETTY_PRINT = new RioSettingImpl<Boolean>(
			"org.openrdf.rio.prettyprint", "Pretty print", Boolean.TRUE);

	/**
	 * Private default constructor.
	 */
	private BasicWriterSettings() {
	}

}
