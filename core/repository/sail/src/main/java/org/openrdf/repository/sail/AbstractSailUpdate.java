/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.repository.sail;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Dataset;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.AbstractUpdate;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.repository.sail.helpers.SailUpdateExecutor;
import org.openrdf.rio.ParserConfig;
import org.openrdf.sail.SailConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeen Broekstra
 */
public abstract class AbstractSailUpdate extends AbstractUpdate {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ParsedUpdate parsedUpdate;

	private final SailConnection con;
	private final ValueFactory vf;
	private final ParserConfig parserConfig;

	protected AbstractSailUpdate(ParsedUpdate parsedUpdate, SailConnection con, ValueFactory vf, ParserConfig parserConfig) {
		this.parsedUpdate = parsedUpdate;
		this.con = con;
		this.vf = vf;
		this.parserConfig = parserConfig;
	}

	public ParsedUpdate getParsedUpdate() {
		return parsedUpdate;
	}

	protected SailConnection getSailConnection() {
		return con;
	}

	@Override
	public String toString() {
		return parsedUpdate.toString();
	}

	@Override
	public void execute()
		throws UpdateExecutionException
	{
		List<UpdateExpr> updateExprs = parsedUpdate.getUpdateExprs();
		Map<UpdateExpr, Dataset> datasetMapping = parsedUpdate.getDatasetMapping();

		SailUpdateExecutor executor = new SailUpdateExecutor(con, vf, parserConfig);

		for (UpdateExpr updateExpr : updateExprs) {

			Dataset activeDataset = getMergedDataset(datasetMapping.get(updateExpr));

			try {
				boolean localTransaction = isLocalTransaction();
				if (localTransaction) {
					beginLocalTransaction();
				}

				executor.executeUpdate(updateExpr, activeDataset, getBindings(), getIncludeInferred(), getMaxExecutionTime());

				if (localTransaction) {
					commitLocalTransaction();
				}
			}
			catch (OpenRDFException e) {
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

	protected abstract boolean isLocalTransaction() throws OpenRDFException;
	protected abstract void beginLocalTransaction() throws OpenRDFException;
	protected abstract void commitLocalTransaction() throws OpenRDFException;

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
