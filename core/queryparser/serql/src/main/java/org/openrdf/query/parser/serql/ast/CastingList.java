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

package org.openrdf.query.parser.serql.ast;

import java.util.AbstractList;
import java.util.List;

/**
 * A list that wraps another list and casts its elements to a specific subtype
 * of the list's element type.
 */
class CastingList<E> extends AbstractList<E> {

	protected List<? super E> _elements;

	public CastingList(List<? super E> elements) {
		_elements = elements;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		return (E) _elements.get(index);
	}

	@Override
	public int size() {
		return _elements.size();
	}
}
