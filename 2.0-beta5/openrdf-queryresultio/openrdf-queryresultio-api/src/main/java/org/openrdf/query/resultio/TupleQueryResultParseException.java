/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

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
