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
package org.openrdf.sail.inferencer.fc;

import org.openrdf.IsolationLevel;
import org.openrdf.IsolationLevels;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnectionListener;
import org.openrdf.sail.SailException;
import org.openrdf.sail.UnknownSailTransactionStateException;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.inferencer.InferencerConnectionWrapper;
import org.openrdf.sail.model.SailModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractForwardChainingInferencerConnection extends InferencerConnectionWrapper
		implements SailConnectionListener
{

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*-----------*
	 * Variables *
	 *-----------*/

	private Sail sail;

	/**
	 * true if the base Sail reported removed statements.
	 */
	private boolean statementsRemoved;

	/**
	 * Contains the statements that have been reported by the base Sail as
	 */
	private Model newStatements;

	protected int totalInferred;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AbstractForwardChainingInferencerConnection(Sail sail, InferencerConnection con) {
		super(con);
		this.sail = sail;
		con.addConnectionListener(this);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// Called by base sail
	@Override
	public void statementAdded(Statement st) {
		if (statementsRemoved) {
			// No need to record, starting from scratch anyway
			return;
		}

		if (newStatements == null) {
			newStatements = createModel();
		}
		newStatements.add(st);
	}

	protected abstract Model createModel();

	// Called by base sail
	@Override
	public void statementRemoved(Statement st) {
		statementsRemoved = true;
		newStatements = null;
	}

	@Override
	public void flushUpdates()
		throws SailException
	{
		if (statementsRemoved) {
			logger.debug("statements removed, starting inferencing from scratch");
			clearInferred();
			super.flushUpdates();

			addAxiomStatements();
			super.flushUpdates();

			newStatements = new SailModel(getWrappedConnection(), true);
			statementsRemoved = false;
		}
		else {
			super.flushUpdates();
		}

		if (hasNewStatements()) {
			doInferencing();
		}

		newStatements = null;
	}

	@Override
	public void begin()
		throws SailException
	{
		this.begin(null);
	}

	@Override
	public void begin(IsolationLevel level)
		throws SailException
	{
		if (level == null) {
			level = sail.getDefaultIsolationLevel();
		}

		IsolationLevel compatibleLevel = IsolationLevels.getCompatibleIsolationLevel(level,
				sail.getSupportedIsolationLevels());
		if (compatibleLevel == null) {
			throw new UnknownSailTransactionStateException("Isolation level " + level
					+ " not compatible with this Sail");
		}
		super.begin(compatibleLevel);
	}

	@Override
	public void rollback()
		throws SailException
	{
		super.rollback();

		statementsRemoved = false;
		newStatements = null;
	}

	/**
	 * Adds all basic set of axiom statements from which the complete set can be
	 * inferred to the underlying Sail.
	 */
	protected abstract void addAxiomStatements()
		throws SailException;

	protected void doInferencing()
		throws SailException
	{
		// initialize some vars
		totalInferred = 0;
		int iteration = 0;

		while (hasNewStatements()) {
			iteration++;
			logger.debug("starting iteration " + iteration);
			Model newThisIteration = prepareIteration();

			int nofInferred = applyRules(newThisIteration);

			logger.debug("iteration " + iteration + " done; inferred " + nofInferred + " new statements");
			totalInferred += nofInferred;
		}
	}

	/**
	 * Returns the number of newly inferred statements.
	 */
	protected abstract int applyRules(Model iteration)
		throws SailException;

	protected Model prepareIteration() {
		Model newThisIteration = newStatements;
		newStatements = null;
		return newThisIteration;
	}

	protected boolean hasNewStatements() {
		return newStatements != null;
	}
}
