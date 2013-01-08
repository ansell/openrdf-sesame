/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

/**
 * Identifies a parser setting along with its default value.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.7.0
 */
public interface ParserSetting<T extends Object> {

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
