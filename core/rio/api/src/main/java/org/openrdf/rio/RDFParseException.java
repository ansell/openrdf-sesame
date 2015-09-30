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
package org.openrdf.rio;

import org.openrdf.OpenRDFException;

/**
 * A parse exception that can be thrown by a parser when it encounters an error
 * from which it cannot or doesn't want to recover.
 */
public class RDFParseException extends OpenRDFException {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -4686126837948873012L;

	private final long lineNo;

	private final long columnNo;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ParseException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public RDFParseException(String msg) {
		this(msg, -1, -1);
	}

	/**
	 * Creates a new ParseException.
	 * 
	 * @param msg
	 *        An error message.
	 * @param lineNo
	 *        A line number associated with the message.
	 * @param columnNo
	 *        A column number associated with the message.
	 */
	public RDFParseException(String msg, long lineNo, long columnNo) {
		super(msg + getLocationString(lineNo, columnNo));
		this.lineNo = lineNo;
		this.columnNo = columnNo;
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied source
	 * exception.
	 * 
	 * @param t
	 *        The source exception.
	 */
	public RDFParseException(Throwable t) {
		this(t, -1, -1);
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied source
	 * exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param t
	 *        The source exception.
	 * @since 2.7.0
	 */
	public RDFParseException(String msg, Throwable t) {
		this(msg, t, -1, -1);
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied source
	 * exception.
	 * 
	 * @param t
	 *        The source exception.
	 * @param lineNo
	 *        A line number associated with the message.
	 * @param columnNo
	 *        A column number associated with the message.
	 */
	public RDFParseException(Throwable t, long lineNo, long columnNo) {
		super(t.getMessage() + getLocationString(lineNo, columnNo), t);
		this.lineNo = lineNo;
		this.columnNo = columnNo;
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied source
	 * exception.
	 * 
	 * @param t
	 *        The source exception.
	 * @param lineNo
	 *        A line number associated with the message.
	 * @param columnNo
	 *        A column number associated with the message.
	 * @since 2.7.0
	 */
	public RDFParseException(String msg, Throwable t, long lineNo, long columnNo) {
		super(msg + getLocationString(lineNo, columnNo), t);
		this.lineNo = lineNo;
		this.columnNo = columnNo;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the line number associated with this parse exception.
	 * 
	 * @return A line number, or -1 if no line number is available or
	 *         applicable.
	 */
	public long getLineNumber() {
		return lineNo;
	}

	/**
	 * Gets the column number associated with this parse exception.
	 * 
	 * @return A column number, or -1 if no column number is available or
	 *         applicable.
	 */
	public long getColumnNumber() {
		return columnNo;
	}

	/**
	 * Creates a string to that shows the specified line and column number.
	 * Negative line numbers are interpreted as unknowns. Example output: "[line
	 * 12, column 34]". If the specified line number is negative, this method
	 * returns an empty string.
	 */
	public static String getLocationString(long lineNo, long columnNo) {
		if (lineNo < 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder(16);
		sb.append(" [line ");
		sb.append(lineNo);

		if (columnNo >= 0) {
			sb.append(", column ");
			sb.append(columnNo);
		}

		sb.append("]");
		return sb.toString();
	}
}
