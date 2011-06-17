/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.query.Dataset;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.AbstractOperation;
import org.openrdf.query.parser.ParsedModify;
import org.openrdf.query.parser.ParsedUpdate;

/**
 * @author Jeen Broekstra
 */
public class SailUpdate extends AbstractOperation implements Update {

	private final ParsedUpdate parsedUpdate;

	private final SailRepositoryConnection con;

	protected SailUpdate(ParsedUpdate parsedUpdate, SailRepositoryConnection con) {
		this.parsedUpdate = parsedUpdate;
		this.con = con;
	}

	public ParsedUpdate getParsedUpdate() {
		return parsedUpdate;
	}

	protected SailRepositoryConnection getConnection() {
		return con;
	}
	
	/**
	 * Gets the "active" dataset for this update. The active dataset is either the
	 * dataset that has been specified using {@link #setDataset(Dataset)} or the
	 * dataset that has been specified in the update, where the former takes
	 * precedence over the latter.
	 * 
	 * @return The active dataset, or <tt>null</tt> if there is no dataset.
	 */
	/*
	public Dataset getActiveDataset() {
		if (dataset != null) {
			return dataset;
		}

		// No external dataset specified, use update operation's own dataset (if any)
		return parsedUpdate.getDataset();
	}
	*/

	@Override
	public String toString() {
		return parsedUpdate.toString();
	}

	public void execute()
		throws UpdateExecutionException
	{
		
		if (parsedUpdate instanceof ParsedModify) {
			// TODO
		}
	}

}
