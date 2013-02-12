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
package org.openrdf.rio;

import java.io.Serializable;

/**
 * Identifies a parser setting along with its default value.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public interface ParserSetting<T extends Object> extends Serializable {

	/**
	 * A unique key for this parser setting.
	 * 
	 * @return A unique key identifying this parser setting.
	 */
	String getKey();

	/**
	 * The human readable name for this parser setting
	 * 
	 * @return The name for this parser setting.
	 */
	String getDescription();
	
	/**
	 * Returns the default value for this parser setting if it is not set by a
	 * user.
	 * 
	 * @return The default value for this parser setting.
	 */
	T getDefaultValue();
}
