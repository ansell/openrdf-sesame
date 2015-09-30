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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * A {@link Function} that tries to cast its argument to an
 * <tt>xsd:dateTime</tt>.
 * 
 * @author Arjohn Kampman
 */
public class DateTimeCast implements Function {

	public String getURI() {
		return XMLSchema.DATETIME.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length != 1) {
			throw new ValueExprEvaluationException("xsd:dateTime cast requires exactly 1 argument, got "
					+ args.length);
		}

		if (args[0] instanceof Literal) {
			Literal literal = (Literal)args[0];
			IRI datatype = literal.getDatatype();

			if (QueryEvaluationUtil.isStringLiteral(literal)) {
				String dateTimeValue = XMLDatatypeUtil.collapseWhiteSpace(literal.getLabel());
				if (XMLDatatypeUtil.isValidDateTime(dateTimeValue)) {
					return valueFactory.createLiteral(dateTimeValue, XMLSchema.DATETIME);
				}
			}
			else if (datatype != null) {
				if (datatype.equals(XMLSchema.DATETIME)) {
					return literal;
				}
				if (datatype.equals(XMLSchema.DATE)) {
					// If ST is xs:date, then let SYR be eg:convertYearToString(
					// fn:year-from-date( SV )), let SMO be eg:convertTo2CharString(
					// fn:month-from-date( SV )), let SDA be eg:convertTo2CharString(
					// fn:day-from-date( SV )) and let STZ be eg:convertTZtoString(
					// fn:timezone-from-date( SV )); TV is xs:dateTime( fn:concat(
					// SYR , '-', SMO , '-', SDA , 'T00:00:00 ', STZ ) ).
					try {
						XMLGregorianCalendar calValue = literal.calendarValue();

						int year = calValue.getYear();
						int month = calValue.getMonth();
						int day = calValue.getDay();
						int timezoneOffset = calValue.getTimezone();

						if (DatatypeConstants.FIELD_UNDEFINED != year && DatatypeConstants.FIELD_UNDEFINED != month
								&& DatatypeConstants.FIELD_UNDEFINED != day)
						{
							StringBuilder dtBuilder = new StringBuilder();
							dtBuilder.append(year);
							dtBuilder.append("-");
							if (month < 10) {
								dtBuilder.append("0");
							}
							dtBuilder.append(month);
							dtBuilder.append("-");
							if (day < 10) {
								dtBuilder.append("0");
							}
							dtBuilder.append(day);
							dtBuilder.append("T00:00:00");
							if (DatatypeConstants.FIELD_UNDEFINED != timezoneOffset) {
								int minutes = Math.abs(timezoneOffset);
								int hours = minutes / 60;
								minutes = minutes - (hours * 60);
								if (timezoneOffset > 0) {
									dtBuilder.append("+");
								}
								else {
									dtBuilder.append("-");
								}
								if (hours < 10) {
									dtBuilder.append("0");
								}
								dtBuilder.append(hours);
								dtBuilder.append(":");
								if (minutes < 10) {
									dtBuilder.append("0");
								}
								dtBuilder.append(minutes);
							}
							
							return valueFactory.createLiteral(dtBuilder.toString(), XMLSchema.DATETIME);
						}
						else {
							throw new ValueExprEvaluationException("not a valid date value: " + literal);
						}
					}
					catch (IllegalArgumentException e) {
						throw new ValueExprEvaluationException("not a valid calendar value: " + literal);
					}
				}
			}
		}

		throw new ValueExprEvaluationException("Invalid argument for xsd:dateTime cast: " + args[0]);
	}
}
