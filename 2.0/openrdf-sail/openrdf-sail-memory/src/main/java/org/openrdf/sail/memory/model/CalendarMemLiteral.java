/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory.model;

import java.io.IOException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.datatypes.XMLDatatypeUtil;

/**
 * An extension of MemLiteral that stores a Calendar value to avoid parsing.
 * 
 * @author David Huynh
 * @author Arjohn Kampman
 */
public class CalendarMemLiteral extends MemLiteral {

	private static final long serialVersionUID = -7903843639313451580L;

	/*-----------*
	 * Variables *
	 *-----------*/

	transient private XMLGregorianCalendar calendar;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CalendarMemLiteral(Object creator, XMLGregorianCalendar calendar) {
		this(creator, calendar.toXMLFormat(), calendar);
	}

	public CalendarMemLiteral(Object creator, String label, XMLGregorianCalendar calendar) {
		super(creator, label, XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()));
		this.calendar = calendar;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public XMLGregorianCalendar calendarValue()
	{
		return calendar;
	}

	private void readObject(java.io.ObjectInputStream in)
		throws IOException
	{
		try {
			in.defaultReadObject();
			calendar = XMLDatatypeUtil.parseCalendar(this.getLabel());
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}
	}
}
