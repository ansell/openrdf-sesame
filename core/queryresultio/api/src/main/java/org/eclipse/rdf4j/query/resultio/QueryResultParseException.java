/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.resultio;

import org.eclipse.rdf4j.OpenRDFException;

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
	 * Creates a new QueryResultParseException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param t
	 *        The source exception.
	 * @since 2.7.0
	 */
	public QueryResultParseException(String msg, Throwable t) {
		super(msg, t);
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
