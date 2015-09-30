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
package org.eclipse.rdf4j.query.algebra.evaluation.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.IntegerCast;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestIntegerCast {

	private IntegerCast ic;

	private ValueFactory f = new ValueFactoryImpl();

	@Before
	public void setUp()
		throws Exception
	{
		this.ic = new IntegerCast();
	}

	@After
	public void tearDown()
		throws Exception
	{
	}

	@Test
	public void testCastDouble() {
		Literal dbl = f.createLiteral(100.01d);
		try {
			Literal result = ic.evaluate(f, dbl);
			assertNotNull(result);
			assertEquals(XMLSchema.INTEGER, result.getDatatype());
			assertEquals(100, result.intValue());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testCastDoubleWithLargeFraction() {
		Literal dbl = f.createLiteral(100.987456d);
		try {
			Literal result = ic.evaluate(f, dbl);
			assertNotNull(result);
			assertEquals(XMLSchema.INTEGER, result.getDatatype());
			assertEquals(100, result.intValue());
		}
		catch (ValueExprEvaluationException e) {
			fail(e.getMessage());
		}
	}
}
