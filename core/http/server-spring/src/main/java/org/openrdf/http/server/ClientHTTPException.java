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
package org.openrdf.http.server;

import java.net.HttpURLConnection;

/**
 * HTTP-related exception indicating that an HTTP client has erred. Status codes
 * for these types of errors are in the 4xx range. The default status code for
 * constructors without a <tt>statusCode</tt> parameter is
 * <tt>400 Bad Request</tt>.
 * 
 * @author Arjohn Kampman
 */
public class ClientHTTPException extends HTTPException {

	private static final long serialVersionUID = 7722604284325312749L;

	private static final int DEFAULT_STATUS_CODE = HttpURLConnection.HTTP_BAD_REQUEST;

	/**
	 * Creates a {@link ClientHTTPException} with status code 400 "Bad Request".
	 */
	public ClientHTTPException() {
		this(DEFAULT_STATUS_CODE);
	}

	/**
	 * Creates a {@link ClientHTTPException} with status code 400 "Bad Request".
	 */
	public ClientHTTPException(String msg) {
		this(DEFAULT_STATUS_CODE, msg);
	}

	/**
	 * Creates a {@link ClientHTTPException} with status code 400 "Bad Request".
	 */
	public ClientHTTPException(String msg, Throwable t) {
		this(DEFAULT_STATUS_CODE, t);
	}

	/**
	 * Creates a {@link ClientHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 4xx range.
	 */
	public ClientHTTPException(int statusCode) {
		super(statusCode);
	}

	/**
	 * Creates a {@link ClientHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 4xx range.
	 */
	public ClientHTTPException(int statusCode, String message) {
		super(statusCode, message);
	}

	/**
	 * Creates a {@link ClientHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 4xx range.
	 */
	public ClientHTTPException(int statusCode, String message, Throwable t) {
		super(statusCode, message, t);
	}

	/**
	 * Creates a {@link ClientHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 4xx range.
	 */
	public ClientHTTPException(int statusCode, Throwable t) {
		super(statusCode, t);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (statusCode < 400 || statusCode > 499) {
			throw new IllegalArgumentException("Status code must be in the 4xx range, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
