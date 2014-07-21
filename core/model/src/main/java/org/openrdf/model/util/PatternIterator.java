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
package org.openrdf.model.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Excludes an Iterator<Statement> based on a given basic graph pattern.
 */
public class PatternIterator<S extends Statement> implements Iterator<S> {

	private final Iterator<S> filteredIter;

	private final Value subj;

	private final Value pred;

	private final Value obj;

	private final Value[] contexts;

	private S nextElement;

	private boolean nextCalled;

	public PatternIterator(Iterator<S> iter, Value subj, Value pred, Value obj, Value... contexts) {
		this.filteredIter = iter;
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
		this.contexts = notNull(contexts);
	}

	public boolean hasNext() {
		findNextElement();

		return nextElement != null;
	}

	public S next() {
		findNextElement();

		S result = nextElement;

		if (result != null) {
			nextElement = null;
			nextCalled = true;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	private void findNextElement() {
		while (nextElement == null && filteredIter.hasNext()) {
			S candidate = filteredIter.next();

			if (accept(candidate)) {
				nextElement = candidate;
			}
		}
	}

	public void remove() {
		if (!nextCalled)
			throw new IllegalStateException();
		filteredIter.remove();
	}

	/**
	 * Tests whether or not the specified statement should be returned by this
	 * iterator. All objects from the wrapped iterator pass through this method
	 * in the same order as they are coming from the wrapped iterator.
	 * 
	 * @param st
	 *        The statement to be tested.
	 * @return <tt>true</tt> if the object should be returned, <tt>false</tt>
	 *         otherwise.
	 * @throws X
	 */
	protected boolean accept(S st) {
		if (subj != null && !subj.equals(st.getSubject())) {
			return false;
		}
		if (pred != null && !pred.equals(((Statement)st).getPredicate())) {
			return false;
		}
		if (obj != null && !obj.equals(st.getObject())) {
			return false;
		}
		Resource stContext = st.getContext();
		if (contexts != null && contexts.length == 0) {
			// Any context matches
			return true;
		}
		else {
			// Accept if one of the contexts from the pattern matches
			for (Value context : notNull(contexts)) {
				if (context == null && stContext == null) {
					return true;
				}
				if (context != null && context.equals(stContext)) {
					return true;
				}
			}

			return false;
		}
	}

	private Value[] notNull(Value[] contexts) {
		if (contexts == null) {
			return new Resource[] { null };
		}
		return contexts;
	}
}
