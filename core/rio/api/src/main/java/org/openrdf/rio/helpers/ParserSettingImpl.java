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
public final class ParserSettingImpl<T> implements ParserSetting<T> {

	/**
	 * A unique key for this parser setting.
	 */
	private final String key;

	/**
	 * A human-readable description for this parser setting
	 */
	private final String description;

	/**
	 * The default value for this parser setting. <br>
	 * NOTE: This value must be immutable.
	 */
	private final T defaultValue;

	public ParserSettingImpl(String key, String description, T defaultValue) {

		if (key == null) {
			throw new NullPointerException("Parser Setting key cannot be null");
		}

		if (description == null) {
			throw new NullPointerException("Parser Setting description cannot be null");
		}

		this.key = key;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public String getDescription() {
		return description;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

}
