package org.openrdf.sail.rdbms.managers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.openrdf.sail.rdbms.model.RdbmsURI;

public class PredicateManager {
	private UriManager uris;
	private Map<String, Long> ids = new HashMap<String, Long>();
	private Map<Long, String> predicates = new HashMap();

	public void setUriManager(UriManager uris) {
		this.uris = uris;
	}

	public synchronized long getIdOfPredicate(RdbmsURI uri) throws SQLException {
		Long id = ids.get(uri.stringValue());
		if (id == null) {
			id = uris.getInternalId(uri);
			ids.put(uri.stringValue(), id);
			predicates.put(id, uri.stringValue());
		}
		return id;
	}

	public synchronized Long getIdIfPredicate(RdbmsURI uri) throws SQLException {
		return ids.get(uri.stringValue());
	}

	public synchronized String getPredicateUri(long id) {
		return predicates.get(id);
	}

	public synchronized void remove(long id) {
		if (predicates.containsKey(id)) {
			ids.remove(predicates.get(id));
			predicates.remove(id);
		}
	}
}
