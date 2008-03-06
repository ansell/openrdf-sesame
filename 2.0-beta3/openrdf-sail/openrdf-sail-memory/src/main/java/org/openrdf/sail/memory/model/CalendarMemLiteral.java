/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.datatypes.XMLDatatypeUtil;

/**
 * An extension of MemLiteral that stores a Calendar value to avoid parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class CalendarMemLiteral extends MemLiteral {

	/*-----------*
	 * Variables *
	 *-----------*/

	private XMLGregorianCalendar _calendar;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CalendarMemLiteral(Object creator, XMLGregorianCalendar calendar) {
		super(creator, calendar.toXMLFormat(), XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()));
		_calendar = calendar;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public XMLGregorianCalendar calendarValue()
	{
		return _calendar;
	}
}
