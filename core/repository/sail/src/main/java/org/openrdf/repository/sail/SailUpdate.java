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

import org.openrdf.OpenRDFException;
import org.openrdf.query.Dataset;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.impl.AbstractParserUpdate;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.helpers.SailUpdateExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeen Broekstra
 */
public class SailUpdate extends AbstractParserUpdate {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final SailRepositoryConnection con;

	protected SailUpdate(ParsedUpdate parsedUpdate, SailRepositoryConnection con) {
		super(parsedUpdate);
		this.con = con;
	}

	protected SailRepositoryConnection getConnection() {
		return con;
	}

	@Override
	public void execute()
		throws UpdateExecutionException
	{
		ParsedUpdate parsedUpdate = getParsedUpdate();
		List<UpdateExpr> updateExprs = parsedUpdate.getUpdateExprs();
		Map<UpdateExpr, Dataset> datasetMapping = parsedUpdate.getDatasetMapping();

		SailUpdateExecutor executor = new SailUpdateExecutor(con.getSailConnection(), con.getValueFactory(), con.getParserConfig());

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

	private boolean isLocalTransaction()
		throws RepositoryException
	{
		return !getConnection().isActive();
	}

	private void beginLocalTransaction()
		throws RepositoryException
	{
		getConnection().begin();
	}

	private void commitLocalTransaction()
		throws RepositoryException
	{
		getConnection().commit();
	}
}
