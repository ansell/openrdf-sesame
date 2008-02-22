/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * A Facade to the five literal value tables. Which are labels, languages,
 * datatypes, numeric values, and dateTime values.
 * 
 * @author James Leigh
 * 
 */
public class LiteralTable {
	private static TimeZone Z = TimeZone.getTimeZone("GMT");

	public static long getCalendarValue(XMLGregorianCalendar xcal) {
		return xcal.toGregorianCalendar(Z, Locale.US, null).getTimeInMillis();
	}

	public interface LiteralHandler {
		public abstract void handleLiteral(long id, String label);

		public abstract void handleLiteral(long id, String label,
				String language);

		public abstract void handleLiteral(long id, String label, URI datatype);
	}

	private ValueTable labels;
	private ValueTable longLabels;
	private ValueTable languages;
	private ValueTable datatypes;
	private ValueTable numeric;
	private ValueTable dateTime;
	private String SELECT;
	private int version;

	public ValueTable getLabelTable() {
		return labels;
	}

	public void setLabelTable(ValueTable labels) {
		this.labels = labels;
	}

	public ValueTable getLongLabelTable() {
		return longLabels;
	}

	public void setLongLabelTable(ValueTable longLabels) {
		this.longLabels = longLabels;
	}

	public ValueTable getLanguageTable() {
		return languages;
	}

	public void setLanguageTable(ValueTable languages) {
		this.languages = languages;
	}

	public ValueTable getDatatypeTable() {
		return datatypes;
	}

	public void setDatatypeTable(ValueTable datatypes) {
		this.datatypes = datatypes;
	}

	public ValueTable getNumericTable() {
		return numeric;
	}

	public void setNumericTable(ValueTable numeric) {
		this.numeric = numeric;
	}

	public ValueTable getDateTimeTable() {
		return dateTime;
	}

	public void setDateTimeTable(ValueTable dateTime) {
		this.dateTime = dateTime;
	}

	public void initialize() throws SQLException {
		labels.initialize();
		longLabels.initialize();
		languages.initialize();
		datatypes.initialize();
		numeric.initialize();
		dateTime.initialize();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT labels.id, CASE WHEN labels.value IS NOT NULL THEN labels.value ELSE ll.value END, g.value, d.value\n");
		sb.append("FROM ").append(labels.getName()).append(" labels\n");
		sb.append(" LEFT JOIN ").append(longLabels.getName());
		sb.append(" ll ON (ll.id = labels.id)\n");
		sb.append(" LEFT JOIN ").append(languages.getName());
		sb.append(" g ON (g.id = labels.id)\n");
		sb.append(" LEFT JOIN ").append(datatypes.getName());
		sb.append(" d ON (d.id = labels.id)\n");
		sb.append("WHERE labels.value IN (");
		for (int i = 0, n = getSelectChunkSize(); i < n; i++) {
			sb.append("?,");
		}
		sb.setCharAt(sb.length() - 1, ')');
		SELECT = sb.toString();
	}

	public int getSelectChunkSize() {
		return labels.getSelectChunkSize();
	}

	public int getBatchSize() {
		return labels.getBatchSize();
	}

	public int getIdVersion() {
		return version;
	}

	public void insertSimple(long id, String label) throws SQLException {
		if (IdCode.decode(id).isLong()) {
			longLabels.insert(id, label);
		} else {
			labels.insert(id, label);
		}
	}

	public void insertLanguage(long id, String label, String language)
			throws SQLException {
		insertSimple(id, label);
		languages.insert(id, language);
	}

	public void insertDatatype(long id, String label, String datatype)
			throws SQLException {
		insertSimple(id, label);
		datatypes.insert(id, datatype);
	}

	public void insertNumeric(long id, String label, String datatype,
			double value) throws SQLException {
		labels.insert(id, label);
		datatypes.insert(id, datatype);
		numeric.insert(id, value);
	}

	public void insertDateTime(long id, String label, String datatype,
			long value) throws SQLException {
		labels.insert(id, label);
		datatypes.insert(id, datatype);
		dateTime.insert(id, value);
	}

	public void flush() throws SQLException {
		labels.flush();
		longLabels.flush();
		languages.flush();
		datatypes.flush();
		numeric.flush();
		dateTime.flush();
	}

	public void optimize() throws SQLException {
		labels.optimize();
		longLabels.optimize();
		languages.optimize();
		datatypes.optimize();
		numeric.optimize();
		dateTime.optimize();
	}

	public void load(Collection<? extends Literal> literals, LiteralHandler handler)
			throws SQLException {
		PreparedStatement stmt = prepareSelect();
		try {
			int p = 0;
			for (Literal lit : literals) {
				stmt.setString(++p, lit.getLabel());
				if (p < getSelectChunkSize())
					continue;
				importNeededIds(stmt, handler);
				p = 0;
			}
			if (p > 0) {
				while (p < getSelectChunkSize()) {
					stmt.setNull(++p, Types.VARCHAR);
				}
				importNeededIds(stmt, handler);
			}
		} finally {
			stmt.close();
		}
	}

	protected PreparedStatement prepareSelect() throws SQLException {
		return labels.prepareStatement(SELECT);
	}

	protected void importNeededIds(PreparedStatement stmt,
			LiteralHandler handler) throws SQLException {
		ResultSet rs = stmt.executeQuery();
		try {
			while (rs.next()) {
				long id = rs.getLong(1);
				String label = rs.getString(2);
				String language = rs.getString(3);
				String datatype = rs.getString(4);
				if (datatype != null) {
					handler.handleLiteral(id, label, new URIImpl(datatype));
				} else if (language != null) {
					handler.handleLiteral(id, label, language);
				} else {
					handler.handleLiteral(id, label);
				}
			}
		} finally {
			rs.close();
		}
	}

	public void removedStatements(int count, String condition)
			throws SQLException {
		boolean bool = false;
		bool |= labels.expungeRemovedStatements(count, condition);
		bool |= longLabels.expungeRemovedStatements(count, condition);
		bool |= languages.expungeRemovedStatements(count, condition);
		bool |= datatypes.expungeRemovedStatements(count, condition);
		bool |= numeric.expungeRemovedStatements(count, condition);
		bool |= dateTime.expungeRemovedStatements(count, condition);
		if (bool) {
			version++;
		}
	}
}
