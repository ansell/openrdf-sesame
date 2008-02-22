/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private static final long SPAN = 1152921504606846975l;
	public static final int SHIFT = Long.toBinaryString(SPAN).length();
	/** 255 */
	public static final int LONG = 255;
	public static final int MOD = 16;
	private static final String UTF_8 = "UTF-8";
	private static ThreadLocal<MessageDigest> md5 = new ThreadLocal<MessageDigest>() {
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new AssertionError(e);
			}
		}
		
	};

	static {
		for (IdCode code : values()) {
			code.minId = Arrays.asList(values()).indexOf(code) * (SPAN + 1);
		}
	}

	public static IdCode decode(long id) {
		int idx = (int) (id >>> SHIFT);
		IdCode[] values = values();
		if (idx < 0 || idx >= values.length)
			throw new IllegalArgumentException("Invalid ID " + id);
		return values[idx];
	}

	private long minId;

	public long getId(String value) {
		try {
			MessageDigest digest = md5.get();
			digest.update(value.getBytes(UTF_8));
			long id = new BigInteger(1, digest.digest()).longValue();
			return id & SPAN | minId;
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	public long getId(String prefix, String value) {
		try {
			MessageDigest digest = md5.get();
			digest.update(prefix.getBytes(UTF_8));
			long m = new BigInteger(1, digest.digest()).longValue();
			digest.update(value.getBytes(UTF_8));
			long id = new BigInteger(1, digest.digest()).longValue();
			return (m * 31 + id) & SPAN | minId;
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
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
		return minId;
	}

	public long maxId() {
		return minId + SPAN;
	}

	public int code() {
		return (int) (minId >>> SHIFT);
	}

	public boolean isLong() {
		return URI_LONG.equals(this) || SIMPLE_LONG.equals(this)
				|| LANG_LONG.equals(this) || TYPED_LONG.equals(this)
				|| XML.equals(this);
	}
}
