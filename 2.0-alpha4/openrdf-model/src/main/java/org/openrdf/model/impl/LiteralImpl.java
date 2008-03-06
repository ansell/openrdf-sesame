/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.impl;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;

/**
 * An implementation of the {@link Literal} interface.
 */
public class LiteralImpl implements Literal {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The literal's label.
	 */
	private final String _label;

	/**
	 * The literal's language tag (null if not applicable).
	 */
	private final String _language;

	/**
	 * The literal's datatype (null if not applicable).
	 */
	private final URI _datatype;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new plain literal with the supplied label.
	 *
	 * @param label The label for the literal, must not be <tt>null</tt>.
	 */
	public LiteralImpl(String label) {
		this(label, null, null);
	}

	/**
	 * Creates a new plain literal with the supplied label and language tag.
	 *
	 * @param label The label for the literal, must not be <tt>null</tt>.
	 * @param language The language tag for the literal.
	 */
	public LiteralImpl(String label, String language) {
		this(label, language, null);
	}

	/**
	 * Creates a new datyped literal with the supplied label and datatype.
	 *
	 * @param label The label for the literal, must not be <tt>null</tt>.
	 * @param datatype The datatype for the literal.
	 */
	public LiteralImpl(String label, URI datatype) {
		this(label, null, datatype);
	}
	
	/**
	 * Creates a new Literal object, initializing the variables with the
	 * supplied parameters.
	 */
	private LiteralImpl(String label, String language, URI datatype) {
		assert label != null;
		
		_label = label;
		_language = (language == null) ? null : language.toLowerCase();
		_datatype = datatype;
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements Literal.getLabel()
	public String getLabel() {
		return _label;
	}

	// implements Literal.getLanguage()
	public String getLanguage() {
		return _language;
	}

	// implements Literal.getDatatype()
	public URI getDatatype() {
		return _datatype;
	}

	// Overrides Object.equals(Object), implements Literal.equals(Object)
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof Literal) {
			Literal other = (Literal)o;

			// Compare labels
			if (!_label.equals(other.getLabel())) {
				return false;
			}

			// Compare datatypes
			if (_datatype == null) {
				if (other.getDatatype() != null) {
					return false;
				}
			}
			else {
				if (!_datatype.equals(other.getDatatype())) {
					return false;
				}
			}

			// Compare language tags
			if (_language == null) {
				if (other.getLanguage() != null) {
					return false;
				}
			}
			else {
				if (!_language.equals(other.getLanguage())) {
					return false;
				}
			}

			return true;
		}

		return false;
	}

	// overrides Object.hashCode(), implements hashCode()
	public int hashCode() {
		return _label.hashCode();
	}
	
	/**
	 * Returns the label of the literal.
	 */
	public String toString() {
		return _label;
	}
}
