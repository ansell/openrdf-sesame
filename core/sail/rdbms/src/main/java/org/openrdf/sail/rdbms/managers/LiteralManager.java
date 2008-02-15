/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import static org.openrdf.sail.rdbms.schema.LiteralTable.getCalendarValue;

import java.sql.SQLException;
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.sail.rdbms.managers.base.ValueManagerBase;
import org.openrdf.sail.rdbms.model.RdbmsLiteral;
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

	public LiteralManager(LiteralTable table) {
		super("literals");
		this.table = table;
	}

	@Override
	public void flushTable() throws SQLException {
		table.flush();
	}

	@Override
	public int getIdVersion() {
		return table.getIdVersion();
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
	protected long nextId(RdbmsLiteral value) {
		return table.nextId(value);
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
