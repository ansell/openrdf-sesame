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
package org.eclipse.rdf4j;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.SESAME;

/**
 * Enumeration of Transaction {@link IsolationLevel}s supported by Sesame. Note
 * that Sesame stores are not required to support all levels, consult the
 * documentatation for the specific SAIL implementation you are using to find
 * out which levels are supported.
 * 
 * @author Jeen Broekstra
 * @author James Leigh
 * @since 2.8.0
 */
public enum IsolationLevels implements IsolationLevel {

	/**
	 * None: the lowest isolation level; transactions can see their own changes,
	 * but may not be able to roll them back and no support for isolation among
	 * transactions is guaranteed
	 */
	NONE,

	/**
	 * Read Uncommitted: transactions can be rolled back, but not necessarily
	 * isolated: concurrent transactions might see each other's uncommitted data
	 * (so-called 'dirty reads')
	 */
	READ_UNCOMMITTED(NONE),

	/**
	 * Read Committed: in this isolation level only statements from other
	 * transactions that have been committed (at some point) can be seen by this
	 * transaction.
	 */
	READ_COMMITTED(READ_UNCOMMITTED, NONE),

	/**
	 * Snapshot Read: in addition to {@link #READ_COMMITTED}, query results in
	 * this isolation level that are observed within a successful transaction
	 * will observe a consistent snapshot. Changes to the data occurring while a
	 * query is evaluated will not affect that query result.
	 */
	SNAPSHOT_READ(READ_COMMITTED, READ_UNCOMMITTED, NONE),

	/**
	 * Snapshot: in addition to {@link #SNAPSHOT_READ}, successful transactions
	 * in this isolation level will operate against a particular dataset
	 * snapshot. Transactions in this isolation level will see either the
	 * complete effects of other transactions (consistently throughout) or not at
	 * all.
	 */
	SNAPSHOT(SNAPSHOT_READ, READ_COMMITTED, READ_UNCOMMITTED, NONE),

	/**
	 * Serializable: in addition to {@link #SNAPSHOT}, this isolation level
	 * requires that all other successful transactions must appear to occur
	 * either completely before or completely after a successful serializable
	 * transaction.
	 */
	SERIALIZABLE(SNAPSHOT, SNAPSHOT_READ, READ_COMMITTED, READ_UNCOMMITTED, NONE);

	private final List<? extends IsolationLevels> compatibleLevels;

	private IsolationLevels(IsolationLevels... compatibleLevels) {
		this.compatibleLevels = Arrays.asList(compatibleLevels);
	}

	@Override
	public boolean isCompatibleWith(IsolationLevel otherLevel) {
		return this.equals(otherLevel) || compatibleLevels.contains(otherLevel);
	}

	/**
	 * Determines the first compatible isolation level in the list of supported
	 * levels, for the given level. Returns the level itself if it is in the list
	 * of supported levels. Returns null if no compatible level can be found.
	 * 
	 * @param level
	 *        the {@link IsolationLevel} for which to determine a compatible
	 *        level.
	 * @param supportedLevels
	 *        a list of supported isolation levels from which to select the
	 *        closest compatible level.
	 * @return the given level if it occurs in the list of supported levels.
	 *         Otherwise, the first compatible level in the list of supported
	 *         isolation levels, or <code>null</code> if no compatible level can
	 *         be found.
	 * @since 2.8.0
	 * @throws IllegalArgumentException
	 *         if either one of the input parameters is <code>null</code>.
	 */
	public static IsolationLevel getCompatibleIsolationLevel(IsolationLevel level,
			List<? extends IsolationLevel> supportedLevels)
	{
		if (supportedLevels == null) {
			throw new IllegalArgumentException("list of supported levels may not be null");
		}
		if (level == null) {
			throw new IllegalArgumentException("level may not be null");
		}
		if (!supportedLevels.contains(level)) {
			IsolationLevel compatibleLevel = null;
			// see we if we can find a compatible level that is supported
			for (IsolationLevel supportedLevel : supportedLevels) {
				if (supportedLevel.isCompatibleWith(level)) {
					compatibleLevel = supportedLevel;
					break;
				}
			}

			return compatibleLevel;
		}
		else {
			return level;
		}
	}

	public IRI getURI() {
		final ValueFactory f = SimpleValueFactory.getInstance();
		return f.createIRI(SESAME.NAMESPACE, this.name());
	}
}
