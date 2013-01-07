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
 */
public interface ParserSetting<T extends Object> {

	/**
	 * The human readable name for this parser setting
	 * 
	 * @return The name for this parser setting.
	 */
	String getName();

	/**
	 * Returns the default value for this parser setting if it is not set by a
	 * user.
	 * 
	 * @return The default value for this parser setting.
	 */
	T getDefaultValue();
}
