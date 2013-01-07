/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import org.openrdf.rio.ParserSetting;

/**
 * Basic implementation of {@link ParserSetting} interface.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.7.0
 */
public class ParserSettingImpl<T> implements ParserSetting<T> {

	private final String name;

	private final T defaultValue;

	public ParserSettingImpl(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

}
