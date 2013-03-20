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
 * A parse exception that can be thrown by a parser when it encounters an error
 * from which it cannot or doesn't want to recover.
 */
public class RDFParseException extends OpenRDFException {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -4686126837948873012L;

	private final int lineNo;

	private final int columnNo;

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
	public RDFParseException(String msg, int lineNo, int columnNo) {
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
	public RDFParseException(Throwable t, int lineNo, int columnNo) {
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
	public RDFParseException(String msg, Throwable t, int lineNo, int columnNo) {
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
	 * @return A line number, or -1 if no line number is available or applicable.
	 */
	public int getLineNumber() {
		return lineNo;
	}

	/**
	 * Gets the column number associated with this parse exception.
	 * 
	 * @return A column number, or -1 if no column number is available or
	 *         applicable.
	 */
	public int getColumnNumber() {
		return columnNo;
	}

	/**
	 * Creates a string to that shows the specified line and column number.
	 * Negative line numbers are interpreted as unknowns. Example output: "[line
	 * 12, column 34]". If the specified line number is negative, this method
	 * returns an empty string.
	 */
	public static String getLocationString(int lineNo, int columnNo) {
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
