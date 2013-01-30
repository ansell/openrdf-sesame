/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.model.impl;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.datatypes.XMLDatatypeUtil;

/**
 * An extension of {@link LiteralImpl} that stores a calendar value to avoid
 * parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class CalendarLiteralImpl extends LiteralImpl {

	private static final long serialVersionUID = -8959671333074894312L;

	private final XMLGregorianCalendar calendar;

	/**
	 * Creates a literal for the specified calendar using a datatype appropriate
	 * for the value indicated by {@link XMLGregorianCalendar#getXMLSchemaType()}.
	 */
	public CalendarLiteralImpl(XMLGregorianCalendar calendar) {
		super(calendar.toXMLFormat(), XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()));
		this.calendar = calendar;
	}

	@Override
	public XMLGregorianCalendar calendarValue()
	{
		return calendar;
	}
}
