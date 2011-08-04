/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public abstract class HashFunctionTest {

	private HashFunction hashFunction;

	private String toHash;

	private String expectedDigest;

	private ValueFactory f = new ValueFactoryImpl();

	@Test
	public void testEvaluate() {
		try {
			Literal hash = getHashFunction().evaluate(f, f.createLiteral(getToHash()));

			assertNotNull(hash);
			assertNull(hash.getDatatype());

			assertEquals(hash.getLabel(), getExpectedDigest());
		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluate2() {
		try {
			Literal hash = getHashFunction().evaluate(f, f.createLiteral(getToHash(), XMLSchema.STRING));

			assertNotNull(hash);
			assertNull(hash.getDatatype());

			assertEquals(hash.getLabel(), getExpectedDigest());
		}
		catch (ValueExprEvaluationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testEvaluate3() {
		try {
			getHashFunction().evaluate(f, f.createLiteral("4", XMLSchema.INTEGER));

			fail("incompatible operand should have resulted in type error.");
		}
		catch (ValueExprEvaluationException e) {
			// do nothing, expected
		}
	}

	/**
	 * @param hashFunction
	 *        The hashFunction to set.
	 */
	public void setHashFunction(HashFunction hashFunction) {
		this.hashFunction = hashFunction;
	}

	/**
	 * @return Returns the hashFunction.
	 */
	public HashFunction getHashFunction() {
		return hashFunction;
	}

	/**
	 * @param expectedDigest
	 *        The expectedDigest to set.
	 */
	public void setExpectedDigest(String expectedDigest) {
		this.expectedDigest = expectedDigest;
	}

	/**
	 * @return Returns the expectedDigest.
	 */
	public String getExpectedDigest() {
		return expectedDigest;
	}

	/**
	 * @param toHash
	 *        The toHash to set.
	 */
	public void setToHash(String toHash) {
		this.toHash = toHash;
	}

	/**
	 * @return Returns the toHash.
	 */
	public String getToHash() {
		return toHash;
	}
}
