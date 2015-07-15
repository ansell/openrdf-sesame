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

import org.openrdf.OpenRDFException;

/**
 * An exception indicating that the execution of an update failed.
 * 
 * @author Jeen
 */
public class UpdateExecutionException extends OpenRDFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7969399526232927434L;

	public UpdateExecutionException() {
		super();
	}

	/**
	 * Creates a new UpdateExecutionException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public UpdateExecutionException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link UpdateExecutionException} wrapping another exception.
	 * 
	 * @param cause
	 *        the cause of the exception
	 */
	public UpdateExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new {@link UpdateExecutionException} wrapping another exception.
	 * 
	 * @param msg
	 *        and error message.
	 * @param cause
	 *        the cause of the exception
	 */
	public UpdateExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
