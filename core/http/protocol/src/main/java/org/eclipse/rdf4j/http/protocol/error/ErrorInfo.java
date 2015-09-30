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
