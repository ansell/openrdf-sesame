/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.util.Arrays;

/**
 * Indicates the different type of internal id used within the store and some
 * basic properties.
 * 
 * @author James Leigh
 * 
 */
public enum IdCode {
	// 0000 0010 0011 0100 0101 0110 0111 1000 1001 1010 1011 1100 1101
	URI, URI_LONG, BNODE, SIMPLE, SIMPLE_LONG, TYPED, TYPED_LONG, NUMERIC, DATETIME, DATETIME_ZONED, LANG, LANG_LONG, XML, B14, B15, B16;
	private static final long STEP = 1152921504606846976l;
	public static final int SHIFT = Long.toBinaryString(STEP).length() - 1;
	/** 255 */
	public static final int LONG = 255;
	public static final int MOD = 16;

	public static IdCode decode(long id) {
		int idx = (int) (id >>> SHIFT);
		IdCode[] values = values();
		if (idx < 0 || idx >= values.length)
			throw new IllegalArgumentException("Invalid ID " + id);
		return values[idx];
	}

	public boolean isBNode() {
		return BNODE.equals(this);
	}

	public boolean isURI() {
		return URI.equals(this) || URI_LONG.equals(this);
	}

	public boolean isLiteral() {
		return !BNODE.equals(this) && !URI.equals(this)
				&& !URI_LONG.equals(this);
	}

	public boolean isSimpleLiteral() {
		return SIMPLE.equals(this) || SIMPLE_LONG.equals(this);
	}

	public boolean isLanguageLiteral() {
		return LANG.equals(this) || LANG_LONG.equals(this);
	}

	public boolean isTypedLiteral() {
		return isLiteral() && !isSimpleLiteral() && !isLanguageLiteral();
	}

	public boolean isNumericLiteral() {
		return NUMERIC.equals(this);
	}

	public boolean isCalendarLiteral() {
		return DATETIME.equals(this) || DATETIME_ZONED.equals(this);
	}

	public boolean isXmlLiteral() {
		return XML.equals(this);
	}

	public long minId() {
		return index() * STEP;
	}

	public int index() {
		return Arrays.asList(values()).indexOf(this);
	}

	public long maxId() {
		return minId() + (STEP - 1);
	}

	public int code() {
		return (int) (minId() >>> SHIFT);
	}

	public boolean isLong() {
		return URI_LONG.equals(this) || SIMPLE_LONG.equals(this)
				|| LANG_LONG.equals(this) || TYPED_LONG.equals(this)
				|| XML.equals(this);
	}
}
