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

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.util.Literals;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * An implementation of the {@link Literal} interface.
 * 
 * @author Arjohn Kampman
 * @author David Huynh
 */
public class LiteralImpl implements Literal {

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
	 * The literal's language tag (null if not applicable).
	 */
	private String language;

	/**
	 * The literal's datatype (null if not applicable).
	 */
	private URI datatype;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected LiteralImpl() {
	}

	/**
	 * Creates a new plain literal with the supplied label.
	 * 
	 * @param label
	 *        The label for the literal, must not be <tt>null</tt>.
	 */
	public LiteralImpl(String label) {
		this(label, null, null);
	}

	/**
	 * Creates a new plain literal with the supplied label and language tag.
	 * 
	 * @param label
	 *        The label for the literal, must not be <tt>null</tt>.
	 * @param language
	 *        The language tag for the literal.
	 */
	public LiteralImpl(String label, String language) {
		this(label, language, null);
	}

	/**
	 * Creates a new datyped literal with the supplied label and datatype.
	 * 
	 * @param label
	 *        The label for the literal, must not be <tt>null</tt>.
	 * @param datatype
	 *        The datatype for the literal.
	 */
	public LiteralImpl(String label, URI datatype) {
		this(label, null, datatype);
	}

	/**
	 * Creates a new Literal object, initializing the variables with the supplied
	 * parameters.
	 */
	private LiteralImpl(String label, String language, URI datatype) {
		setLabel(label);
		if (language != null) {
			setLanguage(language.toLowerCase());
		}
		else {
			setDatatype(datatype);
		}
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void setLabel(String label) {
		if (label == null) {
			throw new NullPointerException("Literal label cannot be null");
		}

		this.label = label;
	}

	@Override
	public String getLabel() {
		return label;
	}

	protected void setLanguage(String language) {
		if (language != null) {
			this.datatype = RDF.LANGSTRING;
		}
		this.language = language;
	}

	@Override
	public String getLanguage() {
		return language;
	}

	@Override
	public boolean isLanguageLiteral() {
		return this.language != null;
	}

	protected void setDatatype(URI datatype) {
		if (datatype == null) {
			this.datatype = XMLSchema.STRING;
		}
		else {
			this.datatype = datatype;
		}
	}

	@Override
	public URI getDatatype() {
		return datatype;
	}
	
	@Override
	public boolean isTypedLiteral() {
		return this.language == null;
	}
	
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
			if (datatype == null) {
				if (other.getDatatype() != null) {
					return false;
				}
			}
			else {
				if (!datatype.equals(other.getDatatype())) {
					return false;
				}
			}

			// Compare language tags
			if (language == null) {
				if (other.getLanguage() != null) {
					return false;
				}
			}
			else {
				if (!language.equals(other.getLanguage())) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		return result;
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

		if (isLanguageLiteral()) {
			sb.append('@');
			sb.append(language);
		}
		else if (Literals.hasNonNativeDatatype(this)) {
			sb.append("^^<");
			sb.append(datatype.toString());
			sb.append(">");
		}

		return sb.toString();
	}

	@Override
	public String stringValue() {
		return label;
	}

	@Override
	public boolean booleanValue() {
		return XMLDatatypeUtil.parseBoolean(getLabel());
	}

	@Override
	public byte byteValue() {
		return XMLDatatypeUtil.parseByte(getLabel());
	}

	@Override
	public short shortValue() {
		return XMLDatatypeUtil.parseShort(getLabel());
	}

	@Override
	public int intValue() {
		return XMLDatatypeUtil.parseInt(getLabel());
	}

	@Override
	public long longValue() {
		return XMLDatatypeUtil.parseLong(getLabel());
	}

	@Override
	public float floatValue() {
		return XMLDatatypeUtil.parseFloat(getLabel());
	}

	@Override
	public double doubleValue() {
		return XMLDatatypeUtil.parseDouble(getLabel());
	}

	@Override
	public BigInteger integerValue() {
		return XMLDatatypeUtil.parseInteger(getLabel());
	}

	@Override
	public BigDecimal decimalValue() {
		return XMLDatatypeUtil.parseDecimal(getLabel());
	}

	@Override
	public XMLGregorianCalendar calendarValue() {
		return XMLDatatypeUtil.parseCalendar(getLabel());
	}
}
