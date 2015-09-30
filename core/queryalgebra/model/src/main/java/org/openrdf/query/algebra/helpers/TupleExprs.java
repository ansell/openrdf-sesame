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
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.TupleExpr;

/**
 * Utility methods for {@link TupleExpr} objects.
 * 
 * @author Jeen Broekstra
 * @since 2.7.3
 */
public class TupleExprs {

	/**
	 * Verifies if the supplied {@link TupleExpr} contains a {@link Projection}.
	 * If the supplied TupleExpr is a {@link Join} or contains a {@link Join},
	 * projections inside that Join's arguments will not be taken into
	 * account.
	 * 
	 * @param t
	 *        a tuple expression.
	 * @return <code>true</code> if the TupleExpr contains a projection (outside
	 *         of a Join), <code>false</code> otherwise.
	 * @since 2.7.3
	 */
	public static boolean containsProjection(TupleExpr t) {
		@SuppressWarnings("serial")
		class VisitException extends Exception {
		}
		final boolean[] result = new boolean[1];
		try {
			t.visit(new AbstractQueryModelVisitor<VisitException>() {

				@Override
				public void meet(Projection node)
					throws VisitException
				{
					result[0] = true;
					throw new VisitException();
				}

				@Override
				public void meet(Join node)
					throws VisitException
				{
					// projections already inside a Join need not be
					// taken into account
					result[0] = false;
					throw new VisitException();
				}
			});
		}
		catch (VisitException ex) {
			// Do nothing. We have thrown this exception on the first
			// meeting of Projection or Join.
		}
		return result[0];
	}
}
