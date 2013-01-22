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

import org.openrdf.OpenRDFException;

/**
 * An exception that can be thrown by an RDFHandler when it encounters an
 * unrecoverable error. If an exception is associated with the error then this
 * exception can be wrapped in an RDFHandlerException and can later be retrieved
 * from it when the RDFHandlerException is catched using the
 * <tt>getCause()</tt>.
 */
public class RDFHandlerException extends OpenRDFException {

	private static final long serialVersionUID = -1931215293637533642L;

	/**
	 * Creates a new RDFHandlerException.
	 *
	 * @param msg An error message.
	 */
	public RDFHandlerException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new RDFHandlerException.
	 *
	 * @param cause The cause of the exception.
	 */
	public RDFHandlerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new RDFHandlerException wrapping another exception.
	 *
	 * @param msg An error message.
	 * @param cause The cause of the exception.
	 */
	public RDFHandlerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
