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
package org.eclipse.rdf4j.query.algebra.evaluation.iterator;

import java.io.Serializable;
import java.util.Objects;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

/**
 * Compact and efficient representation of a binding set for use as a key in
 * hash maps.
 * 
 * @author MJAHale
 */
public class BindingSetHashKey implements Serializable {

	private static final long serialVersionUID = 6407405580643353289L;

	public static final BindingSetHashKey EMPTY = new BindingSetHashKey(new Value[0]);

	private final Value[] values;

	private transient int hashcode;

	public static BindingSetHashKey create(String[] varNames, BindingSet bindings) {
		BindingSetHashKey key;
		int varNameSize = varNames.length;
		if (varNameSize > 0) {
			Value[] keyValues = new Value[varNameSize];
			for (int i = 0; i < varNameSize; i++) {
				Value value = bindings.getValue(varNames[i]);
				keyValues[i] = value;
			}
			key = new BindingSetHashKey(keyValues);
		}
		else {
			key = BindingSetHashKey.EMPTY;
		}
		return key;
	}

	private BindingSetHashKey(Value[] values) {
		this.values = values;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof BindingSetHashKey)) {
			return false;
		}

		BindingSetHashKey jk = (BindingSetHashKey)o;
		if (this.values.length != jk.values.length) {
			return false;
		}

		for (int i = values.length - 1; i >= 0; i--) {
			if (!Objects.equals(this.values[i], jk.values[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (hashcode == 0) {
			hashcode = Objects.hash((Object[])values);
		}
		return hashcode;
	}

}
