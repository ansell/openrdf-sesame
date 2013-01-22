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
package org.openrdf.query.dawg;

import org.openrdf.OpenRDFException;

/**
 * An exception that is thrown to indicate that the parsing of a DAWG Test
 * Result Set graph failed due to an incompatible or incomplete graph.
 */
public class DAWGTestResultSetParseException extends OpenRDFException {

	private static final long serialVersionUID = -8655777672973690037L;

	/**
	 * Creates a new DAWGTestResultSetParseException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public DAWGTestResultSetParseException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new DAWGTestResultSetParseException wrapping another exception.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public DAWGTestResultSetParseException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new DAWGTestResultSetParseException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public DAWGTestResultSetParseException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
