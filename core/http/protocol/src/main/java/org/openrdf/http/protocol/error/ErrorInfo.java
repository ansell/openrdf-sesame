/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.error;

/**
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 */
public class ErrorInfo {

	private ErrorType errorType;

	private String errMSg;

	public ErrorInfo(String errMsg) {
		assert errMsg != null : "errMsg must not be null";
		this.errMSg = errMsg;
	}

	public ErrorInfo(ErrorType errorType, String errMsg) {
		this(errMsg);
		this.errorType = errorType;
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
