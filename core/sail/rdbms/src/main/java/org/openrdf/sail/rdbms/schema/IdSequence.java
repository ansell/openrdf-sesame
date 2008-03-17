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
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sail.rdbms.model.RdbmsValue;

/**
 * 
 * @author James Leigh
 */
public class IdSequence {

	private static final String UTF_8 = "UTF-8";

	private static ThreadLocal<MessageDigest> md5 = new ThreadLocal<MessageDigest>() {

		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("MD5");
			}
			catch (NoSuchAlgorithmException e) {
				throw new AssertionError(e);
			}
		}

	};

	private long SPAN = 1152921504606846975l;

	private int SHIFT = Long.toBinaryString(SPAN).length();

	/** 255 */
	private int LONG = 255;

	private int MOD = 16;

	private long[] minIds;

	private HashTable table;

	private ConcurrentMap<ValueType, AtomicLong> seq;

	public int getMod() {
		return MOD;
	}

	public int getShift() {
		return SHIFT;
	}

	public void setHashTable(HashTable table) {
		this.table = table;
	}

	public void init()
		throws SQLException
	{
		minIds = new long[ValueType.values().length];
		for (int i = 0; i < minIds.length; i++) {
			minIds[i] = i * (SPAN + 1);
		}
		if (table != null) {
			seq = new ConcurrentHashMap<ValueType, AtomicLong>();
			for (long max : table.maxIds(getShift(), getMod())) {
				ValueType code = valueOf(max);
				if (max > minId(code)) {
					seq.put(code, new AtomicLong(max));
				}
			}
		}
	}

	public double code(Literal value) {
		return (int)(minId(valueOf(value)) >>> SHIFT);
	}

	public long hashOf(Value value) {
		MessageDigest digest = md5.get();
		long type = hashLiteralType(digest, value);
		long hash = type * 31 + hash(digest, value.stringValue());
		return hash & SPAN | minId(valueOf(value));
	}

	public boolean isLiteral(long id) {
		return valueOf(id).isLiteral();
	}

	public boolean isLong(long id) {
		return valueOf(id).isLong();
	}

	public boolean isURI(long id) {
		return valueOf(id).isURI();
	}

	public long maxId(ValueType type) {
		return minId(type) + SPAN;
	}

	public long minId(ValueType type) {
		return minIds[type.index()];
	}

	public long idOf(RdbmsValue value) {
		assert seq == null;
		return hashOf(value);
	}

	public long nextId(RdbmsValue value) {
		ValueType code = valueOf(value);
		if (!seq.containsKey(code)) {
			seq.putIfAbsent(code, new AtomicLong(minId(code)));
		}
		return seq.get(code).incrementAndGet();
	}

	public ValueType valueOf(long id) {
		int idx = (int)(id >>> SHIFT);
		ValueType[] values = ValueType.values();
		if (idx < 0 || idx >= values.length)
			throw new IllegalArgumentException("Invalid ID " + id);
		return values[idx];
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

	private long hashLiteralType(MessageDigest digest, Value value) {
		if (value instanceof Literal) {
			Literal lit = (Literal)value;
			if (lit.getDatatype() != null)
				return hash(digest, lit.getDatatype().stringValue());
			if (lit.getLanguage() != null)
				return hash(digest, lit.getLanguage());
		}
		return 0;
	}

	private boolean isZoned(Literal lit) {
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

	private ValueType valueOf(BNode value) {
		return ValueType.BNODE;
	}

	private ValueType valueOf(Literal lit) {
		String lang = lit.getLanguage();
		URI dt = lit.getDatatype();
		int length = lit.stringValue().length();
		if (lang != null) {
			// language
			if (length > LONG)
				return ValueType.LANG_LONG;
			return ValueType.LANG;
		}
		if (dt == null) {
			// simple
			if (length > LONG)
				return ValueType.SIMPLE_LONG;
			return ValueType.SIMPLE;
		}
		if (isNumericDatatype(dt))
			return ValueType.NUMERIC;
		if (isCalendarDatatype(dt)) {
			// calendar
			if (isZoned(lit))
				return ValueType.DATETIME_ZONED;
			return ValueType.DATETIME;
		}
		if (RDF.XMLLITERAL.equals(dt))
			return ValueType.XML;
		if (length > LONG)
			return ValueType.TYPED_LONG;
		return ValueType.TYPED;
	}

	private ValueType valueOf(URI value) {
		if (value.stringValue().length() > LONG)
			return ValueType.URI_LONG;
		return ValueType.URI;
	}

	private ValueType valueOf(Value value) {
		if (value instanceof URI)
			return valueOf((URI)value);
		if (value instanceof Literal)
			return valueOf((Literal)value);
		assert value instanceof BNode : value;
		return valueOf((BNode)value);
	}
}
