/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

/**
 * A listener interface for listening to the parser's progress.
 */
public interface ParseLocationListener {

	/**
	 * Signals an update of a parser's progress, indicated by a line
	 * and column number. Both line and column number start with value 1
	 * for the first line or column.
	 *
	 * @param lineNo The line number, or -1 if none is available.
	 * @param columnNo The column number, or -1 if none is available.
	 */
	public void parseLocationUpdate(int lineNo, int columnNo);

}
