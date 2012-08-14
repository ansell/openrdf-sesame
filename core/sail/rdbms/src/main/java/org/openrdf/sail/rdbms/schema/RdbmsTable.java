/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents and controls the underlying database table.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsTable {

	public static int total_opt;

	public static int MAX_DELTA_TO_FORCE_OPTIMIZE = 10000;

	private static final String[] TYPE_TABLE = new String[] { "TABLE" };

	private Logger logger = LoggerFactory.getLogger(RdbmsTable.class);

	private int addedCount;

	private Connection conn;

	private String name;

	private int removedCount;

	private long rowCount;

	private PreparedStatement clear;

	public RdbmsTable(String name) {
		super();
		this.name = name;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void close()
		throws SQLException
	{
		if (clear != null) {
			clear.close();
		}
	}

	public long size() {
		assert rowCount >= 0 : rowCount;
		return rowCount;
	}

	public void clear()
		throws SQLException
	{
		if (clear == null) {
			clear = conn.prepareStatement(buildClear());
		}
		clear.execute();
		rowCount = 0;
	}

	public void createTable(CharSequence columns)
		throws SQLException
	{
		execute(buildCreateTable(columns));
		rowCount = 0;
	}

	public void createTransactionalTable(CharSequence columns)
		throws SQLException
	{
		execute(buildCreateTransactionalTable(columns));
		rowCount = 0;
	}

	public void createTemporaryTable(CharSequence columns)
		throws SQLException
	{
		try {
			execute(buildCreateTemporaryTable(columns));
		}
		catch (SQLException e) {
			// must already exist
		}
	}

	public void execute(String command)
		throws SQLException
	{
		if (command != null) {
			Statement st = conn.createStatement();
			try {
				st.execute(command);
			}
			catch (SQLException e) {
				logger.warn(e.getMessage() + '\n' + command);
				throw e;
			}
			finally {
				st.close();
			}
		}
	}

	public int executeUpdate(String command, String... parameters)
		throws SQLException
	{
		PreparedStatement st = conn.prepareStatement(command);
		try {
			for (int i = 0; i < parameters.length; i++) {
				if (parameters[i] == null) {
					st.setNull(i + 1, Types.VARCHAR);
				}
				else {
					st.setString(i + 1, parameters[i]);
				}
			}
			return st.executeUpdate();
		}
		catch (SQLException e) {
			logger.warn(e.getMessage() + '\n' + command);
			throw e;
		}
		finally {
			st.close();
		}
	}

	public String getCatalog() {
		return null;
	}

	public String getName() {
		return name;
	}

	public String getSchema() {
		return null;
	}

	public void primaryIndex(String... columns)
		throws SQLException
	{
		execute(buildPrimaryIndex(columns));
	}

	public void index(String... columns)
		throws SQLException
	{
		if (columns.length == 1 && columns[0].equalsIgnoreCase("value")
				&& getName().toUpperCase().contains("LONG_"))
		{
			execute(buildLongIndex(columns));
		}
		else {
			execute(buildIndex(columns));
		}
	}

	public void dropIndex(String name)
		throws SQLException
	{
		execute(buildDropIndex(name));
	}

	public boolean isCreated()
		throws SQLException
	{
		DatabaseMetaData metaData = conn.getMetaData();
		String c = getCatalog();
		String s = getSchema();
		String n = getName();
		ResultSet tables = metaData.getTables(c, s, n, TYPE_TABLE);
		try {
			return tables.next();
		}
		finally {
			tables.close();
		}
	}

	public Map<String, List<String>> getIndexes()
		throws SQLException
	{
		DatabaseMetaData metaData = conn.getMetaData();
		String c = getCatalog();
		String s = getSchema();
		String n = getName();
		ResultSet indexes = metaData.getIndexInfo(c, s, n, false, false);
		try {
			Map<String, List<String>> names = new HashMap<String, List<String>>();
			while (indexes.next()) {
				String index = indexes.getString(6);
				String column = indexes.getString(9);
				List<String> columns = names.get(index);
				if (columns == null) {
					names.put(index, columns = new ArrayList<String>());
				}
				columns.add(column);
			}
			return names;
		}
		finally {
			indexes.close();
		}
	}

	public long count()
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*)\n");
		sb.append("FROM ").append(name);
		String query = sb.toString();
		Statement st = conn.createStatement();
		try {
			ResultSet rs = st.executeQuery(query);
			try {
				if (rs.next()) {
					rowCount = rs.getLong(1);
					assert rowCount >= 0 : rowCount;
					return rowCount;
				}
				return 0;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	public void modified(int inserted, int deleted)
		throws SQLException
	{
		if (inserted < 1 && deleted < 1)
			return;
		addedCount += inserted;
		removedCount += deleted;
		rowCount += inserted - deleted;
		assert rowCount >= 0 : rowCount;
	}

	public void optimize()
		throws SQLException
	{
		if (optimize(addedCount + removedCount, rowCount)) {
			execute(buildOptimize());
			addedCount = removedCount = 0;
			total_opt += 1;
		}
	}

	public PreparedStatement prepareStatement(String sql)
		throws SQLException
	{
		return conn.prepareStatement(sql);
	}

	public void rollback()
		throws SQLException
	{
		conn.rollback();
	}

	public List<Object[]> select(String... columns)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		for (String column : columns) {
			if (sb.length() == 0) {
				sb.append("SELECT ");
			}
			else {
				sb.append(", ");
			}
			sb.append(column);
		}
		sb.append("\nFROM ").append(name);
		String query = sb.toString();
		List<Object[]> result = new ArrayList<Object[]>();
		Statement st = conn.createStatement();
		try {
			ResultSet rs = st.executeQuery(query);
			try {
				int columnCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					Object[] row = new Object[columnCount];
					for (int i = 0; i < row.length; i++) {
						row[i] = rs.getObject(i + 1);
					}
					result.add(row);
				}
				rowCount = result.size();
				assert rowCount >= 0 : rowCount;
				return result;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	public int[] aggregate(String... expressions)
		throws SQLException
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(*)");
		for (String expression : expressions) {
			sb.append(", ").append(expression);
		}
		sb.append("\nFROM ").append(name);
		String query = sb.toString();
		Statement st = conn.createStatement();
		try {
			ResultSet rs = st.executeQuery(query);
			try {
				if (!rs.next())
					throw new AssertionError();
				int columnCount = rs.getMetaData().getColumnCount();
				int[] result = new int[columnCount - 1];
				for (int i = 0; i < result.length; i++) {
					result[i] = rs.getInt(i + 2);
				}
				rowCount = rs.getLong(1);
				assert rowCount >= 0 : rowCount;
				return result;
			}
			finally {
				rs.close();
			}
		}
		finally {
			st.close();
		}
	}

	protected boolean optimize(int delta, long rowCount) {
		if (delta > MAX_DELTA_TO_FORCE_OPTIMIZE)
			return true;
		return delta != 0 && rowCount / delta <= 2;
	}

	protected String buildClear() {
		return "DELETE FROM " + name;
	}

	protected String buildCreateTable(CharSequence columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(name);
		sb.append(" (\n").append(columns).append(")");
		return sb.toString();
	}

	protected String buildCreateTransactionalTable(CharSequence columns) {
		return buildCreateTable(columns);
	}

	protected String buildCreateTemporaryTable(CharSequence columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TEMPORARY TABLE ").append(name);
		sb.append(" (\n").append(columns).append(")");
		return sb.toString();
	}

	protected String buildPrimaryIndex(String... columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE ");
		sb.append(getName());
		sb.append(" ADD PRIMARY KEY (");
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(columns[i]);
		}
		sb.append(")");
		return sb.toString();
	}

	protected String buildIndex(String... columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE INDEX ").append(buildIndexName(columns));
		sb.append(" ON ").append(name).append(" (");
		for (int i = 0; i < columns.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(columns[i]);
		}
		sb.append(")");
		return sb.toString();
	}

	protected String buildLongIndex(String... columns) {
		return buildIndex(columns);
	}

	protected String buildDropIndex(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP INDEX ").append(name);
		return sb.toString();
	}

	/**
	 * Creates an index name based on the name of the columns and table that it's
	 * supposed to index.
	 */
	protected String buildIndexName(String... columns) {
		StringBuffer sb = new StringBuffer(32);
		sb.append(getName()).append("_").append(columns[0]);
		for (int i = 1; i < columns.length; i++) {
			sb.append("_").append(columns[i]);
		}
		sb.append("_idx");
		return sb.toString();
	}

	protected String buildOptimize()
		throws SQLException
	{
		// There is no default for this in SQL92.
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RdbmsTable))
			return false;
		final RdbmsTable other = (RdbmsTable)obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	public void drop()
		throws SQLException
	{
		execute("DROP TABLE " + name);
	}

	@Override
	public String toString() {
		return getName();
	}
}
