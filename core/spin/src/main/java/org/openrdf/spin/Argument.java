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
package org.openrdf.spin;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Class to represent a SPIN argument.
 */
public class Argument {

	private final URI uri;

	private final URI valueType;

	private final boolean optional;

	private final Value defaultValue;

	public Argument(URI uri, URI valueType, boolean optional, Value defaultValue) {
		this.uri = uri;
		this.valueType = valueType;
		this.optional = optional;
		this.defaultValue = defaultValue;
	}

	public URI getPredicate() {
		return uri;
	}

	public URI getValueType() {
		return valueType;
	}

	public boolean isOptional() {
		return optional;
	}

	public Value getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if(valueType != null) {
			buf.append(valueType).append(" ");
		}
		buf.append(uri);
		return buf.toString();
	}
}
