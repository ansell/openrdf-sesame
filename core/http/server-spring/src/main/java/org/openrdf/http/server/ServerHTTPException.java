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
package org.openrdf.http.server;

import java.net.HttpURLConnection;

/**
 * HTTP-related exception indicating that an error occurred in a server. Status
 * codes for these types of errors are in the 5xx range. The default status code
 * for constructors without a <tt>statusCode</tt> parameter is
 * <tt>500 Internal Server Error</tt>.
 * 
 * @author Arjohn Kampman
 */
public class ServerHTTPException extends HTTPException {

	private static final long serialVersionUID = -3949837199542648966L;

	private static final int DEFAULT_STATUS_CODE = HttpURLConnection.HTTP_INTERNAL_ERROR;

	/**
	 * Creates a {@link ServerHTTPException} with status code 500 "Internal
	 * Server Error".
	 */
	public ServerHTTPException() {
		this(DEFAULT_STATUS_CODE);
	}

	/**
	 * Creates a {@link ServerHTTPException} with status code 500 "Internal
	 * Server Error".
	 */
	public ServerHTTPException(String msg) {
		this(DEFAULT_STATUS_CODE, msg);
	}

	/**
	 * Creates a {@link ServerHTTPException} with status code 500 "Internal
	 * Server Error".
	 */
	public ServerHTTPException(String msg, Throwable t) {
		this(DEFAULT_STATUS_CODE, t);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code. The
	 * supplied status code must be in the 5xx range.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode) {
		super(statusCode);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code. The
	 * supplied status code must be in the 5xx range.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode, String message) {
		super(statusCode, message);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code. The
	 * supplied status code must be in the 5xx range.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode, String message, Throwable t) {
		super(statusCode, message, t);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code. The
	 * supplied status code must be in the 5xx range.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode, Throwable t) {
		super(statusCode, t);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (statusCode < 500 || statusCode > 599) {
			throw new IllegalArgumentException("Status code must be in the 5xx range, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
