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
package org.openrdf.query.algebra.evaluation.iterator;

import java.io.Serializable;
import java.util.Objects;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

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
