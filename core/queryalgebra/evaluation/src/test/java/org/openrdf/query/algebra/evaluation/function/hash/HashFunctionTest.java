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
package org.openrdf.query.algebra.evaluation.function.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

/**
 * @author jeen
 */
public abstract class HashFunctionTest {

	private HashFunction hashFunction;

	private String toHash;

	private String expectedDigest;

	private ValueFactory f = SimpleValueFactory.getInstance();

	@Test
	public void testEvaluate() {
		try {
			Literal hash = getHashFunction().evaluate(f, f.createLiteral(getToHash()));

			assertNotNull(hash);
			assertEquals(XMLSchema.STRING, hash.getDatatype());

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
			assertEquals(XMLSchema.STRING, hash.getDatatype());

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
