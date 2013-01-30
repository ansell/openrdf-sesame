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

/**
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class ErrorInfo {

	private final ErrorType errorType;

	private final String errMSg;

	public ErrorInfo(String errMsg) {
		this(null, errMsg);
	}

	public ErrorInfo(ErrorType errorType, String errMsg) {
		assert errMsg != null : "errMsg must not be null";
		this.errorType = errorType;
		this.errMSg = errMsg;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public String getErrorMessage() {
		return errMSg;
	}

	@Override
	public String toString() {
		if (errorType != null) {
			StringBuilder sb = new StringBuilder(64);
			sb.append(errorType);
			sb.append(": ");
			sb.append(errMSg);
			return sb.toString();
		}
		else {
			return errMSg;
		}
	}

	/**
	 * Parses the string output that is produced by {@link #toString()}.
	 */
	public static ErrorInfo parse(String errInfoString) {
		String message = errInfoString;
		ErrorType errorType = null;

		int colonIdx = errInfoString.indexOf(':');
		if (colonIdx >= 0) {
			String label = errInfoString.substring(0, colonIdx).trim();
			errorType = ErrorType.forLabel(label);

			if (errorType != null) {
				message = errInfoString.substring(colonIdx + 1);
			}
		}

		return new ErrorInfo(errorType, message.trim());
	}
}
