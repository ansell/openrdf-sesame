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
package org.openrdf.rio;

/**
 * A RuntimeException indicating that a specific RDF format is not supported. A typical cause
 * of this exception is that the class library for the specified RDF format is not present
 * in the classpath.
 * 
 * @author jeen
 */
public class UnsupportedRDFormatException extends RuntimeException {

	private static final long serialVersionUID = -2709196386078518696L;

	/**
	 * Creates a new UnsupportedRDFormatException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public UnsupportedRDFormatException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new UnsupportedRDFormatException.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public UnsupportedRDFormatException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new UnsupportedRDFormatException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public UnsupportedRDFormatException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
