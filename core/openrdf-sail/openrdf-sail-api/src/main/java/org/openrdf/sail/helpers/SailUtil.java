/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.helpers;

import org.openrdf.sail.Sail;
import org.openrdf.sail.StackableSail;

/**
 * Defines utility methods for working with Sails.
 */
public class SailUtil {

	/**
	 * Searches a stack of Sails from top to bottom for a Sail that is
	 * an instance of the suppied class or interface. The first Sail that
	 * matches (i.e. the one closest to the top) is returned.
	 *
	 * @param topSail The top of the Sail stack.
	 * @param sailClass A class or interface.
	 * @return A Sail that is an instance of sailClass, or null if no such
	 * Sail was found.
	 */
	public static Sail findSailInStack(Sail topSail, Class sailClass) {
		if (sailClass == null) {
			return null;
		}

		// Check Sails on the stack one by one, starting with the top-most.
		Sail currentSail = topSail;

		while (currentSail != null) {
			// Check current Sail
			if (sailClass.isInstance(currentSail)) {
				break;
			}

			// Current Sail is not an instance of sailClass, check the
			// rest of the stack
			if (currentSail instanceof StackableSail) {
				currentSail = ((StackableSail)currentSail).getBaseSail();
			}
			else {
				// We've reached the bottom of the stack
				currentSail = null;
			}
		}

		return currentSail;
	}
	

}
