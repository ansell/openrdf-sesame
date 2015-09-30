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
