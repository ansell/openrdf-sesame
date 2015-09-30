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
package org.openrdf.http.protocol.transaction.operations;

import info.aduna.lang.ObjectUtil;

import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;

/**
 * A context operation with (optional) subject, predicate, object.
 * 
 * @author Arjohn Kampman
 * @author Leo Sauermann
 */
public abstract class StatementOperation extends ContextOperation {

	private Resource subject;

	private IRI predicate;

	private Value object;

	protected StatementOperation(Resource... contexts) {
		super(contexts);
	}

	public Resource getSubject() {
		return subject;
	}

	public void setSubject(Resource subject) {
		this.subject = subject;
	}

	public IRI getPredicate() {
		return predicate;
	}

	public void setPredicate(IRI predicate) {
		this.predicate = predicate;
	}

	public Value getObject() {
		return object;
	}

	public void setObject(Value object) {
		this.object = object;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof StatementOperation) {
			StatementOperation o = (StatementOperation)other;

			return ObjectUtil.nullEquals(getSubject(), o.getSubject())
					&& ObjectUtil.nullEquals(getPredicate(), o.getPredicate())
					&& ObjectUtil.nullEquals(getObject(), o.getObject())
					&& super.equals(other);
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int hashCode = ObjectUtil.nullHashCode(getSubject());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getPredicate());
		hashCode = 31 * hashCode + ObjectUtil.nullHashCode(getObject());
		hashCode = 31 * hashCode + super.hashCode();
		return hashCode;
	}
}
