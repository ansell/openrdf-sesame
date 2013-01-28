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
package org.openrdf.sail.helpers;

import org.openrdf.sail.Sail;
import org.openrdf.sail.StackableSail;

/**
 * Defines utility methods for working with Sails.
 */
public class SailUtil {

	/**
	 * Searches a stack of Sails from top to bottom for a Sail that is an
	 * instance of the suppied class or interface. The first Sail that matches
	 * (i.e. the one closest to the top) is returned.
	 * 
	 * @param topSail
	 *        The top of the Sail stack.
	 * @param sailClass
	 *        A class or interface.
	 * @return A Sail that is an instance of sailClass, or null if no such Sail
	 *         was found.
	 */
	@SuppressWarnings("unchecked")
	public static <C extends Sail> C findSailInStack(Sail topSail, Class<C> sailClass) {
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

		return (C)currentSail;
	}
}
