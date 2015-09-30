/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.model.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

/**
 * Abstract base class for {@link ValueFactory} implementations. It implements
 * all basic {@link Value} creation methods by using the default implementations
 * ({@link SimpleBNode}, {@link SimpleIRI}, etc), and type-optimized subclasses
 * (e.g. {@link BooleanLiteral}, {@link NumericLiteral}) where possible.
 * 
 * @author Arjohn Kampman
 * @author Jeen Broekstra
 */
public abstract class AbstractValueFactory implements ValueFactory {

	/**
	 * "universal" ID for bnode prefixes to prevent blank node clashes (unique
	 * per classloaded instance of this class)
	 */
	private static long lastBNodePrefixUID = 0;

	private static synchronized long getNextBNodePrefixUid() {
		return lastBNodePrefixUID = Math.max(System.currentTimeMillis(), lastBNodePrefixUID + 1);
	}

	private static final DatatypeFactory datatypeFactory;

	static {
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		}
		catch (DatatypeConfigurationException e) {
			throw new Error("Could not instantiate javax.xml.datatype.DatatypeFactory", e);
		}
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The ID for the next bnode that is created.
	 */
	private int nextBNodeID;

	/**
	 * The prefix for any new bnode IDs.
	 */
	private String bnodePrefix;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AbstractValueFactory() {
		initBNodeParams();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public IRI createIRI(String iri) {
		return new SimpleIRI(iri);
	}

	@Override
	public IRI createIRI(String namespace, String localName) {
		return createIRI(namespace + localName);
	}

	@Override
	public BNode createBNode(String nodeID) {
		return new SimpleBNode(nodeID);
	}

	@Override
	public Literal createLiteral(String value) {
		return new SimpleLiteral(value, XMLSchema.STRING);
	}

	@Override
	public Literal createLiteral(String value, String language) {
		return new SimpleLiteral(value, language);
	}

	@Override
	public Literal createLiteral(boolean b) {
		return b ? BooleanLiteral.TRUE : BooleanLiteral.FALSE;
	}

	@Override
	public Literal createLiteral(String value, IRI datatype) {
		return new SimpleLiteral(value, datatype);
	}

	@Override
	public Statement createStatement(Resource subject, IRI predicate, Value object) {
		return new SimpleStatement(subject, predicate, object);
	}

	@Override
	public Statement createStatement(Resource subject, IRI predicate, Value object, Resource context) {
		return new ContextStatement(subject, predicate, object, context);
	}

	/**
	 * Generates a new bnode prefix and resets <tt>nextBNodeID</tt> to <tt>1</tt>
	 * .
	 */
	protected void initBNodeParams() {
		// BNode prefix is based on currentTimeMillis(). Combined with a
		// sequential number per session, this gives a unique identifier.
		bnodePrefix = "node" + Long.toString(getNextBNodePrefixUid(), 32) + "x";
		nextBNodeID = 1;
	}

	@Override
	public synchronized BNode createBNode() {
		int id = nextBNodeID++;

		BNode result = createBNode(bnodePrefix + id);

		if (id == Integer.MAX_VALUE) {
			// Start with a new bnode prefix
			initBNodeParams();
		}

		return result;
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#BYTE} as parameters.
	 */
	@Override
	public Literal createLiteral(byte value) {
		return createIntegerLiteral(value, XMLSchema.BYTE);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#SHORT} as parameters.
	 */
	@Override
	public Literal createLiteral(short value) {
		return createIntegerLiteral(value, XMLSchema.SHORT);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#INT} as parameters.
	 */
	@Override
	public Literal createLiteral(int value) {
		return createIntegerLiteral(value, XMLSchema.INT);
	}

	/**
	 * Calls {@link #createIntegerLiteral(Number, IRI)} with the supplied value
	 * and {@link XMLSchema#LONG} as parameters.
	 */
	@Override
	public Literal createLiteral(long value) {
		return createIntegerLiteral(value, XMLSchema.LONG);
	}

	/**
	 * Calls {@link #createNumericLiteral(Number, IRI)} with the supplied value
	 * and datatype as parameters.
	 */
	protected Literal createIntegerLiteral(Number value, IRI datatype) {
		return createNumericLiteral(value, datatype);
	}

	/**
	 * Calls {@link #createFPLiteral(Number, IRI)} with the supplied value and
	 * {@link XMLSchema#FLOAT} as parameters.
	 */
	@Override
	public Literal createLiteral(float value) {
		return createFPLiteral(value, XMLSchema.FLOAT);
	}

	/**
	 * Calls {@link #createFPLiteral(Number, IRI)} with the supplied value and
	 * {@link XMLSchema#DOUBLE} as parameters.
	 */
	@Override
	public Literal createLiteral(double value) {
		return createFPLiteral(value, XMLSchema.DOUBLE);
	}

	@Override
	public Literal createLiteral(BigInteger bigInteger) {
		return createIntegerLiteral(bigInteger, XMLSchema.INTEGER);
	}

	@Override
	public Literal createLiteral(BigDecimal bigDecimal) {
		return createNumericLiteral(bigDecimal, XMLSchema.DECIMAL);
	}

	/**
	 * Calls {@link #createNumericLiteral(Number, IRI)} with the supplied value
	 * and datatype as parameters.
	 */
	protected Literal createFPLiteral(Number value, IRI datatype) {
		return createNumericLiteral(value, datatype);
	}

	/**
	 * Creates specific optimized subtypes of SimpleLiteral for numeric
	 * datatypes.
	 */
	protected Literal createNumericLiteral(Number number, IRI datatype) {
		if (number instanceof BigDecimal) {
			return new DecimalLiteral((BigDecimal)number, datatype);
		}
		if (number instanceof BigInteger) {
			return new IntegerLiteral((BigInteger)number, datatype);
		}
		return new NumericLiteral(number, datatype);
	}

	/**
	 * Calls {@link ValueFactory#createLiteral(String, IRI)} with the
	 * String-value of the supplied calendar and the appropriate datatype as
	 * parameters.
	 * 
	 * @see XMLGregorianCalendar#toXMLFormat()
	 * @see XMLGregorianCalendar#getXMLSchemaType()
	 * @see XMLDatatypeUtil#qnameToURI(javax.xml.namespace.QName)
	 */
	@Override
	public Literal createLiteral(XMLGregorianCalendar calendar) {
		return createLiteral(calendar.toXMLFormat(), XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()));
	}

	/**
	 * Converts the supplied {@link Date} to a {@link XMLGregorianCalendar}, then
	 * calls {@link ValueFactory#createLiteral(XMLGregorianCalendar)}.
	 * 
	 * @since 2.7.0
	 */
	@Override
	public Literal createLiteral(Date date) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(date);

		XMLGregorianCalendar xmlGregCalendar = datatypeFactory.newXMLGregorianCalendar(c);
		return createLiteral(xmlGregCalendar);
	}
}
