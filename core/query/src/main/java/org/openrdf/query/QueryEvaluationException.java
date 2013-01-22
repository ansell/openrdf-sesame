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
 * An exception indicating that the evaluation of a query failed.
 * 
 * @author Arjohn Kampman
 */
public class QueryEvaluationException extends OpenRDFException {

	private static final long serialVersionUID = 602749602257031631L;

	public QueryEvaluationException() {
		super();
	}

	/**
	 * Creates a new TupleQueryResultHandlerException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public QueryEvaluationException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException wrapping another exception.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public QueryEvaluationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new TupleQueryResultHandlerException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public QueryEvaluationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
