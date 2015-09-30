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
package org.eclipse.rdf4j.model.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Extends the Apache Commons Collections test, {@link AbstractTestSet} to
 * enable testing of the OpenRDF Model collection implementations.
 */
public abstract class ApacheSetTestCase extends AbstractTestSet {

	private ValueFactory vf = SimpleValueFactory.getInstance();

	public ApacheSetTestCase(String name) {
		super(name);
	}

	/**
	 * Tests whether the set that is under test is equal to a confirmed set.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void testSetEquals() {
		resetEmpty();
		assertEquals("Empty sets should be equal", getSet(), getConfirmedSet());
		verify();

		Collection set2 = makeConfirmedCollection();
		set2.add(getOneElement());
		assertFalse("Empty set shouldn't equal nonempty set", getSet().equals(set2));

		resetFull();
		assertEquals("Full sets should be equal", getSet(), getConfirmedSet());
		verify();

		set2.clear();
		set2.addAll(Arrays.asList(getOtherElements()));
		assertFalse("Sets with different contents shouldn't be equal", getSet().equals(set2));
	}

	/**
	 * @return A single element that can be added to the collection under test.
	 */
	public abstract Object getOneElement();

	/**
	 * Override this method to indicate that null's are not supported by Model
	 * implementations.
	 */
	@Override
	public boolean isNullSupported() {
		return false;
	}

	/**
	 * Makes an empty set. The returned set should have no elements.
	 * 
	 * @return an empty set
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public abstract Set makeEmptySet();

	/**
	 * Returns an empty Set for use in modification testing.
	 * 
	 * @return a confirmed empty collection
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public abstract Collection makeConfirmedCollection();

	/**
	 * Converts the standard list of elements returned by the super class
	 * implementation of {@link AbstractTestCollection#getFullNonNullElements()}
	 * into a set of objects suitable for insertion into a {@link Model}.
	 * 
	 * @return A set of non-null elements that will be returned by
	 *         {@link #getFullElements()}.
	 */
	@Override
	public Object[] getFullNonNullElements() {
		return convert(super.getFullNonNullElements());
	}

	/**
	 * Converts the standard list of elements returned by the super class
	 * implementation of {@link AbstractTestCollection#getFullNonNullElements()}
	 * into a set of objects suitable for insertion into a {@link Model}.
	 * <p>
	 * IMPORTANT: These elements must not be present in the results of
	 * {@link #getFullNonNullElements()}.
	 * 
	 * @return A set of non-null elements that will be returned by
	 *         {@link #getOtherElements()}.
	 */
	@Override
	public Object[] getOtherNonNullElements() {
		return convert(super.getOtherNonNullElements());
	}

	/**
	 * A method that must be overridden to generate a set of non-null elements
	 * using the given seets.
	 * 
	 * @param seeds
	 * @return
	 */
	public abstract Object[] convert(Object[] seeds);

	/**
	 * Creates a URI using the given seed in both the prefix and the suffix of
	 * the URI.
	 * 
	 * @param seed
	 *        The object to use as a value to create a {@link IRI}.
	 * @return A URI based on the value of the seed parameter.
	 */
	public IRI createURI(Object seed) {
		String prefix = "urn:test:" + seed.getClass().getSimpleName() + ":";
		if (seed instanceof Number)
			return vf.createIRI(prefix + ((Number)seed).intValue());
		if (seed instanceof Character)
			return vf.createIRI(prefix + ((Character)seed).hashCode());
		return vf.createIRI(prefix + seed.toString());
	}

	/**
	 * Creates a literal using the value of the seed.
	 * 
	 * @param seed
	 *        The object to use as a value to create a {@link Literal}.
	 * @return A literal based on the value of the seed parameter.
	 */
	public Literal createLiteral(Object seed) {
		if (seed instanceof Integer)
			return vf.createLiteral((Integer)seed);
		if (seed instanceof Double)
			return vf.createLiteral((Double)seed);
		if (seed instanceof Long)
			return vf.createLiteral((Long)seed);
		if (seed instanceof Short)
			return vf.createLiteral((Short)seed);
		if (seed instanceof Byte)
			return vf.createLiteral((Byte)seed);
		if (seed instanceof Float)
			return vf.createLiteral((Float)seed);
		if (seed instanceof Number)
			return vf.createLiteral(((Number)seed).intValue());
		if (seed instanceof Character)
			return vf.createLiteral(true);
		return vf.createLiteral(seed.toString());
	}

}
