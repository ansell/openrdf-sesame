package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;
import java.util.Map;

import info.aduna.collections.LRUMap;

import org.openrdf.sail.rdbms.model.RdbmsURI;

public class PredicateManager {
	private UriManager uris;
	private Map<Long, String> predicates = new LRUMap<Long, String>(64);

	public void setUriManager(UriManager uris) {
		this.uris = uris;
	}

	public long getIdOfPredicate(RdbmsURI uri) throws SQLException {
		Long id = uris.getInternalId(uri);
		synchronized(predicates) {
			predicates.put(id, uri.stringValue());
		}
		return id;
	}

	public String getPredicateUri(long id) {
		synchronized(predicates) {
			return predicates.get(id);
		}
	}

	public void remove(long id) {
		synchronized(predicates) {
			predicates.remove(id);
		}
	}
}
