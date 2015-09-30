/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.repository.sail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.IRI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Dataset;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.AbstractOperation;
import org.openrdf.query.impl.SimpleDataset;
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

				executor.executeUpdate(updateExpr, activeDataset, getBindings(), true, getMaxExecutionTime());

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
			SimpleDataset mergedDataset = new SimpleDataset();

			boolean merge = false;

			Set<IRI> dgs = sparqlDefinedDataset.getDefaultGraphs();
			if (dgs != null && dgs.size() > 0) {
				merge = true;
				// one or more USING-clauses in the update itself, we need to define
				// the default graphs by means of the update itself
				for (IRI graphURI : dgs) {
					mergedDataset.addDefaultGraph(graphURI);
				}
			}

			Set<IRI> ngs = sparqlDefinedDataset.getNamedGraphs();
			if (ngs != null && ngs.size() > 0) {
				merge = true;
				// one or more USING NAMED-claused in the update, we need to define
				// the named graphs by means of the update itself.
				for (IRI graphURI : ngs) {
					mergedDataset.addNamedGraph(graphURI);
				}
			}

			if (merge) {
				mergedDataset.setDefaultInsertGraph(dataset.getDefaultInsertGraph());

				for (IRI graphURI : dataset.getDefaultRemoveGraphs()) {
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
