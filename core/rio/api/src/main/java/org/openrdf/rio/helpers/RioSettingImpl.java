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
 * Basic implementation of {@link RioSetting} interface.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public final class RioSettingImpl<T> implements RioSetting<T> {

	/**
	 * @since 2.7.0
	 */
	private static final long serialVersionUID = 270L;

	/**
	 * A unique key for this setting.
	 */
	private final String key;

	/**
	 * A human-readable description for this setting
	 */
	private final String description;

	/**
	 * The default value for this setting. <br>
	 * NOTE: This value must be immutable.
	 */
	private final T defaultValue;

	/**
	 * Create a new setting object that will be used to reference the given
	 * setting.
	 * 
	 * @param key
	 *        A unique key to use for this setting.
	 * @param description
	 *        A short human-readable description for this setting.
	 * @param defaultValue
	 *        An immutable value specifying the default for this setting.
	 */
	public RioSettingImpl(String key, String description, T defaultValue) {

		if (key == null) {
			throw new NullPointerException("Setting key cannot be null");
		}

		if (description == null) {
			throw new NullPointerException("Setting description cannot be null");
		}

		this.key = key;
		this.description = description;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

}
