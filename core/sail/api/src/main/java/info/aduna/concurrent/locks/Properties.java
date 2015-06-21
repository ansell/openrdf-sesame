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

package info.aduna.concurrent.locks;

/**
 * Class controlling various logging properties such as the amount of lock
 * tracking that is done for debugging (at the cost of performance).
 * 
 * @author Arjohn Kampman
 */
public class Properties {

	/**
	 * The system property "info.aduna.concurrent.locks.trackLocks" that can be
	 * used to enable lock tracking by giving it a (non-null) value.
	 */
	public static final String TRACK_LOCKS = "info.aduna.concurrent.locks.trackLocks";

	/**
	 * Sets of clears the {@link #TRACK_LOCKS} system property.
	 */
	public static void setLockTrackingEnabled(boolean trackLocks) {
		if (trackLocks) {
			System.setProperty(TRACK_LOCKS, "");
		}
		else {
			System.clearProperty(TRACK_LOCKS);
		}
	}

	public static boolean lockTrackingEnabled() {
		try {
			return System.getProperty(TRACK_LOCKS) != null;
		}
		catch (SecurityException e) {
			// Thrown when not allowed to read system properties, for example when
			// running in applets
			return false;
		}
	}
}
