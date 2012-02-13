/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.Dataset;

/**
 * Abstract superclass of all operations that can be formulated in a query
 * language and parsed by the query parser.
 * 
 * @author Jeen Broekstra
 */
public abstract class ParsedOperation {

	/**
	 * The dataset that was specified in the operation, if any.
	 */
	private Dataset dataset;

	/**
	 * The source string (e.g. SPARQL query) that produced this operation.
	 */
	private final String sourceString;

	public ParsedOperation() {
		this(null);
	}
	
	public ParsedOperation(String sourceString) {
		super();
		this.sourceString = sourceString;
	}
	
	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return Returns the sourceString.
	 */
	public String getSourceString() {
		return sourceString;
	}

}
