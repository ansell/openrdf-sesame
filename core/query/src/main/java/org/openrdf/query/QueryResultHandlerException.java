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
 * The super class of exceptions originating from {@link QueryResultHandler}
 * implementations.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class QueryResultHandlerException extends OpenRDFException {

	private static final long serialVersionUID = 5096811224670124398L;

	/**
	 * Creates a new QueryResultHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public QueryResultHandlerException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new QueryResultHandlerException wrapping another exception.
	 * 
	 * @param t
	 *        The cause of the exception.
	 */
	public QueryResultHandlerException(Throwable t) {
		super(t);
	}

	/**
	 * Creates a new QueryResultHandlerException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param t
	 *        The cause of the exception.
	 */
	public QueryResultHandlerException(String msg, Throwable t) {
		super(msg, t);
	}

}