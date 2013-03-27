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

/**
 * An enumeration used to define constants used with the
 * {@link BasicParserSettings#LARGE_LITERALS_HANDLING} parser setting.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public enum LiteralHandling {

	/**
	 * Indicates that large literals should be preserved. This is the default
	 * behaviour.
	 */
	PRESERVE,

	/**
	 * Indicates that statements containing large literals should be dropped,
	 * based on based on the {@link BasicParserSettings#LARGE_LITERALS_LIMIT}
	 * setting.
	 */
	DROP,

	/**
	 * Indicates that values of large literals should be truncated, based on the
	 * {@link BasicParserSettings#LARGE_LITERALS_LIMIT} setting.
	 */
	TRUNCATE

}
