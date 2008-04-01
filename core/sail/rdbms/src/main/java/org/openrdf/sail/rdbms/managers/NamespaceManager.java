/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.schema.NamespacesTable;

/**
 * Manages the namespace prefixes.
 * 
 * @author James Leigh
 * 
 */
public class NamespaceManager {

	private Map<String, NamespaceImpl> byNamespace = new ConcurrentHashMap<String, NamespaceImpl>();

	private Map<String, NamespaceImpl> byPrefix = new ConcurrentHashMap<String, NamespaceImpl>();

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

	public NamespaceImpl findNamespace(String namespace)
		throws RdbmsException
	{
		if (namespace == null)
			return null;
		if (byNamespace.containsKey(namespace))
			return byNamespace.get(namespace);
		try {
			return create(namespace);
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
	}

	private void load()
		throws SQLException
	{
		Map<String, NamespaceImpl> map = new HashMap<String, NamespaceImpl>();
		Map<String, NamespaceImpl> prefixes = new HashMap<String, NamespaceImpl>();
		for (Object[] row : table.selectAll()) {
			String prefix = (String)row[0];
			String namespace = (String)row[1];
			NamespaceImpl ns = new NamespaceImpl(prefix, namespace);
			map.put(namespace, ns);
			if (prefix != null) {
				prefixes.put(prefix, ns);
			}
		}
		byNamespace.putAll(map);
		byPrefix.clear();
		byPrefix.putAll(prefixes);
	}

	private synchronized NamespaceImpl create(String namespace)
		throws SQLException
	{
		if (byNamespace.containsKey(namespace))
			return byNamespace.get(namespace);
		table.insert(null, namespace);
		NamespaceImpl ns = new NamespaceImpl(null, namespace);
		byNamespace.put(ns.getName(), ns);
		return ns;
	}

	public void setPrefix(String prefix, String name)
		throws RdbmsException
	{
		NamespaceImpl ns = findNamespace(name);
		try {
			table.updatePrefix(prefix, name);
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		ns.setPrefix(prefix);
		byPrefix.put(prefix, ns);
	}

	public NamespaceImpl findByPrefix(String prefix) {
		return byPrefix.get(prefix);
	}

	public void removePrefix(String prefix)
		throws RdbmsException
	{
		NamespaceImpl ns = findByPrefix(prefix);
		if (ns == null)
			return;
		try {
			table.updatePrefix(prefix, ns.getName());
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		ns.setPrefix(null);
		byPrefix.remove(prefix);
	}

	public Collection<? extends Namespace> getNamespacesWithPrefix()
		throws RdbmsException
	{
		return byPrefix.values();
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
