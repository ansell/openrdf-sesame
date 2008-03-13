/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.LiteralTable;

/**
 * Manages RDBMS Literals. Including creation, id lookup, and inserting them
 * into the database.
 * 
 * @author James Leigh
 * 
 */
public class LiteralManager extends ValueManagerBase<RdbmsLiteral> {
	private static TimeZone Z = TimeZone.getTimeZone("GMT");

	public static long getCalendarValue(XMLGregorianCalendar xcal) {
		return xcal.toGregorianCalendar(Z, Locale.US, null).getTimeInMillis();
	}

	public static LiteralManager instance;
	private LiteralTable table;

	public LiteralManager() {
		instance = this;
	}

	public void setTable(LiteralTable table) {
		this.table = table;
	}

	@Override
	public int getIdVersion() {
		return table.getIdVersion();
	}

	@Override
	public void close()
		throws SQLException
	{
		super.close();
		table.close();
	}

	@Override
	protected void optimize() throws SQLException {
		table.optimize();
	}

	@Override
	protected Literal key(RdbmsLiteral value) {
		return value;
	}

	@Override
	protected void insert(long id, RdbmsLiteral literal) throws SQLException, InterruptedException {
		long hash = IdCode.valueOf(literal).hash(literal);
		String label = literal.getLabel();
		String language = literal.getLanguage();
		URI datatype = literal.getDatatype();
		if (datatype == null && language == null) {
			table.insertSimple(id, hash, label);
		} else if (datatype == null) {
			table.insertLanguage(id, hash, label, language);
		} else {
			String dt = datatype.stringValue();
			try {
				if (XMLDatatypeUtil.isNumericDatatype(datatype)) {
					table.insertNumeric(id, hash, label, dt, literal.doubleValue());
				} else if (XMLDatatypeUtil.isCalendarDatatype(datatype)) {
					long value = getCalendarValue(literal.calendarValue());
					table.insertDateTime(id, hash, label, dt, value);
				} else {
					table.insertDatatype(id, hash, label, dt);
				}
			} catch (NumberFormatException e) {
				table.insertDatatype(id, hash, label, dt);
			} catch (IllegalArgumentException e) {
				table.insertDatatype(id, hash, label, dt);
			}
		}
	}

	@Override
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	@Override
	protected long getMissingId(RdbmsLiteral lit) {
		return IdCode.valueOf(lit).hash(lit);
	}

}
