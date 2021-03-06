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
package org.openrdf.model.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * A simple default implementation of the {@link Literal} interface.
 * 
 * @author Arjohn Kampman
 * @author David Huynh
 */
public class SimpleLiteral implements Literal {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -1649571784782592271L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The literal's label.
	 */
	private String label;

	/**
	 * The literal's language tag.
	 */
	private String language;

	/**
	 * The literal's datatype.
	 */
	private IRI datatype;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected SimpleLiteral() {
	}

	/**
	 * Creates a new plain literal with the supplied label.
	 * 
	 * @param label
	 *        The label for the literal, must not be <tt>null</tt>.
	 */
	protected SimpleLiteral(String label) {
		setLabel(label);
		setDatatype(XMLSchema.STRING);
	}

	/**
	 * Creates a new plain literal with the supplied label and language tag.
	 * 
	 * @param label
	 *        The label for the literal, must not be <tt>null</tt>.
	 * @param language
	 *        The language tag for the literal, must not be <tt>null</tt>.
	 */
	protected SimpleLiteral(String label, String language) {
		setLabel(label);
		setLanguage(language);
	}

	/**
	 * Creates a new datyped literal with the supplied label and datatype.
	 * 
	 * @param label
	 *        The label for the literal, must not be <tt>null</tt>.
	 * @param datatype
	 *        The datatype for the literal.
	 */
	protected SimpleLiteral(String label, IRI datatype) {
		setLabel(label);
		if (RDF.LANGSTRING.equals(datatype)) {
			throw new IllegalArgumentException("datatype rdf:langString requires a language tag");
		}
		else if (datatype == null) {
			datatype = XMLSchema.STRING;
		}
		setDatatype(datatype);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void setLabel(String label) {
		Objects.requireNonNull(label, "Literal label cannot be null");
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	protected void setLanguage(String language) {
		Objects.requireNonNull(language);
		this.language = language;
		setDatatype(RDF.LANGSTRING);
	}

	public Optional<String> getLanguage() {
		return Optional.ofNullable(language);
	}

	protected void setDatatype(IRI datatype) {
		this.datatype = datatype;
	}

	public IRI getDatatype() {
		return datatype;
	}

	// Overrides Object.equals(Object), implements Literal.equals(Object)
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof Literal) {
			Literal other = (Literal)o;

			// Compare labels
			if (!label.equals(other.getLabel())) {
				return false;
			}

			// Compare datatypes
			if (!datatype.equals(other.getDatatype())) {
				return false;
			}

			if (getLanguage().isPresent() && other.getLanguage().isPresent()) {
				return getLanguage().get().equalsIgnoreCase(other.getLanguage().get());
			}
			// If only one has a language, then return false
			else if (getLanguage().isPresent() || other.getLanguage().isPresent()) {
				return false;
			}

			return true;
		}

		return false;
	}

	// overrides Object.hashCode(), implements hashCode()
	@Override
	public int hashCode() {
		return label.hashCode();
	}

	/**
	 * Returns the label of the literal.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(label.length() * 2);

		sb.append('"');
		sb.append(label);
		sb.append('"');

		if (Literals.isLanguageLiteral(this)) {
			sb.append('@');
			sb.append(language);
		}
		else {
			sb.append("^^<");
			sb.append(datatype.toString());
			sb.append(">");
		}

		return sb.toString();
	}

	public String stringValue() {
		return label;
	}

	public boolean booleanValue() {
		return XMLDatatypeUtil.parseBoolean(getLabel());
	}

	public byte byteValue() {
		return XMLDatatypeUtil.parseByte(getLabel());
	}

	public short shortValue() {
		return XMLDatatypeUtil.parseShort(getLabel());
	}

	public int intValue() {
		return XMLDatatypeUtil.parseInt(getLabel());
	}

	public long longValue() {
		return XMLDatatypeUtil.parseLong(getLabel());
	}

	public float floatValue() {
		return XMLDatatypeUtil.parseFloat(getLabel());
	}

	public double doubleValue() {
		return XMLDatatypeUtil.parseDouble(getLabel());
	}

	public BigInteger integerValue() {
		return XMLDatatypeUtil.parseInteger(getLabel());
	}

	public BigDecimal decimalValue() {
		return XMLDatatypeUtil.parseDecimal(getLabel());
	}

	public XMLGregorianCalendar calendarValue() {
		return XMLDatatypeUtil.parseCalendar(getLabel());
	}
}
