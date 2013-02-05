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
package org.openrdf.query;

/**
 * An exception that can be thrown by an BooleanQueryResultHandler when it
 * encounters an unrecoverable error.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.7.0
 */
public class BooleanQueryResultHandlerException extends QueryResultHandlerException {

	private static final long serialVersionUID = 8530574857852836665L;

	/**
	 * Creates a new BooleanQueryResultHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public BooleanQueryResultHandlerException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new BooleanQueryResultHandlerException wrapping another
	 * exception.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public BooleanQueryResultHandlerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new BooleanQueryResultHandlerException wrapping another
	 * exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public BooleanQueryResultHandlerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
