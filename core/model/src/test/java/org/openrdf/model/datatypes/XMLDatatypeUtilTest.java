/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.datatypes;

import static org.junit.Assert.fail;

import org.junit.Test;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Unit tests on {@link org.openrdf.model.datatypes.XMLDatatypeUtil}
 * 
 * @author Jeen Broekstra
 */
public class XMLDatatypeUtilTest {

	/** valid xsd:date values */
	private static final String[] VALID_DATES = {
			"2001-01-01",
			"2001-01-01Z",
			"2001-12-12+10:00",
			"-1800-06-06Z",
			"2004-02-29" // leap year
	};

	/** invalid xsd:date values */
	private static final String[] INVALID_DATES = {
			"foo",
			"Mon, 11 Jul 2005 +0200",
			"2001",
			"01",
			"2001-01",
			"2001-13-01",
			"2001-01-32",
			"2001-12-12+16:00",
			"2003-02-29" // not a leap year
	};

	/** valid xsd:gYear values */
	private static final String[] VALID_GYEAR = { "2001", "2001+02:00", "2001Z", "-2001" };

	/** invalid xsd:gYear values */
	private static final String[] INVALID_GYEAR = { "foo", "01", "2001-01", "2001-01-01" };

	/** valid xsd:gDay values */
	private static final String[] VALID_GDAY = { "---01", "---26Z", "---12-06:00", "---13+10:00" };

	/** invalid xsd:gDay values */
	private static final String[] INVALID_GDAY = {
			"01",
			"--01-",
			"2001-01",
			"foo",
			"---1",
			"---01+16:00",
			"---32" };

	/**
	 * Test method for
	 * {@link org.openrdf.model.datatypes.XMLDatatypeUtil#isValidValue(java.lang.String, org.openrdf.model.URI)}
	 * .
	 */
	@Test
	public void testIsValidValue() {
		testValidation(VALID_DATES, XMLSchema.DATE, true);
		testValidation(INVALID_DATES, XMLSchema.DATE, false);

		testValidation(VALID_GYEAR, XMLSchema.GYEAR, true);
		testValidation(INVALID_GYEAR, XMLSchema.GYEAR, false);

		testValidation(VALID_GDAY, XMLSchema.GDAY, true);
		testValidation(INVALID_GDAY, XMLSchema.GDAY, false);
	}

	private void testValidation(String[] values, URI datatype, boolean validValues) {
		for (String value : values) {
			boolean result = XMLDatatypeUtil.isValidValue(value, datatype);
			if (validValues) {
				if (!result) {
					fail("value " + value + " should have validated for type " + datatype);
				}
			}
			else {
				if (result) {
					fail("value " + value + " should not have validated for type " + datatype);
				}
			}
		}
	}
}
