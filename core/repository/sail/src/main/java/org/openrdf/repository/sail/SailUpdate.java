/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.Dataset;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.AbstractOperation;
import org.openrdf.query.impl.FallbackDataset;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Jeen Broekstra
 */
public class SailUpdate extends AbstractOperation implements Update {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
	 * Gets the "active" dataset for this update. The active dataset is either
	 * the dataset that has been specified using {@link #setDataset(Dataset)} or
	 * the dataset that has been specified in the update, where the former takes
	 * precedence over the latter.
	 * 
	 * @return The active dataset, or <tt>null</tt> if there is no dataset.
	 */
	public Dataset getActiveDataset() {
		if (dataset != null) {
			return FallbackDataset.fallback(dataset, parsedUpdate.getDataset());
		}

		// No external dataset specified, use update operation's own dataset (if
		// any)
		return parsedUpdate.getDataset();
	}

	@Override
	public String toString() {
		return parsedUpdate.toString();
	}

	public void execute()
		throws UpdateExecutionException
	{

		List<UpdateExpr> updateExprs = parsedUpdate.getUpdateExprs();

		for (UpdateExpr updateExpr : updateExprs) {
			// LOAD is handled at the Repository API level because it requires
			// access to the Rio parser.
			if (updateExpr instanceof Load) {

				Load load = (Load)updateExpr;

				Value source = load.getSource().getValue();
				Value graph = load.getGraph() != null ? load.getGraph().getValue() : null;

				SailRepositoryConnection conn = getConnection();
				try {
					URL sourceURL = new URL(source.stringValue());

					if (graph == null) {
						conn.add(sourceURL, source.stringValue(), null);
					}
					else {
						conn.add(sourceURL, source.stringValue(), null, (Resource)graph);
					}
				}
				catch (Exception e) {
					logger.warn("exception during update execution: ", e);
					if (!load.isSilent()) {
						throw new UpdateExecutionException(e);
					}
				}
			}
			else {
				// pass update operation to the SAIL.
				SailConnection conn = getConnection().getSailConnection();

				try {
					conn.executeUpdate(updateExpr, getActiveDataset(), getBindings(), true);
					getConnection().autoCommit();
				}
				catch (SailException e) {
					logger.warn("exception during update execution: ", e);
					if (!updateExpr.isSilent()) {
						throw new UpdateExecutionException(e);
					}
				}
				catch (RepositoryException e) {
					logger.warn("exception during update execution: ", e);
					if (!updateExpr.isSilent()) {
						throw new UpdateExecutionException(e);
					}
				}
			}
		}
	}
}
