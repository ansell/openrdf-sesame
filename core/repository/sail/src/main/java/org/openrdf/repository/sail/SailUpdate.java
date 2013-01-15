/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Dataset;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.AbstractOperation;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.helpers.SailUpdateExecutor;
import org.openrdf.rio.RDFParseException;
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

	@Override
	public String toString() {
		return parsedUpdate.toString();
	}

	public void execute()
		throws UpdateExecutionException
	{
		List<UpdateExpr> updateExprs = parsedUpdate.getUpdateExprs();
		Map<UpdateExpr, Dataset> datasetMapping = parsedUpdate.getDatasetMapping();

		ValueFactory vf = getConnection().getValueFactory();
		SailConnection conn = getConnection().getSailConnection();
		SailUpdateExecutor executor = new SailUpdateExecutor(conn, vf, getConnection().getParserConfig());

		for (UpdateExpr updateExpr : updateExprs) {

			Dataset activeDataset = getMergedDataset(datasetMapping.get(updateExpr));

			try {
				boolean localTransaction = !getConnection().isActive();
				if (localTransaction) {
					getConnection().begin();
				}

				executor.executeUpdate(updateExpr, activeDataset, getBindings(), true);

				if (localTransaction) {
					getConnection().commit();
				}
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
			catch (RDFParseException e) {
				logger.warn("exception during update execution: ", e);
				if (!updateExpr.isSilent()) {
					throw new UpdateExecutionException(e);
				}
			}
			catch (IOException e) {
				logger.warn("exception during update execution: ", e);
				if (!updateExpr.isSilent()) {
					throw new UpdateExecutionException(e);
				}
			}
		}
	}

	protected Dataset getMergedDataset(Dataset sparqlDefinedDataset) {
		if (sparqlDefinedDataset == null) {
			return dataset;
		}
		else if (dataset == null) {
			return sparqlDefinedDataset;
		}
		else {
			DatasetImpl mergedDataset = new DatasetImpl();

			boolean merge = false;

			Set<URI> dgs = sparqlDefinedDataset.getDefaultGraphs();
			if (dgs != null && dgs.size() > 0) {
				merge = true;
				// one or more USING-clauses in the update itself, we need to define
				// the default graphs by means of the update itself
				for (URI graphURI : dgs) {
					mergedDataset.addDefaultGraph(graphURI);
				}
			}

			Set<URI> ngs = sparqlDefinedDataset.getNamedGraphs();
			if (ngs != null && ngs.size() > 0) {
				merge = true;
				// one or more USING NAMED-claused in the update, we need to define
				// the named graphs by means of the update itself.
				for (URI graphURI : ngs) {
					mergedDataset.addNamedGraph(graphURI);
				}
			}

			if (merge) {
				mergedDataset.setDefaultInsertGraph(dataset.getDefaultInsertGraph());

				for (URI graphURI : dataset.getDefaultRemoveGraphs()) {
					mergedDataset.addDefaultRemoveGraph(graphURI);
				}

				return mergedDataset;
			}
			else {
				return sparqlDefinedDataset;
			}

		}
	}
}
