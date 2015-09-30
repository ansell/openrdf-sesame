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
package org.eclipse.rdf4j.query.algebra.evaluation.util;

import static org.eclipse.rdf4j.query.algebra.Compare.CompareOp.*;
import static org.junit.Assert.*;

import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.Compare.CompareOp;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jeen Broekstra
 */
public class QueryEvaluationUtilTest {

	private ValueFactory f = SimpleValueFactory.getInstance();

	private Literal arg1simple;

	private Literal arg2simple;

	private Literal arg1en;

	private Literal arg2en;

	private Literal arg1cy;

	private Literal arg2cy;

	private Literal arg1string;

	private Literal arg2string;

	private Literal arg1int;

	private Literal arg2int;

	private Literal arg1year;

	private Literal arg2year;

	@Before
	public void setUp()
		throws Exception
	{
		arg1simple = f.createLiteral("abc");
		arg2simple = f.createLiteral("b");

		arg1en = f.createLiteral("abc", "en");
		arg2en = f.createLiteral("b", "en");

		arg1cy = f.createLiteral("abc", "cy");
		arg2cy = f.createLiteral("b", "cy");

		arg1string = f.createLiteral("abc", XMLSchema.STRING);
		arg2string = f.createLiteral("b", XMLSchema.STRING);

		arg1year = f.createLiteral("2007", XMLSchema.GYEAR);
		arg2year = f.createLiteral("2009", XMLSchema.GYEAR);

		arg1int = f.createLiteral(10);
		arg2int = f.createLiteral(1);
	}

	@Test
	public void testCompatibleArguments()
		throws Exception
	{

		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1simple, arg2int));

		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1en, arg2simple));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1en, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg2en, arg2cy));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1en, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1en, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1en, arg2int));

		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg2cy, arg2en));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1cy, arg2int));

		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1string, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1string, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1string, arg2cy));
		assertTrue(QueryEvaluationUtil.compatibleArguments(arg1string, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1string, arg2int));

		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2simple));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2en));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2cy));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2string));
		assertFalse(QueryEvaluationUtil.compatibleArguments(arg1int, arg2int));

	}

	@Test
	public void testCompareEQ()
		throws Exception
	{
		assertCompareFalse(arg1simple, arg2simple, EQ);
		assertCompareFalse(arg1simple, arg2en, EQ);
		assertCompareFalse(arg1simple, arg2cy, EQ);
		assertCompareFalse(arg1simple, arg2string, EQ);
		assertCompareException(arg1simple, arg2int, EQ);
		assertCompareException(arg1simple, arg2year, EQ);

		assertCompareFalse(arg1en, arg2simple, EQ);
		assertCompareFalse(arg1en, arg2en, EQ);
		assertCompareFalse(arg2en, arg2cy, EQ);
		assertCompareFalse(arg1en, arg2cy, EQ);
		assertCompareFalse(arg1en, arg2string, EQ);
		assertCompareFalse(arg1en, arg2int, EQ);

		assertCompareFalse(arg1cy, arg2simple, EQ);
		assertCompareFalse(arg1cy, arg2en, EQ);
		assertCompareFalse(arg2cy, arg2en, EQ);
		assertCompareFalse(arg1cy, arg2cy, EQ);
		assertCompareFalse(arg1cy, arg2string, EQ);
		assertCompareFalse(arg1cy, arg2int, EQ);

		assertCompareFalse(arg1string, arg2simple, EQ);
		assertCompareFalse(arg1string, arg2en, EQ);
		assertCompareFalse(arg1string, arg2cy, EQ);
		assertCompareFalse(arg1string, arg2string, EQ);
		assertCompareException(arg1string, arg2int, EQ);
		assertCompareException(arg1string, arg2year, EQ);

		assertCompareException(arg1int, arg2simple, EQ);
		assertCompareFalse(arg1int, arg2en, EQ);
		assertCompareFalse(arg1int, arg2cy, EQ);
		assertCompareException(arg1int, arg2string, EQ);
		assertCompareFalse(arg1int, arg2int, EQ);
		assertCompareException(arg1int, arg2year, EQ);

	}

	@Test
	public void testCompareNE()
		throws Exception
	{
		assertCompareTrue(arg1simple, arg2simple, NE);
		assertCompareTrue(arg1simple, arg2en, NE);
		assertCompareTrue(arg1simple, arg2cy, NE);
		assertCompareTrue(arg1simple, arg2string, NE);
		assertCompareException(arg1simple, arg2int, NE);
		assertCompareException(arg1simple, arg2year, NE);

		assertCompareTrue(arg1en, arg2simple, NE);
		assertCompareTrue(arg1en, arg2en, NE);
		assertCompareTrue(arg2en, arg2cy, NE);
		assertCompareTrue(arg1en, arg2cy, NE);
		assertCompareTrue(arg1en, arg2string, NE);
		assertCompareTrue(arg1en, arg2int, NE);

		assertCompareTrue(arg1cy, arg2simple, NE);
		assertCompareTrue(arg1cy, arg2en, NE);
		assertCompareTrue(arg2cy, arg2en, NE);
		assertCompareTrue(arg1cy, arg2cy, NE);
		assertCompareTrue(arg1cy, arg2string, NE);
		assertCompareTrue(arg1cy, arg2int, NE);

		assertCompareTrue(arg1string, arg2simple, NE);
		assertCompareTrue(arg1string, arg2en, NE);
		assertCompareTrue(arg1string, arg2cy, NE);
		assertCompareTrue(arg1string, arg2string, NE);
		assertCompareException(arg1string, arg2int, NE);
		assertCompareException(arg1string, arg2year, NE);

		assertCompareException(arg1int, arg2simple, NE);
		assertCompareTrue(arg1int, arg2en, NE);
		assertCompareTrue(arg1int, arg2cy, NE);
		assertCompareException(arg1int, arg2string, NE);
		assertCompareTrue(arg1int, arg2int, NE);
		assertCompareException(arg1int, arg2year, NE);

	}

	/**
	 * Assert that there is an exception as a result of comparing the two
	 * literals with the given operator.
	 * 
	 * @param lit1
	 *        The left literal
	 * @param lit2
	 *        The right literal
	 * @param op
	 *        The operator for the comparison
	 */
	private void assertCompareException(Literal lit1, Literal lit2, CompareOp op)
		throws Exception
	{
		try {
			boolean returnValue = QueryEvaluationUtil.compareLiterals(lit1, lit2, op);
			fail("Did not receive expected ValueExprEvaluationException (return value was " + returnValue
					+ ") for " + lit1.toString() + op.getSymbol() + lit2.toString());
		}
		catch (ValueExprEvaluationException e) {
			// Expected exception
		}
	}

	/**
	 * Assert that there is no exception as a result of comparing the two
	 * literals with the given operator and it returns false.
	 * 
	 * @param lit1
	 *        The left literal
	 * @param lit2
	 *        The right literal
	 * @param op
	 *        The operator for the comparison
	 */
	private void assertCompareFalse(Literal lit1, Literal lit2, CompareOp op)
		throws Exception
	{
		assertFalse("Compare did not return false for " + lit1.toString() + op.getSymbol() + lit2.toString(),
				QueryEvaluationUtil.compareLiterals(lit1, lit2, op));
	}

	/**
	 * Assert that there is no exception as a result of comparing the two
	 * literals with the given operator and it returns true.
	 * 
	 * @param lit1
	 *        The left literal
	 * @param lit2
	 *        The right literal
	 * @param op
	 *        The operator for the comparison
	 */
	private void assertCompareTrue(Literal lit1, Literal lit2, CompareOp op)
		throws Exception
	{
		assertTrue("Compare did not return true for " + lit1.toString() + op.getSymbol() + lit2.toString(),
				QueryEvaluationUtil.compareLiterals(lit1, lit2, op));
	}

}
