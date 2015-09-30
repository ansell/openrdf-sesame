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
package org.openrdf;

import org.openrdf.model.Resource;

/**
 * General utility methods for OpenRDF/Sesame modules.
 * 
 * @author Arjohn Kampman
 */
public class OpenRDFUtil {

	/**
	 * Verifies that the supplied contexts parameter is not <tt>null</tt>,
	 * throwing an {@link IllegalArgumentException} if it is.
	 * <p>
	 * The semantics of supplying <tt>null</tt> as the value of the
	 * <tt>contexts</tt> vararg is not completely clear; it can either be
	 * equivalent to supplying an empty array (i.e.: matching all statements
	 * disregarding context), or to supplying a <tt>null</tt>-Resource value
	 * (e.g.: matching all statements with no associated context). As we so far
	 * haven't been able to prefer one over the other, methods operating on
	 * contexts currently throw {@link IllegalArgumentException}s.
	 * 
	 * @param contexts
	 *        The parameter to check.
	 * @throws IllegalArgumentException
	 *         If the supplied contexts parameter is <tt>null</tt>.
	 */
	public static void verifyContextNotNull(Resource... contexts) {
		if (contexts == null) {
			throw new IllegalArgumentException(
					"Illegal value null array for contexts argument; either the value should be cast to Resource or an empty array should be supplied");
		}
	}
}
