/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import static org.openrdf.model.datatypes.XMLDatatypeUtil.isCalendarDatatype;
import static org.openrdf.model.datatypes.XMLDatatypeUtil.isNumericDatatype;
import static org.openrdf.sail.rdbms.schema.LiteralTable.getCalendarValue;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
import org.openrdf.sail.rdbms.schema.IdCode;
import org.openrdf.sail.rdbms.schema.LiteralTable;
import org.openrdf.sail.rdbms.schema.LiteralTable.LiteralHandler;

/**
 * Manages RDBMS Literals. Including creation, id lookup, and inserting them
 * into the database.
 * 
 * @author James Leigh
 * 
 */
public class LiteralManager extends ValueManagerBase<Literal, RdbmsLiteral> {
	private LiteralTable table;

	public LiteralManager(Lock idLock, LiteralTable table) {
		super(idLock);
		this.table = table;
	}

	@Override
	public int getIdVersion() {
		return table.getIdVersion();
	}

	@Override
	protected void flushTable() throws SQLException {
		table.flush();
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
	protected void insert(long id, RdbmsLiteral literal) throws SQLException {
		String label = literal.getLabel();
		String language = literal.getLanguage();
		URI datatype = literal.getDatatype();
		if (datatype == null && language == null) {
			table.insertSimple(id, label);
		} else if (datatype == null) {
			table.insertLanguage(id, label, language);
		} else {
			String dt = datatype.stringValue();
			try {
				if (XMLDatatypeUtil.isNumericDatatype(datatype)) {
					table.insertNumeric(id, label, dt, literal.doubleValue());
				} else if (XMLDatatypeUtil.isCalendarDatatype(datatype)) {
					long value = getCalendarValue(literal.calendarValue());
					table.insertDateTime(id, label, dt, value);
				} else {
					table.insertDatatype(id, label, dt);
				}
			} catch (NumberFormatException e) {
				table.insertDatatype(id, label, dt);
			} catch (IllegalArgumentException e) {
				table.insertDatatype(id, label, dt);
			}
		}
	}

	@Override
	protected int getBatchSize() {
		return table.getBatchSize();
	}

	@Override
	protected int getSelectChunkSize() {
		return table.getSelectChunkSize();
	}

	@Override
	protected long getMissingId(RdbmsLiteral lit) {
		String lang = lit.getLanguage();
		URI dt = lit.getDatatype();
		int length = lit.stringValue().length();
		if (lang != null) {
			// language
			if (length > IdCode.LONG)
				return getMissingId(lit, IdCode.LANG_LONG);
			return getMissingId(lit, IdCode.LANG);
		}
		if (dt == null) {
			// simple
			if (length > IdCode.LONG)
				return getMissingId(lit, IdCode.SIMPLE_LONG);
			return getMissingId(lit, IdCode.SIMPLE);
		}
		if (isNumericDatatype(dt))
			return getMissingId(lit, IdCode.NUMERIC);
		if (isCalendarDatatype(dt)) {
			// calendar
			if (isZoned(lit))
				return getMissingId(lit, IdCode.DATETIME_ZONED);
			return getMissingId(lit, IdCode.DATETIME);
		}
		if (RDF.XMLLITERAL.equals(dt))
			return getMissingId(lit, IdCode.XML);
		if (length > IdCode.LONG)
			return getMissingId(lit, IdCode.TYPED_LONG);
		return getMissingId(lit, IdCode.TYPED);
	
	}

	/**
	 * @param lang_long
	 * @return
	 */
	private long getMissingId(RdbmsLiteral lit, IdCode code) {
		if (code.isTypedLiteral())
			return code.getId(lit.getDatatype().stringValue(), lit.getLabel());
		if (code.isLanguageLiteral())
			return code.getId(lit.getLanguage(), lit.getLabel());
		return code.getId(lit.getLabel());
	}

	public static boolean isZoned(Literal lit) {
		String stringValue = lit.stringValue();
		int length = stringValue.length();
		if (length < 1)
			return false;
		if (stringValue.charAt(length - 1) == 'Z')
			return true;
		if (length < 6)
			return false;
		if (stringValue.charAt(length - 3) != ':')
			return false;
		char chr = stringValue.charAt(length - 6);
		return chr == '+' || chr == '-';
	}

	@Override
	protected void loadIds(final Map<Literal, RdbmsLiteral> needIds)
			throws SQLException {
		final int version = table.getIdVersion();
		LiteralHandler handler = new LiteralHandler() {
			public void handleLiteral(long id, String label) {
				RdbmsLiteral lit = needIds.get(new LiteralImpl(label));
				if (lit != null) {
					lit.setInternalId(id);
					lit.setVersion(version);
				}
			}

			public void handleLiteral(long id, String label, String language) {
				RdbmsLiteral lit = needIds
						.get(new LiteralImpl(label, language));
				if (lit != null) {
					lit.setInternalId(id);
					lit.setVersion(version);
				}
			}

			public void handleLiteral(long id, String label, URI datatype) {
				RdbmsLiteral lit = needIds
						.get(new LiteralImpl(label, datatype));
				if (lit != null) {
					lit.setInternalId(id);
					lit.setVersion(version);
				}
			}
		};
		table.load(needIds.values(), handler);
	}

}
