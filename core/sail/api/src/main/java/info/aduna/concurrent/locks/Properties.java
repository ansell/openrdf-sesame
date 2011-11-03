/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
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
