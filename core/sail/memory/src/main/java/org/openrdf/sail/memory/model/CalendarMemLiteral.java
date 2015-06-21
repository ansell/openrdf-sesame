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
package org.openrdf.sail.memory.model;

import java.io.IOException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.URI;
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
		this(creator, label, XMLDatatypeUtil.qnameToURI(calendar.getXMLSchemaType()), calendar);
	}

	public CalendarMemLiteral(Object creator, String label, URI datatype, XMLGregorianCalendar calendar) {
		super(creator, label, datatype);
		this.calendar = calendar;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public XMLGregorianCalendar calendarValue() {
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
