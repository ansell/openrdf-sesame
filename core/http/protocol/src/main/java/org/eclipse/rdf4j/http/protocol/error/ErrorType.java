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
package org.eclipse.rdf4j.http.protocol.error;

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
