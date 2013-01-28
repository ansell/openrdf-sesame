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

import java.util.Arrays;

/**
 * Indicates the different type of internal id used within the store and some
 * basic properties.
 * 
 * @author James Leigh
 * 
 */
public enum ValueType {
	// 0000 0010 0011 0100 0101 0110 0111 1000 1001 1010 1011 1100 1101
	URI,
	URI_LONG,
	BNODE,
	SIMPLE,
	SIMPLE_LONG,
	TYPED,
	TYPED_LONG,
	NUMERIC,
	DATETIME,
	DATETIME_ZONED,
	LANG,
	LANG_LONG,
	XML,
	B14,
	B15,
	B16;

	private int index;
	static {
		for (ValueType type : values()) {
			type.index = Arrays.asList(values()).indexOf(type);
		}
	}

	public int index() {
		return index;
	}

	public boolean isBNode() {
		return BNODE.equals(this);
	}

	public boolean isURI() {
		return URI.equals(this) || URI_LONG.equals(this);
	}

	public boolean isLiteral() {
		return !BNODE.equals(this) && !URI.equals(this) && !URI_LONG.equals(this);
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

	public boolean isLong() {
		return URI_LONG.equals(this) || SIMPLE_LONG.equals(this) || LANG_LONG.equals(this)
				|| TYPED_LONG.equals(this) || XML.equals(this);
	}
}
