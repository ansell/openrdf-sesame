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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

	/**
	 * Logger disabled here to check its effect on intermittent Jenkins CI
	 * failures.
	 */
	// protected final Logger log = LoggerFactory.getLogger(this.getClass());

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