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
package org.openrdf.sail.rdbms.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Namespace;
import org.openrdf.model.impl.SimpleNamespace;
import org.openrdf.sail.rdbms.exceptions.RdbmsException;
import org.openrdf.sail.rdbms.schema.NamespacesTable;

/**
 * Manages the namespace prefixes.
 * 
 * @author James Leigh
 * 
 */
public class NamespaceManager {

	private Map<String, SimpleNamespace> byNamespace = new ConcurrentHashMap<String, SimpleNamespace>();

	private Map<String, SimpleNamespace> byPrefix = new ConcurrentHashMap<String, SimpleNamespace>();

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

	public SimpleNamespace findNamespace(String namespace)
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
		Map<String, SimpleNamespace> map = new HashMap<String, SimpleNamespace>();
		Map<String, SimpleNamespace> prefixes = new HashMap<String, SimpleNamespace>();
		for (Object[] row : table.selectAll()) {
			String prefix = (String)row[0];
			String namespace = (String)row[1];
			if (namespace == null)
				continue;
			SimpleNamespace ns = new SimpleNamespace(prefix, namespace);
			map.put(namespace, ns);
			if (prefix != null) {
				prefixes.put(prefix, ns);
			}
		}
		byNamespace.putAll(map);
		byPrefix.clear();
		byPrefix.putAll(prefixes);
	}

	private synchronized SimpleNamespace create(String namespace)
		throws SQLException
	{
		if (byNamespace.containsKey(namespace))
			return byNamespace.get(namespace);
		table.insert(null, namespace);
		SimpleNamespace ns = new SimpleNamespace(null, namespace);
		byNamespace.put(ns.getName(), ns);
		return ns;
	}

	public void setPrefix(String prefix, String name)
		throws RdbmsException
	{
		SimpleNamespace ns = findNamespace(name);
		try {
			table.updatePrefix(prefix, name);
		}
		catch (SQLException e) {
			throw new RdbmsException(e);
		}
		ns.setPrefix(prefix);
		byPrefix.put(prefix, ns);
	}

	public SimpleNamespace findByPrefix(String prefix) {
		return byPrefix.get(prefix);
	}

	public void removePrefix(String prefix)
		throws RdbmsException
	{
		SimpleNamespace ns = findByPrefix(prefix);
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
