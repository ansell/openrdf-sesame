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
package org.openrdf.http.protocol.error;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Arjohn Kampman
 */
public class ErrorType {

	private static final Map<String, ErrorType> registry = new HashMap<String, ErrorType>();

	public static final ErrorType MALFORMED_QUERY = register("MALFORMED QUERY");

	public static final ErrorType MALFORMED_DATA = register("MALFORMED DATA");

	public static final ErrorType UNSUPPORTED_QUERY_LANGUAGE = register("UNSUPPORTED QUERY LANGUAGE");

	public static final ErrorType UNSUPPORTED_FILE_FORMAT = register("UNSUPPORTED FILE FORMAT");

	protected static ErrorType register(String label) {
		synchronized (registry) {
			ErrorType errorType = registry.get(label);

			if (errorType == null) {
				errorType = new ErrorType(label);
				registry.put(label, errorType);
			}

			return errorType;
		}
	}

	public static ErrorType forLabel(String label) {
		synchronized (registry) {
			return registry.get(label);
		}
	}

	/**
	 * The error type's label.
	 */
	private String label;

	private ErrorType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ErrorType) {
			return ((ErrorType)other).getLabel().equals(this.getLabel());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getLabel().hashCode();
	}

	@Override
	public String toString() {
		return label;
	}
}
