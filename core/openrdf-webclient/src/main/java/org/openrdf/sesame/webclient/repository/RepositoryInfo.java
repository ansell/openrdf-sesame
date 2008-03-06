/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RepositoryInfo {

	/** Logger for this class and subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	private String _location;

	private String _description;

	public void setLocation(String location) {
		_location = location;
	}

	public String getLocation() {
		return _location;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public String getDescription() {
		return _description;
	}

}
