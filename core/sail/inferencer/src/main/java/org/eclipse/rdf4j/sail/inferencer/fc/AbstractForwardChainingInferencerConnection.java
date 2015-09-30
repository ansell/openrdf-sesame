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
package org.eclipse.rdf4j.sail.inferencer.fc;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.SailConnectionListener;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.UnknownSailTransactionStateException;
import org.eclipse.rdf4j.sail.inferencer.InferencerConnection;
import org.eclipse.rdf4j.sail.inferencer.InferencerConnectionWrapper;
import org.eclipse.rdf4j.sail.model.SailModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractForwardChainingInferencerConnection extends InferencerConnectionWrapper implements
		SailConnectionListener
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
		super.flushUpdates();

		if (statementsRemoved) {
			logger.debug("statements removed, starting inferencing from scratch");
			clearInferred();
			addAxiomStatements();

			newStatements = new SailModel(getWrappedConnection(), true);

			statementsRemoved = false;
		}

		if(hasNewStatements()) {
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
	protected abstract void addAxiomStatements() throws SailException;

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

	protected abstract int applyRules(Model iteration) throws SailException;

	protected Model prepareIteration() {
		Model newThisIteration = newStatements;
		newStatements = null;
		return newThisIteration;
	}

	protected boolean hasNewStatements() {
		return newStatements != null && !newStatements.isEmpty();
	}
}
