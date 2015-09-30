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
package org.eclipse.rdf4j.sail.federation.algebra;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.query.algebra.TupleExpr;

/**
 * An abstract superclass for n-ary tuple operators which have one or more
 * arguments.
 */
public abstract class AbstractNaryTupleOperator extends AbstractNaryOperator<TupleExpr> implements TupleExpr {

	private static final long serialVersionUID = 4703129201150946366L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public AbstractNaryTupleOperator() {
		super();
	}

	/**
	 * Creates a new n-ary tuple operator.
	 */
	public AbstractNaryTupleOperator(TupleExpr... args) {
		super(args);
	}

	/**
	 * Creates a new n-ary tuple operator.
	 */
	public AbstractNaryTupleOperator(List<? extends TupleExpr> args) {
		super(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);

		for (TupleExpr arg : getArgs()) {
			bindingNames.addAll(arg.getBindingNames());
		}

		return bindingNames;
	}

	public Set<String> getAssuredBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);

		for (TupleExpr arg : getArgs()) {
			bindingNames.addAll(arg.getAssuredBindingNames());
		}

		return bindingNames;
	}

	@Override
	public AbstractNaryTupleOperator clone() { // NOPMD
		return (AbstractNaryTupleOperator)super.clone();
	}
}
