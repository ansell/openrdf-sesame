package org.openrdf.query.algebra.evaluation.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

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

}
