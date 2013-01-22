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
package org.openrdf.sail.rdbms.schema;

/**
 * A bit array of the possible value types that have been stored in an object
 * column.
 * 
 * @author James Leigh
 * 
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
