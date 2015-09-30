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
package org.eclipse.rdf4j.rio;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for {@link ParserConfig} and {@link WriterConfig}.
 * 
 * @author Peter Ansell
 */
public class RioConfig implements Serializable {

	/**
	 * @since 2.7.14
	 */
	private static final long serialVersionUID = 2714L;

	/**
	 * A map containing mappings from settings to their values.
	 */
	protected final ConcurrentMap<RioSetting<Object>, Object> settings = new ConcurrentHashMap<RioSetting<Object>, Object>();

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 
	 */
	public RioConfig() {
		super();
	}

	/**
	 * Return the value for a given {@link RioSetting} or the default value if it
	 * has not been set.
	 * 
	 * @param setting
	 *        The {@link RioSetting} to fetch a value for.
	 * @return The value for the parser setting, or the default value if it is
	 *         not set.
	 * @since 2.7.0
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(RioSetting<T> setting) {
		Object result = settings.get(setting);

		if (result == null) {
			return setting.getDefaultValue();
		}

		return (T)result;
	}

	/**
	 * Sets a {@link RioSetting} to have a new value. If the value is null, the
	 * parser setting is removed and the default will be used instead.
	 * 
	 * @param setting
	 *        The setting to set a new value for.
	 * @param value
	 *        The value for the parser setting, or null to reset the parser
	 *        setting to use the default value.
	 * @since 2.7.0
	 */
	@SuppressWarnings("unchecked")
	public <T extends Object> void set(RioSetting<T> setting, T value) {

		if (value == null) {
			settings.remove(setting);
		}
		else {
			Object putIfAbsent = settings.putIfAbsent((RioSetting<Object>)setting, value);

			if (putIfAbsent != null) {
				// override the previous setting anyway, putIfAbsent just gives us
				// information about whether it was previously set or not
				settings.put((RioSetting<Object>)setting, value);

				// this.log.trace("Overriding previous setting for {}",
				// setting.getKey());
			}
		}
	}

	/**
	 * Checks for whether a {@link RioSetting} has been explicitly set by a user.
	 * 
	 * @param setting
	 *        The setting to check for.
	 * @return True if the parser setting has been explicitly set, or false
	 *         otherwise.
	 * @since 2.7.0
	 */
	public <T extends Object> boolean isSet(RioSetting<T> setting) {
		return settings.containsKey(setting);
	}

	/**
	 * Resets all settings back to their default values.
	 * 
	 * @since 2.7.0
	 */
	public void useDefaults() {
		settings.clear();
	}
}