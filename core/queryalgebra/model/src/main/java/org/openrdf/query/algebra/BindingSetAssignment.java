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
