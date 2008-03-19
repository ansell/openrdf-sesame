/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.mysql.alt;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.dbcp.SQLNestedException;

/**
 * 
 * @author James Leigh
 */
public class BatchInsertStatement extends DelegatingPreparedStatement {

	private static final int BUF_SIZE = 512 * 1024;

	public static String prepareSql(String table, String... columns) {
		StringBuilder sb = new StringBuilder();
		sb.append("LOAD DATA LOCAL INFILE ?");
		sb.append("\nINTO TABLE ").append(table);
		sb.append("\nCHARACTER SET utf8");
		sb.append("\nFIELDS TERMINATED BY ? ENCLOSED BY ? ESCAPED BY ?");
		sb.append("\nLINES TERMINATED BY ? STARTING BY ?");
		sb.append("\n(");
		for (String column : columns) {
			sb.append(column).append(',');
		}
		sb.setCharAt(sb.length() - 1, ')');
		return sb.toString();
	}

	private OutputStreamWriter writer;

	private Object[] record;

	private File file;

	private Pattern escape = Pattern.compile("([\\t\\n\\\\])");

	public BatchInsertStatement(PreparedStatement stmt, String table, String... columns)
		throws SQLException
	{
		super(new DelegatingConnection(stmt.getConnection()), stmt);
		record = new Object[columns.length];
		try {
			file = File.createTempFile(table, ".mysql");
			super.setString(1, file.getAbsolutePath());
			super.setString(2, "\t");
			super.setString(3, "");
			super.setString(4, "\\");
			super.setString(5, "\n");
			super.setString(6, "");
			OutputStream out = new BufferedOutputStream(new FileOutputStream(file), BUF_SIZE);
			writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
		}
		catch (IOException e) {
			throw new SQLNestedException(e.toString(), e);
		}
	}

	@Override
	public boolean isClosed() {
		return super.isClosed();
	}

	@Override
	public void setObject(int idx, Object obj)
		throws SQLException
	{
		record[idx - 1] = obj;
	}

	@Override
	public void addBatch()
		throws SQLException
	{
		try {
			for (int i = 0; i < record.length; i++) {
				if (i > 0) {
					writer.write('\t');
				}
				if (record[i] instanceof Number) {
					writer.write(record[i].toString());
				} else {
					Matcher matcher = escape.matcher(record[i].toString());
					writer.write(matcher.replaceAll("\\$1"));
				}
			}
			writer.write('\n');
		}
		catch (IOException e) {
			throw new SQLNestedException(e.toString(), e);
		}
	}

	@Override
	public int[] executeBatch()
		throws SQLException
	{
		try {
			writer.flush();
			writer.close();
			writer = null;
			return new int[] { super.executeUpdate() };
		}
		catch (IOException e) {
			throw new SQLNestedException(e.toString(), e);
		} finally {
			file.delete();
			file = null;
		}
	}

	@Override
	protected void finalize()
		throws Throwable
	{
		if (writer != null) {
			writer.close();
		}
		if (file != null) {
			file.delete();
		}
	}

}
