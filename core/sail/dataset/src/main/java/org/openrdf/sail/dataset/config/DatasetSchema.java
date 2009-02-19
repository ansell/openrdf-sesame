/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.dataset.config;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 * @author James Leigh
 */
public class DatasetSchema {

	/** http://www.openrdf.org/config/sail/dataset# */
	public static final String NAMESPACE = "http://www.openrdf.org/config/sail/dataset#";

	public static final URI GRAPH = new URIImpl(NAMESPACE + "graph");

	public static final URI NAME = new URIImpl(NAMESPACE + "name");

	/**
	 * Absent dataset indicates the graph is known, but will be loaded
	 * externally.
	 */
	public static final URI DATASET = new URIImpl(NAMESPACE + "dataset");

	/** These are the only datasets that this repository can load. */
	public static final URI CLOSED = new URIImpl(NAMESPACE + "closed");

	private DatasetSchema() {
		// no constructor
	}
}
