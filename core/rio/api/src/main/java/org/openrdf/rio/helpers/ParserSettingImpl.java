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

import org.openrdf.rio.ParserSetting;

/**
 * Basic implementation of {@link ParserSetting} interface.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public final class ParserSettingImpl<T> implements ParserSetting<T> {

	/**
	 * @since 2.7.0
	 */
	private static final long serialVersionUID = 270L;

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
