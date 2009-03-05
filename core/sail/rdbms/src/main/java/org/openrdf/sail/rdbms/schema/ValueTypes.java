/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

/**
 * A bit array of the possible value types that have been stored in an object
 * column.
 * 
 * @author James Leigh
 */
public class ValueTypes implements Cloneable {

	public static ValueTypes UNKNOWN = new ValueTypes();
	static {
		UNKNOWN.bnodes = true;
		UNKNOWN.uris = true;
		UNKNOWN.literals = true;
		UNKNOWN.typed = true;
		UNKNOWN.numeric = true;
		UNKNOWN.calendar = true;
		UNKNOWN.languages = true;
		UNKNOWN.longValues = true;
	}

	public static ValueTypes RESOURCE = new ValueTypes();
	static {
		RESOURCE.bnodes = true;
		RESOURCE.uris = true;
		RESOURCE.longValues = true;
	}

	public static ValueTypes URI = new ValueTypes();
	static {
		URI.uris = true;
		URI.longValues = true;
	}

	private boolean bnodes;

	private boolean uris;

	private boolean literals;

	private boolean typed;

	private boolean numeric;

	private boolean calendar;

	private boolean languages;

	private boolean longValues;

	public boolean isBNodes() {
		return bnodes;
	}

	public boolean isURIs() {
		return uris;
	}

	public boolean isLiterals() {
		return literals;
	}

	public boolean isTyped() {
		return typed;
	}

	public boolean isNumeric() {
		return numeric;
	}

	public boolean isCalendar() {
		return calendar;
	}

	public boolean isLanguages() {
		return languages;
	}

	public boolean isLong() {
		return longValues;
	}

	public void reset() {
		bnodes = false;
		uris = false;
		literals = false;
		typed = false;
		numeric = false;
		calendar = false;
		languages = false;
		longValues = false;
	}

	public void add(ValueType code) {
		bnodes |= code.isBNode();
		uris |= code.isURI();
		literals |= code.isLiteral();
		typed |= code.isTypedLiteral();
		numeric |= code.isNumericLiteral();
		calendar |= code.isCalendarLiteral();
		languages |= code.isLanguageLiteral();
		longValues |= code.isLong();
	}

	public ValueTypes merge(ValueTypes valueTypes) {
		bnodes |= valueTypes.bnodes;
		uris |= valueTypes.uris;
		literals |= valueTypes.literals;
		typed |= valueTypes.typed;
		numeric |= valueTypes.numeric;
		calendar |= valueTypes.calendar;
		languages |= valueTypes.languages;
		longValues |= valueTypes.longValues;
		return this;
	}

	@Override
	public ValueTypes clone() {
		try {
			return (ValueTypes)super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
}
