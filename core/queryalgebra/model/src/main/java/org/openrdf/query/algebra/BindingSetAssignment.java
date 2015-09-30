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
package org.openrdf.query.algebra;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.query.BindingSet;

/**
 */
public class BindingSetAssignment extends AbstractQueryModelNode implements TupleExpr {

	private Set<String> bindingNames;
	private Iterable<BindingSet> bindingSets;
	
	@Override
	public Set<String> getBindingNames() {
		return getAssuredBindingNames();
	}

	@Override
	public Set<String> getAssuredBindingNames() {
		if(bindingNames == null) {
			bindingNames = findBindingNames();
		}
		return bindingNames;
	}

	private Set<String> findBindingNames() {
		Set<String> result = new HashSet<String>();
		if (bindingSets != null) {
			for (BindingSet set: bindingSets) {
				result.addAll(set.getBindingNames());
			}
		}
		return result;
	}

	@Override
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BindingSetAssignment;
	}

	@Override
	public int hashCode() {
		return "BindingSetAssignment".hashCode();
	}

	@Override
	public BindingSetAssignment clone() {
		return (BindingSetAssignment)super.clone();
	}

	/**
	 * @param bindingNames The bindingNames to set if known.
	 */
	public void setBindingNames(Set<String> bindingNames) {
		this.bindingNames = bindingNames;
	}

	/**
	 * @param bindingSets The bindingSets to set.
	 */
	public void setBindingSets(Iterable<BindingSet> bindingSets) {
		this.bindingSets = bindingSets;
	}

	/**
	 * @return Returns the bindingSets.
	 */
	public Iterable<BindingSet> getBindingSets() {
		return bindingSets;
	}

	@Override
	public String getSignature() {
		String signature = super.getSignature();
		
		signature += " (" + this.getBindingSets().toString() + ")";
		
		return signature;
	}
}
