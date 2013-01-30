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
package org.openrdf.query.resultio;

import org.openrdf.OpenRDFException;

/**
 * A parse exception that can be thrown by a query result parser when it
 * encounters an error from which it cannot or doesn't want to recover.
 * 
 * @author Arjohn Kampman
 */
public class QueryResultParseException extends OpenRDFException {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -6212290295459157916L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private int lineNo = -1;

	private int columnNo = -1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new QueryResultParseException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public QueryResultParseException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new QueryResultParseException.
	 * 
	 * @param msg
	 *        An error message.
	 * @param lineNo
	 *        A line number associated with the message.
	 * @param columnNo
	 *        A column number associated with the message.
	 */
	public QueryResultParseException(String msg, int lineNo, int columnNo) {
		super(msg);
		this.lineNo = lineNo;
		this.columnNo = columnNo;
	}

	/**
	 * Creates a new QueryResultParseException wrapping another exception. The
	 * QueryResultParseException will inherit its message from the supplied
	 * source exception.
	 * 
	 * @param t
	 *        The source exception.
	 */
	public QueryResultParseException(Throwable t) {
		super(t);
	}

	/**
	 * Creates a new QueryResultParseException wrapping another exception. The
	 * QueryResultParseException will inherit its message from the supplied
	 * source exception.
	 * 
	 * @param t
	 *        The source exception.
	 * @param lineNo
	 *        A line number associated with the message.
	 * @param columnNo
	 *        A column number associated with the message.
	 */
	public QueryResultParseException(Throwable t, int lineNo, int columnNo) {
		super(t);
		this.lineNo = lineNo;
		this.columnNo = columnNo;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Gets the line number associated with this parse exception.
	 * 
	 * @return A line number, or <tt>-1</tt> if no line number is available or
	 *         applicable.
	 */
	public int getLineNumber() {
		return lineNo;
	}

	/**
	 * Gets the column number associated with this parse exception.
	 * 
	 * @return A column number, or <tt>-1</tt> if no column number is available
	 *         or applicable.
	 */
	public int getColumnNumber() {
		return columnNo;
	}
}
