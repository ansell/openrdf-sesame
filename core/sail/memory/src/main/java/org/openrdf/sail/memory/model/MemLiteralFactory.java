/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralFactoryImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * A factory for MemValue objects that keeps track of created objects to prevent
 * the creation of duplicate objects, minimizing memory usage as a result.
 * 
 * @author Arjohn Kampman
 * @author David Huynh
 * @author James Leigh
 */
public class MemLiteralFactory extends LiteralFactoryImpl {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Registry containing the set of MemLiteral objects as used by a
	 * MemoryStore. This registry enables the reuse of objects, minimizing the
	 * number of objects in main memory.
	 */
	private final WeakObjectRegistry<MemLiteral> literalRegistry = new WeakObjectRegistry<MemLiteral>();

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * See getMemValue() for description.
	 */
	public synchronized MemLiteral getMemLiteral(Literal literal) {
		if (isOwnMemValue(literal)) {
			return (MemLiteral)literal;
		}
		else {
			return literalRegistry.get(literal);
		}
	}

	/**
	 * Checks whether the supplied value is an instance of <tt>MemValue</tt> and
	 * whether it has been created by this MemValueFactory.
	 */
	private boolean isOwnMemValue(Value value) {
		return value instanceof MemValue && ((MemValue)value).getCreator() == this;
	}

	/**
	 * Gets all literals that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<MemLiteral> getMemLiterals() {
		return Collections.unmodifiableSet(literalRegistry);
	}

	/**
	 * See createMemValue() for description.
	 */
	public synchronized MemLiteral createMemLiteral(Literal literal) {
		MemLiteral memLiteral = null;

		String label = literal.getLabel();
		URI datatype = literal.getDatatype();
		if (datatype != null) {
			try {
				if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
					memLiteral = new IntegerMemLiteral(this, label, literal.integerValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.DECIMAL)) {
					memLiteral = new DecimalMemLiteral(this, label, literal.decimalValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.FLOAT)) {
					memLiteral = new NumericMemLiteral(this, label, literal.floatValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.DOUBLE)) {
					memLiteral = new NumericMemLiteral(this, label, literal.doubleValue(), datatype);
				}
				else if (datatype.equals(XMLSchema.BOOLEAN)) {
					memLiteral = new BooleanMemLiteral(this, label, literal.booleanValue());
				}
				else if (datatype.equals(XMLSchema.DATETIME)) {
					memLiteral = new CalendarMemLiteral(this, label, literal.calendarValue());
				}
				else {
					memLiteral = new MemLiteral(this, literal.getLabel(), datatype);
				}
			}
			catch (IllegalArgumentException e) {
				// Unable to parse literal label to primitive type
				memLiteral = new MemLiteral(this, literal.getLabel(), datatype);
			}
		}
		else if (literal.getLanguage() != null) {
			memLiteral = new MemLiteral(this, literal.getLabel(), literal.getLanguage());
		}
		else {
			memLiteral = new MemLiteral(this, literal.getLabel());
		}

		boolean wasNew = literalRegistry.add(memLiteral);
		assert wasNew : "Created a duplicate MemLiteral for literal " + literal;

		return memLiteral;
	}

	@Override
	public synchronized Literal createLiteral(String value) {
		Literal tempLiteral = new LiteralImpl(value);
		MemLiteral memLiteral = literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	@Override
	public synchronized Literal createLiteral(String value, String language) {
		Literal tempLiteral = new LiteralImpl(value, language);
		MemLiteral memLiteral = literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	@Override
	public synchronized Literal createLiteral(String value, URI datatype) {
		Literal tempLiteral = new LiteralImpl(value, datatype);
		MemLiteral memLiteral = literalRegistry.get(tempLiteral);

		if (memLiteral == null) {
			memLiteral = createMemLiteral(tempLiteral);
		}

		return memLiteral;
	}

	@Override
	public synchronized Literal createLiteral(boolean value) {
		MemLiteral newLiteral = new BooleanMemLiteral(this, value);
		return getSharedLiteral(newLiteral);
	}

	@Override
	protected Literal createIntegerLiteral(Number n, URI datatype) {
		MemLiteral newLiteral = new IntegerMemLiteral(this, BigInteger.valueOf(n.longValue()), datatype);
		return getSharedLiteral(newLiteral);
	}

	@Override
	protected Literal createFPLiteral(Number n, URI datatype) {
		MemLiteral newLiteral = new NumericMemLiteral(this, n, datatype);
		return getSharedLiteral(newLiteral);
	}

	@Override
	public synchronized Literal createLiteral(XMLGregorianCalendar calendar) {
		MemLiteral newLiteral = new CalendarMemLiteral(this, calendar);
		return getSharedLiteral(newLiteral);
	}

	private Literal getSharedLiteral(MemLiteral newLiteral) {
		MemLiteral sharedLiteral = literalRegistry.get(newLiteral);

		if (sharedLiteral == null) {
			boolean wasNew = literalRegistry.add(newLiteral);
			assert wasNew : "Created a duplicate MemLiteral for literal " + newLiteral;
			sharedLiteral = newLiteral;
		}

		return sharedLiteral;
	}
}
