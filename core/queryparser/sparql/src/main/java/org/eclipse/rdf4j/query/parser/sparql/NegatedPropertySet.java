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
package org.eclipse.rdf4j.query.parser.sparql;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.StatementPattern.Scope;

/**
 * A negated property set is a SPARQL construction of the form {?X !(uri|^uri)
 * ?Y}. This class is a temporary representation used by the parser. It is
 * converted by the TupleExprBuilder into a set of joins and filters on regular
 * statement patterns.
 * 
 * @author Jeen
 */
public class NegatedPropertySet {

	private Scope scope;

	private Var subjectVar;

	private List<ValueExpr> objectList;

	private Var contextVar;

	private List<PropertySetElem> propertySetElems = new ArrayList<PropertySetElem>();

	/**
	 * @param scope
	 *        The scope to set.
	 */
	public void setScope(Scope scope) {
		this.scope = scope;
	}

	/**
	 * @return Returns the scope.
	 */
	public Scope getScope() {
		return scope;
	}

	/**
	 * @param subjectVar
	 *        The subjectVar to set.
	 */
	public void setSubjectVar(Var subjectVar) {
		this.subjectVar = subjectVar;
	}

	/**
	 * @return Returns the subjectVar.
	 */
	public Var getSubjectVar() {
		return subjectVar;
	}

	/**
	 * @param objectList
	 *        The objectList to set.
	 */
	public void setObjectList(List<ValueExpr> objectList) {
		this.objectList = objectList;
	}

	/**
	 * @return Returns the objectList.
	 */
	public List<ValueExpr> getObjectList() {
		return objectList;
	}

	/**
	 * @param contextVar
	 *        The contextVar to set.
	 */
	public void setContextVar(Var contextVar) {
		this.contextVar = contextVar;
	}

	/**
	 * @return Returns the contextVar.
	 */
	public Var getContextVar() {
		return contextVar;
	}

	/**
	 * @param propertySetElems
	 *        The propertySetElems to set.
	 */
	public void setPropertySetElems(List<PropertySetElem> propertySetElems) {
		this.propertySetElems = propertySetElems;
	}

	/**
	 * @return Returns the propertySetElems.
	 */
	public List<PropertySetElem> getPropertySetElems() {
		return propertySetElems;
	}

	/**
	 * @param elem
	 */
	public void addPropertySetElem(PropertySetElem elem) {
		propertySetElems.add(elem);

	}

}
