/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import static org.openrdf.model.datatypes.XMLDatatypeUtil.isCalendarDatatype;
import static org.openrdf.model.datatypes.XMLDatatypeUtil.isNumericDatatype;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

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

	public static IdCode valueOf(long id) {
		int idx = (int) (id >>> SHIFT);
		IdCode[] values = values();
		if (idx < 0 || idx >= values.length)
			throw new IllegalArgumentException("Invalid ID " + id);
		return values[idx];
	}

	public static IdCode valueOf(Value value) {
		if (value instanceof URI)
			return valueOf((URI) value);
		if (value instanceof Literal)
			return valueOf((Literal) value);
		assert value instanceof BNode : value;
		return valueOf((BNode) value);
	}

	public static IdCode valueOf(BNode value) {
		return BNODE;
	}

	public static IdCode valueOf(URI value) {
		if (value.stringValue().length() > LONG)
			return URI_LONG;
		return URI;
	}

	public static IdCode valueOf(Literal lit) {
		String lang = lit.getLanguage();
		URI dt = lit.getDatatype();
		int length = lit.stringValue().length();
		if (lang != null) {
			// language
			if (length > IdCode.LONG)
				return IdCode.LANG_LONG;
			return IdCode.LANG;
		}
		if (dt == null) {
			// simple
			if (length > IdCode.LONG)
				return IdCode.SIMPLE_LONG;
			return IdCode.SIMPLE;
		}
		if (isNumericDatatype(dt))
			return IdCode.NUMERIC;
		if (isCalendarDatatype(dt)) {
			// calendar
			if (isZoned(lit))
				return IdCode.DATETIME_ZONED;
			return IdCode.DATETIME;
		}
		if (RDF.XMLLITERAL.equals(dt))
			return IdCode.XML;
		if (length > IdCode.LONG)
			return IdCode.TYPED_LONG;
		return IdCode.TYPED;
	}

	private static boolean isZoned(Literal lit) {
		String stringValue = lit.stringValue();
		int length = stringValue.length();
		if (length < 1)
			return false;
		if (stringValue.charAt(length - 1) == 'Z')
			return true;
		if (length < 6)
			return false;
		if (stringValue.charAt(length - 3) != ':')
			return false;
		char chr = stringValue.charAt(length - 6);
		return chr == '+' || chr == '-';
	}

	private long minId;

	public long hash(Value value) {
		MessageDigest digest = md5.get();
		long type = hashLiteralType(digest, value);
		long hash = type * 31 + hash(digest, value.stringValue());
		return hash & SPAN | minId;
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

	private long hashLiteralType(MessageDigest digest, Value value) {
		if (value instanceof Literal) {
			Literal lit = (Literal) value;
			if (lit.getDatatype() != null)
				return hash(digest, lit.getDatatype().stringValue());
			if (lit.getLanguage() != null)
				return hash(digest, lit.getLanguage());
		}
		return 0;
	}

	private long hash(MessageDigest digest, String str) {
		try {
			digest.update(str.getBytes(UTF_8));
			return new BigInteger(1, digest.digest()).longValue();
		}
		catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}
}
