/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.function.datetime;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.evaluation.function.Function;

/**
 * The SPARQL built-in {@link Function} NOW, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-now">SPARQL Query Language
 * for RDF</a>
 * 
 * @author Jeen Broekstra
 */
public class Now implements Function {

	public String getURI() {
		return "NOW";
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 0) {
			throw new ValueExprEvaluationException("NOW requires 0 argument, got " + args.length);
		}
		
		Calendar cal = Calendar.getInstance();
		
		Date now = cal.getTime();
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(now);
		try {
			XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			
			return valueFactory.createLiteral(date);
		}
		catch (DatatypeConfigurationException e) {
			throw new ValueExprEvaluationException(e);
		}


	}

}
