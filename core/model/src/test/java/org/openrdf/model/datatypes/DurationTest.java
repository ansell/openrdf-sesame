/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.datatypes;

import static org.openrdf.model.vocabulary.XMLSchema.DURATION;
import static org.openrdf.model.vocabulary.XMLSchema.DURATION_DAYTIME;
import static org.openrdf.model.vocabulary.XMLSchema.DURATION_YEARMONTH;

import javax.xml.datatype.Duration;

import junit.framework.TestCase;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * @author James Leigh
 */
public class DurationTest extends TestCase {

	public void testYearMonth()
		throws Exception
	{
		ValueFactory vf = new ValueFactoryImpl();
		Literal lit = vf.createLiteral("P1Y", DURATION_YEARMONTH);
		Duration duration = lit.durationValue();
		assertEquals(lit, vf.createLiteral(duration));
	}

	public void testDayTime()
		throws Exception
	{
		ValueFactory vf = new ValueFactoryImpl();
		Literal lit = vf.createLiteral("P1D", DURATION_DAYTIME);
		Duration duration = lit.durationValue();
		assertEquals(lit, vf.createLiteral(duration));
	}

	public void testFullDuration()
		throws Exception
	{
		ValueFactory vf = new ValueFactoryImpl();
		Literal lit = vf.createLiteral("P1Y1M1D", DURATION);
		Duration duration = lit.durationValue();
		assertEquals(lit, vf.createLiteral(duration));
	}
}
