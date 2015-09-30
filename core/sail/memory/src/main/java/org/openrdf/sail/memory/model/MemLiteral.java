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
package org.openrdf.sail.memory.model;

import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleLiteral;
import org.openrdf.model.vocabulary.XMLSchema;

/**
 * A MemoryStore-specific extension of Literal giving it node properties.
 * 
 * @author Arjohn Kampman
 */
public class MemLiteral extends SimpleLiteral implements MemValue {

	private static final long serialVersionUID = 4288477328829845024L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The object that created this MemLiteral.
	 */
	transient private final Object creator;

	/**
	 * The list of statements for which this MemLiteral is the object.
	 */
	transient private volatile MemStatementList objectStatements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new Literal which will get the supplied label.
	 * 
	 * @param creator
	 *        The object that is creating this MemLiteral.
	 * @param label
	 *        The label for this literal.
	 */
	public MemLiteral(Object creator, String label) {
		super(label, XMLSchema.STRING);
		this.creator = creator;
	}

	/**
	 * Creates a new Literal which will get the supplied label and language code.
	 * 
	 * @param creator
	 *        The object that is creating this MemLiteral.
	 * @param label
	 *        The label for this literal.
	 * @param lang
	 *        The language code of the supplied label.
	 */
	public MemLiteral(Object creator, String label, String lang) {
		super(label, lang);
		this.creator = creator;
	}

	/**
	 * Creates a new Literal which will get the supplied label and datatype.
	 * 
	 * @param creator
	 *        The object that is creating this MemLiteral.
	 * @param label
	 *        The label for this literal.
	 * @param datatype
	 *        The datatype of the supplied label.
	 */
	public MemLiteral(Object creator, String label, IRI datatype) {
		super(label, datatype);
		this.creator = creator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Object getCreator() {
		return creator;
	}

	public boolean hasStatements() {
		return objectStatements != null;
	}

	public MemStatementList getObjectStatementList() {
		if (objectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return objectStatements;
		}
	}

	public int getObjectStatementCount() {
		if (objectStatements == null) {
			return 0;
		}
		else {
			return objectStatements.size();
		}
	}

	public void addObjectStatement(MemStatement st) {
		if (objectStatements == null) {
			objectStatements = new MemStatementList(1);
		}

		objectStatements.add(st);
	}

	public void removeObjectStatement(MemStatement st) {
		objectStatements.remove(st);

		if (objectStatements.isEmpty()) {
			objectStatements = null;
		}
	}

	public void cleanSnapshotsFromObjectStatements(int currentSnapshot) {
		if (objectStatements != null) {
			objectStatements.cleanSnapshots(currentSnapshot);

			if (objectStatements.isEmpty()) {
				objectStatements = null;
			}
		}
	}
}
