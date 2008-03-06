/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;

public class RepositoryInfo {

	private String id;

	private String location;

	private String description;

	private boolean readable;

	private boolean writable;

	private Repository repository;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public static Map<String, RepositoryInfo> getAll(HTTPClient httpClient, boolean includeSystemRepo)
		throws RepositoryException
	{
		Map<String, RepositoryInfo> result = new TreeMap<String, RepositoryInfo>();

		Logger logger = LoggerFactory.getLogger(RepositoryInfo.class);

		try {
			TupleQueryResult responseFromServer = httpClient.getRepositoryList();
			while (responseFromServer.hasNext()) {
				BindingSet bindingSet = responseFromServer.next();
				RepositoryInfo repInfo = new RepositoryInfo();

				String uri = bindingSet.getValue("uri").toString();
				String id = ((Literal)bindingSet.getValue("id")).getLabel();

				// FIXME: check should really be on SystemRepository.ID, but that
				// introduces unwanted dependency
				if (includeSystemRepo || !("SYSTEM".equals(id))) {
					Value title = bindingSet.getValue("title");
					String description = null;
					if (title instanceof Literal) {
						description = ((Literal)title).getLabel();
					}
					boolean readable = ((Literal)bindingSet.getValue("readable")).booleanValue();
					boolean writable = ((Literal)bindingSet.getValue("writable")).booleanValue();

					repInfo.setLocation(uri);
					repInfo.setId(id);
					repInfo.setDescription(description);
					repInfo.setReadable(readable);
					repInfo.setWritable(writable);

					result.put(repInfo.getId(), repInfo);
				}
			}
		}
		catch (IOException ioe) {
			logger.warn("Unable to retrieve list of repositories", ioe);
			result = null;
		}
		catch (QueryEvaluationException e) {
			logger.warn("Unable to retrieve list of repositories", e);
			result = null;
		}

		return result;
	}
}
