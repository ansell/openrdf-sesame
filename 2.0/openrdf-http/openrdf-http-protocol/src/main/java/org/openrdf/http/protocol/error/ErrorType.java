/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
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

	public boolean equals(Object other) {
		if (other instanceof ErrorType) {
			return ((ErrorType)other).getLabel().equals(this.getLabel());
		}

		return false;
	}

	@Override
	public String toString() {
		return label;
	}
}
