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
package org.openrdf.sail.rdbms.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;

/**
 * Wraps a {@link LiteralImpl} providing an internal id and version.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsLiteral extends RdbmsValue implements Literal {

	private static final long serialVersionUID = -8213249522968522279L;

	private Literal lit;

	public RdbmsLiteral(Literal lit) {
		this.lit = lit;
	}

	public RdbmsLiteral(Number id, Integer version, Literal lit) {
		super(id, version);
		this.lit = lit;
	}

	@Override
	public boolean booleanValue() {
		return lit.booleanValue();
	}

	@Override
	public byte byteValue() {
		return lit.byteValue();
	}

	@Override
	public XMLGregorianCalendar calendarValue() {
		return lit.calendarValue();
	}

	@Override
	public BigDecimal decimalValue() {
		return lit.decimalValue();
	}

	@Override
	public double doubleValue() {
		return lit.doubleValue();
	}

	@Override
	public float floatValue() {
		return lit.floatValue();
	}

	@Override
	public URI getDatatype() {
		return lit.getDatatype();
	}

	public String getLabel() {
		return lit.getLabel();
	}

	@Override
	public Optional<String> getLanguage() {
		return lit.getLanguage();
	}

	@Override
	public BigInteger integerValue() {
		return lit.integerValue();
	}

	@Override
	public int intValue() {
		return lit.intValue();
	}

	@Override
	public long longValue() {
		return lit.longValue();
	}

	@Override
	public short shortValue() {
		return lit.shortValue();
	}

	@Override
	public String stringValue() {
		return lit.stringValue();
	}

	@Override
	public String toString() {
		return lit.toString();
	}

	@Override
	public boolean equals(Object other) {
		return lit.equals(other);
	}

	@Override
	public int hashCode() {
		return lit.hashCode();
	}
}
