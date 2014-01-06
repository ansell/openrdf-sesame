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
	 * Snapshot Read: in addition to {@link IsolationLevel#READ_COMMITTED}, query
	 * results in this isolation level that are observed within a successful
	 * transaction will observe a consistent snapshot. Changes to the data
	 * occurring while a query is evaluated will not affect that query result.
	 */
	SNAPSHOT_READ(READ_COMMITTED, READ_UNCOMMITTED, NONE),

	/**
	 * Snapshot: in addition to {@link IsolationLevel#SNAPSHOT_READ}, successful
	 * transactions in this isolation level will view a consistent snapshot. This
	 * isolation level will observe either the complete effects of other
	 * change-sets and their dependencies or no effects of other change-sets.
	 */
	SNAPSHOT(SNAPSHOT_READ, READ_COMMITTED, READ_UNCOMMITTED, NONE),

	/**
	 * Serializable: in addition to {@link IsolationLevel#SNAPSHOT}, this
	 * isolation level requires that all other successful transactions must
	 * appear to occur either completely before or completely after a successful
	 * serializable transaction.
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
}
