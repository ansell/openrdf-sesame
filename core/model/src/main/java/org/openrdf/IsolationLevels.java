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
package org.openrdf;

import java.util.Arrays;
import java.util.List;

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

	/** None: the lowest isolation level; transactions are not supported */
	NONE,

	/**
	 * Read Uncommitted: transactions are suppported, but not isolated:
	 * concurrent transactions may see each other's uncommitted data (so-called
	 * 'dirty reads')
	 */
	READ_UNCOMMITTED,

	/**
	 * Read Committed: in this isolation level only statements that have been
	 * committed (at some point) can be seen by the transaction.
	 */
	READ_COMMITTED,

	/**
	 * Repeatable Read: in addition to {@link IsolationLevel#READ_COMMITTED},
	 * statements in this isolation level that are observed within a successful
	 * transaction will remain observable by the transaction until the end.
	 */
	REPEATABLE_READ(READ_COMMITTED),

	/**
	 * Snapshot: in addition to {@link IsolationLevel#REPEATABLE_READ},
	 * successful transactions in this isolation level will view a consistent
	 * snapshot. This isolation level will observe either the complete effects of
	 * other change-sets and their dependency or no effects of other change-sets.
	 */
	SNAPSHOT(REPEATABLE_READ, READ_COMMITTED),

	/**
	 * Serializable: in addition to {@link IsolationLevel#SNAPSHOT}, this
	 * isolation level requires that all other successful transactions must
	 * appear to occur either completely before or completely after a successful
	 * serializable transaction.
	 */
	SERIALIZABLE(SNAPSHOT, REPEATABLE_READ, READ_COMMITTED);

	private final List<? extends IsolationLevels> compatibleLevels;

	private IsolationLevels(IsolationLevels... compatibleLevels) {
		this.compatibleLevels = Arrays.asList(compatibleLevels);
	}

	@Override
	public boolean isCompatibleWith(IsolationLevel otherLevel) {
		return compatibleLevels.contains(otherLevel);
	}
}
