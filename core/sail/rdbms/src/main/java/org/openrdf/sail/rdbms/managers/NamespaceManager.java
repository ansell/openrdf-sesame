/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.sail.rdbms.cursor.NamespaceCursor;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.schema.NamespacesTable;

/**
 * Manages the namespace prefixes.
 * 
 * @author James Leigh
 */
public class NamespaceManager {

	/** Namespaces records in database, they are never removed */
	private Set<String> namespaces = new HashSet<String>();

	/** prefix -> namespace **/
	private Map<String, String> map = new ConcurrentHashMap<String, String>();

	private Connection conn;

	private NamespacesTable table;

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void setNamespacesTable(NamespacesTable table) {
		this.table = table;
	}

	public void close()
		throws RdbmsException
	{
		try {
			conn.close();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public void initialize()
		throws RdbmsException
	{
		try {
			load();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	private void load()
		throws SQLException
	{
		synchronized (namespaces) {
			namespaces.clear();
			Map<String, String> p2n = new HashMap<String, String>();
			for (Object[] row : table.selectAll()) {
				String prefix = (String)row[0];
				String namespace = (String)row[1];
				if (namespace == null) {
					continue;
				}
				namespaces.add(namespace);
				if (prefix != null) {
					p2n.put(prefix, namespace);
				}
			}
			map.clear();
			map.putAll(p2n);
		}
	}

	public void setPrefix(String prefix, String name)
		throws RdbmsException
	{
		try {
			synchronized (namespaces) {
				if (!namespaces.contains(name)) {
					table.insert(prefix, name);
					namespaces.add(name);
					map.put(prefix, name);
					return;
				}
			}
			table.updatePrefix(prefix, name);
			map.put(prefix, name);
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public NamespaceImpl findByPrefix(String prefix) {
		String namespace = map.get(prefix);
		if (namespace == null) {
			return null;
		}
		return new NamespaceImpl(prefix, namespace);
	}

	public void removePrefix(String prefix)
		throws RdbmsException
	{
		try {
			String namespace = map.get(prefix);
			if (namespace != null) {
				table.updatePrefix(null, namespace);
				map.remove(prefix);
			}
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	public NamespaceCursor getNamespacesWithPrefix()
		throws RdbmsException
	{
		return new NamespaceCursor(map.entrySet().iterator());
	}

	public void clearPrefixes()
		throws RdbmsException
	{
		try {
			table.clearPrefixes();
			load();
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

}
