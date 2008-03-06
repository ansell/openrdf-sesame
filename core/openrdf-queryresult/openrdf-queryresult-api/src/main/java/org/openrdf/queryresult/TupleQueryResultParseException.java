/*  Copyright (C) 2001-2006 Aduna (http://www.aduna-software.com/)
 *
 *  This software is part of the Sesame Framework. It is licensed under
 *  the following two licenses as alternatives:
 *
 *   1. Open Software License (OSL) v3.0
 *   2. GNU Lesser General Public License (LGPL) v2.1 or any newer
 *      version
 *
 *  By using, modifying or distributing this software you agree to be 
 *  bound by the terms of at least one of the above licenses.
 *
 *  See the file LICENSE.txt that is distributed with this software
 *  for the complete terms and further details.
 */
package org.openrdf.queryresult;

/**
 * A parse exception that can be thrown by a parser when it encounters
 * an error from which it cannot or doesn't want to recover.
 */
public class TupleQueryResultParseException extends Exception {

	private static final long serialVersionUID = -6212290295459157916L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private int _lineNo = -1;

	private int _columnNo = -1;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ParseException.
	 *
	 * @param msg An error message.
	 */
	public TupleQueryResultParseException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new ParseException.
	 *
	 * @param msg An error message.
	 * @param lineNo A line number associated with the message.
	 * @param columnNo A column number associated with the message.
	 */
	public TupleQueryResultParseException(String msg, int lineNo, int columnNo) {
		super(msg);
		_lineNo = lineNo;
		_columnNo = columnNo;
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied
	 * source exception.
	 *
	 * @param t The source exception.
	 */
	public TupleQueryResultParseException(Throwable t) {
		super(t);
	}

	/**
	 * Creates a new ParseException wrapping another exception. The
	 * ParseException will inherit its message from the supplied
	 * source exception.
	 *
	 * @param t The source exception.
	 * @param lineNo A line number associated with the message.
	 * @param columnNo A column number associated with the message.
	 */
	public TupleQueryResultParseException(Throwable t, int lineNo, int columnNo) {
		super(t);
		_lineNo = lineNo;
		_columnNo = columnNo;
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Gets the line number associated with this parse exception.
	 * @return A line number, or -1 if no line number is available
	 * or applicable.
	 */
	public int getLineNumber() {
		return _lineNo;
	}

	/**
	 * Gets the column number associated with this parse exception.
	 * @return A column number, or -1 if no column number is available
	 * or applicable.
	 */
	public int getColumnNumber() {
		return _columnNo;
	}
}
