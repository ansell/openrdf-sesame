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

/**
 * HTTP-related exception that includes the relevant HTTP status code.
 * 
 * @author Arjohn Kampman
 */
public class HTTPException extends Exception {

	private static final long serialVersionUID = 1356463348553827230L;

	private int statusCode;

	public HTTPException(int statusCode) {
		super();
		setStatusCode(statusCode);
	}

	public HTTPException(int statusCode, String message) {
		super(message);
		setStatusCode(statusCode);
	}

	public HTTPException(int statusCode, String message, Throwable t) {
		super(message, t);
		setStatusCode(statusCode);
	}

	public HTTPException(int statusCode, Throwable t) {
		super(t);
		setStatusCode(statusCode);
	}

	public final int getStatusCode() {
		return statusCode;
	}

	protected void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}
