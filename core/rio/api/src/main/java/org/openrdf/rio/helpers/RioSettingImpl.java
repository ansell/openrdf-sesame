/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
