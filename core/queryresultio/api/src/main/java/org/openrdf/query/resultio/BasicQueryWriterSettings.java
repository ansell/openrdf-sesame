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
package org.openrdf.query.resultio;

import org.openrdf.rio.RioSetting;
import org.openrdf.rio.helpers.RioSettingImpl;

/**
 * {@link RioSetting} constants to use with {@link QueryResultWriter}s.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class BasicQueryWriterSettings {

	/**
	 * Specifies whether the writer should add the proprietary
	 * "http://www.openrdf.org/schema/qname#qname" annotations to output.
	 * <p>
	 * Defaults to false.
	 * 
	 * @since 2.7.0
	 */
	public final static RioSetting<Boolean> ADD_SESAME_QNAME = new RioSettingImpl<Boolean>(
			"org.openrdf.query.resultio.addsesameqname", "Add Sesame QName", false);

	/**
	 * Private default constructor
	 */
	private BasicQueryWriterSettings() {
	}

}
